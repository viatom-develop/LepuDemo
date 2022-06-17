package com.example.lpdemo;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lpdemo.update.DfuService;
import com.example.lpdemo.update.PickUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class UpdateActivity extends Activity {
    String path;
    String dfu_macAddress = "";//通过蓝牙连接可以获取，或者nRf Connect查看
    String mBluetoothServiceName = "";//蓝牙名
    public static String PREFS_NAME = "update_succ_macAddress";
    /**
     * 请求打开蓝牙
     */
    private static final int REQUEST_ENABLE_BLUETOOTH = 100;
    LinearLayout updateSuccessLl;
    TextView duringUpgradeTv;
    TextView versionTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        dfu_macAddress = getIntent().getStringExtra("macAddr");
        mBluetoothServiceName = getIntent().getStringExtra("bleName");
        updateSuccessLl = findViewById(R.id.update_success_ll);
        duringUpgradeTv = findViewById(R.id.during_upgrade_tv);
        versionTv = findViewById(R.id.version_tv);
        Button backMainBtn = findViewById(R.id.back_main_btn);
        backMainBtn.setOnClickListener(v -> {
            finish();
        });
        startUpdate(StartActivity.Companion.getUpdatePath());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();

            if (uri == null)
                return;
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();

            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = PickUtils.getPath(this, uri);

            }
//            Log.d("path======",path);
            File file = new File(path);
            Log.d("path======", String.valueOf(file.length()));

        }
    }

    private void startUpdate(int raw) {
        final DfuServiceInitiator starter = new DfuServiceInitiator(dfu_macAddress)//mac地址
                .setDeviceName(mBluetoothServiceName)//名字
                .setKeepBond(true);


// If you want to have experimental buttonless DFU feature supported call additionally:
        starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
// but be aware of this: https://devzone.nordicsemi.com/question/100609/sdk-12-bootloader-erased-after-programming/
// and other issues related to this experimental service.

// Init packet is required by Bootloader/DFU from SDK 7.0+ if HEX or BIN file is given above.
// In case of a ZIP file, the init packet (a DAT file) must be included inside the ZIP file.
//            if (mFileType == DfuService.TYPE_AUTO)
        starter.setZip(raw);
//            else {
//                starter.setBinOrHex(mFileType, mFileStreamUri, mFilePath).setInitFile(mInitFileStreamUri, mInitFilePath);
//            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            starter.setForeground(false);
            starter.setDisableNotification(true);
        }

        final DfuServiceController controller = starter.start(this, DfuService.class);
// You may use the controller to pause, resume or abort the DFU process.


//            DfuServiceInitiator.createDfuNotificationChannel(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, dfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, dfuProgressListener);

    }

    private final DfuProgressListener dfuProgressListener = new DfuProgressListener() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
//          progressBar.setIndeterminate(true);
//          mTextPercentage.setText(R.string.dfu_status_connecting);
            Log.i("TEST", "onDeviceConnecting: " + deviceAddress);
        }

        @Override
        public void onDeviceConnected(String deviceAddress) {
            Log.i("TEST", "onDeviceConnected: " + deviceAddress);
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
//          progressBar.setIndeterminate(true);
//          mTextPercentage.setText(R.string.dfu_status_starting);
            Log.i("TEST", "onDfuProcessStarting: " + deviceAddress);


        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            Log.i("TEST", "onDfuProcessStarted: " + deviceAddress);
        }

        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            Log.i("TEST", "onEnablingDfuMode: " + deviceAddress);
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            Log.i("TEST", "onProgressChanged: " + deviceAddress + "百分比" + percent + ",speed "
                    + speed + ",avgSpeed " + avgSpeed + ",currentPart " + currentPart
                    + ",partTotal " + partsTotal);
            duringUpgradeTv.setVisibility(View.VISIBLE);
//            Toast.makeText(MainActivity.this,"升级进度：" + percent + "%",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            Log.i("TEST", "onFirmwareValidating: " + deviceAddress);
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            Log.i("TEST", "onDeviceDisconnecting: " + deviceAddress);
        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            Log.i("TEST", "onDeviceDisconnected: " + deviceAddress);
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            Log.i("TEST", "onDfuCompleted: " + deviceAddress);
            duringUpgradeTv.setVisibility(View.GONE);
            updateSuccessLl.setVisibility(View.VISIBLE);
            //2.定义一个只允许本程序访问的SharedPreFerences对象
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            //3.生成一个保存编辑变量
            SharedPreferences.Editor editor = settings.edit();
            //4.添加要保存的键值和真值
            editor.putString(dfu_macAddress, StartActivity.Companion.getVersion());
            //5.提交数据保存
            editor.commit();
            versionTv.setText(StartActivity.Companion.getVersion());
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            Log.i("TEST", "onDfuAborted: " + deviceAddress);

        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            Log.i("TEST", "onError: " + deviceAddress + ",message:" + message);
            duringUpgradeTv.setVisibility(View.VISIBLE);
            duringUpgradeTv.setText(getString(R.string.update_successful));
            new AlertDialog.Builder(UpdateActivity.this)
                    .setMessage(R.string.upgrade_unsuccessful)
                    .setPositiveButton("Ok", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            }
                    ).show();
        }
    };
}

