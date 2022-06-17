package com.example.lpdemo.update;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class NotificationActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
//        // If this activity is the root activity of the task, the app is not running
//        if (isTaskRoot()) {
//            // Start the app before finishing
//            final Intent intent = new Intent(this, DeviceManageActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtras(getIntent().getExtras()); // copy all extras
//            startActivity(intent);
//        }

        // Now finish, which will drop you to the activity at which you were at the top of the task stack
        finish();


    }
}
