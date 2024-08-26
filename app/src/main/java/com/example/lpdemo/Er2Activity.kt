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
import com.example.lpdemo.databinding.ActivityEr2Binding
import com.example.lpdemo.utils.*
import com.example.lpdemo.views.EcgBkg
import com.example.lpdemo.views.EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.constants.Constant
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.CrcError
import com.lepu.blepro.ext.er2.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.DecompressUtil
import kotlin.collections.ArrayList
import kotlin.math.floor

class Er2Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Er2Activity"
    // Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2,
    // Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3, Bluetooth.MODEL_ER2_S
    private var model = Bluetooth.MODEL_ER2
    private lateinit var binding: ActivityEr2Binding

    private var config = Er2Config()

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
        binding = ActivityEr2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        DataController.nWave = 1
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
                    val intent = Intent(this@Er2Activity, WaveEcgActivity::class.java)
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
            BleServiceHelper.BleServiceHelper.er2GetInfo(model)
        }
        binding.factoryReset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er2FactoryReset(model)
        }
        binding.getConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er2GetConfig(model)
        }
        binding.setConfig.setOnClickListener {
            config.isSoundOn = !config.isSoundOn
            BleServiceHelper.BleServiceHelper.er2SetConfig(model, config)
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
            BleServiceHelper.BleServiceHelper.er2GetFileList(model)
        }
        binding.readFile.setOnClickListener {
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
            readFile()
        }
        bleState.observe(this) {
            if (it) {
                binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                waveHandler.removeCallbacks(ecgWaveTask)
                BleServiceHelper.BleServiceHelper.stopRtTask(model)
                binding.bleState.setImageResource(R.mipmap.bluetooth_error)
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
                val index = floor((binding.ecgBkg.width / dm.xdpi * 25.4 / 25 * 125) * DataController.speed).toInt()
                DataController.maxIndex = index
                dataEcgSrc.value = null
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun initEcgView() {
        // cal screen
        val dm = resources.displayMetrics
        val index = floor((binding.ecgBkg.width / dm.xdpi * 25.4 / 25 * 125) * DataController.speed).toInt()
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Info)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryReset)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEr2FactoryReset $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2GetConfig)
            .observe(this) {
                config = it.data as Er2Config
                binding.dataLog.text = "$config"
                // config.soundOn
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEr2SetConfig $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2RtData)
            .observe(this) {
                val data = it.data as RtData
                DataController.receive(data.wave.ecgFloats)
                binding.hr.text = "${data.param.hr}"
                binding.dataLog.text = "${data.param}"
                // sampling rate：125HZ
                // mV = n * 0.002467（data.wave.ecgFloats = data.wave.ecgShorts * 0.002467）
                // data.param.batteryState：0（no charge），1（charging），2（charging complete），3（low battery）
                // data.param.battery：0-100
                // data.param.recordTime：unit（s）
                // data.param.curStatus：0（idle），1（preparing），2（measuring），3（saving file），4（saving succeed），
                //                       5（less than 30s, file not saved），6（6 retests），7（lead off）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList)
            .observe(this) {
                fileNames = it.data as ArrayList<String>
                binding.dataLog.text = "$fileNames"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEr1ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadingFileProgress)
            .observe(this) {
                val data = it.data as Int  // 0-100
                binding.dataLog.text = "${fileNames[0]} $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileComplete)
            .observe(this) {
                val data = it.data as Er2File
                if (data.fileName.contains("a")) {
                    val file = Er2AnalysisFile(data.content)
                    // file.recordingTime：unit（s）
                    // file.resultList.diagnosis：Er2EcgDiagnosis
                    // diagnosis.isRegular：Whether Regular ECG Rhythm
                    // diagnosis.isPoorSignal：Whether Unable to analyze
                    // diagnosis.isLessThan30s：Whether Less than 30s (no analysis if less than 30s)
                    // diagnosis.isMoving：Whether Action detected (not analyzed)
                    // diagnosis.isFastHr：Whether Fast Heart Rate
                    // diagnosis.isSlowHr：Whether Slow Heart Rate
                    // diagnosis.isIrregular：Whether Irregular ECG Rhythm
                    // diagnosis.isPvcs：Whether Possible ventricular premature beats
                    // diagnosis.isHeartPause：Whether Possible heart pause
                    // diagnosis.isFibrillation：Whether Possible Atrial fibrillation
                    // diagnosis.isWideQrs：Whether Wide QRS duration
                    // diagnosis.isProlongedQtc：Whether QTc is prolonged
                    // diagnosis.isShortQtc：Whether QTc is short
                    // diagnosis.isStElevation：Whether ST segment elevation
                    // diagnosis.isStDepression：Whether ST segment depression
                } else if (data.fileName.contains("R")) {
                    val file = Er2EcgFile(data.content)
                    val ecgShorts = DecompressUtil.er1Decompress(file.waveData)
                    val ecgData = EcgData()
                    val startTime = DateUtil.getSecondTimestamp(data.fileName.replace("R", ""))
                    ecgData.fileName = data.fileName
                    ecgData.duration = file.recordingTime
                    ecgData.shortData = ecgShorts
                    ecgData.startTime = startTime
                    ecgList.add(ecgData)
                    ecgAdapter.setNewInstance(ecgList)
                    ecgAdapter.notifyDataSetChanged()
                    // sampling rate：125HZ
                    // mV = n * 0.002467（ecgFloats = ecgShorts * 0.002467）
                    // file.recordingTime：unit（s）
                }
                fileNames.removeAt(0)
                readFile()
            }
        LiveEventBus.get<CrcError>(EventMsgConst.Cmd.EventCmdCrcError)
            .observe(this) {
                when (it.model) {
                    Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2,
                    Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3, Bluetooth.MODEL_ER2_S -> {
                        when (it.type) {
                            Constant.ErrorType.READ_FILE_TYPE -> {
                                readFile()
                            }
                            Constant.ErrorType.GET_INFO_TYPE -> {
                                BleServiceHelper.BleServiceHelper.er2GetInfo(it.model)
                            }
                        }
                    }
                }
            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
        BleServiceHelper.BleServiceHelper.er2ReadFile(model, fileNames[0])
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