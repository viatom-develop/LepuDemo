# Handler API Mapping (lepu-blepro 1.4.0)

`1.4.0` introduces the object-oriented **Handler API** (`com.lepu.blepro.ext.api.*`).
The sample project instantiates all Handlers in one place:
`app/src/main/java/com/example/lpdemo/comm/SDKMap.kt`.

Every device Activity in the project has its corresponding Handler; the two map
one-to-one, and all of them are wired to the new API.

## Activity ↔ Handler mapping

| Activity | Handler (SDKMap) |
|----------|------------------|
| Ad5Activity | ad5FhrHandler |
| AirBpActivity | airBpHandler |
| Aoj20aActivity | aoj20aHandler |
| Ap20Activity | ap20Handler |
| BBSMBS1Activity | bbsmP1Handler |
| BiolandBgmActivity | biolandBgmHandler |
| Bp2Activity | bp2Handler |
| Bp2wActivity | bp2wHandler |
| Bp3Activity | bp3Handler |
| BpmActivity | bpmHandler |
| CheckmeActivity | checkmeHandler |
| CheckmeLeActivity | checkmeLeHandler |
| CheckmeMonitorActivity | vetcorderHandler |
| CheckmePodActivity | checkmePodHandler |
| EcnActivity | ecnHandler |
| Er1Activity | er1Handler |
| Er2Activity | er2Handler |
| Er2SActivity | er2Handler |
| Er3Activity | er3Handler |
| FhrActivity | fhrHandler |
| LemActivity | lemHandler |
| LepodActivity | lepodHandler |
| LpBp2wActivity | lpBp2wHandler |
| Lpm311Activity | lpm311Handler |
| OxyActivity | oxyCommonHandler |
| OxyIIActivity | oxyIIHandler |
| Pc102Activity | pc100Handler |
| Pc303Activity | pc300Handler |
| Pc60fwActivity | pc60FwHandler |
| Pc68bActivity | pc68bHandler |
| Pc80bActivity | pc80Handler |
| Pf10Aw1Activity | pf10Aw1Handler |
| PoctorM3102Activity | poctorM3102Handler |
| PulsebitExActivity | pulsebitHandler |
| Sp20Activity | sp20Handler |
| VcominActivity | vcominFhrHandler |
| VentilatorActivity | ventilatorHandler |
| Vtm20fActivity | vtm20fHandler |

> 38 device Activities in total, mapping to 39 Handler instances in SDKMap
> (`er2Handler` is shared by both `Er2Activity` and `Er2SActivity`; `oxyCommonHandler`
> is used by `OxyActivity` and by the scan/connect flow).

## Migration pattern

Device Activities use a new/old dual-branch style so both APIs coexist, which
makes gradual rollout easy:

```kotlin
binding.getInfo.setOnClickListener {
    if (model == SDKMap.youBle.second) {
        SDKMap.ap20Handler.getInfo()                         // new: Handler API
    } else {
        BleServiceHelper.BleServiceHelper.ap20GetInfo(model) // old: BleServiceHelper
    }
}
```

The data-receiving side (`LiveEventBus.get<InterfaceEvent>(...)` inside `initEventBus()`)
is identical for both APIs and remains unchanged.
