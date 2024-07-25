package com.example.lpdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lpdemo.databinding.ActivityEcnBinding
import com.example.lpdemo.utils.*
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.ext.ecn.File
import com.lepu.blepro.ext.ecn.RtData
import com.lepu.blepro.ext.ecn.RtState
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver

class EcnActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "EcnActivity"
    private val model = Bluetooth.MODEL_ECN
    private lateinit var binding: ActivityEcnBinding
    private var fileNames = arrayListOf<String>()
    private lateinit var ecgAdapter: EcgAdapter
    var ecgList: ArrayList<EcgData> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEcnBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        bleState.observe(this) {
            if (it) {
                binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.bleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.fileRcv.layoutManager = this
        }
        ecgAdapter = EcgAdapter(R.layout.device_item, null).apply {
            binding.fileRcv.adapter = this
        }
        ecgAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.size > 0) {
                (adapter.getItem(position) as EcgData).let {
                    val intent = Intent(this@EcnActivity, DataActivity::class.java)
                    intent.putExtra("pdfBytes", it.data)
                    startActivity(intent)
                }
            }
        }
        binding.startRtData.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnStartRtData(model)
        }
        binding.stopRtData.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnStopRtData(model)
        }
        binding.startCollect.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnStartCollect(model)
        }
        binding.stopCollect.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnStopCollect(model)
        }
        binding.getState.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnGetRtState(model)
        }
        binding.getResult.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnGetDiagnosisResult(model)
        }
        binding.getFileList.setOnClickListener {
            fileNames.clear()
            ecgList.clear()
            ecgAdapter.setNewInstance(ecgList)
            ecgAdapter.notifyDataSetChanged()
            BleServiceHelper.BleServiceHelper.ecnGetFileList(model)
        }
        binding.readFile.setOnClickListener {
            readFile()
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnStartRtData)
            .observe(this) {
                binding.dataLog.text = "Start send real-time data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnStopRtData)
            .observe(this) {
                binding.dataLog.text = "Stop send real-time data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnStartCollect)
            .observe(this) {
                binding.dataLog.text = "Start collect real-time data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnStopCollect)
            .observe(this) {
                binding.dataLog.text = "Stop collect real-time data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnRtData)
            .observe(this) {
                val data = it.data as RtData
                binding.dataLog.text = "$data"
//                data.wave.len1 : 通道数
//                data.wave.len2 : 每通道采样点数
                for (i in 0 until data.wave.len1) {
                    val data = data.wave.wave.copyOfRange(i*data.wave.len2, (i+1)*data.wave.len2)  // 每通道采样点
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnGetRtState)
            .observe(this) {
                val data = it.data as RtState
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnDiagnosisResult)
            .observe(this) {
                val data = it.data as ArrayList<String>
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnGetFileList)
            .observe(this) {
                fileNames = it.data as ArrayList<String>
                binding.dataLog.text = "$fileNames"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "EventEcnReadingFileProgress $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadFileComplete)
            .observe(this) {
                val data = it.data as File
                binding.dataLog.text = "$data"
                val ecgData = EcgData()
                ecgData.fileName = data.fileName
                ecgData.data = data.content
                ecgList.add(ecgData)
                ecgAdapter.setNewInstance(ecgList)
                ecgAdapter.notifyDataSetChanged()
                fileNames.removeAt(0)
                readFile()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEcnReadFileError $data"
            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
        BleServiceHelper.BleServiceHelper.ecnReadFile(model, fileNames[0])
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