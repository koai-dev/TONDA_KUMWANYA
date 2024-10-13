package in.gabsinfo.usertimeslotgeofenceattendance.service;

import android.app.IntentService;
import android.content.Intent;

import in.gabsinfo.usertimeslotgeofenceattendance.utils.WebServiceRest;

public class SendFCMNotificationService extends IntentService {

    public SendFCMNotificationService() {
        super("SendFCMNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getStringExtra("message");
        WebServiceRest.sentFCMNotification(message);
    }
}
