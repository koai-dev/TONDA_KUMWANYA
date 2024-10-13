package in.gabsinfo.usertimeslotgeofenceattendance.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//http://www.javamultiplex.com/2017/03/java-program-how-to-print-all-mondays-in-given-month-and-year.html
public class NumberOfMondaysInMonth {

    /*
     * d -> day of month
     * M -> month of year
     * y -> year
     */
    public static List<String> getNumberOfDateByMonthYearName(String dayName,String monthName,String year){
        List<String> dates = new ArrayList<>();
        try {
            Log.d("Extract Date","Day name : "+dayName+" - "+"Month name : "+monthName+" - "+"Year Name : "+year);
            System.out.println();
            Date date = null;
            String myDate = null;

            DateFormat format = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US);
            int dayAndMonthNumber[] = new int[2];
            int days = 0, monthNumber = 0;
            // Converting String to uppercase
            monthName = monthName.toUpperCase();
            if (isValidMonth(monthName) && isValidYear(year)) {
                dayAndMonthNumber = getNumberofDaysAndMonthNumberByMonthName(monthName, year);
                days = dayAndMonthNumber[0];
                monthNumber = dayAndMonthNumber[1];
                // Converting String to int.
                int myYear = Integer.parseInt(year);
                // Creating Calendar class instance.
                Calendar cal = Calendar.getInstance();
                // Adding given month and year in Calendar.
                cal.set(Calendar.MONTH, monthNumber);
                cal.set(Calendar.YEAR, myYear);

                /*
                 * Adding all the dates related to given month and year to
                 * Calendar. and checking day for each date. If it is Monday
                 * then increase the counter and print date.
                 */

                /*
                 * cal.get(Calendar.DAY_OF_WEEK) = 1 (Sunday)
                 * cal.get(Calendar.DAY_OF_WEEK) = 2 (Monday)
                 * cal.get(Calendar.DAY_OF_WEEK) = 3 (Tuesday)
                 * cal.get(Calendar.DAY_OF_WEEK) = 4 (Wednesday)
                 * cal.get(Calendar.DAY_OF_WEEK) = 5 (Thursday)
                 * cal.get(Calendar.DAY_OF_WEEK) = 6 (Friday)
                 * cal.get(Calendar.DAY_OF_WEEK) = 7 (Saturday)
                 */
                int dayIndex=-1;
                switch (dayName) {
                    case ConstantData.SUNDAY:
                        dayIndex=1;
                        break;
                    case ConstantData.MONDAY:
                        dayIndex=2;
                        break;
                    case ConstantData.TUESDAY:
                        dayIndex=3;
                        break;
                    case ConstantData.WEDNESDAY:
                        dayIndex=4;
                        break;
                    case ConstantData.THURESDAY:
                        dayIndex=5;
                        break;
                    case ConstantData.FRIDAY:
                        dayIndex=6;
                        break;
                    case ConstantData.SATURDAY:
                        dayIndex=7;
                        break;
                    default:
                        break;
                }
                for (int i = 1; i <= days; i++) {

                    // Adding day of month in Calendar.
                    cal.set(Calendar.DAY_OF_MONTH, i);


                    if (cal.get(Calendar.DAY_OF_WEEK) == dayIndex) {
                        date = cal.getTime();
                        myDate = format.format(date);
                        dates.add(myDate);
                    }
                }
            } else {
                System.out.println("Month name should be valid and Year should be 4 digit long.");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return dates;
    }

    public static int[] getNumberofDaysAndMonthNumberByMonthName(String monthName, String year) {

        int dayAndMonthNumber[] = new int[2];
        int days = 0;
        int monthNumber = 0;
        switch (monthName) {
            case "JANUARY":
            case "JAN":
                days = 31;
                monthNumber = 0;
                break;
            case "FEBRUARY":
            case "FEB":
                if (isLeapYear(year)) {
                    days = 29;
                } else {
                    days = 28;
                }
                monthNumber = 1;
                break;
            case "MARCH":
            case "MAR":
                days = 31;
                monthNumber = 2;
                break;
            case "APRIL":
            case "APR":
                days = 30;
                monthNumber = 3;
                break;
            case "MAY":
                days = 31;
                monthNumber = 4;
                break;
            case "JUNE":
            case "JUN":
                days = 30;
                monthNumber = 5;
                break;
            case "JULY":
            case "JUL":
                days = 31;
                monthNumber = 6;
                break;
            case "AUGUST":
            case "AUG":
                days = 31;
                monthNumber = 7;
                break;
            case "SEPTEMBER":
            case "SEP":
                days = 30;
                monthNumber = 8;
                break;
            case "OCTOBER":
            case "OCT":
                days = 31;
                monthNumber = 9;
                break;
            case "NOVEMBER":
            case "NOV":
                days = 30;
                monthNumber = 10;
                break;
            case "DECEMBER":
            case "DEC":
                days = 31;
                monthNumber = 11;
                break;
        }
        dayAndMonthNumber[0] = days;
        dayAndMonthNumber[1] = monthNumber;
        return dayAndMonthNumber;
    }

    private static boolean isLeapYear(String year) {

        boolean result = false;
        // Converting String to int.
        int myYear = Integer.parseInt(year);
        if ((myYear % 4 == 0 && myYear % 100 != 0) || myYear % 400 == 0) {
            result = true;
        }
        return result;
    }

    private static boolean isValidMonth(String monthName) {

        boolean result = false;
        String[] fullMonthNames = { "JANUARY", "FEBRUARY", "MARCH", "APRIL","MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER" };
        String[] halfMonthNames = { "JAN", "FEB", "MAR", "APR","JUN","JUL", "AUG", "SEP","OCT", "NOV", "DEC" };
        // Converting Array to List
        List<String> fullMonths = new ArrayList<>(Arrays.asList(fullMonthNames));
        List<String> halfMonths = new ArrayList<>(Arrays.asList(halfMonthNames));
        if (fullMonths.contains(monthName) || halfMonths.contains(monthName)) {
            result = true;
        }
        return result;
    }

    private static boolean isValidYear(String year) {

        boolean result = false;
        // Regular expression that matches a String contains 4 digits.
        String pattern = "[0-9]{4}";
        if (year.matches(pattern)) {
            result = true;
        }
        return result;
    }
}
