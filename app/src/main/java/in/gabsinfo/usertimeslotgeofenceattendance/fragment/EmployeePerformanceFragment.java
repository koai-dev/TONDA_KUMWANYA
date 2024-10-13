package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.Interface.IRecyclerViewItemClickListener;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.EmployeePerformanceAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.UserAttendanceHistoryAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PerformanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;

public class EmployeePerformanceFragment extends Fragment implements View.OnClickListener, IRecyclerViewItemClickListener {

    private TextView mTvSelectedFromMonth;
    private TextView mTvSelectedToMonth;
    private PersonModel mPersonModel;
    private PieChart pieChart;
    private ArrayList<PerformanceModel> mList=new ArrayList<>();
    private EmployeePerformanceAdapter mAdapter;
    private BottomSheetBehavior mBottomSheetBehavior;
    private View mPersistentbottomSheet;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemSearch=menu.findItem(R.id.action_search);
        if(itemSearch!=null) {
            itemSearch.setVisible(false);
        }

        MenuItem itemLogout = menu.findItem(R.id.item_logout);
        itemLogout.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_performance, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        Bundle bundle = getArguments();
        if (bundle!=null){
            mPersonModel = (PersonModel) bundle.getSerializable("PersonModel");
        }

        setToolbar(view);
        init(view);
        return view;
    }


    private void setToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        assert mPersonModel != null;
        toolbar.setTitle(mPersonModel.getName());

        if(getActivity() instanceof AdminDashboardActivity){
            ((AdminDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }else if(getActivity() instanceof UserDashboardActivity){
            ((UserDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
            Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }

        Drawable drawable = toolbar.getNavigationIcon();
        assert drawable != null;
        drawable.setColorFilter(ContextCompat.getColor(requireActivity(),
                android.R.color.black), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
    }

    private void init(View view) {
        CardView cvStartDate=view.findViewById(R.id.cv_start_date);
        CardView cvEndDate=view.findViewById(R.id.cv_end_date);
        mTvSelectedFromMonth=view.findViewById(R.id.tv_selected_from_month);
        mTvSelectedToMonth=view.findViewById(R.id.tv_selected_to_month);
        Button btnApply =view.findViewById(R.id.btn_apply_filter);

        TextView tvOutPendingLabel=view.findViewById(R.id.tv_out_pending_label);
        TextView tvFullDaysLabel=view.findViewById(R.id.tv_full_days_label);
        TextView tvHalfDaysLabel=view.findViewById(R.id.tv_half_days_label);
        TextView tvPresentButLeaveLabel=view.findViewById(R.id.tv_present_but_leave_label);
        TextView tvWorkingHoursLabel=view.findViewById(R.id.tv_working_hours_label);


        CoordinatorLayout coordinatorLayout = view.findViewById(R.id.coordinator);
        mPersistentbottomSheet = coordinatorLayout.findViewById(R.id.bottomSheetLayout);
        mBottomSheetBehavior = BottomSheetBehavior.from(mPersistentbottomSheet);
        mPersistentbottomSheet.setVisibility(View.GONE);
        RelativeLayout mBottomLayout = view.findViewById(R.id.bottom_layout);
        RecyclerView rv = view.findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                layoutManager.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);
        mAdapter = new EmployeePerformanceAdapter(getActivity(),this,mPersonModel.getWorkType(),mList);
        rv.setAdapter(mAdapter);

        pieChart = view.findViewById(R.id.idPieChart);
        pieChart.setDescription("");
        pieChart.setCenterText(generateCenterText());
        pieChart.setCenterTextSize(10f);
        // radius of the center hole in percent of maximum radius
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);

        // enable rotation of the chart by touch
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(false);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                mBottomLayout.performClick();
            }

            @Override
            public void onNothingSelected() {
                mBottomLayout.performClick();
            }
        });

        Legend l = pieChart.getLegend();
        l.setXEntrySpace(10);
        l.setYEntrySpace(0);
        l.setTextColor(Color.BLACK);
        l.setEnabled(true);

         if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
            tvHalfDaysLabel.setVisibility(View.GONE);
            tvPresentButLeaveLabel.setVisibility(View.GONE);
            tvWorkingHoursLabel.setVisibility(View.VISIBLE);
        }else{
             tvHalfDaysLabel.setVisibility(View.VISIBLE);
             tvPresentButLeaveLabel.setVisibility(View.VISIBLE);
             tvWorkingHoursLabel.setVisibility(View.GONE);
         }

        mBottomLayout.setOnClickListener(this);
        cvStartDate.setOnClickListener(this);
        cvEndDate.setOnClickListener(this);
        btnApply.setOnClickListener(this);
    }

    private SpannableString generateCenterText() {
        SpannableString s = new SpannableString("Attendance\nRecords");
        s.setSpan(new RelativeSizeSpan(2f), 0, 10, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 10, s.length(), 0);
        return s;
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.cv_start_date){
            setDate(mTvSelectedFromMonth);
        }else if(v.getId()==R.id.cv_end_date){
            setDate(mTvSelectedToMonth);
        }else if(v.getId()==R.id.btn_apply_filter){
            if(mAdapter!=null){
                mAdapter.clearAdapter();
            }
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            mPersistentbottomSheet.setVisibility(View.GONE);

            getAttendanceHistoryForCustomRange();
        }else if(v.getId()==R.id.bottom_layout){
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    @Override
    public void onRecyclerViewItemClick(Object object) {
        getAttendanceRecordForSelectedDate(((PerformanceModel)object).getDate());
    }

    /**
     * Here we attendance records for selected custom range
     */
    private void getAttendanceHistoryForCustomRange() {
        String selectedFromMonth = mTvSelectedFromMonth.getText().toString().trim();
        String selectedToMonth = mTvSelectedToMonth.getText().toString().trim();
        if (selectedFromMonth.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.msg_select_from_date_value), Toast.LENGTH_LONG).show();
        } else if (selectedToMonth.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.msg_select_to_date_value), Toast.LENGTH_LONG).show();
        }else {
            SimpleDateFormat format = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US);
            Date fromDate=null;
            try {
                fromDate = format.parse(selectedFromMonth);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Date toDate=null;
            try {
                toDate = format.parse(selectedToMonth);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            assert fromDate != null;
            assert toDate != null;
            if(fromDate.after(toDate)){
                Toast.makeText(getActivity(), getString(R.string.msg_to_date_always_greatern_then_from_date), Toast.LENGTH_LONG).show();
            }else{

                ArrayList<String> monthYearList=new ArrayList<>();
                if(fromDate.equals(toDate)){
                    monthYearList.add(selectedFromMonth);
                }else{
                    //Here we get list of month and year between two date
                    Calendar beginCalendar = Calendar.getInstance();
                    Calendar finishCalendar = Calendar.getInstance();

                    beginCalendar.setTime(fromDate);
                    finishCalendar.setTime(toDate);

                    while (beginCalendar.before(finishCalendar)) {
                        String date =format.format(beginCalendar.getTime());
                        monthYearList.add(date);
                        // Add One Month to get next Month
                        beginCalendar.add(Calendar.MONTH, 1);
                    }
                    monthYearList.add(selectedToMonth);
                }

                HashMap<String, PerformanceModel> hashMap = new HashMap<>();
                if (monthYearList.size() > 0) {
                    CommonMethods.showProgressDialog(getActivity());
                    for (String monthYear :
                            monthYearList) {
                        System.out.println(monthYear);
                        AttendanceApplication.refCompanyUserAttendanceDetails
                                .child(mPersonModel.getFirebaseKey())
                                .child(monthYear)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        PerformanceModel performanceModel = new PerformanceModel();
                                        performanceModel.setDate(dataSnapshot.getKey());

                                        int TotalFullDayCount = 0;
                                        int TotalHalfDayCount = 0;
                                        int TotalMissPunch = 0;
                                        int NoOfDaysUserPresentButLeaveBefore = 0;
                                        double TotalHalfDaySum = 0.0;
                                        int TotalMinutes = 0;
                                        String workType = mPersonModel.getWorkType();

                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                AttendanceModel detailModel = ds.getValue(AttendanceModel.class);

                                                /**
                                                 * Here we simply count present day of user
                                                 */
                                                assert detailModel != null;
                                                if (detailModel.getPresentDay() == -1) {
                                                    TotalMissPunch++;
                                                } else {

                                                    if (workType.equals(ConstantData.WORK_TYPE_HOUR_WISE)) {
                                                        if (detailModel.getPresentDay() == 1) {
                                                            TotalFullDayCount++;
                                                            String[] split = detailModel.getTotalWorkingHours().split(":");
                                                            Integer hours = Integer.valueOf(split[0]);
                                                            Integer minuts = Integer.valueOf(split[1]);

                                                            TotalMinutes = TotalMinutes + ((hours * 60) + minuts);
                                                        }
                                                    }else{
                                                        if (detailModel.getPresentDay() == 1) {
                                                            TotalFullDayCount++;
                                                        } else if (detailModel.getPresentDay() == 0.5) {
                                                            TotalHalfDayCount++;
                                                            TotalHalfDaySum = TotalHalfDaySum + detailModel.getPresentDay();
                                                        } else if (detailModel.getPresentDay() == 0) {
                                                            NoOfDaysUserPresentButLeaveBefore++;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        performanceModel.setOutPending(TotalMissPunch);
                                        performanceModel.setFullDay(TotalFullDayCount);
                                        if (workType.equals(ConstantData.WORK_TYPE_HOUR_WISE)) {
                                            performanceModel.setTotalMinutes(TotalMinutes);
                                        }else{
                                            performanceModel.setHalfDay(TotalHalfDayCount);
                                            performanceModel.setPresentButLeave(NoOfDaysUserPresentButLeaveBefore);
                                        }
                                        /**
                                         * Add data into recyclerview
                                         */
                                        if(mAdapter!=null){
                                            mAdapter.addItem(performanceModel);
                                        }
                                        /**
                                         * Add data for chart
                                         */
                                        hashMap.put(monthYear, performanceModel);
                                        /**
                                         * Here we call method to display data into chart only
                                         * when we get data for all month
                                         */
                                        if(hashMap.size()==monthYearList.size()) {
                                            addDataSet(hashMap);
                                            mPersistentbottomSheet.setVisibility(View.VISIBLE);
                                            CommonMethods.cancelProgressDialog();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        CommonMethods.cancelProgressDialog();
                                    }
                                });
                    }
                }
            }
        }
    }

    private void getAttendanceRecordForSelectedDate(String selectedDate){
        if (CommonMethods.isNetworkConnected(requireActivity())) {

            ArrayList<AttendanceModel> mTransactionList = new ArrayList<AttendanceModel>();

            CommonMethods.showProgressDialog(getActivity());
            AttendanceApplication.refCompanyUserAttendanceDetails
                    .child(mPersonModel.getFirebaseKey())
                    .child(selectedDate)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            mTransactionList.clear();

                            if (dataSnapshot.exists()) {

                                double TotalHalfDaySum = 0.0;
                                int TotalMinutes = 0;

                                String workType = mPersonModel.getWorkType();

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                    AttendanceModel detailModel = ds.getValue(AttendanceModel.class);
                                    assert detailModel != null;
                                    detailModel.setFirebaseKey(ds.getKey());
                                    mTransactionList.add(detailModel);



                                    /**
                                     * Here we simply count present day of user
                                     */
                                    if (detailModel.getPresentDay() == -1) {
                                        detailModel.setType(getString(R.string.label_out_pending));
                                    } else {

                                        if (workType.equals(ConstantData.WORK_TYPE_HOUR_WISE)) {
                                            if (detailModel.getPresentDay() == 1) {
                                                detailModel.setType(getString(R.string.label_full_days));

                                                String[] split = detailModel.getTotalWorkingHours().split(":");
                                                Integer hours = Integer.valueOf(split[0]);
                                                Integer minuts = Integer.valueOf(split[1]);

                                                TotalMinutes = TotalMinutes + ((hours * 60) + minuts);
                                            }
                                        }else{
                                            if (detailModel.getPresentDay() == 1) {
                                                detailModel.setType(getString(R.string.label_full_days));
                                            } else if (detailModel.getPresentDay() == 0.5) {
                                                TotalHalfDaySum = TotalHalfDaySum + detailModel.getPresentDay();
                                                detailModel.setType(getString(R.string.label_half_days));
                                            } else if (detailModel.getPresentDay() == 0) {
                                                detailModel.setType(getString(R.string.label_present_but_leave));
                                            }
                                        }
                                    }
                                }
                            }

                            CommonMethods.cancelProgressDialog();
                            if (mTransactionList.size() == 0) {
                                Toast.makeText(getActivity(), getString(R.string.msg_no_attendance_found), Toast.LENGTH_SHORT).show();
                            }else{
                                showDialogForAttendanceHistory(selectedDate,mTransactionList);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            CommonMethods.cancelProgressDialog();
                            Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            CommonMethods.showConnectionAlert(getActivity());
        }
    }

    /**
     * Here we display selected day detail records into recycler view
     * @param selectedDate
     * @param mTransactionList
     */
    private void showDialogForAttendanceHistory(String selectedDate,ArrayList<AttendanceModel> mTransactionList){
        AlertDialog.Builder builder=new AlertDialog.Builder(requireActivity(),R.style.CustomDialogTheme);
        @SuppressLint("InflateParams")
        View view=LayoutInflater.from(getActivity()).inflate(R.layout.dialog_attendance_history,null);
        TextView tvSelectedMonth=view.findViewById(R.id.tv_selected_month);
        RecyclerView rv=view.findViewById(R.id.rv);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mLayoutManager);
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(rv.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.horizontal_divider);
        assert horizontalDivider != null;
        horizontalDecoration.setDrawable(horizontalDivider);
        rv.addItemDecoration(horizontalDecoration);
        builder.setView(view);

        builder.setPositiveButton(getActivity().getString(R.string.action_ok), null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

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

        tvSelectedMonth.setText(selectedDate);
        UserAttendanceHistoryAdapter mAdapter = new UserAttendanceHistoryAdapter(getActivity(),
                mPersonModel.getWorkType(), mTransactionList,
                new UserAttendanceHistoryAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(AttendanceModel model, int position) {

                    }
                });
        rv.setAdapter(mAdapter);
    }



    private void setDate(TextView tv) {
        Calendar today = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getActivity(), new MonthPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int selectedMonth, int selectedYear) {

                Date selectedMonthYearDate = null;
                try {
                    selectedMonthYearDate = new SimpleDateFormat("MM/yyyy", Locale.US).parse(selectedMonth + 1 + "/" + selectedYear);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (selectedMonthYearDate != null) {
                    String selectedMonthYear = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US).format(selectedMonthYearDate);
                    tv.setText(selectedMonthYear);
                }
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));

        builder.setActivatedMonth(today.get(Calendar.MONTH))
                .setMinYear(2019)
                .setActivatedYear(today.get(Calendar.YEAR))
                .setMaxYear(today.get(Calendar.YEAR))
                .setMinMonth(Calendar.FEBRUARY)
                .setTitle(getString(R.string.alert_title_select_month))
                .setMonthRange(Calendar.JANUARY, Calendar.DECEMBER)
                .build()
                .show();
    }



    //http://ctalandroid.blogspot.com/2019/02/android-bar-chart-using-mpandroidchart.html
    private void addDataSet(HashMap<String, PerformanceModel> hashMap) {

        int TotalFullDayCount = 0;
        int TotalHalfDayCount = 0;
        int TotalMissPunch = 0;
        int NoOfDaysUserPresentButLeaveBefore = 0;
        int TotalMinutes = 0;

        Iterator iterator = hashMap.keySet().iterator();
        while( iterator. hasNext() )
        {
            PerformanceModel model = hashMap.get(iterator.next());

            assert model != null;
            TotalFullDayCount=TotalFullDayCount+model.getFullDay();
            TotalHalfDayCount=TotalHalfDayCount+model.getHalfDay();
            TotalMissPunch=TotalMissPunch+model.getOutPending();
            NoOfDaysUserPresentButLeaveBefore=NoOfDaysUserPresentButLeaveBefore+model.getPresentButLeave();
            TotalMinutes=TotalMinutes+model.getTotalMinutes();
        }

        ArrayList<Entry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();
        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        if(TotalMissPunch>0) {
            xEntrys.add(getString(R.string.label_out_pending));
            yEntrys.add(new Entry(Float.valueOf(String.valueOf(TotalMissPunch)),0));
            colors.add(ContextCompat.getColor(requireActivity(),R.color.colorMissPunch));
        }

        if(TotalFullDayCount>0) {
            xEntrys.add(getString(R.string.label_full_days));
            yEntrys.add(new Entry(Float.valueOf(String.valueOf(TotalFullDayCount)),1));
            colors.add(ContextCompat.getColor(requireActivity(),R.color.colorFullDayPresent));
        }

        if(TotalHalfDayCount>0) {
            xEntrys.add(getString(R.string.label_half_days));
            yEntrys.add(new Entry(Float.valueOf(String.valueOf(TotalHalfDayCount)),2));
            colors.add(ContextCompat.getColor(requireActivity(),R.color.colorHalfDayPresent));
        }

        if(NoOfDaysUserPresentButLeaveBefore>0) {
            xEntrys.add(getString(R.string.label_present_but_leave));
            yEntrys.add(new Entry(Float.valueOf(String.valueOf(NoOfDaysUserPresentButLeaveBefore)),3));
            colors.add(ContextCompat.getColor(requireActivity(),R.color.colorPresentButLeaveBefore));
        }

        if (TotalMinutes > 0) {
            Date mmDate = null;
            try {
                mmDate = new SimpleDateFormat("mm", Locale.US).parse(String.valueOf(TotalMinutes));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(mmDate!=null) {
                xEntrys.add(getString(R.string.label_working_hours));
                yEntrys.add(new BarEntry(Float.valueOf(new SimpleDateFormat("HH.mm", Locale.US).format(mmDate)),4));
                colors.add(ContextCompat.getColor(requireActivity(), R.color.colorWorkingHours));
            }
        }
        PieDataSet  pieDataSet = new PieDataSet (yEntrys, "");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setSelectionShift(10);
        pieDataSet.setColors(colors);

        PieData pieData = new PieData(xEntrys, pieDataSet);
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.WHITE);
        pieChart.setData(pieData);
        // refresh/update pie chart
        pieChart.invalidate();
        // animate piechart
        //pieChart.animateY(5000);
        pieChart.animateXY(1000, 1000);
    }

    public void onBackPress() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else{
            requireActivity().getSupportFragmentManager().popBackStack();
            if(getActivity() instanceof UserDashboardActivity) {
                ((UserDashboardActivity) getActivity()).mFlContainer.setVisibility(View.GONE);
            }
        }
    }
}
