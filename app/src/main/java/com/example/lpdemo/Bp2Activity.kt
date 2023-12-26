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
import com.lepu.blepro.ext.bp2.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.FilterUtil
import kotlinx.android.synthetic.main.activity_bp2.*
import kotlin.math.floor

class Bp2Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Bp2Activity"
    // Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T
    private var model = Bluetooth.MODEL_BP2

    private var config = Bp2Config()

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
        setContentView(R.layout.activity_bp2)
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
                    val intent = Intent(this@Bp2Activity, WaveEcgActivity::class.java)
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
            BleServiceHelper.BleServiceHelper.bp2GetInfo(model)
        }
        factory_reset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bp2FactoryReset(model)
        }
        get_config.setOnClickListener {
            BleServiceHelper.BleServiceHelper.bp2GetConfig(model)
        }
        set_config.setOnClickListener {
            config.isSoundOn = !config.isSoundOn
            BleServiceHelper.BleServiceHelper.bp2SetConfig(model, config)
        }
        start_rt_task.setOnClickListener {
            waveHandler.removeCallbacks(ecgWaveTask)
            waveHandler.postDelayed(ecgWaveTask, 1000)
            BleServiceHelper.BleServiceHelper.startRtTask(model)
        }
        stop_rt_task.setOnClickListener {
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
        }
        get_file_list.setOnClickListener {
            fileNames.clear()
            ecgList.clear()
            ecgAdapter.setNewInstance(ecgList)
            ecgAdapter.notifyDataSetChanged()
            BleServiceHelper.BleServiceHelper.bp2GetFileList(model)
        }
        read_file.setOnClickListener {
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
            readFile()
        }
        bleState.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                bp_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                waveHandler.removeCallbacks(ecgWaveTask)
                BleServiceHelper.BleServiceHelper.stopRtTask(model)
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                bp_ble_state.setImageResource(R.mipmap.bluetooth_error)
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
        // cal screen, 250HZ
        DataController.nWave = 2
        val dm = resources.displayMetrics
        val index = floor(ecg_bkg.width / dm.xdpi * 25.4 / 25 * 250).toInt()
        DataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        DataController.mm2px = mm2px

        ecg_bkg.measure(0, 0)
        ecgBkg = EcgBkg(this)
        ecg_bkg.addView(ecgBkg)

        ecg_view.measure(0, 0)
        ecgView = EcgView(this)
        ecg_view.addView(ecgView)
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Info)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset)
            .observe(this) {
                val data = it.data as Boolean
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfig)
            .observe(this) {
                config = it.data as Bp2Config
                // config.soundOn: Heartbeat sound switch
                data_log.text = "$config"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventBp2SetConfig $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData)
            .observe(this) {
                val data = it.data as RtData
                // data.status: RtStatus
                // data.status.deviceStatus: 0(STATUS_SLEEP), 1(STATUS_MEMERY), 2(STATUS_CHARGE), 3(STATUS_READY),
                //                           4(STATUS_BP_MEASURING), 5(STATUS_BP_MEASURE_END),
                //                           6(STATUS_ECG_MEASURING), 7(STATUS_ECG_MEASURE_END), 20(STATUS_VEN)
                // data.status.batteryStatus: 0(no charge), 1(charging), 2(charging complete), 3(low battery)
                // data.status.percent: 0-100
                // data.param: RtParam
                // data.param.paramDataType: 0(Bp measuring), 1(Bp end), 2(Ecg measuring), 3(Ecg end)
                when(data.param.paramDataType) {
                    0 -> {
                        val bpIng = RtBpIng(data.param.paramData)
                        tv_ps.text = "${bpIng.pressure}"
                        tv_pr_bp.text = "${bpIng.pr}"
                        data_log.text = "deflate：${if (bpIng.isDeflate) "yes" else "no"}\n" +
                                "pulse wave：${if (bpIng.isPulse) "yes" else "no"}"
                    }
                    1 -> {
                        val bpResult = RtBpResult(data.param.paramData)
                        tv_sys.text = "${bpResult.sys}"
                        tv_dia.text = "${bpResult.dia}"
                        tv_mean.text = "${bpResult.mean}"
                        tv_pr_bp.text = "${bpResult.pr}"
                        data_log.text = "deflate：${if (bpResult.isDeflate) "yes" else "no"}\n" +
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
                        hr.text = "${ecgIng.hr}"
                        data_log.text = "lead status：${if (ecgIng.isLeadOff) "lead off" else "lead on"}\n" +
                                "pool signal：${if (ecgIng.isPoolSignal) "yes" else "no"}\n" +
                                "duration: ${ecgIng.curDuration} s"
                        DataController.receive(data.param.ecgFloatsFilter)
                        // sampling rate：250HZ
                        // mV = n * 0.003098 (data.param.ecgFloats = data.param.ecgShorts * 0.003098)
                    }
                    3 -> {
                        val ecgResult = RtEcgResult(data.param.paramData)
                        hr.text = "${ecgResult.hr}"
                        data_log.text = "result：${ecgResult.diagnosis.resultMess}\n" +
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList)
            .observe(this) {
                fileNames = it.data as ArrayList<String>
                data_log.text = "$fileNames"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError)
            .observe(this) {
                val data = it.data as String
                data_log.text = "EventBp2ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress)
            .observe(this) {
                val data = it.data as Int  // 0-100
                data_log.text = "${fileNames[0]} $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete)
            .observe(this) {
                val data = it.data as Bp2File
                // data.type: 1(BP), 2(ECG)
                if (data.type == 1) {
                    val file = BpFile(data.content)
                    // file.measureTime：unit（s）
                    Log.d(TAG, "BpFile : $file")
                } else if (data.type == 2) {
                    val file = EcgFile(data.content)
                    val ecgData = EcgData()
                    val startTime = DateUtil.getSecondTimestamp(data.fileName)
                    ecgData.fileName = data.fileName
                    ecgData.duration = file.recordingTime
                    ecgData.shortData = FilterUtil.getEcgFileFilterData(it.model, data.content)
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
        BleServiceHelper.BleServiceHelper.bp2ReadFile(model, fileNames[0])
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