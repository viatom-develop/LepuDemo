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
import com.lepu.blepro.utils.DateUtil
import kotlinx.android.synthetic.main.activity_ventilator.*
import java.util.*
import kotlin.math.max
import kotlin.math.min

class VentilatorActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "VentilatorActivity"
    // Bluetooth.MODEL_R20, Bluetooth.MODEL_R21, Bluetooth.MODEL_R10, Bluetooth.MODEL_R11, Bluetooth.MODEL_LERES
    private var model = Bluetooth.MODEL_R20
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
        setContentView(R.layout.activity_ventilator)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
        // 连接蓝牙后进行交换密钥通讯，进入加密模式，所有指令允许执行
        // 不进行加密模式，部分指令不允许执行
        BleServiceHelper.BleServiceHelper.ventilatorEncrypt(model, "0001")
    }

    private fun initView() {
        ble_name.text = deviceName
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            wifi_rcv.layoutManager = this
        }
        wifiAdapter = StringAdapter(R.layout.device_item, null).apply {
            wifi_rcv.adapter = this
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
        ventilator_ventilation_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                BleServiceHelper.BleServiceHelper.ventilatorVentilationSwitch(model, isChecked)
            }
        }
        ventilator_mask_test.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                BleServiceHelper.BleServiceHelper.ventilatorMaskTest(model, isChecked)
            }
        }
        get_rt_state.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ventilatorGetRtState(model)
        }
        get_rt_param.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ventilatorGetRtParam(model)
        }
        get_file_list.setOnClickListener {
            fileNames.clear()
            BleServiceHelper.BleServiceHelper.ventilatorGetFileList(model)
        }
        read_file.setOnClickListener {
            readFile()
        }
        get_wifi_list.setOnClickListener {
            wifiList.clear()
            BleServiceHelper.BleServiceHelper.ventilatorGetWifiList(model)
        }
        get_wifi_config.setOnClickListener {
            BleServiceHelper.BleServiceHelper.ventilatorGetWifiConfig(model)
        }
        bleState.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }
        system_setting.setOnClickListener {
            system_setting.background = getDrawable(R.drawable.string_selected)
            measure_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            ventilation_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            warning_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            other_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            system_setting_layout.visibility = View.VISIBLE
            measure_setting_layout.visibility = View.GONE
            ventilation_setting_layout.visibility = View.GONE
            warning_setting_layout.visibility = View.GONE
            other_layout.visibility = View.GONE
            spinnerSet = false
            BleServiceHelper.BleServiceHelper.ventilatorGetSystemSetting(model)
        }
        measure_setting.setOnClickListener {
            measure_setting.background = getDrawable(R.drawable.string_selected)
            system_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            ventilation_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            warning_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            other_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            measure_setting_layout.visibility = View.VISIBLE
            system_setting_layout.visibility = View.GONE
            ventilation_setting_layout.visibility = View.GONE
            warning_setting_layout.visibility = View.GONE
            other_layout.visibility = View.GONE
            spinnerSet = false
            BleServiceHelper.BleServiceHelper.ventilatorGetVentilationSetting(model)
            BleServiceHelper.BleServiceHelper.ventilatorGetMeasureSetting(model)
        }
        ventilation_setting.setOnClickListener {
            ventilation_setting.background = getDrawable(R.drawable.string_selected)
            system_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            measure_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            warning_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            other_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            ventilation_setting_layout.visibility = View.VISIBLE
            system_setting_layout.visibility = View.GONE
            measure_setting_layout.visibility = View.GONE
            warning_setting_layout.visibility = View.GONE
            other_layout.visibility = View.GONE
            BleServiceHelper.BleServiceHelper.ventilatorGetRtState(model)
            spinnerSet = false
            BleServiceHelper.BleServiceHelper.ventilatorGetVentilationSetting(model)
        }
        warning_setting.setOnClickListener {
            warning_setting.background = getDrawable(R.drawable.string_selected)
            system_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            measure_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            ventilation_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            other_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            warning_setting_layout.visibility = View.VISIBLE
            system_setting_layout.visibility = View.GONE
            measure_setting_layout.visibility = View.GONE
            ventilation_setting_layout.visibility = View.GONE
            other_layout.visibility = View.GONE
            spinnerSet = false
            BleServiceHelper.BleServiceHelper.ventilatorGetWarningSetting(model)
        }
        other_setting.setOnClickListener {
            other_setting.background = getDrawable(R.drawable.string_selected)
            system_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            measure_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            ventilation_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            warning_setting.background = getDrawable(R.drawable.dialog_hint_shape)
            other_layout.visibility = View.VISIBLE
            system_setting_layout.visibility = View.GONE
            measure_setting_layout.visibility = View.GONE
            ventilation_setting_layout.visibility = View.GONE
            warning_setting_layout.visibility = View.GONE
            BleServiceHelper.BleServiceHelper.ventilatorGetRtState(model)
        }
        // 绑定/解绑
        bound.setOnCheckedChangeListener { buttonView, isChecked ->
            BleServiceHelper.BleServiceHelper.ventilatorDeviceBound(model, isChecked)
        }
        // 进入医生模式
        doctor_mode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    var pin = pin.text.toString()
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
            unit.adapter = this
        }
        unit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            language.adapter = this
        }
        language.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        brightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.SCREEN
                    systemSetting.screenSetting.brightness = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                brightness_process.text = "$progress %"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    brightness_range.text = "范围：${brightness.min}% - ${brightness.max}%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        brightness_sub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.SCREEN
            systemSetting.screenSetting.brightness = --brightness.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        brightness_add.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.SCREEN
            systemSetting.screenSetting.brightness = ++brightness.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 屏幕设置：自动熄屏
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("常亮", "30秒", "60秒", "90秒", "120秒")).apply {
            screen_off.adapter = this
        }
        screen_off.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        filter.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
                    systemSetting.replacements.filter = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                filter_process.text = if (progress == 0) {
                    "关"
                } else {
                    "$progress 个月"
                }
                filter_range.text = "范围：关 - ${filter.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        filter_sub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.filter = --filter.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        filter_add.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.filter = ++filter.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：面罩
        mask.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
                    systemSetting.replacements.mask = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                mask_process.text = if (progress == 0) {
                    "关"
                } else {
                    "$progress 个月"
                }
                mask_range.text = "范围：关 - ${mask.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        mask_sub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.mask = --mask.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        mask_add.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.mask = ++mask.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：管道
        tube.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
                    systemSetting.replacements.tube = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                tube_process.text = if (progress == 0) {
                    "关"
                } else {
                    "$progress 个月"
                }
                tube_range.text = "范围：关 - ${tube.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        tube_sub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.tube = --tube.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        tube_add.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.tube = ++tube.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：水箱
        tank.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
                    systemSetting.replacements.tank = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                tank_process.text = if (progress == 0) {
                    "关闭"
                } else {
                    "$progress 个月"
                }
                tank_range.text = "范围：关 - ${tank.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        tank_sub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.tank = --tank.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        tank_add.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.REPLACEMENT
            systemSetting.replacements.tank = ++tank.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 音量设置
        volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = Constant.VentilatorSystemSetting.VOLUME
                    systemSetting.volumeSetting.volume = progress.times(5)
                    BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
                }
                volume_process.text = "${progress.times(5)} %"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    volume_range.text = "范围：${volume.min}% - ${volume.max}%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        volume_sub.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.VOLUME
            volume.progress--
            systemSetting.volumeSetting.volume = volume.progress.times(5)
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        volume_add.setOnClickListener {
            systemSetting.type = Constant.VentilatorSystemSetting.VOLUME
            volume.progress++
            systemSetting.volumeSetting.volume = volume.progress.times(5)
            BleServiceHelper.BleServiceHelper.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 测量设置
        // 湿化等级
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("关闭", "1", "2", "3", "4", "5", "自动")).apply {
            humidification.adapter = this
        }
        humidification.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            epr.adapter = this
        }
        epr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        auto_start.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                measureSetting.type = Constant.VentilatorMeasureSetting.AUTO_SWITCH
                measureSetting.autoSwitch.isAutoStart = isChecked
                BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
            }
        }
        // 自动启停：自动停止
        auto_end.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                measureSetting.type = Constant.VentilatorMeasureSetting.AUTO_SWITCH
                measureSetting.autoSwitch.isAutoEnd = isChecked
                BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
            }
        }
        // 预加热
        pre_heat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                measureSetting.type = Constant.VentilatorMeasureSetting.PRE_HEAT
                measureSetting.preHeat.isOn = isChecked
                BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
            }
        }
        // 缓冲压力
        ramp_pressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
                    measureSetting.ramp.pressure = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
                ramp_pressure_process.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ramp_pressure_range.text = "范围：${ramp_pressure.min.times(0.5f)} - ${ramp_pressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
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
        ramp_pressure_sub.setOnClickListener {
            ramp_pressure.progress--
            measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
            measureSetting.ramp.pressure = ramp_pressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        ramp_pressure_add.setOnClickListener {
            ramp_pressure.progress++
            measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
            measureSetting.ramp.pressure = ramp_pressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 缓冲时间
        ramp_time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
                    measureSetting.ramp.time = progress.times(5)
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
                ramp_time_process.text = "${progress.times(5)}min"
                ramp_time_range.text = "范围：关 - ${ramp_time.max.times(5)}min"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        ramp_time_sub.setOnClickListener {
            ramp_time.progress--
            measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
            measureSetting.ramp.time = ramp_time.progress.times(5)
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        ramp_time_add.setOnClickListener {
            ramp_time.progress++
            measureSetting.type = Constant.VentilatorMeasureSetting.RAMP
            measureSetting.ramp.time = ramp_time.progress.times(5)
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 管道类型
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("15mm", "22mm")).apply {
            tube_type.adapter = this
        }
        tube_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            mask_type.adapter = this
        }
        mask_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        mask_pressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    measureSetting.type = Constant.VentilatorMeasureSetting.MASK
                    measureSetting.mask.pressure = progress.toFloat()
                    BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
                }
                mask_pressure_process.text = "${progress.toFloat()}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mask_pressure_range.text = "范围：${mask_pressure.min.times(1.0f)} - ${mask_pressure.max.times(1.0f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
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
        mask_pressure_sub.setOnClickListener {
            mask_pressure.progress--
            measureSetting.type = Constant.VentilatorMeasureSetting.MASK
            measureSetting.mask.pressure = mask_pressure.progress.toFloat()
            BleServiceHelper.BleServiceHelper.ventilatorSetMeasureSetting(model, measureSetting)
        }
        mask_pressure_add.setOnClickListener {
            mask_pressure.progress++
            measureSetting.type = Constant.VentilatorMeasureSetting.MASK
            measureSetting.mask.pressure = mask_pressure.progress.toFloat()
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
            ventilation_mode.adapter = this
        }
        ventilation_mode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        cpap_pressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE
                    ventilationSetting.cpapPressure.pressure = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                cpap_pressure_process.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    cpap_pressure_range.text = "范围：${cpap_pressure.min.times(0.5f)} - ${cpap_pressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
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
        cpap_pressure_sub.setOnClickListener {
            cpap_pressure.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE
            ventilationSetting.cpapPressure.pressure = cpap_pressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        cpap_pressure_add.setOnClickListener {
            cpap_pressure.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE
            ventilationSetting.cpapPressure.pressure = cpap_pressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // APAP模式压力最大值Pmax
        apap_pressure_max.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MAX
                    ventilationSetting.apapPressureMax.max = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                apap_pressure_max_process.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    apap_pressure_max_range.text = "范围：${apap_pressure_max.min.times(0.5f)} - ${apap_pressure_max.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
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
        apap_pressure_max_sub.setOnClickListener {
            apap_pressure_max.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MAX
            ventilationSetting.apapPressureMax.max = apap_pressure_max.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        apap_pressure_max_add.setOnClickListener {
            apap_pressure_max.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MAX
            ventilationSetting.apapPressureMax.max = apap_pressure_max.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // APAP模式压力最小值Pmin
        apap_pressure_min.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MIN
                    ventilationSetting.apapPressureMin.min = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                apap_pressure_min_process.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    apap_pressure_min_range.text = "范围：${apap_pressure_min.min.times(0.5f)} - ${apap_pressure_min.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
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
        apap_pressure_min_sub.setOnClickListener {
            apap_pressure_min.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MIN
            ventilationSetting.apapPressureMin.min = apap_pressure_min.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        apap_pressure_min_add.setOnClickListener {
            apap_pressure_min.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_MIN
            ventilationSetting.apapPressureMin.min = apap_pressure_min.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气压力
        ipap_pressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_INHALE
                    ventilationSetting.pressureInhale.inhale = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                ipap_pressure_process.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ipap_pressure_range.text = "范围：${ipap_pressure.min.times(0.5f)} - ${ipap_pressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
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
        ipap_pressure_sub.setOnClickListener {
            ipap_pressure.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_INHALE
            ventilationSetting.pressureInhale.inhale = ipap_pressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        ipap_pressure_add.setOnClickListener {
            ipap_pressure.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_INHALE
            ventilationSetting.pressureInhale.inhale = ipap_pressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 呼气压力
        epap_pressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_EXHALE
                    ventilationSetting.pressureExhale.exhale = progress.times(0.5f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                epap_pressure_process.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    epap_pressure_range.text = "范围：${epap_pressure.min.times(0.5f)} - ${epap_pressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
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
        epap_pressure_sub.setOnClickListener {
            epap_pressure.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_EXHALE
            ventilationSetting.pressureExhale.exhale = epap_pressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        epap_pressure_add.setOnClickListener {
            epap_pressure.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.PRESSURE_EXHALE
            ventilationSetting.pressureExhale.exhale = epap_pressure.progress.times(0.5f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气时间
        inspiratory_time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.INHALE_DURATION
                    ventilationSetting.inhaleDuration.duration = progress.div(10f)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                inspiratory_time_process.text = "${progress.div(10f)}s"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    inspiratory_time_range.text = "范围：${String.format("%.1f", inspiratory_time.min.times(0.1f))} - ${String.format("%.1f", inspiratory_time.max.times(0.1f))}s"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        inspiratory_time_sub.setOnClickListener {
            inspiratory_time.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.INHALE_DURATION
            ventilationSetting.inhaleDuration.duration = inspiratory_time.progress.div(10f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        inspiratory_time_add.setOnClickListener {
            inspiratory_time.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.INHALE_DURATION
            ventilationSetting.inhaleDuration.duration = inspiratory_time.progress.div(10f)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 呼吸频率
        respiratory_frequency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.RESPIRATORY_RATE
                    ventilationSetting.respiratoryRate.rate = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                respiratory_frequency_process.text = "$progress bpm"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    respiratory_frequency_range.text = "范围：${respiratory_frequency.min} - ${respiratory_frequency.max}bpm"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        respiratory_frequency_sub.setOnClickListener {
            respiratory_frequency.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.RESPIRATORY_RATE
            ventilationSetting.respiratoryRate.rate = respiratory_frequency.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        respiratory_frequency_add.setOnClickListener {
            respiratory_frequency.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.RESPIRATORY_RATE
            ventilationSetting.respiratoryRate.rate = respiratory_frequency.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 压力上升时间
        raise_time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = Constant.VentilatorVentilationSetting.RAISE_DURATION
                    ventilationSetting.pressureRaiseDuration.duration = progress.times(50)
                    BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                raise_time_process.text = "${progress.times(50)}ms"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    raise_time_range.text = "范围：${raise_time.min.times(50)} - ${raise_time.max.times(50)}ms"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        raise_time_sub.setOnClickListener {
            raise_time.progress--
            ventilationSetting.type = Constant.VentilatorVentilationSetting.RAISE_DURATION
            ventilationSetting.pressureRaiseDuration.duration = raise_time.progress.times(50)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        raise_time_add.setOnClickListener {
            raise_time.progress++
            ventilationSetting.type = Constant.VentilatorVentilationSetting.RAISE_DURATION
            ventilationSetting.pressureRaiseDuration.duration = raise_time.progress.times(50)
            BleServiceHelper.BleServiceHelper.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气触发灵敏度
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("自动挡", "1", "2", "3", "4", "5")).apply {
            i_trigger.adapter = this
        }
        i_trigger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            e_trigger.adapter = this
        }
        e_trigger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            leak_high.adapter = this
        }
        leak_high.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                                auto_end.isChecked = false
                                auto_end.isEnabled = false
                            }
                            .setNegativeButton("取消") { _, _ ->
                                leak_high.setSelection(0)
                            }
                            .create()
                            .show()
                    } else {
                        warningSetting.type = Constant.VentilatorWarningSetting.LEAK_HIGH
                        warningSetting.warningLeak.high = position.times(15)
                        BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                        if (position == 0) {
                            auto_end.isEnabled = true
                        }
                    }
                } else {
                    if (position == 0) {
                        auto_end.isEnabled = true
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼吸暂停
        ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf("关闭", "10s", "20s", "30s")).apply {
            apnea.adapter = this
        }
        apnea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        vt_low.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                vt_low_process.text = if (progress == 19) {
                    "关"
                } else {
                    "${progress.times(10)}ml"
                }
                vt_low_range.text = "范围：关 - ${vt_low.max.times(10)}ml"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        vt_low_sub.setOnClickListener {
            vt_low.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.VT_LOW
            warningSetting.warningVt.low = if (vt_low.progress == 19) {
                0
            } else {
                vt_low.progress.times(10)
            }
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        vt_low_add.setOnClickListener {
            vt_low.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.VT_LOW
            warningSetting.warningVt.low = vt_low.progress.times(10)
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 分钟通气量低
        low_ventilation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.LOW_VENTILATION
                    warningSetting.warningVentilation.low = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                low_ventilation_process.text = if (progress == 0) {
                    "关"
                } else {
                    "${progress}L/min"
                }
                low_ventilation_range.text = "范围：关 - ${low_ventilation.max}L/min"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        low_ventilation_sub.setOnClickListener {
            low_ventilation.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.LOW_VENTILATION
            warningSetting.warningVentilation.low = low_ventilation.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        low_ventilation_add.setOnClickListener {
            low_ventilation.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.LOW_VENTILATION
            warningSetting.warningVentilation.low = low_ventilation.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 呼吸频率高
        rr_high.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.RR_HIGH
                    warningSetting.warningRrHigh.high = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                rr_high_process.text = if (progress == 0) {
                    "关"
                } else {
                    "${progress}bpm"
                }
                rr_high_range.text = "范围：关 - ${rr_high.max}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        rr_high_sub.setOnClickListener {
            rr_high.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.RR_HIGH
            warningSetting.warningRrHigh.high = rr_high.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        rr_high_add.setOnClickListener {
            rr_high.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.RR_HIGH
            warningSetting.warningRrHigh.high = rr_high.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 呼吸频率低
        rr_low.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = Constant.VentilatorWarningSetting.RR_LOW
                    warningSetting.warningRrLow.low = progress
                    BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
                }
                rr_low_process.text = if (progress == 0) {
                    "关"
                } else {
                    "${progress}bpm"
                }
                rr_low_range.text = "范围：关 - ${rr_low.max}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        rr_low_sub.setOnClickListener {
            rr_low.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.RR_LOW
            warningSetting.warningRrLow.low = rr_low.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        rr_low_add.setOnClickListener {
            rr_low.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.RR_LOW
            warningSetting.warningRrLow.low = rr_low.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 血氧饱和度低
        spo2_low.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                spo2_low_process.text = if (progress == 79) {
                    "关"
                } else {
                    "${progress}%"
                }
                spo2_low_range.text = "范围：关 - ${spo2_low.max}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        spo2_low_sub.setOnClickListener {
            spo2_low.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.SPO2_LOW
            warningSetting.warningSpo2Low.low = if (spo2_low.progress == 79) {
                0
            } else {
                spo2_low.progress
            }
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        spo2_low_add.setOnClickListener {
            spo2_low.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.SPO2_LOW
            warningSetting.warningSpo2Low.low = spo2_low.progress
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 脉率/心率高
        hr_high.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                hr_high_process.text = if (progress == 9) {
                    "关"
                } else {
                    "${progress.times(10)}bpm"
                }
                hr_high_range.text = "范围：关 - ${hr_high.max.times(10)}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        hr_high_sub.setOnClickListener {
            hr_high.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.HR_HIGH
            warningSetting.warningHrHigh.high = if (hr_high.progress == 9) {
                0
            } else {
                hr_high.progress.times(10)
            }
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        hr_high_add.setOnClickListener {
            hr_high.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.HR_HIGH
            warningSetting.warningHrHigh.high = hr_high.progress.times(10)
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 脉率/心率低
        hr_low.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                hr_low_process.text = if (progress == 5) {
                    "关"
                } else {
                    "${progress.times(5)}bpm"
                }
                hr_low_range.text = "范围：关 - ${hr_low.max.times(5)}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        hr_low_sub.setOnClickListener {
            hr_low.progress--
            warningSetting.type = Constant.VentilatorWarningSetting.HR_LOW
            warningSetting.warningHrLow.low = if (hr_low.progress == 5) {
                0
            } else {
                hr_low.progress.times(5)
            }
            BleServiceHelper.BleServiceHelper.ventilatorSetWarningSetting(model, warningSetting)
        }
        hr_low_add.setOnClickListener {
            hr_low.progress++
            warningSetting.type = Constant.VentilatorWarningSetting.HR_LOW
            warningSetting.warningHrLow.low = hr_low.progress.times(5)
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
                        data_log.text = "exchange key succcess"
                    }
                    Constant.VentilatorResponseType.TYPE_NORMAL_ERROR -> {
                        data_log.text = "exchange key error, disconnect"
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
                        data_log.text = "sync time succcess"
                    }
                    Constant.VentilatorResponseType.TYPE_DECRYPT_FAILED -> {
                        data_log.text = "decrypt data error, disconnect"
                        BleServiceHelper.BleServiceHelper.disconnect(false)
                    }
                }
                BleServiceHelper.BleServiceHelper.ventilatorGetInfo(model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfo)
            .observe(this) {
                deviceInfo = it.data as DeviceInfo
                data_log.text = "$deviceInfo"
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
                    ventilation_mode.adapter = this
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
                data_log.text = "$data"
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
                data_log.text = "$fileNames"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileListError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileComplete)
            .observe(this) {
                val file = it.data as StatisticsFile
                val unit = systemSetting.unitSetting.pressureUnit
                data_log.text = "文件名：${file.fileName}\n" +
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
                data_log.text = "$data"
                when (data) {
                    Constant.VentilatorResponseType.TYPE_NORMAL_ERROR -> {
                        // The device is currently scanning and cannot be obtained. Please call the query WiFi list command again
                        Handler().postDelayed({
                            BleServiceHelper.BleServiceHelper.ventilatorGetWifiList(it.model)
                        }, 1000)
                    }
                    Constant.VentilatorResponseType.TYPE_DECRYPT_FAILED -> {
                        data_log.text = "encrypt error, disconnect"
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
                data_log.text = "$data"
                when (data) {
                    Constant.VentilatorResponseType.TYPE_NORMAL_ERROR -> {
                        // no wifi config
                        data_log.text = "no wifi config"
                    }
                    Constant.VentilatorResponseType.TYPE_DECRYPT_FAILED -> {
                        data_log.text = "decrypt data error, disconnect"
                        BleServiceHelper.BleServiceHelper.disconnect(false)
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfig)
            .observe(this) {
                val data = it.data as WifiConfig
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWifiConfig)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBound)
            .observe(this) {
                val data = it.data as Int
                bound.isChecked = data == 0
                data_log.text = when (data) {
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
                data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorMode)
            .observe(this) {
                val data = it.data as DoctorModeResult
                data_log.text = if (data.isSuccess) {
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
                data_log.text = "$data"
                ventilator_ventilation_switch.isChecked = data.isVentilated
                ventilator_ventilation_switch.isEnabled = data.deviceMode == 2
                ventilator_mask_test.isEnabled = !data.isVentilated
                doctor_mode.isChecked = data.deviceMode != 0
                device_mode.text = "${when (data.deviceMode) {
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
                            ipap_pressure.min = 12
                        }
                        epap_pressure.max = 46
                    }
                    // CE
                    2 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ipap_pressure.min = 8
                        }
                        epap_pressure.max = 50
                    }
                }
                if (data.deviceMode == 2) {
                    ventilation_setting.visibility = View.VISIBLE
                    warning_setting.visibility = View.VISIBLE
                } else {
                    ventilation_setting.visibility = View.GONE
                    ventilation_setting_layout.visibility = View.GONE
                    warning_setting.visibility = View.GONE
                    warning_setting_layout.visibility = View.GONE
                }
                if (data.isVentilated) {
                    system_setting.visibility = View.GONE
                    system_setting_layout.visibility = View.GONE
                    measure_setting.visibility = View.GONE
                    measure_setting_layout.visibility = View.GONE
                } else {
                    system_setting.visibility = View.VISIBLE
                    measure_setting.visibility = View.VISIBLE
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
                data_log.text = "pressure：${data.pressure} ${if (systemSetting.unitSetting.pressureUnit == 0) "cmH2O" else "hPa"}\n" +
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
                ventilator_mask_test.isChecked = data.status == 1
                // BLE医生模式下
                if (this::rtState.isInitialized) {
                    if (rtState.deviceMode == 2) {
                        ventilator_ventilation_switch.isEnabled = data.status != 1
                    }
                }
                ventilator_mask_test_text.text = "status：${when (data.status) {
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
                ventilator_event.text = "$data"
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
                unit.setSelection(systemSetting.unitSetting.pressureUnit)
                language.setSelection(systemSetting.languageSetting.language)
                brightness.progress = systemSetting.screenSetting.brightness
                screen_off.setSelection(systemSetting.screenSetting.autoOff.div(30))
                filter.progress = systemSetting.replacements.filter
                mask.progress = systemSetting.replacements.mask
                tube.progress = systemSetting.replacements.tube
                tank.progress = systemSetting.replacements.tank
                volume.progress = systemSetting.volumeSetting.volume.div(5)
                spinnerSet = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSettingError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetSystemSetting)
            .observe(this) {
                data_log.text = "系统设置成功"
                BleServiceHelper.BleServiceHelper.ventilatorGetSystemSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSetting)
            .observe(this) {
                spinnerSet = false
                measureSetting = it.data as MeasureSetting
                if (measureSetting.humidification.humidification == 0xff) {
                    humidification.setSelection(6)
                } else if (measureSetting.humidification.humidification > 6) {
                    humidification.setSelection(0)
                } else {
                    humidification.setSelection(measureSetting.humidification.humidification)
                }
                if (measureSetting.pressureReduce.epr > 4) {
                    epr.setSelection(0)
                } else {
                    epr.setSelection(measureSetting.pressureReduce.epr)
                }
                auto_start.isChecked = measureSetting.autoSwitch.isAutoStart
                auto_end.isChecked = measureSetting.autoSwitch.isAutoEnd
                pre_heat.isChecked = measureSetting.preHeat.isOn
                ramp_pressure.progress = measureSetting.ramp.pressure.div(0.5).toInt()
                if (this::rtState.isInitialized) {
                    when (rtState.ventilationMode) {
                        // CPAP
                        0 -> ramp_pressure.max = cpap_pressure.progress
                        // APAP
                        1 -> ramp_pressure.max = apap_pressure_min.progress
                        // S、S/T、T
                        2, 3, 4 -> ramp_pressure.max = epap_pressure.progress
                    }
                }
                ramp_time.progress = measureSetting.ramp.time.div(5)
                tube_type.setSelection(measureSetting.tubeType.type)
                mask_type.setSelection(measureSetting.mask.type)
                mask_pressure.progress = measureSetting.mask.pressure.toInt()
                spinnerSet = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSettingError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetMeasureSetting)
            .observe(this) {
                data_log.text = "测量设置成功"
                BleServiceHelper.BleServiceHelper.ventilatorGetMeasureSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSetting)
            .observe(this) {
                spinnerSet = false
                ventilationSetting = it.data as VentilationSetting
                ventilation_mode.setSelection(ventilationSetting.ventilationMode.mode)
                layoutGone()
                layoutVisible(ventilationSetting.ventilationMode.mode)
                cpap_pressure.progress = ventilationSetting.cpapPressure.pressure.div(0.5).toInt()
                apap_pressure_max.progress = ventilationSetting.apapPressureMax.max.div(0.5).toInt()
                apap_pressure_min.progress = ventilationSetting.apapPressureMin.min.div(0.5).toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    apap_pressure_max.min = apap_pressure_min.progress
                }
                apap_pressure_min.max = apap_pressure_max.progress
                ipap_pressure.progress = ventilationSetting.pressureInhale.inhale.div(0.5).toInt()
                epap_pressure.progress = ventilationSetting.pressureExhale.exhale.div(0.5).toInt()
                if (this::rtState.isInitialized) {
                    if (rtState.standard == 2) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ipap_pressure.min = epap_pressure.progress
                        }
                        epap_pressure.max = ipap_pressure.progress
                    } else if (rtState.standard == 1) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ipap_pressure.min = epap_pressure.progress + 4
                        }
                        epap_pressure.max = ipap_pressure.progress - 4
                    }
                }
                raise_time.progress = ventilationSetting.pressureRaiseDuration.duration.div(50)
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
                    raise_time.min = max(100, minT).div(50)
                }
                raise_time.max = min((limT), 900).div(50)
                inspiratory_time.progress = ventilationSetting.inhaleDuration.duration.times(10).toInt()
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
                    inspiratory_time.min = max(0.3f, temp).times(10).toInt()
                }
                inspiratory_time.max = min((60f/ventilationSetting.respiratoryRate.rate)*2/3, 4.0f).times(10).toInt()
                respiratory_frequency.progress = ventilationSetting.respiratoryRate.rate
                respiratory_frequency.max = min((60/(ventilationSetting.inhaleDuration.duration/2*3).toInt()),30)
                i_trigger.setSelection(ventilationSetting.inhaleSensitive.sentive)
                e_trigger.setSelection(ventilationSetting.exhaleSensitive.sentive)
                spinnerSet = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSettingError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetVentilationSetting)
            .observe(this) {
                data_log.text = "通气设置成功"
                BleServiceHelper.BleServiceHelper.ventilatorGetVentilationSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSetting)
            .observe(this) {
                spinnerSet = false
                warningSetting = it.data as WarningSetting
                leak_high.setSelection(warningSetting.warningLeak.high.div(15))
                low_ventilation.progress = warningSetting.warningVentilation.low
                vt_low.progress = if (warningSetting.warningVt.low == 0) {
                    19
                } else {
                    warningSetting.warningVt.low.div(10)
                }
                rr_high.progress = warningSetting.warningRrHigh.high
                rr_low.progress = warningSetting.warningRrLow.low
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (warningSetting.warningRrLow.low != 0 && warningSetting.warningRrHigh.high != 0) {
                        rr_high.min = rr_low.progress + 2
                        rr_low.max = rr_high.progress - 2
                    } else {
                        rr_low.min = 0
                        rr_low.max = 60
                        rr_high.min = 0
                        rr_high.max = 60
                    }
                }
                spo2_low.progress = if (warningSetting.warningSpo2Low.low == 0) {
                    79
                } else {
                    warningSetting.warningSpo2Low.low
                }
                hr_high.progress = if (warningSetting.warningHrHigh.high == 0) {
                    9
                } else {
                    warningSetting.warningHrHigh.high.div(10)
                }
                hr_low.progress = if (warningSetting.warningHrLow.low == 0) {
                    5
                } else {
                    warningSetting.warningHrLow.low.div(5)
                }
                apnea.setSelection(warningSetting.warningApnea.apnea.div(10))
                spinnerSet = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSettingError)
            .observe(this) {
                // Constant.VentilatorResponseType
                val data = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWarningSetting)
            .observe(this) {
                data_log.text = "警告设置成功"
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
        epr_layout.visibility = View.GONE
        cpap_pressure_layout.visibility = View.GONE
        apap_pressure_max_layout.visibility = View.GONE
        apap_pressure_min_layout.visibility = View.GONE
        ipap_pressure_layout.visibility = View.GONE
        epap_pressure_layout.visibility = View.GONE
        raise_time_layout.visibility = View.GONE
        i_trigger_layout.visibility = View.GONE
        e_trigger_layout.visibility = View.GONE
        inspiratory_time_layout.visibility = View.GONE
        respiratory_frequency_layout.visibility = View.GONE
        low_ventilation_layout.visibility = View.GONE
        vt_low_layout.visibility = View.GONE
        rr_high_layout.visibility = View.GONE
        rr_low_layout.visibility = View.GONE
    }
    private fun layoutVisible(mode: Int) {
        when (mode) {
            // CPAP
            0 -> {
                epr_layout.visibility = View.VISIBLE
                cpap_pressure_layout.visibility = View.VISIBLE
            }
            // APAP
            1 -> {
                epr_layout.visibility = View.VISIBLE
                apap_pressure_max_layout.visibility = View.VISIBLE
                apap_pressure_min_layout.visibility = View.VISIBLE
            }
            // S
            2 -> {
                ipap_pressure_layout.visibility = View.VISIBLE
                epap_pressure_layout.visibility = View.VISIBLE
                raise_time_layout.visibility = View.VISIBLE
                i_trigger_layout.visibility = View.VISIBLE
                e_trigger_layout.visibility = View.VISIBLE
                low_ventilation_layout.visibility = View.VISIBLE
                vt_low_layout.visibility = View.VISIBLE
                rr_high_layout.visibility = View.VISIBLE
                rr_low_layout.visibility = View.VISIBLE
            }
            // S/T
            3 -> {
                ipap_pressure_layout.visibility = View.VISIBLE
                epap_pressure_layout.visibility = View.VISIBLE
                inspiratory_time_layout.visibility = View.VISIBLE
                respiratory_frequency_layout.visibility = View.VISIBLE
                raise_time_layout.visibility = View.VISIBLE
                i_trigger_layout.visibility = View.VISIBLE
                e_trigger_layout.visibility = View.VISIBLE
                low_ventilation_layout.visibility = View.VISIBLE
                vt_low_layout.visibility = View.VISIBLE
                rr_high_layout.visibility = View.VISIBLE
                rr_low_layout.visibility = View.VISIBLE
            }
            // T
            4 -> {
                ipap_pressure_layout.visibility = View.VISIBLE
                epap_pressure_layout.visibility = View.VISIBLE
                inspiratory_time_layout.visibility = View.VISIBLE
                respiratory_frequency_layout.visibility = View.VISIBLE
                raise_time_layout.visibility = View.VISIBLE
                low_ventilation_layout.visibility = View.VISIBLE
                vt_low_layout.visibility = View.VISIBLE
                rr_high_layout.visibility = View.VISIBLE
                rr_low_layout.visibility = View.VISIBLE
            }
        }
    }
}