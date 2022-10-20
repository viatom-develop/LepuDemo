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
import com.lepu.blepro.ext.LemData
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_lem.*

class LemActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "LemActivity"
    private val model = Bluetooth.MODEL_LEM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lem)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.lemGetInfo(model)
        }
        get_battery.setOnClickListener {
            BleServiceHelper.BleServiceHelper.lemGetBattery(model)
        }
        heat_switch.setOnClickListener {
            // true：Heating mode on，false：Heating mode off
            BleServiceHelper.BleServiceHelper.lemHeatMode(model, true)
        }
        set_time.setOnClickListener {
            // Constant.LemMassageTime
            BleServiceHelper.BleServiceHelper.lemMassageTime(model, Constant.LemMassageTime.MIN_10)
        }
        set_mode.setOnClickListener {
            // Constant.LemMassageMode
            BleServiceHelper.BleServiceHelper.lemMassageMode(model, Constant.LemMassageMode.SOOTHING)
        }
        set_level.setOnClickListener {
            // 0-15
            BleServiceHelper.BleServiceHelper.lemMassageLevel(model, 10)
        }
        bleState.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemDeviceInfo)
            .observe(this) {
                val data = it.data as LemData
                data_log.text = "$data"
                // data.battery：1-100
                // data.heatMode：true（Heating mode on），false（Heating mode off）
                // data.massageLevel：0-15
                // data.massageMode：0-4
                // 0：Vitality mode(Constant.LemMassageMode.VITALITY)
                // 1：Dynamic mode(Constant.LemMassageMode.DYNAMIC)
                // 2：Thump mode(Constant.LemMassageMode.HAMMERING)
                // 3：Soothing mode(Constant.LemMassageMode.SOOTHING)
                // 4：Automatic mode(Constant.LemMassageMode.AUTOMATIC)
                // data.massageTime：0-2
                // 0：15 min(Constant.LemMassageTime.MIN_15)
                // 1：10min(Constant.LemMassageTime.MIN_10)
                // 2：5min(Constant.LemMassageTime.MIN_5)

            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemBattery)
            .observe(this) {
                val data = it.data as Int
                // 1-100
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetHeatMode)
            .observe(this) {
                val data = it.data as Boolean
                // true：Heating mode on，false：Heating mode off
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageLevel)
            .observe(this) {
                val data = it.data as Int
                // 0-15
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageTime)
            .observe(this) {
                val data = it.data as Int
                // 0-2
                // 0：15 min(Constant.LemMassageTime.MIN_15)
                // 1：10min(Constant.LemMassageTime.MIN_10)
                // 2：5min(Constant.LemMassageTime.MIN_5)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageMode)
            .observe(this) {
                val data = it.data as Int
                // 0-4
                // 0：Vitality mode(Constant.LemMassageMode.VITALITY)
                // 1：Dynamic mode(Constant.LemMassageMode.DYNAMIC)
                // 2：Thump mode(Constant.LemMassageMode.HAMMERING)
                // 3：Soothing mode(Constant.LemMassageMode.SOOTHING)
                // 4：Automatic mode(Constant.LemMassageMode.AUTOMATIC)
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