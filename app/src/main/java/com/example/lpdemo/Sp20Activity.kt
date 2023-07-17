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
import com.lepu.blepro.constants.Constant
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.sp20.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_sp20.*

class Sp20Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Sp20Activity"
    // Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS
    private var model = Bluetooth.MODEL_SP20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sp20)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        bleState.observe(this) {
            if (it) {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }

        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.sp20GetInfo(model)
        }
        get_battery.setOnClickListener {
            BleServiceHelper.BleServiceHelper.sp20GetBattery(model)
        }
        get_config.setOnClickListener {
            BleServiceHelper.BleServiceHelper.sp20GetConfig(model, Constant.Sp20ConfigType.LOW_OXY_THRESHOLD)
//            BleServiceHelper.BleServiceHelper.sp20GetConfig(model, Constant.Sp20ConfigType.LOW_HR_THRESHOLD)
//            BleServiceHelper.BleServiceHelper.sp20GetConfig(model, Constant.Sp20ConfigType.HIGH_HR_THRESHOLD)
        }
        set_config.setOnClickListener {
            BleServiceHelper.BleServiceHelper.sp20SetConfig(model, Constant.Sp20ConfigType.LOW_OXY_THRESHOLD, 99/*(85-99)*/)
//            BleServiceHelper.BleServiceHelper.sp20SetConfig(model, Constant.Sp20ConfigType.LOW_HR_THRESHOLD, 99/*(30-99)*/)
//            BleServiceHelper.BleServiceHelper.sp20SetConfig(model, Constant.Sp20ConfigType.HIGH_HR_THRESHOLD, 250/*(100-250)*/)
        }

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20DeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20RtParam)
            .observe(this) {
                val data = it.data as RtParam
                tv_oxy.text = "${data.spo2}"
                tv_pr.text = "${data.pr}"
                tv_pi.text = "${data.pi}"
                data_log.text = "$data"
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
                data_log.text = when (data.type) {
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
                data_log.text = if (data.success) {
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
                data_log.text = "$data"
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