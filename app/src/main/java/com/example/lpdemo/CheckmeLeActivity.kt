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
import com.lepu.blepro.constants.Constant
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.checkmele.DlcRecord
import com.lepu.blepro.ext.checkmele.EcgRecord
import com.lepu.blepro.ext.checkmele.OxyRecord
import com.lepu.blepro.ext.checkmele.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_checkme_le.*

class CheckmeLeActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "CheckmeLeActivity"
    private val model = Bluetooth.MODEL_CHECKME_LE

    private var fileNames = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkme_le)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeLeGetInfo(model)
        }
        get_oxy_list.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeLeGetFileList(model, Constant.CheckmeLeListType.OXY_TYPE)
        }
        get_ecg_list.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeLeGetFileList(model, Constant.CheckmeLeListType.ECG_TYPE)
        }
        get_dlc_list.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeLeGetFileList(model, Constant.CheckmeLeListType.DLC_TYPE)
        }
        read_file.setOnClickListener {
            // 1. get list first 2. then read file
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "GetFileListError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListProgress)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "GetFileListProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeOxyList)
            .observe(this) {
                val data = it.data as ArrayList<OxyRecord>
                data_log.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeEcgList)
            .observe(this) {
                val data = it.data as ArrayList<EcgRecord>
                for (i in data) {
                    fileNames.add(i.recordName)
                }
                data_log.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeDlcList)
            .observe(this) {
                val data = it.data as ArrayList<DlcRecord>
                for (i in data) {
                    fileNames.add(i.recordName)
                }
                data_log.text = data.toString()
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "ReadingFileProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileComplete)
            .observe(this) {
                val data = it.data as EcgFile
                Log.d(TAG, "data: $data")
                data_log.text = "$data"
                fileNames.removeAt(0)
                readFile()
                // sampling rate：500HZ
                // mV = n * 0.0012820952991323（data.wFs = data.waveShortData * 0.0012820952991323）
                // data.result：LeEcgDiagnosis
                // data.result.isRegular：Whether Regular ECG Rhythm
                // data.result.isPoorSignal：Whether Unable to analyze
                // data.result.isHighHr：Whether High Heart Rate
                // data.result.isLowHr：Whether Low Heart Rate
                // data.result.isIrregular：Whether Irregular ECG Rhythm
                // data.result.isHighQrs：Whether High QRS Value
                // data.result.isHighSt：Whether High ST Value
                // data.result.isLowSt：Whether Low ST Value
                // data.result.isPrematureBeat：Whether Suspected Premature Beat
            }

    }

    private fun readFile() {
        if (fileNames.size == 0) return
        BleServiceHelper.BleServiceHelper.checkmeLeReadFile(model, fileNames[0])
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