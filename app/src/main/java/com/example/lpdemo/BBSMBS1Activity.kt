package com.example.lpdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lpdemo.databinding.ActivityBbsms1Binding
import com.example.lpdemo.utils.EcgAdapter
import com.example.lpdemo.utils.EcgData
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.example.lpdemo.utils.ecgData
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.ext.bbsm.BbsmP1Config
import com.lepu.blepro.ext.bbsm.BbsmP1EventFile
import com.lepu.blepro.ext.bbsm.BbsmP1RecordFile
import com.lepu.blepro.ext.bbsm.DeviceInfo
import com.lepu.blepro.ext.bbsm.FileContent
import com.lepu.blepro.ext.bbsm.RtParam
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver

class BBSMBS1Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "BBSMS1Activity"
    private var model = Bluetooth.MODEL_BBSM_BS1

    private lateinit var binding: ActivityBbsms1Binding

    private var fileNames = arrayListOf<String>()
    private lateinit var ecgAdapter: EcgAdapter
    var ecgList: ArrayList<EcgData> = arrayListOf()

    private var config = BbsmP1Config()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBbsms1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        bleState.observe(this) {
            binding.bleState.setImageResource(if (it) R.mipmap.bluetooth_ok else R.mipmap.bluetooth_error)
        }
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.ecgFileRcv.layoutManager = this
        }
        ecgAdapter = EcgAdapter(R.layout.device_item, null).apply {
            binding.ecgFileRcv.adapter = this
        }
        ecgAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.isNotEmpty()) {
                (adapter.getItem(position) as EcgData).let {
                    val intent = Intent(this@BBSMBS1Activity, FileContentShowActivity::class.java)
                    intent.putExtra("model", model)
                    ecgData.fileName = it.fileName
                    ecgData.data = it.data
                    startActivity(intent)
                }
            }
        }
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bbsmp1GetInfo(model)
        }
        binding.factoryReset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bbsmp1FactoryReset(model)
        }
        binding.getConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bbsmp1GetConfig(model)
        }
        var configClickExample = 0
        binding.setConfig.setOnClickListener {
            val lampSwitch = BbsmP1Config.LampSwitch().apply {
                isRrOn = true
                isTempOn = false
                isKickQuiltOn = true
                isLieDownSleepOn = false
            }
            val beepSwitch = BbsmP1Config.BeepSwitch().apply {
                isRrOn = true
                isTempOn = true
                isKickQuiltOn = false
                isLieDownSleepOn = false
            }
            val remindSwitch = BbsmP1Config.RemindSwitch().apply {
                isRrOn = false
                isTempOn = false
                isKickQuiltOn = false
                isLieDownSleepOn = true
            }
            val tempLow = BbsmP1Config.TempLow().apply { low = 20.0F }
            val tempHigh = BbsmP1Config.TempHigh().apply { high = 80.0F }
            val rrLow = BbsmP1Config.RrLow().apply { low = 33 }
            val rrHigh = BbsmP1Config.RrHigh().apply { high = 100 }
            val warningSensitive = BbsmP1Config.WarningSensitive().apply { sensitive = 10 }
            val lampWorkTime = BbsmP1Config.LampWorkTime().apply { time = 40 }
            val dropTemp = BbsmP1Config.DropTemp().apply { temp = 38 }
            config.also {
                if (configClickExample == 0) {
                    it.lampSwitch = lampSwitch
                } else if (configClickExample == 1) {
                    it.beepSwitch = beepSwitch
                } else if (configClickExample == 2) {
                    it.remindSwitch = remindSwitch
                } else if (configClickExample == 3) {
                    it.tempLow = tempLow
                } else if (configClickExample == 4) {
                    it.tempHigh = tempHigh
                } else if (configClickExample == 5) {
                    it.rrLow = rrLow
                } else if (configClickExample == 6) {
                    it.rrHigh = rrHigh
                } else if (configClickExample == 7) {
                    it.warningSensitive = warningSensitive
                } else if (configClickExample == 8) {
                    it.lampWorkTime = lampWorkTime
                } else if (configClickExample == 9) {
                    it.dropTemp = dropTemp
                } else if (configClickExample == 10) {
                    it.setAllBbsmP1Config(
                        lampSwitch,
                        beepSwitch,
                        remindSwitch,
                        tempLow,
                        tempHigh,
                        rrLow,
                        rrHigh,
                        warningSensitive,
                        lampWorkTime,
                        dropTemp
                    )
                }
            }
            BleServiceHelper.BleServiceHelper.bbsmp1SetConfig(model, config)
            if (configClickExample == 10) {
                configClickExample = 0
            } else {
                configClickExample++
            }
            "2.set config ($configClickExample)".let { binding.setConfig.text = it }
        }
        binding.getRtData.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bbsmp1GetRtData(model)
        }
        binding.getFileList.setOnClickListener {
            fileNames.clear()
            ecgList.clear()
            ecgAdapter.setNewInstance(ecgList)
            ecgAdapter.notifyDataSetChanged()
            BleServiceHelper.BleServiceHelper.bbsmp1GetFileList(model)
        }
        binding.readFile.setOnClickListener {
            readFile()
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BBSMP1.EventBbsmP1Info)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BBSMP1.EventBbsmP1FactoryReset)
            .observe(this) {
                val data = it.data as Boolean
                "EventBbsmP1FactoryReset $data".let { binding.dataLog.text = it }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BBSMP1.EventBbsmP1GetConfig)
            .observe(this) {
                config = it.data as BbsmP1Config
                binding.dataLog.text = "$config"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BBSMP1.EventBbsmP1SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                "EventBbsmP1SetConfig $data".let { binding.dataLog.text = it }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BBSMP1.EventBbsmP1RtData)
            .observe(this) {
                val data = it.data as RtParam
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BBSMP1.EventBbsmP1FileList)
            .observe(this) {
                fileNames = it.data as ArrayList<String>
                binding.dataLog.text = "$fileNames"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BBSMP1.EventBbsmP1ReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                "EventBbsmP1ReadFileError $data".let { binding.dataLog.text = it }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BBSMP1.EventBbsmP1ReadingFileProgress)
            .observe(this) {
                val data = it.data as Int  // 0-100
                "${fileNames[0]} $data %".let { binding.dataLog.text = it }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BBSMP1.EventBbsmP1ReadFileComplete)
            .observe(this) {
                val data = it.data as FileContent
                val ecgData = EcgData()
                ecgData.fileName = data.fileName
                if (data.fileName.contains("R")) {
                    val file = BbsmP1RecordFile(data.content)
                    ecgData.duration = file.duration
                } else {
                    val file = BbsmP1EventFile(data.content)
                }
                ecgData.data = data.content
                ecgList.add(ecgData)
                ecgAdapter.setNewInstance(ecgList)
                ecgAdapter.notifyDataSetChanged()
                fileNames.removeAt(0)
                readFile()
            }
    }

    private fun readFile() {
        if (fileNames.isEmpty()) return
        BleServiceHelper.BleServiceHelper.bbsmp1ReadFile(model, fileNames[0])
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