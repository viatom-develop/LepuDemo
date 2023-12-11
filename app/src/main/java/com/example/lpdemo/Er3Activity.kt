package com.example.lpdemo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_er3.*
import kotlin.math.floor

class Er3Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Er3Activity"
    // Bluetooth.MODEL_ER3, Bluetooth.MODEL_M12
    private var model = Bluetooth.MODEL_ER3

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
        setContentView(R.layout.activity_er3)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ble_name.text = deviceName
        bkg12.post {
            initEcgView()
        }
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er3GetInfo(model)
        }
        factory_reset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er3FactoryReset(model)
        }
        get_config.setOnClickListener {
            BleServiceHelper.BleServiceHelper.er3GetConfig(model)
        }
        set_config.setOnClickListener {
            // 0: 监护模式0.5-40
            // 1: 手术模式1-20
            // 2: ST模式0.05-40
            BleServiceHelper.BleServiceHelper.er3SetConfig(model, 0)
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
        bleState.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
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
        val index = floor(bkg1.width / dm.xdpi * 25.4 / 25 * 250).toInt()
        Er3DataController.maxIndex = index

        // 每像素占多少mm=每英寸长25.4mm/每英寸像素
        val mm2px = 25.4f / dm.xdpi
        Er3DataController.mm2px = mm2px

        bkg1.measure(0, 0)
        ecgBkg1 = Er3EcgBkg(this)
        bkg1.addView(ecgBkg1)
        view1.measure(0, 0)
        ecgView1 = Er3EcgView(this)
        view1.addView(ecgView1)

        bkg2.measure(0, 0)
        ecgBkg2 = Er3EcgBkg(this)
        bkg2.addView(ecgBkg2)
        view2.measure(0, 0)
        ecgView2 = Er3EcgView(this)
        view2.addView(ecgView2)

        bkg3.measure(0, 0)
        ecgBkg3 = Er3EcgBkg(this)
        bkg3.addView(ecgBkg3)
        view3.measure(0, 0)
        ecgView3 = Er3EcgView(this)
        view3.addView(ecgView3)

        bkg4.measure(0, 0)
        ecgBkg4 = Er3EcgBkg(this)
        bkg4.addView(ecgBkg4)
        view4.measure(0, 0)
        ecgView4 = Er3EcgView(this)
        view4.addView(ecgView4)

        bkg5.measure(0, 0)
        ecgBkg5 = Er3EcgBkg(this)
        bkg5.addView(ecgBkg5)
        view5.measure(0, 0)
        ecgView5 = Er3EcgView(this)
        view5.addView(ecgView5)

        bkg6.measure(0, 0)
        ecgBkg6 = Er3EcgBkg(this)
        bkg6.addView(ecgBkg6)
        view6.measure(0, 0)
        ecgView6 = Er3EcgView(this)
        view6.addView(ecgView6)

        bkg7.measure(0, 0)
        ecgBkg7 = Er3EcgBkg(this)
        bkg7.addView(ecgBkg7)
        view7.measure(0, 0)
        ecgView7 = Er3EcgView(this)
        view7.addView(ecgView7)

        bkg8.measure(0, 0)
        ecgBkg8 = Er3EcgBkg(this)
        bkg8.addView(ecgBkg8)
        view8.measure(0, 0)
        ecgView8 = Er3EcgView(this)
        view8.addView(ecgView8)

        bkg9.measure(0, 0)
        ecgBkg9 = Er3EcgBkg(this)
        bkg9.addView(ecgBkg9)
        view9.measure(0, 0)
        ecgView9 = Er3EcgView(this)
        view9.addView(ecgView9)

        bkg10.measure(0, 0)
        ecgBkg10 = Er3EcgBkg(this)
        bkg10.addView(ecgBkg10)
        view10.measure(0, 0)
        ecgView10 = Er3EcgView(this)
        view10.addView(ecgView10)

        bkg11.measure(0, 0)
        ecgBkg11 = Er3EcgBkg(this)
        bkg11.addView(ecgBkg11)
        view11.measure(0, 0)
        ecgView11 = Er3EcgView(this)
        view11.addView(ecgView11)

        bkg12.measure(0, 0)
        ecgBkg12 = Er3EcgBkg(this)
        bkg12.addView(ecgBkg12)
        view12.measure(0, 0)
        ecgView12 = Er3EcgView(this)
        view12.addView(ecgView12)
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3Info)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3FactoryReset)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventEr3FactoryReset $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3GetConfigError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventEr3GetConfigError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3GetConfig)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "${when (data) {
                    0 -> "监护模式0.5-40"
                    1 -> "手术模式1-20"
                    2 -> "ST模式0.05-40"
                    else -> ""
                }}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventEr3SetConfig $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3RtData)
            .observe(this) {
                val data = it.data as RtData
                Er3DataController.receive(data.wave.waveFloats, data.param.isLeadOffLA, data.param.isLeadOffLL)
                ble_battery.text = "电量：${data.param.battery} %"
                hr.text = "${data.param.hr}"
                data_log.text = "脉率：${data.param.pr}\n" +
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