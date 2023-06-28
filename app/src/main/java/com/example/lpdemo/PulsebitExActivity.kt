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
import com.lepu.blepro.ext.pulsebit.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_pulsebit_ex.*

class PulsebitExActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "PulsebitExActivity"
    // Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4
    private var model = Bluetooth.MODEL_PULSEBITEX

    private var fileNames = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pulsebit_ex)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pulsebitExGetInfo(model)
        }
        get_file_list.setOnClickListener {
            // 1. get list first
            BleServiceHelper.BleServiceHelper.pulsebitExGetFileList(model)
        }
        read_file.setOnClickListener {
            // 2. then read file
            readFile()
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "GetFileListError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListProgress)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "GetFileListProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileList)
            .observe(this) {
                val data = it.data as ArrayList<String>
                for (i in data) {
                    fileNames.add(i)
                }
                data_log.text = data.toString()
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "ReadingFileProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileComplete)
            .observe(this) {
                val data = it.data as EcgFile
                Log.d(TAG, "data: $data")
                data_log.text = "$data"
                fileNames.removeAt(0)
                readFile()
                // sampling rate：500HZ
                // mV = n * 0.0012820952991323（data.wFs = data.waveShortData * 0.0012820952991323）
                // data.result：ExEcgDiagnosis
                // data.result.isRegular：Whether Regular ECG Rhythm
                // data.result.isPoorSignal：Whether Unable to analyze
                // data.result.isFastHr：Whether Fast Heart Rate
                // data.result.isSlowHr：Whether Slow Heart Rate
                // data.result.isIrregular：Whether Irregular ECG Rhythm
                // data.result.isPvcs：Whether Possible ventricular premature beats
                // data.result.isHeartPause：Whether Possible heart pause
                // data.result.isFibrillation：Whether Possible Atrial fibrillation
                // data.result.isWideQrs：Whether Wide QRS duration
                // data.result.isProlongedQtc：Whether QTc is prolonged
                // data.result.isShortQtc：Whether QTc is short
                // data.result.isStElevation：Whether ST segment elevation
                // data.result.isStDepression：Whether ST segment depression
            }

    }

    private fun readFile() {
        if (fileNames.size == 0) return
        BleServiceHelper.BleServiceHelper.pulsebitExReadFile(model, fileNames[0])
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