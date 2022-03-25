package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
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

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20SetConfigResult)
            .observe(this, {
                val data = it.data as SetConfigResult

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