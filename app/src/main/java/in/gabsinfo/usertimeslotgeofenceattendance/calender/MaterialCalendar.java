package in.gabsinfo.usertimeslotgeofenceattendance.calender;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import in.gabsinfo.usertimeslotgeofenceattendance.fragment.UserAttendanceHistoryInCalendarFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;


/**
 * Created by Maximilian on 9/2/14.
 */
public class MaterialCalendar {
    // Variables
    public static int mMonth = -1;
    public static int mYear = -1;
    public static int mCurrentDay = -1;
    public static int mCurrentMonth = -1;
    public static int mCurrentYear = -1;
    public static int mFirstDay = -1;
    public static int mNumDaysInMonth = -1;
    public static Activity mActivity;
    private static Fragment mFragment;


    public static void getInitialCalendarInfo(Fragment fragment) {
        mFragment = fragment;
        mActivity = fragment.getActivity();

        Calendar cal = Calendar.getInstance();

        if (cal != null) {
            Log.d("MONTH_NUMBER", String.valueOf(cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
            mNumDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            mMonth = cal.get(Calendar.MONTH);
            mYear = cal.get(Calendar.YEAR);

            mCurrentDay = cal.get(Calendar.DAY_OF_MONTH);
            mCurrentMonth = mMonth;
            mCurrentYear = mYear;

            getFirstDay(mMonth, mYear);
            getNumDayInMonth(mMonth, mYear);

            Log.d("CURRENT_DAY", String.valueOf(mCurrentDay));
            Log.d("CURRENT_MONTH_INFO", String.valueOf(getMonthName(mMonth) + " " + mYear + " has " + mNumDaysInMonth
                    + " days " +
                    "and starts on " + mFirstDay));

            IsFutureMonthOrYear(mYear, mMonth);
        }
    }


    private static void refreshCalendar(TextView monthTextView, GridView calendarGridView,
                                        MaterialCalendarAdapter materialCalendarAdapter, int month, int year) {

        checkCurrentDay(month, year);
        getNumDayInMonth(month, year);
        getFirstDay(month, year);

        if (monthTextView != null) {
            Log.d("REFRESH_MONTH", String.valueOf(month));
            Date refreshedDate=null;
            try {
                refreshedDate = new SimpleDateFormat("MMMMM/yyyy",Locale.US).parse(getMonthName(month) + "/" + year);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(refreshedDate!=null) {
                monthTextView.setText(new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US).format(refreshedDate));
            }
        }

        if (mFragment instanceof UserAttendanceHistoryInCalendarFragment) {
            ((UserAttendanceHistoryInCalendarFragment) mFragment).mNumEventsOnDay = -1;
            /**
             * Hit service to get time sheet filled days
             */
            ((UserAttendanceHistoryInCalendarFragment) mFragment).getData();
            //((CalendarActivity)mActivity).rlAlertMessage.setVisibility(View.GONE);

            if (materialCalendarAdapter != null) {
                if (calendarGridView != null) {
                    // Set current day to be auto selected when first opened
                    if (mCurrentDay != -1 && mFirstDay != -1) {
                        int startingPosition = 6 + mFirstDay;
                        int currentDayPosition = startingPosition + MaterialCalendar.mCurrentDay;

                        //Log.d("INITIAL_SELECTED_POSITION", String.valueOf(currentDayPosition));
                        calendarGridView.setItemChecked(currentDayPosition, true);
                    } else {
                        calendarGridView.setItemChecked(calendarGridView.getCheckedItemPosition(), false);
                    }
                    //calendarGridView.setItemChecked(calendarGridView.getCheckedItemPosition(), false);
                }
                materialCalendarAdapter.notifyDataSetChanged();
            }
        }

    }

    private static String getMonthName(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }

    private static void checkCurrentDay(int month, int year) {
        if (month == mCurrentMonth && year == mCurrentYear) {
            Calendar cal = Calendar.getInstance();
            mCurrentDay = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            // TODO: TUSHAR
            mCurrentDay = -1;
        }
    }

    private static void getNumDayInMonth(int month, int year) {
        Calendar selectedCal = Calendar.getInstance();
        if (selectedCal != null) {
            selectedCal.set(Calendar.MONTH, month);
            selectedCal.set(Calendar.YEAR, year);

            /**
             * This will helps to hide future dates
             */
            /*
            Calendar current = Calendar.getInstance();
            int cYear = current.get(Calendar.YEAR);
            int cMonth = current.get(Calendar.MONTH);

            if (cYear == year
                    && cMonth == month) {
                mNumDaysInMonth = current.get(Calendar.DAY_OF_MONTH);
            }else {
                mNumDaysInMonth = selectedCal.getActualMaximum(Calendar.DAY_OF_MONTH);
            }*/

            mNumDaysInMonth = selectedCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            Log.d("MONTH_NUMBER", String.valueOf(selectedCal.getActualMaximum(Calendar.DAY_OF_MONTH)));
        }
    }

    private static void getFirstDay(int month, int year) {
        Calendar cal = Calendar.getInstance();
        if (cal != null) {
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_MONTH, 1);

            switch (cal.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SUNDAY:
                    Log.d("FIRST_DAY", "Sunday");
                    mFirstDay = 0;
                    break;

                case Calendar.MONDAY:
                    Log.d("FIRST_DAY", "Monday");
                    mFirstDay = 1;
                    break;

                case Calendar.TUESDAY:
                    Log.d("FIRST_DAY", "Tuesday");
                    mFirstDay = 2;
                    break;

                case Calendar.WEDNESDAY:
                    Log.d("FIRST_DAY", "Wednesday");
                    mFirstDay = 3;
                    break;

                case Calendar.THURSDAY:
                    Log.d("FIRST_DAY", "Thursday");
                    mFirstDay = 4;
                    break;

                case Calendar.FRIDAY:
                    Log.d("FIRST_DAY", "Friday");
                    mFirstDay = 5;
                    break;

                case Calendar.SATURDAY:
                    Log.d("FIRST_DAY", "Saturday");
                    mFirstDay = 6;
                    break;

                default:
                    break;
            }
        }
    }

    // Call in View.OnClickListener for Previous ImageView
    public static void previousOnClick(ImageView previousImageView, TextView monthTextView,
                                       GridView calendarGridView, MaterialCalendarAdapter materialCalendarAdapter) {
        if (previousImageView != null && mMonth != -1 && mYear != -1) {
            previousMonth(monthTextView, calendarGridView, materialCalendarAdapter);
        }
    }

    // Call in View.OnClickListener for Next ImageView
    public static void nextOnClick(ImageView nextImageView, TextView monthTextView,
                                   GridView calendarGridView,
                                   MaterialCalendarAdapter materialCalendarAdapter) {
        if (nextImageView != null && mMonth != -1 && mYear != -1) {
            nextMonth(monthTextView, calendarGridView, materialCalendarAdapter);
        }
    }

    private static void previousMonth(TextView monthTextView, GridView calendarGridView,
                                      MaterialCalendarAdapter materialCalendarAdapter) {
        if (mMonth == 0) {
            mMonth = 11;
            mYear = mYear - 1;
        } else {
            mMonth = mMonth - 1;
        }

        if (mFragment != null) {
            if (mFragment instanceof UserAttendanceHistoryInCalendarFragment) {
                ((UserAttendanceHistoryInCalendarFragment) mFragment).mNext.setVisibility(View.VISIBLE);
            } else {
                ((UserAttendanceHistoryInCalendarFragment) mFragment).mNext.setVisibility(View.VISIBLE);
            }
        }
        refreshCalendar(monthTextView, calendarGridView, materialCalendarAdapter, mMonth, mYear);
    }

    private static void nextMonth(TextView monthTextView, GridView calendarGridView,
                                  MaterialCalendarAdapter materialCalendarAdapter) {

        if (mMonth == 11) {
            mMonth = 0;
            mYear = mYear + 1;
        } else {
            mMonth = mMonth + 1;
        }

        refreshCalendar(monthTextView, calendarGridView, materialCalendarAdapter, mMonth, mYear);

        IsFutureMonthOrYear(mYear, mMonth);
    }

    // Call in GridView.OnItemClickListener for custom Calendar GirdView
    public static void selectCalendarDay(MaterialCalendarAdapter materialCalendarAdapter, int position) {
        Log.d("SELECTED_POSITION", String.valueOf(position));
        int weekPositions = 6;
        int noneSelectablePositions = weekPositions + mFirstDay;

        if (position > noneSelectablePositions) {
            getSelectedDate(position, mMonth, mYear);

            if (materialCalendarAdapter != null) {
                materialCalendarAdapter.notifyDataSetChanged();
            }
        }
    }

    private static void IsFutureMonthOrYear(int mYear, int mMonth) {

        int mMonth_ = -1;
        int mYear_ = -1;
        if (mMonth == 11) {
            mMonth_ = 0;
            mYear_ = mYear + 1;
        } else {
            mMonth_ = mMonth + 1;
            mYear_ = mYear;
        }

        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(mYear_, mMonth_, 1);
        long selectedMilli = selectedCalendar.getTimeInMillis();
        Date dateForSelectedCalendar = new Date(selectedMilli);
        if (dateForSelectedCalendar.after(new Date())) {
            if (mFragment != null) {
                if (mFragment instanceof UserAttendanceHistoryInCalendarFragment) {
                    ((UserAttendanceHistoryInCalendarFragment) mFragment).mNext.setVisibility(View.GONE);
                }
            }
        }
    }


    public static void getSelectedDate(int selectedPosition, int month, int year) {
        int weekPositions = 6;
        int dateNumber = selectedPosition - weekPositions - mFirstDay;
        Log.d("DATE_NUMBER", String.valueOf(dateNumber));
        Log.d("SELECTED_DATE", String.valueOf(month + "/" + dateNumber + "/" + year));
    }
}

