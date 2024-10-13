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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.SignInActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.UserRegistrationInputModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class AdminSignInFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    public EditText mEtMobileNoOrEmailId, mEtPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_sign_in, container, false);
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
        TextView tvForgotCredentials = view.findViewById(R.id.tv_forgot_credentials);

        if(SharePreferences.getBool(SharePreferences.KEY_IS_USER_LOGGED_IN,SharePreferences.DEFAULT_BOOLEAN)){
            mEtMobileNoOrEmailId.setText(SharePreferences.getStr(SharePreferences.KEY_USER_EMAIL_ID,SharePreferences.DEFAULT_STRING));
            mEtPassword.setText(SharePreferences.getStr(SharePreferences.KEY_USER_PASSWORD,SharePreferences.DEFAULT_STRING));
            performVerification();
        }

        cbShowPassword.setOnCheckedChangeListener(this);
        tvForgotCredentials.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.tv_forgot_credentials){
            if (getActivity() != null
                    && getActivity() instanceof SignInActivity) {
                ((SignInActivity) getActivity()).mobileNoVerificationFor =
                        ((SignInActivity) getActivity()).CONST_FORGOT_CREDENTIAL;
                ((SignInActivity) getActivity()).verifyMobileNo();
            }
        }
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
                        if(CommonMethods.isValidEmail(strMobileNoOrEmailId)){//Means user has entered email id
                            query= AttendanceApplication.refCompanyUserDetails
                                    .child(SharePreferences.getStr(SharePreferences.KEY_COMPANY_CODE, SharePreferences.DEFAULT_STRING))
                                    .orderByChild("userType_emailId")
                                    .equalTo(ConstantData.TYPE_ADMIN + "-" + strMobileNoOrEmailId);
                        }else{
                            query= AttendanceApplication.refCompanyUserDetails
                                    .child(SharePreferences.getStr(SharePreferences.KEY_COMPANY_CODE, SharePreferences.DEFAULT_STRING))
                                    .orderByChild("userType_mobileNo")
                                    .equalTo(ConstantData.TYPE_ADMIN + "-" + strMobileNoOrEmailId);
                        }*/

                Query query = AttendanceApplication.refCompanyUserDetails
                        .orderByChild("userType_emailId")
                        .equalTo(ConstantData.TYPE_ADMIN + "-" + strEmailId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        CommonMethods.cancelProgressDialog();
                        if (dataSnapshot.exists()) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                UserRegistrationInputModel detailModel = ds.getValue(UserRegistrationInputModel.class);

                                assert detailModel != null;
                                if (detailModel.getPassword().equals(strPassword)) {

                                    SharePreferences.setStr(SharePreferences.KEY_USER_EMAIL_ID, detailModel.getEmailId());
                                    SharePreferences.setStr(SharePreferences.KEY_USER_PASSWORD, detailModel.getPassword());
                                    SharePreferences.setStr(SharePreferences.KEY_USER_NAME, detailModel.getName());
                                    SharePreferences.setStr(SharePreferences.KEY_USER_MOBILE_NO, detailModel.getMobileNo());
                                    SharePreferences.setStr(SharePreferences.KEY_USER_PROFILE_IMAGE, detailModel.getProfileImage());
                                    SharePreferences.setBool(SharePreferences.KEY_IS_ADMIN_USER, true);
                                    SharePreferences.setBool(SharePreferences.KEY_IS_TRACKING_ENABLE, false);
                                    SharePreferences.setBool(SharePreferences.KEY_IS_USER_LOGGED_IN, true);
                                    SharePreferences.setStr(SharePreferences.KEY_USER_FIREBASE_KEY, ds.getKey());
                                    SharePreferences.setStr(SharePreferences.KEY_USER_REGISTRATION_DATE, detailModel.getRegistrationDate());

                                    /**
                                     * Stop location tracking service
                                     */
                                    CommonMethods.stopTracking(getActivity());

                                    //startActivity(new Intent(getActivity(), AdminPlanPurchaseActivity.class));
                                    startActivity(new Intent(getActivity(), AdminDashboardActivity.class));

                                    if (getActivity() != null) {
                                        if (getActivity() instanceof SignInActivity) {
                                            getActivity().finish();
                                        }
                                    }
                                } else {
                                    CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                            getString(R.string.msg_invalid_admin), getString(R.string.action_ok));
                                }
                            }
                        } else {
                            CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                    getString(R.string.msg_invalid_admin), getString(R.string.action_ok));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        CommonMethods.cancelProgressDialog();
                        CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                String.format(getString(R.string.msg_issue_while_validating_admin), e.getMessage()), getString(R.string.action_ok));
                    }
                });

            } else {
                CommonMethods.showConnectionAlert(getActivity());
            }
        }
    }
}