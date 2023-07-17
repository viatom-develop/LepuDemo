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
import com.lepu.blepro.ext.ap20.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_ap20.*

class Ap20Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Ap20Activity"
    // MODEL_AP20, MODEL_AP20_WPS
    private var model = Bluetooth.MODEL_AP20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ap20)
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
            BleServiceHelper.BleServiceHelper.ap20GetInfo(model)
        }
        get_battery.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ap20GetBattery(model)
        }
        get_config.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ap20GetConfig(model, Constant.Ap20ConfigType.BACK_LIGHT)
//            BleServiceHelper.BleServiceHelper.ap20GetConfig(model, Constant.Ap20ConfigType.ALARM_SWITCH)
//            BleServiceHelper.BleServiceHelper.ap20GetConfig(model, Constant.Ap20ConfigType.LOW_OXY_THRESHOLD)
//            BleServiceHelper.BleServiceHelper.ap20GetConfig(model, Constant.Ap20ConfigType.LOW_HR_THRESHOLD)
//            BleServiceHelper.BleServiceHelper.ap20GetConfig(model, Constant.Ap20ConfigType.HIGH_HR_THRESHOLD)
        }
        set_config.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ap20SetConfig(model, Constant.Ap20ConfigType.ALARM_SWITCH, 0/*off*/)
//            BleServiceHelper.BleServiceHelper.ap20SetConfig(model, Constant.Ap20ConfigType.ALARM_SWITCH, 1/*on*/)
//            BleServiceHelper.BleServiceHelper.ap20SetConfig(model, Constant.Ap20ConfigType.BACK_LIGHT, 5/*(0-5)*/)
//            BleServiceHelper.BleServiceHelper.ap20SetConfig(model, Constant.Ap20ConfigType.LOW_OXY_THRESHOLD, 99/*(85-99)*/)
//            BleServiceHelper.BleServiceHelper.ap20SetConfig(model, Constant.Ap20ConfigType.LOW_HR_THRESHOLD, 99/*(30-99)*/)
//            BleServiceHelper.BleServiceHelper.ap20SetConfig(model, Constant.Ap20ConfigType.HIGH_HR_THRESHOLD, 250/*(100-250)*/)
        }

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20DeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtOxyParam)
            .observe(this) {
                val data = it.data as RtOxyParam
                tv_oxy.text = "${data.spo2}"
                tv_pr.text = "${data.pr}"
                tv_pi.text = "${data.pi}"
                // data.spo2：0%-100%（0：invalid）
                // data.pr：0-511bpm（0：invalid）
                // data.pi：0%-25.5%（0：invalid）
                // data.battery：0-3（0：0%-25%，1：25%-50%，2：50%-75%，3：75%-100%）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtOxyWave)
            .observe(this) {
                val data = it.data as RtOxyWave
                // data.waveIntData：0-127
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathParam)
            .observe(this) {
                val data = it.data as RtBreathParam
                // data.rr：respiratory rate（6-60bpm, 0：invalid）
                // data.sign：0(normal breathing), 1(no breathing)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathWave)
            .observe(this) {
                val data = it.data as RtBreathWave

            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20BatLevel)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "battery level : $data (0:0-25%,1:25-50%,2:50-75%,3:75-100%)"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20GetConfigResult)
            .observe(this) {
                val data = it.data as GetConfigResult
                data_log.text = when (data.type) {
                    Constant.Ap20ConfigType.BACK_LIGHT -> {
                        "Backlight level (0-5) : ${data.data}"
                    }
                    Constant.Ap20ConfigType.ALARM_SWITCH -> {
                        if (data.data == 1) {
                            "Alarm : on"
                        } else {
                            "Alarm : off"
                        }
                    }
                    Constant.Ap20ConfigType.LOW_OXY_THRESHOLD -> {
                        "Spo2 Lo (85-99) : ${data.data}"
                    }
                    Constant.Ap20ConfigType.LOW_HR_THRESHOLD -> {
                        "PR Lo (30-99) : ${data.data}"
                    }
                    Constant.Ap20ConfigType.HIGH_HR_THRESHOLD -> {
                        "PR Hi (100-250) : ${data.data}"
                    }
                    else -> ""
                }
                // data.type：
                // 0：Constant.Ap20ConfigType.BACK_LIGHT
                // 1：Constant.Ap20ConfigType.ALARM_SWITCH
                // 2：Constant.Ap20ConfigType.LOW_OXY_THRESHOLD
                // 3：Constant.Ap20ConfigType.LOW_HR_THRESHOLD
                // 4：Constant.Ap20ConfigType.HIGH_HR_THRESHOLD
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20SetConfigResult)
            .observe(this) {
                val data = it.data as SetConfigResult
                data_log.text = if (data.success) {
                    "Set config success"
                } else {
                    "Set config fail"
                }
                // data.type：
                // 0：Constant.Ap20ConfigType.BACK_LIGHT
                // 1：Constant.Ap20ConfigType.ALARM_SWITCH
                // 2：Constant.Ap20ConfigType.LOW_OXY_THRESHOLD
                // 3：Constant.Ap20ConfigType.LOW_HR_THRESHOLD
                // 4：Constant.Ap20ConfigType.HIGH_HR_THRESHOLD
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