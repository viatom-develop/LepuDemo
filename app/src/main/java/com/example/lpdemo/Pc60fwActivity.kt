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
import com.lepu.blepro.ext.pc60fw.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_pc60fw.*

class Pc60fwActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Pc60fwActivity"
    // Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC_60NW, Bluetooth.MODEL_PC_60NW_1,
    // Bluetooth.MODEL_PC66B, Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
    // Bluetooth.MODEL_OXYSMART, Bluetooth.MODEL_POD2B,
    // Bluetooth.MODEL_POD_1W, Bluetooth.MODEL_S5W,
    // Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
    // Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
    // Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
    // Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
    // Bluetooth.MODEL_S6W, Bluetooth.MODEL_S6W1,
    // Bluetooth.MODEL_PC60NW_BLE, Bluetooth.MODEL_PC60NW_WPS,
    // Bluetooth.MODEL_PC_60NW_NO_SN
    private var model = Bluetooth.MODEL_PC60FW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pc60fw)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        bleState.observe(this) {
            if (it) {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }

        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc60fwGetInfo(model)
        }

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtParam)
            .observe(this) {
                val data = it.data as RtParam
                tv_oxy.text = "${data.spo2}"
                tv_pr.text = "${data.pr}"
                tv_pi.text = "${data.pi}"
                // data.spo2：0%-100%（0：invalid）
                // data.pr：0-511bpm（0：invalid）
                // data.pi：0%-25.5%（0：invalid）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtWave)
            .observe(this) {
                val data = it.data as RtWave
                // data.waveIntData：0-127
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwBatLevel)
            .observe(this) {
                val data = it.data as Int
                // 0：0-25%，1：25-50%，2：50-75%，3：75-100%
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwWorkingStatus)
            .observe(this) {
                val data = it.data as WorkingStatus
                data_log.text = "$data"
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