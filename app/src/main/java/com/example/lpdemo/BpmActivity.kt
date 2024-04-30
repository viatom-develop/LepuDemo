package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityBpmBinding
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

class BpmActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "BpmActivity"
    private val model = Bluetooth.MODEL_BPM
    private lateinit var binding: ActivityBpmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBpmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bpmGetInfo(model)
        }
        binding.getRtState.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bpmGetRtState(model)
        }
        binding.getFileList.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bpmGetFileList(model)
        }
        bleState.observe(this) {
            if (it) {
                binding.bpBleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.bpBleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmState)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "device state : ${getRtState(data)}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRtData)
            .observe(this) {
                val data = it.data as Int
                binding.tvPs.text = "$data"
                binding.dataLog.text = "real-time pressure : $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureResult)
            .observe(this) {
                val data = it.data as RecordData
                binding.tvSys.text = "${data.sys}"
                binding.tvDia.text = "${data.dia}"
                binding.tvPrBp.text = "${data.pr}"
                binding.dataLog.text = "$data"
                // data.irregularHrFlag：Whether the heart rate is irregular
                // data.storeId：Record number
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureErrorResult)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "error result : ${getErrorResult(data)}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordData)
            .observe(this) {
                val data = it.data as RecordData
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordEnd)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "get file list end : $data"
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