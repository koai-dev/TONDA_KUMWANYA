package in.gabsinfo.usertimeslotgeofenceattendance.model;

public class UserRegistrationInputModel {

    private String name;
    private String mobileNo;
    private String emailId;
    private String password;
    private String userType;
    private String userType_mobileNo;//Used for authentication purpose
    private String userType_emailId;//Used for authentication purpose
    private String registrationDate;
    private String isAdsBlock=String.valueOf(false);
    private String adsBlockPurchaseDateTime;
    private String profileImage;
    private String profileImageName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
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

    public String getUserType_mobileNo() {
        return userType_mobileNo;
    }

    public void setUserType_mobileNo(String userType_mobileNo) {
        this.userType_mobileNo = userType_mobileNo;
    }

    public String getUserType_emailId() {
        return userType_emailId;
    }

    public void setUserType_emailId(String userType_emailId) {
        this.userType_emailId = userType_emailId;
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
}
