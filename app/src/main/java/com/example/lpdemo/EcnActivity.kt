package com.example.lpdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lpdemo.utils.*
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.ext.ecn.File
import com.lepu.blepro.ext.ecn.FileList
import com.lepu.blepro.ext.ecn.RtData
import com.lepu.blepro.ext.ecn.RtState
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.DateUtil
import kotlinx.android.synthetic.main.activity_bp2.*
import kotlinx.android.synthetic.main.activity_ecn.*
import kotlinx.android.synthetic.main.activity_ecn.ble_name
import kotlinx.android.synthetic.main.activity_ecn.ble_state
import kotlinx.android.synthetic.main.activity_ecn.data_log
import kotlinx.android.synthetic.main.activity_ecn.get_file_list
import kotlinx.android.synthetic.main.activity_ecn.read_file

class EcnActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "EcnActivity"
    private val model = Bluetooth.MODEL_ECN
    private var fileNames = arrayListOf<String>()
    private lateinit var ecgAdapter: EcgAdapter
    var ecgList: ArrayList<EcgData> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ecn)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        bleState.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            file_rcv.layoutManager = this
        }
        ecgAdapter = EcgAdapter(R.layout.device_item, null).apply {
            file_rcv.adapter = this
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
        start_rt_data.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnStartRtData(model)
        }
        stop_rt_data.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnStopRtData(model)
        }
        start_collect.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnStartCollect(model)
        }
        stop_collect.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnStopCollect(model)
        }
        get_state.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnGetRtState(model)
        }
        get_result.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ecnGetDiagnosisResult(model)
        }
        get_file_list.setOnClickListener {
            fileNames.clear()
            ecgList.clear()
            ecgAdapter.setNewInstance(ecgList)
            ecgAdapter.notifyDataSetChanged()
            BleServiceHelper.BleServiceHelper.ecnGetFileList(model)
        }
        read_file.setOnClickListener {
            readFile()
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnStartRtData)
            .observe(this) {
                data_log.text = "Start send real-time data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnStopRtData)
            .observe(this) {
                data_log.text = "Stop send real-time data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnStartCollect)
            .observe(this) {
                data_log.text = "Start collect real-time data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnStopCollect)
            .observe(this) {
                data_log.text = "Stop collect real-time data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnRtData)
            .observe(this) {
                val data = it.data as RtData
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnGetRtState)
            .observe(this) {
                val data = it.data as RtState
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnDiagnosisResult)
            .observe(this) {
                val data = it.data as ArrayList<String>
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnGetFileList)
            .observe(this) {
                val data = it.data as FileList
                data_log.text = "$data"
                for (file in data.list) {
                    fileNames.add(file.fileName)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "EventEcnReadingFileProgress $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadFileComplete)
            .observe(this) {
                val data = it.data as File
                data_log.text = "$data"
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
                data_log.text = "EventEcnReadFileError $data"
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