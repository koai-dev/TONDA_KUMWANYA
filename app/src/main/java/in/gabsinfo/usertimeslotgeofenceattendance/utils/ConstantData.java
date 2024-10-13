package in.gabsinfo.usertimeslotgeofenceattendance.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.DecimalFormat;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;

public class ConstantData {

    private static Context context = AttendanceApplication.getAppContext();


    //Fragments Tags
    public static final String Validate_Company_Fragment = "Validate_Company_Fragment";
    public static final String Company_Registration_Fragment = "Company_Registration_Fragment";
    public static final String User_Type_Selection_Fragment = "User_Type_Selection_Fragment";
    public static final String Admin_Sign_In_Fragment = "Admin_Sign_In_Fragment";
    public static final String User_Sign_In_Fragment = "User_Sign_In_Fragment";
    public static final String Admin_Sign_Up_Fragment = "Admin_Sign_Up_Fragment";
    public static final String CONST_LOG_FILE = context.getResources().getString(R.string.app_name) + ".jpeg";

    public static String FIREBASE_NOTIFICATION_MESSAGE_TOPIC="Users";
    public static String FIREBASE_NODE="GEO_FENCE_ATTENDANCE";
    public static String FIREBASE_NODE_PUBLIC_HOLIDAY="PUBLIC_HOLIDAY";
    public static String FIREBASE_NODE_COMPANY_DETAIL="COMPANY_DETAIL";
    public static String FIREBASE_NODE_COMPANY_LOCATIONS="COMPANY_LOCATIONS";
    public static String FIREBASE_NODE_COMPANY_USERS="COMPANY_USER";
    public static String FIREBASE_NODE_COMPANY_USERS_ATTENDANCE="COMPANY_USER_ATTENDANCE";
    public static String FIREBASE_NODE_PHONE_MAPPING_REQUEST="PHONE_MAPPING_REQUEST";
    public static String FIREBASE_NODE_LOCATION_TRACKING="LOCATION_TRACKING";
    public static String FIREBASE_NODE_TIMESHEET="TIMESHEET";
    public static String FIREBASE_NODE_TIMESHEET_CATEGORY="TIMESHEET_CATEGORY";
    public static String FIREBASE_NODE_NOTES="NOTES";
    public static String FIREBASE_STORAGE_NODE_EMPLOYEE_PROFILE_IMAGE="EMPLOYEE_PROFILE_IMAGE";
    public static String TYPE_ADMIN="ADMIN";
    public static String TYPE_USER="USER";
    public static String FIREBASE_LEAVE_NODE="LEAVE";
    public static String FIREBASE_LEAVE_NODE_LEAVE_MASTER="LEAVE_MASTER";
    public static String FIREBASE_LEAVE_NODE_TOTAL_LEAVE_TAKEN_BY_EMPLOYEE="TOTAL_LEAVE_TAKEN_BY_EMPLOYEE";
    public static String FIREBASE_LEAVE_NODE_LEAVE_APPLICATION_BY_EMPLOYEE="LEAVE_APPLICATION_BY_EMPLOYEE";

    public static String WORK_TYPE_DAY_WISE="Day Wise";
    public static String WORK_TYPE_HOUR_WISE="Hour Wise";
    public static String WORK_TYPE_MONTH_WISE="Month Wise";

    public static String TWENTY_FOUR_HOURS_FORMAT="HH:mm";
    public static String TWELVE_HOURS_FORMAT="hh:mm aa";
    public static String DAY_FORMAT="dd";
    public static String MONTH_FORMAT="MMM";
    public static String YEAR_FORMAT="yyyy";
    public static String DATE_FORMAT=DAY_FORMAT+" "+MONTH_FORMAT+" "+YEAR_FORMAT;
    public static String DATE_HOUR_FORMAT=DAY_FORMAT+" "+MONTH_FORMAT+" "+YEAR_FORMAT+" "+TWELVE_HOURS_FORMAT;
    public static String MONTH_YEAR_FORMAT=MONTH_FORMAT+" "+YEAR_FORMAT;
    public static String TWENTY_FOUR_HOUR_DATE_FORMAT=DAY_FORMAT+" "+MONTH_FORMAT+" "+YEAR_FORMAT+" "+"HH:mm";
    public static String TWELVE_HOUR_DATE_FORMAT=DAY_FORMAT+" "+MONTH_FORMAT+" "+YEAR_FORMAT+" "+"hh:mm a";
    public String CURRENT_MONTH=String.valueOf(DateFormat.format(MONTH_FORMAT, new java.util.Date()));
    public String CURRENT_YEAR=String.valueOf(DateFormat.format(YEAR_FORMAT, new java.util.Date()));
    public String CURRENT_DAY=String.valueOf(DateFormat.format(DAY_FORMAT, new java.util.Date()));

    public static String PUNCH_IN_NOW = "Punch IN Now";
    public static String PUNCH_OUT_NOW = "Punch OUT Now";
    public static String GOOD_BYE = "Good Bye";
    public static String YET_TO_COME = "Yet to come";
    public static String YET_TO_LEAVE = "Yet to leave";

    public static String CONST_IN = "IN";
    public static String CONST_OUT = "OUT";
    public static String CONST_DONE = "DONE";

    //Convert amount to round up
    //Round Up - https://www.mkyong.com/java/how-to-round-double-float-value-to-2-decimal-points-in-java/
    public static DecimalFormat df = new DecimalFormat("0.00");

    public static final String CONST_LANGUAGE_FOR_ENGLISH = "ENGLISH";
    public static final String CONST_LANGUAGE_CODE_FOR_ENGLISH = "en";
    public static final String CONST_LANGUAGE_FOR_GUJARATI = "ગુજરાતી";
    public static final String CONST_LANGUAGE_CODE_FOR_GUJARATI = "gu";
    public static final String CONST_LANGUAGE_FOR_HINDI = "HINDI";
    public static final String CONST_LANGUAGE_CODE_FOR_HINDI = "hi";


    public static final String SUNDAY = "SUNDAY";
    public static final String MONDAY = "MONDAY";
    public static final String TUESDAY = "TUESDAY";
    public static final String WEDNESDAY = "WEDNESDAY";
    public static final String THURESDAY = "THURSDAY";
    public static final String FRIDAY = "FRIDAY";
    public static final String SATURDAY = "SATURDAY";
    public static final String DEFAULT_SHOP_OPEN_TIME = "10:00 AM";
    public static final String DEFAULT_SHOP_CLOSE_TIME = "07:00 PM";
    public static final String DEFAULT_FULL_DAY_HOURS = "09:00";
    public static final String DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY = "00:00";


    /*Firebase*/
    public static final String NOTIFICATION_TITLE_EMPLOYEE_IN = "Employee In";
    public static final String NOTIFICATION_TITLE_EMPLOYEE_OUT = "Employee Out";
    public static final String NOTIFICATION_TITLE_NEW_UPDATE_AVAILABLE = "New update is available";
    public static final String NOTIFICATION_TITLE_EXTRA = "Extra";


    public static final String SUBSCRIBE_ALL_COMPANY_USERS = "AllCompanyUsers";//For all user including all the companies

    public static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    public static final String FCM_SERVER_KEY = "key=AAAA2Mq4VYg:APA91bHxHWZMru_pu2GdC5X9RdOoeqfJldbsxz7f5CkEYSQ3E90KfjgoM5l1NVgXz91RmrGRGx_Ru62kvq5ta2FNWi1aRkbtNJvKtghyZty1x7GTRlGkcU86QgrA7vz9m7V-675MEU-G";


    public static final int TIME_SHEET_ADD=12;
    public static final String DATA_MODEL="DataModel";
    public static final String TIME_SHEET_ENTRY_MODEL="TimeSheetEntryModel";

    public static String START_LOCATION_SERVICE = "in.gabsinfo.usertimeslotgeofenceattendance.service.TrackingService.action.startforeground";
    public static String STOP_LOCATION_SERVICE = "in.gabsinfo.usertimeslotgeofenceattendance.service.TrackingService.action.stopforeground";
    public static int NOTIFICATION_ID_FOR_LOCATION = 1001;
    public static final String CHANNEL_ID_FOR_LOCATION = "Location Update";
    public static final String CHANNEL_NAME_FOR_LOCATION = "Location Update Channel";
}
