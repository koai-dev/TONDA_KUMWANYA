package in.gabsinfo.usertimeslotgeofenceattendance.application;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.PrintWriter;
import java.io.StringWriter;

import in.gabsinfo.usertimeslotgeofenceattendance.utils.AppendLog;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class AttendanceApplication extends MultiDexApplication {


    private static Context context;
    public static DatabaseReference referenceNode;
    public static DatabaseReference refPublicHoliday;
    public static DatabaseReference refCompanyUserDetails;
    public static DatabaseReference refCompanyUserAttendanceDetails;
    public static DatabaseReference refPhoneMappingRequest;
    public static DatabaseReference refLocationTracking;
    public static DatabaseReference refNotes;
    public static StorageReference storageReference;



    public static Context getAppContext() {
        return AttendanceApplication.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        AttendanceApplication.context = getApplicationContext();
        SharePreferences.getInstance();

        referenceNode = FirebaseDatabase.getInstance().getReference(ConstantData.FIREBASE_NODE);
        refPublicHoliday = referenceNode.child(ConstantData.FIREBASE_NODE_PUBLIC_HOLIDAY);
        refCompanyUserDetails = referenceNode
                .child(ConstantData.FIREBASE_NODE_COMPANY_USERS);
        refCompanyUserAttendanceDetails = referenceNode
                .child(ConstantData.FIREBASE_NODE_COMPANY_USERS_ATTENDANCE);
        refPhoneMappingRequest = referenceNode
                .child(ConstantData.FIREBASE_NODE_PHONE_MAPPING_REQUEST);
        refLocationTracking = referenceNode
                .child(ConstantData.FIREBASE_NODE_LOCATION_TRACKING);
        refNotes = referenceNode
                .child(ConstantData.FIREBASE_NODE_NOTES);
        storageReference = FirebaseStorage.getInstance().getReference(ConstantData.FIREBASE_STORAGE_NODE_EMPLOYEE_PROFILE_IMAGE);

        //Setup handler for uncaught exceptions.
       /* Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                if(BuildConfig.DEBUG) {
                    handleUncaughtException(thread, e);
                }
            }
        });*/
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    // here you can handle all unexpected crashes
    public void handleUncaughtException(Thread thread, Throwable exception) {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }

        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        appendLogs("APP_EXCEPTION");
        appendLogs("Date " + CommonMethods.getCurrentDateTime());
        appendLogs("Exception : " + stackTrace.toString());

        appendLogs("\n------- DEVICE INFORMATION------\n");
        appendLogs("Brand: ");
        appendLogs(Build.BRAND);
        String LINE_SEPARATOR = "\n";
        appendLogs(LINE_SEPARATOR);
        appendLogs("Model: ");
        appendLogs(Build.MODEL);
        appendLogs(LINE_SEPARATOR);
        appendLogs("\n---FIRMWARE---\n");
        appendLogs("SDK: ");
        appendLogs(Build.VERSION.SDK);
        appendLogs(LINE_SEPARATOR);
        appendLogs("Release: ");
        appendLogs(Build.VERSION.RELEASE);
        appendLogs(LINE_SEPARATOR);
        appendLogs("\n--- VERSION---\n");
        appendLogs("Version Code: ");
        Object versionCode = info == null ? "(null)" : info.versionCode;
        appendLogs(String.valueOf(versionCode));
        appendLogs(LINE_SEPARATOR);
        appendLogs("Version Name: ");
        appendLogs(info == null ? "(null)" : info.versionName);
        appendLogs("--------------------------------------------");
    }

    private void appendLogs(String logtext) {
        try {
            AppendLog log = new AppendLog();
            log.appendLog(context, logtext, ConstantData.CONST_LOG_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
