package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityCheckmePodBinding
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

class CheckmePodActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "CheckmePodActivity"
    // Bluetooth.MODEL_CHECK_POD, Bluetooth.MODEL_CHECKME_POD_WPS
    private var model = Bluetooth.MODEL_CHECK_POD
    private lateinit var binding: ActivityCheckmePodBinding
    private var isStartRtTask = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckmePodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        binding.getInfo.setOnClickListener {
            if (isStartRtTask) {
                isStartRtTask = false
                BleServiceHelper.BleServiceHelper.stopRtTask(model)
            }
            BleServiceHelper.BleServiceHelper.checkmePodGetInfo(model)
        }
        binding.getList.setOnClickListener {
            if (isStartRtTask) {
                isStartRtTask = false
                BleServiceHelper.BleServiceHelper.stopRtTask(model)
            }
            BleServiceHelper.BleServiceHelper.checkmePodGetFileList(model)
        }
        binding.startRtTask.setOnClickListener {
            isStartRtTask = true
            if (BleServiceHelper.BleServiceHelper.isRtStop(model)) {
                BleServiceHelper.BleServiceHelper.startRtTask(model)
            }
        }
        binding.stopRtTask.setOnClickListener {
            isStartRtTask = false
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
        }
        bleState.observe(this) {
            if (it) {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
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
                binding.dataLog.text = "$data"
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "GetFileListError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "GetFileListProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodFileList)
            .observe(this) {
                val data = it.data as ArrayList<Record>
                binding.dataLog.text = data.toString()
                Toast.makeText(this, "${data.size}", Toast.LENGTH_SHORT).show()
                // data.temp：unit ℃
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodRtDataError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "RtDataError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodRtData)
            .observe(this) {
                val data = it.data as RtData
                binding.tvOxy.text = "${data.param.spo2}"
                binding.tvPr.text = "${data.param.pr}"
                binding.tvPi.text = "${data.param.pi}"
                binding.tvTemp.text = "${data.param.temp}"
                binding.dataLog.text = "$data"
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