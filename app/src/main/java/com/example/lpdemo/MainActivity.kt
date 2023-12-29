package com.example.lpdemo

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.provider.Settings
import android.util.SparseArray
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lpdemo.utils.*
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.permissionx.guolindev.PermissionX
import kotlinx.android.synthetic.main.activity_main.*
import no.nordicsemi.android.ble.observer.ConnectionObserver

class MainActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "MainActivity"

    private lateinit var dialog: ProgressDialog

    private val models = intArrayOf(
        Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC_60NW, Bluetooth.MODEL_PC_60NW_1,
        Bluetooth.MODEL_PC66B, Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
        Bluetooth.MODEL_OXYSMART, Bluetooth.MODEL_POD2B,
        Bluetooth.MODEL_POD_1W, Bluetooth.MODEL_S5W,
        Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
        Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
        Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
        Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
        Bluetooth.MODEL_S6W, Bluetooth.MODEL_S6W1,
        Bluetooth.MODEL_PC60NW_BLE, Bluetooth.MODEL_PC60NW_WPS,
        Bluetooth.MODEL_PC_60NW_NO_SN, // Pc60fwActivity
        Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
        Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
        Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
        Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
        Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
        Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
        Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
        Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU,
        Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS,
        Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYFIT_WPS,
        Bluetooth.MODEL_KIDSO2_WPS, Bluetooth.MODEL_SI_PO6,  // OxyActivity
        Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
        Bluetooth.MODEL_PC80B_BLE2,  // Pc80bActivity
        Bluetooth.MODEL_PC100,  // Pc102Activity
        Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS,  // Ap20Activity
        Bluetooth.MODEL_PC_68B,  // Pc68bActivity
        Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4,  // PulsebitExActivity
        Bluetooth.MODEL_CHECKME_LE,  // CheckmeLeActivity
        Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
        Bluetooth.MODEL_GM_300SNT, Bluetooth.MODEL_GM_300SNT_BLE,
        Bluetooth.MODEL_CMI_PC303,  // Pc303Activity
        Bluetooth.MODEL_CHECK_POD, Bluetooth.MODEL_CHECKME_POD_WPS,  // CheckmePodActivity
        Bluetooth.MODEL_AOJ20A,  // Aoj20aActivity
        Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS,  // Sp20Activity
        Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_CHECK_ADV,  // CheckmeMonitorActivity
        Bluetooth.MODEL_TV221U,  // Vtm20fActivity
        Bluetooth.MODEL_BPM,  // BpmActivity
        Bluetooth.MODEL_BIOLAND_BGM,  // BiolandBgmActivity
        Bluetooth.MODEL_POCTOR_M3102,  // PoctorM3102Activity
        Bluetooth.MODEL_LPM311,  // Lpm311Activity
        Bluetooth.MODEL_LEM,  // LemActivity
        Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1,  // Er1Activity
        Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
        Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3, Bluetooth.MODEL_ER2_S,  // Er2Activity
        Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T,  // Bp2Activity
        Bluetooth.MODEL_BP2W,  // Bp2wActivity
        Bluetooth.MODEL_LP_BP2W,  // LpBp2wActivity
        Bluetooth.MODEL_ER3, Bluetooth.MODEL_M12,  // Er3Activity
        Bluetooth.MODEL_LEPOD, Bluetooth.MODEL_LEPOD_PRO,  // LepodActivity
        Bluetooth.MODEL_ECN,  // EcnActivity
        Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
        Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
        Bluetooth.MODEL_LERES,  // VentilatorActivity
        Bluetooth.MODEL_FHR,  // FhrActivity
        Bluetooth.MODEL_VTM_AD5, Bluetooth.MODEL_FETAL,  // Ad5Activity
        Bluetooth.MODEL_VCOMIN,   // VcominActivity
    )

    private var list = arrayListOf<Bluetooth>()
    private var adapter = DeviceAdapter(R.layout.device_item, list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initView()
        initEventBus()
        needPermission()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        initService()
    }

    private fun needPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

    private fun initService() {
        if (BleServiceHelper.BleServiceHelper.checkService()) {
            // BleService already init
            BleServiceHelper.BleServiceHelper.startScan(models)
        } else {
            // Save the original file path. Er1, VBeat and HHM1 are currently supported
            val rawFolders = SparseArray<String>()
//            rawFolders.set(Bluetooth.MODEL_ER1, "${getExternalFilesDir(null)?.absolutePath}/er1")
//            rawFolders.set(Bluetooth.MODEL_ER1_N, "${getExternalFilesDir(null)?.absolutePath}/vbeat")
//            rawFolders.set(Bluetooth.MODEL_HHM1, "${getExternalFilesDir(null)?.absolutePath}/hhm1")

            BleServiceHelper.BleServiceHelper.initLog(true).initRawFolder(rawFolders).initService(application)
        }
    }

    private fun initView() {

        dialog = ProgressDialog(this)

        scan.setOnClickListener {
            BleServiceHelper.BleServiceHelper.startScan(models)
        }
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            rcv.layoutManager = this
        }
        rcv.adapter = adapter
        adapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as Bluetooth).let {
                // set interface before connect
                BleServiceHelper.BleServiceHelper.setInterfaces(it.model)
                // add observer(ble state)
                lifecycle.addObserver(BIOL(this, intArrayOf(it.model)))
                // stop scan before connect
                BleServiceHelper.BleServiceHelper.stopScan()
                // connect
                BleServiceHelper.BleServiceHelper.connect(applicationContext, it.model, it.device)

                deviceModel = it.model
                deviceName = it.name
                deviceAddress = it.macAddr

                if (this::dialog.isInitialized) {
                    dialog.show()
                }
                BluetoothController.clear()
                splitDevices(ble_split.text.toString())
            }
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit)
            .observe(this) {
                // BleService init success
                BleServiceHelper.BleServiceHelper.startScan(models)
                Log.d(TAG, "EventServiceConnectedAndInterfaceInit")
            }
        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this) {
                // scan result
                splitDevices(ble_split.text.toString())
                Log.d(TAG, "EventDeviceFound")
            }
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceDisconnectReason)
            .observe(this) {
                // ConnectionObserver.REASON_NOT_SUPPORTED: SDK will not auto reconnect device, services error, try to reboot device
                val reason = when (it) {
                    ConnectionObserver.REASON_UNKNOWN -> "The reason of disconnection is unknown."
                    ConnectionObserver.REASON_SUCCESS -> "The disconnection was initiated by the user."
                    ConnectionObserver.REASON_TERMINATE_LOCAL_HOST -> "The local device initiated disconnection."
                    ConnectionObserver.REASON_TERMINATE_PEER_USER -> "The remote device initiated graceful disconnection."
                    ConnectionObserver.REASON_LINK_LOSS -> "This reason will only be reported when ConnectRequest.shouldAutoConnect() was called and connection to the device was lost. Android will try to connect automatically."
                    ConnectionObserver.REASON_NOT_SUPPORTED -> "The device does not hav required services."
                    ConnectionObserver.REASON_TIMEOUT -> "The connection timed out. The device might have reboot, is out of range, turned off or doesn't respond for another reason."
                    else -> "disconnect"
                }
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                Toast.makeText(this, reason, Toast.LENGTH_SHORT).show()
            }
        //--------------------pc80b,pc102,pc60fw,pc68b,pod1w,pc300--------------------
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                // connect success
                Log.d(TAG, "EventBleDeviceReady")
                when (it) {
                    Bluetooth.MODEL_PC100 -> {
                        startActivity(Intent(this, Pc102Activity::class.java))
                    }
                    Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
                    Bluetooth.MODEL_PC80B_BLE2 -> {
                        val intent = Intent(this, Pc80bActivity::class.java)
                        intent.putExtra("model", it)
                        startActivity(intent)
                    }
                    Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC_60NW, Bluetooth.MODEL_PC_60NW_1,
                    Bluetooth.MODEL_PC66B, Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
                    Bluetooth.MODEL_OXYSMART, Bluetooth.MODEL_POD2B,
                    Bluetooth.MODEL_POD_1W, Bluetooth.MODEL_S5W,
                    Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
                    Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
                    Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
                    Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
                    Bluetooth.MODEL_S6W, Bluetooth.MODEL_S6W1,
                    Bluetooth.MODEL_PC60NW_BLE, Bluetooth.MODEL_PC60NW_WPS,
                    Bluetooth.MODEL_PC_60NW_NO_SN -> {
                        val intent = Intent(this, Pc60fwActivity::class.java)
                        intent.putExtra("model", it)
                        startActivity(intent)
                    }
                    Bluetooth.MODEL_PC_68B -> {
                        startActivity(Intent(this, Pc68bActivity::class.java))
                    }
                    Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
                    Bluetooth.MODEL_GM_300SNT, Bluetooth.MODEL_GM_300SNT_BLE,
                    Bluetooth.MODEL_CMI_PC303-> {
                        val intent = Intent(this, Pc303Activity::class.java)
                        intent.putExtra("model", it)
                        startActivity(intent)
                    }
                    Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_CHECK_ADV -> {
                        val intent = Intent(this, CheckmeMonitorActivity::class.java)
                        intent.putExtra("model", it)
                        startActivity(intent)
                    }
                    Bluetooth.MODEL_TV221U -> {
                        startActivity(Intent(this, Vtm20fActivity::class.java))
                    }
                    Bluetooth.MODEL_BIOLAND_BGM -> {
                        startActivity(Intent(this, BiolandBgmActivity::class.java))
                    }
                    Bluetooth.MODEL_POCTOR_M3102 -> {
                        startActivity(Intent(this, PoctorM3102Activity::class.java))
                    }
                    Bluetooth.MODEL_LPM311 -> {
                        startActivity(Intent(this, Lpm311Activity::class.java))
                    }
                    Bluetooth.MODEL_LEM -> {
                        startActivity(Intent(this, LemActivity::class.java))
                    }
                    Bluetooth.MODEL_ECN -> {
                        startActivity(Intent(this, EcnActivity::class.java))
                    }
                    Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
                    Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
                    Bluetooth.MODEL_LERES -> {
                        val intent = Intent(this, VentilatorActivity::class.java)
                        intent.putExtra("model", it)
                        startActivity(intent)
                    }
                    Bluetooth.MODEL_FHR -> {
                        startActivity(Intent(this, FhrActivity::class.java))
                    }
                    Bluetooth.MODEL_VTM_AD5, Bluetooth.MODEL_FETAL -> {
                        val intent = Intent(this, Ad5Activity::class.java)
                        intent.putExtra("model", it)
                        startActivity(intent)
                    }
                    Bluetooth.MODEL_VCOMIN -> {
                        startActivity(Intent(this, VcominActivity::class.java))
                    }
                    else -> {
                        Toast.makeText(this, "connect success", Toast.LENGTH_SHORT).show()
                        adapter.setList(null)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        //----------------------ap10/ap20/ap20wps---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20SetTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                val intent = Intent(this, Ap20Activity::class.java)
                intent.putExtra("model", it.model)
                startActivity(intent)
            }
        //----------------------pulsebit ex/hhm4/checkme---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitSetTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                val intent = Intent(this, PulsebitExActivity::class.java)
                intent.putExtra("model", it.model)
                startActivity(intent)
            }
        //----------------------checkme le---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeSetTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                startActivity(Intent(this, CheckmeLeActivity::class.java))
            }
        //----------------------checkme pod---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodSetTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                val intent = Intent(this, CheckmePodActivity::class.java)
                intent.putExtra("model", it.model)
                startActivity(intent)
            }
        //----------------------aoj20a---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aSetTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                startActivity(Intent(this, Aoj20aActivity::class.java))
            }
        //----------------------sp20/sp20wps---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20SetTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                val intent = Intent(this, Sp20Activity::class.java)
                intent.putExtra("model", it.model)
                startActivity(intent)
            }
        //----------------------oxy---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo)
            .observe(this) {
                val types = it.data as Array<String>
                if (types.isEmpty()) return@observe
                if (types[0] == "SetTIME") {
                    if (this::dialog.isInitialized) {
                        dialog.dismiss()
                    }
                    val intent = Intent(this, OxyActivity::class.java)
                    intent.putExtra("model", it.model)
                    startActivity(intent)
                }
            }
        //----------------------bpm---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmSyncTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                startActivity(Intent(this, BpmActivity::class.java))
            }
        //----------------------er1/vbeat/hhm1-------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                val intent = Intent(this, Er1Activity::class.java)
                intent.putExtra("model", it.model)
                startActivity(intent)
            }
        //------------------er2/lp er2/duoek/hhm2/hhm3----------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetTime)
            .observe(this) {
                BleServiceHelper.BleServiceHelper.er2GetInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Info)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                val data = it.data as com.lepu.blepro.ext.er2.DeviceInfo
                // ER2-S信心相联 定制版本
                if (data.branchCode.equals("40020000")) {
                    val intent = Intent(this, Er2SActivity::class.java)
                    intent.putExtra("model", it.model)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, Er2Activity::class.java)
                    intent.putExtra("model", it.model)
                    startActivity(intent)
                }
            }
        //------------------bp2/bp2a/bp2t----------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                val intent = Intent(this, Bp2Activity::class.java)
                intent.putExtra("model", it.model)
                startActivity(intent)
            }
        //------------------bp2w----------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSyncTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                startActivity(Intent(this, Bp2wActivity::class.java))
            }
        //------------------lp-bp2w----------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSyncUtcTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                startActivity(Intent(this, LpBp2wActivity::class.java))
            }
        //------------------er3----------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3SetTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                startActivity(Intent(this, Er3Activity::class.java))
            }
        //------------------lepod----------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodSetTime)
            .observe(this) {
                if (this::dialog.isInitialized) {
                    dialog.dismiss()
                }
                startActivity(Intent(this, LepodActivity::class.java))
            }
    }

    private fun splitDevices(name: String) {
        list.clear()
        for (b in BluetoothController.getDevices()) {
            if (b.name.contains(name, true)) {
                list.add(b)
            }
        }

        adapter.setNewInstance(list)
        adapter.notifyDataSetChanged()

    }

    override fun onBleStateChanged(model: Int, state: Int) {
        // Ble.State
        Log.d(TAG, "model $model, state: $state")
        _bleState.value = state == Ble.State.CONNECTED
        Log.d(TAG, "bleState $bleState")
    }
}