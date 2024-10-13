package in.gabsinfo.usertimeslotgeofenceattendance.model;

public class DataModel {

    private String Name;
    private long totalHours;
    private long totalMinutes;
    private String totalWorkHours= String.valueOf(0);

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public long getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(long totalHours) {
        this.totalHours = totalHours;
    }

    public long getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(long totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public String getTotalWorkHours() {
        return totalWorkHours;
    }

    public void setTotalWorkHours(String totalWorkHours) {
        this.totalWorkHours = totalWorkHours;
    }
}
