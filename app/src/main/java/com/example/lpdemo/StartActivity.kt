package com.example.lpdemo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main_update.*

class StartActivity: Activity() {
    companion object {
        //语言翻译，包名，也要修改
       val version ="v1.3.1.0"
        //https://cloud.viatomtech.com/80d/#/page1
        //https://cloud.viatomtech.com/80d/#/page2
        //https://cloud.viatomtech.com/80d/#/page3
        //https://cloud.viatomtech.com/80d/#/page4
        val uriPath = "https://cloud.viatomtech.com/80d/#/page5"
        val updatePath = R.raw.v1310tr_en
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_update)

        checkPermissions()
        initView()
    }

    private fun initView() {
        update_btn.setOnClickListener {
            startActivity(Intent(this, ScanningActivity::class.java))
        }
        use_btn.setOnClickListener {
            //https://cloud.viatomtech.com/80d/#/page1
            //https://cloud.viatomtech.com/80d/#/page2
            //https://cloud.viatomtech.com/80d/#/page3
            //https://cloud.viatomtech.com/80d/#/page4
            val uri = Uri.parse(uriPath)  //设置要操作的路径
            val it = Intent()
            it.setAction(Intent.ACTION_VIEW);  //设置要操作的Action
            it.setData(uri); //要设置的数据
            startActivity(it);   //执行跳转

        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionDeniedList: MutableList<String> = ArrayList()
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission)
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            ActivityCompat.requestPermissions(this, deniedPermissions, 12)
        }
    }
}