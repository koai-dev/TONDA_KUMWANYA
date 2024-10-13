package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;

//http://rajeshvijayakumar.blogspot.com/2015/08/uber-like-setting-location-by-dragging.html
//Address based on location
public class FindCompanyBranchOnMapFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 15;
    private static final int PERMISSIONS_REQUEST_GNABLE_GPS = 16;
    private EditText mEtLocation, mEtLatitude, mEtLongitude;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mSelectedLocation = null;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;

    private onLocationSelectListener onLocationSelectListener;
    private TextView mTvHintForLocationSelect;
    private MarkerOptions mMarkerOptions;

    public interface onLocationSelectListener {
        public void SelectedLocation(LatLng location);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onLocationSelectListener = (onLocationSelectListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onLocationSelectListener = null;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemInfo = menu.findItem(R.id.item_info);
        if(itemInfo!=null)
            itemInfo.setVisible(false);

        MenuItem itemSubmit = menu.findItem(R.id.item_submit);
        if(itemSubmit!=null)
            itemSubmit.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_company_branch_on_map, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        setToolBar(view);
        init(view);
        return view;
    }


    private void setToolBar(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        //toolbar.setTitle(getString(R.string.toolbar_title_find_branch_in_map));
        toolbar.setTitle("");
        ((AdminDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
        Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        /**
         * Change back button color
         */
        try {
            final Drawable upArrow =  ContextCompat.getDrawable(getActivity(), R.drawable.ic_navigate_before_black);
            assert upArrow != null;
            upArrow.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.black), PorterDuff.Mode.SRC_ATOP);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setHomeAsUpIndicator(upArrow);
        } catch (Exception e) {
            e.printStackTrace();
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AdminDashboardActivity) requireActivity()).onBackPressed();
            }
        });


    }

    private void init(View view) {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);



        mTvHintForLocationSelect = view.findViewById(R.id.tv_hint_for_location_select);
        ImageView ivRefreshLocation = view.findViewById(R.id.iv_refresh_location);
        mEtLocation = view.findViewById(R.id.et_location);
        mEtLatitude = view.findViewById(R.id.et_latitude);
        mEtLongitude = view.findViewById(R.id.et_longitude);
        Button btnSaveLocation = view.findViewById(R.id.btn_user_location);

        ivRefreshLocation.setOnClickListener(this);
        btnSaveLocation.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(requireActivity())
                .addConnectionCallbacks(FindCompanyBranchOnMapFragment.this)
                .addOnConnectionFailedListener(FindCompanyBranchOnMapFragment.this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10 * 1000);    // 10 seconds, in milliseconds
        mLocationRequest.setFastestInterval(5 * 1000);   // 1 second, in milliseconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            showRationaleDialog();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.iv_refresh_location){
            if (mGoogleApiClient.isConnected()) {
                settingRequest();
            }
        }else if(v.getId()==R.id.btn_user_location){
            if (mSelectedLocation != null) {
                if(onLocationSelectListener!=null)
                    onLocationSelectListener.SelectedLocation(mSelectedLocation);
            } else {
                CommonMethods.showAlertDailogueWithOK(getActivity(),
                        getString(R.string.title_alert), getString(R.string.msg_select_brnach_location_from_map),
                        getString(R.string.action_ok));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        Drawable myDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.img_marker, null);
        Bitmap icon = myDrawable != null ? ((BitmapDrawable) myDrawable).getBitmap() : null;
        mMarkerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void onMapClick(LatLng latLng) {
        mTvHintForLocationSelect.setVisibility(View.GONE);
        mSelectedLocation = latLng;
        if (mMap != null) {
            mMap.clear();
            if (latLng != null) {
                mMap.addMarker(mMarkerOptions.position(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
                mEtLocation.setText(CommonMethods.getAddressOnlyFromLocation(getActivity(), latLng.latitude, latLng.longitude));
                mEtLatitude.setText(String.format("%f", latLng.latitude));
                mEtLongitude.setText(String.format("%f", latLng.longitude));
            } else {
                mEtLocation.setText("");
                mEtLatitude.setText("");
                mEtLongitude.setText("");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected()) {
            settingRequest();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /*Ending the updates for the location service*/
    @Override
    public void onStop() {
        if (mMap != null)
            mMap.clear();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        settingRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Toast.makeText(getActivity(), "Connection Suspended!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Connection Failed!", Toast.LENGTH_SHORT).show();
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(requireActivity(), 90000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("Current Location", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /*Method to get the enable location settings dialog*/
    private void settingRequest() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(requireActivity(), PERMISSIONS_REQUEST_GNABLE_GPS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PERMISSIONS_REQUEST_GNABLE_GPS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.setMyLocationEnabled(true);
                    getLocation();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    //Toast.makeText(getActivity(), "Location Service not Enabled.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted. Do the
                // contacts-related task you need to do.
                if (ContextCompat.checkSelfPermission(requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.setMyLocationEnabled(true);
                    getLocation();
                }
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(getActivity(), getString(R.string.msg_permission_denined), Toast.LENGTH_LONG).show();
                } else {
                    showRationaleDialog();
                }
            }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mGoogleApiClient != null) {
            if(mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, FindCompanyBranchOnMapFragment.this);

                /*Getting the location after aquiring location service*/
                Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (mCurrentLocation != null) {
                    mSelectedLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    if (mMap != null) {
                        mMap.clear();
                        mMap.addMarker(mMarkerOptions.position(mSelectedLocation));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mSelectedLocation, 18f));
                        mEtLocation.setText(CommonMethods.getAddressOnlyFromLocation(getActivity(), mSelectedLocation.latitude, mSelectedLocation.longitude));
                        mEtLatitude.setText(String.format("%f", mSelectedLocation.latitude));
                        mEtLongitude.setText(String.format("%f", mSelectedLocation.longitude));

                    }
                }
            }else{
                mGoogleApiClient.connect();
            }
        }
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(requireActivity())
                .setPositiveButton(getString(R.string.action_ok), (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                })
                .setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .setMessage(getString(R.string.msg_requirement_of_location_permission))
                .show();
    }


    /*When Location changes, this method get called. */
    @Override
    public void onLocationChanged(Location location) {
        /*if(location!=null)
            onMapClick(new LatLng(location.getLatitude(),location.getLongitude()));*/
    }
}
