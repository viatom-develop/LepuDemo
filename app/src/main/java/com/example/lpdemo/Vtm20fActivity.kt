package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityVtm20fBinding
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.vtm20f.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver

class Vtm20fActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Vtm20fActivity"
    private val model = Bluetooth.MODEL_TV221U
    private lateinit var binding: ActivityVtm20fBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVtm20fBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM20f.EventVTM20fRtParam)
            .observe(this) {
                val data = it.data as RtParam
                binding.tvOxy.text = data.spo2.toString()
                binding.tvPr.text = data.pr.toString()
                binding.tvPi.text = data.pi.toString()
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM20f.EventVTM20fRtWave)
            .observe(this) {
                val data = it.data as RtWave
                // data.wave：0-100
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