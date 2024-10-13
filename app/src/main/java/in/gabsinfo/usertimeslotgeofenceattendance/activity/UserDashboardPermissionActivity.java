package in.gabsinfo.usertimeslotgeofenceattendance.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.concurrent.atomic.AtomicBoolean;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.NavigationController;

public class UserDashboardPermissionActivity extends AppCompatActivity {
    private static final String TAG = UserDashboardPermissionActivity.class.getSimpleName();
    private ActivityResultLauncher<Intent> intentResultLauncher = null;
    private PersonModel mPersonModel;

    private static String ACCESS_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    private static String POST_NOTIFICATION = Manifest.permission.POST_NOTIFICATIONS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        if (getIntent().getExtras() != null) {
            mPersonModel = (PersonModel) getIntent().getExtras().getSerializable("PersonModel");
        } else {
            Toast.makeText(this, "No model found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        initialize();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        launchActivity();
    }

    private void initialize() {

        NavigationController.getInstance().initialize(this);
        intentResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    handleView();
                });

        handleView();
    }

    private void handleView() {
        if (!CommonMethods.hasPostNotificationPermissions(this)) {
            if (NavigationController.getInstance().getNavController().getCurrentDestination() != null
                    && NavigationController.getInstance().getNavController().getCurrentDestination().getId() != R.id.enableNotificationPermissionFragment) {
                NavigationController.getInstance().getNavController().clearBackStack(R.id.enableNotificationPermissionFragment);
            }
        } else if (!CommonMethods.hasLocationPermissions(UserDashboardPermissionActivity.this)) {
            if (NavigationController.getInstance().getNavController().getCurrentDestination() != null
                    && NavigationController.getInstance().getNavController().getCurrentDestination().getId() != R.id.enableLocationPermissionFragment) {
                NavigationController.getInstance().getNavController().navigate(R.id.enableLocationPermissionFragment);
            }
        } else if (!CommonMethods.hasBackgroundLocationPermissions(UserDashboardPermissionActivity.this)) {
            if (NavigationController.getInstance().getNavController().getCurrentDestination() != null
                    && NavigationController.getInstance().getNavController().getCurrentDestination().getId() != R.id.enableBackgroundLocationPermissionFragment)
                NavigationController.getInstance().getNavController().navigate(R.id.enableBackgroundLocationPermissionFragment);
        } else {
            launchActivity();
        }
    }


    //////Post Notification
    public void requestForNotificationPermissions() {
        notificationPermissionRequest.launch(POST_NOTIFICATION);
    }

    ActivityResultLauncher<String> notificationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestPermission(), isGranted -> {
                        if (CommonMethods.hasPostNotificationPermissions(this)) {
                            handleFurtherPermissionsAfterPostNotificationPermission();
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    POST_NOTIFICATION)) {
                                showRationalPermissionDialog(POST_NOTIFICATION);
                            } else {
                                showManuallyGrantPermissionDialog(POST_NOTIFICATION);
                            }
                        }
                    }
            );

    public void handleFurtherPermissionsAfterPostNotificationPermission() {
        if (!CommonMethods.hasLocationPermissions(UserDashboardPermissionActivity.this)) {
            if (NavigationController.getInstance().getNavController().getCurrentDestination() != null
                    && NavigationController.getInstance().getNavController().getCurrentDestination().getId() != R.id.enableBackgroundLocationPermissionFragment)
                NavigationController.getInstance().getNavController().navigate(R.id.enableLocationPermissionFragment);
        } else if (!CommonMethods.hasBackgroundLocationPermissions(UserDashboardPermissionActivity.this)) {
            if (NavigationController.getInstance().getNavController().getCurrentDestination() != null
                    && NavigationController.getInstance().getNavController().getCurrentDestination().getId() != R.id.enableBackgroundLocationPermissionFragment)
                NavigationController.getInstance().getNavController().navigate(R.id.enableBackgroundLocationPermissionFragment);
        } else {
            CommonMethods.notifyTrackingStatusToEmployee(UserDashboardPermissionActivity.this, mPersonModel);
            launchActivity();
        }
    }

    //Location Service
    //Tutorial : https://medium.com/swlh/request-location-permission-correctly-in-android-11-61afe95a11ad
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
                if (CommonMethods.hasLocationPermissions(UserDashboardPermissionActivity.this)) {
                    if (!CommonMethods.hasBackgroundLocationPermissions(UserDashboardPermissionActivity.this)) {
                        if (NavigationController.getInstance().getNavController().getCurrentDestination() != null
                                && NavigationController.getInstance().getNavController().getCurrentDestination().getId() != R.id.enableBackgroundLocationPermissionFragment)
                            NavigationController.getInstance().getNavController().navigate(R.id.enableBackgroundLocationPermissionFragment);
                    } else {
                        CommonMethods.notifyTrackingStatusToEmployee(UserDashboardPermissionActivity.this, mPersonModel);
                        launchActivity();
                    }
                } else {
                    AtomicBoolean stopLoop = new AtomicBoolean(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        result.forEach((permission, isGranted) -> {
                            if (!stopLoop.get()) {
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


    //Background Location Service
    public void requestForBackgroundLocationPermissions() {
        backgroundLocationPermissionRequest.launch(ACCESS_BACKGROUND_LOCATION);
    }

    ActivityResultLauncher<String> backgroundLocationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestPermission(), isGranted -> {

                        if (CommonMethods.hasBackgroundLocationPermissions(UserDashboardPermissionActivity.this)) {
                            CommonMethods.notifyTrackingStatusToEmployee(UserDashboardPermissionActivity.this, mPersonModel);
                            launchActivity();
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    ACCESS_BACKGROUND_LOCATION)) {
                                showRationalPermissionDialog(ACCESS_BACKGROUND_LOCATION);
                            } else {
                                showManuallyGrantPermissionDialog(ACCESS_BACKGROUND_LOCATION);
                            }
                        }
                    }
            );


    // Show a rationale dialog explaining why the permission is required
    private void showRationalPermissionDialog(String permission) {
        String title = "";
        String desc = "";
        if (permission.equals(POST_NOTIFICATION)) {
            title = String.format(getString(R.string.permission_title),
                    "Notifications");
            desc = getString(R.string.app_name) + " requires notifications permission as “Allow“ to notify you about events.";
        } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            title = String.format(getString(R.string.permission_title),
                    "Location");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                desc = getString(R.string.app_name) + " requires location permission as “While using the app“ to determine your current location.";
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                desc = getString(R.string.app_name) + " requires location permission as “Allow only while using the app“ to determine your current location";
            } else {
                desc = getString(R.string.app_name) + " requires location permission as “Allow“ to determine your current location.";
            }
        } else if (permission.equals(ACCESS_BACKGROUND_LOCATION)) {
            title = String.format(getString(R.string.permission_title),
                    "Location");
            desc = getString(R.string.app_name) + " requires background location permission as “Allow all the time“ to determine your current location even when the app is closed or not in use.";
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(desc)
                .setPositiveButton("Allow", (dialog, which) -> {
                    dialog.cancel();
                    if (permission.equals(POST_NOTIFICATION)) {
                        requestForNotificationPermissions();
                    } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)
                            || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        requestForLocationPermissions();
                    } else if (permission.equals(ACCESS_BACKGROUND_LOCATION)) {
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
        if (permission.equals(POST_NOTIFICATION)) {
            title = String.format(getString(R.string.permission_title),
                    "Notifications");
            desc = "Without notifications permission " + getString(R.string.app_name) + " will not able to notify about events.\n\nAllow permission manually:\nSettings > Permissions > Click on Notifications permission > enable Toggle.";
        } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            title = String.format(getString(R.string.permission_title),
                    "Location");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                desc = "Without location permission " + getString(R.string.app_name) + " will not work properly.\n\nAllow permission manually:\nSettings > Permissions > Click on Location permission > Allow All the Time.";
            } else {
                desc = "Without location permission " + getString(R.string.app_name) + " will not work properly.\n\nAllow permission manually:\nSettings > Permissions > Click on Location permission > enable Toggle.";
            }
        } else if (permission.equals(ACCESS_BACKGROUND_LOCATION)) {
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

    public void launchActivity() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("PersonModel", mPersonModel);
        Intent intent = new Intent(UserDashboardPermissionActivity.this, UserDashboardActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

}