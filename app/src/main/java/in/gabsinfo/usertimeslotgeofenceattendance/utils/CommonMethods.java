package in.gabsinfo.usertimeslotgeofenceattendance.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.Interface.IPermissionGrantedListener;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.SignInActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardPermissionActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.LatLong;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.ShopTimingModel;
import in.gabsinfo.usertimeslotgeofenceattendance.service.SendFCMNotificationService;
import in.gabsinfo.usertimeslotgeofenceattendance.service.TrackingService;

public class CommonMethods {

    public static SimpleDateFormat actualTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    public static SimpleDateFormat requiredTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
    public static Dialog mDialog;
    int pageNumber=0;
    public static void cancelProgressDialog() {
        if (mDialog != null
                && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public static void showProgressDialog(Activity activity) {
        if (activity != null
                && mDialog == null) {
            mDialog = new Dialog(activity);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setContentView(R.layout.dialog);

            mDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, android.R.color.transparent));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(mDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;

            mDialog.getWindow().setAttributes(lp);
            mDialog.show();
        }
    }

    public static Boolean hasCameraPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return CommonMethods.isPermissionsGranted(context, Manifest.permission.CAMERA);
        } else
            return true;
    }

    public static Boolean hasPostNotificationPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return CommonMethods.isPermissionsGranted(context, Manifest.permission.POST_NOTIFICATIONS);
        } else
            return true;
    }

    public static Boolean hasLocationPermissions(Context context) {

        boolean fineLocation = CommonMethods.isPermissionsGranted(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            boolean foreground = CommonMethods.isPermissionsGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
            return fineLocation && foreground;
        } else {
            return fineLocation;
        }
    }

    public static Boolean hasBackgroundLocationPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return CommonMethods.isPermissionsGranted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            return true;
        }
    }

    public static Boolean hasReadStoragePermissions(Context context) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true;
        } else
            return CommonMethods.isPermissionsGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE);*/
        return true;
    }

    public static boolean isPermissionsGranted(Context activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isGPSEnabled(Context context){
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model.toUpperCase();
        }
        return manufacturer.toUpperCase() + " " + model;
    }

    @SuppressLint("HardwareIds")
    public static  String getDeviceId() {
        String strdeviceID = "";
        strdeviceID = Settings.Secure.getString(AttendanceApplication.getAppContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (strdeviceID != null)
            strdeviceID = strdeviceID.toUpperCase();
        return strdeviceID;
    }

    public static void showAlertForLogout(final Activity activity) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity,R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(activity.getString(R.string.title_alert_confirm_logout));
        alertDialogBuilder.setMessage(activity.getString(R.string.msg_alert_confirm_logout))
                .setCancelable(true)
                .setPositiveButton(activity.getString(R.string.action_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                SharePreferences.clearSP();

                                activity.startActivity(new Intent(activity, SignInActivity.class));
                                activity.finish();
                            }
                        })
                .setNegativeButton(activity.getString(R.string.action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public static void showAlertForExit(final Activity activity) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity,R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(activity.getString(R.string.alert_title_exit));
        alertDialogBuilder.setMessage(activity.getString(R.string.alert_msg_exit))
                .setCancelable(true)
                .setPositiveButton(activity.getString(R.string.action_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                activity.finish();
                            }
                        })
                .setNegativeButton(activity.getString(R.string.action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setNeutralButton(activity.getString(R.string.action_rate_us),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                rateApplication(activity);
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public static void showAlertForTimeSlotNotFound(final Activity activity) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity, R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(activity.getString(R.string.alert_title_time_slot_not_found));
        alertDialogBuilder.setMessage(activity.getString(R.string.alert_msg_alert_time_slot_not_found))
                .setCancelable(true)
                .setPositiveButton(activity.getString(R.string.action_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                activity.onBackPressed();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }


    public static void shareApplication(Activity activity) {
        try {
            int applicationNameId = activity.getApplicationInfo().labelRes;
            final String appPackageName = activity.getPackageName();
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, activity.getString(applicationNameId));

            String link = "https://play.google.com/store/apps/details?id=" + appPackageName + "&hl=en";
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<b>"+activity.getString(R.string.app_name)+"</b> -  The extra ordinary Geo-fence attendance App.\n" +
                    "Please Download App From Below Link And <b>Provide Rate & Review</b> And Share Application.")
                    + " " + link);

            activity.startActivity(Intent.createChooser(i, "Share link:"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void rateApplication(Activity activity) {

        final String appPackageName = activity.getPackageName(); // getPackageName() from Context or Activity object
        try {

            Uri uri = Uri.parse("market://details?id=" + appPackageName);
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            activity.startActivity(goToMarket);

        } catch (ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void moreAppsFromCompany(Activity activity) {
        String developerName = "Gabs Infotech";     //where geeks is the company name in the play store
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=" + developerName)));
        } catch (ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/search?q=" + developerName + "&hl=en")));
        }

    }



    public static void call(Activity activity, String mobileNo) {
        try {
            //Intent my_callIntent = new Intent(Intent.ACTION_CALL);
            Intent my_callIntent = new Intent(Intent.ACTION_DIAL);
            my_callIntent.setData(Uri.parse("tel:" + mobileNo));
            //here the word 'tel' is important for making a call...
            activity.startActivity(my_callIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "Error in your phone call" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    //http://www.java67.com/2018/01/how-to-create-random-alphabetic-or-alphanumeric-string-java.html
    static final String SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz";
    static SecureRandom secureRnd = new SecureRandom();

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(SOURCE.charAt(secureRnd.nextInt(SOURCE.length())));
        return sb.toString();
    }


    public static void mapNavigation(Activity activity, double latitude, double longitude) {
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="
                    + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            activity.startActivity(mapIntent);
        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("NewApi")
    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isDeviceSupportCamera(Activity act) {
        // if this device has a camera
        return act.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    /**
     * Method is used to get file size in KB.
     *
     * @param file
     */
    public static double getFileSizeInKB(File file) {

        long size = 0;
        size = file.length();
        return (double) size / 1024;
    }

    public static float getFileSizeInMB(File file) {

        /*int fileSize = 0;
        try {
            fileSize = Integer.parseInt(String.valueOf(file.length() / 1024)); // file size in KB
            Log.e("fileSize", "" + fileSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileSize;*/

        float size = 0;
        size = file.length();
        return (float) size / 1024 / 1024;
    }

    public static boolean isNetworkConnected(Context activtiy) {
        ConnectivityManager cmss = (ConnectivityManager) activtiy
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo niss = cmss.getActiveNetworkInfo();
        return niss != null;
    }

    public static void showAlertDailogueWithOK(Activity activity, String title,
                                               String message, String posiBtn) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity,R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message).setPositiveButton(posiBtn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public static void showConnectionAlert(final Context context) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                Objects.requireNonNull(context),R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(context.getString(R.string.alert_title_connectionError));
        alertDialogBuilder.setMessage(context.getString(R.string.alert_msg_connectionError))
                .setPositiveButton(context.getString(R.string.action_ok), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

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

    public static String get24HoursFromMinutes(int minutes) {
        Date minutesInDate = null;
        try {
            minutesInDate = new SimpleDateFormat("mm", Locale.US).parse(String.valueOf(minutes));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (minutesInDate != null) {
            return new SimpleDateFormat("HH:mm", Locale.US).format(minutesInDate);
        }
        return String.valueOf(0);
    }

    /**
     * @param punchInTime
     * @param punchOutTime
     * @return
     */
    public static int calculateTotalHours(String punchInTime, String punchOutTime) {

        SimpleDateFormat requiredTimeFormat = new SimpleDateFormat("hh:mm aa", Locale.US);

        Date punchInDateTime = null;
        try {
            punchInDateTime = requiredTimeFormat.parse(punchInTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date punchOutDateTime = null;
        try {
            punchOutDateTime = requiredTimeFormat.parse(punchOutTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (punchInDateTime != null
                && punchOutDateTime != null) {
            long mills = punchOutDateTime.getTime() - punchInDateTime.getTime();
            Log.v("Data1", "" + punchInDateTime.getTime());
            Log.v("Data2", "" + punchOutDateTime.getTime());
            int hours = (int) (mills / (1000 * 60 * 60));
            int mins = (int) (mills / (1000 * 60)) % 60;

            return (hours*60) + mins;
        }
        return 0;
    }

    public static void showAlertForChangeDate(final Activity mActivity) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                Objects.requireNonNull(mActivity),R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(mActivity.getString(R.string.alert_title_date_modified));
        alertDialogBuilder.setMessage(mActivity.getString(R.string.alert_msg_date_modified))
                .setPositiveButton(mActivity.getString(R.string.action_make_date_time_proper), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        mActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 26);
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    public static boolean isAutoDateTimeEnabled(Context context) {
        try {

            String timeSettings = "";
            if (Build.VERSION.SDK_INT > 17) {
                timeSettings = Settings.System.getString(
                        context.getContentResolver(),
                        Settings.System.AUTO_TIME);

                if (timeSettings.contentEquals("0")) {
//					android.provider.Settings.Global.putString(
//							context.getContentResolver(),
//							android.provider.Settings.Global.AUTO_TIME, "1");
                    return false;
                } else {
                    return true;
                }
            }
            // SDK < 16
            else {
                timeSettings = Settings.System.getInt(
                        context.getContentResolver(),
                        Settings.System.AUTO_TIME) + "";

                if (timeSettings.contentEquals("0")) {
//					android.provider.Settings.System.putInt(
//							context.getContentResolver(),
//							android.provider.Settings.System.AUTO_TIME, 1);
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss",Locale.US);
        Date date = new Date();
        System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48
        return dateFormat.format(date);
    }

    public static enum SubDirectory {
        APP_PDF_TIMESHEET_REPORT_DATA("REPORT"),APP_PDF_SALARY_SLIP_DATA("SALARY_SLIP"),APP_XLS_DATA("XLS"), APP_LOG_DIRECTORY("Log"),
        APP_PDF_DATA("PDF");
        private final String subDirectoryName;

        private SubDirectory(String subDirectoryName) {
            this.subDirectoryName = subDirectoryName;
        }

        @Override
        public String toString() {
            return subDirectoryName;
        }
    }


    public static File getApplicationDirectory(SubDirectory subFolderName,
                                               Context activity, boolean isPublic) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File internalDownloadDirectory = new File(getMountedPaths(), activity.getResources().getString(R.string.app_name));
            if (!internalDownloadDirectory.exists()) {
                internalDownloadDirectory.mkdirs();
            }
            internalDownloadDirectory.setReadable(true);
            internalDownloadDirectory.setWritable(true);
            return internalDownloadDirectory;
        } else {
            File directory;
            if (isPublic) {
                directory = new File(activity.getExternalFilesDir(null)
                        + "/"
                        + subFolderName);

            } else {
                directory = activity.getFilesDir();
            }
            if (directory == null
                    || (!directory.exists() && !directory.mkdirs())) {
                return null;
            }
            return directory;
        }
    }


    private static String getMountedPaths() {
        String sdcardPath = "";
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        try {
            proc = runtime.exec("mount");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        InputStream is = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        String line;
        BufferedReader br = new BufferedReader(isr);
        try {
            while ((line = br.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("fat")) {// TF card
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        sdcardPath = columns[1];
                    }
                } else if (line.contains("fuse")) {// internal storage
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        sdcardPath = columns[1];
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sdcardPath;
    }

    /**
     * This is for to round up the Float value
     */
    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    /**
     * @return Current Date only
     */
    public String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        return String.valueOf(mDay) + "/" + String.valueOf(mMonth + 1) + "/"
                + String.valueOf(mYear);
    }

    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        try {
            double earthRadius = 3958.75;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                    * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double dist = earthRadius * c;

            int meterConversion = 1609;

            return (float) (dist * meterConversion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (float) 0;
    }


    /* Find Nearest Location */
    public PersonModel getNearestMarker(ArrayList<LatLong> markers,
                                        LatLong origin, PersonModel mainModel) {
        // TODO Auto-generated method stub

        String branchCode = "";
        String branchName = "";
        LatLong nearestMarker = null;
        float nearestLocationRadius = (float) 0;
        double lowestDistance = Double.MAX_VALUE;

        if (markers != null) {

            for (LatLong marker : markers) {

                double dist = distFrom(
                        Float.valueOf(String.valueOf(origin.getLat())),
                        Float.valueOf(String.valueOf(origin.getLng())),
                        Float.valueOf(String.valueOf(marker.getLat())),
                        Float.valueOf(String.valueOf(marker.getLng())));

                if (dist < lowestDistance) {
                    nearestMarker = marker;
                    lowestDistance = dist;
                    nearestLocationRadius = marker.getRadius();
                    branchCode=marker.getBranchCode();
                    branchName=marker.getBranchName();
                }
            }
        }
        mainModel.setLatitude(String.valueOf(nearestMarker.getLat()));
        mainModel.setLongitude(String.valueOf(nearestMarker.getLng()));
        mainModel.setBranchCode(branchCode);
        mainModel.setBranchName(branchName);
        mainModel.setRadius(nearestLocationRadius);

        return mainModel;
    }

    public static boolean isPlayServicesAvailable(Context context) {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, resultCode, 2).show();
            return false;
        }
        return true;
    }

    public static double[] getDayTypeForMonthWise(ShopTimingModel timeModel,
                                                String strSelectedPunchInTime, String strSelectedPunchOutTime) {
        double[] doubles=new double[2];
        int totalWorkingMinutes = CommonMethods.calculateTotalHours(strSelectedPunchInTime,
                strSelectedPunchOutTime);
        String totalWorkingHours=CommonMethods.get24HoursFromMinutes(totalWorkingMinutes);

        SimpleDateFormat actTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        Date totalWorkingHoursInDate = null;
        try {
            totalWorkingHoursInDate = actTimeFormat.parse(totalWorkingHours);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date hourForFullDayInDate = null;
        try {
            hourForFullDayInDate = actTimeFormat.parse(timeModel.getHoursForFullDay());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(timeModel.isHalfDayAllow()) {

            Date hourForHalfDayInDate = null;
            try {
                hourForHalfDayInDate = actTimeFormat.parse(timeModel.getHoursForHalfDay());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (totalWorkingHoursInDate.compareTo(hourForFullDayInDate) == 0
                    || totalWorkingHoursInDate.compareTo(hourForFullDayInDate) > 0) {
                doubles[0]=1;
                if (totalWorkingHoursInDate.compareTo(hourForFullDayInDate) > 0) {
                    long diff = totalWorkingHoursInDate.getTime() - hourForFullDayInDate.getTime();
                    int hours = (int) (diff / (1000 * 60 * 60));
                    int mins = (int) (diff / (1000 * 60)) % 60;
                    doubles[1] =(hours*60)+mins;
                } else {
                    doubles[1] = 0;
                }
            } else if (totalWorkingHoursInDate.compareTo(hourForHalfDayInDate) == 0
                    || totalWorkingHoursInDate.compareTo(hourForHalfDayInDate) > 0) {
                doubles[0]=0.5;
                doubles[1]=0;
            } else {
                doubles[0]=0;
                doubles[1]=0;
            }
            return doubles;
        }else{
            if (totalWorkingHoursInDate.compareTo(hourForFullDayInDate) == 0
                    || totalWorkingHoursInDate.compareTo(hourForFullDayInDate) > 0) {
                doubles[0] = 1;
                if (totalWorkingHoursInDate.compareTo(hourForFullDayInDate) > 0) {
                    long diff = totalWorkingHoursInDate.getTime() - hourForFullDayInDate.getTime();
                    int hours = (int) (diff / (1000 * 60 * 60));
                    int mins = (int) (diff / (1000 * 60)) % 60;
                    doubles[1] =(hours*60)+mins;
                } else {
                    doubles[1] = 0;
                }
            } else {
                doubles[0]=0;
                doubles[1]=0;
            }
            return doubles;
        }
    }


    public static boolean isLanguageSupported(Context context, String text) {

        final int WIDTH_PX = 200;
        final int HEIGHT_PX = 80;

        int w = WIDTH_PX, h = HEIGHT_PX;
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(w, h, conf);
        Bitmap orig = bitmap.copy(conf, false);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setTextSize((int) (14 * scale));

        // draw text to the Canvas
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;

        canvas.drawText(text, x, y, paint);
        boolean res = !orig.sameAs(bitmap);
        orig.recycle();
        bitmap.recycle();
        return res;
    }


    public static File getApplicationDirectoryForLog(SubDirectory subFolderName,
                                                     Context activity, boolean isPublic) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File internalDownloadDirectory = new File(getMountedPaths(), activity.getResources().getString(R.string.app_name));
            if (!internalDownloadDirectory.exists()) {
                internalDownloadDirectory.mkdirs();
            }
            internalDownloadDirectory.setReadable(true);
            internalDownloadDirectory.setWritable(true);
            return internalDownloadDirectory;
        } else {
            File directory;
            if (isPublic) {
                directory = new File(activity.getExternalFilesDir(null)
                        + "/"
                        + subFolderName);

            } else {
                directory = activity.getFilesDir();
            }
            if (directory == null
                    || (!directory.exists() && !directory.mkdirs())) {
                return null;
            }
            return directory;
        }
    }

    public static String getAddressOnlyFromLocation(Activity activity,double latitude,double longitude){
        String address="";
        Geocoder geocoder = new Geocoder(activity,
                Locale.getDefault());

        List<Address> addresses = new ArrayList<Address>();
        try {
            addresses = geocoder.getFromLocation(latitude,
                    longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            address = (addresses.get(0).getAddressLine(0) != null) ? addresses
                    .get(0).getAddressLine(0) : "";
        }

        if (address == null) {
            address="";
        }

        return address;
    }

    public static List<Address> getAddressObjectFromLocation(Activity activity,double latitude,double longitude){
        Geocoder geocoder = new Geocoder(activity,
                Locale.getDefault());

        List<Address> addresses = new ArrayList<Address>();
        try {
            addresses = geocoder.getFromLocation(latitude,
                    longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    public static void loadImage(Activity activity, String strUrl, ImageView imageView, Drawable errorDrawable) {

        Glide.with(activity).load(strUrl)
                .thumbnail(0.1f)
                .error(errorDrawable)
                .placeholder(ContextCompat.getDrawable(activity, R.drawable.loading_transparent))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
        imageView.setColorFilter(ContextCompat.getColor(activity, android.R.color.transparent));
    }

    public static void loadDefaultImage(Activity activity, ImageView imageView,Drawable defaultDrawable) {

        Glide.with(activity).load(defaultDrawable)
                .into(imageView);
        //imageView.setColorFilter(ContextCompat.getColor(activity, R.color.profile_default_tint));
    }



    /**
     * Trigger notification while employee marking their attendance
     */
    public  static void sendNotificationForAttendance(Activity activity, PersonModel mPersonModel, AttendanceModel attendanceModel, String strPunchType){
        if (mPersonModel.isNotifyAdminForPunchIn()
                || mPersonModel.isNotifyAdminForPunchOut()) {
            try {

                JSONObject dataObj = new JSONObject();
                if (mPersonModel.isNotifyAdminForPunchIn()
                        && strPunchType.equals("Punch In")) {
                    dataObj.put("title", ConstantData.NOTIFICATION_TITLE_EMPLOYEE_IN);
                    dataObj.put("notificationTitle", ConstantData.NOTIFICATION_TITLE_EMPLOYEE_IN);
                    if(attendanceModel.getPunchInLocationCode().contains("Mark From AnyWhere")){
                        //Means employee are allowed to mark punch from anywhere and hence there is no any punch location code
                        dataObj.put("notificationContent", "'" + mPersonModel.getName() + "' (" + mPersonModel.getDesignation() + ") come to working place at "
                                + attendanceModel.getPunchDate().concat(" " + attendanceModel.getPunchInTime()) + ".");
                    }else {
                        dataObj.put("notificationContent", "'" + mPersonModel.getName() + "' (" + mPersonModel.getDesignation() + ") come to working place '"
                                + mPersonModel.getBranchName() + "' at " + attendanceModel.getPunchDate().concat(" " + attendanceModel.getPunchInTime()) + ".");
                    }

                    JSONObject mainObj = new JSONObject();
                    mainObj.put("data", dataObj);
                    mainObj.put("to", "/topics/" +"Admin");

                    Intent cbIntent = new Intent(activity, SendFCMNotificationService.class);
                    cbIntent.putExtra("message", mainObj.toString());
                    activity.startService(cbIntent);

                } else if (mPersonModel.isNotifyAdminForPunchOut()
                        && strPunchType.equals("Punch Out")) {
                    dataObj.put("title", ConstantData.NOTIFICATION_TITLE_EMPLOYEE_OUT);
                    dataObj.put("notificationTitle", ConstantData.NOTIFICATION_TITLE_EMPLOYEE_OUT);
                    if(attendanceModel.getPunchInLocationCode().contains("Mark From AnyWhere")){
                        //Means employee are allowed to mark punch from anywhere and hence there is no any punch location code
                        dataObj.put("notificationContent", "'" + mPersonModel.getName() + "' (" + mPersonModel.getDesignation() + ") leave working place at "
                                + attendanceModel.getPunchDate().concat(" " + attendanceModel.getPunchOutTime()) + ".");
                    }else {
                        dataObj.put("notificationContent", "'" + mPersonModel.getName() + "' (" + mPersonModel.getDesignation() + ") leave working place '"
                                + mPersonModel.getBranchName() + "' at " + attendanceModel.getPunchDate().concat(" " + attendanceModel.getPunchOutTime()) + ".");
                    }

                    JSONObject mainObj = new JSONObject();
                    mainObj.put("data", dataObj);
                    mainObj.put("to", "/topics/" +"Admin");

                    Intent cbIntent = new Intent(activity, SendFCMNotificationService.class);
                    cbIntent.putExtra("message", mainObj.toString());
                    activity.startService(cbIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Send common broadcast message
     */
    public  static void sendCommonNotificationForAllEmployees(Activity activity,
                                                                        String notificationIdentity,
                                                                        String notificationTitle,
                                                                        String notificationContent,
                                                                        String notificationImageUrl,
                                                                        String topic){
        try {
            JSONObject dataObj = new JSONObject();
            dataObj.put("title", notificationIdentity);
            dataObj.put("notificationTitle", notificationTitle);
            dataObj.put("notificationContent", notificationContent);
            dataObj.put("notificationImageUrl",notificationImageUrl.trim().length()>0?notificationImageUrl:"");

            JSONObject mainObj = new JSONObject();
            mainObj.put("data", dataObj);
            mainObj.put("to", "/topics/" +topic);

            Intent cbIntent =  new Intent(activity, SendFCMNotificationService.class);
            cbIntent.putExtra("message", mainObj.toString());
            activity.startService(cbIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 14,
            Font.BOLD);
    //https://trinitytuts.com/create-pdf-file-in-android/

    public void createPDF(Activity activity, PersonModel mPersonModel,
                          ArrayList<AttendanceModel> mTransactionList, String selectMonthAndYear) throws DocumentException {

        CommonMethods.showProgressDialog(activity);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Collections.sort(mTransactionList, new Comparator< AttendanceModel >() {
                    @Override public int compare(AttendanceModel p1, AttendanceModel p2) {
                        return p1.getDay()- p2.getDay(); // Ascending
                    }
                });

                //create document file
                Document doc = new Document();
                try {
                    // CSV generation
                    File file = new File(CommonMethods.getApplicationDirectory(
                            CommonMethods.SubDirectory.APP_PDF_TIMESHEET_REPORT_DATA, activity, true),
                            mPersonModel.getName()+"_"+selectMonthAndYear+ ".pdf");
                    FileOutputStream fOut = new FileOutputStream(file);
                    PdfWriter writer = PdfWriter.getInstance(doc, fOut);

                    //open the document
                    doc.open();

                    Chunk mHeaderChunk = new Chunk("ATTENDANCE REPORT - "+mPersonModel.getName(), headerFont);
                    Paragraph mHeaderParagraph = new Paragraph(mHeaderChunk);
                    mHeaderParagraph.setAlignment(Element.ALIGN_CENTER);
                    doc.add(mHeaderParagraph);

                    Chunk mSubHeaderChunk = new Chunk(selectMonthAndYear,smallBold);
                    Paragraph mSubHeaderParagraph = new Paragraph(mSubHeaderChunk);
                    mSubHeaderParagraph.setAlignment(Element.ALIGN_CENTER);
                    doc.add(mSubHeaderParagraph);

                    Paragraph preface = new Paragraph();
                    addEmptyLine(preface, 1);
                    doc.add(preface);

                    try {
                        PdfPTable table = new PdfPTable(6);
                        // 100.0f mean width of table is same as Document size
                        table.setWidthPercentage(100.0f);
                        float[] columnWidth = new float[]{18, 8, 26,12,13, 23};
                        table.setWidths(columnWidth);

                        PdfPCell cell = new PdfPCell(new Paragraph(new Chunk("DATE", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("DAY", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("TIME DURATION", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("HOURS", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("OVER\nTIME", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("TYPE", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);
                        table.setHeaderRows(1);


                        int length = mTransactionList.size();
                        AttendanceModel model = null;
                        int TotalMinutes=0;
                        double TotalOverTime=0;
                        double TotalPresentDay=0;
                        double TotalHalfDayDay=0;
                        for (int i = 0; i < length; i++) {
                            model = mTransactionList.get(i);

                            if (model.getType().equalsIgnoreCase(activity.getString(R.string.label_public_holiday))
                                    || model.getType().equalsIgnoreCase(activity.getString(R.string.label_non_working_day))
                                    || model.getType().equalsIgnoreCase(activity.getString(R.string.label_absent_day))) {

                                cell = new PdfPCell(new Phrase(model.getDate()));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                if (model.getType().equalsIgnoreCase(activity.getString(R.string.label_absent_day)))
                                    cell = new PdfPCell(new Phrase(String.valueOf(0)));
                                else
                                    cell = new PdfPCell(new Phrase("-"));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase("-"));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase("-"));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase("-"));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase(model.getType()));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);
                            } else {
                                String NoOfDays = "0";
                                String timeDuration = "-";
                                String type = "-";

                                if (model.getPresentDay() == -1) {//Means user has not do punch out
                                    NoOfDays = String.valueOf(0);
                                } else {
                                    if (model.getPresentDay() % 1 == 0)  // true: it's an integer, false: it's not an integer
                                        NoOfDays = String.valueOf((int) model.getPresentDay());
                                    else
                                        NoOfDays = String.valueOf(model.getPresentDay());
                                }


                                if (model.getPresentDay() == -1) {//Means user has not do punch out
                                    timeDuration = model.getPunchInTime() + " - " +
                                            "NA";
                                } else {
                                    timeDuration = model.getPunchInTime() + " - " +
                                            model.getPunchOutTime();
                                }

                                if (model.getPresentDay() == -1) {
                                    type = activity.getString(R.string.label_out_pending);
                                } else if (model.getPresentDay() == 1) {
                                    type = activity.getString(R.string.label_full_days);
                                    TotalPresentDay++;
                                } else if (model.getPresentDay() == 0) {
                                    type = activity.getString(R.string.label_present_but_leave);
                                } else if (model.getPresentDay() == 0.5) {
                                    type = activity.getString(R.string.label_half_days);
                                    TotalHalfDayDay=TotalHalfDayDay=0.5;
                                }


                                cell = new PdfPCell(new Phrase(model.getDate()));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase(NoOfDays));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase(timeDuration));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                if (model.getTotalWorkingHours() != null) {
                                    String[] split = model.getTotalWorkingHours().split(":");
                                    Integer hours = Integer.valueOf(split[0]);
                                    Integer minuts = Integer.valueOf(split[1]);
                                    TotalMinutes = TotalMinutes + ((hours * 60) + minuts);
                                }

                                cell = new PdfPCell(new Phrase(model.getTotalWorkingHours()));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                TotalOverTime=TotalOverTime+model.getOverTimeInMinutes();
                                if ((int) model.getOverTimeInMinutes() == 0) {
                                    cell = new PdfPCell(new Phrase("-"));
                                } else {
                                    cell = new PdfPCell(new Phrase(get24HoursFromMinutes((int) model.getOverTimeInMinutes())));
                                }
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase(type));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);
                            }
                        }


                        cell = new PdfPCell(new Paragraph(new Chunk("TOTAL", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(
                                new Chunk(String.valueOf(TotalPresentDay+TotalHalfDayDay), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(""));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        Date mmDate = null;
                        try {
                            mmDate = new SimpleDateFormat("mm", Locale.US).parse(String.valueOf(TotalMinutes));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (mmDate != null) {
                            cell = new PdfPCell(new Paragraph(
                                    new Chunk(CommonMethods.actualTimeFormat.format(mmDate), smallBold)));
                        } else {
                            cell = new PdfPCell(new Paragraph(""));
                        }
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(
                                new Chunk(get24HoursFromMinutes((int)TotalOverTime), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);


                        cell = new PdfPCell(new Paragraph(""));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);
                        doc.add(table);


                        addEmptyLine(new Paragraph(), 2);
                        doc.add(preface);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                /*Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);
                                }else{
                                    uri = Uri.fromFile(file);
                                }


                                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(activity),
                                        R.style.CustomDialogTheme);
                                @SuppressLint("InflateParams")
                                View view = LayoutInflater.from(activity).inflate(R.layout.dialog_pdf_viewer, null);
                                PDFView pdfView = view.findViewById(R.id.pdfView);

                                pdfView.fromFile(file)
                                        .defaultPage(pageNumber)
                                        .enableSwipe(true) // allows to block changing pages using swipe
                                        .swipeHorizontal(false)
                                        .enableDoubletap(true)
                                        .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                                        .scrollHandle(new DefaultScrollHandle(activity))
                                        .onPageChange(new OnPageChangeListener() {
                                            @Override
                                            public void onPageChanged(int page, int pageCount) {
                                                pageNumber = page;
                                            }
                                        })
                                        .load();
                                builder.setView(view);
                                builder
                                        .setPositiveButton(activity.getString(R.string.action_share), null)
                                        .setNegativeButton(activity.getString(R.string.action_cancel), null);




                                final AlertDialog alertDialog = builder.create();
                                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(final DialogInterface dialog) {
                                        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                        btnPositive.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View view) {
                                                Intent shareIntent = new Intent();
                                                shareIntent.setAction(Intent.ACTION_SEND);
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                                shareIntent.setType("application/*");
                                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                activity.startActivity(Intent.createChooser(shareIntent, "Share ..."));
                                            }
                                        });

                                        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                                        btnNegative.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();

                                                if (file != null
                                                        && file.exists())
                                                    file.delete();
                                            }
                                        });
                                    }
                                });
                                alertDialog.setCancelable(false);
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.show();*/


                                Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);
                                }else{
                                    uri = Uri.fromFile(file);
                                }

                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                shareIntent.setType("application/*");
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                activity.startActivity(Intent.createChooser(shareIntent, "Share ..."));
                            }
                        });

                    } catch (DocumentException de) {
                        Log.e("PDFCreator", "DocumentException:" + de);
                    } finally {
                        doc.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonMethods.cancelProgressDialog();
                    }
                });
            }
        });
    }

    public void createPDFForSalarySlip(Activity activity, PersonModel mPersonModel,
                                       ArrayList<AttendanceModel> mTransactionList, String selectMonthAndYear,
                                       String strTotalDaysInMonth, String strTotalAttendedDays, String strTotalWorkingDays, String strTotalPresentDays,
                                       String strTotalHalfDays, String strTotalAbsentDays,
                                       String strTotalPublicHolidays,String strTotalNonWorkingDays,String strTotalPaidDays, String strAmountPerDay, String strTotalAmountToPay,
                                       boolean isPublicHolidayCountedInSalary,
                                       boolean isNonWorkingDayCountedInSalary) throws DocumentException {

        CommonMethods.showProgressDialog(activity);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Collections.sort(mTransactionList, new Comparator< AttendanceModel >() {
                    @Override public int compare(AttendanceModel p1, AttendanceModel p2) {
                        return p1.getDay()- p2.getDay(); // Ascending
                    }
                });

                //create document file
                Document doc = new Document();
                try {
                    // CSV generation
                    File file = new File(CommonMethods.getApplicationDirectory(
                            CommonMethods.SubDirectory.APP_PDF_SALARY_SLIP_DATA, activity, true),
                            mPersonModel.getName()+"_"+selectMonthAndYear+ ".pdf");
                    FileOutputStream fOut = new FileOutputStream(file);
                    PdfWriter writer = PdfWriter.getInstance(doc, fOut);

                    //open the document
                    doc.open();

                    Chunk mHeaderChunk = new Chunk("ATTENDANCE REPORT - "+mPersonModel.getName(), headerFont);
                    Paragraph mHeaderParagraph = new Paragraph(mHeaderChunk);
                    mHeaderParagraph.setAlignment(Element.ALIGN_CENTER);
                    doc.add(mHeaderParagraph);

                    Chunk mSubHeaderChunk = new Chunk(selectMonthAndYear,smallBold);
                    Paragraph mSubHeaderParagraph = new Paragraph(mSubHeaderChunk);
                    mSubHeaderParagraph.setAlignment(Element.ALIGN_CENTER);
                    doc.add(mSubHeaderParagraph);

                    Paragraph preface = new Paragraph();
                    addEmptyLine(preface, 1);
                    doc.add(preface);

                    try {
                        PdfPTable table = new PdfPTable(6);
                        // 100.0f mean width of table is same as Document size
                        table.setWidthPercentage(100.0f);
                        float[] columnWidth = new float[]{18, 8, 26,12,13, 23};
                        table.setWidths(columnWidth);

                        PdfPCell cell = new PdfPCell(new Paragraph(new Chunk("DATE", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("DAY", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("TIME DURATION", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("HOURS", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("OVER\nTIME", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(new Chunk("TYPE", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);
                        table.setHeaderRows(1);


                        int length = mTransactionList.size();
                        AttendanceModel model = null;
                        int TotalMinutes=0;
                        double TotalPresentDay=0;
                        double TotalHalfDayDay=0;
                        for (int i = 0; i < length; i++) {
                            model = mTransactionList.get(i);

                            if (model.getType().equalsIgnoreCase(activity.getString(R.string.label_public_holiday))
                                    || model.getType().equalsIgnoreCase(activity.getString(R.string.label_non_working_day))
                                    || model.getType().equalsIgnoreCase(activity.getString(R.string.label_absent_day))) {

                                cell = new PdfPCell(new Phrase(model.getDate()));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                if (model.getType().equalsIgnoreCase(activity.getString(R.string.label_absent_day)))
                                    cell = new PdfPCell(new Phrase(String.valueOf(0)));
                                else
                                    cell = new PdfPCell(new Phrase("-"));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase("-"));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase("-"));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase("-"));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase(model.getType()));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);
                            } else {
                                String NoOfDays = "0";
                                String timeDuration = "-";
                                String type = "-";

                                if (model.getPresentDay() == -1) {//Means user has not do punch out
                                    NoOfDays = String.valueOf(0);
                                } else {
                                    if (model.getPresentDay() % 1 == 0)  // true: it's an integer, false: it's not an integer
                                        NoOfDays = String.valueOf((int) model.getPresentDay());
                                    else
                                        NoOfDays = String.valueOf(model.getPresentDay());
                                }


                                if (model.getPresentDay() == -1) {//Means user has not do punch out
                                    timeDuration = model.getPunchInTime() + " - " +
                                            "NA";
                                } else {
                                    timeDuration = model.getPunchInTime() + " - " +
                                            model.getPunchOutTime();
                                }

                                if (model.getPresentDay() == -1) {
                                    type = activity.getString(R.string.label_out_pending);
                                } else if (model.getPresentDay() == 1) {
                                    type = activity.getString(R.string.label_full_days);
                                    TotalPresentDay++;
                                } else if (model.getPresentDay() == 0) {
                                    type = activity.getString(R.string.label_present_but_leave);
                                } else if (model.getPresentDay() == 0.5) {
                                    type = activity.getString(R.string.label_half_days);
                                    TotalHalfDayDay=TotalHalfDayDay=0.5;
                                }


                                cell = new PdfPCell(new Phrase(model.getDate()));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase(NoOfDays));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase(timeDuration));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                if (model.getTotalWorkingHours() != null) {
                                    String[] split = model.getTotalWorkingHours().split(":");
                                    Integer hours = Integer.valueOf(split[0]);
                                    Integer minuts = Integer.valueOf(split[1]);
                                    TotalMinutes = TotalMinutes + ((hours * 60) + minuts);
                                }

                                cell = new PdfPCell(new Phrase(model.getTotalWorkingHours()));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);


                                if ((int) model.getOverTimeInMinutes() == 0) {
                                    cell = new PdfPCell(new Phrase("-"));
                                } else {
                                    cell = new PdfPCell(new Phrase(get24HoursFromMinutes((int) model.getOverTimeInMinutes())));
                                }
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);

                                cell = new PdfPCell(new Phrase(type));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPadding(5f);
                                table.addCell(cell);
                            }
                        }


                        cell = new PdfPCell(new Paragraph(new Chunk("TOTAL", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(
                                new Chunk(String.valueOf(TotalPresentDay+TotalHalfDayDay), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        Date mmDate = null;
                        try {
                            mmDate = new SimpleDateFormat("mm", Locale.US).parse(String.valueOf(TotalMinutes));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (mmDate != null) {
                            cell = new PdfPCell(new Paragraph(
                                    new Chunk(CommonMethods.actualTimeFormat.format(mmDate), smallBold)));
                        } else {
                            cell = new PdfPCell(new Paragraph(""));
                        }
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5f);
                        table.addCell(cell);

                        cell = new PdfPCell(new Paragraph(""));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setColspan(2);
                        cell.setPadding(5f);
                        table.addCell(cell);
                        doc.add(table);

                        addEmptyLine(new Paragraph(), 2);
                        doc.add(preface);

                        //SALARY CALCULATION
                        Paragraph mSalaryCalParagraph = new Paragraph(new Chunk("SALARY CALCULATION",smallBold));
                        mSalaryCalParagraph.setAlignment(Element.ALIGN_CENTER);
                        doc.add(mSalaryCalParagraph);

                        addEmptyLine(new Paragraph(), 1);
                        doc.add(preface);

                        PdfPTable table1 = new PdfPTable(2);
                        // 100.0f mean width of table is same as Document size
                        table1.setWidthPercentage(100.0f);
                        float[] columnWidth1 = new float[]{50,50};
                        table1.setWidths(columnWidth1);

                        cell = new PdfPCell(new Paragraph(
                                new Chunk(activity.getString(R.string.label_decided_amount), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        String decidedAmount = mPersonModel.getCurrencySymbol()
                                .concat(CommonMethods.currencyFormatter(String.valueOf(mPersonModel.getAmount())));
                        if(mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_DAY_WISE)){
                            cell = new PdfPCell(new Paragraph(
                                    new Chunk(decidedAmount.concat(" / DAY"), smallBold)));
                        }else  if(mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_MONTH_WISE)){
                            cell = new PdfPCell(new Paragraph(
                                    new Chunk(decidedAmount.concat(" / MONTH"), smallBold)));
                        }
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Days In Month"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(strTotalDaysInMonth)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Attended Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(strTotalAttendedDays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        if(!isPublicHolidayCountedInSalary)
                            cell = new PdfPCell(new Phrase("Total Public Holidays"));
                        else
                            cell = new PdfPCell(new Phrase("Total Public Holidays (Count In Salary)"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(strTotalPublicHolidays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        if(!isNonWorkingDayCountedInSalary)
                            cell = new PdfPCell(new Phrase("Total Non Working Days"));
                        else
                            cell = new PdfPCell(new Phrase("Total Non Working Days (Count In Salary)"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(strTotalNonWorkingDays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Actual Working Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(strTotalWorkingDays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Present Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(strTotalPresentDays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Half Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(strTotalHalfDays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Absent Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        // Total Absent Days + Total Leaves
                        cell = new PdfPCell(new Phrase(String.valueOf(strTotalAbsentDays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Paragraph(
                                new Chunk("Total Paid Days", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Paragraph(
                                new Chunk(mPersonModel.getCurrencySymbol().concat(String.valueOf(strTotalPaidDays)), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Paragraph(
                                new Chunk("Amount Per Day", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Paragraph(
                                new Chunk(mPersonModel.getCurrencySymbol().concat(strAmountPerDay), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Paragraph(
                                new Chunk("SALARY", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Paragraph(
                                new Chunk(mPersonModel.getCurrencySymbol().concat(strTotalAmountToPay), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);
                        table1.setHeaderRows(1);
                        doc.add(table1);



                        //SALARY CALCULATION
                        /*Paragraph mSalaryCalParagraph = new Paragraph(new Chunk("SALARY CALCULATION",smallBold));
                        mSalaryCalParagraph.setAlignment(Element.ALIGN_CENTER);
                        doc.add(mSalaryCalParagraph);

                        addEmptyLine(new Paragraph(), 1);
                        doc.add(preface);

                        PdfPTable table1 = new PdfPTable(2);
                        // 100.0f mean width of table is same as Document size
                        table1.setWidthPercentage(100.0f);
                        float[] columnWidth1 = new float[]{50,50};
                        table1.setWidths(columnWidth1);


                        int totalDaysInMonth = 0;
                        int totalAttendedDays = 0;

                        SimpleDateFormat monthYearFormat = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US);
                        SimpleDateFormat monthFormat = new SimpleDateFormat(ConstantData.MONTH_FORMAT, Locale.US);
                        SimpleDateFormat yearFormat = new SimpleDateFormat(ConstantData.YEAR_FORMAT, Locale.US);

                        // Get a calendar instance
                        Calendar calendar = Calendar.getInstance();
                        if (selectMonthAndYear.equalsIgnoreCase(monthYearFormat.format(new Date()))) {
                            // Get the last date of the current month. To get the last date for a
                            // specific month you can set the calendar month using calendar object
                            // calendar.set(Calendar.MONTH, theMonth) method.
                            int lastDate = calendar.getActualMaximum(Calendar.DATE);
                            // Set the calendar date to the last date of the month so then we can
                            // get the last day of the month
                            calendar.set(Calendar.DATE, lastDate);
                            totalDaysInMonth = calendar.get(Calendar.DAY_OF_MONTH);
                        } else {
                            Date monthYearDate = null;
                            try {
                                monthYearDate = monthYearFormat.parse(selectMonthAndYear);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            calendar.set(Calendar.MONTH, monthYearDate.getMonth());
                            calendar.set(Calendar.YEAR, monthYearDate.getYear());
                            int lastDate = calendar.getActualMaximum(Calendar.DATE);
                            calendar.set(Calendar.DATE, lastDate);
                            totalDaysInMonth = calendar.get(Calendar.DAY_OF_MONTH);
                        }

                        //Here we check whether target month is current month or not
                        //if its current month then we count only those many days which
                        //are till now
                        assert selectMonthAndYear != null;
                        if (selectMonthAndYear.equalsIgnoreCase(monthYearFormat.format(new Date()))) {
                            totalAttendedDays = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                        } else {
                            Date monthYearDate = null;
                            try {
                                monthYearDate = monthYearFormat.parse(selectMonthAndYear);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (monthYearDate != null) {
                                int[] dayAndMonthNumber = NumberOfMondaysInMonth.getNumberofDaysAndMonthNumberByMonthName(
                                        monthFormat.format(monthYearDate).toUpperCase(), yearFormat.format(monthYearDate)
                                );
                                totalAttendedDays = dayAndMonthNumber[0];
                            }
                        }

                        double TotalPresentDayCount = 0;
                        double TotalHalfDays = 0;
                        double TotalAbsentDayCount = 0;
                        double TotalPublicHolidayCount = 0;
                        double TotalNonWorkingDayCount = 0;
                        assert mTransactionList != null;
                        if (mTransactionList.size() > 0) {
                            for (AttendanceModel attendanceModel :
                                    mTransactionList) {

                                if (attendanceModel.getType().equalsIgnoreCase(activity.getString(R.string.label_out_pending))) {
                                    TotalAbsentDayCount++;
                                } else if (attendanceModel.getType().equalsIgnoreCase(activity.getString(R.string.label_full_days))) {
                                    TotalPresentDayCount++;
                                } else if (attendanceModel.getType().equalsIgnoreCase(activity.getString(R.string.label_half_days))) {
                                    TotalHalfDays = TotalHalfDays + 0.5;
                                } else if (attendanceModel.getType().equalsIgnoreCase(activity.getString(R.string.label_present_but_leave))) {
                                    TotalAbsentDayCount++;
                                } else if (attendanceModel.getType().equalsIgnoreCase(activity.getString(R.string.label_public_holiday))) {
                                    TotalPublicHolidayCount++;
                                } else if (attendanceModel.getType().equalsIgnoreCase(activity.getString(R.string.label_non_working_day))) {
                                    TotalNonWorkingDayCount++;
                                }
                            }
                        }

                        // Total Attended Days - (Total Public Holiday + Total Non Working Days)
                        double totalWorkingDays = totalAttendedDays
                                - (TotalPublicHolidayCount + TotalNonWorkingDayCount);

                        // Leave day means user not come at office though its working days
                        // so we simply deduct sum of Total Present + Total Half Days
                        // from Total Working Days
                        double TotalLeavesCount = totalWorkingDays - (TotalPresentDayCount + TotalHalfDays);


                        cell = new PdfPCell(new Paragraph(
                                new Chunk(activity.getString(R.string.label_decided_amount), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        String decidedAmount = mPersonModel.getCurrencySymbol()
                                .concat(CommonMethods.currencyFormatter(String.valueOf(mPersonModel.getAmount())));
                        if(mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_DAY_WISE)){
                            cell = new PdfPCell(new Paragraph(
                                    new Chunk(decidedAmount.concat(" / DAY"), smallBold)));
                        }else  if(mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_MONTH_WISE)){
                            cell = new PdfPCell(new Paragraph(
                                    new Chunk(decidedAmount.concat(" / MONTH"), smallBold)));
                        }
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Days In Month"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(totalDaysInMonth)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Attended Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(totalAttendedDays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        if(!isPublicHolidayCountedInSalary)
                            cell = new PdfPCell(new Phrase("Total Public Holidays"));
                        else
                            cell = new PdfPCell(new Phrase("Total Public Holidays (Count In Salary)"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(TotalPublicHolidayCount)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        if(!isNonWorkingDayCountedInSalary)
                            cell = new PdfPCell(new Phrase("Total Non Working Days"));
                        else
                            cell = new PdfPCell(new Phrase("Total Non Working Days (Count In Salary)"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(TotalNonWorkingDayCount)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Actual Working Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(totalWorkingDays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Present Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(TotalPresentDayCount)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Half Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        cell = new PdfPCell(new Phrase(String.valueOf(TotalHalfDays)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Phrase("Total Absent Days"));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        // Total Absent Days + Total Leaves
                        cell = new PdfPCell(new Phrase(String.valueOf(TotalLeavesCount)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Paragraph(
                                new Chunk("Total Paid Days", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        double totalPaidDays=TotalPresentDayCount + TotalHalfDays ;
                        if (isPublicHolidayCountedInSalary)
                            totalPaidDays = totalPaidDays + TotalPublicHolidayCount;

                        if (isNonWorkingDayCountedInSalary)
                            totalPaidDays = totalPaidDays + TotalNonWorkingDayCount;

                        cell = new PdfPCell(new Paragraph(
                                new Chunk(mPersonModel.getCurrencySymbol().concat(String.valueOf(totalPaidDays)), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Paragraph(
                                new Chunk("Amount Per Day", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        double perDayAmount=0;
                        if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_MONTH_WISE)) {
                            assert mPersonModel != null;
                            perDayAmount = (double) mPersonModel.getAmount() / Double.valueOf(totalDaysInMonth);
                            cell = new PdfPCell(new Paragraph(
                                    new Chunk(mPersonModel.getCurrencySymbol().concat(CommonMethods.currencyFormatter(
                                            ConstantData.df.format(perDayAmount))), smallBold)));
                        } else if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_DAY_WISE)) {
                            assert mPersonModel != null;
                            perDayAmount = (double) mPersonModel.getAmount();
                            cell = new PdfPCell(new Paragraph(
                                    new Chunk(mPersonModel.getCurrencySymbol().concat(CommonMethods.currencyFormatter(
                                            ConstantData.df.format(perDayAmount))), smallBold)));
                        }
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);


                        cell = new PdfPCell(new Paragraph(
                                new Chunk("SALARY", smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);

                        double finalPayment = perDayAmount * totalPaidDays;
                        cell = new PdfPCell(new Paragraph(
                                new Chunk(mPersonModel.getCurrencySymbol().concat(CommonMethods.currencyFormatter(String.valueOf(finalPayment))), smallBold)));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setPadding(5f);
                        table1.addCell(cell);
                        table1.setHeaderRows(1);
                        doc.add(table1);*/


                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                /*Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);
                                }else{
                                    uri = Uri.fromFile(file);
                                }


                                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(activity),
                                        R.style.CustomDialogTheme);
                                @SuppressLint("InflateParams")
                                View view = LayoutInflater.from(activity).inflate(R.layout.dialog_pdf_viewer, null);
                                PDFView pdfView = view.findViewById(R.id.pdfView);
                                pdfView.fromFile(file)
                                        .defaultPage(pageNumber)
                                        .enableSwipe(true) // allows to block changing pages using swipe
                                        .swipeHorizontal(false)
                                        .enableDoubletap(true)
                                        .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                                        .scrollHandle(new DefaultScrollHandle(activity))
                                        .onPageChange(new OnPageChangeListener() {
                                            @Override
                                            public void onPageChanged(int page, int pageCount) {
                                                pageNumber = page;
                                            }
                                        })
                                        .load();

                                builder.setView(view);
                                builder
                                        .setPositiveButton(activity.getString(R.string.action_share), null)
                                        .setNegativeButton(activity.getString(R.string.action_cancel), null);


                                final AlertDialog alertDialog = builder.create();
                                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(final DialogInterface dialog) {
                                        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                        btnPositive.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View view) {
                                                Intent shareIntent = new Intent();
                                                shareIntent.setAction(Intent.ACTION_SEND);
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                                shareIntent.setType("application/*");
                                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                activity.startActivity(Intent.createChooser(shareIntent, "Share ..."));
                                            }
                                        });

                                        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                                        btnNegative.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();

                                                if (file != null
                                                        && file.exists())
                                                    file.delete();
                                            }
                                        });
                                    }
                                });
                                alertDialog.setCancelable(false);
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.show();*/

                                Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);
                                }else{
                                    uri = Uri.fromFile(file);
                                }

                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                shareIntent.setType("application/*");
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                activity.startActivity(Intent.createChooser(shareIntent, "Share ..."));
                            }
                        });

                    } catch (DocumentException de) {
                        Log.e("PDFCreator", "DocumentException:" + de);
                    } finally {
                        doc.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonMethods.cancelProgressDialog();
                    }
                });
            }
        });
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    public static void showDialogForInformation(Activity activity, String strTitle, String strMessage) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                Objects.requireNonNull(activity),R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(strTitle);
        alertDialogBuilder.setMessage(Html.fromHtml(strMessage))
                .setPositiveButton(activity.getString(R.string.action_got_it), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

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

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static String getWishForDay(Activity activity){
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay < 12){
            return activity.getString(R.string.label_good_morning);
        }else if(timeOfDay < 16){
            return  activity.getString(R.string.label_good_afternoon);
        }else if(timeOfDay < 21){
            return activity.getString(R.string.label_good_evening);
        }else {
            return activity.getString(R.string.label_good_night);
        }
    }

    public static void setTransparentStatusBar(Activity activity){
        //make translucent statusBar on kitkat devices
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            CommonMethods.setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            CommonMethods.setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
    private static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * Chooses random color defined in res/array.xml
     */
    public static int getRandomMaterialColor(Activity activity,String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = activity.getResources().getIdentifier("mdcolor_" + typeColor, "array", activity.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = activity.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }



    public static String currencyFormatter(String num) {
        double m = Double.parseDouble(num);
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return formatter.format(m);
    }

    public static void startSound(Activity activity,String filename) {
        AssetFileDescriptor afd = null;
        try {
            afd = activity.getResources().getAssets().openFd(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaPlayer player = new MediaPlayer();
        try {
            assert afd != null;
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.setVolume(1f, 1f);
        player.setLooping(false);
        player.start();
    }

    public static String convertStringToDateAndFindTime(String dateInString) {
        DateFormat parser = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US);
        DateFormat formator = new SimpleDateFormat("hh:mm a", Locale.US);
        Date date = null;
        try {
            date = parser.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String strTime = null;
        if(date != null) {
            try {
                strTime = formator.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strTime;
    }


    public static Date convertStringToDate(String dateInString) {
        DateFormat parser = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US);
        Date date = null;
        try {
            date = parser.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    //Convert Date to Calendar
    public static Calendar dateToCalendar(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }

    public static Date convertEventStringToDate(String eventDateInString) {
        DateFormat parser = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US);
        Date date = null;
        try {
            date = parser.parse(eventDateInString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date convertStringToDateTime(String dateInString) {
        DateFormat parser = new SimpleDateFormat(ConstantData.TWENTY_FOUR_HOUR_DATE_FORMAT, Locale.US);
        Date date = null;
        try {
            date = parser.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static int compareTimeWithOtherTime(String strEndTime,String strStartTime) {

        int result = 0;

        try {
            Date d = parseTime(strStartTime);
            Date d1 = parseTime(strEndTime);

            result = d1.compareTo(d);

            if (result == 1)// Greater then main compared date
            {
            } else if (result == -1)// Smaller then main compared date
            {
            } else if (result == 0)// Both are same
            {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    public static Date parseTime(String time) {

        SimpleDateFormat inputParser = new SimpleDateFormat("HH:mm",Locale.US);
        try {
            return inputParser.parse(time);
        } catch (ParseException e) {
            return null;
        }
    }

    //https://stackoverflow.com/questions/16335178/different-font-size-of-strings-in-the-same-textview
    public static void setFontSizeForPath(String wholeText,String targetText,TextView textView) {
        Spannable spannable = new SpannableString(wholeText);
        spannable.setSpan(new RelativeSizeSpan(1.5f),0, targetText.length(),  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // set size
        spannable.setSpan(new RelativeSizeSpan(1f), targetText.length(), wholeText.length(),  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // set size
        textView.setText( spannable );
    }

    /**
     * Send notification to particular employee for tracking purpose
     * @param personModel
     */
    public static void notifyTrackingStatusToEmployee(Activity activity,PersonModel personModel) {
        try {
            JSONObject dataObj = new JSONObject();
            dataObj.put("title", "TRACKING");
            dataObj.put("notificationTitle", personModel.getFirebaseKey());
            dataObj.put("notificationContent", String.valueOf(personModel.isTrackingEnable()));
            dataObj.put("notificationTrackingDeviceId", personModel.getTrackingDeviceId());

            JSONObject mainObj = new JSONObject();
            mainObj.put("data", dataObj);
            mainObj.put("to", "/topics/"+personModel.getFirebaseKey());

            Intent cbIntent =  new Intent(activity, SendFCMNotificationService.class);
            cbIntent.putExtra("message", mainObj.toString());
            activity.startService(cbIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void notifyTrackingStatusToEmployee(Activity activity,PersonModel personModel,
                                                      boolean isTrackingEnable) {
        try {
            JSONObject dataObj = new JSONObject();
            dataObj.put("title", "TRACKING");
            dataObj.put("notificationTitle", personModel.getFirebaseKey());
            dataObj.put("notificationContent", isTrackingEnable);
            dataObj.put("notificationTrackingDeviceId", personModel.getTrackingDeviceId());

            JSONObject mainObj = new JSONObject();
            mainObj.put("data", dataObj);
            mainObj.put("to", "/topics/" +personModel.getFirebaseKey());

            Intent cbIntent =  new Intent(activity, SendFCMNotificationService.class);
            cbIntent.putExtra("message", mainObj.toString());
            activity.startService(cbIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public  static void startTracking(Context context){
        if (hasPostNotificationPermissions(context)
                && hasLocationPermissions(context)
                && hasBackgroundLocationPermissions(context)) {
            if (!CommonMethods.isMyServiceRunning(context, TrackingService.class.getName())) {
                Intent myIntent = new Intent(context, TrackingService.class);
                myIntent.setAction(ConstantData.START_LOCATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(myIntent);
                } else {
                    context.startService(myIntent);
                }
            }
        }
    }

    public  static void stopTracking(Context context){
        if (CommonMethods.isMyServiceRunning(context, TrackingService.class.getName())) {
            Intent myIntent = new Intent(context, TrackingService.class);
            myIntent.setAction(ConstantData.STOP_LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(myIntent);
            } else {
                context.startService(myIntent);
            }
        }
    }

    public static boolean isMyServiceRunning(Context arg, String className) {
        ActivityManager manager = (ActivityManager) arg
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (className.equals(service.service.getClassName())) {
                Log.i("Autostart", "started");
                return true;
            }
        }
        return false;
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    public static void showSettingsDialog(Activity activity, int permissionsRequest) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(activity.getString(R.string.title_required_permission));
        alertDialogBuilder.setMessage(activity.getString(R.string.message_required_permission))
                .setPositiveButton(activity.getString(R.string.action_go_settings), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        openSettings(activity,permissionsRequest);
                    }
                });
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    // navigating user to app settings
    private static void openSettings(Activity activity, int permissionsRequest) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, permissionsRequest);
    }

    //Check GPS Status true/false
    public static boolean checkGPSStatus(Context context){
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE );
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    public static void handleMultiplePermission(Context activity, IPermissionGrantedListener listener, ArrayList<String> permissions) {
        Dexter.withContext(activity)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            listener.isGranted(true);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public static void handleCameraPermission(Context activity, IPermissionGrantedListener listener) {
        Dexter.withContext(activity)
                .withPermissions(Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            listener.isGranted(true);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public static void handleExternalStoragePermission(Context activity, IPermissionGrantedListener listener) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            Dexter.withContext(activity)
                    .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                listener.isGranted(true);
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        } else {
            listener.isGranted(true);
        }
    }
}

