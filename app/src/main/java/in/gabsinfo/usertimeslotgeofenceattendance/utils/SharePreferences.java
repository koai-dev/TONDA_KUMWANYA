package in.gabsinfo.usertimeslotgeofenceattendance.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;


public class SharePreferences {

    public final static String PREFS_NAME = "tent_city_prefs";

    private static SharedPreferences PREF_APP;
    private static SharedPreferences.Editor editor;


    public static final String KEY_VERSION_CODE = "KEY_VERSION_CODE";
    public static final String KEY_IS_WELCOME_SCREEN_SHOWN = "KEY_IS_WELCOME_SCREEN_SHOWN";
    public static final String KEY_WORK_AREA_LIST = "KEY_WORK_AREA_LIST";

    public static final String KEY_IS_ADMIN_USER = "KEY_IS_ADMIN_USER";
    public static final String KEY_USER_EMAIL_ID = "KEY_USER_EMAIL_ID";
    public static final String KEY_USER_PASSWORD = "KEY_USER_PASSWORD";
    public static final String KEY_USER_PROFILE_IMAGE = "KEY_USER_PROFILE_IMAGE";
    public static final String KEY_USER_NAME = "KEY_USER_NAME";
    public static final String KEY_USER_MOBILE_NO = "KEY_USER_MOBILE_NO";
    public static final String KEY_IS_USER_LOGGED_IN = "KEY_IS_USER_LOGGED_IN";
    public static final String KEY_USER_FIREBASE_KEY = "KEY_USER_FIREBASE_KEY";
    public static final String KEY_USER_REGISTRATION_DATE = "KEY_USER_REGISTRATION_DATE";
    public static final String KEY_IS_PUNCH = "KEY_IS_PUNCH";
    public static final String KEY_EMPLOYEE_WORK_TYPE = "KEY_EMPLOYEE_WORK_TYPE";
    public static final String KEY_IS_TRACKING_ENABLE = "KEY_IS_TRACKING_ENABLE";
    public static final String KEY_TRACKING_DEVICE_ID = "KEY_TRACKING_DEVICE_ID";
    public static final String KEY_DIALOG_SHOWN_FOR_PUBLIC_HOLIDAY = "KEY_DIALOG_SHOWN_FOR_PUBLIC_HOLIDAY";
    public static final String KEY_DIALOG_SHOWN_FOR_TRACKING = "KEY_DIALOG_SHOWN_FOR_TRACKING";
    public static final String KEY_PURCHASE_SUBSCRIPTION = "KEY_PURCHASE_SUBSCRIPTION";
    public static final boolean DEFAULT_BOOLEAN = false;
    public static final String DEFAULT_STRING = "";
    public static final long DEFAULT_LONG = 0L;
    public static final int DEFAULT_INT = 0;

    private SharePreferences() {
    }

    //The context passed into the getInstance should be application level context.
    public static void getInstance() {
        if (PREF_APP == null) {
            PREF_APP = AttendanceApplication.getAppContext().getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
            editor = PREF_APP.edit();
        }
    }

    public static void setInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(String key, int defaultValue) {
        return PREF_APP.getInt(key, defaultValue);
    }

    public static void setStr(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public static String getStr(String key, String defaultValue) {
        return PREF_APP.getString(key, defaultValue);
    }

    public static void setBool(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBool(String key, boolean value) {
        return PREF_APP.getBoolean(key, value);
    }

    public static void setLong(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(String key, long value) {
        return PREF_APP.getLong(key, value);
    }

    //This is called when user logout from application
    public static void clearSP() {

        int versionCode = SharePreferences.getInt(KEY_VERSION_CODE, DEFAULT_INT);
        boolean isWelcomeScreenShown = SharePreferences.getBool(KEY_IS_WELCOME_SCREEN_SHOWN, DEFAULT_BOOLEAN);
        boolean isDialogShownForPublicHoliday = SharePreferences.getBool(KEY_DIALOG_SHOWN_FOR_PUBLIC_HOLIDAY, DEFAULT_BOOLEAN);
        boolean isDialogShownForTracking = SharePreferences.getBool(KEY_DIALOG_SHOWN_FOR_TRACKING, DEFAULT_BOOLEAN);
        //boolean purchaseSubscription = SharePreferences.getBool(KEY_PURCHASE_SUBSCRIPTION, DEFAULT_BOOLEAN);
        //For Tracking
        String userFirebaseKey = SharePreferences.getStr(KEY_USER_FIREBASE_KEY, DEFAULT_STRING);
        boolean isTrackingEnable = SharePreferences.getBool(KEY_IS_TRACKING_ENABLE, DEFAULT_BOOLEAN);
        String trackingDeviceId = SharePreferences.getStr(KEY_TRACKING_DEVICE_ID, DEFAULT_STRING);


        editor.clear();
        editor.commit();

        SharePreferences.setInt(KEY_VERSION_CODE, versionCode);
        SharePreferences.setBool(KEY_IS_WELCOME_SCREEN_SHOWN, isWelcomeScreenShown);
        SharePreferences.setBool(KEY_DIALOG_SHOWN_FOR_PUBLIC_HOLIDAY, isDialogShownForPublicHoliday);
        SharePreferences.setBool(KEY_DIALOG_SHOWN_FOR_TRACKING, isDialogShownForTracking);
        SharePreferences.setStr(KEY_USER_FIREBASE_KEY, userFirebaseKey);
        SharePreferences.setBool(KEY_IS_TRACKING_ENABLE, isTrackingEnable);
        SharePreferences.setStr(KEY_TRACKING_DEVICE_ID, trackingDeviceId);
        //SharePreferences.setBool(KEY_PURCHASE_SUBSCRIPTION, purchaseSubscription);
    }

    //This was called when we wants to clear application catch
    //which was trigger from notification
    public static void clearWholeApplicationSP() {
        editor.clear();
        editor.commit();
    }
}
