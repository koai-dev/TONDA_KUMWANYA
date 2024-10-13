package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.ShopTimingModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;

public class AdminMarkAttendanceFragment extends Fragment implements View.OnClickListener {

    private TextView mTvDate;
    private TextView mTvSelectPunchInTime, mTvSelectPunchOutTime, mTvTotalHours,mTvDayCountAs;
    private ToggleSwitch mToggleSwitch;
    private String mSelectedDate;
    private String mSelectedMonthYear;
    private final SimpleDateFormat actualTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private final SimpleDateFormat requiredTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
    private LinearLayout mLlPunchOutControls;
    private AttendanceModel mPreviousAttendanceModel = null;
    private PersonModel mPersonModel;
    private String mPreviousSelectedDate="";
    private double mOverTimeInMinutes=0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemList=menu.findItem(R.id.item_list);
        if(itemList!=null)
            itemList.setVisible(false);

        MenuItem itemShare=menu.findItem(R.id.item_share);
        if(itemShare!=null)
            itemShare.setVisible(false);

        MenuItem itemInfo=menu.findItem(R.id.item_info);
        if(itemInfo!=null)
            itemInfo.setVisible(false);

        MenuItem itemShowAttendancePositionOnMap=menu.findItem(R.id.item_show_attendance_position_on_map);
        if(itemShowAttendancePositionOnMap!=null)
            itemShowAttendancePositionOnMap.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_mark_attendance, container, false);
        view.setOnTouchListener((v, event) -> true);

        //https://gist.github.com/ferdy182/d9b3525aa65b5b4c468a
        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorDivider));
        // To run the animation as soon as the view is layout in the view hierarchy we add this
        // listener and remove it
        // as soon as it runs to prevent multiple animations if the view changes bounds
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                int cx = 20;
                int cy = 20;

                // get the hypotenuse so the radius is from one corner to the other
                int radius = (int) Math.hypot(right, bottom);

                Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                reveal.setInterpolator(new DecelerateInterpolator(2f));
                reveal.setDuration(1000);
                reveal.start();
            }
        });


        setToolbar(view);
        init(view);
        return view;
    }

    private void setToolbar(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_title_admin_update_attendance));
        ((AdminDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AdminDashboardActivity) requireActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
        Objects.requireNonNull(((AdminDashboardActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> ((AdminDashboardActivity) requireActivity()).onBackPressed());
    }

    private void init(View view) {

        if (getArguments() != null) {
            mPersonModel = (PersonModel) getArguments().getSerializable("PersonModel");
            mPreviousSelectedDate =  getArguments().getString("SelectedDate");
        }

        TextView tvPersonName = view.findViewById(R.id.tv_person_name);
        TextView tvPersonMobilNo = view.findViewById(R.id.tv_person_mobile_no);
        RelativeLayout rlSelectDate = view.findViewById(R.id.rl_select_date);
        mTvDate = view.findViewById(R.id.tv_date);
        RelativeLayout rlSelectPunchInTime = view.findViewById(R.id.rl_select_punch_in_time);
        mTvSelectPunchInTime = view.findViewById(R.id.tv_select_punch_in_time);

        mLlPunchOutControls = view.findViewById(R.id.ll_punch_out_controls);
        RelativeLayout rlSelectPunchOutTime = view.findViewById(R.id.rl_select_punch_out_time);
        mTvSelectPunchOutTime = view.findViewById(R.id.tv_select_punch_out_time);
        mTvTotalHours = view.findViewById(R.id.tv_total_hours);
        LinearLayout mLlDaySelection = view.findViewById(R.id.ll_day_selection);
        mToggleSwitch = view.findViewById(R.id.toggle_filter);
        mTvDayCountAs = view.findViewById(R.id.tv_day_count_as);


        rlSelectPunchInTime.setOnClickListener(this);
        rlSelectPunchOutTime.setOnClickListener(this);

        if(mPersonModel!=null) {
            tvPersonName.setText(mPersonModel.getName());
            tvPersonMobilNo.setText(mPersonModel.getMobileNo());

            /**
             * Here we hide Day/Attendance selection portion
             * when WORK TYPE is HOURS
             */
            if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
                mLlDaySelection.setVisibility(View.GONE);
            } else {
                mLlDaySelection.setVisibility(View.VISIBLE);
            }
        }

        if (mPreviousSelectedDate != null) {
            mTvDate.setText(mPreviousSelectedDate);
            /**
             * Don't change this.
             * If user come for update attendance for particular day then user is not able to change
             * date.If we allows to give rights to change date then there is a case
             * for duplicate attendance entry for same date
             */
            rlSelectDate.setOnClickListener(null);

            mSelectedDate = mPreviousSelectedDate;

            SimpleDateFormat fullDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US);
            SimpleDateFormat monthYear = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US);

            Date previousSelectedDate = null;
            try {
                previousSelectedDate = fullDate.parse(mPreviousSelectedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (previousSelectedDate != null) {
                mSelectedMonthYear = monthYear.format(previousSelectedDate);
            }
        } else {
            String currentDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
            String monthYear = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
            mTvDate.setText(currentDate);
            mSelectedDate = currentDate;
            mSelectedMonthYear = monthYear;
            rlSelectDate.setOnClickListener(this);
        }

        mToggleSwitch.setOnToggleSwitchChangeListener((position, isChecked) -> {
            switch (position){
                case 0:
                    mTvDayCountAs.setText(getString(R.string.label_absent_day));
                    break;
                case 1:
                    mTvDayCountAs.setText(getString(R.string.label_half_days));
                    break;
                case 2:
                    mTvDayCountAs.setText(getString(R.string.label_full_days));
                    break;
            }
        });

        getAttendanceData();
        //getLastWeekAttendanceData();
    }


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_submit, menu);
        menu.findItem(R.id.item_info).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_submit) {
            validateAndMarkAttendance();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.rl_select_date){
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(requireActivity(), (view, year1, month1, dayOfMonth) -> {

                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year1);
                c.set(Calendar.MONTH, month1);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String selectedDate_ = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(c.getTime());
                String selectedMonthYear_ = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US).format(c.getTime());
                mTvDate.setText(selectedDate_);
                mSelectedDate = selectedDate_;
                mSelectedMonthYear = selectedMonthYear_;

                getAttendanceData();
            }, year, month, day);
            dpd.show();
        }else if(v.getId()==R.id.rl_select_punch_in_time){
            Calendar mCurrentInTime = Calendar.getInstance();
            int hour = mCurrentInTime.get(Calendar.HOUR_OF_DAY);
            int minute = mCurrentInTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) -> {

                Date punchInTime = null;
                try {
                    punchInTime = actualTimeFormat.parse(selectedHour + ":" + selectedMinute);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (punchInTime != null) {

                    if (mTvSelectPunchOutTime.getText().toString().trim().length() > 0) {
                        Date selectedPunchOutTimeDate=null;
                        try {
                            selectedPunchOutTimeDate=requiredTimeFormat.parse(mTvSelectPunchOutTime.getText().toString().trim());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if(punchInTime.compareTo(selectedPunchOutTimeDate)<0) {
                            mTvSelectPunchInTime.setText(requiredTimeFormat.format(punchInTime));

                            int totalWorkingMinutes = CommonMethods.calculateTotalHours(mTvSelectPunchInTime.getText().toString().trim(),
                                    mTvSelectPunchOutTime.getText().toString().trim());
                            String totalWorkingHours=CommonMethods.get24HoursFromMinutes(totalWorkingMinutes);
                            mTvTotalHours.setText(totalWorkingHours.concat(" ").concat(getString(R.string.label_hours)));

                            calculateAttendanceType();

                        }else{
                            CommonMethods.showAlertDailogueWithOK(getActivity(),getString(R.string.title_alert),
                                    getString(R.string.msg_punch_in_time_always_smaller_then_punch_out_time),getString(R.string.action_ok));
                        }
                    }else {
                        mTvSelectPunchInTime.setText(requiredTimeFormat.format(punchInTime));
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.msg_time_parsing_issue), Toast.LENGTH_SHORT).show();
                }

            }, hour, minute, false);//Yes 24 hour time
            mTimePicker.show();
        }else if(v.getId()==R.id.rl_select_punch_out_time){
            if (mTvSelectPunchInTime.getText().toString().trim().length() > 0) {
                Calendar mCurrentOutTime = Calendar.getInstance();
                int hour_ = mCurrentOutTime.get(Calendar.HOUR_OF_DAY);
                int minute_ = mCurrentOutTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker_;
                mTimePicker_ = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) -> {
                    Date selectedPunchOutTimeDate = null;
                    try {
                        selectedPunchOutTimeDate = actualTimeFormat.parse(String.valueOf(selectedHour).concat(":").concat(String.valueOf(selectedMinute)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (selectedPunchOutTimeDate != null) {

                        Date selectedPunchInTimeDate=null;
                        try {
                            selectedPunchInTimeDate=requiredTimeFormat.parse(mTvSelectPunchInTime.getText().toString().trim());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if(selectedPunchOutTimeDate.compareTo(selectedPunchInTimeDate)>0) {
                            mTvSelectPunchOutTime.setText(requiredTimeFormat.format(selectedPunchOutTimeDate));
                            int totalWorkingMinutes = CommonMethods.calculateTotalHours(mTvSelectPunchInTime.getText().toString().trim(),
                                    mTvSelectPunchOutTime.getText().toString().trim());
                            String totalWorkingHours=CommonMethods.get24HoursFromMinutes(totalWorkingMinutes);
                            mTvTotalHours.setText(totalWorkingHours.concat(" ").concat(getString(R.string.label_hours)));

                            calculateAttendanceType();
                        }else{
                            CommonMethods.showAlertDailogueWithOK(getActivity(),getString(R.string.title_alert),
                                    getString(R.string.msg_punch_out_time_always_greater_then_punch_in_time),getString(R.string.action_ok));
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.msg_time_parsing_issue), Toast.LENGTH_SHORT).show();
                    }
                }, hour_, minute_, false);//Yes 24 hour time
                mTimePicker_.show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.msg_select_punch_in_time_first), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void calculateAttendanceType() {
        mOverTimeInMinutes=0;
        if(mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)){
            mToggleSwitch.setCheckedTogglePosition(2);
        }else{
            ArrayList<ShopTimingModel> mTimeSlotArrayList = mPersonModel.getTimeSlotList();
            if (mTimeSlotArrayList.size() > 0) {
                //Here we convert punch date into DATE format
                Date punchDate = null;
                try {
                    punchDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).parse(mSelectedDate);
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
                 * Here we EXTRACT time slot model for particular current date/punch date
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
                if (timeModel != null) {

                    double[] calculatedDay = CommonMethods.getDayTypeForMonthWise(timeModel,mTvSelectPunchInTime.getText().toString().trim(),
                            mTvSelectPunchOutTime.getText().toString().trim());
                    if (calculatedDay[0] == 1) {
                        mToggleSwitch.setCheckedTogglePosition(2);
                        mOverTimeInMinutes=calculatedDay[1];
                    } else if (calculatedDay[0] == 0.5) {
                        mToggleSwitch.setCheckedTogglePosition(1);
                    } else if (calculatedDay[0] == 0) {
                        mToggleSwitch.setCheckedTogglePosition(0);
                    }
                }
            }
        }
    }


    private void getAttendanceData() {
        mPreviousAttendanceModel = null;
        mTvSelectPunchInTime.setHint("In Time");
        mTvSelectPunchInTime.setText("");
        showOrHidePunchOutControls(false);
        if (!mSelectedDate.isEmpty()) {

            /**
             * Here we get attendance data for selected date
             */
            AttendanceApplication.refCompanyUserAttendanceDetails
                    .child(mPersonModel.getFirebaseKey())
                    .child(mSelectedMonthYear)
                    .orderByChild("punchDate").equalTo(mSelectedDate)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            CommonMethods.cancelProgressDialog();
                            if (dataSnapshot.exists()) {

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    mPreviousAttendanceModel = ds.getValue(AttendanceModel.class);
                                    assert mPreviousAttendanceModel != null;
                                    mPreviousAttendanceModel.setFirebaseKey(ds.getKey());

                                    if (mPreviousAttendanceModel.getPunchInTime() != null
                                            && mPreviousAttendanceModel.getPunchOutTime() != null) {
                                        showOrHidePunchOutControls(true);

                                        mTvSelectPunchInTime.setText(mPreviousAttendanceModel.getPunchInTime());
                                        mTvSelectPunchOutTime.setText(mPreviousAttendanceModel.getPunchOutTime());

                                        int totalWorkingMinutes = CommonMethods.calculateTotalHours(mPreviousAttendanceModel.getPunchInTime(),
                                                mPreviousAttendanceModel.getPunchOutTime());
                                        String totalWorkingHours=CommonMethods.get24HoursFromMinutes(totalWorkingMinutes);
                                        mTvTotalHours.setText(totalWorkingHours.concat(" ").concat(getString(R.string.label_hours)));

                                        if (mPreviousAttendanceModel.getPresentDay() == 1) {
                                            mToggleSwitch.setCheckedTogglePosition(2);
                                        } else if (mPreviousAttendanceModel.getPresentDay() == 0.5) {
                                            mToggleSwitch.setCheckedTogglePosition(1);
                                        } else if (mPreviousAttendanceModel.getPresentDay() == 0) {
                                            mToggleSwitch.setCheckedTogglePosition(0);
                                        } else {
                                            mToggleSwitch.setCheckedTogglePosition(0);
                                        }
                                    } else if (mPreviousAttendanceModel.getPunchInTime() != null) {
                                        showOrHidePunchOutControls(true);
                                        mTvSelectPunchInTime.setText(mPreviousAttendanceModel.getPunchInTime());
                                    } else {
                                        showOrHidePunchOutControls(false);
                                    }
                                    break;
                                }
                            }else{
                                //Means admin wants to add attendance for fully absent day
                                showOrHidePunchOutControls(true);
                                mToggleSwitch.setCheckedTogglePosition(0);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            CommonMethods.cancelProgressDialog();
                            CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                    "Issue while fetching attendance : " + databaseError.getMessage(), getString(R.string.action_ok));
                        }
                    });
        }
    }


    /**
     * User to validate details and mark attendance
     */
    private void validateAndMarkAttendance() {
        String strSelectedDate = mTvDate.getText().toString().trim();
        String strSelectedPunchInTime = mTvSelectPunchInTime.getText().toString().trim();
        String strSelectedPunchOutTime = mTvSelectPunchOutTime.getText().toString().trim();

        if (strSelectedDate.isEmpty() || strSelectedPunchInTime.isEmpty()
                || strSelectedPunchOutTime.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.msg_all_fields_required), Toast.LENGTH_SHORT).show();
        } else {
            Date selectedDate = null;
            try {
                selectedDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).parse(strSelectedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            AttendanceModel attendanceModel = new AttendanceModel();
            attendanceModel.setPersonName(mPersonModel.getName());
            attendanceModel.setPersonMobileNo(mPersonModel.getMobileNo());
            attendanceModel.setPersonFirebaseKey(mPersonModel.getFirebaseKey());
            attendanceModel.setPunchDate(strSelectedDate);
            if (selectedDate != null) {
                attendanceModel.setPunchDateInMillis(selectedDate.getTime());
            }
            attendanceModel.setPunchInTime(strSelectedPunchInTime);
            attendanceModel.setPunchOutTime(strSelectedPunchOutTime);
            attendanceModel.setOverTimeInMinutes(mOverTimeInMinutes);
            if (mPreviousAttendanceModel != null) {
                if (mPreviousAttendanceModel.getPunchInLatitude() != 0
                        && mPreviousAttendanceModel.getPunchInLongitude() != 0) {
                    attendanceModel.setPunchInLatitude(mPreviousAttendanceModel.getPunchInLatitude());
                    attendanceModel.setPunchInLongitude(mPreviousAttendanceModel.getPunchInLongitude());
                }

                if (mPreviousAttendanceModel.getPunchOutLatitude() != 0
                        && mPreviousAttendanceModel.getPunchOutLongitude() != 0) {
                    attendanceModel.setPunchOutLatitude(mPreviousAttendanceModel.getPunchOutLatitude());
                    attendanceModel.setPunchOutLongitude(mPreviousAttendanceModel.getPunchOutLongitude());
                }
            }

            switch (mToggleSwitch.getCheckedTogglePosition()) {
                case 0:
                    attendanceModel.setPresentDay(0);
                    break;
                case 1:
                    attendanceModel.setPresentDay(0.5);
                    break;
                case 2:
                    attendanceModel.setPresentDay(1);
                    break;
                default:
                    break;
            }

            int totalWorkingMinutes = CommonMethods.calculateTotalHours(strSelectedPunchInTime,
                    strSelectedPunchOutTime);
            String totalWorkingHours=CommonMethods.get24HoursFromMinutes(totalWorkingMinutes);
            attendanceModel.setTotalWorkingHours(totalWorkingHours);
            attendanceModel.setPunchInBy("ADMIN");
            attendanceModel.setPunchOutBy("ADMIN");

            uploadAttendanceDetails(attendanceModel);
        }
    }

    private void uploadAttendanceDetails(final AttendanceModel attendanceModel) {


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireActivity()
                ,R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(getString(R.string.alert_title_mark_attendance));
        alertDialogBuilder.setMessage(getString(R.string.alert_message_mark_attendance))
                .setPositiveButton(R.string.action_yes, null)
                .setNegativeButton(R.string.action_no, null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(dialog -> {
            Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPositive.setOnClickListener(view -> {
                alertDialog.dismiss();

                if (CommonMethods.isNetworkConnected(requireActivity())) {
                    CommonMethods.showProgressDialog(getActivity());

                    Query query;
                    if (mPreviousAttendanceModel == null) {
                        String attendanceKey = AttendanceApplication.refCompanyUserAttendanceDetails
                                .child(mPersonModel.getFirebaseKey())
                                .child(mSelectedMonthYear).push().getKey();
                        assert attendanceKey != null;
                        query = AttendanceApplication.refCompanyUserAttendanceDetails
                                .child(mPersonModel.getFirebaseKey())
                                .child(mSelectedMonthYear).child(attendanceKey);
                    } else {
                        query = AttendanceApplication.refCompanyUserAttendanceDetails
                                .child(mPersonModel.getFirebaseKey())
                                .child(mSelectedMonthYear).child(mPreviousAttendanceModel.getFirebaseKey());
                    }

                    ((DatabaseReference) query).setValue(attendanceModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    CommonMethods.cancelProgressDialog();
                                    if (mPreviousAttendanceModel == null) {
                                        Toast.makeText(getActivity(), getString(R.string.msg_attendance_added_successfully), Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(getActivity(), getString(R.string.msg_attendance_updated_successfully), Toast.LENGTH_SHORT).show();
                                    }

                                    if (getActivity() != null) {
                                        //0 is Branch List,1 is User List, 2 is Calender View and 3 Mark Attendance means current fragment
                                        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 2) {
                                            FragmentManager.BackStackEntry backEntry = getActivity().getSupportFragmentManager().getBackStackEntryAt(2);
                                            String tag = backEntry.getName();
                                            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(tag);
                                            /**
                                             * Here we refresh attendance history data again
                                             */
                                            if (fragment instanceof UserAttendanceHistoryInCalendarFragment) {
                                                ((UserAttendanceHistoryInCalendarFragment)fragment).getData();
                                            }
                                        }
                                        if (getActivity() instanceof AdminDashboardActivity) {
                                            getActivity().onBackPressed();
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                CommonMethods.cancelProgressDialog();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    CommonMethods.showConnectionAlert(getActivity());
                }
            });

            Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            btnNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    private void showOrHidePunchOutControls(boolean show) {
        mTvSelectPunchOutTime.setHint("Out Time");
        mTvSelectPunchOutTime.setText("");
        mTvTotalHours.setText("");
        mToggleSwitch.setCheckedTogglePosition(0);
        if (show) {
            mLlPunchOutControls.setVisibility(View.VISIBLE);
        } else {
            mLlPunchOutControls.setVisibility(View.GONE);
        }
    }

}