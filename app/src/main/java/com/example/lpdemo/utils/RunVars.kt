package com.example.lpdemo.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.utils.HexString
import org.apache.commons.io.FileUtils
import java.io.File

val _bleState = MutableLiveData<Boolean>().apply {
    value = false
}
val bleState: LiveData<Boolean> = _bleState

val dataEcgSrc: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}

val ecgData = EcgData()

var deviceModel = 0
var deviceName = ""
var deviceAddress = ""

// er3
val dataEcgSrc1: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc2: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc3: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc4: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc5: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc6: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc7: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc8: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc9: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc10: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc11: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}
val dataEcgSrc12: MutableLiveData<FloatArray> by lazy {
    MutableLiveData<FloatArray>()
}

// sdk save the original file name : userId + fileName + .dat
fun getOffset(model: Int, fileName: String, userId: String): ByteArray {
    val trimStr = HexString.trimStr(fileName)
    BleServiceHelper.BleServiceHelper.rawFolder?.get(model)?.let { s ->
        val mFile = File(s, "$userId$trimStr.dat")
        if (mFile.exists()) {
            FileUtils.readFileToByteArray(mFile)?.let {
                return it
            }
        } else {
            return ByteArray(0)
        }
    }
    return ByteArray(0)
}

fun toSignedShort(byte1: Byte, byte2: Byte):Short {
    return ((byte2.toInt() shl 8) or (byte1.toInt() and 0xFF)).toShort()
}
