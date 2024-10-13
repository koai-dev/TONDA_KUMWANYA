package in.gabsinfo.usertimeslotgeofenceattendance.model;

import java.util.ArrayList;

public class LeaveMasterModel {

    private String financialMonth="Apr 2019";
    private ArrayList<LeaveTypeModel> leaveTypes;

    public String getFinancialMonth() {
        return financialMonth;
    }

    public void setFinancialMonth(String financialMonth) {
        this.financialMonth = financialMonth;
    }

    public ArrayList<LeaveTypeModel> getLeaveTypes() {
        return leaveTypes;
    }

    public void setLeaveTypes(ArrayList<LeaveTypeModel> leaveTypes) {
        this.leaveTypes = leaveTypes;
    }
}
