package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.annotation.SuppressLint;
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
import in.gabsinfo.usertimeslotgeofenceattendance.databinding.FragmentEnableBackgroundLocationPermissionBinding;


public class EnableBackgroundLocationPermissionFragment extends Fragment {
    private static final String TAG = FragmentEnableBackgroundLocationPermissionBinding.class.getSimpleName();

    private FragmentEnableBackgroundLocationPermissionBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEnableBackgroundLocationPermissionBinding.inflate(inflater, container, false);
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

        binding.txtNoThanks.setVisibility(View.VISIBLE);
        binding.txtNoThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() instanceof UserDashboardPermissionActivity) {
                    ((UserDashboardPermissionActivity) getActivity()).launchActivity();
                }else if (getActivity() instanceof UserDashboardActivity) {
                    ((UserDashboardActivity) getActivity()).onBackPressed();
                    ((UserDashboardActivity) getActivity()).handleMarkAttendanceClick();
                }
            }
        });

        binding.txtDescription.setText(
                String.format(
                        getString(R.string.label_background_service_requires_for),
                        getString(R.string.app_name),
                        "your",
                        "your"
                )
        );

        binding.txtDescriptionForRequiredAction.setText(Html.fromHtml("To see maps for automatically tracked activities, allow " + getString(R.string.app_name) + " to use your location all the time. Please tap <b>“Allow all the time”</b>."));
        //binding.txtDescriptionForRequiredAction.setText(Html.fromHtml("When you are requested access, please tap <b>“Allow all the time”</b>."));

        binding.btnEnable.setOnClickListener(onEnableButtonClick);
    }


    View.OnClickListener onEnableButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getActivity() != null) {
                if (getActivity() instanceof UserDashboardPermissionActivity) {
                    ((UserDashboardPermissionActivity) getActivity()).requestForBackgroundLocationPermissions();
                }else if (getActivity() instanceof UserDashboardActivity) {
                    ((UserDashboardActivity) getActivity()).requestForBackgroundLocationPermissions();
                }
            }
        }
    };
}
