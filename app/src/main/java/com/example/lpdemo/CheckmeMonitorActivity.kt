package com.example.lpdemo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityCheckmeMonitorBinding
import com.example.lpdemo.utils.*
import com.example.lpdemo.views.EcgBkg
import com.example.lpdemo.views.EcgView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.checkmemonitor.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlin.math.floor

class CheckmeMonitorActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "CheckmeMonitorActivity"
    // Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_CHECK_ADV
    private var model = Bluetooth.MODEL_CHECK_ADV
    private lateinit var binding: ActivityCheckmeMonitorBinding

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
        binding = ActivityCheckmeMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.bleName.text = deviceName
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeMonitor.EventCheckmeMonitorRtData)
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