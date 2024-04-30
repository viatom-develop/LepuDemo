package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityPf10aw1Binding
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.pf10aw1.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver

class Pf10Aw1Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Pf10Aw1Activity"
    // Bluetooth.MODEL_PF_10AW_1, Bluetooth.MODEL_PF_10BWS,
    // Bluetooth.MODEL_SA10AW_PU, Bluetooth.MODEL_PF10BW_VE,
    private var model = Bluetooth.MODEL_PF_10BWS
    private lateinit var binding: ActivityPf10aw1Binding

    private var fileNames = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPf10aw1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        bleState.observe(this) {
            if (it) {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }

        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pf10Aw1GetInfo(model)
        }
        binding.getConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pf10Aw1GetConfig(model)
        }
        binding.getFileList.setOnClickListener {
            fileNames.clear()
            BleServiceHelper.BleServiceHelper.pf10Aw1GetFileList(model)
        }
        binding.readFile.setOnClickListener {
            readFile()
        }
        binding.setSpo2Low.setOnClickListener {
            // 85%-99%, 1%
            BleServiceHelper.BleServiceHelper.pf10Aw1SetSpo2Low(model, 90)
        }
        binding.setPrLow.setOnClickListener {
            // 30bpm-60bpm, 5bpm
            BleServiceHelper.BleServiceHelper.pf10Aw1SetPrLow(model, 60)
        }
        binding.setPrHigh.setOnClickListener {
            // 100bpm-240bpm, 5bpm
            BleServiceHelper.BleServiceHelper.pf10Aw1SetPrHigh(model, 120)
        }
        binding.setEsMode.setOnClickListener {
            // 0：keep screen on，1：1 min screen off，2：3 min screen off，3：5 min screen off
            BleServiceHelper.BleServiceHelper.pf10Aw1SetEsMode(model, 0)
        }
        binding.setAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            // Threshold reminder switch
            BleServiceHelper.BleServiceHelper.pf10Aw1SetAlarmSwitch(model, isChecked)
        }
        binding.setBeep.setOnCheckedChangeListener { buttonView, isChecked ->
            BleServiceHelper.BleServiceHelper.pf10Aw1SetBeepSwitch(model, isChecked)
        }
        binding.factoryReset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pf10Aw1FactoryReset(model)
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1GetInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1GetFileList)
            .observe(this) {
                fileNames = it.data as ArrayList<String>
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1ReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventPf10Aw1ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1ReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "进度 $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1ReadFileComplete)
            .observe(this) {
                val data = it.data as OxyFile
                binding.dataLog.text = "$data"
                fileNames.removeAt(0)
                readFile()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1WorkingStatus)
            .observe(this) {
                val data = it.data as WorkingStatus
                // data.mode : 1（Spot mode, Not currently supported）

                // data.mode : 2（Continuous mode）
                // data.step : 0(prepare) 1(measuring) 2(completed, saved) 3(less than 2 min, not save)
                // data.para1 : duration(unit s)

                // data.mode : 3（menu）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1RtWave)
            .observe(this) {
                val data = it.data as RtWave
                // data.waveIntData：0-127
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1RtParam)
            .observe(this) {
                val data = it.data as RtParam
                binding.tvOxy.text = data.spo2.toString()
                binding.tvPr.text = data.pr.toString()
                binding.tvPi.text = data.pi.toString()
                // data.batLevel：0-3
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1GetConfig)
            .observe(this) {
                val data = it.data as Config
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventPf10Aw1SetConfig $data"
                BleServiceHelper.BleServiceHelper.pf10Aw1GetConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1FactoryReset)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventPf10Aw1FactoryReset $data"
            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
        BleServiceHelper.BleServiceHelper.pf10Aw1ReadFile(model, fileNames[0])
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