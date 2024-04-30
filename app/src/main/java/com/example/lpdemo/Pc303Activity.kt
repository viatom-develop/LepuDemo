package com.example.lpdemo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityPc303Binding
import com.example.lpdemo.utils.*
import com.example.lpdemo.views.EcgBkg
import com.example.lpdemo.views.EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.pc303.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlin.math.floor

class Pc303Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Pc303Activity"
    // Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
    // Bluetooth.MODEL_GM_300SNT, Bluetooth.MODEL_GM_300SNT_BLE,
    // Bluetooth.MODEL_CMI_303
    private var model = Bluetooth.MODEL_PC300
    private lateinit var binding: ActivityPc303Binding

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
        binding = ActivityPc303Binding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
        BleServiceHelper.BleServiceHelper.pc300GetGlucometerType(model)
    }

    private fun initView() {
        binding.bleName.text = deviceName
        binding.ecgBkg.post {
            initEcgView()
        }
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc300GetInfo(model)
        }
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("爱奥乐", "百捷", "CE")).apply {
            binding.gluType.adapter = this
        }
        binding.gluType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 1:爱奥乐 2:百捷 4:CE
                if (position == 2) {
                    BleServiceHelper.BleServiceHelper.pc300SetGlucometerType(model, position+2)
                } else {
                    BleServiceHelper.BleServiceHelper.pc300SetGlucometerType(model, position+1)
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        bleState.observe(this) {
            if (it) {
                binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
                binding.bpBleState.setImageResource(R.mipmap.bluetooth_ok)
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
                waveHandler.removeCallbacks(ecgWaveTask)
                waveHandler.postDelayed(ecgWaveTask, 1000)
            } else {
                waveHandler.removeCallbacks(ecgWaveTask)
                binding.bleState.setImageResource(R.mipmap.bluetooth_error)
                binding.bpBleState.setImageResource(R.mipmap.bluetooth_error)
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300DeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
                // data.batLevel：0-3（0：0-25%，1：25-50%，2：50-75%，3：75-100%）
                // data.batStatus：0 正常，1 充电中，2 已充满
            }
        // ----------------------bp----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpStart)
            .observe(this) {
                binding.dataLog.text = "bp start"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpStop)
            .observe(this) {
                binding.dataLog.text = "bp stop"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtBpData)
            .observe(this) {
                val data = it.data as Int
                binding.tvPs.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpResult)
            .observe(this) {
                val data = it.data as BpResult
                binding.tvSys.text = "${data.sys}"
                binding.tvDia.text = "${data.dia}"
                binding.tvMean.text = "${data.map}"
                binding.tvPrBp.text = "${data.pr}"
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpErrorResult)
            .observe(this) {
                val data = it.data as BpResultError
                binding.dataLog.text = "$data"
            }
        // ----------------------oxy----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyParam)
            .observe(this) {
                val data = it.data as RtOxyParam
                binding.tvOxy.text = "${data.spo2}"
                binding.tvPr.text = "${data.pr}"
                binding.tvPi.text = "${data.pi}"
                binding.dataLog.text = "$data"
                // data.spo2：0%-100%（0：invalid）
                // data.pr：0-511bpm（0：invalid）
                // data.pi：0%-25.5%（0：invalid）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyWave)
            .observe(this) {
                val data = it.data as RtOxyWave
                // data.waveIntData：0-127
            }
        // ----------------------temp----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300TempResult)
            .observe(this) {
                // if receive twice same result, just get one of them
                // normal temp：32 - 43
                val data = it.data as Float
                binding.dataLog.text = if (data < 32 || data > 43) {
                    "abnormal temp $data ℃"
                } else {
                    "normal temp $data ℃"
                }
            }
        // ----------------------glu----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300GluResult)
            .observe(this) {
                val data = it.data as GluResult
                binding.dataLog.text = if (data.unit == 0) {
                    "${data.data} mmol/L, result : ${data.resultMess}"
                } else {
                    "${data.data} mg/dL, result : ${data.resultMess}"
                }
            }
        // ----------------------ecg----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgStart)
            .observe(this) {
                binding.dataLog.text = "ecg start"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgStop)
            .observe(this) {
                binding.dataLog.text = "ecg stop"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtEcgWave)
            .observe(this) {
                val data = it.data as RtEcgWave
                DataController.receive(data.ecgFloats)
                // 0 is preparing, about 10 s, then 1,2,3... is measuring
                binding.dataLog.text = if (data.seqNo == 0) {
                    "preparing ${data.seqNo}"
                } else {
                    "measuring ${data.seqNo}"
                }
                Log.d(TAG, "${data.digit}, ${data.ecgInts.joinToString(",")}")
                Log.d(TAG, "${data.digit}, ${data.ecgFloats.joinToString(",")}")
                // sampling rate：150HZ
                // data.digit：0，data.ecgInts：0-255
                // mV = (n - 128) * (1 / 28.5)（data.ecgFloats = (data.ecgInts - 128) * (1 / 28.5)）
                // data.digit：1，data.ecgInts：0-4095
                // mV = (n - 2048) * (1 / 394)（data.ecgFloats = (data.ecgInts - 2048) * (1 / 394)）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgResult)
            .observe(this) {
                val data = it.data as EcgResult
                binding.hr.text = "${data.hr}"
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300SetGlucometerType)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "set glu type $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300GetGlucometerType)
            .observe(this) {
                val data = it.data as Int
                if (data == 4) {
                    binding.gluType.setSelection(data-2)
                } else {
                    binding.gluType.setSelection(data-1)
                }
                binding.dataLog.text = "get glu type $data"
            }
        // ----------------------ua----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300UaResult)
            .observe(this) {
                val data = it.data as Float
                binding.dataLog.text = "$data mg/dL"
            }
        // ----------------------chol----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300CholResult)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "$data mg/dL"
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