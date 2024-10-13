package in.gabsinfo.usertimeslotgeofenceattendance.model;

import java.util.ArrayList;

public class HolidayMasterModel {

    private String FirebaseKey;
    private String BranchCode;
    private String BranchName;
    private ArrayList<HolidayModel> holidayModelArrayList;

    public String getFirebaseKey() {
        return FirebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        FirebaseKey = firebaseKey;
    }

    public String getBranchCode() {
        return BranchCode;
    }

    public void setBranchCode(String branchCode) {
        BranchCode = branchCode;
    }

    public String getBranchName() {
        return BranchName;
    }

    public void setBranchName(String branchName) {
        BranchName = branchName;
    }

    public ArrayList<HolidayModel> getHolidayModelArrayList() {
        return holidayModelArrayList;
    }

    public void setHolidayModelArrayList(ArrayList<HolidayModel> holidayModelArrayList) {
        this.holidayModelArrayList = holidayModelArrayList;
    }
}
