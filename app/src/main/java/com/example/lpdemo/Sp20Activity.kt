package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivitySp20Binding
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.constants.Constant
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.sp20.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver

class Sp20Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Sp20Activity"
    // Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS
    private var model = Bluetooth.MODEL_SP20
    private lateinit var binding: ActivitySp20Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySp20Binding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        bleState.observe(this) {
            if (it) {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }

        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.sp20GetInfo(model)
        }
        binding.getBattery.setOnClickListener {
            BleServiceHelper.BleServiceHelper.sp20GetBattery(model)
        }
        binding.getConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.sp20GetConfig(model, Constant.Sp20ConfigType.LOW_OXY_THRESHOLD)
//            BleServiceHelper.BleServiceHelper.sp20GetConfig(model, Constant.Sp20ConfigType.LOW_HR_THRESHOLD)
//            BleServiceHelper.BleServiceHelper.sp20GetConfig(model, Constant.Sp20ConfigType.HIGH_HR_THRESHOLD)
        }
        binding.setConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.sp20SetConfig(model, Constant.Sp20ConfigType.LOW_OXY_THRESHOLD, 99/*(85-99)*/)
//            BleServiceHelper.BleServiceHelper.sp20SetConfig(model, Constant.Sp20ConfigType.LOW_HR_THRESHOLD, 99/*(30-99)*/)
//            BleServiceHelper.BleServiceHelper.sp20SetConfig(model, Constant.Sp20ConfigType.HIGH_HR_THRESHOLD, 250/*(100-250)*/)
        }

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20DeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20RtParam)
            .observe(this) {
                val data = it.data as RtParam
                binding.tvOxy.text = "${data.spo2}"
                binding.tvPr.text = "${data.pr}"
                binding.tvPi.text = "${data.pi}"
                binding.dataLog.text = "$data"
                // data.spo2：0%-100%（0：invalid）
                // data.pr：0-511bpm（0：invalid）
                // data.pi：0%-25.5%（0：invalid）
                // data.battery：0-3（0：0%-25%，1：25%-50%，2：50%-75%，3：75%-100%）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20RtWave)
            .observe(this) {
                val data = it.data as RtWave
                // data.waveIntData：0-127
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20Battery)
            .observe(this) {
                val data = it.data as Int
                // 0-3（0：0-25%，1：25-50%，2：50-75%，3：75-100%）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20GetConfig)
            .observe(this) {
                val data = it.data as GetConfigResult
                binding.dataLog.text = when (data.type) {
                    Constant.Sp20ConfigType.LOW_OXY_THRESHOLD -> {
                        "Spo2 Lo (85-99) : ${data.data}"
                    }
                    Constant.Sp20ConfigType.LOW_HR_THRESHOLD -> {
                        "PR Lo (30-99) : ${data.data}"
                    }
                    Constant.Sp20ConfigType.HIGH_HR_THRESHOLD -> {
                        "PR Hi (100-250) : ${data.data}"
                    }
                    else -> ""
                }
                // data.type：
                // 2：Constant.Sp20ConfigType.LOW_OXY_THRESHOLD
                // 3：Constant.Sp20ConfigType.LOW_HR_THRESHOLD
                // 4：Constant.Sp20ConfigType.HIGH_HR_THRESHOLD
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20SetConfig)
            .observe(this) {
                val data = it.data as SetConfigResult
                binding.dataLog.text = if (data.success) {
                    "Set config success"
                } else {
                    "Set config fail"
                }
                // data.type：
                // 2：Constant.Sp20ConfigType.LOW_OXY_THRESHOLD
                // 3：Constant.Sp20ConfigType.LOW_HR_THRESHOLD
                // 4：Constant.Sp20ConfigType.HIGH_HR_THRESHOLD
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20TempData)
            .observe(this) {
                val data = it.data as TempResult
                binding.dataLog.text = "$data"
                // data.result：0（normal），1（low），2（high）
                // data.unit：0（℃），1（℉）
            }
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // Ble.State
        Log.d(TAG, "model $model, state: $state")

        _bleState.value = state == Ble.State.CONNECTED
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

}