package com.example.lpdemo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.utils.DataController
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.dataEcgSrc
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
import kotlinx.android.synthetic.main.activity_pc303.*
import kotlin.math.floor

class Pc303Activity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Pc303Activity"
    private val model = Bluetooth.MODEL_PC300

    private lateinit var ecgBkg: EcgBkg
    private lateinit var ecgView: EcgView
    /**
     * rt wave
     */
    private val waveHandler = Handler()

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
//            LepuBleLog.d("DataRec: ${DataController.dataRec.size}, delayed $interval")

            val temp = DataController.draw(5)
            dataEcgSrc.value = DataController.feed(dataEcgSrc.value, temp)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pc303)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ecg_bkg.post {
            initEcgView()
        }
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc300GetInfo(model)
        }
        bleState.observe(this, {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                bp_ble_state.setImageResource(R.mipmap.bluetooth_ok)
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                bp_ble_state.setImageResource(R.mipmap.bluetooth_error)
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        })
        dataEcgSrc.observe(this, {
            if (this::ecgView.isInitialized) {
                ecgView.setDataSrc(it)
                ecgView.invalidate()
            }
        })
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

        waveHandler.post(EcgWaveTask())

    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300DeviceInfo)
            .observe(this, {
                // 设备信息
                val data = it.data as DeviceInfo
                data_log.text = data.toString()
            })
        // ----------------------bp----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpStart)
            .observe(this, {
                data_log.text = "bp start"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpStop)
            .observe(this, {
                data_log.text = "bp stop"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtBpData)
            .observe(this, {
                val data = it.data as Int
                tv_ps.text = "$data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpResult)
            .observe(this, {
                val data = it.data as BpResult
                tv_sys.text = data.sys.toString()
                tv_dia.text = data.dia.toString()
                tv_mean.text = data.map.toString()
                tv_pr_bp.text = data.pr.toString()
                data_log.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpErrorResult)
            .observe(this, {
                val data = it.data as BpResultError
                data_log.text = data.toString()
            })
        // ----------------------oxy----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyParam)
            .observe(this, {
                val data = it.data as RtOxyParam
                tv_oxy.text = data.spo2.toString()
                tv_pr.text = data.pr.toString()
                tv_pi.text = data.pi.toString()
                data_log.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyWave)
            .observe(this, {
                val data = it.data as RtOxyWave

            })
        // ----------------------temp----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300TempResult)
            .observe(this, {
                // if receive twice same result, just get one of them
                // normal temp：32 - 43
                val data = it.data as Float
                data_log.text = if (data < 32) {
                    "abnormal temp $data ℃"
                } else {
                    "normal temp $data ℃"
                }
            })
        // ----------------------glu----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300GluResult)
            .observe(this, {
                val data = it.data as GluResult
                data_log.text = if (data.unit == 0) {
                    "${data.data} mmol/L, result : ${data.resultMess}"
                } else {
                    "${data.data} mg/dL, result : ${data.resultMess}"
                }
            })
        // ----------------------ecg----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgStart)
            .observe(this, {
                data_log.text = "ecg start"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgStop)
            .observe(this, {
                data_log.text = "ecg stop"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtEcgWave)
            .observe(this, {
                val data = it.data as RtEcgWave
                DataController.receive(data.ecgFloats)
                // 0 is preparing, about 10 s, then 1,2,3... is measuring
                data_log.text = if (data.seqNo == 0) {
                    "preparing ${data.seqNo}"
                } else {
                    "measuring ${data.seqNo}"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgResult)
            .observe(this, {
                val data = it.data as EcgResult
                hr.text = "${data.hr}"
                data_log.text = "$data"
            })
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // 蓝牙状态 Ble.State
        Log.d(TAG, "model $model, state: $state")

        _bleState.value = state == Ble.State.CONNECTED
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

}