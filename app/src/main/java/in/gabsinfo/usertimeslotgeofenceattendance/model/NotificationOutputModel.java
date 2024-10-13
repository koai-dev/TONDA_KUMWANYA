package in.gabsinfo.usertimeslotgeofenceattendance.model;

/**
 * Created by Tushar on 16/02/2018.
 */

public class NotificationOutputModel {

    private int success=0;
    private int failure=1;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }
}
