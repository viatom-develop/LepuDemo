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
import com.example.lpdemo.databinding.ActivityBp3Binding
import com.example.lpdemo.utils.*
import com.example.lpdemo.views.EcgBkg
import com.example.lpdemo.views.EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.constants.Constant
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.bp3.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlin.math.floor

class Bp3Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Bp3Activity"
    // Bluetooth.MODEL_BP3D
    private var model = Bluetooth.MODEL_BP3D
    private lateinit var binding: ActivityBp3Binding

    private var config = Bp3Config()

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
        binding = ActivityBp3Binding.inflate(layoutInflater)
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
                    val intent = Intent(this@Bp3Activity, WaveEcgActivity::class.java)
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
            BleServiceHelper.BleServiceHelper.bp3GetInfo(model)
        }
        binding.factoryReset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bp3FactoryReset(model)
        }
        binding.getConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bp3GetConfig(model)
        }
        binding.setConfig.setOnClickListener {
            config.isSoundOn = !config.isSoundOn
            config.avgMeasureMode = 1
            config.volume = 0
            // config.avgMeasureMode: 0(bp x3 off), 1(bp x3 on, interval 30s), 2(bp x3 on, interval 60s),
            //                        3(bp x3 on, interval 90s), 4(bp x3 on, interval 120s)
            // config.volume: 0(off), 1, 2, 3
            BleServiceHelper.BleServiceHelper.bp3SetConfig(model, config)
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
            // Constant.Bp3FileType.USER_TYPE
            // Constant.Bp3FileType.BP_TYPE
            // Constant.Bp3FileType.ECG_TYPE
            BleServiceHelper.BleServiceHelper.bp3GetFileList(model, Constant.Bp3FileType.BP_TYPE)
//            BleServiceHelper.BleServiceHelper.bp3GetFileList(model, Constant.Bp3FileType.ECG_TYPE)
//            BleServiceHelper.BleServiceHelper.bp3GetFileList(model, Constant.Bp3FileType.USER_TYPE)
        }
        binding.getCrc.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bp3GetFileListCrc(model, Constant.Bp3FileType.USER_TYPE)
        }
        binding.readFile.setOnClickListener {
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
            readFile()
        }
        binding.writeUsers.setOnClickListener {
            val userList = UserList()
            userList.fileType = 6
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
            val user = UserList().UserInfo()
            user.aid = 12345
            user.uid = -1
            user.firstName = "测试"
            user.lastName = "一"
            user.birthday = "1991-10-20"
            user.height = 175f
            user.weight = 50f
            user.gender = 0 // 0：男 1：女
            user.icon = BitmapConvertor(this).bp3CreateIcon("测试一")
            userList.userList.add(user)
            val user2 = UserList().UserInfo()
            user2.aid = 12345
            user2.uid = 11111
            user2.firstName = "测试"
            user2.lastName = "2"
            user2.birthday = "1992-1-20"
            user2.height = 165f
            user2.weight = 40f
            user2.gender = 1 // 0：男 1：女
            user2.icon = BitmapConvertor(this).bp3CreateIcon("测试2")
            userList.userList.add(user2)
            val user3 = UserList().UserInfo()
            user3.aid = 12345
            user3.uid = 22222
            user3.firstName = "测试"
            user3.lastName = "three"
            user3.birthday = "1993-6-20"
            user3.height = 165f
            user3.weight = 40f
            user3.gender = 1 // 0：boy 1：girl
            user3.icon = BitmapConvertor(this).bp3CreateIcon("测试three")
            userList.userList.add(user3)
            // userList.size max : 10
            BleServiceHelper.BleServiceHelper.bp3WriteUserList(model, userList)
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3FactoryReset)
            .observe(this) {
                val data = it.data as Boolean
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetConfig)
            .observe(this) {
                // config.soundOn: Heartbeat sound switch
                // config.avgMeasureMode: 0(bp measure x3 off), 1(bp measure x3 on, interval 30s), 2(bp measure x3 on, interval 60s),
                //                        3(bp measure x3 on, interval 90s), 4(bp measure x3 on, interval 120s)
                // config.volume: Voice announcement volume, 0(off), 1, 2, 3
                config = it.data as Bp3Config
                binding.dataLog.text = "$config"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventBp3SetConfig $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3RtData)
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
                when (data.param.paramDataType) {
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
                        binding.dataLog.text =
                            "lead status：${if (ecgIng.isLeadOff) "lead off" else "lead on"}\n" +
                                    "pool signal：${if (ecgIng.isPoolSignal) "Yes" else "No"}\n" +
                                    "duration: ${ecgIng.curDuration} s"
                        DataController.receive(data.param.ecgFloats)
                        // sampling rate：125HZ
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
                    4 -> {
                        val bpEcgIng = RtBpEcgIng(data.param.paramData)
                        binding.hr.text = "${bpEcgIng.hr}"
                        binding.tvPs.text = "${bpEcgIng.pressure}"
                        binding.tvPrBp.text = "${bpEcgIng.pr}"
                        binding.dataLog.text =
                            "lead status：${if (bpEcgIng.isLeadOff) "lead off" else "lead on"}\n" +
                                    "pool signal：${if (bpEcgIng.isPoolSignal) "Yes" else "No"}\n" +
                                    "duration: ${bpEcgIng.curDuration} s\n" +
                                    "deflate：${if (bpEcgIng.isDeflate) "yes" else "no"}\n" +
                                    "pulse wave：${if (bpEcgIng.isPulse) "yes" else "no"}\n" +
                                    "x3 index: ${data.status.avgCnt}\n" +
                                    "x3 wait tick: ${data.status.avgWaitTick} s"
                        DataController.receive(data.param.ecgFloats)
                        // sampling rate：125HZ
                        // mV = n * 0.003098 (data.param.ecgFloats = data.param.ecgShorts * 0.003098)
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3FileList)
            .observe(this) {
                val bleFile = it.data as BleFile
                if (bleFile.type == Constant.Bp3FileType.BP_TYPE) {
                    val file = BpList(bleFile.bytes)
                    for (record in file.recordList) {
                        if (record.isWaveFlag) {
                            fileNames.add("BP${record.fileName.substring(2)}")
                        }
                    }
                    binding.dataLog.text = "$fileNames"
                } else if (bleFile.type == Constant.Bp3FileType.ECG_TYPE) {
                    val file = EcgList(bleFile.bytes)
                    for (record in file.recordList) {
                        fileNames.add(record.fileName)
                    }
                    binding.dataLog.text = "$fileNames"
                } else if (bleFile.type == Constant.Bp3FileType.USER_TYPE) {
                    val file = UserList(bleFile.bytes)
                    binding.dataLog.text = "$file"
                }

                // record.startTime: unit(s)
                // record.measureMode: 0(x1), 1(x3)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadingFileProgress)
            .observe(this) {
                val data = it.data as Int  // 0-100
                if (fileNames.size != 0) {
                    binding.dataLog.text = "${fileNames[0]} $data %"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadFileComplete)
            .observe(this) {
                val bleFile = it.data as BleFile
                if (bleFile.type == Constant.Bp3FileType.ECG_TYPE) {
                    val file = EcgFile(bleFile.bytes, bleFile.fileName)
                    val ecgData = EcgData()
                    ecgData.fileName = file.fileName
                    ecgData.duration = file.duration
                    ecgData.shortData = file.waveShortData
                    ecgData.startTime = file.startTime
                    ecgList.add(ecgData)
                    ecgAdapter.setNewInstance(ecgList)
                    ecgAdapter.notifyDataSetChanged()
                    Log.d(TAG, "EcgFile : $file")
                    // sampling rate：125HZ
                    // mV = file.waveShortData * 0.003098
                    // file.startTime: unit(s)
                    // file.duration：unit（s）
                } else if (bleFile.type == Constant.Bp3FileType.BP_WAVE_TYPE) {
                    val file = BpWaveFile(bleFile.bytes)
                    Log.d(TAG, "BpWaveFile : $file")
                }
                fileNames.removeAt(0)
                readFile()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3WritingFileProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "EventBp3WritingFileProgress $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3WriteFileComplete)
            .observe(this) {
                val data = it.data as ListCrc
                binding.dataLog.text = "EventBp3WriteFileComplete $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetFileListCrc)
            .observe(this) {
                val data = it.data as ListCrc
                binding.dataLog.text = "EventBp3GetFileListCrc $data"
            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
        BleServiceHelper.BleServiceHelper.bp3ReadFile(model, fileNames[0])
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