package in.gabsinfo.usertimeslotgeofenceattendance.model;

public class AppModuleModel {

    private String ModuleName;
    private String ModuleDesc;
    private String ModuleColor;
    private int ModuleId;
    private String URL;
    private String ID;
    private int VersionCode;

    public String getModuleName() {
        return ModuleName;
    }

    public void setModuleName(String moduleName) {
        ModuleName = moduleName;
    }

    public String getModuleDesc() {
        return ModuleDesc;
    }

    public void setModuleDesc(String moduleDesc) {
        ModuleDesc = moduleDesc;
    }

    public String getModuleColor() {
        return ModuleColor;
    }

    public void setModuleColor(String moduleColor) {
        ModuleColor = moduleColor;
    }

    public int getModuleId() {
        return ModuleId;
    }

    public void setModuleId(int moduleId) {
        ModuleId = moduleId;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }


    public int getVersionCode() {
        return VersionCode;
    }

    public void setVersionCode(int versionCode) {
        VersionCode = versionCode;
    }
}
