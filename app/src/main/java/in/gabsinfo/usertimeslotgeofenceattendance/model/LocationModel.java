package in.gabsinfo.usertimeslotgeofenceattendance.model;

import java.io.Serializable;
import java.util.ArrayList;

public class LocationModel implements Serializable {

    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String code;
    private int radius;
    private String firebaseKey;
    private boolean selected=false;

    public LocationModel(double latitude,double longitude,String code){
        this.latitude=latitude;
        this.longitude=longitude;
        this.code=code;
    }
    public LocationModel(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

}
