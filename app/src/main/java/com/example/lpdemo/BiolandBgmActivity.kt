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
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.bioland.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_bioland_bgm.*

class BiolandBgmActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "BiolandBgmActivity"
    private val model = Bluetooth.MODEL_BIOLAND_BGM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bioland_bgm)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.biolandBgmGetInfo(model)
        }
        get_data.setOnClickListener {
            BleServiceHelper.BleServiceHelper.biolandBgmGetGluData(model)
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
                // data.customerType：0-6（0：APPLE，1：AIAOLE，2：HAIER，3：NULL，4：XIAOMI，5：CHANNEL，6：KANWEI）
                // data.battery：0-100
                // data.deviceType：1（sphygmomanometer），2（Blood glucose meter）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmCountDown)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "CountDown：$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmNoGluData)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventBiolandBgmNoGluData $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmGluData)
            .observe(this) {
                val data = it.data as GluData
                data_log.text = "$data"
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