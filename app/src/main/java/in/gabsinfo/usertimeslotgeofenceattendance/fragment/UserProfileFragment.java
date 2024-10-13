package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.ShopTimingModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;

import static in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData.DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY;
public class UserProfileFragment extends Fragment {

    private TextView mTvMonday, mTvTuesday, mTvWednesday, mTvThursday, mTvFriday, mTvSaturday, mTvSunday;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.item_logout);
        if(item!=null)
            item.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
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
        toolbar.setTitle(getString(R.string.toolbar_title_user_profile));
        ((UserDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
        Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((UserDashboardActivity) requireActivity()).onBackPressed();
            }
        });
    }

    private void init(View view) {

        PersonModel mPersonModel = null;
        if (getArguments() != null) {
            mPersonModel = (PersonModel) getArguments().getSerializable("PersonModel");
        } else
            return;

        ImageView ivUserProfile = view.findViewById(R.id.iv_user_profile_image);
        EditText etPersonName = view.findViewById(R.id.et_person_name);
        EditText etPersonAddress = view.findViewById(R.id.et_person_address);
        EditText etPersonMobileNo = view.findViewById(R.id.et_person_mobile_no);
        EditText etPersonDOB = view.findViewById(R.id.et_person_dob);
        EditText etPersonEmailId = view.findViewById(R.id.et_person_email_id);
        EditText etPersonPassword = view.findViewById(R.id.et_person_password);
        EditText etPersonDesignation = view.findViewById(R.id.et_person_designation);

        TextView tv_selected_work_type = view.findViewById(R.id.tv_selected_work_type);
        TextView mTvAmount = view.findViewById(R.id.tv_amount);
        TextView tvAmountSuffix = view.findViewById(R.id.tv_amount_suffix);

        EditText etCurrencySymbol = view.findViewById(R.id.et_currency_symbol);

        CardView cvTimeSlot = view.findViewById(R.id.cv_time_slot);

        mTvMonday = view.findViewById(R.id.tv_monday);
        mTvTuesday = view.findViewById(R.id.tv_tuesday);
        mTvWednesday = view.findViewById(R.id.tv_wednesday);
        mTvThursday = view.findViewById(R.id.tv_thursday);
        mTvFriday = view.findViewById(R.id.tv_friday);
        mTvSaturday = view.findViewById(R.id.tv_saturday);
        mTvSunday = view.findViewById(R.id.tv_sunday);

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

        mCbHalfDayAllowForMonday.setEnabled(false);
        mCbHalfDayAllowForTuesday.setEnabled(false);
        mCbHalfDayAllowForWednesday.setEnabled(false);
        mCbHalfDayAllowForThursday.setEnabled(false);
        mCbHalfDayAllowForFriday.setEnabled(false);
        mCbHalfDayAllowForSaturday.setEnabled(false);
        mCbHalfDayAllowForSunday.setEnabled(false);

        assert mPersonModel != null;
        if (mPersonModel.getProfileImage() != null
                && mPersonModel.getProfileImage().trim().length() > 0) {
            Glide.with(this)
                    .load(mPersonModel.getProfileImage())
                    .placeholder(ContextCompat.getDrawable(requireActivity(), R.drawable.loading_transparent))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            //on load failed
                            ivUserProfile.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            //on load success
                            ivUserProfile.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivUserProfile);
        } else {
            ivUserProfile.setVisibility(View.GONE);
        }

        etPersonName.setText(mPersonModel.getName());
        etPersonAddress.setText(mPersonModel.getAddress());
        etPersonMobileNo.setText(mPersonModel.getMobileDialerCode().concat(mPersonModel.getMobileNo()));
        etPersonDesignation.setText(mPersonModel.getDesignation());
        etPersonEmailId.setText(mPersonModel.getEmailId());
        etPersonPassword.setText(mPersonModel.getPassword());
        etPersonDOB.setText(mPersonModel.getDob());
        mTvAmount.setText(mPersonModel.getCurrencySymbol().concat(" ")
                .concat(CommonMethods.currencyFormatter(String.valueOf(mPersonModel.getAmount()))));
        if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_DAY_WISE)) {
            tv_selected_work_type.setText(ConstantData.WORK_TYPE_DAY_WISE);
            cvTimeSlot.setVisibility(View.VISIBLE);
            tvAmountSuffix.setText(getString(R.string.label_amount_suffix_per_day));
            bindTimeSlotDataForMonthWise(mPersonModel.getTimeSlotList());
        } else if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
            tv_selected_work_type.setText(ConstantData.WORK_TYPE_HOUR_WISE);
            tvAmountSuffix.setText(getString(R.string.label_amount_suffix_per_hour));
            cvTimeSlot.setVisibility(View.GONE);
        }else if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_MONTH_WISE)) {
            tv_selected_work_type.setText(ConstantData.WORK_TYPE_MONTH_WISE);
            tvAmountSuffix.setText(getString(R.string.label_amount_suffix_per_month));
            bindTimeSlotDataForMonthWise(mPersonModel.getTimeSlotList());
            cvTimeSlot.setVisibility(View.VISIBLE);
        }
        etCurrencySymbol.setText(mPersonModel.getCurrencySymbol());
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
                        mTvMonday.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_selected_day));
                        mTvMonday.setTextColor(ContextCompat.getColor(requireActivity(),android.R.color.white));
                        mTvShopTimeForMondayFrom.setText(model.getFromTime());
                        mTvShopTimeForMondayTo.setText(model.getToTime());
                        mTvFullDayHoursFoMonday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForMonday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoMonday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoMonday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.TUESDAY)) {
                        mTvTuesday.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_selected_day));
                        mTvTuesday.setTextColor(ContextCompat.getColor(requireActivity(),android.R.color.white));
                        mTvShopTimeForTuesdayFrom.setText(model.getFromTime());
                        mTvShopTimeForTuesdayTo.setText(model.getToTime());
                        mTvFullDayHoursFoTuesday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForTuesday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoTuesday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoTuesday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.WEDNESDAY)) {
                        mTvWednesday.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_selected_day));
                        mTvWednesday.setTextColor(ContextCompat.getColor(requireActivity(),android.R.color.white));
                        mTvShopTimeForWednesdayFrom.setText(model.getFromTime());
                        mTvShopTimeForWednesdayTo.setText(model.getToTime());
                        mTvFullDayHoursFoWednesday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForWednesday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoWednesday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoWednesday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.THURESDAY)) {
                        mTvThursday.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_selected_day));
                        mTvThursday.setTextColor(ContextCompat.getColor(requireActivity(),android.R.color.white));
                        mTvShopTimeForThursdayFrom.setText(model.getFromTime());
                        mTvShopTimeForThursdayTo.setText(model.getToTime());
                        mTvFullDayHoursFoThursday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForThursday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoThursday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoThursday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.FRIDAY)) {
                        mTvFriday.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_selected_day));
                        mTvFriday.setTextColor(ContextCompat.getColor(requireActivity(),android.R.color.white));
                        mTvShopTimeForFridayFrom.setText(model.getFromTime());
                        mTvShopTimeForFridayTo.setText(model.getToTime());
                        mTvFullDayHoursFoFriday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForFriday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoFriday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoFriday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.SATURDAY)) {
                        mTvSaturday.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_selected_day));
                        mTvSaturday.setTextColor(ContextCompat.getColor(requireActivity(),android.R.color.white));
                        mTvShopTimeForSaturdayFrom.setText(model.getFromTime());
                        mTvShopTimeForSaturdayTo.setText(model.getToTime());
                        mTvFullDayHoursFoSaturday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForSaturday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoSaturday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoSaturday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                        }
                    } else if (model.getDay().equalsIgnoreCase(ConstantData.SUNDAY)) {
                        mTvSunday.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_selected_day));
                        mTvSunday.setTextColor(ContextCompat.getColor(requireActivity(),android.R.color.white));
                        mTvShopTimeForSundayFrom.setText(model.getFromTime());
                        mTvShopTimeForSundayTo.setText(model.getToTime());
                        mTvFullDayHoursFoSunday.setText(model.getHoursForFullDay());
                        mCbHalfDayAllowForSunday.setChecked(model.isHalfDayAllow());
                        if(model.getHoursForHalfDay().trim().length() > 0 ){
                            mTvHalfDayHoursFoSunday.setText(model.getHoursForHalfDay().trim());
                        }else {
                            mTvHalfDayHoursFoSunday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                        }
                    }
                } else {
                    switch (model.getDay()) {
                        case ConstantData.MONDAY:
                            mTvMonday.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_unselected_day));
                            mTvMonday.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimaryText));
                            mTvShopTimeForMondayFrom.setText(getString(R.string.label_close));
                            mTvShopTimeForMondayTo.setText(getString(R.string.label_close));
                            mCbHalfDayAllowForMonday.setChecked(false);
                            mTvFullDayHoursFoMonday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            mTvHalfDayHoursFoMonday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            break;
                        case ConstantData.TUESDAY:
                            mTvTuesday.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_unselected_day));
                            mTvTuesday.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimaryText));
                            mTvShopTimeForTuesdayFrom.setText(getString(R.string.label_close));
                            mTvShopTimeForTuesdayTo.setText(getString(R.string.label_close));
                            mCbHalfDayAllowForTuesday.setChecked(false);
                            mTvFullDayHoursFoTuesday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            mTvHalfDayHoursFoTuesday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            break;
                        case ConstantData.WEDNESDAY:
                            mTvWednesday.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_unselected_day));
                            mTvWednesday.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimaryText));
                            mTvShopTimeForWednesdayFrom.setText(getString(R.string.label_close));
                            mTvShopTimeForWednesdayTo.setText(getString(R.string.label_close));
                            mCbHalfDayAllowForWednesday.setChecked(false);
                            mTvFullDayHoursFoWednesday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            mTvHalfDayHoursFoWednesday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            break;
                        case ConstantData.THURESDAY:
                            mTvThursday.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_unselected_day));
                            mTvThursday.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimaryText));
                            mTvShopTimeForThursdayFrom.setText(getString(R.string.label_close));
                            mTvShopTimeForThursdayTo.setText(getString(R.string.label_close));
                            mCbHalfDayAllowForThursday.setChecked(false);
                            mTvFullDayHoursFoThursday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            mTvHalfDayHoursFoThursday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            break;
                        case ConstantData.FRIDAY:
                            mTvFriday.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_unselected_day));
                            mTvFriday.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimaryText));
                            mTvShopTimeForFridayFrom.setText(getString(R.string.label_close));
                            mTvShopTimeForFridayTo.setText(getString(R.string.label_close));
                            mCbHalfDayAllowForFriday.setChecked(false);
                            mTvFullDayHoursFoFriday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            mTvHalfDayHoursFoFriday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            break;
                        case ConstantData.SATURDAY:
                            mTvSaturday.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_unselected_day));
                            mTvSaturday.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimaryText));
                            mTvShopTimeForSaturdayFrom.setText(getString(R.string.label_close));
                            mTvShopTimeForSaturdayTo.setText(getString(R.string.label_close));
                            mCbHalfDayAllowForSaturday.setChecked(false);
                            mTvFullDayHoursFoSaturday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            mTvHalfDayHoursFoSaturday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            break;
                        case ConstantData.SUNDAY:
                            mTvSunday.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_unselected_day));
                            mTvSunday.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimaryText));
                            mTvShopTimeForSundayFrom.setText(getString(R.string.label_close));
                            mTvShopTimeForSundayTo.setText(getString(R.string.label_close));
                            mCbHalfDayAllowForSunday.setChecked(false);
                            mTvFullDayHoursFoSunday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            mTvHalfDayHoursFoSunday.setText(DEFAULT_FULL_DAY_HOURS_WHILE_HOLIDAY);
                            break;
                        default:
                            break;
                    }

                }
            }
        }
    }
}
