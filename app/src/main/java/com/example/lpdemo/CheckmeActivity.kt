package com.example.lpdemo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityCheckmeBinding
import com.example.lpdemo.utils.*
import com.example.lpdemo.views.EcgBkg
import com.example.lpdemo.views.EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.constants.Constant
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.checkme.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlin.math.floor

class CheckmeActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "CheckmeActivity"
    // Bluetooth.MODEL_CHECKME
    private var model = Bluetooth.MODEL_CHECKME
    private lateinit var binding: ActivityCheckmeBinding

    private var fileNames = arrayListOf<String>()
    private var userIds = mutableListOf<Int>()
    private var type = Constant.CheckmeListType.ECG_TYPE

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
        binding = ActivityCheckmeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
        BleServiceHelper.BleServiceHelper.syncTime(model)
    }

    private fun initView() {
        binding.bleName.text = deviceName
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeGetInfo(model)
        }
        binding.getUserList.setOnClickListener {
            userIds.clear()
            BleServiceHelper.BleServiceHelper.checkmeGetFileList(model, Constant.CheckmeListType.USER_TYPE)
        }
        binding.getTempList.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeGetFileList(model, Constant.CheckmeListType.TEMP_TYPE)
        }
        binding.getOxyList.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeGetFileList(model, Constant.CheckmeListType.OXY_TYPE)
        }
        binding.getGluList.setOnClickListener {
            // userIds
            BleServiceHelper.BleServiceHelper.checkmeGetFileList(model, Constant.CheckmeListType.GLU_TYPE, 1)
        }
        binding.getDlcList.setOnClickListener {
            type = Constant.CheckmeListType.DLC_TYPE
            BleServiceHelper.BleServiceHelper.checkmeGetFileList(model, Constant.CheckmeListType.DLC_TYPE, 1)
        }
        binding.getPedList.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeGetFileList(model, Constant.CheckmeListType.PED_TYPE, 2)
        }
        binding.getBpList.setOnClickListener {
            BleServiceHelper.BleServiceHelper.checkmeGetFileList(model, Constant.CheckmeListType.BP_TYPE, 1)
        }
        binding.getEcgList.setOnClickListener {
            type = Constant.CheckmeListType.ECG_TYPE
            BleServiceHelper.BleServiceHelper.checkmeGetFileList(model, Constant.CheckmeListType.ECG_TYPE)
        }
        binding.getSlmList.setOnClickListener {
            type = Constant.CheckmeListType.SLM_TYPE
            BleServiceHelper.BleServiceHelper.checkmeGetFileList(model, Constant.CheckmeListType.SLM_TYPE)
        }
        binding.readFile.setOnClickListener {
            // 1. get list first 2. then read file
            readFile()
        }
        binding.ecgBkg.post {
            initEcgView()
        }
        bleState.observe(this) {
            if (it) {
                binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
                waveHandler.removeCallbacks(ecgWaveTask)
                waveHandler.postDelayed(ecgWaveTask, 1000)
            } else {
                waveHandler.removeCallbacks(ecgWaveTask)
                binding.bleState.setImageResource(R.mipmap.bluetooth_error)
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
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
        val index = floor(binding.ecgBkg.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        DataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        DataController.mm2px = mm2px

        binding.ecgBkg.measure(0, 0)
        ecgBkg = EcgBkg(this)
        binding.ecgBkg.addView(ecgBkg)

        binding.ecgView.measure(0, 0)
        ecgView = EcgView(this)
        binding.ecgView.addView(ecgView)

        waveHandler.removeCallbacks(ecgWaveTask)
        waveHandler.postDelayed(ecgWaveTask, 1000)

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeRtData)
            .observe(this) {
                val data = it.data as RtData
                binding.tvOxy.text = "${data.spo2}"
                binding.tvPr.text = "${data.pr}"
                binding.tvPi.text = "${data.pi}"
                DataController.receive(data.ecgFloatData)
                binding.hr.text = "${data.hr}"
                binding.dataLog.text = "$data"
                // sampling rate：125HZ
                // mV = n * 0.010769600512711（data.ecgFloatData = data.ecgShortData * 0.010769600512711）
                // data.battery：0-100
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetFileListError)
            .observe(this) {
                val data = it.data as Int // Constant.CheckmeListType
                binding.dataLog.text = "GetFileListError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetFileListProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "GetFileListProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetBpList)
            .observe(this) {
                val data = it.data as ArrayList<BpRecord>
                binding.dataLog.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetPedList)
            .observe(this) {
                val data = it.data as ArrayList<PedRecord>
                binding.dataLog.text = data.toString()
                // data.distance : km
                // data.avgSpeed : km/s
                // data.calorie : kcal
                // data.fat : g
                // data.duration : s
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetOxyList)
            .observe(this) {
                val data = it.data as ArrayList<OxyRecord>
                binding.dataLog.text = data.toString()
                // data.measureMode : 0(Internal leads), 1(External leads)
                // data.result : 0(normal), 1(abnormal)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetGluList)
            .observe(this) {
                val data = it.data as ArrayList<GluRecord>
                binding.dataLog.text = data.toString()
                // data.glu : mg/dL
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetTempList)
            .observe(this) {
                val data = it.data as ArrayList<TempRecord>
                binding.dataLog.text = data.toString()
                // data.measureMode : 0(body temperature), 1(object temperature)
                // data.temp : ℃
                // data.result : 0(normal), 1(abnormal)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetUserList)
            .observe(this) {
                val data = it.data as ArrayList<UserInfo>
                binding.dataLog.text = data.toString()
                for (i in data) {
                    userIds.add(i.id)
                }
                // data.sex : 0(boy), 1(girl)
                // data.weight : kg
                // data.height : cm
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetEcgList)
            .observe(this) {
                val data = it.data as ArrayList<EcgRecord>
                for (i in data) {
                    fileNames.add(i.recordName)
                }
                binding.dataLog.text = data.toString()
                // data.measureMode : 1：Hand-Hand，2：Hand-Chest，3：1-Lead，4：2-Lead
                // data.result : 0(normal), 1(abnormal)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetDlcList)
            .observe(this) {
                val data = it.data as ArrayList<DlcRecord>
                for (i in data) {
                    fileNames.add(i.recordName)
                }
                binding.dataLog.text = data.toString()
                // data.ecgResult : 0(normal), 1(abnormal)
                // data.spo2Result : 0(normal), 1(abnormal)
                // data.bpiResult : 0(normal), 1(abnormal)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetSlmList)
            .observe(this) {
                val data = it.data as ArrayList<SlmRecord>
                for (i in data) {
                    fileNames.add(i.recordName)
                }
                binding.dataLog.text = data.toString()
                // data.lowSpo2Time : s
                // data.result : 0(normal), 1(abnormal)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadFileError)
            .observe(this) {
                val data = it.data as Int  // Constant.CheckmeFileType
                binding.dataLog.text = "ReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "ReadingFileProgress $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadEcgFileComplete)
            .observe(this) {
                val data = it.data as EcgFile
                Log.d(TAG, "data: $data")
                binding.dataLog.text = "$data"
                fileNames.removeAt(0)
                readFile()
                // sampling rate：500HZ
                // mV = n * 0.0012820952991323（data.wFs = data.waveShortData * 0.0012820952991323）
                // data.result：CheckmeEcgDiagnosis
                // data.result.isRegular：Whether Regular ECG Rhythm
                // data.result.isPoorSignal：Whether Unable to analyze
                // data.result.isHighHr：Whether High Heart Rate
                // data.result.isLowHr：Whether Low Heart Rate
                // data.result.isIrregular：Whether Irregular ECG Rhythm
                // data.result.isHighQrs：Whether High QRS Value
                // data.result.isHighSt：Whether High ST Value
                // data.result.isLowSt：Whether Low ST Value
                // data.result.isPrematureBeat：Whether Suspected Premature Beat
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadSlmFileComplete)
            .observe(this) {
                // Store a point every two seconds
                val data = it.data as SlmFile
                Log.d(TAG, "data: $data")
                binding.dataLog.text = "$data"
                fileNames.removeAt(0)
                readFile()
            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
        if (type == Constant.CheckmeListType.SLM_TYPE) {
            BleServiceHelper.BleServiceHelper.checkmeReadFile(model, fileNames[0], Constant.CheckmeFileType.SLM_TYPE)
        } else {
            BleServiceHelper.BleServiceHelper.checkmeReadFile(model, fileNames[0], Constant.CheckmeFileType.ECG_TYPE)
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