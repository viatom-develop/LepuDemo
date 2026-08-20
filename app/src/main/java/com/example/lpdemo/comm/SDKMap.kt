package com.example.lpdemo.comm

import com.lepu.blepro.ext.api.Ad5FhrHandler
import com.lepu.blepro.ext.api.AirBpHandler
import com.lepu.blepro.ext.api.Aoj20aHandler
import com.lepu.blepro.ext.api.Ap20Handler
import com.lepu.blepro.ext.api.BbsmP1Handler
import com.lepu.blepro.ext.api.BiolandBgmHandler
import com.lepu.blepro.ext.api.Bp2Handler
import com.lepu.blepro.ext.api.Bp2wHandler
import com.lepu.blepro.ext.api.Bp3Handler
import com.lepu.blepro.ext.api.BpmHandler
import com.lepu.blepro.ext.api.CheckmeHandler
import com.lepu.blepro.ext.api.CheckmeLeHandler
import com.lepu.blepro.ext.api.CheckmePodHandler
import com.lepu.blepro.ext.api.EcnHandler
import com.lepu.blepro.ext.api.Er1Handler
import com.lepu.blepro.ext.api.Er2Handler
import com.lepu.blepro.ext.api.Er3Handler
import com.lepu.blepro.ext.api.FhrHandler
import com.lepu.blepro.ext.api.LemHandler
import com.lepu.blepro.ext.api.LepodHandler
import com.lepu.blepro.ext.api.LpBp2wHandler
import com.lepu.blepro.ext.api.Lpm311Handler
import com.lepu.blepro.ext.api.OxyCommonHandler
import com.lepu.blepro.ext.api.OxyIIHandler
import com.lepu.blepro.ext.api.Pc100Handler
import com.lepu.blepro.ext.api.Pc300Handler
import com.lepu.blepro.ext.api.Pc60FwHandler
import com.lepu.blepro.ext.api.Pc68bHandler
import com.lepu.blepro.ext.api.Pc80Handler
import com.lepu.blepro.ext.api.Pf10Aw1Handler
import com.lepu.blepro.ext.api.PoctorM3102Handler
import com.lepu.blepro.ext.api.PulsebitHandler
import com.lepu.blepro.ext.api.Sp20Handler
import com.lepu.blepro.ext.api.VcominFhrHandler
import com.lepu.blepro.ext.api.VentilatorHandler
import com.lepu.blepro.ext.api.VetcorderHandler
import com.lepu.blepro.ext.api.Vtm20fHandler

object SDKMap {

    val youBle = Pair(BLUETOOTH_NAME, MODEL)

    val oxyCommonHandler = OxyCommonHandler(youBle.second)
    val ad5FhrHandler = Ad5FhrHandler(youBle.second)
    val airBpHandler = AirBpHandler(youBle.second)
    val aoj20aHandler = Aoj20aHandler(youBle.second)
    val ap20Handler = Ap20Handler(youBle.second)
    val bbsmP1Handler = BbsmP1Handler(youBle.second)
    val biolandBgmHandler = BiolandBgmHandler(youBle.second)
    val bp2Handler = Bp2Handler(youBle.second)
    val bp2wHandler = Bp2wHandler(youBle.second)
    val bp3Handler = Bp3Handler(youBle.second)
    val bpmHandler = BpmHandler(youBle.second)
    val checkmeHandler = CheckmeHandler(youBle.second)
    val checkmeLeHandler = CheckmeLeHandler(youBle.second)
    val vetcorderHandler = VetcorderHandler(youBle.second)
    val checkmePodHandler = CheckmePodHandler(youBle.second)
    val ecnHandler = EcnHandler(youBle.second)
    val er1Handler = Er1Handler(youBle.second)
    val er2Handler = Er2Handler(youBle.second)
    val er3Handler = Er3Handler(youBle.second)
    val fhrHandler = FhrHandler(youBle.second)
    val lemHandler = LemHandler(youBle.second)
    val lepodHandler = LepodHandler(youBle.second)
    val lpBp2wHandler = LpBp2wHandler(youBle.second)
    val lpm311Handler = Lpm311Handler(youBle.second)
    val oxyIIHandler = OxyIIHandler(youBle.second)
    val pc60FwHandler = Pc60FwHandler(youBle.second)
    val pc68bHandler = Pc68bHandler(youBle.second)
    val pc80Handler = Pc80Handler(youBle.second)
    val pc100Handler = Pc100Handler(youBle.second)
    val pc300Handler = Pc300Handler(youBle.second)
    val pf10Aw1Handler = Pf10Aw1Handler(youBle.second)
    val poctorM3102Handler = PoctorM3102Handler(youBle.second)
    val pulsebitHandler = PulsebitHandler(youBle.second)
    val sp20Handler = Sp20Handler(youBle.second)
    val vcominFhrHandler = VcominFhrHandler(youBle.second)
    val ventilatorHandler = VentilatorHandler(youBle.second)
    val vtm20fHandler = Vtm20fHandler(youBle.second)

}