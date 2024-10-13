package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardPermissionActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.databinding.FragmentEnableLocationPermissionBinding;

public class EnableLocationPermissionFragment extends Fragment {
    private static final String TAG = FragmentEnableLocationPermissionBinding.class.getSimpleName();

    private FragmentEnableLocationPermissionBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEnableLocationPermissionBinding.inflate(inflater, container, false);
        binding.getRoot().setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
    }

    private void initialize() {
        binding.btnEnable.setOnClickListener(onEnableButtonClick);
        binding.txtNoThanks.setVisibility(View.VISIBLE);
        binding.txtNoThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() instanceof UserDashboardPermissionActivity) {
                    ((UserDashboardPermissionActivity) getActivity()).launchActivity();
                }else if (getActivity() instanceof UserDashboardActivity) {
                    ((UserDashboardActivity) getActivity()).onBackPressed();
                }
            }
        });


        binding.txtDescription.setText(getString(R.string.app_name).concat(" requires location services to determine your current location on map."));

        //Android 11 and above we have to ask for 2 separate permissions for location and background service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.txtDescriptionForRequiredAction.setText(Html.fromHtml("When you are requested access, please tap <b>“While using the app”</b>."));
        }
        //Android 10
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            binding.txtDescriptionForRequiredAction.setText(Html.fromHtml("When you are requested access, please tap <b>“Allow only while using the app”</b>."));
        }
        //Prior to Android 10
        else {
            binding.txtDescriptionForRequiredAction.setText(Html.fromHtml("When you are requested access, please tap <b>“Allow”</b>."));
        }
    }


    View.OnClickListener onEnableButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getActivity() != null) {
                if (getActivity() instanceof UserDashboardPermissionActivity) {
                    ((UserDashboardPermissionActivity) getActivity()).requestForLocationPermissions();
                }else if (getActivity() instanceof UserDashboardActivity) {
                    ((UserDashboardActivity) getActivity()).requestForLocationPermissions();
                }
            }
        }
    };
}
