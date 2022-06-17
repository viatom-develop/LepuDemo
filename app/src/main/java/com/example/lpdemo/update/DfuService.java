package com.example.lpdemo.update;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import no.nordicsemi.android.dfu.DfuBaseService;

public class DfuService extends DfuBaseService {


    public DfuService() {
    }

    @Nullable
    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        /*
         * As a target activity the NotificationActivity is returned, not the MainActivity. This is because
         * the notification must create a new task:
         *
         * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         *
         * when you press it. You can use NotificationActivity to check whether the new activity
         * is a root activity (that means no other activity was open earlier) or that some
         * other activity is already open. In the latter case the NotificationActivity will just be
         * closed. The system will restore the previous activity. However, if the application has been
         * closed during upload and you click the notification, a NotificationActivity will
         * be launched as a root activity. It will create and start the main activity and
         * terminate itself.
         *
         * This method may be used to restore the target activity in case the application
         * was closed or is open. It may also be used to recreate an activity history using
         * startActivities(...).
         */
        return NotificationActivity.class;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}