package com.example.lpdemo

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityWaveEcgBinding
import com.example.lpdemo.utils.ecgData
import com.example.lpdemo.utils.getOffset
import com.example.lpdemo.views.WaveEcgView
import com.lepu.blepro.ext.er1.Er1EcgFile
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.DecompressUtil

class WaveEcgActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaveEcgBinding
    var filterEcgView: WaveEcgView? = null
    var currentZoomLevel = 0
    val handler = Handler()
    var mAlertDialog: AlertDialog? = null

    var filterWaveData: ShortArray? = null
    var mills = 0L
    var model = Bluetooth.MODEL_ER1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaveEcgBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAlertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage("正在处理，请稍等...")
            .create()
        mAlertDialog?.show()

        model = intent.getIntExtra("model", Bluetooth.MODEL_ER1)
        filterWaveData = when (model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_HHM1 -> {
                val data = Er1EcgFile(getOffset(model, ecgData.fileName, ""))
                DecompressUtil.er1Decompress(data.waveData)
            }
            else -> {
                ecgData.shortData
            }
        }
        mills = ecgData.startTime

        handler.postDelayed({
            ecgWave()
        }, 1000)

        ArrayAdapter(this,
            android.R.layout.simple_list_item_1,
            arrayListOf("5mm/mV", "10mm/mV", "20mm/mV")
        ).apply {
            binding.gain.adapter = this
        }
        binding.gain.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (filterEcgView != null) {
                    filterEcgView!!.currentZoomPosition = position
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        ArrayAdapter(this,
            android.R.layout.simple_list_item_1,
            arrayListOf("6.25mm/s", "12.5mm/s", "25mm/s")
        ).apply {
            binding.speed.adapter = this
        }
        binding.speed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (filterEcgView != null) {
                    when (position) {
                        0 -> filterEcgView!!.setmSpeed(6.25f)
                        1 -> filterEcgView!!.setmSpeed(12.5f)
                        2 -> filterEcgView!!.setmSpeed(25f)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
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
            filterEcgView = WaveEcgView(this, mills*1000, filterWaveData, filterWaveData!!.size, width*1f, height*1f, currentZoomLevel, model)
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