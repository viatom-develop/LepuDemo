package com.example.lpdemo

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lpdemo.utils.DataController
import com.example.lpdemo.utils.DeviceAdapter
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "MainActivity"

    private lateinit var dialog: ProgressDialog

    private val permission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN)

    private var list = arrayListOf<Bluetooth>()
    private var adapter = DeviceAdapter(R.layout.device_item, list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        needPermission()
        initService()
        initView()
        initEventBus()
    }

    private fun needPermission(){
        for (p in permission) {
            if (ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, permission, 0)
                break
            }
        }
    }

    private fun initService() {
        if (BleServiceHelper.BleServiceHelper.checkService()) {
            // 蓝牙服务已经初始化
        } else {
            BleServiceHelper.BleServiceHelper.initService(application, BleSO.getInstance(application))
        }
    }

    private fun initView() {

        dialog = ProgressDialog(this)

        scan.setOnClickListener {
            BleServiceHelper.BleServiceHelper.startScan()
        }
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            rcv.layoutManager = this
        }
        rcv.adapter = adapter
        adapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as Bluetooth).let {
                // 连接前先设置interface
                BleServiceHelper.BleServiceHelper.setInterfaces(it.model)
                // 添加监听订阅
                lifecycle.addObserver(BIOL(this, intArrayOf(it.model)))
                // 停止扫描
                BleServiceHelper.BleServiceHelper.stopScan()
                // 连接
                BleServiceHelper.BleServiceHelper.connect(this, it.model, it.device)

                dialog.show()
            }
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit)
            .observe(this, {
                // 蓝牙服务初始化成功
                Log.d(TAG, "EventServiceConnectedAndInterfaceInit")
            })
        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this, {
                // 扫到设备
                adapter.setList(BluetoothController.getDevices())
                adapter.notifyDataSetChanged()
                Log.d(TAG, "EventDeviceFound")
            })
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady)
            .observe(this, {
                dialog.dismiss()
                // 连接成功
                Log.d(TAG, "EventBleDeviceReady")
                if (it == Bluetooth.MODEL_PC100) {
                    startActivity(Intent(this, Pc102Activity::class.java))
                } else if (it == Bluetooth.MODEL_PC80B) {
                    startActivity(Intent(this, Pc80bActivity::class.java))
                }
                finish()
            })
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // 蓝牙状态 Ble.State
        Log.d(TAG, "model $model, state: $state")
        _bleState.value = state == Ble.State.CONNECTED
        Log.d(TAG, "bleState ${bleState}")
    }
}