package in.gabsinfo.usertimeslotgeofenceattendance.activity;


import static androidx.core.content.FileProvider.getUriForFile;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yalantis.ucrop.UCrop;

import java.io.File;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;

public class ImagePickerActivity extends AppCompatActivity {
    private static final String TAG = ImagePickerActivity.class.getSimpleName();
    public static final String INTENT_IMAGE_PICKER_OPTION = "image_picker_option";
    public static final String INTENT_ASPECT_RATIO_X = "aspect_ratio_x";
    public static final String INTENT_ASPECT_RATIO_Y = "aspect_ratio_Y";
    public static final String INTENT_LOCK_ASPECT_RATIO = "lock_aspect_ratio";
    public static final String INTENT_IMAGE_COMPRESSION_QUALITY = "compression_quality";
    public static final String INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT = "set_bitmap_max_width_height";
    public static final String INTENT_BITMAP_MAX_WIDTH = "max_width";
    public static final String INTENT_BITMAP_MAX_HEIGHT = "max_height";
    public static final String INTENT_IMAGE_CAPTURE_FROM_FRONT_CAMERA = "image_capture_from_front_camera";


    public static final int REQUEST_IMAGE_CAPTURE = 0;
    public static final int REQUEST_GALLERY_IMAGE = 1;

    private boolean lockAspectRatio = false, setBitmapMaxWidthHeight = false;
    private int ASPECT_RATIO_X = 16, ASPECT_RATIO_Y = 9, bitmapMaxWidth = 1000, bitmapMaxHeight = 1000;
    private int IMAGE_COMPRESSION = 80;
    public static String fileName;
    public boolean imageCaptureFromFrontCameraOnly;

    private static String CAMERA = Manifest.permission.CAMERA;
    private static String STORAGE = Manifest.permission.READ_MEDIA_IMAGES;

    public interface PickerOptionListener {
        void onTakeCameraSelected();

        void onChooseGallerySelected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_image_intent_null), Toast.LENGTH_LONG).show();
            return;
        }

        ASPECT_RATIO_X = intent.getIntExtra(INTENT_ASPECT_RATIO_X, ASPECT_RATIO_X);
        ASPECT_RATIO_Y = intent.getIntExtra(INTENT_ASPECT_RATIO_Y, ASPECT_RATIO_Y);
        IMAGE_COMPRESSION = intent.getIntExtra(INTENT_IMAGE_COMPRESSION_QUALITY, IMAGE_COMPRESSION);
        lockAspectRatio = intent.getBooleanExtra(INTENT_LOCK_ASPECT_RATIO, false);
        setBitmapMaxWidthHeight = intent.getBooleanExtra(INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, false);
        bitmapMaxWidth = intent.getIntExtra(INTENT_BITMAP_MAX_WIDTH, bitmapMaxWidth);
        bitmapMaxHeight = intent.getIntExtra(INTENT_BITMAP_MAX_HEIGHT, bitmapMaxHeight);
        imageCaptureFromFrontCameraOnly = intent.getBooleanExtra(INTENT_IMAGE_CAPTURE_FROM_FRONT_CAMERA, false);

        int requestCode = intent.getIntExtra(INTENT_IMAGE_PICKER_OPTION, -1);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (!CommonMethods.hasCameraPermissions(this)) {
                requestForCameraPermissions();
            } else {
                takeCameraImage();
            }
        } else {
            if (!CommonMethods.hasReadStoragePermissions(this)) {
                requestForReadStoragePermissions();
            } else {
                chooseImageFromGallery();
            }
        }
    }

    public static void showImagePickerOptions(Context context, PickerOptionListener listener) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.lbl_set_profile_photo));

        // add a list
        String[] animals = {context.getString(R.string.lbl_take_camera_picture), context.getString(R.string.lbl_choose_from_gallery)};
        builder.setItems(animals, (dialog, which) -> {
            switch (which) {
                case 0:
                    listener.onTakeCameraSelected();
                    break;
                case 1:
                    listener.onChooseGallerySelected();
                    break;
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void takeCameraImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            fileName = System.currentTimeMillis() + ".jpg";
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName));
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // on below line passing camera intent type as front camera.
            if (imageCaptureFromFrontCameraOnly)
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void chooseImageFromGallery() {
        Intent pickPhoto = null;
        if (Build.VERSION.SDK_INT >= 33) {
            pickPhoto = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    cropImage(getCacheImagePath(fileName));
                } else {
                    setResultCancelled();
                }
                break;
            case REQUEST_GALLERY_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    cropImage(imageUri);
                } else {
                    setResultCancelled();
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    handleUCropResult(data);
                } else {
                    setResultCancelled();
                }
                break;
            case UCrop.RESULT_ERROR:
                final Throwable cropError = UCrop.getError(data);
                Log.e(TAG, "Crop error: " + cropError);
                setResultCancelled();
                break;
            default:
                setResultCancelled();
        }
    }

    private void cropImage(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), queryName(getContentResolver(), sourceUri)));
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(IMAGE_COMPRESSION);

        // applying UI theme
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary));

        if (lockAspectRatio)
            options.withAspectRatio(ASPECT_RATIO_X, ASPECT_RATIO_Y);

        if (setBitmapMaxWidthHeight)
            options.withMaxResultSize(bitmapMaxWidth, bitmapMaxHeight);

        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .start(this);
    }

    private void handleUCropResult(Intent data) {
        if (data == null) {
            setResultCancelled();
            return;
        }
        final Uri resultUri = UCrop.getOutput(data);


        setResultOk(resultUri);

    }

    private void setResultOk(Uri imagePath) {
        Intent intent = new Intent();
        intent.putExtra("path", imagePath);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void setResultCancelled() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private Uri getCacheImagePath(String fileName) {
        File path = new File(getExternalCacheDir(), "camera");
        if (!path.exists()) path.mkdirs();
        File image = new File(path, fileName);
        return getUriForFile(ImagePickerActivity.this, getPackageName() + ".provider", image);
    }

    private static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    /**
     * Calling this will delete the images from cache directory
     * useful to clear some memory
     */
    public static void clearCache(Context context) {
        File path = new File(context.getExternalCacheDir(), "camera");
        if (path.exists() && path.isDirectory()) {
            for (File child : path.listFiles()) {
                child.delete();
            }
        }
    }


    public void requestForCameraPermissions() {
        // base permissions are for M and higher
        cameraPermissionRequest.launch(CAMERA);
    }

    ActivityResultLauncher<String> cameraPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                    .RequestPermission(), isGranted -> {
                //Grant Location Permission
                if (CommonMethods.hasCameraPermissions(ImagePickerActivity.this)) {
                    takeCameraImage();
                } else {
                    if (!isGranted) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(ImagePickerActivity.this,
                                CAMERA)) {
                            showRationalPermissionDialog(CAMERA);
                        } else {
                            showManuallyGrantPermissionDialog(CAMERA);
                        }
                    }
                }
            });

    public void requestForReadStoragePermissions() {
        // base permissions are for M and higher
        storagePermissionRequest.launch(STORAGE);
    }

    ActivityResultLauncher<String> storagePermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                    .RequestPermission(), isGranted -> {
                //Grant Location Permission
                if (CommonMethods.hasReadStoragePermissions(ImagePickerActivity.this)) {
                    chooseImageFromGallery();
                } else {
                    if (!isGranted) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(ImagePickerActivity.this,
                                STORAGE)) {
                            showRationalPermissionDialog(STORAGE);
                        } else {
                            showManuallyGrantPermissionDialog(STORAGE);
                        }
                    }
                }
            });

    private void showRationalPermissionDialog(String permission) {
        String title = "";
        String desc = "";
        if (permission.equals(CAMERA)) {
            title = String.format(getString(R.string.permission_title),
                    "Camera");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                desc = getString(R.string.app_name).concat(" requires camera permission as “While using the app“ to allow you to capture photo.");
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                desc = getString(R.string.app_name).concat(" requires camera permission as “Allow“ to allow you to capture photo.");
            } else {
                desc = getString(R.string.app_name).concat(" requires camera permission as “Allow“ to allow you to capture photo.");
            }
        } else if (permission.equals(STORAGE)) {
            title = String.format(getString(R.string.permission_title),
                    "Storage");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                desc = getString(R.string.app_name) + " requires Files and media permission permission as “Allow“ to choose image from mobile storage.";
            } else
                desc = getString(R.string.app_name) + " requires Storage permission as “Allow“ to choose image from mobile storage.";
        }

        new AlertDialog.Builder(ImagePickerActivity.this)
                .setTitle(title)
                .setMessage(desc)
                .setPositiveButton("Allow", (dialog, which) -> {
                    dialog.cancel();
                    if (permission.equals(CAMERA)) {
                        requestForCameraPermissions();
                    } else if (permission.equals(STORAGE)) {
                        requestForReadStoragePermissions();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .show();
    }

    private void showManuallyGrantPermissionDialog(String permission) {
        String title = "";
        String desc = "";
        if (permission.equals(CAMERA)) {
            title = String.format(getString(R.string.permission_title),
                    "Camera");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                desc = "Without camera permission " + getString(R.string.app_name) + " will not allow you to capture photo.\n\nAllow permission manually:\nSettings > Permissions > Click on Camera permission > Allow only while using the app.";
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                desc = "Without camera permission " + getString(R.string.app_name) + " will not allow you to capture photo.\n\nAllow permission manually:\nSettings > Permissions > Click on Camera permission > Allow.";
            } else {
                desc = "Without camera permission " + getString(R.string.app_name) + " will not allow you to capture photo.\n\nAllow permission manually:\nSettings > Permissions > Click on Camera permission > enable Toggle.";
            }
        } else if (permission.equals(STORAGE)) {
            title = String.format(getString(R.string.permission_title),
                    "Storage");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                desc = "Without Photos and videos permission " + getString(R.string.app_name) + " will not allow you to choose image from mobile storage.\n\nAllow permission manually:\nSettings > Permissions > Click on Photos and videos permission > Allow.";
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                desc = "Without Files and media permission " + getString(R.string.app_name) + " will not allow you to choose image from mobile storage.\n\nAllow permission manually:\nSettings > Permissions > Click on Files and media permission > Allow access to media only.";
            } else
                desc = "Without storage permission " + getString(R.string.app_name) + " will not allow you to choose image from mobile storage.\n\nAllow permission manually:\nSettings > Permissions > Click on Storage permission > enable Toggle.";
        }

        new AlertDialog.Builder(ImagePickerActivity.this)
                .setTitle(title)
                .setMessage(desc)
                .setCancelable(false)
                .setPositiveButton("Settings", (dialog, which) -> {
                    dialog.cancel();
                    redirectToAppSettingPage();
                })
                .setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
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
                /*int requestCode = getIntent().getIntExtra(INTENT_IMAGE_PICKER_OPTION, -1);
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    if (CommonMethods.hasCameraPermissions(this)) {
                        takeCameraImage();
                    }
                } else {
                    if (CommonMethods.hasReadStoragePermissions(this)) {
                        chooseImageFromGallery();
                    }
                }*/
                finish();
            });
}

