package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityAirbpBinding
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.airbp.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver

class AirBpActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "AirBpActivity"
    private val model = Bluetooth.MODEL_AIRBP
    private lateinit var binding: ActivityAirbpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAirbpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
        BleServiceHelper.BleServiceHelper.airBpGetConfig(model)
    }

    private fun initView() {
        binding.bleName.text = deviceName
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.airBpGetInfo(model)
        }
        binding.getBattery.setOnClickListener {
            BleServiceHelper.BleServiceHelper.airBpGetBattery(model)
        }
        binding.beepSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                BleServiceHelper.BleServiceHelper.airBpSetConfig(model, isChecked)
            }
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpGetBattery)
            .observe(this) {
                val data = it.data as Battery
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpGetInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpSetConfig)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "set config : $data"
                BleServiceHelper.BleServiceHelper.airBpGetConfig(model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpGetConfig)
            .observe(this) {
                val data = it.data as Boolean
                binding.beepSwitch.isChecked = data
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpRtResult)
            .observe(this) {
                val data = it.data as RtResult
                binding.tvSys.text = "${data.sys}"
                binding.tvDia.text = "${data.dia}"
                binding.tvPrBp.text = "${data.pr}"
                binding.dataLog.text = "$data\n" +
                        "状态码${data.stateCode}：\n${when (data.stateCode) {
                            0 -> "充气阶段"
                            1 -> "提示停止打气"
                            2 -> "测量中，不要打气"
                            3 -> "获得测量结果"
                            4 -> "返充气"
                            5 -> "测量失败，噪声太大，结果异常"
                            6 -> "测量失败，信号太弱，结果异常"
                            7 -> "漏气， 5s 内压力下降超过 100mmHg，报漏气，结果异常"
                            8 -> "堵塞， 5s 内压力下降小于 10mmHg，报堵塞，结果 异常"
                            9 -> "系统错误，结果异常"
                            10 -> "充气压力不足，未反充气，结果异常"
                            11 -> "收缩压超量程（高血压），结果异常"
                            12 -> "静态压超过 300mmHg，危险提示，停止打气"
                            13 -> "打气超时,5 分钟未打气(<5mmHg)，结果异常"
                            14 -> "获得测量结果，存在心率不齐"
                            15 -> "获得测量结果，存在弱干扰"
                            16 -> "用户主动放气，结果异常"
                            17 -> "打气超过 300mmHg，结束测量，结果异常"
                            else -> ""
                        }}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpRtData)
            .observe(this) {
                val data = it.data as Int
                binding.tvPs.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpRtState)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "当前运行状态${data}：\n${when (data) {
                    0 -> "充气阶段"
                    1 -> "提示停止打气"
                    2 -> "测量中，不要打气"
                    3 -> "获得测量结果"
                    4 -> "返充气"
                    5 -> "测量失败，噪声太大，结果异常"
                    6 -> "测量失败，信号太弱，结果异常"
                    7 -> "漏气， 5s 内压力下降超过 100mmHg，报漏气，结果异常"
                    8 -> "堵塞， 5s 内压力下降小于 10mmHg，报堵塞，结果 异常"
                    9 -> "系统错误，结果异常"
                    10 -> "充气压力不足，未反充气，结果异常"
                    11 -> "收缩压超量程（高血压），结果异常"
                    12 -> "静态压超过 300mmHg，危险提示，停止打气"
                    13 -> "打气超时,5 分钟未打气(<5mmHg)，结果异常"
                    14 -> "获得测量结果，存在心率不齐"
                    15 -> "获得测量结果，存在弱干扰"
                    16 -> "用户主动放气，结果异常"
                    17 -> "打气超过 300mmHg，结束测量，结果异常"
                    else -> ""
                }}"
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