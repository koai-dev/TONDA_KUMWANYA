package in.gabsinfo.usertimeslotgeofenceattendance.model;

import java.io.Serializable;

public class ShopTimingModel implements Serializable {

    private String day;
    private String fromTime;
    private String toTime;
    private String hoursForFullDay;
    private boolean halfDayAllow;
    private String hoursForHalfDay;

    public ShopTimingModel(String day, String fromTime, String toTime,String hoursForFullDay,
                           boolean halfDayAllow,String hoursForHalfDay) {
        this.day=day;
        this.fromTime=fromTime;
        this.toTime=toTime;
        this.hoursForFullDay=hoursForFullDay;;
        this.halfDayAllow=halfDayAllow;;
        this.hoursForHalfDay=hoursForHalfDay;;
    }

    public ShopTimingModel(){
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getHoursForFullDay() {
        return hoursForFullDay;
    }

    public void setHoursForFullDay(String hoursForFullDay) {
        this.hoursForFullDay = hoursForFullDay;
    }

    public boolean isHalfDayAllow() {
        return halfDayAllow;
    }

    public void setHalfDayAllow(boolean halfDayAllow) {
        this.halfDayAllow = halfDayAllow;
    }

    public String getHoursForHalfDay() {
        return hoursForHalfDay;
    }

    public void setHoursForHalfDay(String hoursForHalfDay) {
        this.hoursForHalfDay = hoursForHalfDay;
    }
}
