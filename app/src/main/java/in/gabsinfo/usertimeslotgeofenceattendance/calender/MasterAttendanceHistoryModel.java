package in.gabsinfo.usertimeslotgeofenceattendance.calender;

public class MasterAttendanceHistoryModel {

    private String Id;
    private String Date;
    private int Day;
    private boolean IsTransactionAdded = false;
    private String Type ;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public int getDay() {
        return Day;
    }

    public void setDay(int day) {
        Day = day;
    }

    public boolean isTransactionAdded() {
        return IsTransactionAdded;
    }

    public void setTransactionAdded(boolean transactionAdded) {
        IsTransactionAdded = transactionAdded;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
