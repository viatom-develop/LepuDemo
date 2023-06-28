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
import com.lepu.blepro.ext.FhrInfo
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_log.*

class FhrActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "FhrActivity"
    private val model = Bluetooth.MODEL_FHR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        bleState.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.FHR.EventFhrDeviceInfo)
            .observe(this) {
                val data = it.data as FhrInfo
                data_log.text = "$data"
                // data.hr：60-240
                // data.volume：0-6
                // data.strength：0-2
                // data.battery：0-6
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.FHR.EventFhrAudioData)
            .observe(this) {
                val data = it.data as ByteArray

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