package in.gabsinfo.usertimeslotgeofenceattendance.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.SplashActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;


/*
//https://android.jlelse.eu/android-push-notification-using-firebase-and-advanced-rest-client-3858daff2f50
POST : https://fcm.googleapis.com/fcm/send
authorization : key=AAAApphKJaE:APA91bHak8z7Qlnxq0OYbY-Uwq3zmii7vTOcPmXLbbFEI2YNzm-LRtMooQxN03i2i7IWRq3yMoZ9L2YConekZbYuZbhxjLu9-4QhMotWSeVz5xMEPwXY7EGMug-yK52HgfXJ5caO_F-J
        content-type : application/json
{
        "data": {
        "title": "Hey",
        "notificationTitle": "Hey",
        "notificationContent": "Check Out This Awesome Game!",
        "notificationImageUrl":"";
        },
        "to": "fW9Mhkkoqrw:APA91bGieSp_bP9a9nRlT58jekF7TGNvVejvM35vDXq1fShOuWAwwSyesfp0pys_guHaXj2ayl_QJlpC6Ca6yXHR48637nX-3L3N49VbWqxFzRsmchCXPLVymfH32KuwqQv3XqI6pk_G"
        OR
        "to": "/topics/GABS_Admin" or
        "to": "/topics/GABS_Users" or
        "to": "/topics/AllCompanyUsers"
        }
*/

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onNewToken(String refreshedToken) {
        super.onNewToken(refreshedToken);

        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData()!=null)
            getImage(remoteMessage);
    }

    private void getImage(final RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        if (data.get("title").equalsIgnoreCase("CLEAR_APP_DATA")) {
            //Here we clear application SP
            SharePreferences.clearWholeApplicationSP();

            //Close application
            try {
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //To start or stop employee tracking
        else if (data.get("title").equalsIgnoreCase("TRACKING")) {
            String employeeFirebaseKey = data.get("notificationTitle");
            boolean employeeTrackingEnable = Boolean.valueOf(data.get("notificationContent"));
            String trackingDeviceId = data.get("notificationTrackingDeviceId");
            //Here we check logged-in user firebase key and firebase key which we got from notification
            //to identify where both are same user or not
            if (SharePreferences.getStr(SharePreferences.KEY_USER_FIREBASE_KEY, SharePreferences.DEFAULT_STRING)
                    .equalsIgnoreCase(employeeFirebaseKey)) {

                if (employeeTrackingEnable) {
                    if (SharePreferences.getStr(SharePreferences.KEY_TRACKING_DEVICE_ID, SharePreferences.DEFAULT_STRING)
                            .equalsIgnoreCase(trackingDeviceId)) {
                        SharePreferences.setBool(SharePreferences.KEY_IS_TRACKING_ENABLE, true);
                        CommonMethods.startTracking(MyFirebaseMessagingService.this);

                        //Here we notify user to enable GPS to track his location
                        if(!CommonMethods.checkGPSStatus(MyFirebaseMessagingService.this)){

                            Config.title = ConstantData.NOTIFICATION_TITLE_EXTRA;
                            Config.notificationTitle = "Enable Location";
                            Config.notificationContent = "Turn your location 'ON' to enjoy application features smoothly.";
                            sendNotification(null);
                        }
                    } else {
                        SharePreferences.setBool(SharePreferences.KEY_IS_TRACKING_ENABLE, false);
                        CommonMethods.stopTracking(MyFirebaseMessagingService.this);
                    }
                } else {
                    SharePreferences.setBool(SharePreferences.KEY_IS_TRACKING_ENABLE, false);
                    CommonMethods.stopTracking(MyFirebaseMessagingService.this);
                }
            }
        }else if (data.get("title").equalsIgnoreCase(ConstantData.NOTIFICATION_TITLE_EXTRA)) {
            Config.title = data.get("title");
            Config.notificationTitle = data.get("notificationTitle");
            Config.notificationContent = data.get("notificationContent");
            Config.notificationImageUrl = data.get("notificationImageUrl");
            if (Config.notificationTitle.contains("( NOTE )")) { //From Notes/Rules module
                Config.notificationTitle = Config.notificationTitle.replace("( NOTE )", "");
                sendNotification(BitmapFactory.decodeResource(
                        getResources(), R.drawable.img_module_notes));
            } else if (Config.notificationTitle.contains("( BROADCAST )")) {//From Broadcast Message module
                Config.notificationTitle = Config.notificationTitle.replace("( BROADCAST )", "");
                sendNotification(BitmapFactory.decodeResource(
                        getResources(), R.drawable.img_module_broadcast_message));
            } else if (Config.notificationTitle.contains("( COMMON )")) {//Message which is common for all employees and
                //message trigger only from REST client only.
                Config.notificationTitle = Config.notificationTitle.replace("( COMMON )", "");
                sendNotification(null);
            }
        }else if (Objects.requireNonNull(data.get("title")).equalsIgnoreCase(ConstantData.NOTIFICATION_TITLE_EMPLOYEE_IN)
                || Objects.requireNonNull(data.get("title")).equalsIgnoreCase(ConstantData.NOTIFICATION_TITLE_EMPLOYEE_OUT)) {

            Config.title = data.get("title");
            Config.notificationTitle = data.get("notificationTitle");
            Config.notificationContent = data.get("notificationContent");
            sendNotification(BitmapFactory.decodeResource(
                    getResources(), R.drawable.img_module_today_presence));

        }else {
            Config.title = data.get("title");
            Config.notificationTitle = data.get("notificationTitle");
            Config.notificationContent = data.get("notificationContent");
            Config.notificationImageUrl = data.get("notificationImageUrl");
            //Create thread to fetch image from notification
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Get image from data Notification
                    Picasso.get()
                            .load(Config.notificationImageUrl)
                            .into(new com.squareup.picasso.Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    sendNotification(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    sendNotification(null);
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                }
                            });
                }
            });
        }
    }


    private void sendNotification(Bitmap bitmap){


        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        }else{
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        }

        String NOTIFICATION_CHANNEL_ID = "";
        String NOTIFICATION_CHANNEL_NAME = "";
        String NOTIFICATION_CHANNEL_DESCRIPTION = "";
        if (Config.title.equalsIgnoreCase(ConstantData.NOTIFICATION_TITLE_NEW_UPDATE_AVAILABLE)) {
            NOTIFICATION_CHANNEL_ID = "51";
            NOTIFICATION_CHANNEL_NAME = ConstantData.NOTIFICATION_TITLE_NEW_UPDATE_AVAILABLE;
            NOTIFICATION_CHANNEL_DESCRIPTION = "New Application Update Is Available To Download.";
        }else if (Config.title.equalsIgnoreCase(ConstantData.NOTIFICATION_TITLE_EMPLOYEE_IN)) {
            NOTIFICATION_CHANNEL_ID = "53";
            NOTIFICATION_CHANNEL_NAME = ConstantData.NOTIFICATION_TITLE_EMPLOYEE_IN;
            NOTIFICATION_CHANNEL_DESCRIPTION = "Got notification when any selected employees mark their attendance.";
        }else if (Config.title.equalsIgnoreCase(ConstantData.NOTIFICATION_TITLE_EMPLOYEE_OUT)) {
            NOTIFICATION_CHANNEL_ID = "54";
            NOTIFICATION_CHANNEL_NAME = ConstantData.NOTIFICATION_TITLE_EMPLOYEE_OUT;
            NOTIFICATION_CHANNEL_DESCRIPTION = "Got notification when any selected employees mark their attendance.";
        }else if (Config.title.equalsIgnoreCase(ConstantData.NOTIFICATION_TITLE_EXTRA)) {
            NOTIFICATION_CHANNEL_ID = "55";
            NOTIFICATION_CHANNEL_NAME = ConstantData.NOTIFICATION_TITLE_EXTRA;
            NOTIFICATION_CHANNEL_DESCRIPTION = "Got notification when any common message from application developer company as well as Write Notes & BroadCast Message by Admin.";
        }



        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel
                    = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_MAX);

            //Configure Notification Channel
            notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            // Sets whether notifications posted to this channel should display notification lights
            notificationChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel
            notificationChannel.setLightColor(Color.BLUE);
            // Sets whether notification posted to this channel should vibrate.
            //notificationChannel.enableVibration(true);
            //notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = null;
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(Config.notificationContent);
        if (bitmap == null) {

            notificationBuilder =
                    new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_fcm_notification)
                            .setContentTitle(Config.notificationTitle)
                            .setAutoCancel(true)
                            .setSound(defaultSound)
                            .setContentIntent(pendingIntent)
                            .setStyle(style)
                            .setWhen(System.currentTimeMillis())
                            .setPriority(Notification.PRIORITY_MAX);

        } else {
            notificationBuilder =
                    new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_fcm_notification)
                            .setContentTitle(Config.notificationTitle)
                            .setAutoCancel(true)
                            .setSound(defaultSound)
                            .setContentIntent(pendingIntent)
                            .setStyle(style)
                            .setLargeIcon(bitmap)
                            .setWhen(System.currentTimeMillis())
                            .setPriority(Notification.PRIORITY_MAX);
        }

        /*NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_fcm_notification)
                        .setContentTitle(Config.notificationTitle)
                        .setAutoCancel(true)
                        .setSound(defaultSound)
                        .setContentIntent(pendingIntent)
                        .setStyle(style)
                        .setLargeIcon(bitmap)
                        .setWhen(System.currentTimeMillis())
                        .setPriority(Notification.PRIORITY_MAX);*/

        //If you wants to show notification icon
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.ic_fcm_notification);
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.ic_fcm_notification);
        }

        assert notificationManager != null;
        notificationManager.notify(new Random().nextInt(100000), notificationBuilder.build());
    }
}
