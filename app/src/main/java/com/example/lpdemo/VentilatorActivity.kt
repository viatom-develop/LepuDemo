package com.example.lpdemo

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lpdemo.databinding.ActivityVentilatorBinding
import com.example.lpdemo.utils.StringAdapter
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.example.lpdemo.utils.deviceName
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.constants.Constant
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.ventilator.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import java.util.*
import kotlin.math.max
import kotlin.math.min

class VentilatorActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "VentilatorActivity"
    // Bluetooth.MODEL_R20, Bluetooth.MODEL_R21, Bluetooth.MODEL_R10, Bluetooth.MODEL_R11, Bluetooth.MODEL_LERES
    private var model = Bluetooth.MODEL_R20
    private lateinit var binding: ActivityVentilatorBinding
    private var fileNames = arrayListOf<String>()

    private lateinit var wifiAdapter: StringAdapter
    private var wifiList = arrayListOf<Wifi>()

    private var systemSetting = SystemSetting()
    private var measureSetting = MeasureSetting()
    private var ventilationSetting = VentilationSetting()
    private var warningSetting = WarningSetting()
    private var spinnerSet = true
    private var deviceInfo = DeviceInfo()
    private lateinit var rtState: RtState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVentilatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
        // 连接蓝牙后进行交换密钥通讯，进入加密模式，所有指令允许执行
        // 不进行加密模式，部分指令不允许执行
        BleServiceHelper.BleServiceHelper.ventilatorEncrypt(model, "0001")
    }

    private fun initView() {
        binding.bleName.text = deviceName
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.wifiRcv.layoutManager = this
        }
        wifiAdapter = StringAdapter(R.layout.device_item, null).apply {
            binding.wifiRcv.adapter = this
        }
        wifiAdapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as String).let {
                val wifiConfig = WifiConfig()
                wifiList[position].pwd = "ViatomCtrl"
                wifiConfig.wifi = wifiList[position]
                val server = Server()
                server.addr = "112.125.89.8"
                server.port = 37256
                wifiConfig.server = server
                BleServiceHelper.BleServiceHelper.ventilatorSetWifiConfig(model, wifiConfig)
                adapter.setList(null)
                adapter.notifyDataSetChanged()
            }
        }
        binding.ventilatorVentilationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                BleServiceHelper.BleServiceHelper.ventilatorVentilationSwitch(model, isChecked)
            }
        }
        binding.ventilatorMaskTest.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                BleServiceHelper.BleServiceHelper.ventilatorMaskTest(model, isChecked)
            }
        }
        binding.getRtState.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ventilatorGetRtState(model)
        }
        binding.getRtParam.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ventilatorGetRtParam(model)
        }
        binding.getFileList.setOnClickListener {
            fileNames.clear()
            BleServiceHelper.BleServiceHelper.ventilatorGetFileList(model)
        }
        binding.readFile.setOnClickListener {
            readFile()
        }
        binding.getWifiList.setOnClickListener {
            wifiList.clear()
            BleServiceHelper.BleServiceHelper.ventilatorGetWifiList(model)
        }
        binding.getWifiConfig.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ventilatorGetWifiConfig(model)
        }
        bleState.observe(this) {
            if (it) {
                binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.bleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }
        binding.systemSetting.setOnClickListener {
            binding.systemSetting.background = getDrawable(R.drawable.string_selected)
            binding.measureSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.warningSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.otherSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.systemSettingLayout.visibility = View.VISIBLE
            binding.measureSettingLayout.visibility = View.GONE
            binding.ventilationSettingLayout.visibility = View.GONE
            binding.warningSettingLayout.visibility = View.GONE
            binding.otherLayout.visibility = View.GONE
            spinnerSet = false
            BleServiceHelper.BleServiceHelper.ventilatorGetSystemSetting(model)
        }
        binding.measureSetting.setOnClickListener {
            binding.measureSetting.background = getDrawable(R.drawable.string_selected)
            binding.systemSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilationSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.warningSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.otherSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.measureSettingLayout.visibility = View.VISIBLE
            binding.systemSettingLayout.visibility = View.GONE
            binding.ventilationSettingLayout.visibility = View.GONE
            binding.warningSettingLayout.visibility = View.GONE
            binding.otherLayout.visibility = View.GONE
            spinnerSet = false
            BleServiceHelper.BleServiceHelper.ventilatorGetVentilationSetting(model)
            BleServiceHelper.BleServiceHelper.ventilatorGetMeasureSetting(model)
        }
        binding.ventilationSetting.setOnClickListener {
            binding.ventilationSetting.background = getDrawable(R.drawable.string_selected)
            binding.systemSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.measureSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.warningSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.otherSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilationSettingLayout.visibility = View.VISIBLE
            binding.systemSettingLayout.visibility = View.GONE
            binding.measureSettingLayout.visibility = View.GONE
            binding.warningSettingLayout.visibility = View.GONE
            binding.otherLayout.visibility = View.GONE
            BleServiceHelper.BleServiceHelper.ventilatorGetRtState(model)
            spinnerSet = false
            BleServiceHelper.BleServiceHelper.ventilatorGetVentilationSetting(model)
        }
        binding.warningSetting.setOnClickListener {
            binding.warningSetting.background = getDrawable(R.drawable.string_selected)
            binding.systemSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.measureSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilationSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.otherSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.warningSettingLayout.visibility = View.VISIBLE
            binding.systemSettingLayout.visibility = View.GONE
            binding.measureSettingLayout.visibility = View.GONE
            binding.ventilationSettingLayout.visibility = View.GONE
            binding.otherLayout.visibility = View.GONE
            spinnerSet = false
            BleServiceHelper.BleServiceHelper.ventilatorGetWarningSetting(model)
        }
        binding.otherSetting.setOnClickListener {
            binding.otherSetting.background = getDrawable(R.drawable.string_selected)
            binding.systemSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.measureSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilationSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.warningSetting.background = getDrawable(R.drawable.dialog_hint_shape)
            binding.otherLayout.visibility = View.VISIBLE
            binding.systemSettingLayout.visibility = View.GONE
            binding.measureSettingLayout.visibility = View.GONE
            binding.ventilationSettingLayout.visibility = View.GONE
            binding.warningSettingLayout.visibility = View.GONE
            BleServiceHelper.BleServiceHelper.ventilatorGetRtState(model)
        }
        // 绑定/解绑
        binding.bound.setOnCheckedChangeListener { buttonView, isChecked ->
            BleServiceHelper.BleServiceHelper.ventilatorDeviceBound(model, isChecked)
        }
        // 进入医生模式
        binding.doctorMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    var pin = binding.pin.text.toString()
                    pin = if (pin == "") {
                        "0319"
                    } else {
                        pin
                    }
                    BleServiceHelper.BleServiceHelper.ventilatorDoctorModeIn(model, pin, System.currentTimeMillis().div(1000))
                } else {
                    BleServiceHelper.BleServiceHelper.ventilatorDoctorModeOut(model)
                }
            }
        }
        // 系统设置
        // 单位设置
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("cmH2O", "hPa")).apply {
            binding.unit.adapter = this
        }
        binding.unit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    systemSetting.type = Constant.VentilatorSystemSetting.UNIT
                    systemSetting.unitSetting.pressureUnit = position
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 语言设置
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("英文", "中文")).apply {
            binding.language.adapter = this
        }
        binding.language.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    systemSetting.type = Constant.VentilatorSystemSetting.LANGUAGE
                    systemSetting.languageSetting.language = position
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 屏幕设置：屏幕亮度
        binding.brightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.SCREEN
                    systemSetting.screenSetting.brightness = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.brightnessProcess.text = "$progress %"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.brightnessRange.text = "范围：${binding.brightness.min}% - ${binding.brightness.max}%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.brightnessSub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.SCREEN
            systemSetting.screenSetting.brightness = --binding.brightness.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.brightnessAdd.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.SCREEN
            systemSetting.screenSetting.brightness = ++binding.brightness.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 屏幕设置：自动熄屏
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("常亮", "30秒", "60秒", "90秒", "120秒")).apply {
            binding.screenOff.adapter = this
        }
        binding.screenOff.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    systemSetting.type = Constant.VentilatorSystemSetting.SCREEN
                    systemSetting.screenSetting.autoOff = position.times(30)
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 耗材设置：过滤棉
        binding.filter.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
                    systemSetting.replacements.filter = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.filterProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "$progress 个月"
                }
                binding.filterRange.text = "范围：关 - ${binding.filter.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.filterSub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.filter = --binding.filter.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.filterAdd.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.filter = ++binding.filter.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：面罩
        binding.mask.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
                    systemSetting.replacements.mask = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.maskProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "$progress 个月"
                }
                binding.maskRange.text = "范围：关 - ${binding.mask.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.maskSub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.mask = --binding.mask.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.maskAdd.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.mask = ++binding.mask.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：管道
        binding.tube.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
                    systemSetting.replacements.tube = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.tubeProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "$progress 个月"
                }
                binding.tubeRange.text = "范围：关 - ${binding.tube.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.tubeSub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.tube = --binding.tube.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.tubeAdd.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.tube = ++binding.tube.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：水箱
        binding.tank.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
                    systemSetting.replacements.tank = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.tankProcess.text = if (progress == 0) {
                    "关闭"
                } else {
                    "$progress 个月"
                }
                binding.tankRange.text = "范围：关 - ${binding.tank.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.tankSub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.tank = --binding.tank.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.tankAdd.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.tank = ++binding.tank.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 音量设置
        binding.volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.VOLUME
                    systemSetting.volumeSetting.volume = progress.times(5)
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.volumeProcess.text = "${progress.times(5)} %"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.volumeRange.text = "范围：${binding.volume.min}% - ${binding.volume.max}%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.volumeSub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.VOLUME
            binding.volume.progress--
            systemSetting.volumeSetting.volume = binding.volume.progress.times(5)
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.volumeAdd.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.VOLUME
            binding.volume.progress++
            systemSetting.volumeSetting.volume = binding.volume.progress.times(5)
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 测量设置
        // 湿化等级
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("关闭", "1", "2", "3", "4", "5", "自动")).apply {
            binding.humidification.adapter = this
        }
        binding.humidification.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.HUMIDIFICATION
                    if (position == 6) {
                        measureSetting.humidification.humidification = 0xff
                    } else {
                        measureSetting.humidification.humidification = position
                    }
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼吸压力释放：呼气压力释放
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("关闭", "1", "2", "3")).apply {
            binding.epr.adapter = this
        }
        binding.epr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.PRESSURE_REDUCE
                    measureSetting.pressureReduce.epr = position
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 自动启停：自动启动
        binding.autoStart.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                measureSetting.type = Constant.VentilatorMeasureSetting.AUTO_SWITCH
                measureSetting.autoSwitch.isAutoStart = isChecked
                BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
            }
        }
        // 自动启停：自动停止
        binding.autoEnd.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                measureSetting.type = Constant.VentilatorMeasureSetting.AUTO_SWITCH
                measureSetting.autoSwitch.isAutoEnd = isChecked
                BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
            }
        }
        // 预加热
        binding.preHeat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                measureSetting.type = Constant.VentilatorMeasureSetting.PRE_HEAT
                measureSetting.preHeat.isOn = isChecked
                BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
            }
        }
        // 缓冲压力
        binding.rampPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
                    measureSetting.ramp.pressure = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
                binding.rampPressureProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.rampPressureRange.text = "范围：${binding.rampPressure.min.times(0.5f)} - ${binding.rampPressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.rampPressureSub.setOnClickListener {
            binding.rampPressure.progress--
            measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
            measureSetting.ramp.pressure = binding.rampPressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        binding.rampPressureAdd.setOnClickListener {
            binding.rampPressure.progress++
            measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
            measureSetting.ramp.pressure = binding.rampPressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 缓冲时间
        binding.rampTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
                    measureSetting.ramp.time = progress.times(5)
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
                binding.rampTimeProcess.text = "${progress.times(5)}min"
                binding.rampTimeRange.text = "范围：关 - ${binding.rampTime.max.times(5)}min"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.rampTimeSub.setOnClickListener {
            binding.rampTime.progress--
            measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
            measureSetting.ramp.time = binding.rampTime.progress.times(5)
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        binding.rampTimeAdd.setOnClickListener {
            binding.rampTime.progress++
            measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
            measureSetting.ramp.time = binding.rampTime.progress.times(5)
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 管道类型
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("15mm", "22mm")).apply {
            binding.tubeType.adapter = this
        }
        binding.tubeType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.TUBE_TYPE
                    measureSetting.tubeType.type = position
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 面罩类型
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("口鼻罩", "鼻罩", "鼻枕")).apply {
            binding.maskType.adapter = this
        }
        binding.maskType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.MASK
                    measureSetting.mask.type = position
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 面罩佩戴匹配测试压力
        binding.maskPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.MASK
                    measureSetting.mask.pressure = progress.toFloat()
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
                binding.maskPressureProcess.text = "${progress.toFloat()}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.maskPressureRange.text = "范围：${binding.maskPressure.min.times(1.0f)} - ${binding.maskPressure.max.times(1.0f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.maskPressureSub.setOnClickListener {
            binding.maskPressure.progress--
            measureSetting.type = Constant.VentilatorMeasureSetting.MASK
            measureSetting.mask.pressure = binding.maskPressure.progress.toFloat()
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        binding.maskPressureAdd.setOnClickListener {
            binding.maskPressure.progress++
            measureSetting.type = Constant.VentilatorMeasureSetting.MASK
            measureSetting.mask.pressure = binding.maskPressure.progress.toFloat()
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 通气设置
        // 通气模式
        val ventilatorModel = VentilatorModel(deviceInfo.branchCode)
        val modes = ArrayList<String>()
        if (ventilatorModel.isSupportCpap) {
            modes.add("CPAP")
        }
        if (ventilatorModel.isSupportApap) {
            modes.add("APAP")
        }
        if (ventilatorModel.isSupportS) {
            modes.add("S")
        }
        if (ventilatorModel.isSupportST) {
            modes.add("S/T")
        }
        if (ventilatorModel.isSupportT) {
            modes.add("T")
        }
        ArrayAdapter(this, android.R.layout.simple_list_item_1, modes).apply {
            binding.ventilationMode.adapter = this
        }
        binding.ventilationMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.VENTILATION_MODE
                    ventilationSetting.ventilationMode.mode = position
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // CPAP模式压力
        binding.cpapPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE
                    ventilationSetting.cpapPressure.pressure = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.cpapPressureProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.cpapPressureRange.text = "范围：${binding.cpapPressure.min.times(0.5f)} - ${binding.cpapPressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.cpapPressureSub.setOnClickListener {
            binding.cpapPressure.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE
            ventilationSetting.cpapPressure.pressure = binding.cpapPressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.cpapPressureAdd.setOnClickListener {
            binding.cpapPressure.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE
            ventilationSetting.cpapPressure.pressure = binding.cpapPressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // APAP模式压力最大值Pmax
        binding.apapPressureMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MAX
                    ventilationSetting.apapPressureMax.max = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.apapPressureMaxProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.apapPressureMaxRange.text = "范围：${binding.apapPressureMax.min.times(0.5f)} - ${binding.apapPressureMax.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.apapPressureMaxSub.setOnClickListener {
            binding.apapPressureMax.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MAX
            ventilationSetting.apapPressureMax.max = binding.apapPressureMax.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.apapPressureMaxAdd.setOnClickListener {
            binding.apapPressureMax.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MAX
            ventilationSetting.apapPressureMax.max = binding.apapPressureMax.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // APAP模式压力最小值Pmin
        binding.apapPressureMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MIN
                    ventilationSetting.apapPressureMin.min = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.apapPressureMinProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.apapPressureMinRange.text = "范围：${binding.apapPressureMin.min.times(0.5f)} - ${binding.apapPressureMin.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.apapPressureMinSub.setOnClickListener {
            binding.apapPressureMin.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MIN
            ventilationSetting.apapPressureMin.min = binding.apapPressureMin.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.apapPressureMinAdd.setOnClickListener {
            binding.apapPressureMin.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MIN
            ventilationSetting.apapPressureMin.min = binding.apapPressureMin.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气压力
        binding.ipapPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_INHALE
                    ventilationSetting.pressureInhale.inhale = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.ipapPressureProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ipapPressureRange.text = "范围：${binding.ipapPressure.min.times(0.5f)} - ${binding.ipapPressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ipapPressureSub.setOnClickListener {
            binding.ipapPressure.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_INHALE
            ventilationSetting.pressureInhale.inhale = binding.ipapPressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.ipapPressureAdd.setOnClickListener {
            binding.ipapPressure.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_INHALE
            ventilationSetting.pressureInhale.inhale = binding.ipapPressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 呼气压力
        binding.epapPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_EXHALE
                    ventilationSetting.pressureExhale.exhale = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.epapPressureProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.epapPressureRange.text = "范围：${binding.epapPressure.min.times(0.5f)} - ${binding.epapPressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.epapPressureSub.setOnClickListener {
            binding.epapPressure.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_EXHALE
            ventilationSetting.pressureExhale.exhale = binding.epapPressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.epapPressureAdd.setOnClickListener {
            binding.epapPressure.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_EXHALE
            ventilationSetting.pressureExhale.exhale = binding.epapPressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气时间
        binding.inspiratoryTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.INHALE_DURATION
                    ventilationSetting.inhaleDuration.duration = progress.div(10f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.inspiratoryTimeProcess.text = "${progress.div(10f)}s"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.inspiratoryTimeRange.text = "范围：${String.format("%.1f", binding.inspiratoryTime.min.times(0.1f))} - ${String.format("%.1f", binding.inspiratoryTime.max.times(0.1f))}s"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.inspiratoryTimeSub.setOnClickListener {
            binding.inspiratoryTime.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.INHALE_DURATION
            ventilationSetting.inhaleDuration.duration = binding.inspiratoryTime.progress.div(10f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.inspiratoryTimeAdd.setOnClickListener {
            binding.inspiratoryTime.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.INHALE_DURATION
            ventilationSetting.inhaleDuration.duration = binding.inspiratoryTime.progress.div(10f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 呼吸频率
        binding.respiratoryFrequency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.RESPIRATORY_RATE
                    ventilationSetting.respiratoryRate.rate = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.respiratoryFrequencyProcess.text = "$progress bpm"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.respiratoryFrequencyRange.text = "范围：${binding.respiratoryFrequency.min} - ${binding.respiratoryFrequency.max}bpm"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.respiratoryFrequencySub.setOnClickListener {
            binding.respiratoryFrequency.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.RESPIRATORY_RATE
            ventilationSetting.respiratoryRate.rate = binding.respiratoryFrequency.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.respiratoryFrequencyAdd.setOnClickListener {
            binding.respiratoryFrequency.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.RESPIRATORY_RATE
            ventilationSetting.respiratoryRate.rate = binding.respiratoryFrequency.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 压力上升时间
        binding.raiseTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.RAISE_DURATION
                    ventilationSetting.pressureRaiseDuration.duration = progress.times(50)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.raiseTimeProcess.text = "${progress.times(50)}ms"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.raiseTimeRange.text = "范围：${binding.raiseTime.min.times(50)} - ${binding.raiseTime.max.times(50)}ms"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.raiseTimeSub.setOnClickListener {
            binding.raiseTime.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.RAISE_DURATION
            ventilationSetting.pressureRaiseDuration.duration = binding.raiseTime.progress.times(50)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.raiseTimeAdd.setOnClickListener {
            binding.raiseTime.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.RAISE_DURATION
            ventilationSetting.pressureRaiseDuration.duration = binding.raiseTime.progress.times(50)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气触发灵敏度
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("自动挡", "1", "2", "3", "4", "5")).apply {
            binding.iTrigger.adapter = this
        }
        binding.iTrigger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.INHALE_SENSITIVE
                    ventilationSetting.inhaleSensitive.sentive = position
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼气触发灵敏度
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("自动挡", "1", "2", "3", "4", "5")).apply {
            binding.eTrigger.adapter = this
        }
        binding.eTrigger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.EXHALE_SENSITIVE
                    ventilationSetting.exhaleSensitive.sentive = position
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 报警设置
        // 漏气量高
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("关闭", "15s", "30s", "45s", "60s")).apply {
            binding.leakHigh.adapter = this
        }
        binding.leakHigh.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    if (warningSetting.warningLeak.high == 0 && position != 0) {
                        AlertDialog.Builder(this@VentilatorActivity)
                            .setTitle("提示")
                            .setMessage("开此报警，会停用自动停止功能")
                            .setPositiveButton("确定") { _, _ ->
                                warningSetting.type = Constant.VentilatorWarningSetting.LEAK_HIGH
                                warningSetting.warningLeak.high = position.times(15)
                                BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                                binding.autoEnd.isChecked = false
                                binding.autoEnd.isEnabled = false
                            }
                            .setNegativeButton("取消") { _, _ ->
                                binding.leakHigh.setSelection(0)
                            }
                            .create()
                            .show()
                    } else {
                        warningSetting.type = Constant.VentilatorWarningSetting.LEAK_HIGH
                        warningSetting.warningLeak.high = position.times(15)
                        BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                        if (position == 0) {
                            binding.autoEnd.isEnabled = true
                        }
                    }
                } else {
                    if (position == 0) {
                        binding.autoEnd.isEnabled = true
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼吸暂停
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("关闭", "10s", "20s", "30s")).apply {
            binding.apnea.adapter = this
        }
        binding.apnea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    warningSetting.type = Constant.VentilatorWarningSetting.APNEA
                    warningSetting.warningApnea.apnea = position.times(10)
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 潮气量低
        binding.vtLow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.VT_LOW
                    warningSetting.warningVt.low = if (progress == 19) {
                        0
                    } else {
                        progress.times(10)
                    }
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.vtLowProcess.text = if (progress == 19) {
                    "关"
                } else {
                    "${progress.times(10)}ml"
                }
                binding.vtLowRange.text = "范围：关 - ${binding.vtLow.max.times(10)}ml"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.vtLowSub.setOnClickListener {
            binding.vtLow.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.VT_LOW
            warningSetting.warningVt.low = if (binding.vtLow.progress == 19) {
                0
            } else {
                binding.vtLow.progress.times(10)
            }
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.vtLowAdd.setOnClickListener {
            binding.vtLow.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.VT_LOW
            warningSetting.warningVt.low = binding.vtLow.progress.times(10)
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 分钟通气量低
        binding.lowVentilation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.LOW_VENTILATION
                    warningSetting.warningVentilation.low = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.lowVentilationProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "${progress}L/min"
                }
                binding.lowVentilationRange.text = "范围：关 - ${binding.lowVentilation.max}L/min"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.lowVentilationSub.setOnClickListener {
            binding.lowVentilation.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.LOW_VENTILATION
            warningSetting.warningVentilation.low = binding.lowVentilation.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.lowVentilationAdd.setOnClickListener {
            binding.lowVentilation.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.LOW_VENTILATION
            warningSetting.warningVentilation.low = binding.lowVentilation.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 呼吸频率高
        binding.rrHigh.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.RR_HIGH
                    warningSetting.warningRrHigh.high = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.rrHighProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "${progress}bpm"
                }
                binding.rrHighRange.text = "范围：关 - ${binding.rrHigh.max}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.rrHighSub.setOnClickListener {
            binding.rrHigh.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.RR_HIGH
            warningSetting.warningRrHigh.high = binding.rrHigh.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.rrHighAdd.setOnClickListener {
            binding.rrHigh.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.RR_HIGH
            warningSetting.warningRrHigh.high = binding.rrHigh.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 呼吸频率低
        binding.rrLow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.RR_LOW
                    warningSetting.warningRrLow.low = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.rrLowProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "${progress}bpm"
                }
                binding.rrLowRange.text = "范围：关 - ${binding.rrLow.max}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.rrLowSub.setOnClickListener {
            binding.rrLow.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.RR_LOW
            warningSetting.warningRrLow.low = binding.rrLow.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.rrLowAdd.setOnClickListener {
            binding.rrLow.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.RR_LOW
            warningSetting.warningRrLow.low = binding.rrLow.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 血氧饱和度低
        binding.spo2Low.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.SPO2_LOW
                    warningSetting.warningSpo2Low.low = if (progress == 79) {
                        0
                    } else {
                        progress
                    }
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.spo2LowProcess.text = if (progress == 79) {
                    "关"
                } else {
                    "${progress}%"
                }
                binding.spo2LowRange.text = "范围：关 - ${binding.spo2Low.max}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.spo2LowSub.setOnClickListener {
            binding.spo2Low.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.SPO2_LOW
            warningSetting.warningSpo2Low.low = if (binding.spo2Low.progress == 79) {
                0
            } else {
                binding.spo2Low.progress
            }
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.spo2LowAdd.setOnClickListener {
            binding.spo2Low.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.SPO2_LOW
            warningSetting.warningSpo2Low.low = binding.spo2Low.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 脉率/心率高
        binding.hrHigh.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.HR_HIGH
                    warningSetting.warningHrHigh.high = if (progress == 9) {
                        0
                    } else {
                        progress.times(10)
                    }
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.hrHighProcess.text = if (progress == 9) {
                    "关"
                } else {
                    "${progress.times(10)}bpm"
                }
                binding.hrHighRange.text = "范围：关 - ${binding.hrHigh.max.times(10)}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.hrHighSub.setOnClickListener {
            binding.hrHigh.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.HR_HIGH
            warningSetting.warningHrHigh.high = if (binding.hrHigh.progress == 9) {
                0
            } else {
                binding.hrHigh.progress.times(10)
            }
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.hrHighAdd.setOnClickListener {
            binding.hrHigh.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.HR_HIGH
            warningSetting.warningHrHigh.high = binding.hrHigh.progress.times(10)
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 脉率/心率低
        binding.hrLow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.HR_LOW
                    warningSetting.warningHrLow.low = if (progress == 5) {
                        0
                    } else {
                        progress.times(5)
                    }
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.hrLowProcess.text = if (progress == 5) {
                    "关"
                } else {
                    "${progress.times(5)}bpm"
                }
                binding.hrLowRange.text = "范围：关 - ${binding.hrLow.max.times(5)}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.hrLowSub.setOnClickListener {
            binding.hrLow.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.HR_LOW
            warningSetting.warningHrLow.low = if (binding.hrLow.progress == 5) {
                0
            } else {
                binding.hrLow.progress.times(5)
            }
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.hrLowAdd.setOnClickListener {
            binding.hrLow.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.HR_LOW
            warningSetting.warningHrLow.low = binding.hrLow.progress.times(5)
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEncrypt)
            .observe(this) {
                // Constant.VentilatorResponseType
                // TYPE_NORMAL_RESPONSE : 1, success, others error
                val data = it.data as Int
                when (data) {
                    Constant.VentilatorResponseType.TYPE_NORMAL_RESPONSE -> {
                        // 交换密钥成功
                        binding.dataLog.text = "exchange key succcess"
                    }
                    Constant.VentilatorResponseType.TYPE_NORMAL_ERROR -> {
                        binding.dataLog.text = "exchange key error, disconnect"
                        BleServiceHelper.BleServiceHelper.disconnect(false)
                    }
                }
                BleServiceHelper.BleServiceHelper.syncTime(model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetUtcTime)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
                when (data) {
                    Constant.VentilatorResponseType.TYPE_NORMAL_RESPONSE -> {
                        // 同步时间成功
                        binding.dataLog.text = "sync time succcess"
                    }
                    Constant.VentilatorResponseType.TYPE_DECRYPT_FAILED -> {
                        binding.dataLog.text = "decrypt data error, disconnect"
                        BleServiceHelper.BleServiceHelper.disconnect(false)
                    }
                }
                BleServiceHelper.BleServiceHelper.ventilatorGetInfo(model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfo)
            .observe(this) {
                deviceInfo = it.data as DeviceInfo
                binding.dataLog.text = "$deviceInfo"
                // 通气模式
                val ventilatorModel = VentilatorModel(deviceInfo.branchCode)
                val modes = ArrayList<String>()
                if (ventilatorModel.isSupportCpap) {
                    modes.add("CPAP")
                }
                if (ventilatorModel.isSupportApap) {
                    modes.add("APAP")
                }
                if (ventilatorModel.isSupportS) {
                    modes.add("S")
                }
                if (ventilatorModel.isSupportST) {
                    modes.add("S/T")
                }
                if (ventilatorModel.isSupportT) {
                    modes.add("T")
                }
                ArrayAdapter(this, android.R.layout.simple_list_item_1, modes).apply {
                    binding.ventilationMode.adapter = this
                }
                // init rtState, systemSetting, measureSetting, ventilationSetting, warningSetting
                BleServiceHelper.BleServiceHelper.ventilatorGetRtState(model)
                BleServiceHelper.BleServiceHelper.ventilatorGetSystemSetting(model)
                BleServiceHelper.BleServiceHelper.ventilatorGetVentilationSetting(model)
                BleServiceHelper.BleServiceHelper.ventilatorGetMeasureSetting(model)
                BleServiceHelper.BleServiceHelper.ventilatorGetWarningSetting(model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfoError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        // BleServiceHelper.BleServiceHelper.ventilatorGetVersionInfo(model)
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfo)
            .observe(this) {
                val data = it.data as VersionInfo
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfoError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileList)
            .observe(this) {
                val data = it.data as RecordList
                // data.startTime : timestamp, unit second
                // data.type : 1(Daily statistics), 2(Single statistics, not used temporarily)
                for (file in data.list) {
                    if (data.type == 1) {
                        fileNames.add(file.recordName)
                    }
                }
                binding.dataLog.text = "$fileNames"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileListError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileComplete)
            .observe(this) {
                val file = it.data as StatisticsFile
                val unit = systemSetting.unitSetting.pressureUnit
                binding.dataLog.text = "文件名：${file.fileName}\n" +
                        "使用天数：${file.usageDays}天\n" +
                        "不小于4小时天数：${file.moreThan4hDays}天\n" +
                        "总使用时间：${String.format("%.1f", file.duration.div(3600f))}小时\n" +
                        "平均使用时间：${String.format("%.1f", file.meanSecond.div(3600f))}小时\n" +
                        "压力：${file.pressure[4]}${if (unit == 0) "cmH2O" else "hPa"}\n" +
                        "呼气压力：${file.epap[4]}${if (unit == 0) "cmH2O" else "hPa"}\n" +
                        "吸气压力：${file.ipap[4]}${if (unit == 0) "cmH2O" else "hPa"}\n" +
                        "AHI：${String.format("%.1f", file.ahiCount.times(3600f).div(file.duration))}/小时\n" +
                        "AI：${String.format("%.1f", file.aiCount.times(3600f).div(file.duration))}/小时\n" +
                        "HI：${String.format("%.1f", file.hiCount.times(3600f).div(file.duration))}/小时\n" +
                        "CAI：${String.format("%.1f", file.caiCount.times(3600f).div(file.duration))}/小时\n" +
                        "OAI：${String.format("%.1f", file.oaiCount.times(3600f).div(file.duration))}/小时\n" +
                        "RERA：${String.format("%.1f", file.rearCount.times(3600f).div(file.duration))}/小时\n" +
                        "潮气量：${if (file.vt[3] < 0 || file.vt[3] > 3000) "**" else file.vt[3]}mL\n" +
                        "漏气量：${if (file.leak[4] < 0 || file.leak[4] > 120) "**" else file.leak[4]}L/min\n" +
                        "分钟通气量：${if (file.mv[3] < 0 || file.mv[3] > 60) "**" else file.mv[3]}L/min\n" +
                        "呼吸频率：${if (file.rr[3] < 0 || file.rr[3] > 60) "**" else file.rr[3]}bpm\n" +
                        "吸气时间：${if (file.ti[3] < 0 || file.ti[3] > 4) "--" else file.ti[3]}s\n" +
                        "吸呼比：${if (file.ie[3] < 0.02 || file.ie[3] > 3) "--" else {
                            if (file.ie[3] < 1) {
                                "1:" + String.format("%.1f", 1f/file.ie[3])
                            } else {
                                String.format("%.1f", file.ie[3].div(1f)) + ":1"
                            }
                        }}\n" +
                        "自主呼吸占比：${if (file.spont < 0 || file.spont > 100) "**" else file.spont}%\n" +
                        "血氧：${if (file.spo2[0] < 70 || file.spo2[0] > 100) "**" else file.spo2[0]}%\n" +
                        "脉率：${if (file.pr[2] < 30 || file.pr[2] > 250) "**" else file.pr[2]}bpm\n" +
                        "心率：${if (file.hr[2] < 30 || file.hr[2] > 250) "**" else file.hr[2]}bpm"
                fileNames.removeAt(0)
                readFile()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiListError)
            .observe(this) {
                // Constant.VentilatorResponseType
                // TYPE_NORMAL_RESPONSE : 1, success
                // TYPE_NORMAL_ERROR : 255, wifi scanning
                val data = it.data as Int
                binding.dataLog.text = "$data"
                when (data) {
                    Constant.VentilatorResponseType.TYPE_NORMAL_ERROR -> {
                        // The device is currently scanning and cannot be obtained. Please call the query WiFi list command again
                        Handler().postDelayed({
                            BleServiceHelper.BleServiceHelper.ventilatorGetWifiList(it.model)
                        }, 1000)
                    }
                    Constant.VentilatorResponseType.TYPE_DECRYPT_FAILED -> {
                        binding.dataLog.text = "encrypt error, disconnect"
                        BleServiceHelper.BleServiceHelper.disconnect(false)
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiList)
            .observe(this) {
                wifiList = it.data as ArrayList<Wifi>
                val data = arrayListOf<String>()
                for (wifi in wifiList) {
                    data.add(wifi.ssid)
                }
                wifiAdapter.setNewInstance(data)
                wifiAdapter.notifyDataSetChanged()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfigError)
            .observe(this) {
                // Constant.VentilatorResponseType
                // TYPE_NORMAL_RESPONSE : 1, success
                // TYPE_NORMAL_ERROR : 255, no wifi config
                val data = it.data as Int
                binding.dataLog.text = "$data"
                when (data) {
                    Constant.VentilatorResponseType.TYPE_NORMAL_ERROR -> {
                        // no wifi config
                        binding.dataLog.text = "no wifi config"
                    }
                    Constant.VentilatorResponseType.TYPE_DECRYPT_FAILED -> {
                        binding.dataLog.text = "decrypt data error, disconnect"
                        BleServiceHelper.BleServiceHelper.disconnect(false)
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfig)
            .observe(this) {
                val data = it.data as WifiConfig
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWifiConfig)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBound)
            .observe(this) {
                val data = it.data as Int
                binding.bound.isChecked = data == 0
                binding.dataLog.text = when (data) {
                    0 -> "绑定成功"
                    1 -> "绑定失败"
                    2 -> "绑定超时"
                    else -> "绑定消息"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBoundError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceUnBound)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
                binding.dataLog.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorMode)
            .observe(this) {
                val data = it.data as DoctorModeResult
                binding.dataLog.text = if (data.isSuccess) {
                    if (data.isOut) {
                        "退出医生模式成功"
                    } else {
                        "进入医生模式成功"
                    }
                } else {
                    if (data.isOut) {
                        "退出医生模式失败, ${when (data.errCode) {
                            1 -> "设备处于医生模式"
                            2 -> "设备处于医生模式（BLE）"
                            3 -> "设备处于医生模式（Socket）"
                            5 -> "设备处于患者模式"
                            else -> ""
                        }}"
                    } else {
                        "进入医生模式失败, ${when (data.errCode) {
                            1 -> "设备处于医生模式"
                            2 -> "设备处于医生模式（BLE）"
                            3 -> "设备处于医生模式（Socket）"
                            4 -> "密码错误"
                            else -> ""
                        }}"
                    }
                }
                BleServiceHelper.BleServiceHelper.ventilatorGetRtState(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorModeError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtState)
            .observe(this) {
                val data = it.data as RtState
                rtState = data
                binding.dataLog.text = "$data"
                binding.ventilatorVentilationSwitch.isChecked = data.isVentilated
                binding.ventilatorVentilationSwitch.isEnabled = data.deviceMode == 2
                binding.ventilatorMaskTest.isEnabled = !data.isVentilated
                binding.doctorMode.isChecked = data.deviceMode != 0
                binding.deviceMode.text = "${when (data.deviceMode) {
                    0 -> "(设备处于患者模式)"
                    1 -> "(设备端医生模式)"
                    2 -> "(BLE端医生模式)"
                    3 -> "(Socket端医生模式)"
                    else -> ""
                }}"
                layoutGone()
                layoutVisible(data.ventilationMode)
                when (data.standard) {
                    // CFDA
                    1 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.ipapPressure.min = 12
                        }
                        binding.epapPressure.max = 46
                    }
                    // CE
                    2 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.ipapPressure.min = 8
                        }
                        binding.epapPressure.max = 50
                    }
                }
                if (data.deviceMode == 2) {
                    binding.ventilationSetting.visibility = View.VISIBLE
                    binding.warningSetting.visibility = View.VISIBLE
                } else {
                    binding.ventilationSetting.visibility = View.GONE
                    binding.ventilationSettingLayout.visibility = View.GONE
                    binding.warningSetting.visibility = View.GONE
                    binding.warningSettingLayout.visibility = View.GONE
                }
                if (data.isVentilated) {
                    binding.systemSetting.visibility = View.GONE
                    binding.systemSettingLayout.visibility = View.GONE
                    binding.measureSetting.visibility = View.GONE
                    binding.measureSettingLayout.visibility = View.GONE
                } else {
                    binding.systemSetting.visibility = View.VISIBLE
                    binding.measureSetting.visibility = View.VISIBLE
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtStateError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtParam)
            .observe(this) {
                val data = it.data as RtParam
                binding.dataLog.text = "pressure：${data.pressure} ${if (systemSetting.unitSetting.pressureUnit == 0) "cmH2O" else "hPa"}\n" +
                        "ipap：${data.ipap} ${if (systemSetting.unitSetting.pressureUnit == 0) "cmH2O" else "hPa"}\n" +
                        "epap：${data.epap} ${if (systemSetting.unitSetting.pressureUnit == 0) "cmH2O" else "hPa"}\n" +
                        "vt：${if (data.vt < 0 || data.vt > 3000) "**" else data.vt}mL\n" +
                        "mv：${if (data.mv < 0 || data.mv > 60) "**" else data.mv} L/min\n" +
                        "leak：${if (data.leak < 0 || data.leak > 120) "**" else data.leak} L/min\n" +
                        "rr：${if (data.rr < 0 || data.rr > 60) "**" else data.rr} bpm\n" +
                        "ti：${if (data.ti < 0.1 || data.ti > 4) "--" else data.ti} s\n" +
                        "ie：${if (data.ie < 0.02 || data.ie > 3) "--" else {
                            if (data.ie < 1) {
                                "1:" + String.format("%.1f", 1f/data.ie)
                            } else {
                                String.format("%.1f", data.ie) + ":1"
                            }
                        }}\n" +
                        "spo2：${if (data.spo2 < 70 || data.spo2 > 100) "**" else data.spo2} %\n" +
                        "pr：${if (data.pr < 30 || data.pr > 250) "**" else data.pr} bpm\n" +
                        "hr：${if (data.hr < 30 || data.hr > 250) "**" else data.hr} bpm"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtParamError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorMaskTest)
            .observe(this) {
                val data = it.data as MaskTestResult
                binding.ventilatorMaskTest.isChecked = data.status == 1
                // BLE医生模式下
                if (this::rtState.isInitialized) {
                    if (rtState.deviceMode == 2) {
                        binding.ventilatorVentilationSwitch.isEnabled = data.status != 1
                    }
                }
                binding.ventilatorMaskTestText.text = "status：${when (data.status) {
                    0 -> "未在测试状态"
                    1 -> "测试中"
                    2 -> "测试结束"
                    else -> "无"
                }}\nleak：${data.leak} L/min\nresult：${when (data.result) {
                    0 -> "测试未完成"
                    1 -> "不合适"
                    2 -> "合适"
                    else -> "无"
                }
                }"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorMaskTestError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEvent)
            .observe(this) {
                // data.eventId: Constant.VentilatorEventId
                // data.alarmLevel: Constant.VentilatorAlarmLevel
                val data = it.data as Event
                binding.ventilatorEvent.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEventError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSetting)
            .observe(this) {
                spinnerSet = false
                systemSetting = it.data as SystemSetting
                binding.unit.setSelection(systemSetting.unitSetting.pressureUnit)
                binding.language.setSelection(systemSetting.languageSetting.language)
                binding.brightness.progress = systemSetting.screenSetting.brightness
                binding.screenOff.setSelection(systemSetting.screenSetting.autoOff.div(30))
                binding.filter.progress = systemSetting.replacements.filter
                binding.mask.progress = systemSetting.replacements.mask
                binding.tube.progress = systemSetting.replacements.tube
                binding.tank.progress = systemSetting.replacements.tank
                binding.volume.progress = systemSetting.volumeSetting.volume.div(5)
                spinnerSet = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSettingError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetSystemSetting)
            .observe(this) {
                binding.dataLog.text = "系统设置成功"
                BleServiceHelper.BleServiceHelper.ventilatorGetSystemSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSetting)
            .observe(this) {
                spinnerSet = false
                measureSetting = it.data as MeasureSetting
                if (measureSetting.humidification.humidification == 0xff) {
                    binding.humidification.setSelection(6)
                } else if (measureSetting.humidification.humidification > 6) {
                    binding.humidification.setSelection(0)
                } else {
                    binding.humidification.setSelection(measureSetting.humidification.humidification)
                }
                if (measureSetting.pressureReduce.epr > 4) {
                    binding.epr.setSelection(0)
                } else {
                    binding.epr.setSelection(measureSetting.pressureReduce.epr)
                }
                binding.autoStart.isChecked = measureSetting.autoSwitch.isAutoStart
                binding.autoEnd.isChecked = measureSetting.autoSwitch.isAutoEnd
                binding.preHeat.isChecked = measureSetting.preHeat.isOn
                binding.rampPressure.progress = measureSetting.ramp.pressure.div(0.5).toInt()
                if (this::rtState.isInitialized) {
                    when (rtState.ventilationMode) {
                        // CPAP
                        0 -> binding.rampPressure.max = binding.cpapPressure.progress
                        // APAP
                        1 -> binding.rampPressure.max = binding.apapPressureMin.progress
                        // S、S/T、T
                        2, 3, 4 -> binding.rampPressure.max = binding.epapPressure.progress
                    }
                }
                binding.rampTime.progress = measureSetting.ramp.time.div(5)
                binding.tubeType.setSelection(measureSetting.tubeType.type)
                binding.maskType.setSelection(measureSetting.mask.type)
                binding.maskPressure.progress = measureSetting.mask.pressure.toInt()
                spinnerSet = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSettingError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetMeasureSetting)
            .observe(this) {
                binding.dataLog.text = "测量设置成功"
                BleServiceHelper.BleServiceHelper.ventilatorGetMeasureSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSetting)
            .observe(this) {
                spinnerSet = false
                ventilationSetting = it.data as VentilationSetting
                binding.ventilationMode.setSelection(ventilationSetting.ventilationMode.mode)
                layoutGone()
                layoutVisible(ventilationSetting.ventilationMode.mode)
                binding.cpapPressure.progress = ventilationSetting.cpapPressure.pressure.div(0.5).toInt()
                binding.apapPressureMax.progress = ventilationSetting.apapPressureMax.max.div(0.5).toInt()
                binding.apapPressureMin.progress = ventilationSetting.apapPressureMin.min.div(0.5).toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.apapPressureMax.min = binding.apapPressureMin.progress
                }
                binding.apapPressureMin.max = binding.apapPressureMax.progress
                binding.ipapPressure.progress = ventilationSetting.pressureInhale.inhale.div(0.5).toInt()
                binding.epapPressure.progress = ventilationSetting.pressureExhale.exhale.div(0.5).toInt()
                if (this::rtState.isInitialized) {
                    if (rtState.standard == 2) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.ipapPressure.min = binding.epapPressure.progress
                        }
                        binding.epapPressure.max = binding.ipapPressure.progress
                    } else if (rtState.standard == 1) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.ipapPressure.min = binding.epapPressure.progress + 4
                        }
                        binding.epapPressure.max = binding.ipapPressure.progress - 4
                    }
                }
                binding.raiseTime.progress = ventilationSetting.pressureRaiseDuration.duration.div(50)
                val limT = when (ventilationSetting.inhaleDuration.duration) {
                    0.3f -> 200
                    0.4f -> 250
                    0.5f -> 300
                    0.6f -> 400
                    0.7f -> 450
                    0.8f -> 500
                    0.9f -> 600
                    1.0f -> 650
                    1.1f -> 700
                    1.2f -> 800
                    1.3f -> 850
                    else -> 900
                }
                val iepap = ventilationSetting.pressureInhale.inhale - ventilationSetting.pressureExhale.exhale
                val minT = when (iepap) {
                    in 2.0..5.0 -> 100
                    in 5.5..10.0 -> 200
                    in 10.5..15.0 -> 300
                    in 15.5..20.0 -> 400
                    in 20.5..21.0 -> 450
                    else -> 100
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.raiseTime.min = max(100, minT).div(50)
                }
                binding.raiseTime.max = min((limT), 900).div(50)
                binding.inspiratoryTime.progress = ventilationSetting.inhaleDuration.duration.times(10).toInt()
                val temp = when (ventilationSetting.pressureRaiseDuration.duration) {
                    200 -> 0.3f
                    250 -> 0.4f
                    300 -> 0.5f
                    400 -> 0.6f
                    450 -> 0.7f
                    500 -> 0.8f
                    600 -> 0.9f
                    650 -> 1.0f
                    700 -> 1.1f
                    800 -> 1.2f
                    850 -> 1.3f
                    else -> 0.3f
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.inspiratoryTime.min = max(0.3f, temp).times(10).toInt()
                }
                binding.inspiratoryTime.max = min((60f/ventilationSetting.respiratoryRate.rate)*2/3, 4.0f).times(10).toInt()
                binding.respiratoryFrequency.progress = ventilationSetting.respiratoryRate.rate
                binding.respiratoryFrequency.max = min((60/(ventilationSetting.inhaleDuration.duration/2*3).toInt()),30)
                binding.iTrigger.setSelection(ventilationSetting.inhaleSensitive.sentive)
                binding.eTrigger.setSelection(ventilationSetting.exhaleSensitive.sentive)
                spinnerSet = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSettingError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetVentilationSetting)
            .observe(this) {
                binding.dataLog.text = "通气设置成功"
                BleServiceHelper.BleServiceHelper.ventilatorGetVentilationSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSetting)
            .observe(this) {
                spinnerSet = false
                warningSetting = it.data as WarningSetting
                binding.leakHigh.setSelection(warningSetting.warningLeak.high.div(15))
                binding.lowVentilation.progress = warningSetting.warningVentilation.low
                binding.vtLow.progress = if (warningSetting.warningVt.low == 0) {
                    19
                } else {
                    warningSetting.warningVt.low.div(10)
                }
                binding.rrHigh.progress = warningSetting.warningRrHigh.high
                binding.rrLow.progress = warningSetting.warningRrLow.low
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (warningSetting.warningRrLow.low != 0 && warningSetting.warningRrHigh.high != 0) {
                        binding.rrHigh.min = binding.rrLow.progress + 2
                        binding.rrLow.max = binding.rrHigh.progress - 2
                    } else {
                        binding.rrLow.min = 0
                        binding.rrLow.max = 60
                        binding.rrHigh.min = 0
                        binding.rrHigh.max = 60
                    }
                }
                binding.spo2Low.progress = if (warningSetting.warningSpo2Low.low == 0) {
                    79
                } else {
                    warningSetting.warningSpo2Low.low
                }
                binding.hrHigh.progress = if (warningSetting.warningHrHigh.high == 0) {
                    9
                } else {
                    warningSetting.warningHrHigh.high.div(10)
                }
                binding.hrLow.progress = if (warningSetting.warningHrLow.low == 0) {
                    5
                } else {
                    warningSetting.warningHrLow.low.div(5)
                }
                binding.apnea.setSelection(warningSetting.warningApnea.apnea.div(10))
                spinnerSet = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSettingError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWarningSetting)
            .observe(this) {
                binding.dataLog.text = "警告设置成功"
                BleServiceHelper.BleServiceHelper.ventilatorGetWarningSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorVentilationSwitch)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorFactoryReset)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
        BleServiceHelper.BleServiceHelper.ventilatorReadFile(model, fileNames[0])
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // 蓝牙状态 Ble.State
        Log.d(TAG, "model $model, state: $state")

        _bleState.value = state == Ble.State.CONNECTED
        if (state == Ble.State.DISCONNECTED) {
            finish()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

    private fun layoutGone() {
        binding.eprLayout.visibility = View.GONE
        binding.cpapPressureLayout.visibility = View.GONE
        binding.apapPressureMaxLayout.visibility = View.GONE
        binding.apapPressureMinLayout.visibility = View.GONE
        binding.ipapPressureLayout.visibility = View.GONE
        binding.epapPressureLayout.visibility = View.GONE
        binding.raiseTimeLayout.visibility = View.GONE
        binding.iTriggerLayout.visibility = View.GONE
        binding.eTriggerLayout.visibility = View.GONE
        binding.inspiratoryTimeLayout.visibility = View.GONE
        binding.respiratoryFrequencyLayout.visibility = View.GONE
        binding.lowVentilationLayout.visibility = View.GONE
        binding.vtLowLayout.visibility = View.GONE
        binding.rrHighLayout.visibility = View.GONE
        binding.rrLowLayout.visibility = View.GONE
    }
    private fun layoutVisible(mode: Int) {
        when (mode) {
            // CPAP
            0 -> {
                binding.eprLayout.visibility = View.VISIBLE
                binding.cpapPressureLayout.visibility = View.VISIBLE
            }
            // APAP
            1 -> {
                binding.eprLayout.visibility = View.VISIBLE
                binding.apapPressureMaxLayout.visibility = View.VISIBLE
                binding.apapPressureMinLayout.visibility = View.VISIBLE
            }
            // S
            2 -> {
                binding.ipapPressureLayout.visibility = View.VISIBLE
                binding.epapPressureLayout.visibility = View.VISIBLE
                binding.raiseTimeLayout.visibility = View.VISIBLE
                binding.iTriggerLayout.visibility = View.VISIBLE
                binding.eTriggerLayout.visibility = View.VISIBLE
                binding.lowVentilationLayout.visibility = View.VISIBLE
                binding.vtLowLayout.visibility = View.VISIBLE
                binding.rrHighLayout.visibility = View.VISIBLE
                binding.rrLowLayout.visibility = View.VISIBLE
            }
            // S/T
            3 -> {
                binding.ipapPressureLayout.visibility = View.VISIBLE
                binding.epapPressureLayout.visibility = View.VISIBLE
                binding.inspiratoryTimeLayout.visibility = View.VISIBLE
                binding.respiratoryFrequencyLayout.visibility = View.VISIBLE
                binding.raiseTimeLayout.visibility = View.VISIBLE
                binding.iTriggerLayout.visibility = View.VISIBLE
                binding.eTriggerLayout.visibility = View.VISIBLE
                binding.lowVentilationLayout.visibility = View.VISIBLE
                binding.vtLowLayout.visibility = View.VISIBLE
                binding.rrHighLayout.visibility = View.VISIBLE
                binding.rrLowLayout.visibility = View.VISIBLE
            }
            // T
            4 -> {
                binding.ipapPressureLayout.visibility = View.VISIBLE
                binding.epapPressureLayout.visibility = View.VISIBLE
                binding.inspiratoryTimeLayout.visibility = View.VISIBLE
                binding.respiratoryFrequencyLayout.visibility = View.VISIBLE
                binding.raiseTimeLayout.visibility = View.VISIBLE
                binding.lowVentilationLayout.visibility = View.VISIBLE
                binding.vtLowLayout.visibility = View.VISIBLE
                binding.rrHighLayout.visibility = View.VISIBLE
                binding.rrLowLayout.visibility = View.VISIBLE
            }
        }
    }
}