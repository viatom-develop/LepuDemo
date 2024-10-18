package com.example.lpdemo

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.lpdemo.databinding.ActivityPc300Binding
import com.example.lpdemo.utils.*
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.ext.pc303.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.observer.BleDataObserver
import com.permissionx.guolindev.PermissionX

class Pc300Activity : AppCompatActivity(), BleChangeObserver, BleDataObserver {

    private val TAG = "Pc300Activity"
    // Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
    // Bluetooth.MODEL_GM_300SNT, Bluetooth.MODEL_GM_300SNT_BLE,
    // Bluetooth.MODEL_CMI_303
    private val models = intArrayOf(
        Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
        Bluetooth.MODEL_GM_300SNT, Bluetooth.MODEL_GM_300SNT_BLE,
        Bluetooth.MODEL_CMI_PC303,
    )
    private lateinit var binding: ActivityPc300Binding

    private var saveDeviceAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPc300Binding.inflate(layoutInflater)
        setContentView(binding.root)
        saveDeviceAddress = readStrPreferences(this, "device_address", "")
        initView()
        needPermission()
    }

    private fun initView() {
        binding.tempMode.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.ear_mode) {
                BleServiceHelper.BleServiceHelper.pc300SetTempMode(deviceModel, Ble.Pc300TempMode.EAR_C)
            } else {
                BleServiceHelper.BleServiceHelper.pc300SetTempMode(deviceModel, Ble.Pc300TempMode.ADULT_HEAD_C)
            }
        }
        binding.gluType.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.aiaole_type) {
                BleServiceHelper.BleServiceHelper.pc300SetGlucometerType(deviceModel, Ble.Pc300GluType.AI_AO_LE)
            } else {
                BleServiceHelper.BleServiceHelper.pc300SetGlucometerType(deviceModel, Ble.Pc300GluType.BAI_JIE)
            }
        }
        binding.shareLog.setOnLongClickListener {
            BleServiceHelper.BleServiceHelper.shareLog()
            true
        }
        binding.initLog.setOnCheckedChangeListener { buttonView, isChecked ->
            BleServiceHelper.BleServiceHelper.initLog(isChecked)
        }
        binding.bleState.text = "蓝牙名:$deviceName\n地址:$deviceAddress"
        bleState.observe(this) {
            if (it) {
                binding.bleState.setTextColor(getColor(R.color.color_blue))
                binding.bleState.text = "蓝牙名:$deviceName\n地址:$deviceAddress"
                binding.connectDevice.text = "断开设备"
                BleServiceHelper.BleServiceHelper.pc300GetTempMode(deviceModel)
            } else {
                binding.bleState.setTextColor(getColor(R.color.colorRed))
                binding.bleState.text = "请先连接设备！"
                binding.connectDevice.text = "连接设备"
            }
        }
        binding.connectDevice.setOnClickListener {
            if (binding.connectDevice.text.equals("连接设备")) {
                binding.bleState.setTextColor(getColor(R.color.colorRed))
                binding.bleState.text = "扫描中..."
                binding.connectDevice.text = "停止连接"
                BluetoothController.clear()
                BleServiceHelper.BleServiceHelper.stopScan()
                BleServiceHelper.BleServiceHelper.startScan(models)
            } else if (binding.connectDevice.text.equals("断开设备")) {
                binding.connectDevice.text = "连接设备"
                BleServiceHelper.BleServiceHelper.disconnect(false)
            } else if (binding.connectDevice.text.equals("停止连接")) {
                Toast.makeText(this, "设备连接中...", Toast.LENGTH_SHORT).show()
            }
        }
        binding.deleteDevice.setOnLongClickListener {
            BleServiceHelper.BleServiceHelper.disconnect(false)
            savePreferences(this, "device_address", "")
            Toast.makeText(this, "设备解绑成功", Toast.LENGTH_SHORT).show()
            BleServiceHelper.BleServiceHelper.startScan(models)
            true
        }
        binding.temp.setOnLongClickListener {
            BleServiceHelper.BleServiceHelper.pc300GetRecord(deviceModel, Ble.Pc300RecordType.TEMP_TYPE)
            true
        }
        binding.glu.setOnLongClickListener {
            BleServiceHelper.BleServiceHelper.pc300GetRecord(deviceModel, Ble.Pc300RecordType.AI_AO_LE_GLU_TYPE)
            true
        }
        binding.sys.setOnLongClickListener {
            BleServiceHelper.BleServiceHelper.pc300GetRecord(deviceModel, Ble.Pc300RecordType.BP_TYPE)
            true
        }
        binding.dia.setOnLongClickListener {
            BleServiceHelper.BleServiceHelper.pc300GetRecord(deviceModel, Ble.Pc300RecordType.BP_TYPE)
            true
        }
        binding.mean.setOnLongClickListener {
            BleServiceHelper.BleServiceHelper.pc300GetRecord(deviceModel, Ble.Pc300RecordType.BP_TYPE)
            true
        }
        binding.bpPr.setOnLongClickListener {
            BleServiceHelper.BleServiceHelper.pc300GetRecord(deviceModel, Ble.Pc300RecordType.BP_TYPE)
            true
        }
        binding.ps.setOnLongClickListener {
            BleServiceHelper.BleServiceHelper.pc300GetRecord(deviceModel, Ble.Pc300RecordType.BP_TYPE)
            true
        }
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // 蓝牙状态 Ble.State
        Log.d(TAG, "model $model, state: $state")

        _bleState.value = state == Ble.State.CONNECTED

        when (state) {
            Ble.State.CONNECTED -> {
                binding.bleState.setTextColor(getColor(R.color.color_blue))
                binding.bleState.text = "蓝牙名:$deviceName\n地址:$deviceAddress"
            }
            Ble.State.CONNECTING -> {
                binding.bleState.setTextColor(getColor(R.color.colorRed))
                binding.bleState.text = "随访箱连接中..."
            }
            Ble.State.DISCONNECTED -> {
                binding.bleState.setTextColor(getColor(R.color.colorRed))
                binding.bleState.text = "请先连接设备！"
            }
            Ble.State.DISCONNECTING -> {
                binding.bleState.setTextColor(getColor(R.color.colorRed))
                binding.bleState.text = "断开连接中..."
            }
            Ble.State.UNKNOWN -> {
                binding.bleState.setTextColor(getColor(R.color.colorRed))
                binding.bleState.text = "连接异常，请重启蓝牙尝试连接"
            }
        }

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        BleServiceHelper.BleServiceHelper.disconnect(false)
        super.onDestroy()
    }

    private fun needPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList, "location permission", "ok", "ignore"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList, "location setting", "ok", "ignore"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    Log.d(TAG, "permission : $allGranted, $grantedList, $deniedList")

                    //permission OK, check Bluetooth status
                    if (allGranted)
                        checkBt()
                }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList, "location permission", "ok", "ignore"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList, "location setting", "ok", "ignore"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    Log.d(TAG, "permission : $allGranted, $grantedList, $deniedList")

                    //permission OK, check Bluetooth status
                    if (allGranted)
                        checkBt()
                }
        } else {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList, "location permission", "ok", "ignore"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList, "location setting", "ok", "ignore"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    Log.d(TAG, "permission : $allGranted, $grantedList, $deniedList")

                    //permission OK, check Bluetooth status
                    if (allGranted)
                        checkBt()
                }
        }
    }
    private fun checkBt() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!adapter.isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    if (adapter.enable()) {
                        needService()
                        Toast.makeText(this, "Bluetooth open successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Bluetooth open failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Bluetooth open failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (adapter.enable()) {
                    needService()
                    Toast.makeText(this, "Bluetooth open successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth open failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            needService()
        }
    }
    private fun needService() {
        var gpsEnabled = false
        var networkEnabled = false
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (!gpsEnabled && !networkEnabled) {
            val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            dialog.setMessage("open location service")
            dialog.setPositiveButton("ok") { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(myIntent, 888)
            }
            dialog.setNegativeButton("cancel") { _, _ ->
                finish()
            }
            dialog.setCancelable(false)
            dialog.show()
        } else {
            initService()
        }
    }
    private fun initService() {
        if (BleServiceHelper.BleServiceHelper.checkService()) {
            // BleService already init
//            BleServiceHelper.BleServiceHelper.startScan(models)
        } else {
            // Save the original file path. Er1, VBeat and HHM1 are currently supported
            val rawFolders = SparseArray<String>()
            rawFolders.set(Bluetooth.MODEL_ER1, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_HHM1, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1S, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1_S, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1_H, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1_W, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1_L, "${getExternalFilesDir(null)?.absolutePath}/er1")

            // initRawFolder必须在initService之前调用
            BleServiceHelper.BleServiceHelper.initRawFolder(rawFolders).initService(application).initLog(true)
        }
        BleServiceHelper.BleServiceHelper.setBleDataObserver(this)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        initService()
    }

    override fun onBleDeviceFound(it: Bluetooth) {
        Log.d(TAG, "onBleDeviceFound $it")
        // scan result
        for (b in BluetoothController.getDevices()) {
            if (saveDeviceAddress != "") {
                if (b.macAddr.equals(saveDeviceAddress)) {
                    BleServiceHelper.BleServiceHelper.disconnect(false)
                    // stop scan before connect
                    BleServiceHelper.BleServiceHelper.stopScan()
                    // set interface before connect
                    BleServiceHelper.BleServiceHelper.setInterfaces(it.model)
                    // add observer(ble state)
                    lifecycle.addObserver(BIOL(this, intArrayOf(it.model)))
                    // connect
                    BleServiceHelper.BleServiceHelper.connect(applicationContext, it.model, it.device)

                    deviceModel = it.model
                    deviceName = it.name
                    deviceAddress = it.macAddr
                    savePreferences(this, "device_address", deviceAddress)
                    BluetoothController.clear()
                    break
                }
            } else {
                if (b.name.contains("PC_300SNT")) {
                    BleServiceHelper.BleServiceHelper.disconnect(false)
                    // stop scan before connect
                    BleServiceHelper.BleServiceHelper.stopScan()
                    // set interface before connect
                    BleServiceHelper.BleServiceHelper.setInterfaces(it.model)
                    // add observer(ble state)
                    lifecycle.addObserver(BIOL(this, intArrayOf(it.model)))
                    // connect
                    BleServiceHelper.BleServiceHelper.connect(applicationContext, it.model, it.device)

                    deviceModel = it.model
                    deviceName = it.name
                    deviceAddress = it.macAddr
                    savePreferences(this, "device_address", deviceAddress)
                    BluetoothController.clear()
                    break
                }
            }
        }
        Log.d(TAG, "EventDeviceFound")
    }

    override fun onBleDeviceReady(model: Int) {
        Log.d(TAG, "onBleDeviceReady $model")
        BleServiceHelper.BleServiceHelper.pc300GetInfo(model)
    }

    override fun onBleBpResult(model: Int, data: BpResult) {
        Log.d(TAG, "onBleBpResult $data")
        binding.sys.text = "收缩压(mmhg) : ${data.sys}"
        binding.dia.text = "舒张压(mmhg) : ${data.dia}"
        binding.mean.text = "平均压(mmhg) : ${data.map}"
        binding.bpPr.text = "心率(次/分钟) : ${data.pr}"
    }

    override fun onBleBpResultError(model: Int, data: BpResultError) {
        Log.d(TAG, "onBleBpResultError $data")
        Toast.makeText(this, "$data", Toast.LENGTH_SHORT).show()
    }

    override fun onBleDeviceInfo(model: Int, data: DeviceInfo) {
        Log.d(TAG, "onBleDeviceInfo $data")
        BleServiceHelper.BleServiceHelper.pc300GetTempMode(model)
    }

    override fun onBleEcgResult(model: Int, data: EcgResult) {

    }

    override fun onBleGetGluType(model: Int, data: Int) {
        Log.d(TAG, "onBleGetGluType $data")
        if (data == Ble.Pc300GluType.AI_AO_LE) {
            binding.tempMode.check(R.id.aiaole_type)
        } else {
            binding.tempMode.check(R.id.baijie_type)
        }
    }

    override fun onBleGetTempMode(model: Int, data: Int) {
        Log.d(TAG, "onBleGetTempMode $data")
        if (data == Ble.Pc300TempMode.EAR_C
            || data == Ble.Pc300TempMode.EAR_F) {
            binding.tempMode.check(R.id.ear_mode)
        } else {
            binding.tempMode.check(R.id.head_mode)
        }
    }

    override fun onBleGluResult(model: Int, data: GluResult) {
        Log.d(TAG, "onBleGluResult $data")
        binding.glu.text = if (data.unit == 0) {
            "血糖(mmol/L) : ${data.data}"
        } else {
            "血糖(mg/dL) : ${data.data}"
        }
    }

    override fun onBleRtBp(model: Int, data: Int) {
        Log.d(TAG, "onBleRtBp $data")
        binding.ps.text = "实时压(mmhg) : $data"
    }

    override fun onBleRtEcgWave(model: Int, data: RtEcgWave) {

    }

    override fun onBleRtOxyParam(model: Int, data: RtOxyParam) {
        Log.d(TAG, "onBleRtOxyParam $data")
        binding.spo2.text = "血氧(%) : ${data.spo2}"
        binding.oxyPr.text = "脉率(次/分钟) : ${data.pr}"
        binding.pi.text = "PI(%) : ${data.pi}"
    }

    override fun onBleRtOxyWave(model: Int, data: RtOxyWave) {

    }

    override fun onBleSetGluType(model: Int, data: Boolean) {
        Log.d(TAG, "onBleSetGluType $data")
        BleServiceHelper.BleServiceHelper.pc300GetGlucometerType(model)
    }

    override fun onBleSetTempMode(model: Int, data: Boolean) {
        Log.d(TAG, "onBleSetTempMode $data")
        BleServiceHelper.BleServiceHelper.pc300GetTempMode(model)
    }

    override fun onBleTempResult(model: Int, data: Float) {
        Log.d(TAG, "onBleTempResult $data")
        binding.temp.text = "体温(℃) : $data"
    }

}