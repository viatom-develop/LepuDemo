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
import com.lepu.blepro.ext.pc102.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_pc102.*

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
        ble_name.text = deviceName
        bleState.observe(this) {
            if (it) {
                bp_ble_state.setImageResource(R.mipmap.bluetooth_ok)
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                bp_ble_state.setImageResource(R.mipmap.bluetooth_error)
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }

        start_bp.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc100StartBp(model)
        }
        stop_bp.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc100StopBp(model)
        }
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc100GetInfo(model)
        }

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100DeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
                // data.batLevel：0-3（0：0-25%，1：25-50%，2：50-75%，3：75-100%）
                // data.batStatus：0（No charge），1（Charging），2（Charging complete）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtBpData)
            .observe(this) {
                val data = it.data as RtBpData
                tv_ps.text = "${data.ps}"
                data_log.text = "$data"
                // data.sign：heart rate signal，0（no hr），1（has hr）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult)
            .observe(this) {
                val data = it.data as BpResult
                tv_sys.text = "${data.sys}"
                tv_dia.text = "${data.dia}"
                tv_mean.text = "${data.map}"
                tv_pr_bp.text = "${data.pr}"
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observe(this) {
                val data = it.data as BpResultError
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyParam)
            .observe(this) {
                val data = it.data as RtOxyParam
                tv_oxy.text = "${data.spo2}"
                tv_pr.text = "${data.pr}"
                tv_pi.text = "${data.pi}"
                data_log.text = "$data"
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