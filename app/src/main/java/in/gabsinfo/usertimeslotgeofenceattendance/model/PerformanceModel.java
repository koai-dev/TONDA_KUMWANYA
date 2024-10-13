package in.gabsinfo.usertimeslotgeofenceattendance.model;

public class PerformanceModel {
    private String date;
    private int fullDay=0;
    private int halfDay=0;
    private int outPending=0;
    private int presentButLeave=0;
    private int totalMinutes =0;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getFullDay() {
        return fullDay;
    }

    public void setFullDay(int fullDay) {
        this.fullDay = fullDay;
    }

    public int getHalfDay() {
        return halfDay;
    }

    public void setHalfDay(int halfDay) {
        this.halfDay = halfDay;
    }

    public int getOutPending() {
        return outPending;
    }

    public void setOutPending(int outPending) {
        this.outPending = outPending;
    }

    public int getPresentButLeave() {
        return presentButLeave;
    }

    public void setPresentButLeave(int presentButLeave) {
        this.presentButLeave = presentButLeave;
    }


    public int getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }
}
