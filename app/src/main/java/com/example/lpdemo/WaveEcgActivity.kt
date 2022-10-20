package com.example.lpdemo

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.utils.ecgData
import com.example.lpdemo.views.WaveEcgView
import com.lepu.blepro.objs.Bluetooth

class WaveEcgActivity : AppCompatActivity() {

    var filterEcgView: WaveEcgView? = null
    var currentZoomLevel = 1
    val handler = Handler()
    var mAlertDialog: AlertDialog? = null

    var filterWaveData: ShortArray? = null
    var mills = 0L
    var model = Bluetooth.MODEL_ER1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wave_ecg)

        mAlertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage("正在处理，请稍等...")
            .create()
        mAlertDialog?.show()

        model = intent.getIntExtra("model", Bluetooth.MODEL_ER1)
        filterWaveData = ecgData.shortData
        mills = ecgData.startTime

        handler.postDelayed({
            ecgWave()
        }, 1000)
    }

    private fun ecgWave() {
        mAlertDialog?.dismiss()
        if (filterEcgView != null) {
            currentZoomLevel = filterEcgView!!.currentZoomPosition
        }

        val layout: RelativeLayout = findViewById(R.id.rl_ecg_container)
        val width = layout.width
        val height = layout.height
        if (filterEcgView == null && filterWaveData != null) {
            filterEcgView = WaveEcgView(this, mills*1000, filterWaveData, filterWaveData!!.size, width*1f, height*1f, currentZoomLevel, false, model)
        }
        if(filterEcgView != null) {
            layout.removeAllViews()
            layout.addView(filterEcgView)
        }
        val layoutParams = layout.layoutParams
        val lineHeight = width * 2 * 4 / (7 * 5) + 20
        layoutParams.height = lineHeight * 9 + 10
        layout.layoutParams = layoutParams
    }

}