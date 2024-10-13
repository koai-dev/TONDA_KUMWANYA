package in.gabsinfo.usertimeslotgeofenceattendance.model;

public class NotesModel {

    private String firebaseKey;
    private String date;
    private String title;
    private String description;
    private boolean allowedToDelete=true;//We have added some fix notes programmatically which we have don't allow to edit or delete

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isAllowedToDelete() {
        return allowedToDelete;
    }

    public void setAllowedToDelete(boolean allowedToDelete) {
        this.allowedToDelete = allowedToDelete;
    }
}
