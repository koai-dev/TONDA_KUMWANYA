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
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardPermissionActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.databinding.FragmentEnableNotificationPermissionBinding;

public class EnableNotificationPermissionFragment extends Fragment {
    private static final String TAG = FragmentEnableNotificationPermissionBinding.class.getSimpleName();

    private FragmentEnableNotificationPermissionBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEnableNotificationPermissionBinding.inflate(inflater, container, false);
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

        binding.txtDescription.setText(
                String.format(getString(R.string.label_notification_permission_requires_for),
                        getString(R.string.app_name))
        );

        binding.txtDescriptionForRequiredAction.setText(Html.fromHtml("When you are requested access, please tap <b>“Allow”</b>."));

        binding.btnEnable.setOnClickListener(onEnableButtonClick);

        if (getActivity() != null) {
            if (getActivity() instanceof UserDashboardPermissionActivity) {
                binding.txtNoThanks.setVisibility(View.VISIBLE);
                binding.txtNoThanks.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() instanceof UserDashboardPermissionActivity) {
                            ((UserDashboardPermissionActivity) getActivity()).handleFurtherPermissionsAfterPostNotificationPermission();
                        }
                    }
                });
            }
        }
    }


    View.OnClickListener onEnableButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getActivity() != null) {
                if (getActivity() instanceof UserDashboardPermissionActivity) {
                    ((UserDashboardPermissionActivity) getActivity()).requestForNotificationPermissions();
                }
            }
        }
    };
}
