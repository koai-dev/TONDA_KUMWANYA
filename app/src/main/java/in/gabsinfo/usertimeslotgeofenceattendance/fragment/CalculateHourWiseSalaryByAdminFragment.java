package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;


public class CalculateHourWiseSalaryByAdminFragment extends Fragment {


    private String noOfHoursForPay;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemShare=menu.findItem(R.id.item_share);
        if(itemShare!=null)
            itemShare.setVisible(false);

        MenuItem itemInfo=menu.findItem(R.id.item_info);
        if(itemInfo!=null)
            itemInfo.setVisible(false);

        MenuItem itemList=menu.findItem(R.id.item_list);
        if(itemList!=null)
            itemList.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_calculate_hour_wise_salary_by_admin,container,false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //https://gist.github.com/ferdy182/d9b3525aa65b5b4c468a
        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorDivider));
        // To run the animation as soon as the view is layout in the view hierarchy we add this
        // listener and remove it
        // as soon as it runs to prevent multiple animations if the view changes bounds
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                int cx = 20;
                int cy = 20;

                // get the hypothenuse so the radius is from one corner to the other
                int radius = (int) Math.hypot(right, bottom);

                Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                reveal.setInterpolator(new DecelerateInterpolator(2f));
                reveal.setDuration(1000);
                reveal.start();
            }
        });

        setToolBar(view);
        init(view);
        return view;
    }

    private void setToolBar(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_title_amount_calculation));
        if(getActivity() instanceof AdminDashboardActivity) {
            ((AdminDashboardActivity) getActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AdminDashboardActivity) requireActivity()).onBackPressed();
                }
            });
        }
    }

    private void init(View view) {

        PersonModel mPersonModel=null;
        if (getArguments() != null) {
            mPersonModel = (PersonModel) getArguments().getSerializable("PersonModel");
            noOfHoursForPay = getArguments().getString("NoOfHoursForPay");
        }else
            return;

        TextView tvPersonName=view.findViewById(R.id.tv_person_name);
        TextView tvPersonMobileNo=view.findViewById(R.id.tv_person_mobile_no);
        TextView tvPersonWorkType=view.findViewById(R.id.tv_person_work_type);
        TextView tvPersonDecidedFulDayAmount=view.findViewById(R.id.tv_person_decided_full_day_amount);

        EditText etAmountPerDay=view.findViewById(R.id.et_amount_per_day);
        final TextView tvTotalHours=view.findViewById(R.id.tv_total_hours);
        final TextView tvTotalMinutes=view.findViewById(R.id.tv_total_minuts);
        final TextView tvTotalAmountToPay=view.findViewById(R.id.tv_total_amount_to_pay);

        TextView tv_currency_symbol1=view.findViewById(R.id.tv_currency_symbol1);
        TextView tv_currency_symbol2=view.findViewById(R.id.tv_currency_symbol2);
        TextView tv_currency_symbol3=view.findViewById(R.id.tv_currency_symbol3);

        String[] split = noOfHoursForPay.split(":");
        final Integer hours = Integer.valueOf(split[0]);
        final Integer minutes = Integer.valueOf(split[1]);

        tvTotalHours.setText(String.valueOf(hours));
        tvTotalMinutes.setText(String.valueOf(minutes));

        etAmountPerDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    tvTotalHours.setText(String.valueOf(hours));
                    tvTotalMinutes.setText(String.valueOf(minutes));
                    tvTotalAmountToPay.setText("");
                }else {
                    /**
                     * Calculate per minute amount
                     */
                    double amountPerMinute = Double.parseDouble(s.toString()) / 60;
                    
                    tvTotalHours.setText(hours + " * " + s.toString());
                    tvTotalMinutes.setText(minutes + " * " + amountPerMinute);

                    int amountHourWise = Integer.parseInt(s.toString()) * hours;
                    double amountMinutesWise = minutes * amountPerMinute;
                    double finalAmount = amountHourWise + amountMinutesWise;
                    tvTotalAmountToPay.setText(CommonMethods.currencyFormatter(String.valueOf(finalAmount)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if(mPersonModel!=null){
            tvPersonName.setText(mPersonModel.getName());
            tvPersonMobileNo.setText(mPersonModel.getMobileNo());
            tvPersonWorkType.setText(mPersonModel.getWorkType());
            tvPersonDecidedFulDayAmount.setText(CommonMethods.currencyFormatter(String.valueOf(mPersonModel.getAmount())));
            etAmountPerDay.setText(String.valueOf(mPersonModel.getAmount()));
            tv_currency_symbol1.setText(mPersonModel.getCurrencySymbol());
            tv_currency_symbol2.setText(mPersonModel.getCurrencySymbol());
            tv_currency_symbol3.setText(mPersonModel.getCurrencySymbol());
        }
    }


    @Override
    public void onDestroyView() {
        CommonMethods.hideKeyboard(getActivity());
        super.onDestroyView();
    }
}
