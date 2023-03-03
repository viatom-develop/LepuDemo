package com.example.lpdemo.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

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
