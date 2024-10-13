package in.gabsinfo.usertimeslotgeofenceattendance.model;

public class LeaveTypeModel {

    private String name;
    private int days=0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
