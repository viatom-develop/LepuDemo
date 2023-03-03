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
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.er1.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.Er1Decompress
import com.lepu.blepro.utils.HexString
import kotlinx.android.synthetic.main.activity_er1.*
import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.floor

class Er1Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Er1Activity"
    // Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1
    private var model = Bluetooth.MODEL_ER1

    private var config = Er1Config()

    private var fileNames = arrayListOf<String>()
    private lateinit var ecgAdapter: EcgAdapter
    var ecgList: ArrayList<EcgData> = arrayListOf()

    private lateinit var ecgBkg: EcgBkg
    private lateinit var ecgView: EcgView
    private var isStartRtTask = false
    /**
     * rt wave
     */
    private val waveHandler = Handler()
    private val ecgWaveTask = EcgWaveTask()

    inner class EcgWaveTask : Runnable {
        override fun run() {
            if (!isStartRtTask) return
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
        setContentView(R.layout.activity_er1)
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
                    val intent = Intent(this@Er1Activity, WaveEcgActivity::class.java)
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
            BleServiceHelper.BleServiceHelper.er1GetInfo(model)
        }
        factory_reset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er1FactoryReset(model)
        }
        get_config.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er1GetConfig(model)
        }
        set_config.setOnClickListener {
            config.isVibration = !config.isVibration
            BleServiceHelper.BleServiceHelper.er1SetConfig(model, config)
        }
        start_rt_task.setOnClickListener {
            isStartRtTask = true
            if (BleServiceHelper.BleServiceHelper.isRtStop(model)) {
                waveHandler.post(ecgWaveTask)
                BleServiceHelper.BleServiceHelper.startRtTask(model)
            }
        }
        stop_rt_task.setOnClickListener {
            isStartRtTask = false
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
        }
        get_file_list.setOnClickListener {
            fileNames.clear()
            ecgList.clear()
            ecgAdapter.setNewInstance(ecgList)
            ecgAdapter.notifyDataSetChanged()
            BleServiceHelper.BleServiceHelper.er1GetFileList(model)
        }
        read_file.setOnClickListener {
            if (isStartRtTask) {
                isStartRtTask = false
                waveHandler.removeCallbacks(ecgWaveTask)
                BleServiceHelper.BleServiceHelper.stopRtTask(model)
            }
            readFile()
        }
        cancel_read_file.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er1CancelReadFile(model)
        }
        bleState.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
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
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Info)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactory)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventEr1ResetFactory $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1GetConfig)
            .observe(this) {
                config = it.data as Er1Config
                data_log.text = "$config"
                // config.vibration
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventEr1SetConfig $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1RtData)
            .observe(this) {
                val data = it.data as RtData
                DataController.receive(data.wave.ecgFloats)
                hr.text = "${data.param.hr}"
                data_log.text = "${data.param}"
                // sampling rate：125HZ
                // 1mV = n * 0.002467（data.wave.ecgFloats = data.wave.ecgShorts * 0.002467）
                // data.param.batteryState：0（no charge），1（charging），2（charging complete），3（low battery）
                // data.param.battery：0-100
                // data.param.recordTime：unit（s）
                // data.param.curStatus：0（idle），1（preparing），2（measuring），3（saving file），4（saving succeed），
                //                       5（less than 30s, file not saved），6（6 retests），7（lead off）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1FileList)
            .observe(this) {
                fileNames = it.data as ArrayList<String>
                data_log.text = "$fileNames"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventEr1ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadingFileProgress)
            .observe(this) {
                val data = it.data as Int  // 0-100
                data_log.text = "${fileNames[0]} $data %"
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
                    val ecgShorts = Er1Decompress.unCompressAlgECG(file.waveData)
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
                    // 1mV = n * 0.002467（ecgFloats = ecgShorts * 0.002467）
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

    // sdk save the original file name : userId + fileName + .dat
    private fun getOffset(model: Int, fileName: String, userId: String): ByteArray {
        val trimStr = HexString.trimStr(fileName)
        BleServiceHelper.BleServiceHelper.rawFolder?.get(model)?.let { s ->
            val mFile = File(s, "$userId$trimStr.dat")
            if (mFile.exists()) {
                FileUtils.readFileToByteArray(mFile)?.let {
                    return it
                }
            } else {
                return ByteArray(0)
            }
        }
        return ByteArray(0)
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
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

}