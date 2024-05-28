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
import com.example.lpdemo.databinding.ActivityEr1Binding
import com.example.lpdemo.utils.*
import com.example.lpdemo.views.EcgBkg
import com.example.lpdemo.views.EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.er1.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.DateUtil
import kotlin.collections.ArrayList
import kotlin.math.floor

class Er1Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Er1Activity"
    // Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1
    private var model = Bluetooth.MODEL_ER1
    private lateinit var binding: ActivityEr1Binding

    private var config = Er1Config()

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
        binding = ActivityEr1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
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
                    val intent = Intent(this@Er1Activity, WaveEcgActivity::class.java)
                    intent.putExtra("model", model)
                    ecgData.startTime = it.startTime
                    ecgData.fileName = it.fileName
                    startActivity(intent)
                }
            }
        }
        binding.ecgBkg.post {
            initEcgView()
        }
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er1GetInfo(model)
        }
        binding.factoryReset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er1FactoryReset(model)
        }
        binding.getConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er1GetConfig(model)
        }
        binding.setConfig.setOnClickListener {
            config.isVibration = !config.isVibration
            BleServiceHelper.BleServiceHelper.er1SetConfig(model, config)
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
            BleServiceHelper.BleServiceHelper.er1GetFileList(model)
        }
        binding.readFile.setOnClickListener {
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
            readFile()
        }
        binding.cancelReadFile.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er1CancelReadFile(model)
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
        DataController.nWave = 1
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Info)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactory)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEr1ResetFactory $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1GetConfig)
            .observe(this) {
                config = it.data as Er1Config
                binding.dataLog.text = "$config"
                // config.vibration
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEr1SetConfig $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1RtData)
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1FileList)
            .observe(this) {
                fileNames = it.data as ArrayList<String>
                binding.dataLog.text = "$fileNames"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEr1ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadingFileProgress)
            .observe(this) {
                val data = it.data as Int  // 0-100
                binding.dataLog.text = "${fileNames[0]} $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileComplete)
            .observe(this) {
                val rawFile = getOffset(it.model, fileNames[0], "")
                val data = it.data as Er1File
                if (it.model == Bluetooth.MODEL_ER1_N) {
                    val file = if (rawFile.isEmpty()) {
                        Er1HrFile(data.content)
                    } else {
                        Er1HrFile(rawFile)
                    }
                    // file.recordingTime：unit（s）
                } else {
                    val file = if (rawFile.isEmpty()) {
                        Er1EcgFile(data.content)
                    } else {
                        Er1EcgFile(rawFile)
                    }
                    val ecgData = EcgData()
                    val startTime = DateUtil.getSecondTimestamp(data.fileName.replace("R", ""))
                    ecgData.fileName = data.fileName
                    ecgData.duration = file.recordingTime
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
        LiveEventBus.get<Int>(EventMsgConst.Download.EventIsCancel)
            .observe(this) {
                // model

            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
        val offset = getOffset(model, fileNames[0], "")
        BleServiceHelper.BleServiceHelper.er1ReadFile(model, fileNames[0], "", offset.size)
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