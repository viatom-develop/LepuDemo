package com.example.lpdemo.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

val _bleState = MutableLiveData<Boolean>().apply {
    value = false
}
val bleState: LiveData<Boolean> = _bleState


var deviceModel = 0
var deviceName = ""
var deviceAddress = ""

fun savePreferences(context: Context, key: String, value: String) {
    val editor: SharedPreferences.Editor
    val preferences: SharedPreferences = context.getSharedPreferences("bluetooth_info", Context.MODE_PRIVATE)
    editor = preferences.edit()
    editor.putString(key, value)
    editor.commit()
}

fun readStrPreferences(context: Context, key: String, defaultValue: String): String {
    val preferences: SharedPreferences = context.getSharedPreferences("bluetooth_info", Context.MODE_PRIVATE)
    return preferences.getString(key, defaultValue) ?: return ""
}