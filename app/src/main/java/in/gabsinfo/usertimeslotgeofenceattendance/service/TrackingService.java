package in.gabsinfo.usertimeslotgeofenceattendance.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.LocationTrackingModel;
import in.gabsinfo.usertimeslotgeofenceattendance.receiver.AutoStartReceiver;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

//https://github.com/JessicaThornsby/GPS-tracker
public class TrackingService extends Service {

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mClient;
    private LocationCallback mLocationCallback;
    //Get a reference to the database, so your app can perform read and write operations//
    private DatabaseReference ref = AttendanceApplication.refLocationTracking;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startLocationUpdates();
        createLocationCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(ConstantData.START_LOCATION_SERVICE)) {
                    buildNotification();
                    requestLocationUpdates();
                } else if (intent.getAction().equals(ConstantData.STOP_LOCATION_SERVICE)) {
                    stopForeground(true);
                    stopSelf();
                }
            }
        }
        return START_STICKY;
    }


    private void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        //Specify how often your app should request the device’s location//
        /* 2 Min */
        long UPDATE_INTERVAL = 4 * 1000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        /* 1 Min */
        long FASTEST_INTERVAL = 2 * 1000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        //Get the most accurate location data available//
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mClient.setMockMode(false);
        // Get last known recent location using new Google Play Services SDK (v11+)
        mClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    private void createLocationCallback() {

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                onLocationChanged(locationResult.getLastLocation());
            }
        };
    }

    private void onLocationChanged(Location location) {
        if (ref != null) {
            //Save the location data to the database//
            if (!SharePreferences.getBool(SharePreferences.KEY_IS_ADMIN_USER, SharePreferences.DEFAULT_BOOLEAN)
                    && SharePreferences.getStr(SharePreferences.KEY_USER_FIREBASE_KEY, SharePreferences.DEFAULT_STRING).trim().length() > 0) {
                ref
                        .child(SharePreferences.getStr(SharePreferences.KEY_USER_FIREBASE_KEY, SharePreferences.DEFAULT_STRING))
                        .child("123456789")//Don't remove this line.If you remove location not getting display to admin
                        .setValue(new LocationTrackingModel(location.getLatitude(), location.getLongitude(),
                                location.getTime(),"Brand - ".concat(Build.BRAND).concat(" ::: Model - ").concat(Build.MODEL)));
            }
        }
    }

    //Create the persistent notification//
    private void buildNotification() {
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);
        }else{
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ConstantData.CHANNEL_ID_FOR_LOCATION)
                .setContentTitle(getString(R.string.app_name)+" Running")
                .setContentText(getString(R.string.app_name)+" using location in background")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false);

        //If you wants to show notification icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_fcm_notification);
            builder.setColor(getResources().getColor(R.color.colorPrimaryDark));
        } else {
            builder.setSmallIcon(R.drawable.ic_fcm_notification);
        }
        Notification notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(ConstantData.CHANNEL_ID_FOR_LOCATION,
                    ConstantData.CHANNEL_NAME_FOR_LOCATION,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(false);
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(ConstantData.NOTIFICATION_ID_FOR_LOCATION, notification);
    }

    //Initiate the request to track the device's location//
    private void requestLocationUpdates() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        //If the app currently has access to the location permission...//
        if (permission == PackageManager.PERMISSION_GRANTED) {

            //...then request location updates//
            DatabaseReference ref = AttendanceApplication.refLocationTracking;
            assert mClient != null;
            mClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }


    @Override
    public void onDestroy() {
        if (mClient != null) {
            mClient.removeLocationUpdates(mLocationCallback);
        }

        stopForeground(true);
        stopSelf();

        //Start Tracking Service again
        Intent i = new Intent(this, AutoStartReceiver.class);
        i.setAction(AutoStartReceiver.START_TRACKING);
        sendBroadcast(i);

        super.onDestroy();
    }
}