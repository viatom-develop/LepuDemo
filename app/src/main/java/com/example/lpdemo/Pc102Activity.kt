package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityPc102Binding
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.pc102.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver

class Pc102Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Pc102Activity"
    private val model = Bluetooth.MODEL_PC100
    private lateinit var binding: ActivityPc102Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPc102Binding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        bleState.observe(this) {
            if (it) {
                binding.bpBleState.setImageResource(R.mipmap.bluetooth_ok)
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.bpBleState.setImageResource(R.mipmap.bluetooth_error)
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }

        binding.startBp.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc100StartBp(model)
        }
        binding.stopBp.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc100StopBp(model)
        }
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc100GetInfo(model)
        }

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100DeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
                // data.batLevel：0-3（0：0-25%，1：25-50%，2：50-75%，3：75-100%）
                // data.batStatus：0（No charge），1（Charging），2（Charging complete）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtBpData)
            .observe(this) {
                val data = it.data as RtBpData
                binding.tvPs.text = "${data.ps}"
                binding.dataLog.text = "$data"
                // data.sign：heart rate signal，0（no hr），1（has hr）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult)
            .observe(this) {
                val data = it.data as BpResult
                binding.tvSys.text = "${data.sys}"
                binding.tvDia.text = "${data.dia}"
                binding.tvMean.text = "${data.map}"
                binding.tvPrBp.text = "${data.pr}"
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observe(this) {
                val data = it.data as BpResultError
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyParam)
            .observe(this) {
                val data = it.data as RtOxyParam
                binding.tvOxy.text = "${data.spo2}"
                binding.tvPr.text = "${data.pr}"
                binding.tvPi.text = "${data.pi}"
                binding.dataLog.text = "$data"
                // data.spo2：0%-100%（0：invalid）
                // data.pr：0-511bpm（0：invalid）
                // data.pi：0%-25.5%（0：invalid）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyWave)
            .observe(this) {
                val data = it.data as RtOxyWave
                // data.waveIntData：0-127
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