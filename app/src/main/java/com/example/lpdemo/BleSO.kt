package com.example.lpdemo

import android.app.Application
import android.util.Log
import com.lepu.blepro.observer.BleServiceObserver

/**
 * BleService Observer
 */
class BleSO private constructor(val application: Application) : BleServiceObserver{
    val TAG : String = "BleSO"

    companion object : SingletonHolder<BleSO, Application>(::BleSO)

    override fun onServiceCreate() {
        Log.d(TAG, "Ble service onCreate")
    }

    override fun onServiceDestroy() {
        Log.d(TAG,"Ble service onServiceDestroy")

    }


}