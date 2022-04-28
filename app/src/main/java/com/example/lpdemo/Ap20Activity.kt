package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
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
    private val model = Bluetooth.MODEL_AP20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ap20)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        bleState.observe(this, {
            if (it) {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        })

        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ap20GetInfo(model)
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
            .observe(this, {
                val data = it.data as DeviceInfo
                data_log.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtOxyParam)
            .observe(this, {
                val data = it.data as RtOxyParam
                tv_oxy.text = data.spo2.toString()
                tv_pr.text = data.pr.toString()
                tv_pi.text = data.pi.toString()
                data_log.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtOxyWave)
            .observe(this, {
                val data = it.data as RtOxyWave

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathParam)
            .observe(this, {
                val data = it.data as RtBreathParam

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathWave)
            .observe(this, {
                val data = it.data as RtBreathWave

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20BatLevel)
            .observe(this, {
                val data = it.data as Int

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20GetConfigResult)
            .observe(this, {
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
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20SetConfigResult)
            .observe(this, {
                val data = it.data as SetConfigResult
                data_log.text = if (data.success) {
                    "Set config success"
                } else {
                    "Set config fail"
                }
            })
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