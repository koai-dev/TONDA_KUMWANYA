package in.gabsinfo.usertimeslotgeofenceattendance.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.AdminSignInFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.SignUpFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.UserSignInFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.model.UserRegistrationInputModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;
import io.github.florent37.shapeofview.shapes.DiagonalView;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    private Fragment mTopFragment = null;
    private TextView mTvAdmin, mTvEmployee;
    private DiagonalView mDvCreateAdminAccount;
    private FrameLayout mFlContainer_;
    private static final int RC_SIGN_IN = 0;
    private final List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build());

    public static String CONST_ACCOUNT_CREATE="ACCOUNT CREATE";
    public static String CONST_FORGOT_CREDENTIAL="FORGOT CREDENTIAL";
    public String mobileNoVerificationFor ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        init();
    }


    private void init() {
        mTvAdmin = findViewById(R.id.tv_admin);
        mTvEmployee = findViewById(R.id.tv_employee);
        mDvCreateAdminAccount = findViewById(R.id.dv_create_admin_account);
        mFlContainer_ = findViewById(R.id.fl_container_);
        FloatingActionButton fabVerifyUser = findViewById(R.id.fab_verify_user);
        Button btnCreateAdminAccount = findViewById(R.id.btn_create_admin_account);

        //accent color text
        TextView redColorTextView = findViewById(R.id.tv_label);
        String redString = getResources().getString(R.string.label_make_free_company_registration);
        redColorTextView.setText(Html.fromHtml(redString));

        mTvAdmin.setOnClickListener(this);
        mTvEmployee.setOnClickListener(this);
        fabVerifyUser.setOnClickListener(this);
        btnCreateAdminAccount.setOnClickListener(this);

        if (SharePreferences.getBool(SharePreferences.KEY_IS_USER_LOGGED_IN, SharePreferences.DEFAULT_BOOLEAN)) {
            if (SharePreferences.getBool(SharePreferences.KEY_IS_ADMIN_USER, SharePreferences.DEFAULT_BOOLEAN)) {
                mTvAdmin.performClick();
            } else {
                mTvEmployee.performClick();
            }
        } else {
            mTvAdmin.performClick();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.tv_admin){
            mTvAdmin.setBackground(ContextCompat.getDrawable(this,R.drawable.left_selected));
            mTvEmployee.setBackground(ContextCompat.getDrawable(this,R.drawable.right_unselected));

            mTvEmployee.setTextColor(getResources().getColor(android.R.color.black));
            mTvAdmin.setTextColor(getResources().getColor(android.R.color.white));

            replaceLoginFragment(new AdminSignInFragment(), ConstantData.Admin_Sign_In_Fragment);
            mDvCreateAdminAccount.setVisibility(View.VISIBLE);
        }else if(v.getId()==R.id.tv_employee){
            mTvEmployee.setBackground(ContextCompat.getDrawable(this,R.drawable.right_selected));
            mTvAdmin.setBackground(ContextCompat.getDrawable(this,R.drawable.left_unselected));

            mTvAdmin.setTextColor(getResources().getColor(android.R.color.black));
            mTvEmployee.setTextColor(getResources().getColor(android.R.color.white));

            replaceLoginFragment(new UserSignInFragment(), ConstantData.User_Sign_In_Fragment);
            mDvCreateAdminAccount.setVisibility(View.INVISIBLE);
        }else if(v.getId()==R.id.fab_verify_user){
            if (mTopFragment != null) {
                if (mTopFragment instanceof AdminSignInFragment) {
                    ((AdminSignInFragment) mTopFragment).performVerification();
                } else if (mTopFragment instanceof UserSignInFragment) {
                    ((UserSignInFragment) mTopFragment).performVerification();
                }
            }
        }else if(v.getId()==R.id.btn_create_admin_account){
            if (CommonMethods.isNetworkConnected(SignInActivity.this)) {
                CommonMethods.showProgressDialog(SignInActivity.this);

                /**
                 * First we check whether the admin account is already exist or not
                 */
                AttendanceApplication.refCompanyUserDetails
                        .orderByChild("userType").equalTo(ConstantData.TYPE_ADMIN)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                CommonMethods.cancelProgressDialog();
                                if (!dataSnapshot.exists()) {

                                    mobileNoVerificationFor=CONST_ACCOUNT_CREATE;
                                    verifyMobileNo();

                                } else {
                                    CommonMethods.showAlertDailogueWithOK(SignInActivity.this, getString(R.string.title_alert),
                                            getString(R.string.msg_admin_account_is_already_created), getString(R.string.action_ok));
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError e) {
                                CommonMethods.cancelProgressDialog();
                                CommonMethods.showAlertDailogueWithOK(SignInActivity.this, getString(R.string.title_alert),
                                        e.getMessage(), getString(R.string.action_ok));
                            }
                        });
            } else {
                CommonMethods.showConnectionAlert(SignInActivity.this);
            }
        }
    }

    /**
     * Implement mobile no verification functionality
     */
    public void verifyMobileNo() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        //.setIsSmartLockEnabled(false,true)
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .setTheme(R.style.AppTheme)
                        //.setTosUrl("https://superapp.example.com/terms-of-service.html")
                        .setPrivacyPolicyUrl("https://superapp.example.com/privacy-policy.html")
                        .setLogo(R.mipmap.ic_launcher)
                        .build(),
                RC_SIGN_IN);
    }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == RC_SIGN_IN) {
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (resultCode == Activity.RESULT_OK) {
                    assert response != null;

                    // Successfully signed in
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null)
                        return;

                    String strMob = user.getPhoneNumber();//With Dialer Code

                    if(mobileNoVerificationFor.trim().length()>0) {
                        if (mobileNoVerificationFor.equalsIgnoreCase(CONST_ACCOUNT_CREATE)) {
                            Bundle bundle = new Bundle();
                            bundle.putString("MobileNo", strMob);
                            addFragment(new SignUpFragment(), null, bundle);
                        } else if (mobileNoVerificationFor.equalsIgnoreCase(CONST_FORGOT_CREDENTIAL)) {
                            getCredentialInformation(user.getPhoneNumber());
                        }
                    }
                } else {
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    // ...
                }
            }
        }


    /**
     * Here we call API to get credential information once
     * mobile no verification done successfully.
     * Note : Forgot Credential Only for Admin Users
     * for Employee they can directly contact with Admin and ask for their details
     */
    public void getCredentialInformation(String phoneNumber){
        if (CommonMethods.isNetworkConnected(this)) {

            CommonMethods.showProgressDialog(this);
            AttendanceApplication.refCompanyUserDetails
                    .orderByChild("userType_mobileNo").equalTo(ConstantData.TYPE_ADMIN + "-" +phoneNumber)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    UserRegistrationInputModel outputModel = ds.getValue(UserRegistrationInputModel.class);
                                    assert outputModel != null;
                                    if (mTopFragment != null
                                            && mTopFragment instanceof AdminSignInFragment) {
                                        ((AdminSignInFragment)mTopFragment).mEtMobileNoOrEmailId.setText(outputModel.getEmailId());
                                        ((AdminSignInFragment)mTopFragment).mEtPassword.setText(outputModel.getPassword());
                                    }
                                }
                            } else {
                                CommonMethods.showAlertDailogueWithOK(SignInActivity.this,getString(R.string.title_alert),
                                        getString(R.string.msg_no_credentials_found_for_entered_mobile_no),getString(R.string.action_ok));
                            }
                            CommonMethods.cancelProgressDialog();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            CommonMethods.showAlertDailogueWithOK(SignInActivity.this, getString(R.string.title_alert),
                                    databaseError.getMessage(), getString(R.string.action_ok));
                            CommonMethods.cancelProgressDialog();
                        }
                    });
        } else {
            CommonMethods.showConnectionAlert(SignInActivity.this);
        }
    }

    // Replace Login Fragment with animation
    public void replaceLoginFragment(Fragment fragment, String fragmentName) {
        if (fragment == null)
            return;
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_from_right,
                        R.anim.slide_to_left,
                        R.anim.slide_from_left,
                        R.anim.slide_to_right)
                .replace(R.id.fl_container, fragment,
                        fragmentName).commit();
        mTopFragment = fragment;
    }

    public void addFragment(Fragment fragment, String fragmentName, Bundle bundle) {
        if (fragment == null)
            return;
        fragment.setArguments(bundle);
        mFlContainer_.setVisibility(View.VISIBLE);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_from_right,
                        R.anim.slide_to_left,
                        R.anim.slide_from_left,
                        R.anim.slide_to_right)
                .add(R.id.fl_container_, fragment,
                        fragmentName).addToBackStack(null).commit();
    }


    @Override
    public void onBackPressed() {
        if(mFlContainer_.getVisibility()==View.VISIBLE){
            getSupportFragmentManager().popBackStack();
            mFlContainer_.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
}

