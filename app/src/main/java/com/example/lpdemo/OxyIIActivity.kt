package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityOxy2Binding
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.constants.Constant
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.oxy2.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver

class OxyIIActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "OxyIIActivity"
    // Bluetooth.MODEL_O2RING_S, Bluetooth.MODEL_S8_AW
    // Bluetooth.MODEL_BAND_WU, Bluetooth.MODEL_SHQO2_PRO
    private var model = Bluetooth.MODEL_O2RING_S
    private lateinit var binding: ActivityOxy2Binding

    private var fileNames = arrayListOf<String>()
    private var config = Config()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOxy2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
        BleServiceHelper.BleServiceHelper.oxyIIGetConfig(model)
    }

    private fun initView() {
        binding.bleName.text = deviceName
        bleState.observe(this) {
            if (it) {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }

        binding.getBattery.setOnClickListener {
            BleServiceHelper.BleServiceHelper.oxyIIGetBattery(model)
        }
        binding.getInfo.setOnClickListener {
            BleServiceHelper.BleServiceHelper.oxyIIGetInfo(model)
        }
        binding.getFileList.setOnClickListener {
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
            fileNames.clear()
//            BleServiceHelper.BleServiceHelper.oxyIIGetFileList(model, Constant.OxyIIFileType.PPG)
            BleServiceHelper.BleServiceHelper.oxyIIGetFileList(model, Constant.OxyIIFileType.OXY)
        }
        binding.readFile.setOnClickListener {
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
            readFile()
        }
        binding.getConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.oxyIIGetConfig(model)
        }
        binding.setMotor.setOnClickListener {
            // （0-20：MIN，20-40：LOW，40-60：MID，60-80：HIGH，80-100：MAX，0 is off）
            config.type = Constant.OxyIIConfigType.MOTOR
            config.motor.motor = 20
            BleServiceHelper.BleServiceHelper.oxyIISetConfig(model, config)
        }
        binding.reset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.oxyIIReset(model)
        }
        binding.factoryReset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.oxyIIFactoryReset(model)
        }
        binding.startRtTask.setOnClickListener {
            BleServiceHelper.BleServiceHelper.startRtTask(model)
        }
        binding.stopRtTask.setOnClickListener {
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetBattery)
            .observe(this) {
                val data = it.data as Battery
                // data.state：0（no charge），1（charging），2（charging complete）, 3:Low battery 10%
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetFileList)
            .observe(this) {
                val data = it.data as FileList
                // Constant.OxyIIFileType.PPG
                // Constant.OxyIIFileType.OXY
                binding.dataLog.text = "$data"
                for (name in data.names) {
                    fileNames.add(name)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventOxyIIReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "进度 $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadFileComplete)
            .observe(this) {
                val data = it.data as FileContent
                // data.name : file name
                val file = if (data.type == Constant.OxyIIFileType.OXY) {
                    OxyFile(data.content)
                } else { // Constant.OxyIIFileType.PPG
                    PpgFile(data.content)
                }
                binding.dataLog.text = "$file"
                fileNames.removeAt(0)
                readFile()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIRtData)
            .observe(this) {
                val data = it.data as RtData
                binding.tvOxy.text = data.param.spo2.toString()
                binding.tvPr.text = data.param.pr.toString()
                binding.tvPi.text = data.param.pi.toString()
                binding.dataLog.text = "$data"
                // data.param.batteryPercent：0-100
                // data.param.batteryState：0（no charge），1（charging），2（charging complete）, 3:Low battery 10%
                // data.param.runStatus：1（measurement preparation stage, 2 min），2（measuring），3（measurement completed）
                // data.param.sensorState：0（lead off），1（lead on），2:SENSOR_STA_PROBE_OUT 3: Sensor or probe malfunction
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReset)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventOxyIIReset $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIFactoryReset)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventOxyIIFactoryReset $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetConfig)
            .observe(this) {
                config = it.data as Config
                binding.dataLog.text = "$config"
                // config.spo2Low.low : 80-95%, step 1%, default 88%
                // config.hrLow.low : 30-70, step 5, default 50
                // config.hrHi.hi : 70-200, step 5, default 120
                // config.motor.motor : 20/40/60/80/100
                // config.buzzer.buzzer : 20/40/60/80/100
                // config.displayMode.mode : 0:Standard,  2:Always On
                // config.brightnessMode.mode : 0：low, 1：mid, 2：high
                // config.storageInterval.interval : data storage interval, unit:s
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIISetConfig)
            .observe(this) {
                val data = it.data as Boolean
                binding.dataLog.text = "EventOxyIISetConfig $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIRtPpg)
            .observe(this) {
                // BleServiceHelper.BleServiceHelper.oxyIIGetRtPpg(model)
                val data = it.data as RtPpg
                binding.dataLog.text = "$data"
            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
//        BleServiceHelper.BleServiceHelper.oxyIIReadFile(model, fileNames[0], Constant.OxyIIFileType.PPG)
        BleServiceHelper.BleServiceHelper.oxyIIReadFile(model, fileNames[0], Constant.OxyIIFileType.OXY)
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // 蓝牙状态 Ble.State
        Log.d(TAG, "model $model, state: $state")

        _bleState.value = state == Ble.State.CONNECTED
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        BleServiceHelper.BleServiceHelper.stopRtTask(model)
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

}