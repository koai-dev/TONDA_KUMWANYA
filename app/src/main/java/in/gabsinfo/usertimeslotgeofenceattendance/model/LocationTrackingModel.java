package in.gabsinfo.usertimeslotgeofenceattendance.model;

public class LocationTrackingModel {

    private double latitude;
    private double longitude;
    private long time;
    private String deviceInfo;

    public LocationTrackingModel(double latitude, double longitude, long time, String deviceInfo) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.deviceInfo = deviceInfo;
    }

    public LocationTrackingModel(){

    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
