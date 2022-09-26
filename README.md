# Android SDK API

## SDK support platform

Version at least Android 7.0

## aar version

> lepu-blepro-0.0.1.aar : add PC-60FW, PC-102, PC-80B, AP-10/AP-20  
> lepu-blepro-0.0.2.aar : add POD-1W  
> lepu-blepro-0.0.3.aar : add PC-68B, PC-303, PulsebitEX, CheckmeLE  
> lepu-blepro-0.0.4.aar : add Checkme Pod, POD-2W, AOJ-20A  
> lepu-blepro-0.0.5.aar : add SP-20, Vetcorder, BPM, Bioland-BGM, PoctorM3102, LPM311, LEM

## import SDK

### permission

Add the permissions in the `AndroidManifest.xml` file :
+ \<uses-permission android:name="android.permission.BLUETOOTH" />  
+ \<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />  
+ \<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
+ \<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

### dependencies

Add the lepu-blepro-x.x.x.aar to libs directory.  
Add the dependencies for the artifacts you need in the `build.gradle` file for your app or module :
+ implementation 'no.nordicsemi.android:ble:2.2.4'
+ implementation(name: 'lepu-blepro-x.x.x', ext: 'aar')

## BleServiceObserver

Used to monitor BleService lifecycle  
`onServiceCreate()`   
`onServiceDestroy()` 

## BleChangeObserver

Used to monitor BluetoothDevice connect status  
`onBleStateChanged(model, state)` :  
`model` : One device type corresponds to one model, such as PC-60FW is Bluetooth.MODEL_PC60FW  
`state` : com.lepu.blepro.constants.Ble.State

## BleServiceHelper

+ ### initService(application, bleServiceObserver)

Init BleService in `application onCreate()` , `PS：Only need to initService once during app operation`  
SDK will send this event after BleService init finish :  
`LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit).post(true)` 

+ ### stopService(application)

+ ### startScan()

SDK will send this event when found BluetoothDevice :   
`LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound).post(bluetooth)` 

+ ### stopScan()

Stop scan before connect BluetoothDevice

+ ### setInterfaces(model)

Set interface before connect BluetoothDevice

+ ### connect(context, model, bluetoothDevice)

+ ### disconnect(autoReconnect)

Disconnect all BluetoothDevice  
autoReconnect : Whether auto connect BluetoothDevice after disconnect

+ ### disconnect(model, autoReconnect)

Disconnect model BluetoothDevice

### AOJ-20A (Bluetooth.MODEL_AOJ20A)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aSetTime).post(InterfaceEvent(model, true))`  

+ #### 1.aoj20aGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeviceData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.aoj20a.DeviceInfo  
> battery : 1-10 (10=100%)  

+ #### 2.aoj20aGetFileList(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<Record>`  com.lepu.blepro.ext.aoj20a.Record  
> temp : unit (℃)  

+ #### 3.aoj20aDeleteData(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeleteData).post(InterfaceEvent(model, true))`  

+ #### 4.Temp Result

Error result :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.aoj20a.ErrorResult  

Normal result :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.aoj20a.TempResult  
> temp : unit (℃)  

### AP-20 (Bluetooth.MODEL_AP20)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20SetTime).post(InterfaceEvent(model, true))` 

+ #### 1.ap20GetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20DeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.DeviceInfo

+ #### 2.ap20SetConfig(model, type, config)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20SetConfigResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.SetConfigResult
> type : Constant.Ap20ConfigType  
> success : true(set config success), false(set config failed)

+ #### 3.ap20GetConfig(model, type)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20GetConfigResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.GetConfigResult
> type : Constant.Ap20ConfigType  
> data : value range  
> Constant.Ap20ConfigType.BACK_LIGHT (0-5)  
> Constant.Ap20ConfigType.ALARM_SWITCH (0 : off, 1 : on)  
> PS : The alarm function is off / on, mainly including low blood oxygen alarm, high or low pulse rate alarm  
> Constant.Ap20ConfigType.LOW_OXY_THRESHOLD (85-99)  
> Constant.Ap20ConfigType.LOW_HR_THRESHOLD (30-99)  
> Constant.Ap20ConfigType.HIGH_HR_THRESHOLD (100-250)

+ #### 4.ap20GetBattery(model)
 
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20BatLevel).post(InterfaceEvent(model, data))`  
`data` : int (0-3, 0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)

+ #### 5.Real-time Oxy param data (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtOxyParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.RtOxyParam
> spo2 : 0%-100% (0 invalid)  
> pr : 0-511bpm (0 invalid)  
> pi : 0%-25.5% (0 invalid)  
> isProbeOff  
> isPulseSearching  
> battery : 0-3 (0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)

+ #### 6.Real-time Oxy waveform data (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtOxyWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.RtOxyWave

+ #### 7.Real-time nasal flow param (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.RtBreathParam
> rr : respiratory rate (6-60bpm, 0 invalid)  
> sign : 0(normal breathing), 1(no breathing)

+ #### 8.Real-time nasal flow wave (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.RtBreathWave
> flowInt : 0-4095  
> snoreInt : 0-4095

### Bioland-BGM (Bluetooth.MODEL_BIOLAND_BGM)

SDK will send this event when BluetoothDevice connected :  
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.biolandBgmGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bioland.DeviceInfo
> customerType : 0(APPLE), 1(AIAOLE), 2(HAIER), 3(NULL), 4(XIAOMI), 5(CHANNEL), 6(KANWEI)  
> battery : 0-100  
> deviceType : 1(Sphygmomanometer), 2(Blood glucose meter)

+ #### 2.biolandBgmGetGluData(model)

Get the latest data ：  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmGluData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bioland.GluData
> resultMg : unit mg/dL (18-Lo, 707-Hi)  
> resultMmol : unit mmol/L (1.0-Lo, 39.3-Hi)

If the device has no data ：  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmNoGluData).post(InterfaceEvent(model, data))`  

+ #### 3.Real-time Glu data

If the method biolandBgmGetInfo or biolandBgmGetGluData is called after the device is connected, the measurement data will be automatically sent.  

(1) Countdown :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmCountDown).post(InterfaceEvent(model, data))`  
(2) Measurement result :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmGluData).post(InterfaceEvent(model, data))`  

### BPM-188 (Bluetooth.MODEL_BPM)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmSyncTime).post(InterfaceEvent(model, true))`  

+ #### 1.bpmGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bpm.DeviceInfo

+ #### 2.bpmGetRtState(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmState).post(InterfaceEvent(model, data))`  
`data` : int
> 0 : Time setting state  
> 1 : Historical interface status  
> 2 : Measurement status  
> 3 : Measuring the pressurized state  
> 4 : The flickering indication of the heart rate in deflating mode  
> 5 : Measurement end state  
> 6 : Standby interface/time interface  

+ #### 3.bpmGetFileList(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bpm.RecordData
> irregularHrFlag : Whether the heart rate is irregular  
> storeId : Record number

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordEnd).post(InterfaceEvent(model, true))`  

+ #### 4.Real-time Bp data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRtData).post(InterfaceEvent(model, data))`  
`data` : int  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bpm.RecordData  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureErrorResult).post(InterfaceEvent(model, data))`  
`data` : int  
> 1 : Sensor vibration anomaly  
> 2 : Not enough heart rate to be detected or blood pressure value to be calculated  
> 3 : Measurement results are abnormal  
> 4 : Cuff is too loose or air leakage(Pressure value less than 30mmHg in 10 seconds)  
> 5 : The tube is blocked  
> 6 : Large pressure fluctuations during measurement  
> 7 : Pressure exceeds upper limit  
> 8 : Calibration data is abnormal or uncalibrated  

### CheckmeLE (Bluetooth.MODEL_CHECKME_LE)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeSetTime).post(InterfaceEvent(model, true))`  

+ #### 1.checkmeLeGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.checkmele.DeviceInfo

+ #### 2.checkmeLeGetFileList(model, type)

> type : Constant.CheckmeLeListType  

Get filelist progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  
Get filelist error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListError).post(InterfaceEvent(model, true))`  
Get filelist complete :  
(1) Oximeter List :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeOxyList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<OxyRecord>`  com.lepu.blepro.ext.checkmele.OxyRecord  
> timestamp : unit (s)  
> spo2 : 0%-100% (0 invalid)  
> pr : 0-255 (0 invalid)  
> pi : 0%-25.5% (0 invalid)  
> normal : true (smile face)，false (sad face)  

(2) ECG Recorder List :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeEcgList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<EcgRecord>`  com.lepu.blepro.ext.checkmele.EcgRecord  

(3) Daily Check List :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeDlcList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<DlcRecord>`  com.lepu.blepro.ext.checkmele.DlcRecord  

+ #### 3.checkmeLeReadFile(model, fileName)

sampling rate : 500HZ  
1mV = n * 0.0012820952991323    

Read file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  
Read file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileError).post(InterfaceEvent(model, true))`  
Read file complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.checkmele.EcgFile
> wFs = waveShortData * 0.0012820952991323  
> recordingTime : unit (s)  
> hr : 0 is invalid value  
> result : LeEcgDiagnosis  
> - isRegular : Whether Regular ECG Rhythm  
> - isPoorSignal : Whether Unable to analyze  
> - isHighHr : Whether High Heart Rate  
> - isLowHr : Whether Low Heart Rate  
> - isIrregular : Whether Irregular ECG Rhythm  
> - isHighQrs : Whether High QRS Value  
> - isHighSt : Whether High ST Value  
> - isLowSt : Whether Low ST Value  
> - isPrematureBeat : Whether Suspected Premature Beat  

### Vetcorder (Bluetooth.MODEL_VETCORDER)
### CheckADV (Bluetooth.MODEL_CHECK_ADV)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.Real-time data

sampling rate : 25HZ  
1mV = n * 0.0097683451362458  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeMonitor.EventCheckmeMonitorRtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.checkmemonitor.RtData
> ecgFloatData = ecgShortData * 0.0097683451362458  
> battery : 0-100  

### Checkme Pod (Bluetooth.MODEL_CHECK_POD)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodSetTime).post(InterfaceEvent(model, true))`  

+ #### 1.checkmePodGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.checkmepod.DeviceInfo

+ #### 2.checkmePodGetFileList(model)

Get filelist progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  

Get filelist error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListError).post(InterfaceEvent(model, true))`  

Get filelist complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodFileList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<Record>`  com.lepu.blepro.ext.checkmepod.Record  
> timestamp : unit (s)  
> spo2 : 0-255 (0 invalid value)  
> pr : 0-255 (0 invalid value)  
> pi : 0%-25.5% (0 invalid value)  
> temp : unit (℃)  

+ #### 3.startRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).post(model)`  

+ #### 4.stopRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).post(model)`  

+ #### 5.Real-time Data (frequency 125HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodRtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.checkmepod.RtData  
> param : RtParam  
> - pr : 0-255 (0 invalid value)  
> - spo2 : 0-255 (0 invalid value)  
> - pi : 0%-25.5% (0 invalid value)  
> - temp : unit (℃)  
> - oxyState : 0 (blood oxygen cable is not inserted), 1 (insert the blood oxygen cable but not the finger), 2 (finger inserted)  
> - tempState : 0 (temperature cable is not inserted), 1 (temperature cable is inserted)  
> - batteryState : 0 (no charge), 1 (charging), 2 (charging complete), 3 (low battery)  
> - battery : 0-100  
> - runStatus : 0 (idle), 1 (prepare), 2 (measuring)  

### LEM1 (Bluetooth.MODEL_LEM)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.lemGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.LemData
> battery : 1-100  
> heatMode : true(Heating mode on), false(Heating mode off)  
> massageMode : 0-4  
> - 0 : Vitality mode(Constant.LemMassageMode.VITALITY)  
> - 1 : Dynamic mode(Constant.LemMassageMode.DYNAMIC)  
> - 2 : Thump mode(Constant.LemMassageMode.HAMMERING)  
> - 3 : Soothing mode(Constant.LemMassageMode.SOOTHING)  
> - 4 : Automatic mode(Constant.LemMassageMode.AUTOMATIC)  
> massageLevel : 0-15  
> massageTime : 0-2  
> - 0 : 15 min(Constant.LemMassageTime.MIN_15)  
> - 1 : 10min(Constant.LemMassageTime.MIN_10)  
> - 2 : 5min(Constant.LemMassageTime.MIN_5)

+ #### 2.lemGetBattery(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemBattery).post(InterfaceEvent(model, data))`  
`data` : int (1-100)

+ #### 3.lemHeatMode(model, on)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetHeatMode).post(InterfaceEvent(model, data))`  
`data` : boolean (true：Heating mode on, false：Heating mode off)

+ #### 4.lemMassageTime(model, time)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageTime).post(InterfaceEvent(model, data))`  
`data` : int (0-2)

+ #### 5.lemMassageMode(model, mode)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageMode).post(InterfaceEvent(model, data))`  
`data` : int (0-4)

+ #### 6.lemMassageLevel(model, level)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageLevel).post(InterfaceEvent(model, data))`  
`data` : int (0-15)

### LPM311 (Bluetooth.MODEL_LPM311)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.lpm311GetData(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LPM311.EventLpm311Data).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.Lpm311Data
> unit : 0(mmol/L), 1(mg/dL)

### PC-60FW (Bluetooth.MODEL_PC60FW)
### PC-66B (Bluetooth.MODEL_PC66B)
### OxySmart (Bluetooth.MODEL_OXYSMART)
### POD-1 (Bluetooth.MODEL_POD_1W)
### POD-2B (Bluetooth.MODEL_POD2B)
### PC-60NW-1 (Bluetooth.MODEL_PC_60NW_1)
### PC-60NW (Bluetooth.MODEL_PC_60NW)
### PF-10 (Bluetooth.MODEL_PF_10)
### PF-10AW (Bluetooth.MODEL_PF_10AW)
### PF-10AW1 (Bluetooth.MODEL_PF_10AW1)
### PF-10BW (Bluetooth.MODEL_PF_10BW)
### PF-10BW1 (Bluetooth.MODEL_PF_10BW1)
### PF-20 (Bluetooth.MODEL_PF_20)
### PF-20B (Bluetooth.MODEL_PF_20B)
### PF-20AW (Bluetooth.MODEL_PF_20AW)
### S5W (Bluetooth.MODEL_S5W)
### S6W (Bluetooth.MODEL_S6W)
### S6W1 (Bluetooth.MODEL_S6W1)
### S7W (Bluetooth.MODEL_S7W)
### S7BW (Bluetooth.MODEL_S7BW)

SDK will send this event when BluetoothDevice connected :  
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.pc60fwGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo).post(InterfaceEvent(model, data))`  
`data` ：com.lepu.blepro.ext.pc60fw.DeviceInfo

+ #### 2.Real-time Oxy param data (BluetoothDevice auto send data, frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc60fw.RtParam
> spo2 : 0%-100% (0 invalid)  
> pr : 0-511bpm (0 invalid)  
> pi : 0%-25.5% (0 invalid)  
> isProbeOff  
> isPulseSearching

+ #### 3.Real-time Oxy waveform data (BluetoothDevice auto send data, frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtWave).post(InterfaceEvent(model, data))`   
`data` : com.lepu.blepro.ext.pc60fw.RtWave

+ #### 4.Battery

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwBatLevel).post(InterfaceEvent(model, data))`  
`data` : int (0-3, 0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)

+ #### 5.Working status

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwWorkingStatus).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc60fw.WorkingStatus  
<table>
	<tr>
	    <th>Mode</th>
	    <th>Step</th>
	    <th>Para1</th>  
     <th>Para2</th>  
	</tr>
	<tr>
	    <td rowspan="6">0x01 Spot Check</td>
	    <td>0x00 idle</td>
     <td>--</td>
     <td>--</td>
	</tr>
	<tr>
	    <td>0x01 Preparing</td>
     <td>--</td>
     <td>--</td>
	</tr>
	<tr>
	    <td>0x02 Measuring</td>
     <td>30s~0s</td>
     <td>--</td>
	</tr>
	<tr>
	    <td>0x03 Result</td>
     <td>Spo2</td>
     <td>Pr</td>
	</tr>
	<tr>
     <td>0x04 Analysis Result</td>
	    <td>Result Code</td>
     <td>--</td>
	</tr>
	<tr>
	    <td>0x05 Finish</td>
     <td>--</td>
     <td>--</td>
	</tr>
	<tr>
	    <td>0x02 Continuous</td>
	    <td>--</td>
	    <td>--</td>
     <td>--</td>
	</tr>
	<tr>
	    <td>0x03 Menu</td>
	    <td>--</td>
	    <td>--</td>
     <td>--</td>
	</tr>
</table>
<table>
	<tr>
	    <th>Result Code</th>
	    <th>Result</th>
	</tr>
	<tr>
	    <td>0x00</td>
	    <td>No irregularity found</td>
	</tr>
	<tr>
	    <td>0x01</td>
	    <td>Suspected a little fast pulse</td>
	</tr>
 <tr>
	    <td>0x02</td>
	    <td>Suspected fast pulse</td>
	</tr>
 <tr>
	    <td>0x03</td>
	    <td>Suspected short run of fast pulse</td>
	</tr>
 <tr>
	    <td>0x04</td>
	    <td>Suspected a little slow pulse</td>
	</tr>
 <tr>
	    <td>0x05</td>
	    <td>Suspected slow pulse</td>
	</tr>
 <tr>
	    <td>0x06</td>
	    <td>Suspected occasional short pulse interval</td>
	</tr>
 <tr>
	    <td>0x07</td>
	    <td>Suspected irregular pulse interval</td>
	</tr>
 <tr>
	    <td>0x08</td>
	    <td>Suspected fast pulse with short pulse interval</td>
	</tr>
 <tr>
	    <td>0x09</td>
	    <td>Suspected slow pulse with short pulse interval</td>
	</tr>
 <tr>
	    <td>0x0A</td>
	    <td>Suspected slow pulse with irregular pulse interval</td>
	</tr>
 <tr>
	    <td>0xFF</td>
	    <td>Poor signal. Measure again</td>
	</tr>
</table>

### PC-68B (Bluetooth.MODEL_PC68B)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)`  

+ #### 1.pc68bGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc68b.DeviceInfo

+ #### 2.pc68bEnableRtData(model, type, enable)

if you can not receive real-time data, use this method to enable.  
> type : Constant.Pc68bEnableType  
> enable : true(receive real-time data), false(do not receive real-time data)

+ #### 3.Real-time Oxy param data (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bRtParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc68b.RtParam
> spo2 : 0%-100% (0 invalid)  
> pr : 0-511bpm (0 invalid)  
> pi : 0%-25.5% (0 invalid)  
> isProbeOff  
> isPulseSearching  
> isCheckProbe  
> vol : 0-3.2V  
> batLevel : 0-3 (0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)

+ #### 4.Real-time Oxy waveform data (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bRtWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc68b.RtWave

### PC80B (Bluetooth.MODEL_PC80B)
### PC80B-BLE (Bluetooth.MODEL_PC80B_BLE)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.pc80bGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc80b.DeviceInfo

+ #### 2.pc80bGetBattery(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bBatLevel).post(InterfaceEvent(model, data))`  
`data` : int (0-3, 0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)

+ #### 3.Real-time ECG data (frequency 150HZ)

sampling rate：150HZ  
1mV = (n - 2048) * (1 / 330))  

Fast mode (30 s) :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bFastData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc80b.RtFastData  
> seqNo  
> gain : device waveform amplitude coefficient  
> channel : 0(detecting channel), 1(internal channel) 2(external channel)  
> measureMode : 0(detecting mode), 1(Fast mode, 30s), 2(Continuous mode)  
> measureStage : 0(detecting stage), 1(preparing), 2(measuring), 3(analyzing), 4(result), 5(stop)  
> leadOff  
> dataType : 1(ECG data), 2(ECG result)  
> ecgData : ECG data  
> - ecgFloats = (ecgInts - 2048) * (1 / 330)  
> ecgResult : ECG result

Continuous mode :  
(1) In the preparation stage, you will receive EventPc80bFastData event :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bFastData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc80b.RtFastData  
> measureStage : 1(preparing), 5(stop, sdk stop sending EventPc80bFastData event, then start to send EventPc80bContinuousData event)
(2) In the formal measurement stage, you will receive EventPc80bContinuousData event :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bContinuousData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc80b.RtContinuousData  
> seqNo  
> hr : 0-255 (0 invalid)  
> gain : device waveform amplitude coefficient  
> leadOff  
> vol : unit (V)  
> ecgData : ECG data  
> - ecgFloats = (ecgInts - 2048) * (1 / 330)  

(3) Exit continuous measurement :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bContinuousDataEnd).post(InterfaceEvent(model, true))`  

+ #### 4.Receive record data（From BluetoothDevice）

read file progress ：  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int（0-100）  
read file error ：  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileError).post(InterfaceEvent(model, true))`  
read file complete ：  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc80b.EcgFile  
> ecgFloats = (ecgInts - 2048) * (1 / 330)  

### PC-100 (Bluetooth.MODEL_PC100)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.pc100GetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100DeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.DeviceInfo
> batLevel : 0-3 (0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)  
> batStatus : 0 (No charge), 1 (Charging), 2 (Charging complete)

+ #### 2.pc100StartBp(model)
  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpStart).post(InterfaceEvent(model, true))` 

+ #### 3.pc100StopBp(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpStop).post(InterfaceEvent(model, true))` 

+ #### 4.Real-time Bp measure result

Normal result :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.BpResult
> sys : systolic pressure  
> pr : pulse rate  
> dia : diastolic pressure  
> map : average pressure  
> result : 0(hr normal), 1(hr irregular)  
> resultMess : result description

Error result :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.BpResultError

+ #### 5.Real-time Bp data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtBpData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.RtBpData
> sign : heart rate signal (0:no hr, 1:has hr)  
> ps : current pressure

+ #### 6.Real-time Oxy param data (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.RtOxyParam  
> spo2 : 0%-100% (0 invalid)  
> pr : 0-511bpm (0 invalid)  
> pi : 0%-25.5% (0 invalid)  
> isDetecting  
> isScanning

+ #### 7.Real-time Oxy waveform data (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.RtOxyWave  

### PC_300SNT (Bluetooth.MODEL_PC303)
### PC_300SNT-BLE (Bluetooth.MODEL_PC300_BLE)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)`  

+ #### 1.pc300GetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300DeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc303.DeviceInfo
> batLevel : 0-3 (0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)

+ #### 2.Real-time Ecg waveform data (frequency 150HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtEcgWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc303.RtEcgWave
> seqNo : 0-255 (0 is preparing, about 10 s, then 1,2,3... is measuring)  
> digit : 0, 1mV = n * (1 / 28.5) (ecgFloats = ecgInts * (1 / 28.5))  
> digit : 1, 1mV = n * (1 / 394) (ecgFloats = ecgInts * (1 / 394))

+ #### 3.Real-time Ecg result

You can receive ecg result after about 30s of measurement :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc303.EcgResult  
> hr : 0-255  
> result : 0-15, 255 is poor signal  
> resultMess : result description

+ #### 4.Real-time Bp pressure data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtBpData).post(InterfaceEvent(model, data))`  
`data` : int

+ #### 5.Real-time Bp measure result

Normal result :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc303.BpResult
> sys : systolic pressure  
> pr : pulse rate  
> dia : diastolic pressure  
> map : average pressure  
> result : 0 (hr normal), 1 (hr irregular)  
> resultMess : result description

Error result :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpErrorResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc303.BpResultError

+ #### 6.Real-time Oxy param data (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc303.RtOxyParam  
> spo2 : 0%-100% (0 invalid)  
> pr : 0-511bpm (0 invalid)  
> pi : 0%-25.5% (0 invalid)  
> isProbeOff  
> isPulseSearching

+ #### 7.Real-time Oxy waveform data (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc303.RtOxyWave

+ #### 8.GLU Result

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300GluResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc303.GluResult  
> unit : 0 (mmol/L), 1 (mg/dL)  
> data : blood glucose value  
> result : 0 (normal), 1 (low), 2 (high)  
> resultMess : result description

+ #### 9.Temp Result

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300TempResult).post(InterfaceEvent(model, data))`  
`data` : 30.00-43.00 ℃, normal range is 32.00-43.00 ℃

### PoctorM3102 (Bluetooth.MODEL_POCTOR_M3102)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)`  

+ #### 1.Real-time data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PoctorM3102.EventPoctorM3102Data).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.PoctorM3102Data
> type : 0(Glucose), 1(Uric Acid), 3(Ketone)  
> result : normal=true, Glucose and Ketone(result/10, unit:mmol/L), Uric Acid(unit:umol/L)  
> result : normal=false, 0(Lo), 1(Hi)


### Pulsebit (Bluetooth.MODEL_PULSEBITEX)
### HHM4 (Bluetooth.MODEL_HHM4)
### Checkme (Bluetooth.MODEL_CHECKME)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitSetTime).post(InterfaceEvent(model, true))`  

+ #### 1.pulsebitExGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pulsebit.DeviceInfo

+ #### 2.pulsebitExGetFileList(model)

Get filelist progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  
Get filelist error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListError).post(InterfaceEvent(model, true))`  
Get filelist complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<String>` (fileNames)

+ #### 3.pulsebitExReadFile(model, fileName)

sampling rate : 500HZ  
1mV = n * 0.0012820952991323

Read file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  
Read file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileError).post(InterfaceEvent(model, true))`  
Read file complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pulsebit.EcgFile  
> wFs = waveShortData * 0.0012820952991323  
> user : 0-2 (0：single mode user, 1：dual mode userA, 2：dual mode userB)  
> recordingTime : unit (s)  
> hr : 0 is invalid value  
> result : ExEcgDiagnosis  
> - isRegular : Whether Regular ECG Rhythm  
> - isPoorSignal : Whether Unable to analyze  
> - isFastHr : Whether Fast Heart Rate  
> - isSlowHr : Whether Slow Heart Rate  
> - isIrregular : Whether Irregular ECG Rhythm  
> - isPvcs : Whether Possible ventricular premature beats  
> - isHeartPause : Whether Possible heart pause  
> - isFibrillation : Whether Possible Atrial fibrillation  
> - isWideQrs : Whether Wide QRS duration  
> - isProlongedQtc : Whether QTc is prolonged  
> - isShortQtc : Whether QTc is short  
> - isStElevation : Whether ST segment elevation  
> - isStDepression : Whether ST segment depression  

### SP-20 (Bluetooth.MODEL_SP20)
### SP-20-BLE (Bluetooth.MODEL_SP20_BLE)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20SetTime).post(InterfaceEvent(model, true))`  

+ #### 1.sp20GetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20DeviceInfo).post(InterfaceEvent(model, true))`  
`data` : com.lepu.blepro.ext.sp20.DeviceInfo

+ #### 2.sp20GetBattery(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20Battery).post(InterfaceEvent(model, true))`  
`data` : int (0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)

+ #### 3.sp20SetConfig(model, type, config)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20SetConfig).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.sp20.SetConfigResult
> type : Constant.Sp20ConfigType  
> success : true(set config success), false(set config failed)

+ #### 4.sp20GetConfig(model, type)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20GetConfig).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.sp20.GetConfigResult
> type : Constant.Sp20ConfigType  
> data : value range  
> Constant.Sp20ConfigType.BACK_LIGHT (0-5)  
> Constant.Sp20ConfigType.ALARM_SWITCH (0 : off, 1 : on)  
> PS : The alarm function is off / on, mainly including low blood oxygen alarm, high or low pulse rate alarm  
> Constant.Sp20ConfigType.LOW_OXY_THRESHOLD (85-99)  
> Constant.Sp20ConfigType.LOW_HR_THRESHOLD (30-99)  
> Constant.Sp20ConfigType.HIGH_HR_THRESHOLD (100-250)

+ #### 5.Real-time Oxy param data (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20RtParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.sp20.RtParam
> spo2 ： 0%-100% (0 invalid)  
> pr ： 0-511bpm (0 invalid)  
> pi ： 0%-25.5% (0 invalid)  
> battery : 0-3 (0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)

+ #### 6.Real-time Oxy waveform data (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20RtWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.sp20.RtWave

+ #### 7.Real-time Temperature data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20TempData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.sp20.TempResult
> result : 0(normal), 1(low), 2(high)  
> unit : 0(℃), 1(℉)

### VTM 20F (Bluetooth.MODEL_TV221U)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)`  

+ #### 1.Real-time Oxy param data (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM20f.EventVTM20fRtParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.vtm20f.RtParam
> spo2 ： 0%-100% (0 invalid)  
> pr ： 0-511bpm (0 invalid)  
> pi ： 0%-25.5% (0 invalid)  

+ #### 2.Real-time Oxy waveform data (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM20f.EventVTM20fRtWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.vtm20f.RtWave



