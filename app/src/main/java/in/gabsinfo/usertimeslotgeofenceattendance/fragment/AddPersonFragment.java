package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import static in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData.DEFAULT_FULL_DAY_HOURS;
import static in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData.DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY;
import static in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData.DEFAULT_SHOP_CLOSE_TIME;
import static in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData.DEFAULT_SHOP_OPEN_TIME;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.io.File;
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
import in.gabsinfo.usertimeslotgeofenceattendance.activity.ImagePickerActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.LocationModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.ShopTimingModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;

public class AddPersonFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private static final int REQUEST_IMAGE = 253;
    private EditText mEtPersonName, mEtPersonAddress, mEtPersonMobileNo, mEtPersonDesignation;
    private CheckBox mCbNotifyForPunchIn,mCbNotifyForPunchOut;
    private EditText mEtPersonDOB, mEtPersonEmailId, mEtPersonPassword;
    private EditText mEtAmount;
    private TextView mTvAmountSuffix;
    private EditText mEtCurrencySymbol;
    private RadioButton mRbWorkTypeDayWise, mRbWorkTypeMonthWise;
    private PersonModel mPersonModel;
    private ImageView mIvUserProfile;
    private Uri imageCaptureUri=null;
    private SimpleDateFormat dateFormatter;
    private static final String DATE_FORMAT = "ddMMMyyyy_HHmmssddMMMyyyy_HHmmss";
    private CountryCodePicker mCcp;
    private SwitchCompat mSwitchTrackEmployee;
    private TextView mTvShopTimeForMondayFrom,mTvShopTimeForTuesdayFrom,mTvShopTimeForWednesdayFrom,mTvShopTimeForThursdayFrom;
    private TextView mTvShopTimeForFridayFrom,mTvShopTimeForSaturdayFrom,mTvShopTimeForSundayFrom;
    private TextView mTvShopTimeForMondayTo,mTvShopTimeForTuesdayTo,mTvShopTimeForWednesdayTo,mTvShopTimeForThursdayTo;
    private TextView mTvShopTimeForFridayTo,mTvShopTimeForSaturdayTo,mTvShopTimeForSundayTo;
    private TextView mTvFullDayHoursFoMonday,mTvFullDayHoursFoTuesday,mTvFullDayHoursFoWednesday,mTvFullDayHoursFoThursday;
    private TextView mTvFullDayHoursFoFriday,mTvFullDayHoursFoSaturday,mTvFullDayHoursFoSunday;
    private CheckBox mCbHalfDayAllowForMonday,mCbHalfDayAllowForTuesday,mCbHalfDayAllowForWednesday,mCbHalfDayAllowForThursday;
    private CheckBox mCbHalfDayAllowForFriday,mCbHalfDayAllowForSaturday,mCbHalfDayAllowForSunday;
    private TextView mTvHalfDayHoursFoMonday,mTvHalfDayHoursFoTuesday,mTvHalfDayHoursFoWednesday,mTvHalfDayHoursFoThursday;
    private TextView mTvHalfDayHoursFoFriday,mTvHalfDayHoursFoSaturday,mTvHalfDayHoursFoSunday;
    private CheckBox mCbMonday,mCbTuesday,mCbWednesday,mCbThursday,mCbFriday,mCbSaturday,mCbSunday;

    private SimpleDateFormat twentyFourHoursFormat = new SimpleDateFormat(ConstantData.TWENTY_FOUR_HOURS_FORMAT, Locale.US);
    private SimpleDateFormat twelveHoursFormat = new SimpleDateFormat(ConstantData.TWELVE_HOURS_FORMAT, Locale.US);
    private int SHOP_TIME_SELECTION_POSITION_DAY_WISE = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.item_logout);
        item.setVisible(false);

        MenuItem itemSearch=menu.findItem(R.id.action_search);
        if(itemSearch!=null) {
            itemSearch.setVisible(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_person, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
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
        toolbar.setTitle(getString(R.string.toolbar_title_add_person));
        ((AdminDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AdminDashboardActivity) requireActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
        Objects.requireNonNull(((AdminDashboardActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AdminDashboardActivity) requireActivity()).onBackPressed();
            }
        });
    }


    private void init(View view) {

        dateFormatter = new SimpleDateFormat(
                DATE_FORMAT, Locale.US);

        mIvUserProfile = view.findViewById(R.id.iv_user_profile_image);
        mEtPersonName = view.findViewById(R.id.et_person_name);
        mEtPersonAddress = view.findViewById(R.id.et_person_address);
        TextView tvPersonDOBTemp = view.findViewById(R.id.tv_person_dob_temp);
        mEtPersonDOB = view.findViewById(R.id.et_person_dob);
        mCcp = view.findViewById(R.id.ccp);
        mEtPersonMobileNo = view.findViewById(R.id.et_person_mobile_no);
        mEtPersonEmailId = view.findViewById(R.id.et_person_email_id);
        mEtPersonPassword = view.findViewById(R.id.et_person_password);
        mEtPersonDesignation = view.findViewById(R.id.et_person_designation);
        mCbNotifyForPunchIn = view.findViewById(R.id.cb_notifiy_for_punch_in);
        mCbNotifyForPunchOut = view.findViewById(R.id.cb_notifiy_for_punch_out);


        RadioGroup mRgWorkTypeSelection = view.findViewById(R.id.rg_work_Type_selection);

        mRbWorkTypeDayWise = view.findViewById(R.id.rb_work_type_day_wise);
        mRbWorkTypeMonthWise = view.findViewById(R.id.rb_work_type_month_wise);
        mEtAmount = view.findViewById(R.id.et_amount);
        mTvAmountSuffix = view.findViewById(R.id.tv_amount_suffix);

        mSwitchTrackEmployee = view.findViewById(R.id.switch_track_employee);

        mCbMonday = view.findViewById(R.id.chk_monday);
        mCbTuesday = view.findViewById(R.id.chk_tuesday);
        mCbWednesday = view.findViewById(R.id.chk_wednesday);
        mCbThursday = view.findViewById(R.id.chk_thuresday);
        mCbFriday = view.findViewById(R.id.chk_friday);
        mCbSaturday = view.findViewById(R.id.chk_saturday);
        mCbSunday = view.findViewById(R.id.chk_sunday);

        LinearLayout llSelectShopTimeForMonday = view.findViewById(R.id.ll_select_shop_time_for_monday);
        LinearLayout llSelectShopTimeForTuesday = view.findViewById(R.id.ll_select_shop_time_for_tuesday);
        LinearLayout llSelectShopTimeForWednesday = view.findViewById(R.id.ll_select_shop_time_for_wednesday);
        LinearLayout llSelectShopTimeForThursday = view.findViewById(R.id.ll_select_shop_time_for_thuresday);
        LinearLayout llSelectShopTimeForFriday = view.findViewById(R.id.ll_select_shop_time_for_friday);
        LinearLayout llSelectShopTimeForSaturday = view.findViewById(R.id.ll_select_shop_time_for_saturday);
        LinearLayout llSelectShopTimeForSunday = view.findViewById(R.id.ll_select_shop_time_for_sunday);

        mTvShopTimeForMondayFrom = view.findViewById(R.id.tv_shop_time_from_monday);
        mTvShopTimeForTuesdayFrom = view.findViewById(R.id.tv_shop_time_from_tuesday);
        mTvShopTimeForWednesdayFrom = view.findViewById(R.id.tv_shop_time_from_wednesday);
        mTvShopTimeForThursdayFrom = view.findViewById(R.id.tv_shop_time_from_thuresday);
        mTvShopTimeForFridayFrom = view.findViewById(R.id.tv_shop_time_from_friday);
        mTvShopTimeForSaturdayFrom = view.findViewById(R.id.tv_shop_time_from_saturday);
        mTvShopTimeForSundayFrom = view.findViewById(R.id.tv_shop_time_from_sunday);

        mTvShopTimeForMondayTo = view.findViewById(R.id.tv_shop_time_to_monday);
        mTvShopTimeForTuesdayTo = view.findViewById(R.id.tv_shop_time_to_tuesday);
        mTvShopTimeForWednesdayTo = view.findViewById(R.id.tv_shop_time_to_wednesday);
        mTvShopTimeForThursdayTo = view.findViewById(R.id.tv_shop_time_to_thuresday);
        mTvShopTimeForFridayTo = view.findViewById(R.id.tv_shop_time_to_friday);
        mTvShopTimeForSaturdayTo = view.findViewById(R.id.tv_shop_time_to_saturday);
        mTvShopTimeForSundayTo = view.findViewById(R.id.tv_shop_time_to_sunday);

        mTvFullDayHoursFoMonday = view.findViewById(R.id.tv_full_day_hours_for_monday);
        mTvFullDayHoursFoTuesday = view.findViewById(R.id.tv_full_day_hours_for_tuesday);
        mTvFullDayHoursFoWednesday = view.findViewById(R.id.tv_full_day_hours_for_wednesday);
        mTvFullDayHoursFoThursday = view.findViewById(R.id.tv_full_day_hours_for_thuresday);
        mTvFullDayHoursFoFriday = view.findViewById(R.id.tv_full_day_hours_for_friday);
        mTvFullDayHoursFoSaturday = view.findViewById(R.id.tv_full_day_hours_for_saturday);
        mTvFullDayHoursFoSunday = view.findViewById(R.id.tv_full_day_hours_for_sunday);

        mCbHalfDayAllowForMonday = view.findViewById(R.id.chk_half_day_allow_for_monday);
        mCbHalfDayAllowForTuesday = view.findViewById(R.id.chk_half_day_allow_for_tuesday);
        mCbHalfDayAllowForWednesday = view.findViewById(R.id.chk_half_day_allow_for_wednesday);
        mCbHalfDayAllowForThursday = view.findViewById(R.id.chk_half_day_allow_for_thuresday);
        mCbHalfDayAllowForFriday = view.findViewById(R.id.chk_half_day_allow_for_friday);
        mCbHalfDayAllowForSaturday = view.findViewById(R.id.chk_half_day_allow_for_saturday);
        mCbHalfDayAllowForSunday = view.findViewById(R.id.chk_half_day_allow_for_sunday);

        mTvHalfDayHoursFoMonday = view.findViewById(R.id.tv_half_day_hours_for_monday);
        mTvHalfDayHoursFoTuesday = view.findViewById(R.id.tv_half_day_hours_for_tuesday);
        mTvHalfDayHoursFoWednesday = view.findViewById(R.id.tv_half_day_hours_for_wednesday);
        mTvHalfDayHoursFoThursday = view.findViewById(R.id.tv_half_day_hours_for_thuresday);
        mTvHalfDayHoursFoFriday = view.findViewById(R.id.tv_half_day_hours_for_friday);
        mTvHalfDayHoursFoSaturday = view.findViewById(R.id.tv_half_day_hours_for_saturday);
        mTvHalfDayHoursFoSunday = view.findViewById(R.id.tv_half_day_hours_for_sunday);

        llSelectShopTimeForMonday.setOnClickListener(this);
        llSelectShopTimeForTuesday.setOnClickListener(this);
        llSelectShopTimeForWednesday.setOnClickListener(this);
        llSelectShopTimeForThursday.setOnClickListener(this);
        llSelectShopTimeForFriday.setOnClickListener(this);
        llSelectShopTimeForSaturday.setOnClickListener(this);
        llSelectShopTimeForSunday.setOnClickListener(this);

        mCbHalfDayAllowForMonday.setOnCheckedChangeListener(this);
        mCbHalfDayAllowForTuesday.setOnCheckedChangeListener(this);
        mCbHalfDayAllowForWednesday.setOnCheckedChangeListener(this);
        mCbHalfDayAllowForThursday.setOnCheckedChangeListener(this);
        mCbHalfDayAllowForFriday.setOnCheckedChangeListener(this);
        mCbHalfDayAllowForSaturday.setOnCheckedChangeListener(this);
        mCbHalfDayAllowForSunday.setOnCheckedChangeListener(this);

        mTvHalfDayHoursFoMonday.setOnClickListener(this);
        mTvHalfDayHoursFoTuesday.setOnClickListener(this);
        mTvHalfDayHoursFoWednesday.setOnClickListener(this);
        mTvHalfDayHoursFoThursday.setOnClickListener(this);
        mTvHalfDayHoursFoFriday.setOnClickListener(this);
        mTvHalfDayHoursFoSaturday.setOnClickListener(this);
        mTvHalfDayHoursFoSunday.setOnClickListener(this);

        /**
         * Set default values for Time Slot
         */
        setDefaultValuesForTimeSlot(ConstantData.MONDAY);
        setDefaultValuesForTimeSlot(ConstantData.TUESDAY);
        setDefaultValuesForTimeSlot(ConstantData.WEDNESDAY);
        setDefaultValuesForTimeSlot(ConstantData.THURESDAY);
        setDefaultValuesForTimeSlot(ConstantData.FRIDAY);
        setDefaultValuesForTimeSlot(ConstantData.SATURDAY);
        setDefaultValuesForTimeSlot(ConstantData.SUNDAY);

        mEtCurrencySymbol = view.findViewById(R.id.et_currency_symbol);


        mEtPersonName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        mEtPersonDOB.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        mEtPersonAddress.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        mEtPersonDesignation.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        mRgWorkTypeSelection.setOnCheckedChangeListener(this);
        mRbWorkTypeMonthWise.setChecked(true);
        mIvUserProfile.setOnClickListener(this);
        tvPersonDOBTemp.setOnClickListener(this);

    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            mPersonModel = (PersonModel) getArguments().getSerializable("PersonModel");
        }

        if (mPersonModel != null) {

            if (mPersonModel.getProfileImage() != null
                    && mPersonModel.getProfileImage().trim().length() > 0) {
                loadProfile(mPersonModel.getProfileImage());
            }

            mEtPersonName.setText(mPersonModel.getName());
            mEtPersonAddress.setText(mPersonModel.getAddress());
            mCcp.setCountryForNameCode(mPersonModel.getCountryCode());
            mEtPersonMobileNo.setText(mPersonModel.getMobileNo());
            mEtPersonDesignation.setText(mPersonModel.getDesignation());
            mEtPersonEmailId.setText(mPersonModel.getEmailId());
            mEtPersonPassword.setText(mPersonModel.getPassword());
            mEtPersonDOB.setText(mPersonModel.getDob());
            mCbNotifyForPunchIn.setChecked(mPersonModel.isNotifyAdminForPunchIn());
            mCbNotifyForPunchOut.setChecked(mPersonModel.isNotifyAdminForPunchOut());

            mEtAmount.setText(String.valueOf(mPersonModel.getAmount()));
            if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_DAY_WISE)) {
                mRbWorkTypeDayWise.setChecked(true);
            } else if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_MONTH_WISE)) {
                mRbWorkTypeMonthWise.setChecked(true);
            }
            bindTimeSlotDataForMonthWise(mPersonModel.getTimeSlotList());

            mEtCurrencySymbol.setText(mPersonModel.getCurrencySymbol());
            mSwitchTrackEmployee.setChecked(mPersonModel.isTrackingEnable());

        }
        mCbMonday.setOnCheckedChangeListener(this);
        mCbTuesday.setOnCheckedChangeListener(this);
        mCbWednesday.setOnCheckedChangeListener(this);
        mCbThursday.setOnCheckedChangeListener(this);
        mCbFriday.setOnCheckedChangeListener(this);
        mCbSaturday.setOnCheckedChangeListener(this);
        mCbSunday.setOnCheckedChangeListener(this);
    }



    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_submit, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_submit) {
            validateAndSubmit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.iv_user_profile_image){
            onProfileImageClick();
        }else if(v.getId()==R.id.tv_person_dob_temp){
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(requireActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    mEtPersonDOB.setText(new SimpleDateFormat
                            (ConstantData.DATE_FORMAT, Locale.US).format(c.getTime()));

                }
            }, year, month, day);
            dpd.show();
        }else if(v.getId()==R.id.ll_select_shop_time_for_monday){
            if (mCbMonday.isChecked()) {
                SHOP_TIME_SELECTION_POSITION_DAY_WISE = 1;
                openTimePicker();
            } else {
                Toast.makeText(getActivity(), getString(R.string.msg_enable_checkbox_first), Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId()==R.id.ll_select_shop_time_for_tuesday){
            if (mCbTuesday.isChecked()) {
                SHOP_TIME_SELECTION_POSITION_DAY_WISE = 2;
                openTimePicker();
            } else {
                Toast.makeText(getActivity(), getString(R.string.msg_enable_checkbox_first), Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId()==R.id.ll_select_shop_time_for_wednesday){
            if (mCbWednesday.isChecked()) {
                SHOP_TIME_SELECTION_POSITION_DAY_WISE = 3;
                openTimePicker();
            } else {
                Toast.makeText(getActivity(), getString(R.string.msg_enable_checkbox_first), Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId()==R.id.ll_select_shop_time_for_thuresday){
            if (mCbThursday.isChecked()) {
                SHOP_TIME_SELECTION_POSITION_DAY_WISE = 4;
                openTimePicker();
            } else {
                Toast.makeText(getActivity(), getString(R.string.msg_enable_checkbox_first), Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId()==R.id.ll_select_shop_time_for_friday){
            if (mCbFriday.isChecked()) {
                SHOP_TIME_SELECTION_POSITION_DAY_WISE = 5;
                openTimePicker();
            } else {
                Toast.makeText(getActivity(), getString(R.string.msg_enable_checkbox_first), Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId()==R.id.ll_select_shop_time_for_saturday){
            if (mCbSaturday.isChecked()) {
                SHOP_TIME_SELECTION_POSITION_DAY_WISE = 6;
                openTimePicker();
            } else {
                Toast.makeText(getActivity(), getString(R.string.msg_enable_checkbox_first), Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId()==R.id.ll_select_shop_time_for_sunday){
            if (mCbSunday.isChecked()) {
                SHOP_TIME_SELECTION_POSITION_DAY_WISE = 7;
                openTimePicker();
            } else {
                Toast.makeText(getActivity(), getString(R.string.msg_enable_checkbox_first), Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId()==R.id.tv_half_day_hours_for_monday){
            SHOP_TIME_SELECTION_POSITION_DAY_WISE = 1;
            selectTimeInHours_(mTvHalfDayHoursFoMonday);
        }else if(v.getId()==R.id.tv_half_day_hours_for_tuesday){
            SHOP_TIME_SELECTION_POSITION_DAY_WISE = 2;
            selectTimeInHours_(mTvHalfDayHoursFoTuesday);
        }else if(v.getId()==R.id.tv_half_day_hours_for_wednesday){
            SHOP_TIME_SELECTION_POSITION_DAY_WISE = 3;
            selectTimeInHours_(mTvHalfDayHoursFoWednesday);
        }else if(v.getId()==R.id.tv_half_day_hours_for_thuresday){
            SHOP_TIME_SELECTION_POSITION_DAY_WISE = 4;
            selectTimeInHours_(mTvHalfDayHoursFoThursday);
        }else if(v.getId()==R.id.tv_half_day_hours_for_friday){
            SHOP_TIME_SELECTION_POSITION_DAY_WISE = 5;
            selectTimeInHours_(mTvHalfDayHoursFoFriday);
        }else if(v.getId()==R.id.tv_half_day_hours_for_saturday){
            SHOP_TIME_SELECTION_POSITION_DAY_WISE = 6;
            selectTimeInHours_(mTvHalfDayHoursFoSaturday);
        }else if(v.getId()==R.id.tv_half_day_hours_for_sunday){
            SHOP_TIME_SELECTION_POSITION_DAY_WISE = 7;
            selectTimeInHours_(mTvHalfDayHoursFoSunday);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId==R.id.rb_work_type_day_wise){
            mTvAmountSuffix.setText(getString(R.string.label_amount_suffix_per_day));
        }else if(checkedId==R.id.rb_work_type_month_wise){
            mTvAmountSuffix.setText(getString(R.string.label_amount_suffix_per_month));
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == REQUEST_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getParcelableExtra("path");
                    try {

                        /*File myFile = new File(uri.getPath());
                        long sizeInKB = myFile.length() / 1024; // Size in KB
                        Log.e("KB Size",String.valueOf(sizeInKB));
                        if (sizeInKB > 100) {
                            Toast.makeText(getActivity(), getString(R.string.msg_capture_shop_owner_image_proper), Toast.LENGTH_SHORT).show();
                            return;
                        }*/

                        imageCaptureUri=uri;
                        // loading profile image from local cache
                        loadProfile(uri.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private void validateAndSubmit() {
        String strPersonName = mEtPersonName.getText().toString().trim();
        String strPersonAddress = mEtPersonAddress.getText().toString().trim();
        String strPersonMobileNo = mEtPersonMobileNo.getText().toString().trim();
        String strPersonEmailId = mEtPersonEmailId.getText().toString().trim();
        String strPersonPassword = mEtPersonPassword.getText().toString().trim();
        String strPersonDOB = mEtPersonDOB.getText().toString().trim();
        String strPersonDesignation = mEtPersonDesignation.getText().toString().trim();

        String strAmount = mEtAmount.getText().toString().trim();
        String strCurrencySymbol = mEtCurrencySymbol.getText().toString().trim();

        if (strPersonName.isEmpty() || strPersonAddress.isEmpty() || strPersonMobileNo.isEmpty() || strPersonDesignation.isEmpty()
                || strPersonEmailId.isEmpty() || strPersonPassword.isEmpty() || strPersonDOB.isEmpty()
                || strAmount.isEmpty()
                || strCurrencySymbol.trim().length() == 0) {
            Toast.makeText(getActivity(), getString(R.string.msg_all_fields_required), Toast.LENGTH_LONG).show();
        } else if (strPersonAddress.length() < 15) {
            Toast.makeText(getActivity(), getString(R.string.msg_enter_proper_address), Toast.LENGTH_LONG).show();
        } else if (strPersonMobileNo.length() < 10) {
            Toast.makeText(getActivity(), getString(R.string.msg_enter_valid_mobile_no), Toast.LENGTH_LONG).show();
        } else if (!CommonMethods.isValidEmail(strPersonEmailId)) {
            Toast.makeText(getActivity(), getString(R.string.msg_enter_valid_email_id), Toast.LENGTH_LONG).show();
        } /*else if ((mPersonModel == null
                && imageCaptureUri == null)
                || (mPersonModel != null
                && (mPersonModel.getProfileImage() == null
                || mPersonModel.getProfileImage().trim().length() == 0)
                && imageCaptureUri == null)) {
            Toast.makeText(getActivity(), getString(R.string.msg_capture_employee_photo), Toast.LENGTH_LONG).show();
        }*/else if (strPersonMobileNo.contains("+")) {
            Toast.makeText(getActivity(), getString(R.string.msg_remove_mobile_dialer_code), Toast.LENGTH_LONG).show();
            mEtPersonMobileNo.requestFocus();
        }  else {

            //Here we check whether time slot value is selected or not
            if (!mCbMonday.isChecked()
                    && !mCbTuesday.isChecked()
                    && !mCbWednesday.isChecked()
                    && !mCbThursday.isChecked()
                    && !mCbFriday.isChecked()
                    && !mCbSaturday.isChecked()
                    && !mCbSunday.isChecked()) {
                Toast.makeText(getActivity(), getString(R.string.msg_set_time_slot_value), Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<ShopTimingModel> timeSlotList = new ArrayList<>();
            boolean foundError=false;
            String errorMessage="";
            /**
             * Here we check whether half day hours are set or not
             * in the case when half day checkbox is checked and
             * master checkbox is checked
             */
            if (mCbMonday.isChecked()
                    && mCbHalfDayAllowForMonday.isChecked()
                    && mTvHalfDayHoursFoMonday.getText().toString().trim().length() == 0) {
                foundError = true;
                errorMessage = String.format(getString(R.string.msg_select_half_day_hours), getString(R.string.label_monday));
            } else {
                timeSlotList.add(new ShopTimingModel(ConstantData.MONDAY,
                        !mTvShopTimeForMondayFrom.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForMondayFrom.getText().toString().trim() : "",
                        !mTvShopTimeForMondayTo.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForMondayTo.getText().toString().trim() : "",
                        !mTvFullDayHoursFoMonday.getText().toString().trim().equalsIgnoreCase(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY) ? mTvFullDayHoursFoMonday.getText().toString().trim() : "",
                        mCbHalfDayAllowForMonday.isChecked(),
                        mTvHalfDayHoursFoMonday.getText().toString().trim()));
            }

            if (!foundError
                    && mCbTuesday.isChecked()
                    && mCbHalfDayAllowForTuesday.isChecked()
                    && mTvHalfDayHoursFoTuesday.getText().toString().trim().length() == 0){
                foundError = true;
                errorMessage = String.format(getString(R.string.msg_select_half_day_hours), getString(R.string.label_tuesday));
            }else{
                timeSlotList.add(new ShopTimingModel(ConstantData.TUESDAY,
                        !mTvShopTimeForTuesdayFrom.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForTuesdayFrom.getText().toString().trim() : "",
                        !mTvShopTimeForTuesdayTo.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForTuesdayTo.getText().toString().trim() : "",
                        !mTvFullDayHoursFoTuesday.getText().toString().trim().equalsIgnoreCase(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY) ? mTvFullDayHoursFoTuesday.getText().toString().trim() : "",
                        mCbHalfDayAllowForTuesday.isChecked(),
                        mTvHalfDayHoursFoTuesday.getText().toString().trim()));
            }


            if (!foundError
                    && mCbWednesday.isChecked()
                    && mCbHalfDayAllowForWednesday.isChecked()
                    && mTvHalfDayHoursFoWednesday.getText().toString().trim().length() == 0){
                foundError = true;
                errorMessage = String.format(getString(R.string.msg_select_half_day_hours), getString(R.string.label_wednesday));
            }else {
                timeSlotList.add(new ShopTimingModel(ConstantData.WEDNESDAY,
                        !mTvShopTimeForWednesdayFrom.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForWednesdayFrom.getText().toString().trim() : "",
                        !mTvShopTimeForWednesdayTo.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForWednesdayTo.getText().toString().trim() : "",
                        !mTvFullDayHoursFoWednesday.getText().toString().trim().equalsIgnoreCase(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY) ? mTvFullDayHoursFoWednesday.getText().toString().trim() : "",
                        mCbHalfDayAllowForWednesday.isChecked(),
                        mTvHalfDayHoursFoWednesday.getText().toString().trim()));
            }

            if (!foundError
                    && mCbThursday.isChecked()
                    && mCbHalfDayAllowForThursday.isChecked()
                    && mTvHalfDayHoursFoThursday.getText().toString().trim().length() == 0){
                foundError = true;
                errorMessage = String.format(getString(R.string.msg_select_half_day_hours), getString(R.string.label_thuresady));
            }else {
                timeSlotList.add(new ShopTimingModel(ConstantData.THURESDAY,
                        !mTvShopTimeForThursdayFrom.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForThursdayFrom.getText().toString().trim() : "",
                        !mTvShopTimeForThursdayTo.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForThursdayTo.getText().toString().trim() : "",
                        !mTvFullDayHoursFoThursday.getText().toString().trim().equalsIgnoreCase(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY) ? mTvFullDayHoursFoThursday.getText().toString().trim() : "",
                        mCbHalfDayAllowForThursday.isChecked(),
                        mTvHalfDayHoursFoThursday.getText().toString().trim()));
            }

            if (!foundError
                    && mCbFriday.isChecked()
                    && mCbHalfDayAllowForFriday.isChecked()
                    && mTvHalfDayHoursFoFriday.getText().toString().trim().length() == 0){
                foundError = true;
                errorMessage = String.format(getString(R.string.msg_select_half_day_hours), getString(R.string.label_friday));

            }else {
                timeSlotList.add(new ShopTimingModel(ConstantData.FRIDAY,
                        !mTvShopTimeForFridayFrom.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForFridayFrom.getText().toString().trim() : "",
                        !mTvShopTimeForFridayTo.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForFridayTo.getText().toString().trim() : "",
                        !mTvFullDayHoursFoFriday.getText().toString().trim().equalsIgnoreCase(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY) ? mTvFullDayHoursFoFriday.getText().toString().trim() : "",
                        mCbHalfDayAllowForFriday.isChecked(),
                        mTvHalfDayHoursFoFriday.getText().toString().trim()));
            }

            if (!foundError
                    && mCbSaturday.isChecked()
                    && mCbHalfDayAllowForSaturday.isChecked()
                    && mTvHalfDayHoursFoSaturday.getText().toString().trim().length() == 0){
                foundError = true;
                errorMessage = String.format(getString(R.string.msg_select_half_day_hours), getString(R.string.label_saturday));
            }else {
                timeSlotList.add(new ShopTimingModel(ConstantData.SATURDAY,
                        !mTvShopTimeForSaturdayFrom.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForSaturdayFrom.getText().toString().trim() : "",
                        !mTvShopTimeForSaturdayTo.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForSaturdayTo.getText().toString().trim() : "",
                        !mTvFullDayHoursFoSaturday.getText().toString().trim().equalsIgnoreCase(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY) ? mTvFullDayHoursFoSaturday.getText().toString().trim() : "",
                        mCbHalfDayAllowForSaturday.isChecked(),
                        mTvHalfDayHoursFoSaturday.getText().toString().trim()));
            }

            if (!foundError
                    && mCbSunday.isChecked()
                    && mCbHalfDayAllowForSunday.isChecked()
                    && mTvHalfDayHoursFoSunday.getText().toString().trim().length() == 0){
                foundError = true;
                errorMessage = String.format(getString(R.string.msg_select_half_day_hours), getString(R.string.label_sunday));
            }else {
                timeSlotList.add(new ShopTimingModel(ConstantData.SUNDAY,
                        !mTvShopTimeForSundayFrom.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForSundayFrom.getText().toString().trim() : "",
                        !mTvShopTimeForSundayTo.getText().toString().trim().equalsIgnoreCase(getString(R.string.label_set)) ? mTvShopTimeForSundayTo.getText().toString().trim() : "",
                        !mTvFullDayHoursFoSunday.getText().toString().trim().equalsIgnoreCase(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY) ? mTvFullDayHoursFoSunday.getText().toString().trim() : "",
                        mCbHalfDayAllowForSunday.isChecked(),
                        mTvHalfDayHoursFoSunday.getText().toString().trim()));
            }

            if(foundError){
                CommonMethods.showAlertDailogueWithOK(getActivity(),getString(R.string.title_alert),
                        errorMessage,getString(R.string.action_ok));
                return;
            }

            String title="";
            String message="";
            if (mPersonModel != null) {
                title=getString(R.string.alert_title_person_update);
                message=getString(R.string.alert_msg_person_update);
            }else{
                title=getString(R.string.alert_title_person_add);
                message=getString(R.string.alert_msg_person_add);
            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireActivity(),
                    R.style.CustomDialogTheme);
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message)
                    .setPositiveButton(getString(R.string.action_yes), null)
                    .setNegativeButton(getString(R.string.action_no), null);

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    btnPositive.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            if (Integer.valueOf(strAmount) == 0) {
                                Toast.makeText(getActivity(), getString(R.string.msg_enter_valid_amount), Toast.LENGTH_SHORT).show();
                            }else{
                                PersonModel model = new PersonModel();
                                if(mPersonModel!=null)
                                    model.setFirebaseKey(mPersonModel.getFirebaseKey());//Used specially for identify user for tracking task
                                model.setName(strPersonName);
                                model.setAddress(strPersonAddress);
                                model.setMobileNo(strPersonMobileNo);
                                model.setMobileDialerCode(mCcp.getSelectedCountryCodeWithPlus());
                                model.setCountryCode(mCcp.getSelectedCountryNameCode());
                                model.setCountryName(mCcp.getSelectedCountryName());
                                model.setEmailId(strPersonEmailId);
                                model.setPassword(strPersonPassword);
                                model.setDob(strPersonDOB);
                                model.setDesignation(strPersonDesignation);
                                model.setNotifyAdminForPunchIn(mCbNotifyForPunchIn.isChecked());
                                model.setNotifyAdminForPunchOut(mCbNotifyForPunchOut.isChecked());
                                model.setUserType(ConstantData.TYPE_USER);
                                model.setCurrencySymbol(strCurrencySymbol);
                                model.setUserType_mobileNo(ConstantData.TYPE_USER + "-" + strPersonMobileNo);
                                model.setUserType_emailId(ConstantData.TYPE_USER + "-" + strPersonEmailId);
                                model.setAmount(Integer.valueOf(strAmount));
                                model.setTimeSlotList(timeSlotList);
                                model.setTrackingEnable(mSwitchTrackEmployee.isChecked());

                                if (mRbWorkTypeDayWise.isChecked()) {
                                    model.setWorkType(ConstantData.WORK_TYPE_DAY_WISE);
                                }else if (mRbWorkTypeMonthWise.isChecked()) {
                                    model.setWorkType(ConstantData.WORK_TYPE_MONTH_WISE);
                                }
                                if (imageCaptureUri != null) {
                                    uploadDetailsToServer(imageCaptureUri, model);
                                } else {
                                    if (mPersonModel != null) {
                                        if((mPersonModel.getProfileImage() == null
                                                || mPersonModel.getProfileImage().trim().length() == 0)){
                                            model.setProfileImage("");
                                            model.setProfileImageName("");
                                            addPersonDetails(model);
                                        }else{
                                            model.setProfileImage(mPersonModel.getProfileImage());
                                            model.setProfileImageName(mPersonModel.getProfileImageName());
                                            addPersonDetails(model);
                                        }
                                    }else{
                                        model.setProfileImage("");
                                        model.setProfileImageName("");
                                        addPersonDetails(model);
                                    }
                                }
                            }
                        }
                    });

                    btnNegative.setOnClickListener(new View.OnClickListener() {
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
        }
    }

    /**
     * Here we get compressed Image from ImageCompression class
     * @param destinationPath
     */
    String strImageNameInStorage="";
    private void uploadDetailsToServer(Uri uri, final PersonModel inputModel) {
        //displaying progress dialog while image is uploading
        CommonMethods.showProgressDialog(getActivity());

        if (mPersonModel != null
                && mPersonModel.getProfileImageName() != null
                && mPersonModel.getProfileImageName().trim().length()>0) {
            strImageNameInStorage = mPersonModel.getProfileImageName();
        } else {
            strImageNameInStorage = dateFormatter.format(new Date())+ ".jpg";
        }

        //getting the storage reference
        final StorageReference sRef = AttendanceApplication.storageReference
                .child(inputModel.getUserType_mobileNo())
                .child(strImageNameInStorage);
        //adding the file to reference
        sRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //displaying success toast
                        Toast.makeText(getActivity(), getString(R.string.msg_image_uploaded_successfully), Toast.LENGTH_SHORT).show();

                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //creating the upload object to store uploaded image details
                                inputModel.setProfileImage(uri.toString());
                                inputModel.setProfileImageName(strImageNameInStorage);
                                addPersonDetails(inputModel);
                            }
                        });

                        CommonMethods.cancelProgressDialog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //dismissing the progress dialog
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addPersonDetails(final PersonModel inputModel) {
        if (CommonMethods.isNetworkConnected(requireActivity())) {
            CommonMethods.showProgressDialog(getActivity());
            if (mPersonModel != null) {
                inputModel.setRegistrationDate(mPersonModel.getRegistrationDate());
                /**
                 * First we check whether the same user exist (based on mobile no)
                 */
                AttendanceApplication.refCompanyUserDetails
                        .orderByChild("emailId").equalTo(inputModel.getMobileNo())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if (ds.getKey().equals(mPersonModel.getFirebaseKey())) {
                                            AttendanceApplication.refCompanyUserDetails
                                                    .child(mPersonModel.getFirebaseKey()).setValue(inputModel)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            CommonMethods.cancelProgressDialog();
                                                            // Write was successful!
                                                            createMessageAndSendEmail("Update",inputModel);

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            CommonMethods.cancelProgressDialog();
                                                            CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                                                    String.format(getString(R.string.msg_issue_while_updating_person_detail), e.getMessage()), getString(R.string.action_ok));
                                                        }
                                                    });
                                        } else {
                                            CommonMethods.cancelProgressDialog();
                                            CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                                    getString(R.string.msg_person_is_already_exist), getString(R.string.action_ok));
                                        }

                                    }
                                } else {
                                    AttendanceApplication.refCompanyUserDetails
                                            .child(mPersonModel.getFirebaseKey()).setValue(inputModel)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    CommonMethods.cancelProgressDialog();
                                                    createMessageAndSendEmail("Update",inputModel);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    CommonMethods.cancelProgressDialog();
                                                    CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                                            String.format(getString(R.string.msg_issue_while_updating_person_detail), e.getMessage()), getString(R.string.action_ok));
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError e) {
                                CommonMethods.cancelProgressDialog();
                                CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                        String.format(getString(R.string.msg_issue_while_checking_person_mobile_no), e.getMessage()), getString(R.string.action_ok));
                            }
                        });
            } else {
                inputModel.setRegistrationDate(new SimpleDateFormat(ConstantData.DATE_HOUR_FORMAT, Locale.US).format(Calendar.getInstance().getTime()));
                /**
                 * First we check whether the same user exist (based on mobile no)
                 */
                AttendanceApplication.refCompanyUserDetails
                        .orderByChild("mobileNo").equalTo(inputModel.getMobileNo())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {

                                    /**
                                     * Here we add person details to fire-base
                                     */

                                    String key = AttendanceApplication.refCompanyUserDetails
                                            .push().getKey();
                                    assert key != null;
                                    inputModel.setFirebaseKey(key);
                                    AttendanceApplication.refCompanyUserDetails
                                            .child(key).setValue(inputModel)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    CommonMethods.cancelProgressDialog();
                                                    createMessageAndSendEmail("Add",inputModel);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    CommonMethods.cancelProgressDialog();
                                                    CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                                            String.format(getString(R.string.msg_issue_while_adding_person_detail), e.getMessage()),
                                                            getString(R.string.action_ok));
                                                }
                                            });
                                } else {
                                    CommonMethods.cancelProgressDialog();
                                    CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                            getString(R.string.msg_person_is_already_exist), getString(R.string.action_ok));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError e) {
                                CommonMethods.cancelProgressDialog();
                                CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                        String.format(getString(R.string.msg_issue_while_checking_person_mobile_no), e.getMessage()),
                                        getString(R.string.action_ok));
                            }
                        });
            }

        } else {
            CommonMethods.showConnectionAlert(getActivity());
        }
    }

    private void createMessageAndSendEmail(String subject, PersonModel model) {

        if (model.getFirebaseKey() != null
                && model.getFirebaseKey().trim().length() > 0) {
            //Notify employee about tracking
            CommonMethods.notifyTrackingStatusToEmployee(getActivity(), model);
        }

        String message1="";
        if(subject.equals("Add")){
            message1="Hi "+model.getName()+",\n\nYou are now registered with company.";
        }else if(subject.equals("Update")){
            message1="Hi "+model.getName()+",\n\nSome of your details getting updated. Kindly check it once.";
        }
        String message2="\n\nKindly download '"+getActivity().getString(R.string.app_name)+"' app and start marking attendance using below credentials : ";
        String message4="\nMobile No : "+model.getMobileNo();
        String message5="\nEmail Id : "+model.getEmailId();
        String message6="\nPassword : "+model.getPassword();
        String message8="";
        /*if(model.isAllowToMarkAttendanceFromPersonalDevice()) {
            message8 = "\n\nPerform Below Task : \n1)Change your password from 'CHANGE PASSWORD' option if needed." +
                    "\n2)Send Phone Mapping request from 'Phone Mapping' option.";
        }else{*/
            message8 = "\n\nPerform Below Task : 1)Change your password from 'CHANGE PASSWORD' option if needed.";
        //}

        String message=message1+message2+message4+message5+message6+message8;


        String alertMessage="";
        if(subject.equals("Add")){
            alertMessage=String.format(getString(R.string.msg_person_added_successfully),model.getName());
        }else if(subject.equals("Update")){
            alertMessage=String.format(getString(R.string.msg_person_detail_updated_successfully),model.getName());
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireActivity(),
                R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(getString(R.string.alert_title_person_added_success));
        alertDialogBuilder.setMessage(alertMessage)
                .setPositiveButton(getString(R.string.action_via_mail), null)
                .setNegativeButton(getString(R.string.action_via_message), null)
                .setNeutralButton(getString(R.string.action_cancel), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button btnNeutral = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        String[] TO = {model.getEmailId()};
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);

                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Attendance]");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String smsNumber = String.format("smsto: %s",
                                model.getMobileDialerCode().concat(model.getMobileNo()));
                        // Create the intent.
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                        // Set the data for the intent as the phone number.
                        smsIntent.setData(Uri.parse(smsNumber));
                        // Add the message (sms) with the key ("sms_body").
                        smsIntent.putExtra("sms_body", message);
                        // If package resolves (target app installed), send intent.
                        if (smsIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(smsIntent);
                        } else {
                            Toast.makeText(getActivity(), "Can't resolve app for ACTION_SEND_TO Intent", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnNeutral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        if(getActivity()!=null){
                            if(getActivity() instanceof AdminDashboardActivity){
                                ((AdminDashboardActivity) getActivity()).onBackPressed();
                            }
                        }
                    }
                });
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();


    }



    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId()==R.id.chk_monday){
            if (isChecked) {
                mTvShopTimeForMondayFrom.setText(DEFAULT_SHOP_OPEN_TIME);
                mTvShopTimeForMondayTo.setText(DEFAULT_SHOP_CLOSE_TIME);
                mTvFullDayHoursFoMonday.setText(DEFAULT_FULL_DAY_HOURS);
                mCbHalfDayAllowForMonday.setEnabled(true);
            } else {
                setDefaultValuesForTimeSlot(ConstantData.MONDAY);
            }
        }else if(buttonView.getId()==R.id.chk_tuesday){
            if (isChecked) {
                mTvShopTimeForTuesdayFrom.setText(DEFAULT_SHOP_OPEN_TIME);
                mTvShopTimeForTuesdayTo.setText(DEFAULT_SHOP_CLOSE_TIME);
                mTvFullDayHoursFoTuesday.setText(DEFAULT_FULL_DAY_HOURS);
                mCbHalfDayAllowForTuesday.setEnabled(true);
            } else {
                setDefaultValuesForTimeSlot(ConstantData.TUESDAY);
            }
        }else if(buttonView.getId()==R.id.chk_wednesday){
            if (isChecked) {
                mTvShopTimeForWednesdayFrom.setText(DEFAULT_SHOP_OPEN_TIME);
                mTvShopTimeForWednesdayTo.setText(DEFAULT_SHOP_CLOSE_TIME);
                mTvFullDayHoursFoWednesday.setText(DEFAULT_FULL_DAY_HOURS);
                mCbHalfDayAllowForWednesday.setEnabled(true);
            } else {
                setDefaultValuesForTimeSlot(ConstantData.WEDNESDAY);
            }
        }else if(buttonView.getId()==R.id.chk_thuresday){
            if (isChecked) {
                mTvShopTimeForThursdayFrom.setText(DEFAULT_SHOP_OPEN_TIME);
                mTvShopTimeForThursdayTo.setText(DEFAULT_SHOP_CLOSE_TIME);
                mTvFullDayHoursFoThursday.setText(DEFAULT_FULL_DAY_HOURS);
                mCbHalfDayAllowForThursday.setEnabled(true);
            } else {
                setDefaultValuesForTimeSlot(ConstantData.THURESDAY);
            }
        }else if(buttonView.getId()==R.id.chk_friday){
            if (isChecked) {
                mTvShopTimeForFridayFrom.setText(DEFAULT_SHOP_OPEN_TIME);
                mTvShopTimeForFridayTo.setText(DEFAULT_SHOP_CLOSE_TIME);
                mTvFullDayHoursFoFriday.setText(DEFAULT_FULL_DAY_HOURS);
                mCbHalfDayAllowForFriday.setEnabled(true);
            } else {
                setDefaultValuesForTimeSlot(ConstantData.FRIDAY);
            }
        }else if(buttonView.getId()==R.id.chk_saturday){
            if (isChecked) {
                mTvShopTimeForSaturdayFrom.setText(DEFAULT_SHOP_OPEN_TIME);
                mTvShopTimeForSaturdayTo.setText(DEFAULT_SHOP_CLOSE_TIME);
                mTvFullDayHoursFoSaturday.setText(DEFAULT_FULL_DAY_HOURS);
                mCbHalfDayAllowForSaturday.setEnabled(true);
            } else {
                setDefaultValuesForTimeSlot(ConstantData.SATURDAY);
            }
        }else if(buttonView.getId()==R.id.chk_sunday){
            if (isChecked) {
                mTvShopTimeForSundayFrom.setText(DEFAULT_SHOP_OPEN_TIME);
                mTvShopTimeForSundayTo.setText(DEFAULT_SHOP_CLOSE_TIME);
                mTvFullDayHoursFoSunday.setText(DEFAULT_FULL_DAY_HOURS);
                mCbHalfDayAllowForSunday.setEnabled(true);
            } else {
                setDefaultValuesForTimeSlot(ConstantData.SUNDAY);
            }
        }else if(buttonView.getId()==R.id.chk_half_day_allow_for_monday){
            if (isChecked) {
                mTvHalfDayHoursFoMonday.setClickable(true);
            }else{
                setDefaultValuesForHalfDayTimeSlot(ConstantData.MONDAY);
            }
        }else if(buttonView.getId()==R.id.chk_half_day_allow_for_tuesday){
            if (isChecked) {
                mTvHalfDayHoursFoTuesday.setClickable(true);
            }else{
                setDefaultValuesForHalfDayTimeSlot(ConstantData.TUESDAY);
            }
        }else if(buttonView.getId()==R.id.chk_half_day_allow_for_wednesday){
            if (isChecked) {
                mTvHalfDayHoursFoWednesday.setClickable(true);
            }else{
                setDefaultValuesForHalfDayTimeSlot(ConstantData.WEDNESDAY);
            }
        }else if(buttonView.getId()==R.id.chk_half_day_allow_for_thuresday){
            if (isChecked) {
                mTvHalfDayHoursFoThursday.setClickable(true);
            }else{
                setDefaultValuesForHalfDayTimeSlot(ConstantData.THURESDAY);
            }
        }else if(buttonView.getId()==R.id.chk_half_day_allow_for_friday){
            if (isChecked) {
                mTvHalfDayHoursFoFriday.setClickable(true);
            }else{
                setDefaultValuesForHalfDayTimeSlot(ConstantData.FRIDAY);
            }
        }else if(buttonView.getId()==R.id.chk_half_day_allow_for_saturday){
            if (isChecked) {
                mTvHalfDayHoursFoSaturday.setClickable(true);
            }else{
                setDefaultValuesForHalfDayTimeSlot(ConstantData.SATURDAY);
            }
        }else if(buttonView.getId()==R.id.chk_half_day_allow_for_sunday){
            if (isChecked) {
                mTvHalfDayHoursFoSunday.setClickable(true);
            }else{
                setDefaultValuesForHalfDayTimeSlot(ConstantData.SUNDAY);
            }
        }
    }

    private void setDefaultValuesForTimeSlot(String day) {
        switch (day) {
            case ConstantData.MONDAY:
                mCbMonday.setChecked(false);
                mTvShopTimeForMondayFrom.setText(getString(R.string.label_set));
                mTvShopTimeForMondayTo.setText(getString(R.string.label_set));
                mCbHalfDayAllowForMonday.setChecked(false);
                mCbHalfDayAllowForMonday.setEnabled(false);
                mTvFullDayHoursFoMonday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                mTvHalfDayHoursFoMonday.setText("");
                mTvHalfDayHoursFoMonday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoMonday.setClickable(false);
                break;
            case ConstantData.TUESDAY:
                mCbTuesday.setChecked(false);
                mTvShopTimeForTuesdayFrom.setText(getString(R.string.label_set));
                mTvShopTimeForTuesdayTo.setText(getString(R.string.label_set));
                mCbHalfDayAllowForTuesday.setChecked(false);
                mCbHalfDayAllowForTuesday.setEnabled(false);
                mTvFullDayHoursFoTuesday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                mTvHalfDayHoursFoTuesday.setText("");
                mTvHalfDayHoursFoTuesday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoTuesday.setClickable(false);
                break;
            case ConstantData.WEDNESDAY:
                mCbWednesday.setChecked(false);
                mTvShopTimeForWednesdayFrom.setText(getString(R.string.label_set));
                mTvShopTimeForWednesdayTo.setText(getString(R.string.label_set));
                mCbHalfDayAllowForWednesday.setChecked(false);
                mCbHalfDayAllowForWednesday.setEnabled(false);
                mTvFullDayHoursFoWednesday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                mTvHalfDayHoursFoWednesday.setText("");
                mTvHalfDayHoursFoWednesday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoWednesday.setClickable(false);
                break;
            case ConstantData.THURESDAY:
                mCbThursday.setChecked(false);
                mTvShopTimeForThursdayFrom.setText(getString(R.string.label_set));
                mTvShopTimeForThursdayTo.setText(getString(R.string.label_set));
                mCbHalfDayAllowForThursday.setChecked(false);
                mCbHalfDayAllowForThursday.setEnabled(false);
                mTvFullDayHoursFoThursday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                mTvHalfDayHoursFoThursday.setText("");
                mTvHalfDayHoursFoThursday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoThursday.setClickable(false);
                break;
            case ConstantData.FRIDAY:
                mCbFriday.setChecked(false);
                mTvShopTimeForFridayFrom.setText(getString(R.string.label_set));
                mTvShopTimeForFridayTo.setText(getString(R.string.label_set));
                mCbHalfDayAllowForFriday.setChecked(false);
                mCbHalfDayAllowForFriday.setEnabled(false);
                mTvFullDayHoursFoFriday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                mTvHalfDayHoursFoFriday.setText("");
                mTvHalfDayHoursFoFriday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoFriday.setClickable(false);
                break;
            case ConstantData.SATURDAY:
                mCbSaturday.setChecked(false);
                mTvShopTimeForSaturdayFrom.setText(getString(R.string.label_set));
                mTvShopTimeForSaturdayTo.setText(getString(R.string.label_set));
                mCbHalfDayAllowForSaturday.setChecked(false);
                mCbHalfDayAllowForSaturday.setEnabled(false);
                mTvFullDayHoursFoSaturday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                mTvHalfDayHoursFoSaturday.setText("");
                mTvHalfDayHoursFoSaturday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoSaturday.setClickable(false);
                break;
            case ConstantData.SUNDAY:
                mCbSunday.setChecked(false);
                mTvShopTimeForSundayFrom.setText(getString(R.string.label_set));
                mTvShopTimeForSundayTo.setText(getString(R.string.label_set));
                mCbHalfDayAllowForSunday.setChecked(false);
                mCbHalfDayAllowForSunday.setEnabled(false);
                mTvFullDayHoursFoSunday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                mTvHalfDayHoursFoSunday.setText("");
                mTvHalfDayHoursFoSunday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoSunday.setClickable(false);
                break;
            default:
                break;
        }
    }
    private void setDefaultValuesForHalfDayTimeSlot(String day) {
        switch (day) {
            case ConstantData.MONDAY:
                mCbHalfDayAllowForMonday.setChecked(false);
                mTvHalfDayHoursFoMonday.setText("");
                mTvHalfDayHoursFoMonday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoMonday.setClickable(false);
                break;
            case ConstantData.TUESDAY:
                mCbHalfDayAllowForTuesday.setChecked(false);
                mTvHalfDayHoursFoTuesday.setText("");
                mTvHalfDayHoursFoTuesday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoTuesday.setClickable(false);
                break;
            case ConstantData.WEDNESDAY:
                mCbHalfDayAllowForWednesday.setChecked(false);
                mTvHalfDayHoursFoWednesday.setText("");
                mTvHalfDayHoursFoWednesday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoWednesday.setClickable(false);
                break;
            case ConstantData.THURESDAY:
                mCbHalfDayAllowForThursday.setChecked(false);
                mTvHalfDayHoursFoThursday.setText("");
                mTvHalfDayHoursFoThursday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoThursday.setClickable(false);
                break;
            case ConstantData.FRIDAY:
                mCbHalfDayAllowForFriday.setChecked(false);
                mTvHalfDayHoursFoFriday.setText("");
                mTvHalfDayHoursFoFriday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoFriday.setClickable(false);
                break;
            case ConstantData.SATURDAY:
                mCbHalfDayAllowForSaturday.setChecked(false);
                mTvHalfDayHoursFoSaturday.setText("");
                mTvHalfDayHoursFoSaturday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoSaturday.setClickable(false);
                break;
            case ConstantData.SUNDAY:
                mCbHalfDayAllowForSunday.setChecked(false);
                mTvHalfDayHoursFoSunday.setText("");
                mTvHalfDayHoursFoSunday.setHint(getString(R.string.label_set));
                mTvHalfDayHoursFoSunday.setClickable(false);
                break;
            default:
                break;
        }
    }

    private void selectTimeInHours_(TextView textView){
        Calendar c1 = Calendar.getInstance();
        int hour1 = c1.get(Calendar.HOUR_OF_DAY);
        int minute1 = c1.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker1;
        mTimePicker1 = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                String hourString = selectedHour < 10 ? "0".concat(String.valueOf(selectedHour)) : String.valueOf(selectedHour);
                String minuteString = selectedMinute < 10 ? "0" .concat(String.valueOf(selectedMinute)) : String.valueOf(selectedMinute);

                String fullDayHours="";
                switch (SHOP_TIME_SELECTION_POSITION_DAY_WISE) {
                    case 1://Monday
                        fullDayHours=mTvFullDayHoursFoMonday.getText().toString().trim();
                        break;
                    case 2://Tuesday
                        fullDayHours=mTvFullDayHoursFoTuesday.getText().toString().trim();
                        break;
                    case 3://Wednesday
                        fullDayHours=mTvFullDayHoursFoWednesday.getText().toString().trim();
                        break;
                    case 4://Thuresday
                        fullDayHours=mTvFullDayHoursFoThursday.getText().toString().trim();
                        break;
                    case 5://Friday
                        fullDayHours=mTvFullDayHoursFoFriday.getText().toString().trim();
                        break;
                    case 6://Saturday
                        fullDayHours=mTvFullDayHoursFoSaturday.getText().toString().trim();
                        break;
                    case 7://Sunday
                        fullDayHours=mTvFullDayHoursFoSunday.getText().toString().trim();
                        break;
                    default:
                        break;
                }

                if (fullDayHours.trim().length() > 0
                        && !fullDayHours.equalsIgnoreCase(getString(R.string.label_set))) {
                    Date fullDayHoursDate = null;
                    try {
                        fullDayHoursDate = twentyFourHoursFormat.parse(fullDayHours);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Date halfDayHoursDate = null;
                    try {
                        halfDayHoursDate = twentyFourHoursFormat.parse(String.valueOf(hourString).concat(":").concat(String.valueOf(minuteString)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (fullDayHoursDate != null
                            && halfDayHoursDate != null) {
                        if (halfDayHoursDate.before(fullDayHoursDate)) {
                            textView.setText(String.valueOf(hourString).concat(":").concat(String.valueOf(minuteString)));
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.mgs_half_day_hours_always_smaller_than_full_day_hours), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }, hour1, minute1, true);//Yes 24 hour time
        mTimePicker1.show();
    }

    private void openTimePicker() {
        if(getActivity()!=null){
            if(getActivity() instanceof AdminDashboardActivity){
                ((AdminDashboardActivity)getActivity()).openTimePicker();
            }
        }
    }

    /**
     * Here we get from-to time value and display into UI
     * @param dateOpenTime
     * @param dateCloseTime
     */

    public void displaySelectionTime(Date dateOpenTime, Date dateCloseTime) {
        String strOpenTime = DEFAULT_SHOP_OPEN_TIME;
        String strCloseTime = DEFAULT_SHOP_CLOSE_TIME;
        try {
            strOpenTime = twelveHoursFormat.format(dateOpenTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            strCloseTime = twelveHoursFormat.format(dateCloseTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long difference = dateCloseTime.getTime() - dateOpenTime.getTime();
        int days = (int) (difference / (1000*60*60*24));
        int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
        int min = (int) (difference - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
        String hourString = hours < 10 ? "0".concat(String.valueOf(hours)) : String.valueOf(hours);
        String minuteString = min < 10 ? "0" .concat(String.valueOf(min)) : String.valueOf(min);

        switch (SHOP_TIME_SELECTION_POSITION_DAY_WISE) {
            case 1://Monday
                mTvShopTimeForMondayFrom.setText(strOpenTime);
                mTvShopTimeForMondayTo.setText(strCloseTime);
                mTvFullDayHoursFoMonday.setText(hourString.concat(":").concat(minuteString));
                /**
                 * Here we set hint value to half day hours
                 * when user select from-to time
                 */
                mTvHalfDayHoursFoMonday.setText("");
                mTvHalfDayHoursFoMonday.setHint(getString(R.string.label_set));
                break;
            case 2://Tuesday
                mTvShopTimeForTuesdayFrom.setText(strOpenTime);
                mTvShopTimeForTuesdayTo.setText(strCloseTime);
                mTvFullDayHoursFoTuesday.setText(hourString.concat(":").concat(minuteString));

                mTvHalfDayHoursFoTuesday.setText("");
                mTvHalfDayHoursFoTuesday.setHint(getString(R.string.label_set));
                break;
            case 3://Wednesday
                mTvShopTimeForWednesdayFrom.setText(strOpenTime);
                mTvShopTimeForWednesdayTo.setText(strCloseTime);
                mTvFullDayHoursFoWednesday.setText(hourString.concat(":").concat(minuteString));

                mTvHalfDayHoursFoWednesday.setText("");
                mTvHalfDayHoursFoWednesday.setHint(getString(R.string.label_set));
                break;
            case 4://Thuresday
                mTvShopTimeForThursdayFrom.setText(strOpenTime);
                mTvShopTimeForThursdayTo.setText(strCloseTime);
                mTvFullDayHoursFoThursday.setText(hourString.concat(":").concat(minuteString));

                mTvHalfDayHoursFoThursday.setText("");
                mTvHalfDayHoursFoThursday.setHint(getString(R.string.label_set));
                break;
            case 5://Friday
                mTvShopTimeForFridayFrom.setText(strOpenTime);
                mTvShopTimeForFridayTo.setText(strCloseTime);
                mTvFullDayHoursFoFriday.setText(hourString.concat(":").concat(minuteString));

                mTvHalfDayHoursFoFriday.setText("");
                mTvHalfDayHoursFoFriday.setHint(getString(R.string.label_set));
                break;
            case 6://Saturday
                mTvShopTimeForSaturdayFrom.setText(strOpenTime);
                mTvShopTimeForSaturdayTo.setText(strCloseTime);
                mTvFullDayHoursFoSaturday.setText(hourString.concat(":").concat(minuteString));

                mTvHalfDayHoursFoSaturday.setText("");
                mTvHalfDayHoursFoSaturday.setHint(getString(R.string.label_set));
                break;
            case 7://Sunday
                mTvShopTimeForSundayFrom.setText(strOpenTime);
                mTvShopTimeForSundayTo.setText(strCloseTime);
                mTvFullDayHoursFoSunday.setText(hourString.concat(":").concat(minuteString));

                mTvHalfDayHoursFoSunday.setText("");
                mTvHalfDayHoursFoSunday.setHint(getString(R.string.label_set));
                break;
            default:
                break;
        }
    }

    private void bindTimeSlotDataForMonthWise(ArrayList<ShopTimingModel> timeList) {
        if (timeList != null
                && timeList.size() > 0) {
            int length = timeList.size();
            ShopTimingModel model = null;
            for (int i = 0; i < length; i++) {
                model = timeList.get(i);

                if (model.getFromTime() != null
                        && model.getFromTime().trim().length() > 0
                        && model.getToTime() != null
                        && model.getToTime().trim().length() > 0) {
                    if (model.getDay().equalsIgnoreCase(ConstantData.MONDAY)) {
                        mCbMonday.setChecked(true);
                        mTvShopTimeForMondayFrom.setText(model.getFromTime());
                        mTvShopTimeForMondayTo.setText(model.getToTime());
                        mTvFullDayHoursFoMonday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForMonday.setEnabled(true);
                        mCbHalfDayAllowForMonday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoMonday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoMonday.setHint(getString(R.string.label_set));
                        }
                        if(!model.isHalfDayAllow()){
                            mTvHalfDayHoursFoMonday.setClickable(false);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.TUESDAY)) {
                        mCbTuesday.setChecked(true);
                        mTvShopTimeForTuesdayFrom.setText(model.getFromTime());
                        mTvShopTimeForTuesdayTo.setText(model.getToTime());
                        mTvFullDayHoursFoTuesday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForTuesday.setEnabled(true);
                        mCbHalfDayAllowForTuesday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoTuesday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoTuesday.setHint(getString(R.string.label_set));
                        }
                        if(!model.isHalfDayAllow()){
                            mTvHalfDayHoursFoTuesday.setClickable(false);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.WEDNESDAY)) {
                        mCbWednesday.setChecked(true);
                        mTvShopTimeForWednesdayFrom.setText(model.getFromTime());
                        mTvShopTimeForWednesdayTo.setText(model.getToTime());
                        mTvFullDayHoursFoWednesday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForWednesday.setEnabled(true);
                        mCbHalfDayAllowForWednesday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoWednesday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoWednesday.setHint(getString(R.string.label_set));
                        }
                        if(!model.isHalfDayAllow()){
                            mTvHalfDayHoursFoWednesday.setClickable(false);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.THURESDAY)) {
                        mCbThursday.setChecked(true);
                        mTvShopTimeForThursdayFrom.setText(model.getFromTime());
                        mTvShopTimeForThursdayTo.setText(model.getToTime());
                        mTvFullDayHoursFoThursday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForThursday.setEnabled(true);
                        mCbHalfDayAllowForThursday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoThursday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoThursday.setHint(getString(R.string.label_set));
                        }
                        if(!model.isHalfDayAllow()){
                            mTvHalfDayHoursFoThursday.setClickable(false);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.FRIDAY)) {
                        mCbFriday.setChecked(true);
                        mTvShopTimeForFridayFrom.setText(model.getFromTime());
                        mTvShopTimeForFridayTo.setText(model.getToTime());
                        mTvFullDayHoursFoFriday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForFriday.setEnabled(true);
                        mCbHalfDayAllowForFriday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoFriday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoFriday.setHint(getString(R.string.label_set));
                        }
                        if(!model.isHalfDayAllow()){
                            mTvHalfDayHoursFoFriday.setClickable(false);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.SATURDAY)) {
                        mCbSaturday.setChecked(true);
                        mTvShopTimeForSaturdayFrom.setText(model.getFromTime());
                        mTvShopTimeForSaturdayTo.setText(model.getToTime());
                        mTvFullDayHoursFoSaturday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForSaturday.setEnabled(true);
                        mCbHalfDayAllowForSaturday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoSaturday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoSaturday.setHint(getString(R.string.label_set));
                        }
                        if(!model.isHalfDayAllow()){
                            mTvHalfDayHoursFoSaturday.setClickable(false);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.SUNDAY)) {
                        mCbSunday.setChecked(true);
                        mTvShopTimeForSundayFrom.setText(model.getFromTime());
                        mTvShopTimeForSundayTo.setText(model.getToTime());
                        mTvFullDayHoursFoSunday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForSunday.setEnabled(true);
                        mCbHalfDayAllowForSunday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoSunday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoSunday.setHint(getString(R.string.label_set));
                        }
                        if(!model.isHalfDayAllow()){
                            mTvHalfDayHoursFoSunday.setClickable(false);
                        }
                    }
                } else {
                    setDefaultValuesForTimeSlot(model.getDay());
                }
            }
        }
    }


    private void onProfileImageClick() {
        captureImages();
    }


    private void captureImages() {

        ImagePickerActivity.showImagePickerOptions(getActivity(), new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void loadProfile(String url) {
        CommonMethods.loadImage(getActivity(),url,mIvUserProfile,
                ContextCompat.getDrawable(requireActivity(),R.drawable.baseline_account_circle_black));
    }
    private void loadDefaultProfile() {
        CommonMethods.loadDefaultImage(getActivity(),mIvUserProfile,
                ContextCompat.getDrawable(requireActivity(),R.drawable.baseline_account_circle_black));
    }

    private int getIndex(Spinner spinner, String branchCode) {
        int index = 0;
        int size = spinner.getCount();
        for (int i = 0; i < size; i++) {
            LocationModel relationModel = (LocationModel) spinner.getItemAtPosition(i);
            if (relationModel.getCode().equalsIgnoreCase(branchCode)) {
                index = i;
                break;
            }
        }
        return index;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ImagePickerActivity.clearCache(requireActivity());
    }


}