package in.gabsinfo.usertimeslotgeofenceattendance.model;

import java.io.Serializable;
import java.util.ArrayList;

public class PersonModel implements Serializable {

    private String firebaseKey;
    private String profileImage;
    private String profileImageName;
    private String name;
    private String address;
    private String mobileNo;
    private String mobileDialerCode;
    private String countryCode;
    private String countryName;
    private String designation;
    private String workType;
    private int amount;
    private String emailId;
    private String dob;
    private String password;
    private String userType;
    private String currencySymbol;
    private String mappedLocations;

    private String deviceId;// Don't change key as its used in PhoneMappingRequestListFragment
    private String userType_mobileNo;//Used for authentication purpose
    private String userType_emailId;//Used for authentication purpose
    private boolean notifyAdminForPunchIn=false;
    private boolean notifyAdminForPunchOut=false;
    /**
     * This are used locally means on page lifecycle only .
     * Its used while marking attendance by User
     */
    // Nearest Location Latitude
    private String Latitude;
    // Nearest Location Longitude
    private String Longitude;
    private ArrayList<LatLong> latLong;
    private float radius;
    //Branch at which user has marked attendance - Geo-fence concept
    private String branchCode;
    private String branchName;
    private String registrationDate;
    private ArrayList<ShopTimingModel> TimeSlotList;

    //To track employee
    private boolean trackingEnable=false;
    private String trackingDeviceId;

    /**
     * We use this entities only for
     * checking users punch IN/OUT which are used in PresentEmployeesFragment
     */
    private String punchDate;
    private String punchInTime;
    private String punchOutTime;
    private double punchInLatitude;
    private double punchInLongitude;
    private double punchOutLatitude;
    private double punchOutLongitude;

    /**
     * Used only for TimeSheet module where we display user list with their monthly total hours
     */
    private String TotalMonthlyFilledHours=String.valueOf(0);

    private String isAdsBlock=String.valueOf(false);
    private String adsBlockPurchaseDateTime;

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getProfileImageName() {
        return profileImageName;
    }

    public void setProfileImageName(String profileImageName) {
        this.profileImageName = profileImageName;
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

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getMobileDialerCode() {
        return mobileDialerCode;
    }

    public void setMobileDialerCode(String mobileDialerCode) {
        this.mobileDialerCode = mobileDialerCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getMappedLocations() {
        return mappedLocations;
    }

    public void setMappedLocations(String mappedLocations) {
        this.mappedLocations = mappedLocations;
    }

    public String getUserType_mobileNo() {
        return userType_mobileNo;
    }

    public void setUserType_mobileNo(String userType_mobileNo) {
        this.userType_mobileNo = userType_mobileNo;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public ArrayList<LatLong> getLatLong() {
        return latLong;
    }

    public void setLatLong(ArrayList<LatLong> latLong) {
        this.latLong = latLong;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getUserType_emailId() {
        return userType_emailId;
    }

    public void setUserType_emailId(String userType_emailId) {
        this.userType_emailId = userType_emailId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public boolean isNotifyAdminForPunchIn() {
        return notifyAdminForPunchIn;
    }

    public void setNotifyAdminForPunchIn(boolean notifyAdminForPunchIn) {
        this.notifyAdminForPunchIn = notifyAdminForPunchIn;
    }

    public boolean isNotifyAdminForPunchOut() {
        return notifyAdminForPunchOut;
    }

    public void setNotifyAdminForPunchOut(boolean notifyAdminForPunchOut) {
        this.notifyAdminForPunchOut = notifyAdminForPunchOut;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }


    public String getIsAdsBlock() {
        return isAdsBlock;
    }

    public void setIsAdsBlock(String isAdsBlock) {
        this.isAdsBlock = isAdsBlock;
    }

    public String getAdsBlockPurchaseDateTime() {
        return adsBlockPurchaseDateTime;
    }

    public void setAdsBlockPurchaseDateTime(String adsBlockPurchaseDateTime) {
        this.adsBlockPurchaseDateTime = adsBlockPurchaseDateTime;
    }

    public String getTotalMonthlyFilledHours() {
        return TotalMonthlyFilledHours;
    }

    public void setTotalMonthlyFilledHours(String totalMonthlyFilledHours) {
        TotalMonthlyFilledHours = totalMonthlyFilledHours;
    }

    public String getPunchDate() {
        return punchDate;
    }

    public void setPunchDate(String punchDate) {
        this.punchDate = punchDate;
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

    public ArrayList<ShopTimingModel> getTimeSlotList() {
        return TimeSlotList;
    }

    public void setTimeSlotList(ArrayList<ShopTimingModel> timeSlotList) {
        TimeSlotList = timeSlotList;
    }

    public boolean isTrackingEnable() {
        return trackingEnable;
    }

    public void setTrackingEnable(boolean trackingEnable) {
        this.trackingEnable = trackingEnable;
    }

    public String getTrackingDeviceId() {
        return trackingDeviceId;
    }

    public void setTrackingDeviceId(String trackingDeviceId) {
        this.trackingDeviceId = trackingDeviceId;
    }
}
