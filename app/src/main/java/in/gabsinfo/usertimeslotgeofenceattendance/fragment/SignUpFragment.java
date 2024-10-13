package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.Interface.IPermissionGrantedListener;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.ImagePickerActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.SignInActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.UserRegistrationInputModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;


public class SignUpFragment extends Fragment implements View.OnClickListener {


    private EditText mEtMobileNo, mEtEmailId, mEtPassword, mEtName;
    private ImageView mIvUserProfileImage;
    private static final int REQUEST_IMAGE = 253;
    private Uri imageCaptureUri = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        init(view);
        return view;
    }

    private void init(View view) {
        mIvUserProfileImage = view.findViewById(R.id.iv_user_profile_image);
        mEtMobileNo = view.findViewById(R.id.et_mobile_no);
        mEtEmailId = view.findViewById(R.id.et_email_id);
        mEtPassword = view.findViewById(R.id.et_password);
        mEtName = view.findViewById(R.id.et_name);
        FloatingActionButton fabCreateAccount = view.findViewById(R.id.fab_create_account);

        mEtMobileNo.setText(getArguments() != null ? getArguments().getString("MobileNo") : "");
        mEtMobileNo.setEnabled(false);
        mEtMobileNo.setFocusable(false);
        mEtName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        mIvUserProfileImage.setOnClickListener(this);
        fabCreateAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_user_profile_image) {
            onProfileImageClick();
        } else if (v.getId() == R.id.fab_create_account) {
            final String strMobileNo = mEtMobileNo.getText().toString().trim();
            final String strEmailId = mEtEmailId.getText().toString().trim();
            final String strPassword = mEtPassword.getText().toString().trim();
            final String strName = mEtName.getText().toString().trim();
            if (imageCaptureUri == null) {
                Toast.makeText(getActivity(), getString(R.string.msg_capture_admin_photo), Toast.LENGTH_LONG).show();
            } else if (strMobileNo.isEmpty() || strEmailId.isEmpty() || strPassword.isEmpty() || strName.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.msg_all_fields_required), Toast.LENGTH_LONG).show();
            } else if (strMobileNo.length() < 10) {
                Toast.makeText(getActivity(), getString(R.string.msg_enter_valid_mobile_no), Toast.LENGTH_LONG).show();
            } else if (!CommonMethods.isValidEmail(strEmailId)) {
                Toast.makeText(getActivity(), getString(R.string.msg_enter_valid_email_id), Toast.LENGTH_LONG).show();
            } else {

                if (CommonMethods.isNetworkConnected(requireActivity())) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireActivity(),
                            R.style.CustomDialogTheme);
                    alertDialogBuilder.setTitle(getString(R.string.alert_title_person_add));
                    alertDialogBuilder.setMessage(getString(R.string.alert_msg_admin_account_register))
                            .setPositiveButton(getString(R.string.action_yes), null)
                            .setNegativeButton(getString(R.string.action_no), null);

                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialog) {
                            Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                            btnPositive.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();

                                    /**
                                     * Here we add ADMIN user details to fire-base
                                     */
                                    UserRegistrationInputModel inputModel = new UserRegistrationInputModel();
                                    inputModel.setName(strName);
                                    inputModel.setMobileNo(strMobileNo);
                                    inputModel.setEmailId(strEmailId);
                                    inputModel.setPassword(strPassword);
                                    inputModel.setUserType(ConstantData.TYPE_ADMIN);
                                    inputModel.setUserType_mobileNo(ConstantData.TYPE_ADMIN + "-" + strMobileNo);
                                    inputModel.setUserType_emailId(ConstantData.TYPE_ADMIN + "-" + strEmailId);
                                    inputModel.setRegistrationDate(new SimpleDateFormat(ConstantData.DATE_HOUR_FORMAT, Locale.US).format(Calendar.getInstance().getTime()));

                                    uploadDetailsToServer(imageCaptureUri, inputModel);
                                }
                            });

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
                } else {
                    CommonMethods.showConnectionAlert(getActivity());
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    assert data != null;
                    Uri uri = data.getParcelableExtra("path");
                    try {

                        /*assert uri != null;
                        File myFile = new File(Objects.requireNonNull(uri.getPath()));
                        long sizeInKB = myFile.length() / 1024; // Size in KB
                        Log.e("KB Size", String.valueOf(sizeInKB));
                        if (sizeInKB > 100) {
                            Toast.makeText(getActivity(), getString(R.string.msg_capture_shop_owner_image_proper), Toast.LENGTH_SHORT).show();
                            return;
                        }*/

                        imageCaptureUri = uri;
                        // loading profile image from local cache
                        loadProfile(uri.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Here we get compressed Image from ImageCompression class
     *
     * @param destinationPath
     */
    private String strImageNameInStorage = "";

    private void uploadDetailsToServer(Uri uri, final UserRegistrationInputModel inputModel) {
        //displaying progress dialog while image is uploading
        CommonMethods.showProgressDialog(getActivity());

        strImageNameInStorage = new SimpleDateFormat(
                "ddMMMyyyy_HHmmss", Locale.US).format(new Date()).toString() + ".jpg";

        //getting the storage reference
        final StorageReference sRef = AttendanceApplication.storageReference
                .child(inputModel.getUserType_mobileNo())
                .child(strImageNameInStorage);
        //adding the file to reference
        sRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //displaying success toast
                        Toast.makeText(getActivity(), getString(R.string.msg_image_uploaded_successfully), Toast.LENGTH_SHORT).show();

                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //creating the upload object to store uploaded image details
                                inputModel.setProfileImage(uri.toString());
                                inputModel.setProfileImageName(strImageNameInStorage);
                                addPersonDetails(inputModel);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //dismissing the progress dialog
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addPersonDetails(final UserRegistrationInputModel inputModel) {

        String key = AttendanceApplication.refCompanyUserDetails.push().getKey();
        assert key != null;
        AttendanceApplication.refCompanyUserDetails
                .child(key).setValue(inputModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(), getString(R.string.msg_admin_account_created_successfully), Toast.LENGTH_SHORT).show();
                        // Write was successful!

                        if (getActivity() != null) {
                            if (getActivity() instanceof SignInActivity) {
                                ((SignInActivity) getActivity()).onBackPressed();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonMethods.cancelProgressDialog();
                        CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                e.getMessage(), getString(R.string.action_ok));
                    }
                });

    }

    private void onProfileImageClick() {
        captureImages();
    }

    private void captureImages() {

        ImagePickerActivity.showImagePickerOptions(getActivity(), new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void loadProfile(String url) {
        CommonMethods.loadImage(getActivity(), url, mIvUserProfileImage,
                ContextCompat.getDrawable(requireActivity(), R.drawable.baseline_account_circle_black));
    }

    private void loadDefaultProfile() {
        CommonMethods.loadDefaultImage(getActivity(), mIvUserProfileImage,
                ContextCompat.getDrawable(requireActivity(), R.drawable.baseline_account_circle_black));
    }

    @Override
    public void onDestroy() {
        ImagePickerActivity.clearCache(requireActivity());
        super.onDestroy();
    }
}