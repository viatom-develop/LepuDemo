package com.example.lpdemo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lpdemo.databinding.ActivityBp2wBinding
import com.example.lpdemo.utils.*
import com.example.lpdemo.views.EcgBkg
import com.example.lpdemo.views.EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.bp2w.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.DateUtil
import kotlin.math.floor

class Bp2wActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Bp2wActivity"
    // Bluetooth.MODEL_BP2W
    private var model = Bluetooth.MODEL_BP2W
    private lateinit var binding: ActivityBp2wBinding

    private var config = Bp2wConfig()

    private var fileNames = arrayListOf<String>()
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
                    20
                }
                DataController.dataRec.size > 150 -> {
                    25
                }
                DataController.dataRec.size > 75 -> {
                    30
                }
                else -> {
                    35
                }
            }

            waveHandler.postDelayed(this, interval.toLong())

            val temp = DataController.draw(5)
            dataEcgSrc.value = DataController.feed(dataEcgSrc.value, temp)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBp2wBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.ecgFileRcv.layoutManager = this
        }
        ecgAdapter = EcgAdapter(R.layout.device_item, null).apply {
            binding.ecgFileRcv.adapter = this
        }
        ecgAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.size > 0) {
                (adapter.getItem(position) as EcgData).let {
                    val intent = Intent(this@Bp2wActivity, WaveEcgActivity::class.java)
                    intent.putExtra("model", model)
                    ecgData.startTime = it.startTime
                    ecgData.shortData = it.shortData
                    startActivity(intent)
                }
            }
        }
        binding.ecgBkg.post {
            initEcgView()
        }
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bp2wGetInfo(model)
        }
        binding.factoryReset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bp2wFactoryReset(model)
        }
        binding.getConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bp2wGetConfig(model)
        }
        binding.setConfig.setOnClickListener {
            config.isSoundOn = !config.isSoundOn
            config.avgMeasureMode = 1
            // config.avgMeasureMode: 0(bp x3 off), 1(bp x3 on, interval 30s), 2(bp x3 on, interval 60s),
            //                        3(bp x3 on, interval 90s), 4(bp x3 on, interval 120s)
            BleServiceHelper.BleServiceHelper.bp2wSetConfig(model, config)
        }
        binding.startRtTask.setOnClickListener {
            waveHandler.removeCallbacks(ecgWaveTask)
            waveHandler.postDelayed(ecgWaveTask, 1000)
            BleServiceHelper.BleServiceHelper.startRtTask(model)
        }
        binding.stopRtTask.setOnClickListener {
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
        }
        binding.getFileList.setOnClickListener {
            fileNames.clear()
            ecgList.clear()
            ecgAdapter.setNewInstance(ecgList)
            ecgAdapter.notifyDataSetChanged()
            BleServiceHelper.BleServiceHelper.bp2wGetFileList(model)
        }
        binding.readFile.setOnClickListener {
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
            readFile()
        }
        bleState.observe(this) {
            if (it) {
                binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
                binding.bpBleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                waveHandler.removeCallbacks(ecgWaveTask)
                BleServiceHelper.BleServiceHelper.stopRtTask(model)
                binding.bleState.setImageResource(R.mipmap.bluetooth_error)
                binding.bpBleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }
        dataEcgSrc.observe(this) {
            if (this::ecgView.isInitialized) {
                ecgView.setDataSrc(it)
                ecgView.invalidate()
            }
        }
        ArrayAdapter(this,
            android.R.layout.simple_list_item_1,
            arrayListOf("5mm/mV", "10mm/mV", "20mm/mV")
        ).apply {
            binding.gain.adapter = this
        }
        binding.gain.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                DataController.ampKey = position
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        ArrayAdapter(this,
            android.R.layout.simple_list_item_1,
            arrayListOf("25mm/s", "12.5mm/s", "6.25mm/s")
        ).apply {
            binding.speed.adapter = this
        }
        binding.speed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> DataController.speed = 1
                    1 -> DataController.speed = 2
                    2 -> DataController.speed = 4
                }
                val dm = resources.displayMetrics
                val index = floor((binding.ecgBkg.width / dm.xdpi * 25.4 / 25 * 250) * DataController.speed).toInt()
                DataController.maxIndex = index
                dataEcgSrc.value = null
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun initEcgView() {
        DataController.nWave = 2
        // cal screen
        val dm = resources.displayMetrics
        val index = floor((binding.ecgBkg.width / dm.xdpi * 25.4 / 25 * 250) * DataController.speed).toInt()
        DataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        DataController.mm2px = mm2px

        binding.ecgBkg.measure(0, 0)
        ecgBkg = EcgBkg(this)
        binding.ecgBkg.addView(ecgBkg)

        binding.ecgView.measure(0, 0)
        ecgView = EcgView(this)
        binding.ecgView.addView(ecgView)
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFactoryReset)
            .observe(this) {
                val data = it.data as Boolean
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetConfig)
            .observe(this) {
                config = it.data as Bp2wConfig
                // config.soundOn: Heartbeat sound switch
                // config.avgMeasureMode: 0(bp measure x3 off), 1(bp measure x3 on, interval 30s), 2(bp measure x3 on, interval 60s),
                //                        3(bp measure x3 on, interval 90s), 4(bp measure x3 on, interval 120s)
                binding.dataLog.text = "$config"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetConfig)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventBp2wSetConfig $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtData)
            .observe(this) {
                val data = it.data as RtData
                // data.status: RtStatus
                // data.status.deviceStatus: 0(STATUS_SLEEP), 1(STATUS_MEMERY), 2(STATUS_CHARGE), 3(STATUS_READY),
                //                           4(STATUS_BP_MEASURING), 5(STATUS_BP_MEASURE_END),
                //                           6(STATUS_ECG_MEASURING), 7(STATUS_ECG_MEASURE_END),
                //                           15(STATUS_BP_AVG_MEASURE), 16(STATUS_BP_AVG_MEASURE_WAIT), 17(STATUS_BP_AVG_MEASURE_END),
                //                           20(STATUS_VEN)
                // data.status.batteryStatus: 0(no charge), 1(charging), 2(charging complete), 3(low battery)
                // data.status.percent: 0-100
                // data.status.avgCnt: Valid in bp avg measure x3, measure number index(0, 1, 2)
                // data.status.avgWaitTick: Valid in bp avg measure x3, measure interval wait tick
                // data.param: RtParam
                // data.param.paramDataType: 0(Bp measuring), 1(Bp end), 2(Ecg measuring), 3(Ecg end)
                when(data.param.paramDataType) {
                    0 -> {
                        val bpIng = RtBpIng(data.param.paramData)
                        binding.tvPs.text = "${bpIng.pressure}"
                        binding.tvPrBp.text = "${bpIng.pr}"
                        binding.dataLog.text = "deflate：${if (bpIng.isDeflate) "yes" else "no"}\n" +
                                "pulse wave：${if (bpIng.isPulse) "yes" else "no"}\n" +
                                "x3 index: ${data.status.avgCnt}\n" +
                                "x3 wait tick: ${data.status.avgWaitTick} s"
                    }
                    1 -> {
                        val bpResult = RtBpResult(data.param.paramData)
                        binding.tvSys.text = "${bpResult.sys}"
                        binding.tvDia.text = "${bpResult.dia}"
                        binding.tvMean.text = "${bpResult.mean}"
                        binding.tvPrBp.text = "${bpResult.pr}"
                        binding.dataLog.text = "deflate：${if (bpResult.isDeflate) "yes" else "no"}\n" +
                                "result：${
                                    when (bpResult.result) {
                                        0 -> "Normal"
                                        1 -> "Unable to analyze(cuff is too loose, inflation is slow, slow air leakage, large air volume)"
                                        2 -> "Waveform disorder(arm movement or other interference detected during pumping)"
                                        3 -> "Weak signal, unable to detect pulse wave(clothes with interference sleeves)"
                                        else -> "Equipment error(valve blocking, over-range blood pressure measurement, serious cuff leakage, software system abnormality, hardware system error, and other abnormalities)"
                                    }
                                }"
                    }
                    2 -> {
                        val ecgIng = RtEcgIng(data.param.paramData)
                        binding.hr.text = "${ecgIng.hr}"
                        binding.dataLog.text = "lead status：${if (ecgIng.isLeadOff) "lead off" else "lead on"}\n" +
                                "pool signal：${if (ecgIng.isPoolSignal) "yes" else "no"}\n" +
                                "duration: ${ecgIng.curDuration} s"
                        DataController.receive(data.param.ecgFloats)
                        // sampling rate：250HZ
                        // mV = n * 0.003098 (data.param.ecgFloats = data.param.ecgShorts * 0.003098)
                    }
                    3 -> {
                        val ecgResult = RtEcgResult(data.param.paramData)
                        binding.hr.text = "${ecgResult.hr}"
                        binding.dataLog.text = "result：${ecgResult.diagnosis.resultMess}\n" +
                                "hr：${ecgResult.hr}\n" +
                                "qrs：${ecgResult.qrs}\n" +
                                "pvcs：${ecgResult.pvcs}\n" +
                                "qtc：${ecgResult.qtc}"
                        // ecgResult.diagnosis：EcgDiagnosis
                        // diagnosis.isRegular：Whether Regular ECG Rhythm
                        // diagnosis.isPoorSignal：Whether Unable to analyze
                        // diagnosis.isLeadOff：Whether Always lead off
                        // diagnosis.isFastHr：Whether Fast Heart Rate
                        // diagnosis.isSlowHr：Whether Slow Heart Rate
                        // diagnosis.isIrregular：Whether Irregular ECG Rhythm
                        // diagnosis.isPvcs：Whether Possible ventricular premature beats
                        // diagnosis.isHeartPause：Whether Possible heart pause
                        // diagnosis.isFibrillation：Whether Possible Atrial fibrillation
                        // diagnosis.isWideQrs：Whether Wide QRS duration
                        // diagnosis.isProlongedQtc：Whether QTc is prolonged
                        // diagnosis.isShortQtc：Whether QTc is short
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFileList)
            .observe(this) {
                fileNames = it.data as ArrayList<String>
                binding.dataLog.text = "$fileNames"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileError)
            .observe(this) {
                val data = it.data as String
                binding.dataLog.text = "EventBp2wReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadingFileProgress)
            .observe(this) {
                val data = it.data as Int  // 0-100
                binding.dataLog.text = "${fileNames[0]} $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileComplete)
            .observe(this) {
                val data = it.data as Bp2wFile
                // data.type: 1(BP), 2(ECG)
                if (data.type == 1) {
                    val file = BpFile(data.content)
                    // file.measureTime：unit（s）
                    // file.measureMode：0（x1），1（x3）
                    Log.d(TAG, "BpFile : $file")
                } else if (data.type == 2) {
                    val file = EcgFile(data.content)
                    val ecgData = EcgData()
                    val startTime = DateUtil.getSecondTimestamp(data.fileName)
                    ecgData.fileName = data.fileName
                    ecgData.duration = file.recordingTime
                    ecgData.shortData = file.waveShortData
                    ecgData.startTime = startTime
                    ecgList.add(ecgData)
                    ecgAdapter.setNewInstance(ecgList)
                    ecgAdapter.notifyDataSetChanged()
                    // sampling rate：125HZ
                    // mV = file.waveShortData * 0.003098
                    // file.measureTime：unit（s）
                    // file.recordingTime：unit（s）
                    // file.connectCable: Whether the cable is connected
                    // file.diagnosis：EcgDiagnosis
                    // diagnosis.isRegular：Whether Regular ECG Rhythm
                    // diagnosis.isPoorSignal：Whether Unable to analyze
                    // diagnosis.isLeadOff：Whether Always lead off
                    // diagnosis.isFastHr：Whether Fast Heart Rate
                    // diagnosis.isSlowHr：Whether Slow Heart Rate
                    // diagnosis.isIrregular：Whether Irregular ECG Rhythm
                    // diagnosis.isPvcs：Whether Possible ventricular premature beats
                    // diagnosis.isHeartPause：Whether Possible heart pause
                    // diagnosis.isFibrillation：Whether Possible Atrial fibrillation
                    // diagnosis.isWideQrs：Whether Wide QRS duration
                    // diagnosis.isProlongedQtc：Whether QTc is prolonged
                    // diagnosis.isShortQtc：Whether QTc is short
                    Log.d(TAG, "EcgFile : $file")
                }
                fileNames.removeAt(0)
                readFile()
            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
        BleServiceHelper.BleServiceHelper.bp2wReadFile(model, fileNames[0])
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // 蓝牙状态 Ble.State
        Log.d(TAG, "model $model, state: $state")

        _bleState.value = state == Ble.State.CONNECTED
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        waveHandler.removeCallbacks(ecgWaveTask)
        BleServiceHelper.BleServiceHelper.stopRtTask(model)
        DataController.clear()
        dataEcgSrc.value = null
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

}