# Android SDK API文档
## SDK支持平台
Android5.0及以上版本
## 导入SDK
blepro-debug.aar
## BleServiceObserver
用于监听蓝牙服务生命周期  
onServiceCreate()  
onServiceDestroy()  
## BleChangeObserver
用于监听蓝牙状态
onBleStateChanged(model, state)：  
model：一种设备对应一个model  
state：蓝牙状态Ble.State
## BleServiceHelper
+ ### initService(application, bleServiceObserver)
SDK初始化方法，在application onCreate()进行初始化，`注：App运行期间，只需要初始化一次，无需重复初始化`  
蓝牙服务初始化完成后发送消息：  
LiveEventBus.get\<Boolean\>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit).post(true)
+ ### stopService(application)
停止蓝牙服务
+ ### startScan()
扫描设备，扫到设备后发送消息：  
LiveEventBus.get\<Bluetooth\>(EventMsgConst.Discovery.EventDeviceFound).post(bluetooth)
+ ### stopScan()
停止扫描，注：在连接设备前必须先停止扫描
+ ### setInterfaces(model)
设置interface，每种设备由对应interface管理蓝牙交互
+ ### connect(context, model, bluetoothDevice)  
连接设备，`注：在连接设备前必须先设置interface` 
+ ### disconnect(autoReconnect)
断开所有设备的连接  
autoReconnect：断开连接后是否自动重连设备
+ ### disconnect(model, autoReconnect)
断开指定model设备的连接  

## (以下按设备分类进行接口说明)：  
### PC-60FW (Bluetooth.MODEL_PC60FW)
连接设备成功后再进行接口的使用，连接成功会发送消息：  
LiveEventBus.get\<Int\>(EventMsgConst.Ble.EventBleDeviceReady).post(model)
+ #### 1.pc60fwGetInfo(model)
请求获取设备信息，sdk拿到设备信息数据会发送消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc60fw.DeviceInfo
+ #### 2.实时血氧参数包（设备自动发送，频率1HZ）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC60Fw.EventPC60FwRtParam).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc60fw.RtParam
+ #### 3.实时血氧波形包（设备自动发送，频率50HZ）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC60Fw.EventPC60FwRtWave).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc60fw.RtWave
+ #### 4.设备电量信息（设备自动发送）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC60Fw.EventPC60FwBatLevel).post(InterfaceEvent(model, data))  
data数据类型：int
+ #### 5.设备工作状态（设备自动发送）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC60Fw.EventPC60FwWorkingStatus).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc60fw.WorkingStatus  

### AP-10 / AP-20 (Bluetooth.MODEL_AP20)
连接设备成功后再进行以下接口的使用，连接成功会发送消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.AP20.EventAp20SetTime).post(InterfaceEvent(model, true))
+ #### 1.ap20GetInfo(model)
获取设备信息消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.AP20.EventAp20DeviceInfo).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.ap20.DeviceInfo
+ #### 2.ap20SetConfig(model, type, config)
配置参数消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.AP20.EventAp20SetConfigResult).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.ap20.SetConfigResult
+ #### 3.ap20GetConfig(model, type)
获取参数消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.AP20.EventAp20GetConfigResult).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.ap20.GetConfigResult
+ #### 4.ap20GetBattery(model)
获取电量信息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.AP20.EventAp20BatLevel).post(InterfaceEvent(model, data))  
data数据类型：int
+ #### 5.实时血氧参数包（设备自动发送，频率1HZ）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.AP20.EventAp20RtOxyParam).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.ap20.RtOxyParam
+ #### 6.实时血氧波形包（设备自动发送，频率50HZ）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.AP20.EventAp20RtOxyWave).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.ap20.RtOxyWave
+ #### 7.实时鼻息流参数包（设备自动发送，频率1HZ）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.AP20.EventAp20RtBreathParam).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.ap20.RtBreathParam
+ #### 8.实时鼻息流波形包（设备自动发送，频率50HZ）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.AP20.EventAp20RtBreathWave).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.ap20.RtBreathWave  

### PC-102 (Bluetooth.MODEL_PC100)
连接设备成功后再进行以下接口的使用，连接成功会发送消息：  
LiveEventBus.get\<Int\>(EventMsgConst.Ble.EventBleDeviceReady).post(model)
+ #### 1.pc100GetInfo(model)
获取设备信息消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC100.EventPc100DeviceInfo).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc102.DeviceInfo
+ #### 2.pc100StartBp(model)
开始血压测量消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC100.EventPc100BpStart).post(InterfaceEvent(model, true))
+ #### 3.pc100StopBp(model)
停止血压测量消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC100.EventPc100BpStop).post(InterfaceEvent(model, true))
+ #### 4.血压测量结果（设备测量完成自动发送）
正常结果消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC100.EventPc100BpResult).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc102.BpResult  
错误结果消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC100.EventPc100BpErrorResult).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc102.BpResultError
+ #### 5.实时血压测量数据（设备测量时自动发送）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC100.EventPc100RtBpData).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc102.RtBpData
+ #### 6.实时血氧参数包（设备自动发送，频率1HZ）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC100.EventPc100RtOxyParam).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc102.RtOxyParam
+ #### 7.实时血氧波形包（设备自动发送，频率50HZ）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC100.EventPc100RtOxyWave).post(InterfaceEvent(model, data))  
data数据类型：ByteArray  

### PC-80B (Bluetooth.MODEL_PC80B)
连接设备成功后再进行以下接口的使用，连接成功会发送消息：  
LiveEventBus.get\<Int\>(EventMsgConst.Ble.EventBleDeviceReady).post(model)
+ #### 1.pc80bGetInfo(model)
获取设备信息消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC80B.EventPc80bDeviceInfo).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc80b.DeviceInfo
+ #### 2.pc80bGetBattery(model)
电量查询消息：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC80B.EventPc80bBatLevel).post(InterfaceEvent(model, data))  
data数据类型：int
+ #### 3.实时心电数据（设备测量时自动发送）
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC80B.EventPc80bRtData).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc80b.RtData
+ #### 4.心电记录传输（从设备端选择文件发送）
传输进度：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC80B.EventPc80bReadingFileProgress).post(InterfaceEvent(model, data))  
data数据类型：int  
传输出错：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC80B.EventPc80bReadFileError).post(InterfaceEvent(model, true))  
传输完成：  
LiveEventBus.get\<InterfaceEvent\>(InterfaceEvent.PC80B.EventPc80bReadFileComplete).post(InterfaceEvent(model, data))  
data数据类型：com.lepu.blepro.ext.pc80b.EcgFile  

+ ### PC-68B (Bluetooth.MODEL_PC68B)



+ ### PC-303 (Bluetooth.MODEL_PC303)

