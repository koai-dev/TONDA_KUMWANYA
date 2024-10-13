package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.SignInActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardPermissionActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class UserSignInFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {


    private EditText mEtMobileNoOrEmailId, mEtPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_sign_in, container, false);
        /*view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });*/
        init(view);
        return view;
    }

    private void init(View view) {
        mEtMobileNoOrEmailId = view.findViewById(R.id.et_mobile_no);
        mEtPassword = view.findViewById(R.id.et_password);
        CheckBox cbShowPassword = view.findViewById(R.id.cb_show_password);

        if(SharePreferences.getBool(SharePreferences.KEY_IS_USER_LOGGED_IN,SharePreferences.DEFAULT_BOOLEAN)){
            mEtMobileNoOrEmailId.setText(SharePreferences.getStr(SharePreferences.KEY_USER_EMAIL_ID,SharePreferences.DEFAULT_STRING));
            mEtPassword.setText(SharePreferences.getStr(SharePreferences.KEY_USER_PASSWORD,SharePreferences.DEFAULT_STRING));
            performVerification();
        }

        cbShowPassword.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            mEtPassword.setTransformationMethod(null); // Show password when box checked
        }else{
            mEtPassword.setTransformationMethod(new PasswordTransformationMethod()); // Hide password when box not checked
        }
    }

    public void performVerification() {
        final String strEmailId = mEtMobileNoOrEmailId.getText().toString().trim();
        final String strPassword = mEtPassword.getText().toString().trim();
        if (strEmailId.isEmpty() || strPassword.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.msg_all_fields_required), Toast.LENGTH_LONG).show();
        } else if (!CommonMethods.isValidEmail(strEmailId)) {
            Toast.makeText(getActivity(), getString(R.string.msg_enter_valid_email_id), Toast.LENGTH_LONG).show();
        } else {
            if (CommonMethods.isNetworkConnected(requireActivity())) {
                CommonMethods.showProgressDialog(getActivity());

                        /*Query query;
                        if (CommonMethods.isValidEmail(strMobileNoOrEmailId)) {//Means user has entered email id
                            query = AttendanceApplication.refCompanyUserDetails
                                    .child(SharePreferences.getStr(SharePreferences.KEY_COMPANY_CODE, SharePreferences.DEFAULT_STRING))
                                    .orderByChild("userType_emailId")
                                    .equalTo(ConstantData.TYPE_USER + "-" + strMobileNoOrEmailId);
                        } else {
                            query = AttendanceApplication.refCompanyUserDetails
                                    .child(SharePreferences.getStr(SharePreferences.KEY_COMPANY_CODE, SharePreferences.DEFAULT_STRING))
                                    .orderByChild("userType_mobileNo")
                                    .equalTo(ConstantData.TYPE_USER + "-" + strMobileNoOrEmailId);
                        }*/

                Query query= AttendanceApplication.refCompanyUserDetails
                        .orderByChild("userType_emailId")
                        .equalTo(ConstantData.TYPE_USER + "-" + strEmailId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                PersonModel personModel = ds.getValue(PersonModel.class);

                                assert personModel != null;
                                if(personModel.getPassword().equals(strPassword)) {
                                    personModel.setFirebaseKey(ds.getKey());
                                    //Here we stop tracking from all device in which current user logged-in
                                    CommonMethods.notifyTrackingStatusToEmployee(getActivity(),personModel,false);

                                    if (personModel.getTrackingDeviceId() == null
                                                    || !personModel.getTrackingDeviceId()
                                                    .equals(CommonMethods.getDeviceId())) {
                                        /**
                                         * To overcome tracking issue while same user logged in into multiple device
                                         *
                                         * This is update current device DeviceId against current logged-in user
                                         * and using this updated device-id we can track location of the userr
                                         * whose firebase device-id and current mobile device id are same
                                         */
                                        //Update device-id

                                        HashMap<String, Object> result = new HashMap<>();
                                        result.put("trackingDeviceId", CommonMethods.getDeviceId());

                                        AttendanceApplication.refCompanyUserDetails
                                                .child(Objects.requireNonNull(ds.getKey()))//Firebase key
                                                .updateChildren(result)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        CommonMethods.cancelProgressDialog();
                                                        personModel.setTrackingDeviceId(CommonMethods.getDeviceId());
                                                        callNextScreen(personModel);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        CommonMethods.cancelProgressDialog();
                                                        CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                                                "Updating Device Id Failed : " + e.getMessage(), getString(R.string.action_ok));
                                                    }
                                                });
                                    } else {
                                        CommonMethods.cancelProgressDialog();
                                        callNextScreen(personModel);
                                    }
                                }else {
                                    CommonMethods.cancelProgressDialog();
                                    CommonMethods.showAlertDailogueWithOK(getActivity(),getString(R.string.title_alert),
                                            getString(R.string.msg_invalid_user),getString(R.string.action_ok));
                                }
                            }
                        } else {
                            CommonMethods.cancelProgressDialog();
                            CommonMethods.showAlertDailogueWithOK(getActivity(),getString(R.string.title_alert),
                                    getString(R.string.msg_invalid_user),getString(R.string.action_ok));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        CommonMethods.cancelProgressDialog();
                        CommonMethods.showAlertDailogueWithOK(getActivity(),getString(R.string.title_alert),
                                String.format(getString(R.string.msg_issue_while_validating_user),e.getMessage()),getString(R.string.action_ok));
                    }
                });
            } else {
                CommonMethods.showConnectionAlert(getActivity());
            }
        }
    }


    private void callNextScreen(PersonModel personModel) {
        SharePreferences.setStr(SharePreferences.KEY_USER_EMAIL_ID, personModel.getEmailId());
        SharePreferences.setStr(SharePreferences.KEY_USER_PASSWORD, personModel.getPassword());
        SharePreferences.setStr(SharePreferences.KEY_USER_NAME, personModel.getName());
        SharePreferences.setStr(SharePreferences.KEY_USER_MOBILE_NO, personModel.getMobileNo());
        SharePreferences.setStr(SharePreferences.KEY_USER_PROFILE_IMAGE, personModel.getProfileImage());
        SharePreferences.setStr(SharePreferences.KEY_EMPLOYEE_WORK_TYPE, personModel.getWorkType());
        SharePreferences.setBool(SharePreferences.KEY_IS_TRACKING_ENABLE, personModel.isTrackingEnable());
        SharePreferences.setStr(SharePreferences.KEY_TRACKING_DEVICE_ID, CommonMethods.getDeviceId());
        SharePreferences.setBool(SharePreferences.KEY_IS_ADMIN_USER, false);
        SharePreferences.setBool(SharePreferences.KEY_IS_USER_LOGGED_IN, true);
        SharePreferences.setStr(SharePreferences.KEY_USER_FIREBASE_KEY, personModel.getFirebaseKey());



        if(personModel.isTrackingEnable()){
            String strTodayDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
            AttendanceApplication.refCompanyUserAttendanceDetails
                    .child(personModel.getFirebaseKey())
                    .child(new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US).format(Calendar.getInstance().getTime()))
                    .orderByChild("punchDate").equalTo(strTodayDate)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    AttendanceModel attendanceModel = ds.getValue(AttendanceModel.class);
                                    assert attendanceModel != null;
                                    if (attendanceModel.getPunchInTime() != null
                                            && attendanceModel.getPunchOutTime() != null) {

                                        //Punch In & Out both marked and hence we have to stop Tracking
                                        //As we have always stop tracking as login time so no need to call stopTracking here
                                        launchUserDashboardScreen(personModel);

                                    } else if (attendanceModel.getPunchInTime() != null
                                            && attendanceModel.getPunchOutTime() == null) {

                                        //Punch In marked yet but Punch Out not marked yet
                                        //Start Tracking
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("PersonModel", personModel);
                                        Intent intent=null;
                                        if (CommonMethods.hasPostNotificationPermissions(getActivity())
                                                && CommonMethods.hasLocationPermissions(getActivity())
                                                && CommonMethods.hasBackgroundLocationPermissions(getActivity())) {
                                            CommonMethods.notifyTrackingStatusToEmployee(getActivity(), personModel);

                                            intent = new Intent(getActivity(), UserDashboardActivity.class);
                                            intent.putExtras(bundle);
                                        } else {
                                            //Ask for permission
                                            intent = new Intent(getActivity(), UserDashboardPermissionActivity.class);
                                            intent.putExtras(bundle);
                                        }

                                        startActivity(intent);
                                        if (getActivity() != null) {
                                            if (getActivity() instanceof SignInActivity) {
                                                getActivity().finish();
                                            }
                                        }
                                    }
                                    break;
                                }
                            } else {

                                //Her we no data found for attendance means attendance is not marked and hence we have to stop Tracking
                                //As we have always stop tracking as login time so no need to call stopTracking here
                                launchUserDashboardScreen(personModel);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }else{
            launchUserDashboardScreen(personModel);
        }
    }

    private void launchUserDashboardScreen(PersonModel personModel) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("PersonModel", personModel);
        Intent intent = new Intent(getActivity(), UserDashboardActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

        if (getActivity() != null) {
            if (getActivity() instanceof SignInActivity) {
                getActivity().finish();
            }
        }
    }
}