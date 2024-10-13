package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.itextpdf.text.DocumentException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.Interface.IPermissionGrantedListener;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.NumberOfMondaysInMonth;

public class CalculateMonthWiseSalaryByAdminFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private EditText mEtTotalPaidDays,mEtAmountPerDay;
    private TextView mTvTotalPublicHolidays,mTvTotalNonWorkingDays;
    private PersonModel mPersonModel;
    private ArrayList<AttendanceModel> mTransactionList;
    private String salaryMonthYear;
    private CheckBox mCbTotalPublicHolidays,mCbTotalNonWorkingHolidays;
    private TextView mTvTotalDaysInMonth,mTvTotalAttendedDays,mTvTotalWorkingDays;
    private TextView mTvTotalPresentDays,mTvTotalHalfDays,mTvTotalAbsentDays;
    private TextView mTvTotalAmountToPay;
    private TextView mTvTotalOverTime;

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
        View view=inflater.inflate(R.layout.fragment_calculate_month_wise_salary_by_admin,container,false);
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

        if (getArguments() != null) {
            mPersonModel = (PersonModel) getArguments().getSerializable("PersonModel");
            mTransactionList = (ArrayList<AttendanceModel>)getArguments().getSerializable("List");
            salaryMonthYear = getArguments().getString("MonthYear");
        }else
            return;

        TextView tvPersonName=view.findViewById(R.id.tv_person_name);
        TextView tvPersonMobileNo=view.findViewById(R.id.tv_person_mobile_no);
        TextView tvPersonWorkType=view.findViewById(R.id.tv_person_work_type);
        TextView tvPersonDecidedAmount=view.findViewById(R.id.tv_person_decided_amount);
        mTvTotalDaysInMonth=view.findViewById(R.id.tv_total_days_in_month);
        mTvTotalAttendedDays=view.findViewById(R.id.tv_total_attended_days);
        mTvTotalWorkingDays=view.findViewById(R.id.tv_total_working_days);

        RelativeLayout rlTotalAttendedDays=view.findViewById(R.id.rl_total_attended_days);
        RelativeLayout rlTotalActualWorkingDays=view.findViewById(R.id.rl_total_actual_Working_days);
        LinearLayout llTotalAbsentDay=view.findViewById(R.id.ll_total_absent_days);
        LinearLayout llTotalPaidDay=view.findViewById(R.id.ll_total_paid_days);
        LinearLayout llTotalAmountPerDay=view.findViewById(R.id.ll_total_amount_per_day);
        LinearLayout llTotalAmountToPay=view.findViewById(R.id.ll_total_amount_to_pay);

        mTvTotalAbsentDays=view.findViewById(R.id.tv_absent_days);
        mTvTotalPresentDays=view.findViewById(R.id.tv_total_presents);
        mTvTotalHalfDays=view.findViewById(R.id.tv_total_half_days);
        mTvTotalPublicHolidays=view.findViewById(R.id.tv_total_public_holidays);
        mTvTotalNonWorkingDays=view.findViewById(R.id.tv_total_non_working_days);

        mCbTotalPublicHolidays=view.findViewById(R.id.cb_total_public_holidays);
        mCbTotalNonWorkingHolidays=view.findViewById(R.id.cb_total_non_working_days);

        mEtTotalPaidDays=view.findViewById(R.id.et_total_paid_days);
        mEtAmountPerDay=view.findViewById(R.id.et_amount_per_day);
        mTvTotalAmountToPay=view.findViewById(R.id.tv_total_amount_to_pay);
        mTvTotalOverTime=view.findViewById(R.id.tv_total_over_time);

        TextView tv_currency_symbol1=view.findViewById(R.id.tv_currency_symbol1);
        TextView tv_currency_symbol2=view.findViewById(R.id.tv_currency_symbol2);
        TextView tv_currency_symbol3=view.findViewById(R.id.tv_currency_symbol3);
        Button btnSendSalarySlip=view.findViewById(R.id.btn_send_salary_slip);

        rlTotalAttendedDays.setOnClickListener(this);
        rlTotalActualWorkingDays.setOnClickListener(this);
        llTotalAbsentDay.setOnClickListener(this);
        llTotalPaidDay.setOnClickListener(this);
        llTotalAmountPerDay.setOnClickListener(this);
        llTotalAmountToPay.setOnClickListener(this);
        mCbTotalPublicHolidays.setOnCheckedChangeListener(this);
        mCbTotalNonWorkingHolidays.setOnCheckedChangeListener(this);
        btnSendSalarySlip.setOnClickListener(this);

        if(mPersonModel!=null){
            tvPersonName.setText(mPersonModel.getName());
            tvPersonMobileNo.setText(mPersonModel.getMobileNo());
            tvPersonWorkType.setText(mPersonModel.getWorkType());
            tvPersonDecidedAmount.setText(CommonMethods.currencyFormatter(String.valueOf(mPersonModel.getAmount())));
            tv_currency_symbol1.setText(mPersonModel.getCurrencySymbol());
            tv_currency_symbol2.setText(mPersonModel.getCurrencySymbol());
            tv_currency_symbol3.setText(mPersonModel.getCurrencySymbol());
        }


        SimpleDateFormat monthYearFormat = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US);
        SimpleDateFormat monthFormat = new SimpleDateFormat(ConstantData.MONTH_FORMAT, Locale.US);
        SimpleDateFormat yearFormat = new SimpleDateFormat(ConstantData.YEAR_FORMAT, Locale.US);

        // Get a calendar instance
        Calendar calendar = Calendar.getInstance();
        if(salaryMonthYear.equalsIgnoreCase(monthYearFormat.format(new Date()))) {
            // Get the last date of the current month. To get the last date for a
            // specific month you can set the calendar month using calendar object
            // calendar.set(Calendar.MONTH, theMonth) method.
            int lastDate = calendar.getActualMaximum(Calendar.DATE);
            // Set the calendar date to the last date of the month so then we can
            // get the last day of the month
            calendar.set(Calendar.DATE, lastDate);
            mTvTotalDaysInMonth.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        }else{
            Date monthYearDate=null;
            try {
                monthYearDate = monthYearFormat.parse(salaryMonthYear);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar.set(Calendar.MONTH,monthYearDate.getMonth());
            calendar.set(Calendar.YEAR,monthYearDate.getYear());
            int lastDate = calendar.getActualMaximum(Calendar.DATE);
            calendar.set(Calendar.DATE, lastDate);
            mTvTotalDaysInMonth.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        }

        /**
         * Here we check whether target month is current month or not
         * if its current month then we count only those many days which
         * are till now
         */
        assert salaryMonthYear != null;
        if(salaryMonthYear.equalsIgnoreCase(monthYearFormat.format(new Date()))){
            int days = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            mTvTotalAttendedDays.setText(String.valueOf(days));
        }else{
            Date monthYearDate=null;
            try {
                monthYearDate = monthYearFormat.parse(salaryMonthYear);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(monthYearDate!=null) {
                int[] dayAndMonthNumber = NumberOfMondaysInMonth.getNumberofDaysAndMonthNumberByMonthName(
                        monthFormat.format(monthYearDate).toUpperCase(), yearFormat.format(monthYearDate)
                );
                int days = dayAndMonthNumber[0];
                mTvTotalAttendedDays.setText(String.valueOf(days));
            }
        }


        double TotalPresentDayCount = 0;
        double TotalHalfDays = 0;
        double TotalAbsentDayCount = 0;
        double TotalPublicHolidayCount = 0;
        double TotalNonWorkingDayCount = 0;
        double TotalOverTimeInMinutes = 0;
        assert mTransactionList != null;
        if (mTransactionList.size() > 0) {
            for (AttendanceModel attendanceModel :
                    mTransactionList) {

                if(attendanceModel.getType().equalsIgnoreCase(getString(R.string.label_out_pending))){
                    TotalAbsentDayCount++;
                }else if(attendanceModel.getType().equalsIgnoreCase(getString(R.string.label_full_days))){
                    TotalPresentDayCount++;
                }else if(attendanceModel.getType().equalsIgnoreCase(getString(R.string.label_half_days))){
                    TotalHalfDays=TotalHalfDays+0.5;
                }else if(attendanceModel.getType().equalsIgnoreCase(getString(R.string.label_present_but_leave))){
                    TotalAbsentDayCount++;
                }else if(attendanceModel.getType().equalsIgnoreCase(getString(R.string.label_public_holiday))){
                    TotalPublicHolidayCount++;
                }else if(attendanceModel.getType().equalsIgnoreCase(getString(R.string.label_non_working_day))){
                    TotalNonWorkingDayCount++;
                }
                TotalOverTimeInMinutes=TotalOverTimeInMinutes+attendanceModel.getOverTimeInMinutes();
            }
        }

        mTvTotalPresentDays.setText(String.valueOf(TotalPresentDayCount));
        mTvTotalHalfDays.setText(String.valueOf(TotalHalfDays));
        mTvTotalPublicHolidays.setText(String.valueOf(TotalPublicHolidayCount));
        mTvTotalNonWorkingDays.setText(String.valueOf(TotalNonWorkingDayCount));
        // Total Attended Days - (Total Public Holiday + Total Non Working Days)
        double totalWorkingDays = Double.valueOf(mTvTotalAttendedDays.getText().toString().trim())
                - (TotalPublicHolidayCount + TotalNonWorkingDayCount);
        mTvTotalWorkingDays.setText(String.valueOf(totalWorkingDays));

        /**
         * Leave day means user not come at office though its working days
         * so we simply deduct sum of Total Present + Total Half Days
         * from Total Working Days
         */
        double TotalLeavesCount = totalWorkingDays-(TotalPresentDayCount+TotalHalfDays);
        // Total Absent Days + Total Leaves
        mTvTotalAbsentDays.setText(String.valueOf(TotalLeavesCount));
        
        //double totalPaidDays=TotalPresentDayCount+TotalHalfDays+TotalPublicHolidayCount+TotalNonWorkingDayCount;
        double totalPaidDays=TotalPresentDayCount+TotalHalfDays;
        mEtTotalPaidDays.setText(String.valueOf(totalPaidDays));

        assert mPersonModel != null;
        double perDayAmount = (double)mPersonModel.getAmount() / Integer.valueOf(mTvTotalDaysInMonth.getText().toString().trim());
        mEtAmountPerDay.setText(ConstantData.df.format(perDayAmount));

        double finalPayment = perDayAmount * totalPaidDays;
        mTvTotalAmountToPay.setText(CommonMethods.currencyFormatter(ConstantData.df.format(finalPayment)));

        mEtTotalPaidDays.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    mTvTotalAmountToPay.setText(String.valueOf(0));
                }else {
                    String strAmountPerDay = mEtAmountPerDay.getText().toString().trim();
                    if(strAmountPerDay.trim().length()>0){
                        mTvTotalAmountToPay.setText(
                                CommonMethods.currencyFormatter(
                                        ConstantData.df.format(Double.valueOf(strAmountPerDay) * Double.valueOf(s.toString()))));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEtAmountPerDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    mTvTotalAmountToPay.setText(String.valueOf(0));
                }else {
                    String strTotalPaidDays = mEtTotalPaidDays.getText().toString().trim();
                    if(strTotalPaidDays.trim().length()>0){
                        mTvTotalAmountToPay.setText(
                                CommonMethods.currencyFormatter(
                                ConstantData.df.format(Double.valueOf(strTotalPaidDays) * Double.valueOf(s.toString()))));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mTvTotalOverTime.setText(CommonMethods.get24HoursFromMinutes((int)TotalOverTimeInMinutes));
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_send_salary_slip){
            CommonMethods.handleExternalStoragePermission(requireActivity(),
                    new IPermissionGrantedListener() {
                        @Override
                        public void isGranted(boolean b) {
                            createPDFFile();
                        }
                    });
        }else if(v.getId()==R.id.rl_total_attended_days){
            CommonMethods.showDialogForInformation(getActivity(),getString(R.string.label_total_absent_days),
                    getString(R.string.msg_info_salary_calc_total_attended_days));
        }else if(v.getId()==R.id.rl_total_actual_Working_days){
            CommonMethods.showDialogForInformation(getActivity(),getString(R.string.label_total_absent_days),
                    getString(R.string.msg_info_salary_calc_total_actual_working_days));
        }else if(v.getId()==R.id.ll_total_absent_days){
            CommonMethods.showDialogForInformation(getActivity(),getString(R.string.label_total_absent_days),
                    getString(R.string.msg_info_salary_calc_total_absent_days));
        }else if(v.getId()==R.id.ll_total_paid_days){
            CommonMethods.showDialogForInformation(getActivity(),getString(R.string.label_total_paid_days),
                    getString(R.string.msg_info_salary_calc_total_paid_days));
        }else if(v.getId()==R.id.ll_total_amount_per_day){
            CommonMethods.showDialogForInformation(getActivity(),getString(R.string.label_amount_per_day),
                    getString(R.string.msg_info_salary_calc_total_amount_per_day));
        }else if(v.getId()==R.id.ll_total_amount_to_pay){
            CommonMethods.showDialogForInformation(getActivity(),getString(R.string.label_total_amount_to_pay),
                    getString(R.string.msg_info_salary_calc_total_amount_to_pay));
        }
    }



    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId()==R.id.cb_total_public_holidays){
            String strTotalPaidDays= mEtTotalPaidDays.getText().toString().trim();
            String strTotalPublicHolidays= mTvTotalPublicHolidays.getText().toString().trim();
            if(isChecked){
                mEtTotalPaidDays.setText(String.valueOf(Double.valueOf(strTotalPaidDays)+Double.valueOf(strTotalPublicHolidays)));
            }else{
                mEtTotalPaidDays.setText(String.valueOf(Double.valueOf(strTotalPaidDays)-Double.valueOf(strTotalPublicHolidays)));
            }
        }else if(buttonView.getId()==R.id.cb_total_non_working_days){
            String strTotalPaidDays_= mEtTotalPaidDays.getText().toString().trim();
            String strTotalNonWorkingDays= mTvTotalNonWorkingDays.getText().toString().trim();
            if(isChecked){
                mEtTotalPaidDays.setText(String.valueOf(Double.valueOf(strTotalPaidDays_)+Double.valueOf(strTotalNonWorkingDays)));
            }else{
                mEtTotalPaidDays.setText(String.valueOf(Double.valueOf(strTotalPaidDays_)-Double.valueOf(strTotalNonWorkingDays)));
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 102) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, do your work....
                createPDFFile();
            } else {
                // permission denied
                // Disable the functionality that depends on this permission.
            }
            // other 'case' statements for other permssions
        }
    }

    private void createPDFFile() {
        String strTotalDaysInMonth=mTvTotalDaysInMonth.getText().toString();
        String strTotalAttendedDays=mTvTotalAttendedDays.getText().toString();
        String strTotalWorkingDays=mTvTotalWorkingDays.getText().toString();
        String strTotalPresentDays=mTvTotalPresentDays.getText().toString();
        String strTotalHalfDays=mTvTotalHalfDays.getText().toString();
        String strTotalAbsentDays=mTvTotalAbsentDays.getText().toString();
        String strTotalPublicHolidays=mTvTotalPublicHolidays.getText().toString();
        String strTotalNonWorkingDays=mTvTotalNonWorkingDays.getText().toString();
        String strTotalPaidDays=mEtTotalPaidDays.getText().toString();
        String strAmountPerDay=mEtAmountPerDay.getText().toString();
        String strTotalAmountToPay=mTvTotalAmountToPay.getText().toString();
        try {
            new CommonMethods().createPDFForSalarySlip(
                    getActivity(), mPersonModel, mTransactionList, salaryMonthYear,
                    strTotalDaysInMonth,strTotalAttendedDays,strTotalWorkingDays,strTotalPresentDays,
                    strTotalHalfDays,strTotalAbsentDays,strTotalPublicHolidays,strTotalNonWorkingDays,
                    strTotalPaidDays,strAmountPerDay,strTotalAmountToPay
                    , mCbTotalPublicHolidays.isChecked(), mCbTotalNonWorkingHolidays.isChecked());
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroyView() {
        CommonMethods.hideKeyboard(getActivity());
        super.onDestroyView();
    }
}
