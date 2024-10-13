package in.gabsinfo.usertimeslotgeofenceattendance.model;

import java.util.ArrayList;

public class MasterEmployeeModel {

    private String FirebaseKey;
    private String BranchCode;
    private String BranchName;
    private ArrayList<PersonModel> employeeList;

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

    public ArrayList<PersonModel> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(ArrayList<PersonModel> employeeList) {
        this.employeeList = employeeList;
    }
}
