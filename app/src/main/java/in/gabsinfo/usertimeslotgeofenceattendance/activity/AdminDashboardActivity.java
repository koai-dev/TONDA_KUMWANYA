package in.gabsinfo.usertimeslotgeofenceattendance.activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.AdminAppModulesAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.AdminExtraAppModulesAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.AddPersonFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.BranchWiseHolidayListFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.EmployeePerformanceFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.FindCompanyBranchOnMapFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.NotesFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.PresentEmployeesFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.SettingsFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.UserListForCalculateSalaryFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.UserListToTrackEmployeeCurrentLocationFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.ViewAllEmployeesFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AppModuleModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class AdminDashboardActivity extends AppCompatActivity implements FindCompanyBranchOnMapFragment.onLocationSelectListener {

    private final String TAG = AdminDashboardActivity.this.getClass().getCanonicalName();
    private FrameLayout mFlContainer;
    private Fragment mTopFragment = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                CommonMethods.showAlertForExit(AdminDashboardActivity.this);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        setToolBar();
        init();
        /**
         * Make group for 'Admin' to send Phone Mapping Request notification by Employee
         */
        FirebaseMessaging.getInstance().subscribeToTopic("Admin");

        FirebaseMessaging.getInstance().subscribeToTopic("Users");
        FirebaseMessaging.getInstance().subscribeToTopic(ConstantData.SUBSCRIBE_ALL_COMPANY_USERS);
    }


    private void setToolBar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_title_dashboard));
        toolbar.setSubtitle(String.format(getString(R.string.label_welcome_user),
                SharePreferences.getStr(SharePreferences.KEY_USER_NAME, SharePreferences.DEFAULT_STRING)));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void init() {

        String strModuleResult = "[{'ModuleName':'Add Holidays','ModuleDesc':'Add public holidays only','ModuleColor':'#2EA752','ModuleId':2,'VersionCode':13},{'ModuleName':'Add Employee','ModuleDesc':'Add Employee for whose wants to mark attendance','ModuleColor':'#E00166','ModuleId':3,'VersionCode':13},{'ModuleName':'View Employees','ModuleDesc':'List of registered employees','ModuleColor':'#5788A6','ModuleId':4,'VersionCode':0},{'ModuleName':'Today Present Employees','ModuleDesc':'Check date wise all registered employees presence or punch in/out','ModuleColor':'#5788A6','ModuleId':5,'VersionCode':0},{'ModuleName':'Calculate Salary','ModuleDesc':'Check month wise attendance & calculate salary for particular employee','ModuleColor':'#298ACB','ModuleId':6,'VersionCode':0},{'ModuleName':'View Attendance Report','ModuleDesc':'Check months wise employees attendance reports in Chart format','ModuleColor':'#298ACB','ModuleId':7,'VersionCode':12},{'ModuleName':'Live Tracking','ModuleDesc':'Track only current location of employee','ModuleColor':'#F4AA11','ModuleId':8,'VersionCode':0},{'ModuleName':'Change Password','ModuleDesc':'Allow to change your password','ModuleColor':'#F4AA11','ModuleId':9,'VersionCode':0}]";
        ArrayList<AppModuleModel> modulesList = new ArrayList<>();
        try {
            Type listType = new TypeToken<List<AppModuleModel>>() {
            }.getType();
            modulesList = new Gson().fromJson(strModuleResult, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageView ivUserProfile =  findViewById(R.id.iv_user_profile_image);
        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvWishForDay = findViewById(R.id.tv_wish_for_day);
        TextView tvCurrentDate = findViewById(R.id.tv_current_date);
        TextView tvCompanyCode = findViewById(R.id.tv_company_code);
        tvWishForDay.setText(CommonMethods.getWishForDay(this).concat(","));
        tvUserName.setText(SharePreferences.getStr(SharePreferences.KEY_USER_NAME, SharePreferences.DEFAULT_STRING));
        tvCurrentDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(Calendar.getInstance().getTime()));

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
        RecyclerView rvServiceProvider = findViewById(R.id.rv_modules);
        rvServiceProvider.setLayoutManager(new GridLayoutManager(this, 2));
        rvServiceProvider.setItemAnimator(new DefaultItemAnimator());
        final AdminAppModulesAdapter mAdapter = new AdminAppModulesAdapter(this,
                R.layout.row_admin_app_module, modulesList, new AdminAppModulesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final AppModuleModel item, int Position) {
                switch (item.getModuleId()) {
                    case 2://Add Holidays
                        loadFragment(new BranchWiseHolidayListFragment(), null);
                        break;
                    case 3://Add Person
                        loadFragment(new AddPersonFragment(), null);
                        break;
                    case 4://View All Persons
                        loadFragment(new ViewAllEmployeesFragment(), null);
                        break;
                    case 5://Check Present Employees
                        loadFragment(new PresentEmployeesFragment(), null);
                        break;
                    case 6://Calculate Salary
                    case 7://View Report
                        Bundle bundle=new Bundle();
                        bundle.putInt("ModuleId",item.getModuleId());
                        loadFragment(new UserListForCalculateSalaryFragment(), bundle);
                        break;
                    case 8://Track employee current location
                        loadFragment(new UserListToTrackEmployeeCurrentLocationFragment(),null);
                        break;
                    case 9://Change Password
                        showPasswordChangeDialog();
                        break;
                    default:
                        break;

                }

            }
        });
        rvServiceProvider.setAdapter(mAdapter);


        String strExtraModuleResult = "[{'ModuleName':'Broadcast Notification','ModuleDesc':'Send common message to all your registered employees.','ModuleColor':'#4855B7','ModuleId':31,'VersionCode':13},{'ModuleName':'Notes / Rules','ModuleDesc':'Important notes/rules which your registered employees need to take care.','ModuleColor':'#118BE8','ModuleId':32,'VersionCode':13}]";
        ArrayList<AppModuleModel> extraModulesList = new ArrayList<>();
        try {
            Type listType = new TypeToken<List<AppModuleModel>>() {
            }.getType();
            extraModulesList = new Gson().fromJson(strExtraModuleResult, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }


        RecyclerView rvExtra = findViewById(R.id.rv_extra);
        rvExtra.setLayoutManager(new GridLayoutManager(this, 2));
        rvExtra.setItemAnimator(new DefaultItemAnimator());
        rvExtra.setAdapter(new AdminExtraAppModulesAdapter(this,
                R.layout.row_admin_timesheet_app_module, extraModulesList, new AdminExtraAppModulesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final AppModuleModel item, int Position) {
                switch (item.getModuleId()) {
                    case 31://Broadcast Notification
                        showDialogToBroadCastNotification();
                        break;
                    case 32://Notes
                        loadFragment(new NotesFragment(), null);
                        break;
                    case 33://Income Expense Manager
                        final String appPackageName = "in.gabsinfo.expense.product";
                        if (!CommonMethods.isAppInstalled(AdminDashboardActivity.this, appPackageName)) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        } else {
                            Intent i = getPackageManager().getLaunchIntentForPackage(appPackageName);
                            startActivity(i);
                        }
                        break;
                    default:
                        break;

                }

            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_logout) {
            loadFragment(new SettingsFragment(), null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get this callback from FindCompanyBranchOnMap fragment
     * which responsible to select location of branch
     *
     * @param latLng
     */
    @Override
    public void SelectedLocation(LatLng latLng) {

    }


    /**
     * Used to open from-to time picker dialog from
     * AddPersonFragment
     */
    SimpleDateFormat actualFormat = new SimpleDateFormat(ConstantData.TWENTY_FOUR_HOURS_FORMAT, Locale.US);

    public void openTimePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
                        if (mTopFragment != null) {
                            if (mTopFragment instanceof AddPersonFragment) {
                                String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
                                String minuteString = minute < 10 ? "0" + minute : "" + minute;
                                String hourStringEnd = hourOfDayEnd < 10 ? "0" + hourOfDayEnd : "" + hourOfDayEnd;
                                String minuteStringEnd = minuteEnd < 10 ? "0" + minuteEnd : "" + minuteEnd;

                                Date dateOpenTime = null;
                                Date dateCloseTime = null;

                                try {
                                    dateOpenTime = actualFormat.parse(String.valueOf(hourString).concat(":").concat(String.valueOf(minuteString)));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    dateCloseTime = actualFormat.parse(String.valueOf(hourStringEnd).concat(":").concat(String.valueOf(minuteStringEnd)));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (dateOpenTime != null
                                        && dateCloseTime != null) {
                                    if (dateOpenTime.before(dateCloseTime)) {

                                        ((AddPersonFragment) mTopFragment).displaySelectionTime(dateOpenTime, dateCloseTime);

                                    } else {
                                        CommonMethods.showAlertDailogueWithOK(AdminDashboardActivity.this, getString(R.string.title_alert),
                                                getString(R.string.msg_close_time_must_be_greater_then_open_time), getString(R.string.action_ok));
                                    }
                                }
                            }
                        }
                    }
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    private void showPasswordChangeDialog() {

        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);
        //then we will inflate the custom alert dialog xml that we created
        final View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_change_password, viewGroup, false);
        final EditText etPassword = dialogView.findViewById(R.id.et_password);
        RelativeLayout rlAddContainer = dialogView.findViewById(R.id.rl_adContainer);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
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
                            Toast.makeText(AdminDashboardActivity.this, getString(R.string.msg_enter_password), Toast.LENGTH_SHORT).show();
                        } else {

                            if (CommonMethods.isNetworkConnected(AdminDashboardActivity.this)) {

                                CommonMethods.showProgressDialog(AdminDashboardActivity.this);
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
                                                        AdminDashboardActivity.this, R.style.CustomDialogTheme);
                                                alertDialogBuilder.setTitle("Success");
                                                alertDialogBuilder.setMessage("Your password updated successfully. Please login again.")
                                                        .setPositiveButton(getString(R.string.action_re_login),
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        SharePreferences.clearSP();

                                                                        Intent intent = new Intent(AdminDashboardActivity.this, SignInActivity.class);
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
                                                CommonMethods.showAlertDailogueWithOK(AdminDashboardActivity.this, getString(R.string.title_alert),
                                                        String.format(getString(R.string.msg_issue_while_updating_password), e.getMessage()),
                                                        getString(R.string.action_ok));
                                            }
                                        });
                            } else {
                                CommonMethods.showConnectionAlert(AdminDashboardActivity.this);
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


    //https://github.com/droidbyme/Recycler-View   Multi-Select
    private void showDialogToBroadCastNotification() {


        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);
        //then we will inflate the custom alert dialog xml that we created
        final View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_broadcast_notification, viewGroup, false);
        final EditText etTitle = dialogView.findViewById(R.id.et_title);
        final EditText etDescription = dialogView.findViewById(R.id.et_description);


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle("Broadcast Notification");
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder
                .setPositiveButton(getString(R.string.action_send), null)
                .setNegativeButton(getString(R.string.action_cancel), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        String strTitle = etTitle.getText().toString().trim();
                        String strDescription = etDescription.getText().toString().trim();
                        if (strTitle.length() == 0)
                            Toast.makeText(AdminDashboardActivity.this, "Enter Title.", Toast.LENGTH_SHORT).show();
                        else if (strDescription.length() == 0)
                            Toast.makeText(AdminDashboardActivity.this, "Enter Description.", Toast.LENGTH_SHORT).show();
                        else {
                            if (CommonMethods.isNetworkConnected(AdminDashboardActivity.this)) {
                                dialog.dismiss();
                                CommonMethods.sendCommonNotificationForAllEmployees(AdminDashboardActivity.this,
                                        ConstantData.NOTIFICATION_TITLE_EXTRA,
                                        strTitle.concat(" ( BROADCAST )"),
                                        strDescription,
                                        "",
                                        ConstantData.SUBSCRIBE_ALL_COMPANY_USERS);
                            }else{
                                CommonMethods.showConnectionAlert(AdminDashboardActivity.this);
                            }
                        }
                    }
                });
                Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                btnNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
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
            mTopFragment = fragment;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager()
                .getFragments();
        for (Fragment fragment : fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            List<Fragment> fragments = getSupportFragmentManager()
                    .getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        if (mFlContainer.getVisibility() == View.VISIBLE) {
            int count = getSupportFragmentManager().getBackStackEntryCount();
            if (count == 1) {
                getSupportFragmentManager().popBackStack();
                mFlContainer.setVisibility(View.GONE);
            } else if (count == 2) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(new EmployeePerformanceFragment().getClass().getCanonicalName());
                if (fragment != null) {
                    ((EmployeePerformanceFragment) fragment).onBackPress();
                } else {
                    getSupportFragmentManager().popBackStack();
                }
            } else {
                //getSupportFragmentManager().popBackStack();
                super.onBackPressed();
            }
        } else {
            CommonMethods.showAlertForExit(this);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (!CommonMethods.isAutoDateTimeEnabled(AdminDashboardActivity.this)) {
            CommonMethods.showAlertForChangeDate(AdminDashboardActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

}
