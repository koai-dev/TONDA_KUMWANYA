package in.gabsinfo.usertimeslotgeofenceattendance.calender;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.UserAttendanceHistoryInCalendarFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;


/**
 * Created by Maximilian on 9/1/14.
 */
public class MaterialCalendarAdapter extends BaseAdapter {
    private final Fragment mFragment;
    // Variables
    private Context mContext;
    private static ViewHolder mHolder;
    int mWeekDayNames = 7;
    int mGridViewIndexOffset = 1;

    private static class ViewHolder {
        ImageView mSelectedDayImageView;
        ImageView materialCurrentDayImageView;
        TextView mTextView;
        TextView mTvPunchInTime,mTvPunchOutTime;
        CardView mCvPunchTiming;
    }

    // Constructor
    public MaterialCalendarAdapter(Fragment fragment) {
        mFragment = fragment;
        mContext = fragment.getActivity();
    }

    @Override
    public int getCount() {
        if (MaterialCalendar.mFirstDay != -1 && MaterialCalendar.mNumDaysInMonth != -1) {
            Log.d("GRID_COUNT", String.valueOf(mWeekDayNames + MaterialCalendar.mFirstDay + MaterialCalendar.mNumDaysInMonth));
            return mWeekDayNames + MaterialCalendar.mFirstDay + MaterialCalendar.mNumDaysInMonth;
        }

        return mWeekDayNames;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_material_day, parent, false);

            mHolder = new ViewHolder();

            if (convertView != null) {
                mHolder.mSelectedDayImageView =  convertView.findViewById(R.id.material_calendar_selected_day);
                mHolder.materialCurrentDayImageView =  convertView.findViewById(R.id.material_calendar_current_day);
                mHolder.mTextView =  convertView.findViewById(R.id.material_calendar_day);
                mHolder.mCvPunchTiming =  convertView.findViewById(R.id.cv_material_punch_timings);
                mHolder.mTvPunchInTime =  convertView.findViewById(R.id.material_punch_in_time);
                mHolder.mTvPunchOutTime =  convertView.findViewById(R.id.material_punch_out_time);
                convertView.setTag(mHolder);
            }
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        if (mHolder.mSelectedDayImageView != null) {
            GridView gridView = (GridView) parent;
            Log.d("ITEM_CHECKED_POSITION", String.valueOf(gridView.isItemChecked(position)));
            if (gridView.isItemChecked(position)) {
                Animation feedBackAnimation = AnimationUtils.loadAnimation(mContext, R.anim.selected_day_feedback);
                mHolder.mSelectedDayImageView.setVisibility(View.VISIBLE);

                if (feedBackAnimation != null) {
                    mHolder.mSelectedDayImageView.startAnimation(feedBackAnimation);
                }
            } else {
                mHolder.mSelectedDayImageView.setVisibility(View.INVISIBLE);
            }
        }


        if (mHolder.mTextView != null) {
            setCalendarDay(position);
        }

        setSavedEvent(position);

        return convertView;
    }


    private void setCalendarDay(int position) {

        if (position <= mWeekDayNames - mGridViewIndexOffset + MaterialCalendar.mFirstDay) {
            mHolder.mTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            Log.d("NO_CLICK_POSITION", String.valueOf(position));
            mHolder.mTextView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            mHolder.mTextView.setTextColor(mContext.getResources().getColor(R.color.calendar_number_text_color));
            mHolder.mTextView.setTypeface(Typeface.DEFAULT);
        }


        switch (position) {
            case 0:
                mHolder.mTextView.setText(mContext.getResources().getString(R.string.sunday));
                break;

            case 1:
                mHolder.mTextView.setText(mContext.getResources().getString(R.string.monday));
                break;

            case 2:
                mHolder.mTextView.setText(mContext.getResources().getString(R.string.tuesday));
                break;

            case 3:
                mHolder.mTextView.setText(mContext.getResources().getString(R.string.wednesday));
                break;

            case 4:
                mHolder.mTextView.setText(mContext.getResources().getString(R.string.thursday));
                break;

            case 5:
                mHolder.mTextView.setText(mContext.getResources().getString(R.string.friday));
                break;

            case 6:
                mHolder.mTextView.setText(mContext.getResources().getString(R.string.saturday));
                break;

            default:
                Log.d("CURRENT_POSITION", String.valueOf(position));
                // TODO: TUSHAR
                mHolder.materialCurrentDayImageView.setVisibility(View.GONE);
                if (position < mWeekDayNames + MaterialCalendar.mFirstDay) {
                    Log.d("BLANK_POSITION", "This is a blank day");
                    mHolder.mTextView.setText("");
                    mHolder.mTextView.setTypeface(Typeface.DEFAULT);
                } else {
                    mHolder.mTextView.setText(String.valueOf(position - (mWeekDayNames - mGridViewIndexOffset) -
                            MaterialCalendar.mFirstDay));
                    //mHolder.mTextView.setTypeface(Typeface.DEFAULT_BOLD);

                    if (MaterialCalendar.mCurrentDay != -1) {
                        int startingPosition = mWeekDayNames - mGridViewIndexOffset + MaterialCalendar.mFirstDay;
                        int currentDayPosition = startingPosition + MaterialCalendar.mCurrentDay;

                        if (position == currentDayPosition) {
                            /*mHolder.mTextView.setTextColor(mContext.getResources().getColor(
                                    R.color.calendar_current_number_text_color));*/
                            mHolder.mTextView.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
                            mHolder.materialCurrentDayImageView.setVisibility(View.VISIBLE);
                        } else {
                            mHolder.mTextView.setTextColor(mContext.getResources().getColor(
                                    R.color.calendar_time_sheet_not_filled_color));
                        }
                    } else {

                        mHolder.mTextView.setTextColor(mContext.getResources().getColor(
                                R.color.calendar_time_sheet_not_filled_color));
                    }

                    /**
                     * Change color of Sunday TextView
                     */
                    /*if (position % 7 == 0) {
                        mHolder.mTextView.setTextColor(mContext.getResources().getColor(
                                R.color.colorSecondaryText));
                    }*/
                }
                break;
        }


    }


    private void setSavedEvent(int position) {
        switch (position) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                mHolder.mCvPunchTiming.setVisibility(View.GONE);
                break;
            default:
                mHolder.mCvPunchTiming.setVisibility(View.INVISIBLE);
                mHolder.mTvPunchInTime.setVisibility(View.INVISIBLE);
                mHolder.mTvPunchOutTime.setVisibility(View.INVISIBLE);

                if (MaterialCalendar.mFirstDay != -1 && ((UserAttendanceHistoryInCalendarFragment) mFragment).mTransactionList != null &&
                        ((UserAttendanceHistoryInCalendarFragment) mFragment).mTransactionList.size() > 0) {

                    int startingPosition = mWeekDayNames - mGridViewIndexOffset + MaterialCalendar.mFirstDay;
                    //Log.d("SAVED_EVENT_STARTING_POS", String.valueOf(startingPosition));
                    if (position > startingPosition) {
                        AttendanceModel eventDaysModel;
                        int len = ((UserAttendanceHistoryInCalendarFragment) mFragment).mTransactionList.size();
                        for (int i = 0; i < len; i++) {
                            eventDaysModel = ((UserAttendanceHistoryInCalendarFragment) mFragment).mTransactionList.get(i);
                            int savedEventPosition = startingPosition + eventDaysModel.getDay();

                            //Log.d("POSITION", String.valueOf(position));
                            //Log.d("SAVED_POSITION", String.valueOf(savedEventPosition));
                            if (position == savedEventPosition)//Means Day found valid
                            {

                                mHolder.mCvPunchTiming.setVisibility(View.VISIBLE);
                                mHolder.mCvPunchTiming.setCardBackgroundColor(ContextCompat.getColor(mContext,
                                        android.R.color.white));

                                mHolder.mTvPunchInTime.setVisibility(View.INVISIBLE);
                                mHolder.mTvPunchInTime.setTextColor(ContextCompat.getColor(mContext,
                                        android.R.color.white));
                                mHolder.mTvPunchOutTime.setVisibility(View.INVISIBLE);
                                mHolder.mTvPunchOutTime.setTextColor(ContextCompat.getColor(mContext,
                                        android.R.color.white));

                                int currentDayPosition = startingPosition + MaterialCalendar.mCurrentDay;
                                if (position == currentDayPosition) {

                                    mHolder.mTextView.setTextColor(ContextCompat.getColor(mContext,
                                            android.R.color.white));

                                    if (eventDaysModel.isTransactionAdded()) {

                                        bindDataWithControl(eventDaysModel,mHolder);
                                    }
                                } else {
                                    if (eventDaysModel.isTransactionAdded()) {

                                        bindDataWithControl(eventDaysModel,mHolder);
                                    } else {
                                        mHolder.mTextView.setTextColor(mContext.getResources().getColor(
                                                R.color.calendar_day_text_color));
                                    }
                                }
                            }else{
                                //mHolder.mSavedEventImageView.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        mHolder.mCvPunchTiming.setVisibility(View.INVISIBLE);
                        mHolder.mTvPunchInTime.setVisibility(View.INVISIBLE);
                        mHolder.mTvPunchOutTime.setVisibility(View.INVISIBLE);
                    }
                }
                break;
        }

    }

    private void bindDataWithControl(AttendanceModel eventDaysModel, ViewHolder mHolder){
        if (eventDaysModel.getType().equalsIgnoreCase(mContext.getString(R.string.label_public_holiday))) {

            mHolder.mTextView.setTextColor(mContext.getResources().getColor(
                    R.color.colorPublicHoliday));
            mHolder.mTextView.setTypeface(Typeface.DEFAULT_BOLD);

            mHolder.mTvPunchInTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchInTime.setText(eventDaysModel.getDescription());
            mHolder.mTvPunchInTime.setTextColor(ContextCompat.getColor(mContext,R.color.colorPublicHoliday));

        }else if (eventDaysModel.getType().equalsIgnoreCase(mContext.getString(R.string.label_non_working_day))) {

            mHolder.mTextView.setTextColor(mContext.getResources().getColor(
                    R.color.colorNonWorkingDay));
            mHolder.mTextView.setTypeface(Typeface.DEFAULT_BOLD);

            mHolder.mTvPunchInTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchInTime.setText("Off");
            mHolder.mTvPunchInTime.setTextColor(ContextCompat.getColor(mContext,R.color.colorNonWorkingDay));

        }else if (eventDaysModel.getType().equalsIgnoreCase(mContext.getString(R.string.label_out_pending))) {

            mHolder.mCvPunchTiming.setCardBackgroundColor(ContextCompat.getColor(mContext,R.color.colorMissPunch));
            mHolder.mTvPunchInTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchInTime.setText(eventDaysModel.getPunchInTime());
            mHolder.mTvPunchOutTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchOutTime.setText("N/A");

        } else if (eventDaysModel.getType().equalsIgnoreCase(mContext.getString(R.string.label_full_days))) {

            mHolder.mCvPunchTiming.setCardBackgroundColor(ContextCompat.getColor(mContext,R.color.colorFullDayPresent));
            mHolder.mTvPunchInTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchInTime.setText(eventDaysModel.getPunchInTime());
            mHolder.mTvPunchOutTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchOutTime.setText(eventDaysModel.getPunchOutTime());

        } else if (eventDaysModel.getType().equalsIgnoreCase(mContext.getString(R.string.label_half_days))) {


            mHolder.mCvPunchTiming.setCardBackgroundColor(ContextCompat.getColor(mContext,R.color.colorHalfDayPresent));
            mHolder.mTvPunchInTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchInTime.setText(eventDaysModel.getPunchInTime());
            mHolder.mTvPunchOutTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchOutTime.setText(eventDaysModel.getPunchOutTime());

        }else if (eventDaysModel.getType().equalsIgnoreCase(mContext.getString(R.string.label_present_but_leave))) {


            mHolder.mCvPunchTiming.setCardBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPresentButLeaveBefore));
            mHolder.mTvPunchInTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchInTime.setText(eventDaysModel.getPunchInTime());
            mHolder.mTvPunchOutTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchOutTime.setText(eventDaysModel.getPunchOutTime());

        } else if (eventDaysModel.getType().equalsIgnoreCase(mContext.getString(R.string.label_absent_day))) {

            mHolder.mCvPunchTiming.setCardBackgroundColor(ContextCompat.getColor(mContext,R.color.colorAbsentDay));
            mHolder.mTvPunchInTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchInTime.setText("N/A");
            mHolder.mTvPunchOutTime.setVisibility(View.VISIBLE);
            mHolder.mTvPunchOutTime.setText("N/A");
        }
    }
}
