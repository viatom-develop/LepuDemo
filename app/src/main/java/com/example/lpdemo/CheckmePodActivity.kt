package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.checkmepod.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_checkme_pod.*

class CheckmePodActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "CheckmePodActivity"
    // Bluetooth.MODEL_CHECK_POD, Bluetooth.MODEL_CHECKME_POD_WPS
    private var model = Bluetooth.MODEL_CHECK_POD
    private var isStartRtTask = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkme_pod)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        get_info.setOnClickListener {
            if (isStartRtTask) {
                isStartRtTask = false
                BleServiceHelper.BleServiceHelper.stopRtTask(model)
            }
            BleServiceHelper.BleServiceHelper.checkmePodGetInfo(model)
        }
        get_list.setOnClickListener {
            if (isStartRtTask) {
                isStartRtTask = false
                BleServiceHelper.BleServiceHelper.stopRtTask(model)
            }
            BleServiceHelper.BleServiceHelper.checkmePodGetFileList(model)
        }
        start_rt_task.setOnClickListener {
            isStartRtTask = true
            if (BleServiceHelper.BleServiceHelper.isRtStop(model)) {
                BleServiceHelper.BleServiceHelper.startRtTask(model)
            }
        }
        stop_rt_task.setOnClickListener {
            isStartRtTask = false
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
        }
        bleState.observe(this) {
            if (it) {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart)
            .observe(this) {
                // start real time task
            }
        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop)
            .observe(this) {
                // stop real time task
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "GetFileListError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListProgress)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "GetFileListProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodFileList)
            .observe(this) {
                val data = it.data as ArrayList<Record>
                data_log.text = data.toString()
                Toast.makeText(this, "${data.size}", Toast.LENGTH_SHORT).show()
                // data.temp：unit ℃
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodRtDataError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "RtDataError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodRtData)
            .observe(this) {
                val data = it.data as RtData
                tv_oxy.text = "${data.param.spo2}"
                tv_pr.text = "${data.param.pr}"
                tv_pi.text = "${data.param.pi}"
                tv_temp.text = "${data.param.temp}"
                data_log.text = "$data"
                // data.param.temp：unit ℃
                // data.param.oxyState：0（Blood oxygen cable is not inserted），1（Insert the blood oxygen cable but not the finger），2（Finger inserted）
                // data.param.tempState：0（Temperature cable is not inserted），1（Temperature cable is inserted）
                // data.param.batteryState：0（no charge），1（charging），2（charging complete），3（low battery）
                // data.param.battery：0-100
                // data.param.runStatus：0（idle），1（preparing），2（measuring）
            }

    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // 蓝牙状态 Ble.State
        Log.d(TAG, "model $model, state: $state")

        _bleState.value = state == Ble.State.CONNECTED
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

}