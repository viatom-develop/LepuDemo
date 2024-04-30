package com.example.lpdemo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityEr3Binding
import com.example.lpdemo.utils.*
import com.example.lpdemo.views.Er3EcgBkg
import com.example.lpdemo.views.Er3EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.er3.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.DecompressUtil
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import kotlin.math.floor

class Er3Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Er3Activity"
    // Bluetooth.MODEL_ER3, Bluetooth.MODEL_M12
    private var model = Bluetooth.MODEL_ER3
    private lateinit var binding: ActivityEr3Binding

    private lateinit var ecgBkg1: Er3EcgBkg
    private lateinit var ecgView1: Er3EcgView
    private lateinit var ecgBkg2: Er3EcgBkg
    private lateinit var ecgView2: Er3EcgView
    private lateinit var ecgBkg3: Er3EcgBkg
    private lateinit var ecgView3: Er3EcgView
    private lateinit var ecgBkg4: Er3EcgBkg
    private lateinit var ecgView4: Er3EcgView
    private lateinit var ecgBkg5: Er3EcgBkg
    private lateinit var ecgView5: Er3EcgView
    private lateinit var ecgBkg6: Er3EcgBkg
    private lateinit var ecgView6: Er3EcgView
    private lateinit var ecgBkg7: Er3EcgBkg
    private lateinit var ecgView7: Er3EcgView
    private lateinit var ecgBkg8: Er3EcgBkg
    private lateinit var ecgView8: Er3EcgView
    private lateinit var ecgBkg9: Er3EcgBkg
    private lateinit var ecgView9: Er3EcgView
    private lateinit var ecgBkg10: Er3EcgBkg
    private lateinit var ecgView10: Er3EcgView
    private lateinit var ecgBkg11: Er3EcgBkg
    private lateinit var ecgView11: Er3EcgView
    private lateinit var ecgBkg12: Er3EcgBkg
    private lateinit var ecgView12: Er3EcgView

    private var isStartRtTask = false
    /**
     * rt wave
     */
    private val waveHandler = Handler()
    private val ecgWaveTask = EcgWaveTask()

    inner class EcgWaveTask : Runnable {
        override fun run() {
            if (!isStartRtTask) {
                return
            }

            val interval: Int = when {
                Er3DataController.dataRec.size > 250*8*2 -> {
                    30
                }
                Er3DataController.dataRec.size > 150*8*2 -> {
                    35
                }
                Er3DataController.dataRec.size > 75*8*2 -> {
                    40
                }
                else -> {
                    45
                }
            }

            waveHandler.postDelayed(this, interval.toLong())

            Er3DataController.draw(10)
            /**
             * update dataEcgSrc
             */
            dataEcgSrc1.value = Er3DataController.src1
            dataEcgSrc2.value = Er3DataController.src2
            dataEcgSrc3.value = Er3DataController.src3
            dataEcgSrc4.value = Er3DataController.src4
            dataEcgSrc5.value = Er3DataController.src5
            dataEcgSrc6.value = Er3DataController.src6
            dataEcgSrc7.value = Er3DataController.src7
            dataEcgSrc8.value = Er3DataController.src8
            dataEcgSrc9.value = Er3DataController.src9
            dataEcgSrc10.value = Er3DataController.src10
            dataEcgSrc11.value = Er3DataController.src11
            dataEcgSrc12.value = Er3DataController.src12
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEr3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
        binding.bkg12.post {
            initEcgView()
        }
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er3GetInfo(model)
        }
        binding.factoryReset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er3FactoryReset(model)
        }
        binding.getMode.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er3GetConfig(model)
        }
        binding.setMode.setOnClickListener {
            // 0: 监护模式0.5-40
            // 1: 手术模式1-20
            // 2: ST模式0.05-40
            BleServiceHelper.BleServiceHelper.er3SetMode(model, 0)
        }
        binding.startRtTask.setOnClickListener {
            isStartRtTask = true
            if (BleServiceHelper.BleServiceHelper.isRtStop(model)) {
                waveHandler.post(ecgWaveTask)
                BleServiceHelper.BleServiceHelper.startRtTask(model)
            }
        }
        binding.stopRtTask.setOnClickListener {
            isStartRtTask = false
            waveHandler.removeCallbacks(ecgWaveTask)
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
        }
        // File data decompress
        binding.decompressTest.setOnClickListener {
            // download file path
            // for example : /assets/W20240111145412
            val file = "${getExternalFilesDir(null)?.absolutePath}/W20240111145412"
            if (!File(file).exists()) return@setOnClickListener
            val bytes = IOUtils.toByteArray(FileInputStream(file), 10)
            val leadType = bytes[2].toInt()
            Thread {
                DecompressUtil.uncompressEcgDataByType(leadType, file)
                // The format of the decompressed data file is two bytes per sampling point in small end mode
                // leadType = 0, 12 channel data, other 8 channel data
                // 12 channel data :
                // W20240111145412_I, W20240111145412_II, W20240111145412_III, W20240111145412_aVF
                // W20240111145412_aVL, W20240111145412_aVR, W20240111145412_V1, W20240111145412_V2
                // W20240111145412_V3, W20240111145412_V4, W20240111145412_V5, W20240111145412_V6
                // 8 channel data :
                // W20240111145412_I, W20240111145412_II, W20240111145412_III, W20240111145412_aVF
                // W20240111145412_aVL, W20240111145412_aVR, W20240111145412_V1, W20240111145412_V5
                val file_I = "${getExternalFilesDir(null)?.absolutePath}/W20240111145412_I"
                if (File(file_I).exists()) {
                    // sampling rate：250HZ
                    // mV = n * 0.00244
                    val data = FileUtils.readFileToByteArray(File(file_I))
                    val shorts = mutableListOf<Float>()
                    for (i in 0 until data.size.div(2)) {
                        shorts.add(toSignedShort(data[i*2], data[i*2+1]) * 0.00244f)
                    }
                }
            }.start()
        }
        bleState.observe(this) {
            if (it) {
                binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.bleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }
        dataEcgSrc1.observe(this) {
            if (this::ecgView1.isInitialized) {
                ecgView1.setDataSrc(it)
                ecgView1.invalidate()
            }
        }
        dataEcgSrc2.observe(this) {
            if (this::ecgView2.isInitialized) {
                ecgView2.setDataSrc(it)
                ecgView2.invalidate()
            }
        }
        dataEcgSrc3.observe(this) {
            if (this::ecgView3.isInitialized) {
                ecgView3.setDataSrc(it)
                ecgView3.invalidate()
            }
        }
        dataEcgSrc4.observe(this) {
            if (this::ecgView4.isInitialized) {
                ecgView4.setDataSrc(it)
                ecgView4.invalidate()
            }
        }
        dataEcgSrc5.observe(this) {
            if (this::ecgView5.isInitialized) {
                ecgView5.setDataSrc(it)
                ecgView5.invalidate()
            }
        }
        dataEcgSrc6.observe(this) {
            if (this::ecgView6.isInitialized) {
                ecgView6.setDataSrc(it)
                ecgView6.invalidate()
            }
        }
        dataEcgSrc7.observe(this) {
            if (this::ecgView7.isInitialized) {
                ecgView7.setDataSrc(it)
                ecgView7.invalidate()
            }
        }
        dataEcgSrc8.observe(this) {
            if (this::ecgView8.isInitialized) {
                ecgView8.setDataSrc(it)
                ecgView8.invalidate()
            }
        }
        dataEcgSrc9.observe(this) {
            if (this::ecgView9.isInitialized) {
                ecgView9.setDataSrc(it)
                ecgView9.invalidate()
            }
        }
        dataEcgSrc10.observe(this) {
            if (this::ecgView10.isInitialized) {
                ecgView10.setDataSrc(it)
                ecgView10.invalidate()
            }
        }
        dataEcgSrc11.observe(this) {
            if (this::ecgView11.isInitialized) {
                ecgView11.setDataSrc(it)
                ecgView11.invalidate()
            }
        }
        dataEcgSrc12.observe(this) {
            if (this::ecgView12.isInitialized) {
                ecgView12.setDataSrc(it)
                ecgView12.invalidate()
            }
        }
    }

    private fun initEcgView() {
        // cal screen
        val dm = resources.displayMetrics
        // 最多可以画多少点=屏幕宽度像素/每英寸像素*25.4mm/25mm/s走速*250个点/s
        val index = floor(binding.bkg1.width / dm.xdpi * 25.4 / 25 * 250).toInt()
        Er3DataController.maxIndex = index

        // 每像素占多少mm=每英寸长25.4mm/每英寸像素
        val mm2px = 25.4f / dm.xdpi
        Er3DataController.mm2px = mm2px

        binding.bkg1.measure(0, 0)
        ecgBkg1 = Er3EcgBkg(this)
        binding.bkg1.addView(ecgBkg1)
        binding.view1.measure(0, 0)
        ecgView1 = Er3EcgView(this)
        binding.view1.addView(ecgView1)

        binding.bkg2.measure(0, 0)
        ecgBkg2 = Er3EcgBkg(this)
        binding.bkg2.addView(ecgBkg2)
        binding.view2.measure(0, 0)
        ecgView2 = Er3EcgView(this)
        binding.view2.addView(ecgView2)

        binding.bkg3.measure(0, 0)
        ecgBkg3 = Er3EcgBkg(this)
        binding.bkg3.addView(ecgBkg3)
        binding.view3.measure(0, 0)
        ecgView3 = Er3EcgView(this)
        binding.view3.addView(ecgView3)

        binding.bkg4.measure(0, 0)
        ecgBkg4 = Er3EcgBkg(this)
        binding.bkg4.addView(ecgBkg4)
        binding.view4.measure(0, 0)
        ecgView4 = Er3EcgView(this)
        binding.view4.addView(ecgView4)

        binding.bkg5.measure(0, 0)
        ecgBkg5 = Er3EcgBkg(this)
        binding.bkg5.addView(ecgBkg5)
        binding.view5.measure(0, 0)
        ecgView5 = Er3EcgView(this)
        binding.view5.addView(ecgView5)

        binding.bkg6.measure(0, 0)
        ecgBkg6 = Er3EcgBkg(this)
        binding.bkg6.addView(ecgBkg6)
        binding.view6.measure(0, 0)
        ecgView6 = Er3EcgView(this)
        binding.view6.addView(ecgView6)

        binding.bkg7.measure(0, 0)
        ecgBkg7 = Er3EcgBkg(this)
        binding.bkg7.addView(ecgBkg7)
        binding.view7.measure(0, 0)
        ecgView7 = Er3EcgView(this)
        binding.view7.addView(ecgView7)

        binding.bkg8.measure(0, 0)
        ecgBkg8 = Er3EcgBkg(this)
        binding.bkg8.addView(ecgBkg8)
        binding.view8.measure(0, 0)
        ecgView8 = Er3EcgView(this)
        binding.view8.addView(ecgView8)

        binding.bkg9.measure(0, 0)
        ecgBkg9 = Er3EcgBkg(this)
        binding.bkg9.addView(ecgBkg9)
        binding.view9.measure(0, 0)
        ecgView9 = Er3EcgView(this)
        binding.view9.addView(ecgView9)

        binding.bkg10.measure(0, 0)
        ecgBkg10 = Er3EcgBkg(this)
        binding.bkg10.addView(ecgBkg10)
        binding.view10.measure(0, 0)
        ecgView10 = Er3EcgView(this)
        binding.view10.addView(ecgView10)

        binding.bkg11.measure(0, 0)
        ecgBkg11 = Er3EcgBkg(this)
        binding.bkg11.addView(ecgBkg11)
        binding.view11.measure(0, 0)
        ecgView11 = Er3EcgView(this)
        binding.view11.addView(ecgView11)

        binding.bkg12.measure(0, 0)
        ecgBkg12 = Er3EcgBkg(this)
        binding.bkg12.addView(ecgBkg12)
        binding.view12.measure(0, 0)
        ecgView12 = Er3EcgView(this)
        binding.view12.addView(ecgView12)
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3Info)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3FactoryReset)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEr3FactoryReset $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3GetConfigError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEr3GetConfigError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3GetConfig)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "${when (data) {
                    0 -> "监护模式0.5-40"
                    1 -> "手术模式1-20"
                    2 -> "ST模式0.05-40"
                    else -> ""
                }}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventEr3SetConfig $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3RtData)
            .observe(this) {
                val data = it.data as RtData
                Er3DataController.receive(data.wave.waveFloats, data.param.isLeadOffLA, data.param.isLeadOffLL)
                binding.bleBattery.text = "电量：${data.param.battery} %"
                binding.hr.text = "${data.param.hr}"
                binding.dataLog.text = "脉率：${data.param.pr}\n" +
                        "体温：${data.param.temp} ℃\n" +
                        "血氧：${data.param.spo2} %\n" +
                        "pi：${data.param.pi} %\n" +
                        "呼吸率：${data.param.respRate}\n" +
                        "电池状态${data.param.batteryStatus}：${
                            when (data.param.batteryStatus) {
                                0 -> "正常使用"
                                1 -> "充电中"
                                2 -> "充满"
                                3 -> "低电量"
                                else -> ""
                            }
                        }\n" +
                        "心电导联线状态：${data.param.isInsertEcgLeadWire}\n" +
                        "血氧状态${data.param.oxyStatus}：${
                            when (data.param.oxyStatus) {
                                0 -> "未接入血氧"
                                1 -> "血氧状态正常"
                                2 -> "血氧手指脱落"
                                3 -> "探头故障"
                                else -> ""
                            }
                        }\n" +
                        "体温状态：${data.param.isInsertTemp}\n" +
                        "测量状态${data.param.measureStatus}：${
                            when (data.param.measureStatus) {
                                0 -> "空闲"
                                1 -> "准备状态"
                                2 -> "正式测量状态"
                                else -> ""
                            }
                        }\n" +
                        "已记录时长：${data.param.recordTime}\n" +
                        "开始测量时间：${data.param.year}-${data.param.month}-${data.param.day} ${data.param.hour}:${data.param.minute}:${data.param.second}\n" +
                        "导联类型${data.param.leadType}：${
                            when (data.param.leadType) {
                                0 -> "LEAD_12，12导"
                                1 -> "LEAD_6，6导"
                                2 -> "LEAD_5，5导"
                                3 -> "LEAD_3，3导"
                                4 -> "LEAD_3_TEMP，3导带体温"
                                5 -> "LEAD_3_LEG，3导胸贴"
                                6 -> "LEAD_5_LEG，5导胸贴"
                                7 -> "LEAD_6_LEG，6导胸贴"
                                0xFF -> "LEAD_NONSUP，不支持的导联"
                                else -> "UNKNOWN，未知导联"
                            }
                        }\n" +
                        "一次性导联的sn：${data.param.leadSn}\n" +
                        "LA导联脱落：${data.param.isLeadOffLA}\n" +
                        "LL导联脱落：${data.param.isLeadOffLL}\n" +
                        "V1导联脱落：${data.param.isLeadOffV1}\n" +
                        "V2导联脱落：${data.param.isLeadOffV2}\n" +
                        "V3导联脱落：${data.param.isLeadOffV3}\n" +
                        "V4导联脱落：${data.param.isLeadOffV4}\n" +
                        "V5导联脱落：${data.param.isLeadOffV5}\n" +
                        "V6导联脱落：${data.param.isLeadOffV6}"
                // sampling rate：250HZ
                // mV = n * 0.00244
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
        Er3DataController.clear()
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

}