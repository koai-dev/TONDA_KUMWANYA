package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.DocumentException;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.Interface.IPermissionGrantedListener;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.DisplayAttendancePositionOnMapActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.UserAttendanceHistoryAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.calender.MaterialCalendar;
import in.gabsinfo.usertimeslotgeofenceattendance.calender.MaterialCalendarAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.HolidayModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.ShopTimingModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.NumberOfMondaysInMonth;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class UserAttendanceHistoryInCalendarFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private PersonModel mPersonModel;
    public GridView mCalendar;
    public ImageView mPrevious, mNext;
    private TextView mMonthName;
    private TextView mTvTotalOutPendingCount, mTvTotalFullDayCount, mTvTotalHalfDayCount, mTvTotalPresentButLeaveCount, mTvTotalWorkingHours;
    private MaterialCalendarAdapter mCalendarAdapter;
    public ArrayList<AttendanceModel> mTransactionList = new ArrayList<AttendanceModel>();
    public static int mNumEventsOnDay = 0;
    private CardView mCvNoOfDaysForPayment;
    private TextView mTvFinalTotalDaysForPayment;
    private boolean mIsAdminUser = SharePreferences.getBool(SharePreferences.KEY_IS_ADMIN_USER, SharePreferences.DEFAULT_BOOLEAN);
    private BottomSheetBehavior mBottomSheetBehavior;
    private TextView mTvBottomSheetViewCalendarSelectedDate, mTvBottomSheetViewPunchInTime, mTvBottomSheetViewPunchOutTime,
            mTvBottomSheetViewOverTime, mTvBottomSheetViewAttendanceType;
    private boolean mIsHolidayFetched = false;
    private boolean mIsAttendanceHistoryFetched = false;
    private Button mBtnBottomSheetViewAttendanceOnMap;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemLogout = menu.findItem(R.id.item_logout);
        if (itemLogout != null) {
            itemLogout.setVisible(false);
        }

        MenuItem itemSearch = menu.findItem(R.id.action_search);
        if (itemSearch != null) {
            itemSearch.setVisible(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_attendance_history_in_calendar, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //https://gist.github.com/ferdy182/d9b3525aa65b5b4c468a
        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), android.R.color.white));
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


        if (getArguments() != null) {
            mPersonModel = (PersonModel) getArguments().getSerializable("PersonModel");
        }
        if (mPersonModel != null) {
            setToolbar(view);
            init(view);
        }
        return view;
    }


    private void setToolbar(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        assert mPersonModel != null;
        toolbar.setTitle(mPersonModel.getName());

        if (getActivity() instanceof AdminDashboardActivity) {
            ((AdminDashboardActivity) getActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AdminDashboardActivity) requireActivity()).onBackPressed();
                }
            });
        } else if (getActivity() instanceof UserDashboardActivity) {
            ((UserDashboardActivity) getActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
            Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((UserDashboardActivity) requireActivity()).onBackPressed();
                }
            });
        }
    }


    private void init(View view) {

        CoordinatorLayout coordinatorLayout = view.findViewById(R.id.coordinator);
        View persistentbottomSheet = coordinatorLayout.findViewById(R.id.bottomSheetLayout);
        mBottomSheetBehavior = BottomSheetBehavior.from(persistentbottomSheet);
        mTvBottomSheetViewCalendarSelectedDate = view.findViewById(R.id.tv_calendar_selected_date);
        mTvBottomSheetViewPunchInTime = view.findViewById(R.id.tv_punch_in_time);
        mTvBottomSheetViewPunchOutTime = view.findViewById(R.id.tv_punch_out_time);
        mTvBottomSheetViewOverTime = view.findViewById(R.id.tv_over_time);
        mTvBottomSheetViewAttendanceType = view.findViewById(R.id.tv_attendance_type);
        mBtnBottomSheetViewAttendanceOnMap = view.findViewById(R.id.btn_view_in_map_bottom_sheet_view);
        Button btnBottomSheetViewEditAttendance = view.findViewById(R.id.btn_edit_attendance_bottom_sheet_view);
        Button btnBottomSheetViewClose = view.findViewById(R.id.btn_close_bottom_sheet_view);

        mPrevious = view.findViewById(R.id.material_calendar_previous);
        mNext = view.findViewById(R.id.material_calendar_next);
        mMonthName = view.findViewById(R.id.material_calendar_month_name);

        mCalendar = view.findViewById(R.id.material_calendar_gridView);

        mTvTotalOutPendingCount = view.findViewById(R.id.tv_total_out_pending);
        mTvTotalFullDayCount = view.findViewById(R.id.tv_total_full_day);
        mTvTotalHalfDayCount = view.findViewById(R.id.tv_total_half_day);
        mTvTotalPresentButLeaveCount = view.findViewById(R.id.tv_total_present_but_leave);
        mTvTotalWorkingHours =
                view.findViewById(R.id.tv_total_working_hours);

        LinearLayout llInfoFullDays =
                view.findViewById(R.id.ll_info_full_days);
        LinearLayout llInfoHalfDays =
                view.findViewById(R.id.ll_info_half_days);
        LinearLayout llInfoPresentButLeaveBefore =
                view.findViewById(R.id.ll_info_present_but_leave_before);
        LinearLayout mLlInfoMissPunch =
                view.findViewById(R.id.ll_info_miss_punch);
        LinearLayout llInfoTotalHours =
                view.findViewById(R.id.ll_info_total_hours);

        mCvNoOfDaysForPayment = view.findViewById(R.id.cv_no_of_days_for_payment);
        mTvFinalTotalDaysForPayment = view.findViewById(R.id.tv_final_total_days_for_payment);
        TextView tvCalculateAmount = view.findViewById(R.id.tv_calculate_amount);

        if (!mIsAdminUser) {
            mCvNoOfDaysForPayment.setVisibility(View.GONE);
        }

        if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
            llInfoFullDays.setVisibility(View.GONE);
            llInfoHalfDays.setVisibility(View.GONE);
            llInfoPresentButLeaveBefore.setVisibility(View.GONE);
            llInfoTotalHours.setVisibility(View.VISIBLE);
        } else {
            llInfoFullDays.setVisibility(View.VISIBLE);
            llInfoHalfDays.setVisibility(View.VISIBLE);
            llInfoPresentButLeaveBefore.setVisibility(View.VISIBLE);
            llInfoTotalHours.setVisibility(View.GONE);
        }

        if (!mIsAdminUser) {
            btnBottomSheetViewEditAttendance.setVisibility(View.GONE);
        }
        // Get Calendar info
        MaterialCalendar.getInitialCalendarInfo(UserAttendanceHistoryInCalendarFragment.this);

        // Month name TextView
        if (mMonthName != null) {
            Calendar cal = Calendar.getInstance();
            if (cal != null) {
                mMonthName.setText(new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US).format(new Date()));
            }
        }

        // GridView for custom Calendar
        if (mCalendar != null) {
            mCalendar.setOnItemClickListener(this);
            mCalendarAdapter = new MaterialCalendarAdapter(UserAttendanceHistoryInCalendarFragment.this);
            mCalendar.setAdapter(mCalendarAdapter);

            // Set current day to be auto selected when first opened
            if (MaterialCalendar.mCurrentDay != -1 && MaterialCalendar.mFirstDay != -1) {
                int startingPosition = 6 + MaterialCalendar.mFirstDay;
                int currentDayPosition = startingPosition + MaterialCalendar.mCurrentDay;

                //Log.d("INITIAL_SELECTED_POSITION", String.valueOf(currentDayPosition));
                mCalendar.setItemChecked(currentDayPosition, true);

                if (mCalendarAdapter != null) {
                    mCalendarAdapter.notifyDataSetChanged();
                }
            }
        }

        mPrevious.setOnClickListener(this);
        mNext.setOnClickListener(this);
        llInfoFullDays.setOnClickListener(this);
        llInfoHalfDays.setOnClickListener(this);
        llInfoPresentButLeaveBefore.setOnClickListener(this);
        mLlInfoMissPunch.setOnClickListener(this);
        llInfoTotalHours.setOnClickListener(this);
        tvCalculateAmount.setOnClickListener(this);
        mBtnBottomSheetViewAttendanceOnMap.setOnClickListener(this);
        btnBottomSheetViewEditAttendance.setOnClickListener(this);
        btnBottomSheetViewClose.setOnClickListener(this);

        /**
         * Here we wait 1 sec before calling API for fetch data
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getData();
                            }
                        });
                    }
                }
            }
        }).start();

    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.item_info){
            String strMessage = "";
            if (mIsAdminUser) {
                strMessage = AttendanceApplication.getAppContext().getString(R.string.alert_msg_info_admin_calculate_salary);
            } else {
                strMessage = AttendanceApplication.getAppContext().getString(R.string.alert_msg_info_user_calculate_salary);
            }
            CommonMethods.showDialogForInformation(requireActivity(),
                    AttendanceApplication.getAppContext().getString(R.string.alert_title_information), strMessage);
        }else if(item.getItemId()==R.id.item_list){
            if (mTransactionList != null
                    && mTransactionList.size() > 0) {
                showDialogForAttendanceHistory();
            } else {
                Toast.makeText(getActivity(), AttendanceApplication.getAppContext().getString(R.string.msg_no_data_found_to_share), Toast.LENGTH_SHORT).show();
            }
        }else if(item.getItemId()==R.id.item_share){
            if (mTransactionList != null
                    && mTransactionList.size() > 0) {
                checkForStoragePermission();
            } else {
                Toast.makeText(getActivity(), AttendanceApplication.getAppContext().getString(R.string.msg_no_data_found_to_share), Toast.LENGTH_SHORT).show();
            }
        }else if(item.getItemId()==R.id.item_show_attendance_position_on_map){
            if (mTransactionList != null
                    && mTransactionList.size() > 0) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("AttendanceList", mTransactionList);
                Intent intent = new Intent(getActivity(), DisplayAttendancePositionOnMapActivity.class);
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "No attendance found.", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v != null) {
            if(v.getId()==R.id.material_calendar_previous){
                MaterialCalendar.previousOnClick(mPrevious, mMonthName, mCalendar, mCalendarAdapter);
                openBottomSheetView(false);
            }else if(v.getId()==R.id.material_calendar_next){
                MaterialCalendar.nextOnClick(mNext, mMonthName, mCalendar, mCalendarAdapter);
                openBottomSheetView(false);
            }else if(v.getId()==R.id.ll_info_miss_punch){
                CommonMethods.showDialogForInformation(getActivity(),
                        AttendanceApplication.getAppContext().getString(R.string.label_out_pending),
                        AttendanceApplication.getAppContext().getString(R.string.msg_info_label_out_pending));
            }else if(v.getId()==R.id.ll_info_full_days){
                CommonMethods.showDialogForInformation(getActivity(),
                        AttendanceApplication.getAppContext().getString(R.string.label_full_days),
                        AttendanceApplication.getAppContext().getString(R.string.msg_info_full_days));
            }else if(v.getId()==R.id.ll_info_half_days){
                CommonMethods.showDialogForInformation(getActivity(),
                        AttendanceApplication.getAppContext().getString(R.string.label_half_days),
                        AttendanceApplication.getAppContext().getString(R.string.msg_info_half_days));
            }else if(v.getId()==R.id.ll_info_present_but_leave_before){
                CommonMethods.showDialogForInformation(getActivity(),
                        AttendanceApplication.getAppContext().getString(R.string.label_present_but_leave),
                        AttendanceApplication.getAppContext().getString(R.string.msg_info_present_but_leave_before));
            }else if(v.getId()==R.id.ll_info_total_hours){
                CommonMethods.showDialogForInformation(getActivity(),
                        AttendanceApplication.getAppContext().getString(R.string.label_working_hours),
                        AttendanceApplication.getAppContext().getString(R.string.msg_info_total_hours));
            }else if(v.getId()==R.id.tv_calculate_amount){
                if (getActivity() != null
                        && getActivity() instanceof AdminDashboardActivity) {
                    if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
                        if (!mTvFinalTotalDaysForPayment.getText().toString().equalsIgnoreCase("0")) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("PersonModel", mPersonModel);
                            bundle.putString("NoOfHoursForPay", mTvFinalTotalDaysForPayment.getText().toString().trim());
                            ((AdminDashboardActivity) getActivity()).loadFragment(new CalculateHourWiseSalaryByAdminFragment(), bundle);
                        } else {
                            Toast.makeText(getActivity(), AttendanceApplication.getAppContext().getString(R.string.msg_no_any_valid_hours_for_payment), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (mTransactionList != null
                                && mTransactionList.size() > 0) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("PersonModel", mPersonModel);
                            bundle.putSerializable("List", mTransactionList);
                            bundle.putString("MonthYear", mMonthName.getText().toString().trim());
                            if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_DAY_WISE)) {
                                ((AdminDashboardActivity) getActivity()).loadFragment(new CalculateDayWiseSalaryByAdminFragment(), bundle);
                            } else if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_MONTH_WISE)) {
                                ((AdminDashboardActivity) getActivity()).loadFragment(new CalculateMonthWiseSalaryByAdminFragment(), bundle);
                            }
                        }
                    }
                }
            }else if(v.getId()==R.id.btn_view_in_map_bottom_sheet_view){
                AttendanceModel attendanceModel = (AttendanceModel) v.getTag();
                if (attendanceModel != null) {
                    if ((attendanceModel.getPunchInLatitude() != 0
                            && attendanceModel.getPunchInLongitude() != 0)
                            || (attendanceModel.getPunchOutLatitude() != 0
                            && attendanceModel.getPunchOutLongitude() != 0)) {
                        Bundle bundle = new Bundle();
                        ArrayList<AttendanceModel> attendanceModelArrayList = new ArrayList<>();
                        attendanceModelArrayList.add(attendanceModel);
                        bundle.putSerializable("AttendanceList", attendanceModelArrayList);
                        Intent intent = new Intent(getActivity(), DisplayAttendancePositionOnMapActivity.class);
                        intent.putExtras(bundle);
                        getActivity().startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), "No attendance location found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }else if(v.getId()==R.id.btn_edit_attendance_bottom_sheet_view){
                if (getActivity() != null
                        && getActivity() instanceof AdminDashboardActivity) {
                    if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("PersonModel", mPersonModel);
                        bundle.putString("SelectedDate", mTvBottomSheetViewCalendarSelectedDate.getText().toString().trim());
                        ((AdminDashboardActivity) getActivity()).loadFragment(new AdminMarkAttendanceFragment(), bundle);
                    } else {
                        checkDayIsValidForAttendance(null, mTvBottomSheetViewCalendarSelectedDate.getText().toString().trim());
                    }
                    //Don't switch position
                    openBottomSheetView(false);
                }
            }else if(v.getId()==R.id.btn_close_bottom_sheet_view){
                openBottomSheetView(false);
            }
        }
    }

    private void openBottomSheetView(boolean open) {

        if (open) {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        } else {
            mTvBottomSheetViewCalendarSelectedDate.setText("");
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getId() == R.id.material_calendar_gridView) {// Reset event list
            mNumEventsOnDay = -1;

            MaterialCalendar.selectCalendarDay(mCalendarAdapter, position);
            int selectedDay = -1;

            if (MaterialCalendar.mFirstDay != -1) {
                selectedDay = position - (6 + MaterialCalendar.mFirstDay);

                if (mTransactionList != null
                        && mTransactionList.size() > 0) {

                    int length = mTransactionList.size();
                    boolean isTransactionAdded = false;
                    for (int i = 0; i < length; i++) {
                        AttendanceModel model = mTransactionList.get(i);
                        if (selectedDay == model.getDay()) {
                            if (model.isTransactionAdded()) {
                                if (model.getType().equalsIgnoreCase(AttendanceApplication.getAppContext().getString(R.string.label_public_holiday))) {
                                    Toast.makeText(getActivity(), model.getDescription(), Toast.LENGTH_SHORT).show();
                                } else if (model.getType().equalsIgnoreCase(AttendanceApplication.getAppContext().getString(R.string.label_non_working_day))) {
                                    Toast.makeText(getActivity(), AttendanceApplication.getAppContext().getString(R.string.label_non_working_day), Toast.LENGTH_SHORT).show();
                                } else {
                                    isTransactionAdded = true;
                                    mTvBottomSheetViewCalendarSelectedDate.setText(model.getDate());

                                    if (model.getPunchInTime() != null) {
                                        mTvBottomSheetViewPunchInTime.setText(model.getPunchInTime());
                                        mTvBottomSheetViewPunchInTime.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimaryText));
                                    } else {
                                        mTvBottomSheetViewPunchInTime.setText("N/A");
                                        mTvBottomSheetViewPunchInTime.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimaryText));
                                    }

                                    if (model.getPunchOutTime() != null) {
                                        mTvBottomSheetViewPunchOutTime.setText(model.getPunchOutTime());
                                        mTvBottomSheetViewPunchOutTime.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
                                    } else {
                                        mTvBottomSheetViewPunchOutTime.setText("N/A");
                                        mTvBottomSheetViewPunchOutTime.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
                                    }

                                    if (model.getPunchInTime() != null
                                            && model.getPunchOutTime() != null) {
                                        if ((int) model.getOverTimeInMinutes() != 0) {
                                            mTvBottomSheetViewOverTime.setText(CommonMethods.get24HoursFromMinutes((int) model.getOverTimeInMinutes()).concat(" H"));
                                        } else {
                                            mTvBottomSheetViewOverTime.setText("N/A");
                                        }
                                        mTvBottomSheetViewOverTime.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
                                    } else {
                                        mTvBottomSheetViewOverTime.setText("N/A");
                                        mTvBottomSheetViewOverTime.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
                                    }
                                    mTvBottomSheetViewAttendanceType.setText("( ".concat(model.getType()).concat(" )"));

                                    if (model.getPunchInLatitude() == 0
                                            && model.getPunchInLongitude() == 0
                                            && model.getPunchOutLatitude() == 0
                                            && model.getPunchOutLongitude() == 0) {
                                        mBtnBottomSheetViewAttendanceOnMap.setVisibility(View.GONE);
                                    } else {
                                        mBtnBottomSheetViewAttendanceOnMap.setVisibility(View.VISIBLE);
                                    }
                                    mBtnBottomSheetViewAttendanceOnMap.setTag(model);
                                    openBottomSheetView(true);
                                }
                            }
                            break;
                        }
                    }
                    if (!isTransactionAdded) {
                        openBottomSheetView(false);
                        mBtnBottomSheetViewAttendanceOnMap.setTag(null);
                    }
                }
            }
        }
    }

    private SimpleDateFormat format = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US);
    private SimpleDateFormat ddFormat = new SimpleDateFormat("dd", Locale.US);

    public void getData() {
        setDefaultValues();
        getAttendanceHistory();
        getPublicHolidays();
        if (!mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
            getNonWorkingDays();
        }
    }

    private void getAttendanceHistory() {
        CommonMethods.showProgressDialog(getActivity());
        AttendanceApplication.refCompanyUserAttendanceDetails
                .child(mPersonModel.getFirebaseKey())
                .child(mMonthName.getText().toString().trim())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mIsAttendanceHistoryFetched = true;
                        if (dataSnapshot.exists()) {

                            int TotalValidPresentDay = 0;
                            int TotalFullDayCount = 0;
                            int TotalHalfDayCount = 0;
                            double TotalHalfDaySum = 0.0;
                            int TotalMissPunch = 0;
                            int NoOfDaysUserPresentButLeaveBefore = 0;
                            int TotalMinutes = 0;

                            String workType = mPersonModel.getWorkType();

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                AttendanceModel detailModel = ds.getValue(AttendanceModel.class);
                                assert detailModel != null;
                                detailModel.setFirebaseKey(ds.getKey());
                                Log.d("History", detailModel.getPunchDate());
                                Date date = null;
                                try {
                                    date = format.parse(detailModel.getPunchDate());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (date != null) {
                                    detailModel.setDate(detailModel.getPunchDate());
                                    detailModel.setDay(Integer.valueOf(ddFormat.format(date)));
                                    detailModel.setTransactionAdded(true);
                                }

                                /**
                                 * Here we simply count present day of user
                                 */
                                if (detailModel.getPresentDay() == -1) {
                                    TotalMissPunch++;
                                    detailModel.setType(AttendanceApplication.getAppContext().getString(R.string.label_out_pending));
                                } else {

                                    if (workType.equals(ConstantData.WORK_TYPE_HOUR_WISE)) {
                                        if (detailModel.getPresentDay() == 1) {
                                            TotalFullDayCount++;
                                            detailModel.setType(AttendanceApplication.getAppContext().getString(R.string.label_full_days));

                                            String[] split = detailModel.getTotalWorkingHours().split(":");
                                            Integer hours = Integer.valueOf(split[0]);
                                            Integer minuts = Integer.valueOf(split[1]);

                                            TotalMinutes = TotalMinutes + ((hours * 60) + minuts);
                                        }
                                    } else {
                                        if (detailModel.getPresentDay() == 1) {
                                            TotalFullDayCount++;
                                            detailModel.setType(AttendanceApplication.getAppContext().getString(R.string.label_full_days));
                                        } else if (detailModel.getPresentDay() == 0.5) {
                                            TotalHalfDayCount++;
                                            TotalHalfDaySum = TotalHalfDaySum + detailModel.getPresentDay();
                                            detailModel.setType(AttendanceApplication.getAppContext().getString(R.string.label_half_days));
                                        } else if (detailModel.getPresentDay() == 0) {
                                            NoOfDaysUserPresentButLeaveBefore++;
                                            detailModel.setType(AttendanceApplication.getAppContext().getString(R.string.label_present_but_leave));
                                        }
                                    }
                                }
                                mTransactionList.add(detailModel);
                            }

                            TotalValidPresentDay = TotalFullDayCount + TotalHalfDayCount;

                            mTvTotalOutPendingCount.setText(String.valueOf(TotalMissPunch));

                            if (workType.equals(ConstantData.WORK_TYPE_HOUR_WISE)) {
                                Date mmDate = null;
                                try {
                                    mmDate = new SimpleDateFormat("mm", Locale.US).parse(String.valueOf(TotalMinutes));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (mmDate != null) {
                                    mTvTotalWorkingHours.setText(CommonMethods.actualTimeFormat.format(mmDate).concat(" ").concat(AttendanceApplication.getAppContext().getString(R.string.label_hours)));
                                    mTvFinalTotalDaysForPayment.setText(CommonMethods.actualTimeFormat.format(mmDate));
                                }
                            } else {
                                mTvTotalFullDayCount.setText(String.valueOf(TotalFullDayCount));
                                mTvTotalHalfDayCount.setText(String.valueOf(TotalHalfDayCount));
                                mTvTotalPresentButLeaveCount.setText(String.valueOf(NoOfDaysUserPresentButLeaveBefore));

                                double finalNoOfDays = TotalFullDayCount + TotalHalfDaySum;
                                if (finalNoOfDays % 1 == 0)  // true: it's an integer, false: it's not an integer
                                    mTvFinalTotalDaysForPayment.setText(String.valueOf((int) finalNoOfDays));
                                else
                                    mTvFinalTotalDaysForPayment.setText(String.valueOf(finalNoOfDays));
                            }

                            if (mTransactionList.size() == 0) {
                                Toast.makeText(getActivity(), AttendanceApplication.getAppContext().getString(R.string.msg_no_attendance_found), Toast.LENGTH_SHORT).show();
                            }
                        }
                        CommonMethods.cancelProgressDialog();
                        if (mCalendarAdapter != null) {
                            mCalendarAdapter.notifyDataSetChanged();
                        }

                        if (mIsAttendanceHistoryFetched
                                && mIsHolidayFetched) {
                            getAbsentDays();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        if (mCalendarAdapter != null) {
                            mCalendarAdapter.notifyDataSetChanged();
                        }
                    }
                });

    }


    /**
     * Here we fetch month wise public holidays
     */
    private void getPublicHolidays() {

        CommonMethods.showProgressDialog(getActivity());
        AttendanceApplication.refPublicHoliday
                .orderByChild("monthYear").equalTo(mMonthName.getText().toString().trim())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mIsHolidayFetched = true;
                        if (dataSnapshot.exists()) {
                            String currentDateStr =
                                    new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(new Date());

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                HolidayModel holidayModel = ds.getValue(HolidayModel.class);
                                assert holidayModel != null;
                                AttendanceModel attendanceModel = new AttendanceModel();
                                attendanceModel.setFirebaseKey(ds.getKey());

                                Date date = null;
                                Date currentDate = null;
                                try {
                                    date = format.parse(holidayModel.getDate());
                                    currentDate = format.parse(currentDateStr);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (date != null) {
                                    if (!date.after(currentDate)) {
                                        attendanceModel.setDate(holidayModel.getDate());
                                        attendanceModel.setDay(Integer.valueOf(ddFormat.format(date)));
                                        attendanceModel.setTransactionAdded(true);
                                        attendanceModel.setType(AttendanceApplication.getAppContext().getString(R.string.label_public_holiday));
                                        attendanceModel.setDescription(holidayModel.getDescription());
                                        mTransactionList.add(attendanceModel);
                                    }
                                }
                            }
                        }
                        CommonMethods.cancelProgressDialog();
                        if (mCalendarAdapter != null) {
                            mCalendarAdapter.notifyDataSetChanged();
                        }

                        if (mIsAttendanceHistoryFetched
                                && mIsHolidayFetched) {
                            getAbsentDays();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Here we get non-working day from time slot list
     */
    private void getNonWorkingDays() {
        /**
         * Here we get time slot list
         */
        ArrayList<ShopTimingModel> mTimeSlotArrayList = mPersonModel.getTimeSlotList();

        SimpleDateFormat monthYearFormat = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US);
        SimpleDateFormat monthFormat = new SimpleDateFormat(ConstantData.MONTH_FORMAT, Locale.US);
        SimpleDateFormat yearFormat = new SimpleDateFormat(ConstantData.YEAR_FORMAT, Locale.US);

        if (mTimeSlotArrayList != null
                && mTimeSlotArrayList.size() > 0) {
            for (ShopTimingModel shopTimingModel :
                    mTimeSlotArrayList) {
                if ((shopTimingModel.getFromTime() == null
                        || shopTimingModel.getFromTime().trim().length() == 0)
                        && (shopTimingModel.getToTime() == null
                        || shopTimingModel.getToTime().trim().length() == 0)) {
                    Date monthYearDate = null;
                    try {
                        monthYearDate = monthYearFormat.parse(mMonthName.getText().toString().trim());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (monthYearDate != null) {

                        String currentDateStr = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(new Date());

                        List<String> dateList = NumberOfMondaysInMonth.getNumberOfDateByMonthYearName(shopTimingModel.getDay(),
                                monthFormat.format(monthYearDate), yearFormat.format(monthYearDate));
                        if (dateList.size() > 0) {
                            for (String strDate :
                                    dateList) {
                                AttendanceModel attendanceModel = new AttendanceModel();
                                Date workOffDate = null;
                                Date currentDate = null;
                                try {
                                    workOffDate = format.parse(strDate);
                                    currentDate = format.parse(currentDateStr);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (workOffDate != null) {
                                    /**
                                     * Here we get only those workOffDate
                                     * which are not future date
                                     */
                                    if (!workOffDate.after(currentDate)) {
                                        attendanceModel.setDate(strDate);
                                        attendanceModel.setDay(Integer.valueOf(ddFormat.format(workOffDate)));
                                        attendanceModel.setTransactionAdded(true);
                                        attendanceModel.setType(AttendanceApplication.getAppContext().getString(R.string.label_non_working_day));
                                        mTransactionList.add(attendanceModel);
                                    }
                                }
                            }
                            if (mCalendarAdapter != null) {
                                mCalendarAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Here we get absent days means the days which are not in
     * No-Punch Out,Present Day,Half Day,Present But Leave,Public Holiday,Non Working Day category
     */
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US);
    private SimpleDateFormat monthFormat = new SimpleDateFormat(ConstantData.MONTH_FORMAT, Locale.US);
    private SimpleDateFormat yearFormat = new SimpleDateFormat(ConstantData.YEAR_FORMAT, Locale.US);

    private void getAbsentDays() {
        String selectedMonthYear = mMonthName.getText().toString().trim();//MMM yyyy

        //First we find total attendant days for selected month
        int totalAttendedDays = 0;
        if (selectedMonthYear.equalsIgnoreCase(monthYearFormat.format(new Date()))) {
            totalAttendedDays = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        } else {
            Date monthYearDate = null;
            try {
                monthYearDate = monthYearFormat.parse(selectedMonthYear);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (monthYearDate != null) {
                int[] dayAndMonthNumber = NumberOfMondaysInMonth.getNumberofDaysAndMonthNumberByMonthName(
                        monthFormat.format(monthYearDate).toUpperCase(), yearFormat.format(monthYearDate)
                );
                totalAttendedDays = dayAndMonthNumber[0];
            }
        }
        ArrayList<AttendanceModel> absentDaysList = new ArrayList<>();
        if (mTransactionList != null
                && mTransactionList.size() > 0) {

            //Here we check whether particular day from attendant days is exists into mTransactionList
            //if not exist than we consider that day as absent day
            int length = mTransactionList.size();
            for (int i = 1; i <= totalAttendedDays; i++) {
                //Formated date dd MMM yyyy like 10 JAN 2019
                String selectedDate = (i < 10 ? "0" + String.valueOf(i) : String.valueOf(i)).concat(" ").concat(selectedMonthYear);
                boolean isMatch = false;
                for (int j = 0; j < length; j++) {
                    if (mTransactionList.get(j).getDate().equalsIgnoreCase(selectedDate)) {
                        isMatch = true;
                        break;
                    }
                }
                if (!isMatch) {
                    AttendanceModel attendanceModel = new AttendanceModel();
                    attendanceModel.setDate(selectedDate);
                    attendanceModel.setDay(i);
                    attendanceModel.setTransactionAdded(true);
                    attendanceModel.setType(AttendanceApplication.getAppContext().getString(R.string.label_absent_day));
                    absentDaysList.add(attendanceModel);
                }
            }
        } else {
            for (int i = 1; i <= totalAttendedDays; i++) {
                //Formated date dd MMM yyyy like 10 JAN 2019
                String selectedDate = String.valueOf(i).concat(" ").concat(selectedMonthYear);

                AttendanceModel attendanceModel = new AttendanceModel();
                attendanceModel.setDate(selectedDate);
                attendanceModel.setDay(i);
                attendanceModel.setTransactionAdded(true);
                attendanceModel.setType(AttendanceApplication.getAppContext().getString(R.string.label_absent_day));
                absentDaysList.add(attendanceModel);
            }
        }
        assert mTransactionList != null;
        mTransactionList.addAll(absentDaysList);
        if (mCalendarAdapter != null) {
            mCalendarAdapter.notifyDataSetChanged();
        }
    }

    private void setDefaultValues() {
        if (mTransactionList != null)
            mTransactionList.clear();

        mIsHolidayFetched = false;
        mIsAttendanceHistoryFetched = false;
        mTvTotalOutPendingCount.setText(String.valueOf(0));
        mTvTotalFullDayCount.setText(String.valueOf(0));
        mTvTotalHalfDayCount.setText(String.valueOf(0));
        mTvTotalPresentButLeaveCount.setText(String.valueOf(0));
        mTvFinalTotalDaysForPayment.setText(String.valueOf(0));
        mTvTotalWorkingHours.setText(String.valueOf(0));
    }

    private void checkForStoragePermission() {
        CommonMethods.handleExternalStoragePermission(requireActivity(),
                new IPermissionGrantedListener() {
                    @Override
                    public void isGranted(boolean b) {
                        try {
                            new CommonMethods().createPDF(
                                    getActivity(), mPersonModel, mTransactionList, mMonthName.getText().toString().trim());
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    /**
     * Here we display records into recycler view
     */
    private void showDialogForAttendanceHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(),
                R.style.CustomDialogTheme);
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_attendance_history, null);
        TextView tvSelectedMonth = view.findViewById(R.id.tv_selected_month);
        RecyclerView rv = view.findViewById(R.id.rv);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mLayoutManager);
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(rv.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.horizontal_divider);
        assert horizontalDivider != null;
        horizontalDecoration.setDrawable(horizontalDivider);
        rv.addItemDecoration(horizontalDecoration);
        builder.setView(view);

        builder.setPositiveButton(AttendanceApplication.getAppContext().getString(R.string.action_ok), null);

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

        tvSelectedMonth.setText(mMonthName.getText().toString().trim());
        UserAttendanceHistoryAdapter mAdapter = new UserAttendanceHistoryAdapter(getActivity(),
                mPersonModel.getWorkType(), mTransactionList,
                new UserAttendanceHistoryAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(AttendanceModel model, int position) {
                        if (getActivity() != null
                                && getActivity() instanceof AdminDashboardActivity) {
                            if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("PersonModel", mPersonModel);
                                bundle.putString("SelectedDate", model.getPunchDate());
                                ((AdminDashboardActivity) getActivity()).loadFragment(new AdminMarkAttendanceFragment(), bundle);

                                alertDialog.dismiss();
                            } else {
                                checkDayIsValidForAttendance(alertDialog, model.getPunchDate());
                            }
                        }
                    }
                });
        rv.setAdapter(mAdapter);
    }

    /**
     * Here we check whether current punch date
     * is public holiday or non working day
     *
     * @param alertDialog
     * @param selectedDate
     */
    private void checkDayIsValidForAttendance(AlertDialog alertDialog, String selectedDate) {

        if (CommonMethods.isNetworkConnected(requireActivity())) {
            CommonMethods.showProgressDialog(getActivity());
            AttendanceApplication.refPublicHoliday
                    .orderByChild("date").equalTo(selectedDate)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            CommonMethods.cancelProgressDialog();
                            if (dataSnapshot.exists()) {
                                //Punch day is public holiday
                                CommonMethods.showAlertDailogueWithOK(getActivity(), AttendanceApplication.getAppContext().getString(R.string.title_alert),
                                        AttendanceApplication.getAppContext().getString(R.string.msg_not_allow_to_add_update_attendance_because_selected_day_is_public_holiday),
                                        AttendanceApplication.getAppContext().getString(R.string.action_ok));
                            } else {

                                /**
                                 * Here we check whether punch date is non working day or not
                                 */
                                ArrayList<ShopTimingModel> mTimeSlotArrayList = mPersonModel.getTimeSlotList();
                                if (mTimeSlotArrayList == null
                                        || mTimeSlotArrayList.size() == 0) {
                                    CommonMethods.showAlertDailogueWithOK(getActivity(),
                                            AttendanceApplication.getAppContext().getString(R.string.alert_title_time_slot_not_found),
                                            AttendanceApplication.getAppContext().getString(R.string.alert_msg_alert_time_slot_not_found),
                                            AttendanceApplication.getAppContext().getString(R.string.action_ok));
                                    return;
                                }

                                //Here we convert punch date into DATE format
                                Date punchDate = null;
                                try {
                                    punchDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).parse(selectedDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                /**
                                 * Here we convert punch date into day name 'MONDAY','SUNDAY' format
                                 */
                                SimpleDateFormat outFormat = new SimpleDateFormat("EEEE", Locale.US);
                                assert punchDate != null;
                                String currentDayName = outFormat.format(punchDate).toUpperCase();
                                /**
                                 * Here we EXTRACT time slot model for particular punch date
                                 */
                                ShopTimingModel timeModel = null;
                                for (ShopTimingModel model :
                                        mTimeSlotArrayList) {
                                    assert model != null;
                                    if (model.getDay().equalsIgnoreCase(currentDayName)) {
                                        timeModel = model;
                                        break;
                                    }
                                }
                                if (timeModel != null
                                        && timeModel.getFromTime() != null
                                        && timeModel.getFromTime().trim().length() > 0
                                        && (timeModel.getToTime() != null
                                        && timeModel.getToTime().trim().length() > 0)) {

                                    /**
                                     * Here we call mark attendance fragment
                                     */
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("PersonModel", mPersonModel);
                                    bundle.putString("SelectedDate", selectedDate);
                                    ((AdminDashboardActivity) requireActivity())
                                            .loadFragment(new AdminMarkAttendanceFragment(), bundle);

                                    if (alertDialog != null
                                            && alertDialog.isShowing())
                                        alertDialog.dismiss();
                                } else {
                                    //Punch day is public holiday
                                    CommonMethods.showAlertDailogueWithOK(getActivity(), AttendanceApplication.getAppContext().getString(R.string.title_alert),
                                            AttendanceApplication.getAppContext().getString(R.string.msg_not_allow_to_add_update_attendance_because_selected_day_is_non_working_day),
                                            AttendanceApplication.getAppContext().getString(R.string.action_ok));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            CommonMethods.cancelProgressDialog();
                            CommonMethods.showAlertDailogueWithOK(getActivity(), AttendanceApplication.getAppContext().getString(R.string.title_alert),
                                    databaseError.getMessage(), AttendanceApplication.getAppContext().getString(R.string.action_ok));
                        }
                    });
        } else {
            CommonMethods.showConnectionAlert(getActivity());
        }
    }


    @Override
    public void onDestroyView() {
        if (mTransactionList != null) {
            mTransactionList.clear();
        }
        super.onDestroyView();
    }
}
