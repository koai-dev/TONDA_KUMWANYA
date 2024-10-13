package in.gabsinfo.usertimeslotgeofenceattendance.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.OnBackPressedCallback;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.UserAppModulesAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.BranchWiseHolidayListFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.EmployeePerformanceFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.EnableBackgroundLocationPermissionFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.EnableLocationPermissionFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.MarkAttendanceFromAnyWhereFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.NotesFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.SettingsFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.UserAttendanceHistoryInCalendarFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.UserProfileFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AppModuleModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.ShopTimingModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class UserDashboardActivity extends AppCompatActivity {

    public FrameLayout mFlContainer;
    private final String TAG = UserDashboardActivity.this.getClass().getCanonicalName();
    private PersonModel mPersonModel;

    private static final int REQUEST_CHECK_GPS_SETTINGS = 179;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                CommonMethods.showAlertForExit(UserDashboardActivity.this);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        if (getIntent().getExtras() != null) {
            mPersonModel = (PersonModel) getIntent().getExtras().getSerializable("PersonModel");
        }else{
            Toast.makeText(this, "No model found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        setToolBar();
        init();
        FirebaseMessaging.getInstance().subscribeToTopic("Users");
        FirebaseMessaging.getInstance().subscribeToTopic(ConstantData.SUBSCRIBE_ALL_COMPANY_USERS);
        //Used to track employee & specially stop tracking when tracking is disabled by admin
        FirebaseMessaging.getInstance().subscribeToTopic(mPersonModel.getFirebaseKey());
    }

    private void setToolBar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_title_dashboard));
        toolbar.setSubtitle(String.format(getString(R.string.label_welcome_user),
                SharePreferences.getStr(SharePreferences.KEY_USER_NAME, SharePreferences.DEFAULT_STRING)));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void init() {

        String strModuleResult = "[{'ModuleName':'Public Holidays','ModuleDesc':'Check allocated public holidays','ModuleId':1,'VersionCode':13},{'ModuleName':'Profile','ModuleDesc':'Display your profile details','ModuleId':2,'VersionCode':13},{'ModuleName':'Mark Attendance','ModuleDesc':'Mark your daily attendance','ModuleId':3,'VersionCode':0},{'ModuleName':'View Attendance','ModuleDesc':'Check your monthly attendance in detailing','ModuleId':4,'VersionCode':0},{'ModuleName':'Reports','ModuleDesc':'Check your month wise attendance reports in Chart format','ModuleId':5,'VersionCode':0},{'ModuleName':'Change Password','ModuleDesc':'Allow to change your password','ModuleId':6,'VersionCode':0},{'ModuleName':'Notes / Rules','ModuleDesc':'Important notes/rules which you need to take care.','ModuleColor':'#118BE8','ModuleId':7,'VersionCode':0}]";
        ArrayList<AppModuleModel> modulesList = new ArrayList<>();
        try {
            Type listType = new TypeToken<List<AppModuleModel>>() {
            }.getType();
            modulesList = new Gson().fromJson(strModuleResult, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageView ivUserProfile =  findViewById(R.id.iv_user_profile_image);
        TextView tvWishForDay =  findViewById(R.id.tv_wish_for_day);
        TextView tvUserName =  findViewById(R.id.tv_user_name);
        TextView tvCurrentDate =  findViewById(R.id.tv_current_date);
        TextView tvCompanyCode =  findViewById(R.id.tv_company_code);
        tvWishForDay.setText(CommonMethods.getWishForDay(this).concat(","));
        tvUserName.setText(SharePreferences.getStr(SharePreferences.KEY_USER_NAME,SharePreferences.DEFAULT_STRING));
        tvCurrentDate.setText( new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(Calendar.getInstance().getTime()));

        String userProfileImage = SharePreferences.getStr(SharePreferences.KEY_USER_PROFILE_IMAGE, SharePreferences.DEFAULT_STRING);
        if (userProfileImage != null
                && userProfileImage.trim().length() > 0) {
            Glide.with(this)
                    .load(userProfileImage)
                    .placeholder(ContextCompat.getDrawable(this, R.drawable.loading_transparent))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            //on load failed
                            ivUserProfile.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            //on load success
                            ivUserProfile.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivUserProfile);
        } else {
            ivUserProfile.setVisibility(View.GONE);
        }

        mFlContainer = findViewById(R.id.fl_container);
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            mFlContainer.setVisibility(View.VISIBLE);
        }

        RecyclerView rvServiceProvider =  findViewById(R.id.rv_modules);
        rvServiceProvider.setLayoutManager(new GridLayoutManager(this, 2));
        rvServiceProvider.setItemAnimator(new DefaultItemAnimator());
        final UserAppModulesAdapter mAdapter = new UserAppModulesAdapter(this,
                R.layout.row_user_app_module, modulesList, new UserAppModulesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final AppModuleModel item, int Position) {
                switch (item.getModuleId()) {
                    case 1://Public Holidays
                        loadFragment(new BranchWiseHolidayListFragment(),null);
                        //Holidays for logged-in users branch
                        /*Bundle bundle=new Bundle();
                        bundle.putString("BranchName",mPersonModel.getWorkForBranchName());
                        bundle.putString("BranchCode",mPersonModel.getWorkForBranchCode());
                        loadFragment(new BranchWiseHolidayListFragment(), bundle);*/
                        break;
                    case 2://User Profile
                        Bundle bundle1 = new Bundle();
                        bundle1.putSerializable("PersonModel", mPersonModel);
                        loadFragment(new UserProfileFragment(), bundle1);
                        break;
                    case 3://Mark Attendance
                        if (!CommonMethods.hasLocationPermissions(UserDashboardActivity.this)) {
                            loadFragment(new EnableLocationPermissionFragment(), null);
                        } else if (!CommonMethods.hasBackgroundLocationPermissions(UserDashboardActivity.this)
                                && mPersonModel.isTrackingEnable()) {
                            loadFragment(new EnableBackgroundLocationPermissionFragment(), null);
                        } else {
                            handleMarkAttendanceClick();
                        }
                        break;
                    case 4://View Attendance Person
                        Bundle bundle2 = new Bundle();
                        bundle2.putSerializable("PersonModel", mPersonModel);
                        loadFragment(new UserAttendanceHistoryInCalendarFragment(), bundle2);
                        break;
                    case 5://Performance
                        Bundle bundle3 = new Bundle();
                        bundle3.putSerializable("PersonModel", mPersonModel);
                        loadFragment(new EmployeePerformanceFragment(), bundle3);
                        break;
                    case 6://Change Password
                        showPasswordChangeDialog();
                        break;
                    case 7://Notes
                        loadFragment(new NotesFragment(),null);
                        break;
                    default:
                        break;
                }
            }
        });
        rvServiceProvider.setAdapter(mAdapter);

        enableGPS();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_logout) {
            loadFragment(new SettingsFragment(),null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CHECK_GPS_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Toast.makeText(UserDashboardActivity.this, "Turn on GPS.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        } else {
            List<Fragment> fragments = getSupportFragmentManager()
                    .getFragments();
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, intent);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager()
                .getFragments();
        for (Fragment fragment : fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    public void handleMarkAttendanceClick() {
        if (!mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
            checkDayIsValidForAttendance();
        } else {
            /**
             * Here we call mark attendance fragment
             */
            SharePreferences.setBool(SharePreferences.KEY_IS_PUNCH, false);
            Bundle bundle2 = new Bundle();
            bundle2.putSerializable("PersonModel", mPersonModel);
            loadFragment(new MarkAttendanceFromAnyWhereFragment(), bundle2);
        }
    }

    /**
     * Here we enable Location permission
     */
    private void enableGPS() {

        // All required changes were successfully made
        if (mPersonModel.isTrackingEnable()
                && CommonMethods.hasPostNotificationPermissions(UserDashboardActivity.this)
                && CommonMethods.hasLocationPermissions(UserDashboardActivity.this)
                && CommonMethods.hasBackgroundLocationPermissions(UserDashboardActivity.this)
                && !CommonMethods.isGPSEnabled(UserDashboardActivity.this)) {


            // Create the location request to start receiving updates
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Create LocationSettingsRequest object using location request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            // Check whether location settings are satisfied
            // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(locationSettingsRequest)
                    .addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                            try {
                                LocationSettingsResponse response = task.getResult(ApiException.class);
                                // All location settings are satisfied. The client can initialize location
                                // requests here.
                            } catch (ApiException exception) {
                                switch (exception.getStatusCode()) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        // Location settings are not satisfied. But could be fixed by showing the
                                        // user a dialog.
                                        try {
                                            // Cast to a resolvable exception.
                                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                                            // Show the dialog by calling startResolutionForResult(),
                                            // and check the result in onActivityResult().
                                            resolvable.startResolutionForResult(
                                                    UserDashboardActivity.this,
                                                    REQUEST_CHECK_GPS_SETTINGS);
                                        } catch (ClassCastException e) {
                                            // Ignore, should be an impossible error.
                                        } catch (IntentSender.SendIntentException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        // Location settings are not satisfied. However, we have no way to fix the
                                        // settings so we won't show the dialog.
                                        Toast.makeText(UserDashboardActivity.this, "Location tracking service not started due to Setting or not available", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Here we check whether current punch date
     * is public holiday or non working day
     */
    private void checkDayIsValidForAttendance() {

        if (CommonMethods.isNetworkConnected(this)) {
            CommonMethods.showProgressDialog(this);
            AttendanceApplication.refPublicHoliday
                    .orderByChild("date").equalTo(new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(new Date()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            CommonMethods.cancelProgressDialog();
                            if (dataSnapshot.exists()) {
                                //Punch day is public holiday
                                CommonMethods.showAlertDailogueWithOK(UserDashboardActivity.this, getString(R.string.title_alert),
                                        getString(R.string.msg_not_allowed_to_mark_attendance_as_today_is_public_holiday),
                                        getString(R.string.action_ok));
                            }else{

                                /**
                                 * Here we check whether punch date is non working day or not
                                 */
                                ArrayList<ShopTimingModel> mTimeSlotArrayList = mPersonModel.getTimeSlotList();
                                if (mTimeSlotArrayList == null
                                        || mTimeSlotArrayList.size() == 0) {
                                    CommonMethods.showAlertDailogueWithOK(UserDashboardActivity.this,
                                            getString(R.string.alert_title_time_slot_not_found),
                                            getString(R.string.alert_msg_alert_time_slot_not_found),
                                            getString(R.string.action_ok));
                                    return;
                                }
                                /**
                                 * Here we convert current date/punch date into day name 'MONDAY','SUNDAY' format
                                 */
                                SimpleDateFormat outFormat = new SimpleDateFormat("EEEE", Locale.US);
                                String currentDayName = outFormat.format(new Date()).toUpperCase();
                                /**
                                 * Here we EXTRACT time slot model for particular current date/punch date
                                 */
                                ShopTimingModel timeModel = null;
                                for (ShopTimingModel model :
                                        mTimeSlotArrayList) {
                                    assert model != null;
                                    if (model.getDay().equalsIgnoreCase(currentDayName)) {
                                        timeModel = model;
                                        break;
                                    }
                                }
                                if (timeModel != null
                                        && timeModel.getFromTime() != null
                                        && timeModel.getFromTime().trim().length() > 0
                                        && (timeModel.getToTime() != null
                                        && timeModel.getToTime().trim().length() > 0)) {

                                    /**
                                     * Here we call mark attendance fragment
                                     */
                                    SharePreferences.setBool(SharePreferences.KEY_IS_PUNCH, false);
                                    Bundle bundle1 = new Bundle();
                                    bundle1.putSerializable("PersonModel", mPersonModel);
                                    loadFragment(new MarkAttendanceFromAnyWhereFragment(), bundle1);
                                }else{
                                    //Punch day is public holiday
                                    CommonMethods.showAlertDailogueWithOK(UserDashboardActivity.this, getString(R.string.title_alert),
                                            getString(R.string.msg_not_allowed_to_mark_attendance_as_today_is_non_working_day),
                                            getString(R.string.action_ok));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            CommonMethods.cancelProgressDialog();
                            CommonMethods.showAlertDailogueWithOK(UserDashboardActivity.this, getString(R.string.title_alert),
                                    databaseError.getMessage(), getString(R.string.action_ok));
                        }
                    });
        } else {
            CommonMethods.showConnectionAlert(UserDashboardActivity.this);
        }
    }




    private void getUserDetails() {
        /**
         * After login when user close application and start application again
         * then we redirect user to this activity at that time
         * we have no person model.
         * And hence we have to get Person model manually as shown below
         */
        if (CommonMethods.isNetworkConnected(UserDashboardActivity.this)) {
            CommonMethods.showProgressDialog(UserDashboardActivity.this);
            AttendanceApplication.refCompanyUserDetails
                    .orderByChild("userType_emailId")
                    .equalTo(ConstantData.TYPE_USER
                            + "-" + SharePreferences.getStr(SharePreferences.KEY_USER_EMAIL_ID, SharePreferences.DEFAULT_STRING))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            CommonMethods.cancelProgressDialog();
                            if (dataSnapshot.exists()) {

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    mPersonModel = ds.getValue(PersonModel.class);
                                    assert mPersonModel != null;
                                    mPersonModel.setFirebaseKey(ds.getKey());
                                }
                            } else {
                                CommonMethods.showAlertDailogueWithOK(UserDashboardActivity.this, getString(R.string.title_alert),
                                        getString(R.string.msg_invalid_user), getString(R.string.action_ok));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError e) {
                            CommonMethods.cancelProgressDialog();
                            CommonMethods.showAlertDailogueWithOK(UserDashboardActivity.this, getString(R.string.title_alert),
                                    String.format(getString(R.string.msg_issue_while_validating_user), e.getMessage()), getString(R.string.action_ok));
                        }
                    });
        } else {
            CommonMethods.showConnectionAlert(UserDashboardActivity.this);
        }
    }

    /**
     * Show dialog to change password
     */
    private void showPasswordChangeDialog() {

        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);
        //then we will inflate the custom alert dialog xml that we created
        final View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_change_password, viewGroup, false);
        final EditText etPassword =  dialogView.findViewById(R.id.et_password);
        RelativeLayout rlAddContainer=dialogView.findViewById(R.id.rl_adContainer);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(getString(R.string.alert_title_change_password));
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder
                .setPositiveButton(getString(R.string.action_change), null)
                .setNegativeButton(getString(R.string.action_cancel), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        String strPassword = etPassword.getText().toString().trim();

                        if (strPassword.isEmpty()) {
                            Toast.makeText(UserDashboardActivity.this, getString(R.string.msg_enter_password), Toast.LENGTH_LONG).show();
                        } else {

                            if (CommonMethods.isNetworkConnected(UserDashboardActivity.this)) {

                                CommonMethods.showProgressDialog(UserDashboardActivity.this);
                                AttendanceApplication.refCompanyUserDetails
                                        .child(SharePreferences.getStr(SharePreferences.KEY_USER_FIREBASE_KEY, SharePreferences.DEFAULT_STRING))
                                        .child("password")
                                        .setValue(strPassword)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                SharePreferences.setStr(SharePreferences.KEY_USER_PASSWORD, strPassword);
                                                alertDialog.dismiss();
                                                CommonMethods.cancelProgressDialog();

                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                        UserDashboardActivity.this, R.style.CustomDialogTheme);
                                                alertDialogBuilder.setTitle("Success");
                                                alertDialogBuilder.setMessage("Your password updated successfully. Please login again.")
                                                        .setPositiveButton(getString(R.string.action_re_login),
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        SharePreferences.clearSP();

                                                                        Intent intent = new Intent(UserDashboardActivity.this, SignInActivity.class);
                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.setCancelable(false);
                                                alertDialog.show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                CommonMethods.cancelProgressDialog();
                                                CommonMethods.showAlertDailogueWithOK(UserDashboardActivity.this, getString(R.string.title_alert),
                                                        String.format(getString(R.string.msg_issue_while_updating_password), e.getMessage()), getString(R.string.action_ok));
                                            }
                                        });
                            } else {
                                CommonMethods.showConnectionAlert(UserDashboardActivity.this);
                            }
                        }
                    }
                });

                Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                btnNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    public void loadFragment(Fragment fragment, Bundle bundle) {

        if (fragment != null) {
            fragment.setArguments(bundle);
            try {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_left, R.anim.slide_right, R.anim.slide_left, R.anim.slide_right);
                fragmentTransaction.add(R.id.fl_container, fragment, fragment.getClass().getCanonicalName())
                        .addToBackStack(fragment.getClass().getCanonicalName())
                        .commit();
                mFlContainer.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onBackPressed() {
        if (mFlContainer.getVisibility() == View.VISIBLE) {
            int count = getSupportFragmentManager().getBackStackEntryCount();
            if (count == 1) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(EmployeePerformanceFragment.class.getCanonicalName());
                if(fragment!=null){
                    ((EmployeePerformanceFragment)fragment).onBackPress();
                }else{
                    getSupportFragmentManager().popBackStack();
                    mFlContainer.setVisibility(View.GONE);
                }
            }else{
                //getSupportFragmentManager().popBackStack();
                super.onBackPressed();
            }
        } else {
            CommonMethods.showAlertForExit(this);
        }
    }

    public void requestForLocationPermissions() {
        // base permissions are for M and higher
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
        }

        locationPermissionRequest.launch(permissions);
    }

    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                    .RequestMultiplePermissions(), result -> {
                //Grant Location Permission
                if (CommonMethods.hasLocationPermissions(UserDashboardActivity.this)) {
                    onBackPressed();
                    if (mPersonModel.isTrackingEnable()
                            && !CommonMethods.hasBackgroundLocationPermissions(UserDashboardActivity.this)) {
                        loadFragment(new EnableBackgroundLocationPermissionFragment(), null);
                    }else{
                        handleMarkAttendanceClick();
                    }
                } else {
                    AtomicBoolean stopLoop= new AtomicBoolean(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        result.forEach((permission, isGranted) -> {
                            if(!stopLoop.get()) {
                                if (!isGranted) {
                                    stopLoop.set(true);
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                            permission)) {
                                        showRationalPermissionDialog(permission);
                                    } else {
                                        showManuallyGrantPermissionDialog(permission);
                                    }
                                }
                            }
                        });
                    }
                }
            });

    public void requestForBackgroundLocationPermissions() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        backgroundLocationPermissionRequest.launch(permissions);
    }
    ActivityResultLauncher<String[]> backgroundLocationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {

                        if (CommonMethods.hasBackgroundLocationPermissions(this)) {
                            onBackPressed();
                            handleMarkAttendanceClick();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                result.forEach((permission, isGranted) -> {
                                    if (!isGranted) {
                                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                permission)) {
                                            showRationalPermissionDialog(permission);
                                        } else {
                                            showManuallyGrantPermissionDialog(permission);
                                        }
                                    }
                                });
                            }
                        }
                    }
            );

    private void showRationalPermissionDialog(String permission) {
        String title = "";
        String desc = "";
        if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            title = String.format(getString(R.string.permission_title),
                    "Location");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                desc = getString(R.string.app_name) + " requires location permission as “While using the app“ to determine your current location.";
            }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                desc = getString(R.string.app_name) + " requires location permission as “Allow only while using the app“ to determine your current location.";
            }else {
                desc = getString(R.string.app_name) + " requires location permission as “Allow“ to determine your current location.";
            }
        }else if (permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            title = String.format(getString(R.string.permission_title),
                    "Location");
            desc = getString(R.string.app_name) + " requires background location permission as “Allow all the time“ to determine your current location even when the app is closed or not in use.";
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(desc)
                .setPositiveButton("Allow", (dialog, which) -> {
                    dialog.cancel();
                    if(permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)
                            || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)){
                        requestForLocationPermissions();
                    }else if(permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                        requestForBackgroundLocationPermissions();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void showManuallyGrantPermissionDialog(String permission) {
        String title = "";
        String desc = "";
        if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            title = String.format(getString(R.string.permission_title),
                    "Location");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                desc = "Without location permission " + getString(R.string.app_name) + " will not work properly.\n\nAllow permission manually:\nSettings > Permissions > Click on Location permission > Allow All the Time.";
            }else {
                desc = "Without location permission " + getString(R.string.app_name) + " will not work properly.\n\nAllow permission manually:\nSettings > Permissions > Click on Location permission > enable Toggle.";
            }
        }else if (permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            title = String.format(getString(R.string.permission_title),
                    "Location");
            desc = "Without background location permission " + getString(R.string.app_name) + " will not work properly in background.\n\nAllow permission manually:\nSettings > Permissions > Click on Location permission > Allow All the Time.";
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(desc)
                .setCancelable(false)
                .setPositiveButton("Settings", (dialog, which) -> {
                    dialog.cancel();
                    redirectToAppSettingPage();
                })
                .show();
    }

    private void redirectToAppSettingPage() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        intentResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> intentResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
            });

    @Override
    public void onResume() {
        super.onResume();
        if (!CommonMethods.isAutoDateTimeEnabled(UserDashboardActivity.this)) {
            CommonMethods.showAlertForChangeDate(UserDashboardActivity.this);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
}
