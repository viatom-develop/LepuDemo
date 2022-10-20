package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.bpm.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_bpm.*

class BpmActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "BpmActivity"
    private val model = Bluetooth.MODEL_BPM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bpm)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bpmGetInfo(model)
        }
        get_rt_state.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bpmGetRtState(model)
        }
        get_file_list.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bpmGetFileList(model)
        }
        bleState.observe(this) {
            if (it) {
                bp_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                bp_ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmState)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "device state : ${getRtState(data)}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRtData)
            .observe(this) {
                val data = it.data as Int
                tv_ps.text = "$data"
                data_log.text = "real-time pressure : $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureResult)
            .observe(this) {
                val data = it.data as RecordData
                tv_sys.text = "${data.sys}"
                tv_dia.text = "${data.dia}"
                tv_pr_bp.text = "${data.pr}"
                data_log.text = "$data"
                // data.irregularHrFlag：Whether the heart rate is irregular
                // data.storeId：Record number
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureErrorResult)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "error result : ${getErrorResult(data)}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordData)
            .observe(this) {
                val data = it.data as RecordData
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordEnd)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "get file list end : $data"
            }
    }

    private fun getRtState(state: Int): String {
        return when (state) {
            0 -> "Time setting state"
            1 -> "Historical interface status"
            2 -> "Measurement status"
            3 -> "Measuring the pressurized state"
            4 -> "The flickering indication of the heart rate in deflating mode"
            5 -> "Measurement end state"
            6 -> "Standby interface/time interface"
            else -> ""
        }
    }

    private fun getErrorResult(errorCode: Int): String {
        return when (errorCode) {
            1 -> "Sensor vibration anomaly"
            2 -> "Not enough heart rate to be detected or blood pressure value to be calculated"
            3 -> "Measurement results are abnormal"
            4 -> "Cuff is too loose or air leakage(Pressure value less than 30mmHg in 10 seconds)"
            5 -> "The tube is blocked"
            6 -> "Large pressure fluctuations during measurement"
            7 -> "Pressure exceeds upper limit"
            8 -> "Calibration data is abnormal or uncalibrated"
            else -> ""
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