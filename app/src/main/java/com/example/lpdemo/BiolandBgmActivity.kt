package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityBiolandBgmBinding
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.bioland.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver

class BiolandBgmActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "BiolandBgmActivity"
    private val model = Bluetooth.MODEL_BIOLAND_BGM
    private lateinit var binding: ActivityBiolandBgmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBiolandBgmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.biolandBgmGetInfo(model)
        }
        binding.getData.setOnClickListener {
            BleServiceHelper.BleServiceHelper.biolandBgmGetGluData(model)
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
                // data.customerType：0-6（0：APPLE，1：AIAOLE，2：HAIER，3：NULL，4：XIAOMI，5：CHANNEL，6：KANWEI）
                // data.battery：0-100
                // data.deviceType：1（sphygmomanometer），2（Blood glucose meter）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmCountDown)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "CountDown：$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmNoGluData)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventBiolandBgmNoGluData $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmGluData)
            .observe(this) {
                val data = it.data as GluData
                binding.dataLog.text = "$data"
                // data.resultMg：unit mg/dL（18-Lo，707-Hi）
                // data.resultMmol：unit mmol/L（1.0-Lo，39.3-Hi）
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