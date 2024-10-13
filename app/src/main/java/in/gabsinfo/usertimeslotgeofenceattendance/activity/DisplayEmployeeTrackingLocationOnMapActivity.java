package in.gabsinfo.usertimeslotgeofenceattendance.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.Interface.LatLngInterpolator;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.LocationTrackingModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.MarkerAnimation;

//https://codinginfinite.com/android-example-animate-marker-map-current-location/
public class DisplayEmployeeTrackingLocationOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap googleMap = null;
    private PersonModel mPersonModel = null;
    private boolean firstTimeFlag = true;
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_employee_tracking_location_on_map);
        if (getIntent().getExtras() != null) {
            mPersonModel = (PersonModel) getIntent().getExtras().getSerializable("PersonModel");
        } else {
            finish();
        }

        setToolBar();
        try {
            // Loading map
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void setToolBar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(mPersonModel.getName());
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Drawable drawable = toolbar.getNavigationIcon();
        assert drawable != null;
        drawable.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        //googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.setTrafficEnabled(false);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);


        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setGravity(Gravity.CENTER);
                snippet.setTypeface(null, Typeface.NORMAL);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);
                return info;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_call, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_call) {
            if (mPersonModel.getMobileDialerCode() != null
                    && mPersonModel.getMobileDialerCode().length() > 0) {
                CommonMethods.call(this, mPersonModel.getMobileDialerCode().concat(mPersonModel.getMobileNo()));
            } else {
                CommonMethods.call(this, mPersonModel.getMobileNo());
            }
        }
        return true;
    }

    /**
     * function to load map. If map is not created it will create it for you
     */
    private void init() {
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map.getMapAsync(this);

        AttendanceApplication.refLocationTracking
                .child(mPersonModel.getFirebaseKey())
                .addChildEventListener(childEventListener);
    }

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (dataSnapshot.exists()) {
                LocationTrackingModel currentLocation = dataSnapshot.getValue(LocationTrackingModel.class);
                if (currentLocation == null)
                    return;

                if (firstTimeFlag && googleMap != null) {
                    animateCamera(currentLocation);
                    firstTimeFlag = false;
                }
                showMarker(currentLocation);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (dataSnapshot.exists()) {
                LocationTrackingModel currentLocation = dataSnapshot.getValue(LocationTrackingModel.class);
                if (currentLocation == null)
                    return;

                if (firstTimeFlag && googleMap != null) {
                    animateCamera(currentLocation);
                    firstTimeFlag = false;
                }
                showMarker(currentLocation);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    private void showMarker(@NonNull LocationTrackingModel currentLocation) {
        Date date_ = new Date();
        date_.setTime(currentLocation.getTime());
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocationMarker == null) {
            currentLocationMarker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title(new SimpleDateFormat(ConstantData.TWELVE_HOUR_DATE_FORMAT, Locale.US).format(date_))
                            .snippet("Last Updated")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_circle_black_24dp)));
        } else {
            currentLocationMarker.setTitle(new SimpleDateFormat(ConstantData.TWELVE_HOUR_DATE_FORMAT, Locale.US).format(date_));
            currentLocationMarker.setSnippet("Last Updated");
            MarkerAnimation.animateMarkerToGB(currentLocationMarker, latLng, new LatLngInterpolator.Spherical());
        }
        currentLocationMarker.showInfoWindow();
    }


    private void animateCamera(@NonNull LocationTrackingModel location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
    }

    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(18).build();
    }

    @Override
    protected void onDestroy() {
        if (childEventListener != null) {
            AttendanceApplication.refLocationTracking
                    .child(mPersonModel.getFirebaseKey())
                    .removeEventListener(childEventListener);
        }
        if (googleMap != null) {
            googleMap.clear();
            googleMap = null;
        }
        super.onDestroy();
        System.gc();
    }
}
