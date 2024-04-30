package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityLemBinding
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

class LemActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "LemActivity"
    private val model = Bluetooth.MODEL_LEM
    private lateinit var binding: ActivityLemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.lemGetInfo(model)
        }
        binding.getBattery.setOnClickListener {
            BleServiceHelper.BleServiceHelper.lemGetBattery(model)
        }
        binding.heatSwitch.setOnClickListener {
            // true：Heating mode on，false：Heating mode off
            BleServiceHelper.BleServiceHelper.lemHeatMode(model, true)
        }
        binding.setTime.setOnClickListener {
            // Constant.LemMassageTime
            BleServiceHelper.BleServiceHelper.lemMassageTime(model, Constant.LemMassageTime.MIN_10)
        }
        binding.setMode.setOnClickListener {
            // Constant.LemMassageMode
            BleServiceHelper.BleServiceHelper.lemMassageMode(model, Constant.LemMassageMode.SOOTHING)
        }
        binding.setLevel.setOnClickListener {
            // 0-15
            BleServiceHelper.BleServiceHelper.lemMassageLevel(model, 10)
        }
        bleState.observe(this) {
            if (it) {
                binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.bleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemDeviceInfo)
            .observe(this) {
                val data = it.data as LemData
                binding.dataLog.text = "$data"
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