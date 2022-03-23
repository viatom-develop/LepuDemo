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