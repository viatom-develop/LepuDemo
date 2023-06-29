# Android SDK API

## SDK support platform

Version at least Android 7.0

## aar version

> lepu-blepro-0.0.1.aar : add PC-60FW, PC-102, PC-80B, AP-10/AP-20  
> lepu-blepro-0.0.2.aar : add POD-1W  
> lepu-blepro-0.0.3.aar : add PC-68B, PC-303, PulsebitEX, CheckmeLE  
> lepu-blepro-0.0.4.aar : add Checkme Pod, POD-2W, AOJ-20A  
> lepu-blepro-0.0.5.aar : add SP-20, Vetcorder, BPM, Bioland-BGM, PoctorM3102, LPM311, LEM  
> lepu-blepro-0.0.8.aar : add ER1, ER2, DuoEK, VBeat, O2, BP2, BP2W, LP-BP2W  
> lepu-blepro-0.0.10.aar : add Ventilator  
> lepu-blepro-1.0.1.aar : add AD5, FHR, VCOIMN

## import SDK

### dependencies
 
Add the dependencies for the artifacts you need in the `build.gradle` file for your app or module :
> implementation 'no.nordicsemi.android:ble:2.2.4'  
> implementation(name: 'lepu-blepro-x.x.x', ext: 'aar')  

### permission

Add the permissions in the `AndroidManifest.xml` file :
+ \<uses-permission android:name="android.permission.BLUETOOTH" />  
+ \<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />  
+ \<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
+ \<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

## BleChangeObserver

Used to monitor BluetoothDevice connect status  
`onBleStateChanged(model, state)` :  
`model` : One device type corresponds to one model, such as PC-60FW is Bluetooth.MODEL_PC60FW  
`state` : com.lepu.blepro.constants.Ble.State

## BleServiceHelper

+ ### initService(application)

Init BleService in `application onCreate()` , `PS：Only need to initService once during app operation`  
SDK will send this event after BleService init finish :  
`LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit).post(true)` 

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
`data` : `ArrayList<Record>`, com.lepu.blepro.ext.aoj20a.Record  
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
### AP-20-WPS (Bluetooth.MODEL_AP20_WPS)

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


### BP2 (Bluetooth.MODEL_BP2)
### BP2A (Bluetooth.MODEL_BP2A)
### BP2T (Bluetooth.MODEL_BP2T)

SDK will send this event when BluetoothDevice connected :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime).post(InterfaceEvent(model, true))`  

+ #### 1.bp2GetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Info).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bp2.DeviceInfo  

+ #### 2.bp2GetFileList(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<String>`  

+ #### 3.bp2ReadFile(model, filename)

Read file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  

Read file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError).post(InterfaceEvent(model, filename))`  
`data` : String  

Read file complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bp2.Bp2File  
> type: 1 (BpFile), 2 (EcgFile)  
> BpFile :  
> measureTime : unit (s)  
> EcgFile :  
> sampling rate : 125HZ  
> mV = waveShortData * 0.003098  
> measureTime : unit (s)  
> recordingTime : unit (s)  
> connectCable : Whether the cable is connected  
> diagnosis : EcgDiagnosis  
> - isRegular : Whether Regular ECG Rhythm  
> - isPoorSignal : Whether Unable to analyze  
> - isLeadOff : Whether Always lead off  
> - isFastHr : Whether Fast Heart Rate  
> - isSlowHr : Whether Slow Heart Rate  
> - isIrregular : Whether Irregular ECG Rhythm  
> - isPvcs : Whether Possible ventricular premature beats  
> - isHeartPause : Whether Possible heart pause  
> - isFibrillation : Whether Possible Atrial fibrillation  
> - isWideQrs : Whether Wide QRS duration  
> - isProlongedQtc : Whether QTc is prolonged  
> - isShortQtc : Whether QTc is short  

+ #### 4.startRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).post(model)`  

+ #### 5.stopRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).post(model)`  

+ #### 6.Real-time Data

sampling rate : 250HZ  
mV = n * 0.003098  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bp2.RtData  
> status : RtStatus  
> - deviceStatus : 0 (STATUS_SLEEP), 1 (STATUS_MEMERY), 2 (STATUS_CHARGE), 3 (STATUS_READY), 4 (STATUS_BP_MEASURING), 5 (STATUS_BP_MEASURE_END), 6 (STATUS_ECG_MEASURING), 7 (STATUS_ECG_MEASURE_END), 20 (STATUS_VEN)  
> - batteryStatus : 0 (no charge), 1 (charging), 2 (charging complete), 3 (low battery)  
> - percent : 0-100  

> param : RtParam  
> - paramDataType : 0 (paramData : RtBpIng), 1 (paramData : RtBpResult), 2 (paramData : RtEcgIng), 3 (paramData : RtEcgResult)  
> - ecgFloats = ecgShorts * 0.003098  

+ #### 7.bp2GetConfig(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfig).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bp2.Bp2Config  
> soundOn : Heartbeat sound switch  

+ #### 8.bp2SetConfig(model, config)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetConfig).post(InterfaceEvent(model, data))`  
`data` : boolean (true : set config success, false : set config failed)  

+ #### 9.bp2FactoryReset(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset).post(InterfaceEvent(model, data))`  
`data` : boolean (true : factory reset success, false : factory reset failed)  


### BP2W (Bluetooth.MODEL_BP2W)

SDK will send this event when BluetoothDevice connected :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSyncTime).post(InterfaceEvent(model, true))`  

+ #### 1.bp2wGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bp2w.DeviceInfo  

+ #### 2.bp2wGetFileList(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFileList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<String>`  

+ #### 3.bp2wReadFile(model, filename)

Read file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  

Read file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileError).post(InterfaceEvent(model, filename))`  
`data` : String  

Read file complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bp2w.Bp2wFile  
> type: 1 (BpFile), 2 (EcgFile)  
> BpFile :  
> measureTime : unit (s)  
> measureMode : 0 (x1), 1(x3)  
> EcgFile :  
> sampling rate : 125HZ  
> mV = waveShortData * 0.003098  
> measureTime : unit (s)  
> recordingTime : unit (s)  
> connectCable : Whether the cable is connected  
> diagnosis : EcgDiagnosis  
> - isRegular : Whether Regular ECG Rhythm  
> - isPoorSignal : Whether Unable to analyze  
> - isLeadOff : Whether Always lead off  
> - isFastHr : Whether Fast Heart Rate  
> - isSlowHr : Whether Slow Heart Rate  
> - isIrregular : Whether Irregular ECG Rhythm  
> - isPvcs : Whether Possible ventricular premature beats  
> - isHeartPause : Whether Possible heart pause  
> - isFibrillation : Whether Possible Atrial fibrillation  
> - isWideQrs : Whether Wide QRS duration  
> - isProlongedQtc : Whether QTc is prolonged  
> - isShortQtc : Whether QTc is short  

+ #### 4.startRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).post(model)`  

+ #### 5.stopRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).post(model)`  

+ #### 6.Real-time Data

sampling rate : 250HZ  
mV = n * 0.003098  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bp2w.RtData  
> status : RtStatus  
> - deviceStatus : 0 (STATUS_SLEEP), 1 (STATUS_MEMERY), 2 (STATUS_CHARGE), 3 (STATUS_READY), 4 (STATUS_BP_MEASURING), 5 (STATUS_BP_MEASURE_END), 6 (STATUS_ECG_MEASURING), 7 (STATUS_ECG_MEASURE_END), 15 (STATUS_BP_AVG_MEASURE), 16 (STATUS_BP_AVG_MEASURE_WAIT), 17 (STATUS_BP_AVG_MEASURE_END), 20 (STATUS_VEN)  
> - batteryStatus : 0 (no charge), 1 (charging), 2 (charging complete), 3 (low battery)  
> - percent : 0-100  
> - avgCnt : Valid in bp avg measure x3, measure number index(0, 1, 2)  
> - avgWaitTick : Valid in bp avg measure x3, measure interval wait tick  

> param : RtParam  
> - paramDataType : 0 (paramData : RtBpIng), 1 (paramData : RtBpResult), 2 (paramData : RtEcgIng), 3 (paramData : RtEcgResult)  
> - ecgFloats = ecgShorts * 0.003098  

+ #### 7.bp2wGetConfig(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetConfig).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.bp2w.Bp2wConfig  
> soundOn : Heartbeat sound switch  
> avgMeasureMode : 0 (bp measure x3 off), 1 (bp measure x3 on, interval 30s), 2 (bp measure x3 on, interval 60s), 3 (bp measure x3 on, interval 90s), 4 (bp measure x3 on, interval 120s)  

+ #### 8.bp2wSetConfig(model, config)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetConfig).post(InterfaceEvent(model, data))`  
`data` : boolean (true : set config success, false : set config failed)  

+ #### 9.bp2wFactoryReset(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFactoryReset).post(InterfaceEvent(model, data))`  
`data` : boolean (true : factory reset success, false : factory reset failed)  


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
`data` : `ArrayList<OxyRecord>`, com.lepu.blepro.ext.checkmele.OxyRecord  
> timestamp : unit (s)  
> spo2 : 0%-100% (0 invalid)  
> pr : 0-255 (0 invalid)  
> pi : 0%-25.5% (0 invalid)  
> normal : true (smile face)，false (sad face)  

(2) ECG Recorder List :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeEcgList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<EcgRecord>`, com.lepu.blepro.ext.checkmele.EcgRecord  

(3) Daily Check List :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeDlcList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<DlcRecord>`, com.lepu.blepro.ext.checkmele.DlcRecord  

+ #### 3.checkmeLeReadFile(model, fileName)

sampling rate : 500HZ  
mV = n * 0.0012820952991323    

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
mV = n * 0.0097683451362458  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeMonitor.EventCheckmeMonitorRtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.checkmemonitor.RtData
> ecgFloatData = ecgShortData * 0.0097683451362458  
> battery : 0-100  


### Checkme Pod (Bluetooth.MODEL_CHECK_POD)
### Checkme Pod-WPS (Bluetooth.MODEL_CHECKME_POD_WPS)

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
`data` : `ArrayList<Record>`, com.lepu.blepro.ext.checkmepod.Record  
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


### ER1 (Bluetooth.MODEL_ER1)
### VBeat (Bluetooth.MODEL_ER1_N)
### HHM1 (Bluetooth.MODEL_HHM1)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetTime).post(InterfaceEvent(model, true))`  

+ #### 1.er1GetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Info).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.er1.DeviceInfo

+ #### 2.er1GetFileList(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1FileList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<String>`  

+ #### 3.er1ReadFile(model, filename)

Read file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  

Read file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileError).post(InterfaceEvent(model, true))`  

Read file complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.er1.Er1File  

+ #### 4.startRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).post(model)`  

+ #### 5.stopRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).post(model)`  

+ #### 6.Real-time Data

sampling rate : 125HZ  
mV = n * 0.002467  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1RtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.er1.RtData  
> param : RtParam  
> - batteryState : 0 (no charge), 1 (charging), 2 (charging complete), 3 (low battery)  
> - battery : 0-100  
> - recordTime : unit (s)  
> - curStatus：0 (idle), 1 (preparing), 2 (measuring), 3 (saving file), 4 (saving succeed), 5 (less than 30s, file not saved), 6 (6 retests), 7 (lead off)  

> wave : RtWave  
> - ecgFloats = ecgShorts * 0.002467  

+ #### 7.er1GetConfig(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1GetConfig).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.er1.Er1Config  
> vibration : vibration warning switch, true (on), false (off)  
> threshold1 : vibrate every ten seconds above the threshold  
> threshold2 : vibrate every two seconds above the threshold  

+ #### 8.er1SetConfig(model, config)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetConfig).post(InterfaceEvent(model, data))`  
`data` : boolean (true : set config success, false : set config failed)  

+ #### 9.er1FactoryReset(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactory).post(InterfaceEvent(model, data))`  
`data` : boolean (true : factory reset success, false : factory reset failed)  


### ER2 (Bluetooth.MODEL_ER2)
### LP ER2 (Bluetooth.MODEL_LP_ER2)
### DuoEK (Bluetooth.MODEL_DUOEK)
### HHM2 (Bluetooth.MODEL_HHM2)
### HHM3 (Bluetooth.MODEL_HHM3)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetTime).post(InterfaceEvent(model, true))`  

+ #### 1.er2GetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Info).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.er2.DeviceInfo

+ #### 2.er2GetFileList(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<String>`  

+ #### 3.er2ReadFile(model, filename)

Read file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  

Read file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileError).post(InterfaceEvent(model, true))`  

Read file complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.er2.Er2File  

+ #### 4.startRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).post(model)`  

+ #### 5.stopRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).post(model)`  

+ #### 6.Real-time Data

sampling rate : 125HZ  
mV = n * 0.002467  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2RtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.er2.RtData  
> param : RtParam  
> - batteryState : 0 (no charge), 1 (charging), 2 (charging complete), 3 (low battery)  
> - battery : 0-100  
> - recordTime : unit (s)  
> - curStatus：0 (idle), 1 (preparing), 2 (measuring), 3 (saving file), 4 (saving succeed), 5 (less than 30s, file not saved), 6 (6 retests), 7 (lead off)  

> wave : RtWave  
> - ecgFloats = ecgShorts * 0.002467  

+ #### 7.er2GetConfig(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2GetConfig).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.er2.Er2Config  
> soundOn : sound reminder switch, true (on), false (off)  

+ #### 8.er2SetConfig(model, config)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetConfig).post(InterfaceEvent(model, data))`  
`data` : boolean (true : set config success, false : set config failed)  

+ #### 9.er2FactoryReset(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryReset).post(InterfaceEvent(model, data))`  
`data` : boolean (true : factory reset success, false : factory reset failed)  


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


### LP-BP2W (Bluetooth.MODEL_LP_BP2W)

SDK will send this event when BluetoothDevice connected :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSyncUtcTime).post(InterfaceEvent(model, true))`  

+ #### 1.lpBp2wGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.lpbp2w.DeviceInfo  

+ #### 2.lpBp2wGetFileList(model, filetype)

filetype : Constant.LpBp2wListType.USER_TYPE  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wUserFileList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<UserInfo>`, com.lepu.blepro.ext.lpbp2w.UserInfo  
> aid : account id  
> uid : user id  
> birthday : "year-month-day"  
> gender : 0 (boy), 1 (girl)  

filetype : Constant.LpBp2wListType.BP_TYPE  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wBpFileList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<BpRecord>`, com.lepu.blepro.ext.lpbp2w.BpRecord  
> startTime : unit (s)  
> measureMode : 0 (x1), 1 (x3)  

filetype : Constant.LpBp2wListType.ECG_TYPE  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wEcgFileList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<EcgRecord>`, com.lepu.blepro.ext.lpbp2w.EcgRecord  
> startIime : unit (s)  
> recordingTime : unit (s)  
> diagnosis : EcgDiagnosis  
> - isRegular : Whether Regular ECG Rhythm  
> - isPoorSignal : Whether Unable to analyze  
> - isLeadOff : Whether Always lead off  
> - isFastHr : Whether Fast Heart Rate  
> - isSlowHr : Whether Slow Heart Rate  
> - isIrregular : Whether Irregular ECG Rhythm  
> - isPvcs : Whether Possible ventricular premature beats  
> - isHeartPause : Whether Possible heart pause  
> - isFibrillation : Whether Possible Atrial fibrillation  
> - isWideQrs : Whether Wide QRS duration  
> - isProlongedQtc : Whether QTc is prolonged  
> - isShortQtc : Whether QTc is short  

+ #### 3.lpBp2wReadFile(model, filename)

Read file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  

Read file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReadFileError).post(InterfaceEvent(model, filename))`  
`data` : String  

Read file complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.lpbp2w.EcgFile  
> sampling rate : 125HZ  
> mV = waveShortData * 0.003098  
> startTime : unit (s)  
> duration : unit (s)  

+ #### 4.startRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).post(model)`  

+ #### 5.stopRtTask(model)

`LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).post(model)`  

+ #### 6.Real-time Data

sampling rate : 250HZ  
mV = n * 0.003098  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wRtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.lpbp2w.RtData  
> status : RtStatus  
> - deviceStatus : 0 (STATUS_SLEEP), 1 (STATUS_MEMERY), 2 (STATUS_CHARGE), 3 (STATUS_READY), 4 (STATUS_BP_MEASURING), 5 (STATUS_BP_MEASURE_END), 6 (STATUS_ECG_MEASURING), 7 (STATUS_ECG_MEASURE_END), 15 (STATUS_BP_AVG_MEASURE), 16 (STATUS_BP_AVG_MEASURE_WAIT), 17 (STATUS_BP_AVG_MEASURE_END), 20 (STATUS_VEN)  
> - batteryStatus : 0 (no charge), 1 (charging), 2 (charging complete), 3 (low battery)  
> - percent : 0-100  
> - avgCnt : Valid in bp avg measure x3, measure number index(0, 1, 2)  
> - avgWaitTick : Valid in bp avg measure x3, measure interval wait tick  

> param : RtParam  
> - paramDataType : 0 (paramData : RtBpIng), 1 (paramData : RtBpResult), 2 (paramData : RtEcgIng), 3 (paramData : RtEcgResult)  
> - ecgFloats = ecgShorts * 0.003098  

+ #### 7.lpBp2wGetConfig(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wGetConfig).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.lpbp2w.LpBp2wConfig  
> soundOn : Heartbeat sound switch  
> avgMeasureMode : 0 (bp measure x3 off), 1 (bp measure x3 on, interval 30s), 2 (bp measure x3 on, interval 60s), 3 (bp measure x3 on, interval 90s), 4 (bp measure x3 on, interval 120s)  
> volume : Voice announcement volume, 0(off), 1, 2, 3  

+ #### 8.lpBp2wSetConfig(model, config)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSetConfig).post(InterfaceEvent(model, data))`  
`data` : boolean (true : set config success, false : set config failed)  

+ #### 9.lpBp2wFactoryReset(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wFactoryReset).post(InterfaceEvent(model, data))`  
`data` : boolean (true : factory reset success, false : factory reset failed)  

+ #### 10.lpBp2WriteUserList(model, userlist)

UserInfo.Icon = BitmapConvertor(context).createIcon("username")  

Write file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WritingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  

Write file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WriteFileError).post(InterfaceEvent(model, filename))`  
`data` : String  

Write file complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WriteFileComplete).post(InterfaceEvent(model, true))`  


### LPM311 (Bluetooth.MODEL_LPM311)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.lpm311GetData(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LPM311.EventLpm311Data).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.Lpm311Data
> unit : 0(mmol/L), 1(mg/dL)


### O2Ring (Bluetooth.MODEL_O2RING)
### O2M (Bluetooth.MODEL_O2M)
### BabyO2 (Bluetooth.MODEL_BABYO2)
### BabyO2N (Bluetooth.MODEL_BABYO2N)
### O2 (Bluetooth.MODEL_CHECKO2)
### SleepO2 (Bluetooth.MODEL_SLEEPO2)
### O2BAND (Bluetooth.MODEL_SNOREO2)
### WearO2 (Bluetooth.MODEL_WEARO2)
### SleepU (Bluetooth.MODEL_SLEEPU)
### Oxylink (Bluetooth.MODEL_OXYLINK)
### KidsO2 (Bluetooth.MODEL_KIDSO2)
### Oxyfit (Bluetooth.MODEL_OXYFIT)
### OxyRing (Bluetooth.MODEL_OXYRING)
### BBSM S1 (Bluetooth.MODEL_BBSM_S1)
### BBSM S2 (Bluetooth.MODEL_BBSM_S2)
### OxyU (Bluetooth.MODEL_OXYU)
### AI S100 (Bluetooth.MODEL_AI_S100)
### O2M-WPS (Bluetooth.MODEL_O2M_WPS)
### CMRing (Bluetooth.MODEL_CMRING)
### Oxyfit-WPS (Bluetooth.MODEL_OXYFIT_WPS)
### KidsO2-WPS (Bluetooth.MODEL_KIDSO2_WPS)
### SI PO6 (Bluetooth.MODEL_SI_PO6)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : `Array<String>` ("SetTIME")  

+ #### 1.oxyGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.oxy.DeviceInfo  
> batteryState : 0 (no charge), 1 (charging), 2 (charging complete)  
> batteryValue : 0-100  
> oxiThr : 80-95  
> motor : KidsO2、Oxylink（0-5：MIN，5-10：LOW，10-17：MID，17-22：HIGH，22-35：MAX）, O2Ring（0-20：MIN，20-40：LOW，40-60：MID，60-80：HIGH，80-100：MAX）  
> workMode : 0 (Sleep Mode), 1 (Minitor Mode)  
> oxiSwitch :  
> 0 (off), 1 (on)-----just sound or vibration  
> 0 (sound off, vibration off), 1 (sound off, vibration on), 2 (sound on, vibration off), 3 (sound on, vibration on)-----sound and vibration  
> hrSwitch :  
> 0 (off), 1 (on)-----just sound or vibration  
> 0 (sound off, vibration off), 1 (sound off, vibration on), 2 (sound on, vibration off), 3 (sound on, vibration on)-----sound and vibration  
> hrLowThr : 30-250  
> hrHighThr : 30-250  
> curState : 0 (preparing), 1 (is ready), 2 (measuring)  
> lightingMode : 0-2 (0 : Standard Mode, 1 : Always Off Mode, 2 : Always On Mode)  
> lightStr : light level, 0-2  
> buzzer : checkO2Plus (0-20 : MIN, 20-40 : LOW, 40-60 : MID, 60-80 : HIGH, 80-100 : MAX)  

+ #### 2.oxyReadFile(model, filename)

Read file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  

Read file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileError).post(InterfaceEvent(model, true))`  

Read file complete :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.oxy.OxyFile  
> operationMode : 0 (Sleep Mode), 1 (Minitor Mode)  
> size : Total bytes of this data file package  
> asleepTime : Reserved for total asleep time future  
> avgSpo2 : Average blood oxygen saturation  
> minSpo2 : Minimum blood oxygen saturation  
> dropsTimes3Percent : drops below baseline - 3  
> dropsTimes4Percent : drops below baseline - 4 
> asleepTimePercent : T90 = (<90% duration time) / (total recording time) *100%  
> durationTime90Percent : Duration time when SpO2 lower than 90%  
> dropsTimes90Percent : Reserved for drop times when SpO2 lower than 90%  
> o2Score : Range: 0-100（For range 0-10, should be (O2 Score) / 10）  
> stepCounter : Total steps  

+ #### 3.oxyGetRtParam(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtParamData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.oxy.RtParam
> battery : 0-100  
> batteryState : 0 (no charge), 1 (charging), 2(charging complete)  
> state : 0 (lead off), 1 (lead on), other (error)  

+ #### 4.oxyUpdateSetting(model, type, value)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : `Array<String>` ("SetMotor", "SetBuzzer")  

+ #### 5.oxyFactoryReset(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyFactoryReset).post(InterfaceEvent(model, data))`  
`data` : boolean (true : factory reset success, false : factory reset failed)  


### PC-60FW (Bluetooth.MODEL_PC60FW)
### PC-66B (Bluetooth.MODEL_PC66B)
### OxySmart (Bluetooth.MODEL_OXYSMART)
### POD-1 (Bluetooth.MODEL_POD_1W)
### POD-2B (Bluetooth.MODEL_POD2B)
### PC-60NW-1 (Bluetooth.MODEL_PC_60NW_1)
### PC-60NW_SN (Bluetooth.MODEL_PC_60NW)
### PC-60NW (Bluetooth.MODEL_PC_60NW_NO_SN)
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
### PC-60NW_BLE (Bluetooth.MODEL_PC60NW_BLE)
### PC-60NW-WPS (Bluetooth.MODEL_PC60NW_WPS)

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
### PC80B_BLE: (Bluetooth.MODEL_PC80B_BLE2)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.pc80bGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc80b.DeviceInfo

+ #### 2.pc80bGetBattery(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bBatLevel).post(InterfaceEvent(model, data))`  
`data` : int (0-3, 0=0-25%, 1=25-50%, 2=50-75%, 3=75-100%)

+ #### 3.Real-time ECG data

sampling rate：150HZ  
mV = (n - 2048) * (1 / 330))  

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


### PC_300SNT (Bluetooth.MODEL_PC300)
### PC_300SNT-BLE (Bluetooth.MODEL_PC300_BLE)
### GM_300SNT (Bluetooth.MODEL_GM_300SNT)

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
> digit : 0, mV = n * (1 / 28.5) (ecgFloats = ecgInts * (1 / 28.5))  
> digit : 1, mV = n * (1 / 394) (ecgFloats = ecgInts * (1 / 394))

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

+ #### 8.Glu Result

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
mV = n * 0.0012820952991323

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
### SP-20-WPS (Bluetooth.MODEL_SP20_WPS)

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


### R20 (Bluetooth.MODEL_R20)
### R21 (Bluetooth.MODEL_R21)
### R10 (Bluetooth.MODEL_R10)
### R11 (Bluetooth.MODEL_R11)
### LERES (Bluetooth.MODEL_LERES)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)`

+ #### 1.ventilatorEncrypt(model, id)

exchange key ：  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEncrypt).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  
> TYPE_NORMAL_ERROR : 255, exchange key failed  

+ #### 2.syncTime(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetUtcTime).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)

+ #### 3.ventilatorGetInfo(model)

get info error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfoError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

get info success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.DeviceInfo  
> branchCode : used to query whether support the CPAP, APAP, S, S/T, T mode, VentilatorModel(branchCode)

+ #### 4.ventilatorGetVersionInfo(model)

get version info error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfoError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

get version info success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.VersionInfo

+ #### 5.ventilatorGetFileList(model)

get file list error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileListError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

get file list success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileList).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.RecordList  
> startTime : timestamp, unit second  
> type : 1(Daily statistics, file name is yyyyMMdd_day.stat), 2(Single statistics, file name is yyyyMMdd_HHmmss.stat, not used temporarily)

+ #### 6.ventilatorReadFile(model, fileName)

read file error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

read file progress :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int (0-100)  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.StatisticsFile  
> duration : unit second  
> usageDays : one day  
> spont : proportion of spontaneous respiration, 0-100%  
> (x_count * 3600) / recording_time = x_index / hour, (x_index in 0-200)  
> ahiCount : number of apnea hypopnea episodes  
> aiCount : number of apnea episodes  
> hiCount : hypoventilation frequency  
> oaiCount : number of respiratory pauses due to airway obstruction  
> caiCount : number of central apnea episodes  
> rearCount : number of awakenings related to respiratory effort  
> sniCount : snoring frequency  
> pbCount : periodic breathing frequency  
> takeOffCount : number of times removed  
> llTime : time for large air leakage  
> float pressure[] : 0.0-40.0cmH2O/hPa, device reports use pressure[4]  
> float ipap[] : inspiratory pressure, 0.0-40.0cmH2O/hPa, device reports use ipap[4]  
> float epap[] : expiratory pressure, 0.0-40.0cmH2O/hPa, device reports use epap[4]  
> int vt[] : tidal volume, 0-3000mL, device reports use vt[3]  
> float mv[] : minute Volume, 0.0-60.0L/min, device reports use mv[3]  
> float leak[] : air leakage rate, 0.0-120.0L/min, device reports use leak[4]  
> int rr[] : respiratory rate, 0-60bpm, device reports use rr[3]  
> float ti[] : inspiratory time, 0.1-4.0s, device reports use ti[3]  
> float ie[] : 1:50-3:1, 0.02-3.0, device reports use ie[3]  
> int spo2[] : 70-100%, device reports use spo2[0]  
> int pr[] : 30-250bpm, device reports use pr[2]  
> int hr[] : 30-250bpm, device reports use hr[2]

+ #### 7.ventilatorGetWifiList(model)

get wifi list error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiListError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  
> TYPE_NORMAL_ERROR : 255, wifi scanning  

get wifi success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiList).post(InterfaceEvent(model, data))`  
`data` : `ArrayList<Wifi>` (com.lepu.blepro.ext.ventilator.Wifi)

+ #### 8.ventilatorGetWifiConfig(model)

get wifi config error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfigError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  
> TYPE_NORMAL_ERROR : 255, no wifi config  

get wifi config success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfig).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.WifiConfig

+ #### 9.ventilatorSetWifiConfig(model, config)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWifiConfig).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)

+ #### 10.ventilatorDeviceBound(model, bound)

bound device error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBoundError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

bound device success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBound).post(InterfaceEvent(model, data))`  
`data` : int (0:success, 1:failed, 2:timeout)  

unbound device :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceUnBound).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)

+ #### 11.ventilatorDoctorModeIn(model, password, timestamp)/ventilatorDoctorModeOut(model)

doctor mode error :   
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorModeError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

doctor mode success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorMode).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.DoctorModeResult  
> errCode : 1(device doctor mode), 2(ble doctor mode), 3(socket doctor mode), 4(password error), 5(normal mode)

+ #### 12.ventilatorGetRtState(model)

get real time state error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtStateError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

get real time state success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtState).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.RtState  
> ventilationMode : 0(CPAP), 1(APAP), 2(S), 3(S/T), 4(T)  
> deviceMode : 0(normal mode), 1(device doctor mode), 2(ble doctor mode), 3(socket doctor mode)
> standard : 1(CFDA), 2(CE), 3(FDA)

+ #### 13.ventilatorGetRtParam(model)

get real time param error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtParamError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

get real time param success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.RtParam  
> pressure : 0-40cmH2O/hPa  
> ipap : 0-40cmH2O/hPa  
> epap : 0-40cmH2O/hPa  
> vt : 0-3000mL  
> mv : 0-60L/min  
> leak : 0-120L/min  
> rr : 0-60bpm  
> ti : 0.1-4s  
> ie : 1:50.0-3.0:1  
> spo2 : 70-100%  
> pr : 30-250bpm  
> hr : 30-250bpm

+ #### 14.ventilatorMaskTest(model, start)

mask test error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorMaskTestError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

mask test success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorMaskTest).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.MaskTestResult  
> status : 0(not in testing state), 1(under testing), 2(end of testing)  
> leak : 0-120L/min  
> result : 0(test not completed), 1(not suitable), 2(suitable)

+ #### 15.Event reporting

event reporting error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEventError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

event reporting success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEvent).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.Event  
> timestamp : event time, unit second  
> alarmLevel : Constant.VentilatorAlarmLevel  
> eventId : Constant.VentilatorEventId

+ #### 16.ventilatorVentilationSwitch(model, start)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorVentilationSwitch).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)

+ #### 17.ventilatorGetSystemSetting(model)

get system setting error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSettingError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

get system setting success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSetting).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.SystemSetting  
> unitSetting : UnitSetting  
> - pressureUnit : 0(cmH2O), 1(hPa)  

> languageSetting : LanguageSetting  
> - language : 0(English), 1(Chinese)  

> screenSetting : ScreenSetting  
> - brightness : 5-100%, step by 1%  
> - autoOff : 0(always screen on), 30s, 60s, 90s, 120s  

> replacements : Replacements  
> - filter : 0(off), 1-12  
> - mask : 0(off), 1-12  
> - tube : 0(off), 1-12  
> - tank : 0(off), 1-12  

> volumeSetting : VolumeSetting  
> - volume : 0-100%, step by 5%

+ #### 18.ventilatorSetSystemSetting(model, setting)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetSystemSetting).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)

+ #### 19.ventilatorGetMeasureSetting(model)

get measure setting error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSettingError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

get measure setting success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSetting).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.MeasureSetting  
> humidification : Humidification  
> - humidification : 0(off), 1-5, 0xff(auto)  

> pressureReduce : PressureReduce  
> - epr : expiratory pressure release, 0(off), 1-3  

> autoSwitch : AutoSwitch  

> preHeat : PreHeat  

> ramp : Ramp  
> - pressure : buffering pressure, 3-cpapPressure(CPAP), 3-apapPressureMin(APAP), 3-epapPressure(S,S/T,T), step by 0.5cmH2O/hPa  
> - time : buffering time, 0xff(auto), 0-60min, step by 5min  

> tubeType : TubeType  
> - type : 0(15mm), 1(19mm)  

> mask : Mask  
> - type : 0(full face), 1(nasal), 2(pillow)

+ #### 20.ventilatorSetMeasureSetting(model, setting)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetMeasureSetting).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)

+ #### 21.ventilatorGetVentilationSetting(model)

get ventilation setting error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSettingError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

get ventilation setting success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSetting).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.VentilationSetting  
> ventilationMode : VentilationMode  
> - mode : 0(CPAP), 1(APAP), 2(S), 3(S/T), 4(T)  

> cpapPressure : CpapPressure  
> - pressure : 4-20, step by 0.5cmH2O/hPa  

> apapPressureMax : ApapPressureMax  
> - max : apapPressureMin-20, step by 0.5cmH2O/hPa  

> apapPressureMin : ApapPressureMin  
> - min : 4-apapPressureMax, step by 0.5cmH2O/hPa  

> pressureExhale : PressureExhale  
> - exhale : 4-25(CE), 6-25(CFDA), step by 0.5cmH2O/hPa  
> - inhale >= exhale+2(CFDA), inhale >= exhale(CE)  

> pressureInhale : PressureInhale  
> - inhale : 4-25(CE), 4-23(CFDA), step by 0.5cmH2O/hPa  
> - inhale >= exhale+2(CFDA), inhale >= exhale(CE)  

> inhaleDuration : InhaleDuration  
> - duration : 0.3-4, step by 0.1s  
> - Max(0.3, pressureRaiseDuration) <= duration <= Min((60/respiratoryRate) * 2 / 3, 4)  

> respiratoryRate : RespiratoryRate  
> - rate : 5-30, step by 1bpm/min  
> - 5 <= rate <= Min(60/(inhaleDuration / 2 * 3))  

> pressureRaiseDuration : PressureRaiseDuration  
> - duration : 100-900ms, step by 50ms  
> - Max(100ms, minT) <= duration <= Min(900ms, LimT)  

> exhaleSensitive : ExhaleSensitive  
> - sentive : 0(auto), 1-5  

> inhaleSensitive : InhaleSensitive  
> - sentive : 0(auto), 1-5  
<table>
      <tr>
         <th>Number</th>
	 <th>Inhale Duration(s)</th>
	 <th>Pressure Raise Duration LimT(ms)</th>  
      </tr>
      <tr>
	 <td>1</td>
	 <td>0.3</td>
         <td>200</td>
      </tr>
      <tr>
	 <td>2</td>
	 <td>0.4</td>
         <td>250</td>
      </tr>
      <tr>
	 <td>3</td>
	 <td>0.5</td>
         <td>300</td>
      </tr>
      <tr>
	 <td>4</td>
	 <td>0.6</td>
         <td>400</td>
      </tr>
      <tr>
	 <td>5</td>
	 <td>0.7</td>
         <td>450</td>
      </tr>
      <tr>
	 <td>6</td>
	 <td>0.8</td>
         <td>500</td>
      </tr>
      <tr>
	 <td>7</td>
	 <td>0.9</td>
         <td>600</td>
      </tr>
      <tr>
	 <td>8</td>
	 <td>1.0</td>
         <td>650</td>
      </tr>
      <tr>
	 <td>9</td>
	 <td>1.1</td>
         <td>700</td>
      </tr>
      <tr>
	 <td>10</td>
	 <td>1.2</td>
         <td>800</td>
      </tr>
      <tr>
	 <td>11</td>
	 <td>1.3</td>
         <td>850</td>
      </tr>
      <tr>
	 <td>12</td>
	 <td>>=1.4</td>
         <td>900</td>
      </tr>
</table>
<table>
      <tr>
         <th>Number</th>
	 <th>Pressure Inhale-Pressure Exhale(sub)</th>
	 <th>Pressure Raise Duration minT(ms)</th>  
      </tr>
      <tr>
	 <td>1</td>
	 <td>2~5</td>
         <td>100</td>
      </tr>
      <tr>
	 <td>2</td>
	 <td>5.5~10</td>
         <td>200</td>
      </tr>
      <tr>
	 <td>3</td>
	 <td>10.5~15</td>
         <td>300</td>
      </tr>
      <tr>
	 <td>4</td>
	 <td>15.5~20</td>
         <td>400</td>
      </tr>
      <tr>
	 <td>5</td>
	 <td>20.5~21</td>
         <td>450</td>
      </tr>
</table>

+ #### 22.ventilatorSetVentilationSetting(model, setting)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetVentilationSetting).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)

+ #### 23.ventilatorGetWarningSetting(model)

get warning setting error :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSettingError).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)  

get warning setting success :  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSetting).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ventilator.WarningSetting  
> warningApnea : WarningApnea  
> - apnea : 0(off), 10s, 20s, 30s  

> warningLeak : WarningLeak  
> - high : 0(off), 15s, 30s, 45s, 60s  

> warningVt : WarningVt  
> - low : 0(off), 200-2000ml, step by 10ml  

> warningVentilation : WarningVentilation  
> - low : 0(off), 1-25L/min, step by 1L/min  

> warningRrHigh : WarningRrHigh  
> - high : 0(off), 1-60bpm, step by 1bpm  
> - warningRrLow+2~60 (if low != 0 and warningRrHigh != 0)  

> warningRrLow : WarningRrLow  
> - low : 0(off), 1-60bpm, step by 1bpm  
> - 1~warningRrHigh-2 (if low != 0 and warningRrHigh != 0)  

> warningSpo2Low : WarningSpo2Low  
> - low : 0(off), 80-95%, step by 1%  

> warningHrHigh : WarningHrHigh  
> - high : 0(off), 100-240bpm, step by 10bpm  

> warningHrLow : WarningHrLow  
> - low : 0(off), 30-70bpm, step by 5bpm  

+ #### 24.ventilatorSetWarningSetting(model, setting)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWarningSetting).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)

+ #### 25.ventilatorFactoryReset(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorFactoryReset).post(InterfaceEvent(model, data))`  
`data` : int (Constant.VentilatorResponseType)


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


### VTM AD5 (Bluetooth.MODEL_VTM_AD5)
### MD1000AF4 (Bluetooth.MODEL_FETAL)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)`  

+ #### 1.Real-time data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AD5.EventAd5RtHr).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.Ad5Data


### VCOMIN (Bluetooth.MODEL_VCOMIN)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)`  

+ #### 1.Real-time data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VCOMIN.EventVcominRtHr).post(InterfaceEvent(model, data))`  
`data` : Int, HR


### FHR-666(BLE) (Bluetooth.MODEL_FHR)

SDK will send this event when BluetoothDevice connected :   
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)`  

+ #### 1.Device info

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.FHR.EventFhrDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.FhrInfo  
> hr：60-240  
> volume：0-6  
> strength：0-2  
> battery：0-6

+ #### 2.Audio data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.FHR.EventFhrAudioData).post(InterfaceEvent(model, data))`  
`data` : byte[]


