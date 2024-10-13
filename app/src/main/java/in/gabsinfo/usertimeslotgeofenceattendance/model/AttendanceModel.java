package in.gabsinfo.usertimeslotgeofenceattendance.model;

import java.io.Serializable;

public class AttendanceModel implements Serializable {

    private String firebaseKey;
    private String punchDate;
    private long punchDateInMillis;
    private String punchInTime;
    private String punchOutTime;;
    private String totalWorkingHours;
    private double presentDay;

    private String punchInLocationCode;
    private String punchOutLocationCode;

    private double punchInLatitude;
    private double punchInLongitude;
    private double punchOutLatitude;
    private double punchOutLongitude;

    private String punchInBy;
    private String punchOutBy;
    private double overTimeInMinutes=0;

    private String personName;
    private String personMobileNo;
    private String personFirebaseKey;


    /**
     * Used For Calender
     */
    private String Id;
    private String Date;
    private int Day;
    private boolean IsTransactionAdded = false;
    private String Type;
    private String Description;//User for public holidays


    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getPunchDate() {
        return punchDate;
    }

    public void setPunchDate(String punchDate) {
        this.punchDate = punchDate;
    }

    public long getPunchDateInMillis() {
        return punchDateInMillis;
    }

    public void setPunchDateInMillis(long punchDateInMillis) {
        this.punchDateInMillis = punchDateInMillis;
    }

    public String getPunchInTime() {
        return punchInTime;
    }

    public void setPunchInTime(String punchInTime) {
        this.punchInTime = punchInTime;
    }

    public String getPunchOutTime() {
        return punchOutTime;
    }

    public void setPunchOutTime(String punchOutTime) {
        this.punchOutTime = punchOutTime;
    }

    public double getPresentDay() {
        return presentDay;
    }

    public void setPresentDay(double presentDay) {
        this.presentDay = presentDay;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonMobileNo() {
        return personMobileNo;
    }

    public void setPersonMobileNo(String personMobileNo) {
        this.personMobileNo = personMobileNo;
    }

    public String getPersonFirebaseKey() {
        return personFirebaseKey;
    }

    public void setPersonFirebaseKey(String personFirebaseKey) {
        this.personFirebaseKey = personFirebaseKey;
    }

    public String getTotalWorkingHours() {
        return totalWorkingHours;
    }

    public void setTotalWorkingHours(String totalWorkingHours) {
        this.totalWorkingHours = totalWorkingHours;
    }

    public String getPunchInLocationCode() {
        return punchInLocationCode;
    }

    public void setPunchInLocationCode(String punchInLocationCode) {
        this.punchInLocationCode = punchInLocationCode;
    }

    public String getPunchOutLocationCode() {
        return punchOutLocationCode;
    }

    public void setPunchOutLocationCode(String punchOutLocationCode) {
        this.punchOutLocationCode = punchOutLocationCode;
    }

    public double getPunchInLatitude() {
        return punchInLatitude;
    }

    public void setPunchInLatitude(double punchInLatitude) {
        this.punchInLatitude = punchInLatitude;
    }

    public double getPunchInLongitude() {
        return punchInLongitude;
    }

    public void setPunchInLongitude(double punchInLongitude) {
        this.punchInLongitude = punchInLongitude;
    }

    public double getPunchOutLatitude() {
        return punchOutLatitude;
    }

    public void setPunchOutLatitude(double punchOutLatitude) {
        this.punchOutLatitude = punchOutLatitude;
    }

    public double getPunchOutLongitude() {
        return punchOutLongitude;
    }

    public void setPunchOutLongitude(double punchOutLongitude) {
        this.punchOutLongitude = punchOutLongitude;
    }

    public String getPunchInBy() {
        return punchInBy;
    }

    public void setPunchInBy(String punchInBy) {
        this.punchInBy = punchInBy;
    }

    public String getPunchOutBy() {
        return punchOutBy;
    }

    public void setPunchOutBy(String punchOutBy) {
        this.punchOutBy = punchOutBy;
    }

    public double getOverTimeInMinutes() {
        return overTimeInMinutes;
    }

    public void setOverTimeInMinutes(double overTimeInMinutes) {
        this.overTimeInMinutes = overTimeInMinutes;
    }

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

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
