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
import com.lepu.blepro.ext.pc80b.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_pc80b.*
import kotlin.math.floor

class Pc80bActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "Pc80bActivity"
    private val model = Bluetooth.MODEL_PC80B

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
        setContentView(R.layout.activity_pc80b)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        ecg_bkg.post {
            initEcgView()
        }
        get_info.setOnClickListener {
            BleServiceHelper.BleServiceHelper.pc80bGetInfo(model)
        }
        bleState.observe(this, {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bDeviceInfo)
            .observe(this, {
                // 设备信息
                val data = it.data as DeviceInfo
                data_log.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bBatLevel)
            .observe(this, {
                val data = it.data as Int
                data_log.text = "BatLevel $data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bRtData)
            .observe(this, {
                val data = it.data as RtData
                if (data.dataType == 1) {
                    data.ecgData.let { data2 ->
                        DataController.receive(data2.ecgFloats)
                    }
                } else if (data.dataType == 0) {
                    data.ecgResult.let { data2 ->
                        hr.text = data2.hr.toString()
                    }
                }
                data_log.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileError)
            .observe(this, {
                val data = it.data as Boolean
                data_log.text = "ReadFileError $data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadingFileProgress)
            .observe(this, {
                val data = it.data as Int
                data_log.text = "ReadingFileProgress $data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileComplete)
            .observe(this, {
                val data = it.data as EcgFile
                data_log.text = data.toString()
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