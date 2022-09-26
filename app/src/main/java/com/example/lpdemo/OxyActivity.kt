package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.oxy.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import kotlinx.android.synthetic.main.activity_oxy.*

class OxyActivity : AppCompatActivity(), BleChangeObserver {

    private val TAG = "OxyActivity"
    // Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
    // Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
    // Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
    // Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
    // Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
    // Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
    // Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
    // Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU,
    // Bluetooth.MODEL_AI_S100
    private var model = Bluetooth.MODEL_O2RING

    private var fileNames = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oxy)
        model = intent.getIntExtra("model", model)
        lifecycle.addObserver(BIOL(this, intArrayOf(model)))
        initView()
        initEventBus()
    }

    private fun initView() {
        bleState.observe(this) {
            if (it) {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                oxy_ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        }

        get_info.setOnClickListener {
            fileNames.clear()
            BleServiceHelper.BleServiceHelper.oxyGetInfo(model)
        }
        read_file.setOnClickListener {
            readFile()
        }
        get_rt_param.setOnClickListener {
            BleServiceHelper.BleServiceHelper.oxyGetRtParam(model)
        }
        factory_reset.setOnClickListener {
            BleServiceHelper.BleServiceHelper.oxyFactoryReset(model)
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                data_log.text = "$data"
                val list = data.fileList.split(",")
                for (name in list) {
                    if (name == "") continue
                    fileNames.add(name)
                }
                Toast.makeText(this, "file list size ${fileNames.size}", Toast.LENGTH_SHORT).show()
                // data.batteryState：0（no charge），1（charging），2（charging complete）
                // data.batteryValue：0%-100%
                // data.oxiThr：80-95
                // data.motor：
                // KidsO2、Oxylink（0-5：MIN，5-10：LOW，10-17：MID，17-22：HIGH，22-35：MAX）
                // O2Ring（0-20：MIN，20-40：LOW，40-60：MID，60-80：HIGH，80-100：MAX）
                // data.workMode：0（Sleep Mode），1（Minitor Mode）
                // data.oxiSwitch：0（off），1（on）
                // data.hrSwitch：0（off），1（on）
                // data.hrLowThr：30-250
                // data.hrHighThr：30-250
                // data.curState：0（preparing），1（is ready），2（measuring）
                // data.lightingMode：0-2（0：Standard Mode，1：Always Off Mode，2：Always On Mode）
                // data.lightStr：0-2
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileError)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventOxyReadFileError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                data_log.text = "进度 $data%"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileComplete)
            .observe(this) {
                val data = it.data as OxyFile
                data_log.text = "$data"
                fileNames.removeAt(0)
                readFile()
                // data.operationMode：0（Sleep Mode），1（Minitor Mode）
                // data.size：Total bytes of this data file package
                // data.asleepTime：Reserved for total asleep time future
                // data.avgSpo2：Average blood oxygen saturation
                // data.minSpo2：Minimum blood oxygen saturation
                // data.dropsTimes3Percent：drops below baseline - 3
                // data.dropsTimes4Percent：drops below baseline - 4
                // data.asleepTimePercent：T90 = (<90% duration time) / (total recording time) *100%
                // data.durationTime90Percent：Duration time when SpO2 lower than 90%
                // data.dropsTimes90Percent：Reserved for drop times when SpO2 lower than 90%
                // data.o2Score：Range: 0~100（For range 0~10, should be (O2 Score) / 10）
                // data.stepCounter：Total steps
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtParamData)
            .observe(this) {
                val data = it.data as RtParam
                tv_oxy.text = data.spo2.toString()
                tv_pr.text = data.pr.toString()
                tv_pi.text = data.pi.toString()
                data_log.text = "$data"
                // data.battery：0-100
                // data.batteryState：0（no charge），1（charging），2（charging complete）
                // data.state：0（lead off），1（lead on），other（error）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyFactoryReset)
            .observe(this) {
                val data = it.data as Boolean
                data_log.text = "EventOxyFactoryReset $data"
            }
    }

    private fun readFile() {
        if (fileNames.size == 0) return
        BleServiceHelper.BleServiceHelper.oxyReadFile(model, fileNames[0])
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