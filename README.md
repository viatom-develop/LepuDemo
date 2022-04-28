# Android SDK API

## SDK support platform

Version at least Android 7.0

## aar version

> lepu-blepro-0.0.1.aar : add PC-60FW, PC-102, PC-80B, AP-10/AP-20  
> lepu-blepro-0.0.2.aar : add POD-1W

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

### PC-60FW (Bluetooth.MODEL_PC60FW)

SDK will send this event when BluetoothDevice connected :  
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.pc60fwGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo).post(InterfaceEvent(model, data))`  
`data` ：com.lepu.blepro.ext.pc60fw.DeviceInfo

+ #### 2.Real-time Oxy param data (BluetoothDevice auto send data, frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc60fw.RtParam
> spo2 : 0%-100%  
> pr : 0-511bpm  
> pi : 0%-25.5%  
> isProbeOff  
> isPulseSearching

+ #### 3.Real-time Oxy waveform data (BluetoothDevice auto send data, frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtWave).post(InterfaceEvent(model, data))`   
`data` : com.lepu.blepro.ext.pc60fw.RtWave

+ #### 4.Battery

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwBatLevel).post(InterfaceEvent(model, data))`  
`data` : int (0-3, 0=25%, 1=50%, 2=75%, 3=100%)

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
	    <td>0x03 Meun</td>
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

### AP-10 / AP-20 (Bluetooth.MODEL_AP20)

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
> BACK_LIGHT (0-5)  
> ALARM_SWITCH (0 : off 1 : on)  
> LOW_OXY_THRESHOLD (85-99)  
> LOW_HR_THRESHOLD (30-99)  
> HIGH_HR_THRESHOLD (100-250)

+ #### 4.ap20GetBattery(model)
 
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20BatLevel).post(InterfaceEvent(model, data))`  
`data` : int (0-3, 0=25%, 1=50%, 2=75%, 3=100%)

+ #### 5.Real-time Oxy param data (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtOxyParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.RtOxyParam
> spo2 : 0%-100%  
> pr : 0-511bpm  
> pi : 0%-25.5%  
> isProbeOff  
> isPulseSearching  
> battery : 0-3 (0=25%, 1=50%, 2=75%, 3=100%)

+ #### 6.Real-time Oxy waveform data (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtOxyWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.RtOxyWave

+ #### 7.Real-time nasal flow param (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.RtBreathParam
> rr : respiratory rate (6-60bpm, 0 invalid value)  
> singleFlag : 0(respiratory normal), 1(no respiratory)

+ #### 8.Real-time nasal flow wave (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathWave).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.ap20.RtBreathWave
> flowInt : 0-4095  
> snoreInt : 0-4095

### PC-102 (Bluetooth.MODEL_PC100)

`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.pc100GetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100DeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.DeviceInfo

+ #### 2.pc100StartBp(model)
  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpStart).post(InterfaceEvent(model, true))` 

+ #### 3.pc100StopBp(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpStop).post(InterfaceEvent(model, true))` 

+ #### 4.Real-time Bp measure result

normal result :  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.BpResult
> sys : systolic pressure  
> pr : pulse rate  
> dia : diastolic pressure  
> map : average pressure  
> result : 0(hr normal), 1(hr irregular)

error result :  

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.BpResultError

+ #### 5.Real-time Bp data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtBpData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.RtBpData
> sign : 0(no hr), 1(has hr)  
> ps : current pressure

+ #### 6.Real-time Oxy param data (frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc102.RtOxyParam  
> spo2 : 0%-100%  
> pr : 0-511bpm  
> pi : 0%-25.5%  
> isDetecting  
> isScanning

+ #### 7.Real-time Oxy waveform data (frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyWave).post(InterfaceEvent(model, data))`  
`data` : ByteArray

### PC-80B (Bluetooth.MODEL_PC80B)
 
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.pc80bGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bDeviceInfo).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc80b.DeviceInfo

+ #### 2.pc80bGetBattery(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bBatLevel).post(InterfaceEvent(model, data))`  
`data` : int (0-3, 0=25%, 1=50%, 2=75%, 3=100%)

+ #### 3.Real-time ECG data

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bRtData).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc80b.RtData  
> seqNo  
> gain : device waveform amplitude coefficient  
> channel : 0(detecting channel), 1(internal channel) 2(external channel)  
> measureMode : 0(detecting model), 1(Fast mode, 30s), 2(Continuous mode)  
> measureStage : 0(detecting stage), 1(preparing), 2(measuring), 3(analyzing), 4(result), 5(stop)  
> leadOff  
> dataType : (1：ECG data 2：ECG result)  
> ecgData : ECG data  
> ecgResult : ECG result

+ #### 4.Receive record data（From BluetoothDevice）

read file progress ：  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadingFileProgress).post(InterfaceEvent(model, data))`  
`data` : int（0-100）  
read file error ：  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileError).post(InterfaceEvent(model, true))`  
read file complete ：  
`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileComplete).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pc80b.EcgFile  

### POD-1W (Bluetooth.MODEL_POD_1W)

SDK will send this event when BluetoothDevice connected :  
`LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)` 

+ #### 1.pod1wGetInfo(model)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.POD1w.EventPOD1wDeviceInfo).post(InterfaceEvent(model, data))`  
`data` ：com.lepu.blepro.ext.pod1w.DeviceInfo

+ #### 2.Real-time Oxy param data (BluetoothDevice auto send data, frequency 1HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.POD1w.EventPOD1wRtParam).post(InterfaceEvent(model, data))`  
`data` : com.lepu.blepro.ext.pod1w.RtParam
> spo2 : 0%-100%  
> pr : 0-511bpm  
> pi : 0%-25.5%  
> isProbeOff  
> isPulseSearching

+ #### 3.Real-time Oxy waveform data (BluetoothDevice auto send data, frequency 50HZ)

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.POD1w.EventPOD1wRtWave).post(InterfaceEvent(model, data))`   
`data` : com.lepu.blepro.ext.pod1w.RtWave

+ #### 4.Battery

`LiveEventBus.get<InterfaceEvent>(InterfaceEvent.POD1w.EventPOD1wBatLevel).post(InterfaceEvent(model, data))`  
`data` : int (0-3, 0=25%, 1=50%, 2=75%, 3=100%)

+ ### PC-68B (Bluetooth.MODEL_PC68B)



+ ### PC-303 (Bluetooth.MODEL_PC303)

