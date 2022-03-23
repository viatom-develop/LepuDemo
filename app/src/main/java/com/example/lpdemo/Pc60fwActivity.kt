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
import com.lepu.blepro.ext.pc60fw.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_pc60fw.*

class Pc60fwActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Pc60fwActivity"
    private val model = Bluetooth.MODEL_PC60FW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pc60fw)
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
            BleServiceHelper.BleServiceHelper.pc60fwGetInfo(model)
        }

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo)
            .observe(this, {
                // 设备信息
                val data = it.data as DeviceInfo
                data_log.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtParam)
            .observe(this, {
                val data = it.data as RtParam
                tv_oxy.text = data.spo2.toString()
                tv_pr.text = data.pr.toString()
                tv_pi.text = data.pi.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtWave)
            .observe(this, {
                val data = it.data as RtWave

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwBatLevel)
            .observe(this, {
                val data = it.data as Int

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwWorkingStatus)
            .observe(this, {
                val data = it.data as WorkingStatus
                data_log.text = data.toString()
            })

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