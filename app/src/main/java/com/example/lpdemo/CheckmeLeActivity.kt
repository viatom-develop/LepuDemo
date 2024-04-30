package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityCheckmeLeBinding
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

class CheckmeLeActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "CheckmeLeActivity"
    private val model = Bluetooth.MODEL_CHECKME_LE
    private lateinit var binding: ActivityCheckmeLeBinding

    private var fileNames = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckmeLeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeLeGetInfo(model)
        }
        binding.getOxyList.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeLeGetFileList(model, Constant.CheckmeLeListType.OXY_TYPE)
        }
        binding.getEcgList.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeLeGetFileList(model, Constant.CheckmeLeListType.ECG_TYPE)
        }
        binding.getDlcList.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeLeGetFileList(model, Constant.CheckmeLeListType.DLC_TYPE)
        }
        binding.readFile.setOnClickListener {
            // 1. get list first 2. then read file
            readFile()
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "GetFileListError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "GetFileListProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeOxyList)
            .observe(this) {
                val data = it.data as ArrayList<OxyRecord>
                binding.dataLog.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeEcgList)
            .observe(this) {
                val data = it.data as ArrayList<EcgRecord>
                for (i in data) {
                    fileNames.add(i.recordName)
                }
                binding.dataLog.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeDlcList)
            .observe(this) {
                val data = it.data as ArrayList<DlcRecord>
                for (i in data) {
                    fileNames.add(i.recordName)
                }
                binding.dataLog.text = data.toString()
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "ReadingFileProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileComplete)
            .observe(this) {
                val data = it.data as EcgFile
                Log.d(TAG, "data: $data")
                binding.dataLog.text = "$data"
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