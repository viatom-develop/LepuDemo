package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.utils.DataController
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.views.EcgBkg
import com.example.lpdemo.views.EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.pc102.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_pc102.*
import kotlin.math.floor

class Pc102Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Pc102Activity"
    private val model = Bluetooth.MODEL_PC100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pc102)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        bleState.observe(this, {
            if (it) {
                bp_ble_state.setImageResource(R.mipmap.bluetooth_ok)
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                bp_ble_state.setImageResource(R.mipmap.bluetooth_error)
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        })

        start_bp.setOnClickListener {
            BleServiceHelper.BleServiceHelper.startBp(model)
        }
        stop_bp.setOnClickListener {
            BleServiceHelper.BleServiceHelper.stopBp(model)
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100DeviceInfo)
            .observe(this, {
                // 设备信息
                val data = it.data as DeviceInfo

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtBpData)
            .observe(this, {
                val data = it.data as RtBpData
                tv_ps.text = data.ps.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult)
            .observe(this, {
                val data = it.data as BpResult
                tv_sys.text = data.sys.toString()
                tv_dia.text = data.dia.toString()
                tv_mean.text = data.map.toString()
                tv_pr_bp.text = data.pr.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observe(this, {
                val data = it.data as BpResultError

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyParam)
            .observe(this, {
                val data = it.data as RtOxyParam
                tv_oxy.text = data.spo2.toString()
                tv_pr.text = data.pr.toString()
                tv_pi.text = data.pi.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyWave)
            .observe(this, {
                val data = it.data as ByteArray

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