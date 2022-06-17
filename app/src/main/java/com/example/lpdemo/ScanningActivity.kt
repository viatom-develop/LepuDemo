
package com.example.lpdemo

import android.Manifest
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lpdemo.utils.DeviceAdapter
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.Bluetooth.MODEL_PC66B
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.rcv
import kotlinx.android.synthetic.main.activity_scanning.*

class ScanningActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "ScanningActivity"
    var macAddr:String = ""
    var bleName:String =""
    private lateinit var dialog: ProgressDialog

    private val models = intArrayOf(
        Bluetooth.MODEL_PC60FW,
        MODEL_PC66B
    )

    private val permission = arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )

    private var list = arrayListOf<Bluetooth>()
    private var adapter = DeviceAdapter(R.layout.scanning_item, list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)
        needPermission()
        initService()


        initView()
        initEventBus()
    }

    override fun onResume() {
        super.onResume()
        BleServiceHelper.BleServiceHelper.startScan(models)
    }
    private fun needPermission(){
        for (p in permission) {
            if (ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, permission, 0)
                break
            }
        }
        checkBt()
    }

    private fun initService() {
        if (BleServiceHelper.BleServiceHelper.checkService()) {
            // BleService already init
        } else {
            BleServiceHelper.BleServiceHelper.initService(application, BleSO.getInstance(application))
        }
    }

    private fun initView() {

        dialog = ProgressDialog(this)


        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            rcv.layoutManager = this
        }
        rcv.adapter = adapter
        adapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as Bluetooth).let {
                // set interface before connect
                BleServiceHelper.BleServiceHelper.setInterfaces(it.model)
                // add observer(ble state)
                lifecycle.addObserver(BIOL(this, intArrayOf(it.model)))
                // stop scan before connect
                BleServiceHelper.BleServiceHelper.stopScan()
                // connect
                BleServiceHelper.BleServiceHelper.connect(applicationContext, it.model, it.device)
                macAddr  = it.macAddr
                bleName = it.name
                dialog.show()
            }
        }
    }
    private fun checkBt() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!adapter.isEnabled) {
            if (adapter.enable()) {
                Toast.makeText(this, "Bluetooth open successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth open failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun initEventBus() {
        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit)
            .observe(this) {
                // BleService init success
                Log.d(TAG, "EventServiceConnectedAndInterfaceInit")
            }
        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this) {
                // scan result
                if (BluetoothController.getDevices().isNotEmpty()) {
                    scanning_ll.visibility = View.GONE
                }
                adapter.setList(BluetoothController.getDevices())
                adapter.notifyDataSetChanged()
                Log.d(TAG, "EventDeviceFound")
            }
        //--------------------pc80b,pc102,pc60fw,pc68b,pod1w,pc300--------------------
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady)
            .observe(this) {
                dialog.dismiss()
                // connect success
                Log.d(TAG, "EventBleDeviceReady")
                when (it) {
                    MODEL_PC66B -> {
                        val intent = Intent(this, UpdateActivity::class.java)
                        intent.putExtra("macAddr", macAddr)
                        intent.putExtra("bleName", bleName)
                        startActivity(intent)
                        finish()
                    }
                }
            }

    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // Ble.State
        Log.d(TAG, "model $model, state: $state")
        _bleState.value = state == Ble.State.CONNECTED
        Log.d(TAG, "bleState $bleState")
    }
}