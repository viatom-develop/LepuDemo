package com.example.lpdemo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lpdemo.utils.*
import com.example.lpdemo.views.EcgBkg
import com.example.lpdemo.views.EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.pc80b.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.getTimeString
import kotlinx.android.synthetic.main.activity_pc80b.*
import kotlin.math.floor

class Pc80bActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Pc80bActivity"
    // Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE
    // Bluetooth.MODEL_PC80B_BLE2
    private var model = Bluetooth.MODEL_PC80B

    private lateinit var ecgAdapter: EcgAdapter
    var ecgList: ArrayList<EcgData> = arrayListOf()

    private lateinit var ecgBkg: EcgBkg
    private lateinit var ecgView: EcgView
    /**
     * rt wave
     */
    private val waveHandler = Handler()
    private val ecgWaveTask = EcgWaveTask()

    inner class EcgWaveTask : Runnable {
        override fun run() {
            val interval: Int = when {
                DataController.dataRec.size > 250 -> {
                    30
                }
                DataController.dataRec.size > 150 -> {
                    35
                }
                DataController.dataRec.size > 75 -> {
                    40
                }
                else -> {
                    45
                }
            }

            waveHandler.postDelayed(this, interval.toLong())

            val temp = DataController.draw(5)
            dataEcgSrc.value = DataController.feed(dataEcgSrc.value, temp)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pc80b)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            ecg_file_rcv.layoutManager = this
        }
        ecgAdapter = EcgAdapter(R.layout.device_item, null).apply {
            ecg_file_rcv.adapter = this
        }
        ecgAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.size > 0) {
                (adapter.getItem(position) as EcgData).let {
                    val intent = Intent(this@Pc80bActivity, WaveEcgActivity::class.java)
                    intent.putExtra("model", model)
                    ecgData.startTime = it.startTime
                    ecgData.shortData = it.shortData
                    startActivity(intent)
                }
            }
        }
        ecg_bkg.post {
            initEcgView()
        }
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc80bGetInfo(model)
        }
        get_battery.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc80bGetBattery(model)
        }
        bleState.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                waveHandler.removeCallbacks(ecgWaveTask)
                waveHandler.postDelayed(ecgWaveTask, 1000)
            } else {
                waveHandler.removeCallbacks(ecgWaveTask)
                ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }
        dataEcgSrc.observe(this) {
            if (this::ecgView.isInitialized) {
                ecgView.setDataSrc(it)
                ecgView.invalidate()

            }
        }
    }

    private fun initEcgView() {
        DataController.nWave = 1
        // cal screen
        val dm = resources.displayMetrics
        val index = floor(ecg_bkg.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        DataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        DataController.mm2px = mm2px

        ecg_bkg.measure(0, 0)
        ecgBkg = EcgBkg(this)
        ecg_bkg.addView(ecgBkg)

        ecg_view.measure(0, 0)
        ecgView = EcgView(this)
        ecg_view.addView(ecgView)

        waveHandler.removeCallbacks(ecgWaveTask)
        waveHandler.postDelayed(ecgWaveTask, 1000)

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bBatLevel)
            .observe(this) {
                val data = it.data as Int
                // 0：0-25%，1：25-50%，2：50-75%，3：75-100%
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bContinuousData)
            .observe(this) {
                val data = it.data as RtContinuousData
                DataController.receive(data.ecgData.ecgFloats)
                hr.text = "${data.hr}"
                data_log.text = "$data"
                // sampling rate：150HZ
                // mV = (n - 2048) * (1 / 330))（data.ecgData.ecgFloats = (data.ecgData.ecgInts - 2048) * (1 / 330)）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bContinuousDataEnd)
            .observe(this) {
                data_log.text = "exit continuous measurement"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bFastData)
            .observe(this) {
                val data = it.data as RtFastData
                if (data.measureMode == 1) {  // 1:Fast mode
                    data_log.text = when (data.measureStage) {
                        1 -> "preparing"
                        2 -> "measuring"
                        3 -> "analyzing"
                        4 -> "result"
                        5 -> "stop"
                        else -> ""
                    }
                } else if (data.measureMode == 2) {  // 2:Continuous mode
                    data_log.text = when (data.measureStage) {
                        1 -> "preparing"
                        5 -> "stop"  // sdk stop sending EventPc80bFastData event, then start to send EventPc80bContinuousData event
                        else -> ""
                    }
                }
                if (data.dataType == 1) {
                    data.ecgData.let { data2 ->
                        DataController.receive(data2.ecgFloats)
                    }
                    // sampling rate：150HZ
                    // mV = (n - 2048) * (1 / 330)（data.ecgData.ecgFloats = (data.ecgData.ecgInts - 2048) * (1 / 330)）
                } else {
                    data.ecgResult.let {
                        data_log.text = "result $it"
                    }
                }
                hr.text = "${data.hr}"
//                data_log.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "ReadingFileProgress $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileComplete)
            .observe(this) {
                val data = it.data as EcgFile
                val ecgData = EcgData()
                ecgData.fileName = getTimeString(data.year, data.month, data.day, data.hour, data.minute, data.second)
                ecgData.startTime = DateUtil.getSecondTimestamp(ecgData.fileName)
                ecgData.duration = 30
                val temp = ShortArray(data.ecgInts.size)
                for ((index, d) in data.ecgInts.withIndex()) {
                    temp[index] = d.toShort()
                }
                ecgData.shortData = temp
                ecgList.add(ecgData)
                ecgAdapter.setNewInstance(ecgList)
                ecgAdapter.notifyDataSetChanged()
                data_log.text = "$data"
                // sampling rate：150HZ
                // mV = (n - 2048) * (1 / 330)（data.ecgFloats = (data.ecgInts - 2048) * (1 / 330)）
            }

    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // 蓝牙状态 Ble.State
        Log.d(TAG, "model $model, state: $state")

        _bleState.value = state == Ble.State.CONNECTED
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        waveHandler.removeCallbacks(ecgWaveTask)
        DataController.clear()
        dataEcgSrc.value = null
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

}