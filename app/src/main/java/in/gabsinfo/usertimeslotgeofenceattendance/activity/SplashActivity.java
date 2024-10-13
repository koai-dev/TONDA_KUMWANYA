package in.gabsinfo.usertimeslotgeofenceattendance.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import in.gabsinfo.usertimeslotgeofenceattendance.BuildConfig;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonMethods.setTransparentStatusBar(SplashActivity.this);
        setContentView(R.layout.activity_splash);

        init();
        handlePermission();
        SharePreferences.setInt(SharePreferences.KEY_VERSION_CODE, BuildConfig.VERSION_CODE);
    }

    private void init() {

    }

    private void handlePermission() {
        if (!CommonMethods.hasPostNotificationPermissions(SplashActivity.this)) {
            requestForNotificationPermissions();
        } else {
            callNextScreen();
        }
    }


    public void requestForNotificationPermissions() {
        String[] permissions = new String[]{Manifest.permission.POST_NOTIFICATIONS};
        notificationPermissionRequest.launch(permissions);
    }

    ActivityResultLauncher<String[]> notificationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        if (CommonMethods.hasPostNotificationPermissions(SplashActivity.this)) {
                            callNextScreen();
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


    // Show a rationale dialog explaining why the permission is required
    private void showRationalPermissionDialog(String permission) {
        String title = "";
        String desc = "";
        if (permission.equals(Manifest.permission.POST_NOTIFICATIONS)) {
            title = String.format(getString(R.string.permission_title),
                    "Notifications");
            desc = getString(R.string.app_name) + " requires notifications permission as “Allow“ to notify you regarding multiple events.";
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(desc)
                .setPositiveButton("Allow", (dialog, which) -> {
                    dialog.cancel();
                    requestForNotificationPermissions();
                })
                .setNegativeButton("Cancel", null)
                .show();


    }

    private void showManuallyGrantPermissionDialog(String permission) {
        String title = "";
        String desc = "";
        if (permission.equals(Manifest.permission.POST_NOTIFICATIONS)) {
            title = String.format(getString(R.string.permission_title),
                    "Notifications");
            desc = "Without notifications permission " + getString(R.string.app_name) + " will not able to notify you regarding multiple events.\n\nAllow permission manually:\nSettings > Permissions > Click on Notifications permission > enable Toggle.";
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
                if (CommonMethods.hasPostNotificationPermissions(SplashActivity.this)) {
                    callNextScreen();
                }
            });

    private void callNextScreen() {

        Thread splashTread = new Thread() {
            public void run() {
                try {
                    synchronized (this) {
                        wait(2 * 1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();

                } finally {
                    if (SharePreferences.getBool(SharePreferences.KEY_IS_WELCOME_SCREEN_SHOWN, SharePreferences.DEFAULT_BOOLEAN)) {

                        if (SharePreferences.getBool(SharePreferences.KEY_IS_USER_LOGGED_IN, SharePreferences.DEFAULT_BOOLEAN)) {

                            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(SplashActivity.this, OnBoardingActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }
            }
        };
        splashTread.start();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
}
