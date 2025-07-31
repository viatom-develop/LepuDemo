package com.example.lpdemo

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lpdemo.databinding.ActivityFileContentShowBinding
import com.example.lpdemo.utils.DataConvert
import com.example.lpdemo.utils.ecgData
import com.lepu.blepro.ext.bbsm.BbsmP1EventFile
import com.lepu.blepro.ext.bbsm.BbsmP1RecordFile
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.DateUtil
import kotlinx.coroutines.launch
import java.util.Date

class FileContentShowActivity : AppCompatActivity() {

    private val TAG = "FileContentShowActivity"

    private lateinit var binding: ActivityFileContentShowBinding

    private var model = Bluetooth.MODEL_BBSM_BS1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileContentShowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.showFileContentTv.movementMethod = ScrollingMovementMethod()
        model = intent.getIntExtra("model", model)
        lifecycleScope.launch {
            if (model == Bluetooth.MODEL_BBSM_BS1) {
                if (ecgData.fileName.contains("R")) {
                    val data = BbsmP1RecordFile(ecgData.data)
                    handleBbsmP1RecordFile(data)
                } else if (ecgData.fileName.contains("E")) {
                    val data = BbsmP1EventFile(ecgData.data)
                    handleBbsmP1EventFile(data)
                }
            }
        }
    }

    private fun handleBbsmP1RecordFile(data: BbsmP1RecordFile) {
        var str = "size：${data.records.size}\n" +
                "timestamp：${data.timestamp}\n" +
                "time：${
                    DateUtil.stringFromDate(
                        Date(data.timestamp.times(1000)),
                        "yyyy-MM-dd HH:mm:ss"
                    )
                }\n" +
                "interval：${data.interval}\n" +
                "duration：${data.duration} 秒, ${DataConvert.getEcgTimeStr(data.duration)}\n"
        if (data.records.size > 40) {
            for (i in 0 until 20) {
                str += "resp：${data.records[i].resp}， gesture：${data.records[i].gesture} ${
                    when (data.records[i].gesture) {
                        0 -> "lie supine"
                        1 -> "Lie on the right side"
                        2 -> "Lie on the left side"
                        3 -> "lie prostrate"
                        4 -> "sit up"
                        else -> "unknown"
                    }
                }， temp：${data.records[i].temp}\n"
            }
            for (i in data.records.size - 20 until data.records.size) {
                str += "resp：${data.records[i].resp}， gesture：${data.records[i].gesture} ${
                    when (data.records[i].gesture) {
                        0 -> "lie supine"
                        1 -> "Lie on the right side"
                        2 -> "Lie on the left side"
                        3 -> "lie prostrate"
                        4 -> "sit up"
                        else -> "unknown"
                    }
                }， temp：${data.records[i].temp}\n"
            }
        } else {
            for (i in 0 until data.records.size) {
                str += "resp：${data.records[i].resp}， gesture：${data.records[i].gesture} ${
                    when (data.records[i].gesture) {
                        0 -> "lie supine"
                        1 -> "Lie on the right side"
                        2 -> "Lie on the left side"
                        3 -> "lie prostrate"
                        4 -> "sit up"
                        else -> "unknown"
                    }
                }， temp：${data.records[i].temp}\n"
            }
        }
        binding.showFileContentTv.text = str
    }

    private fun handleBbsmP1EventFile(data: BbsmP1EventFile) {
        var str = "size：${data.events.size}\n"
        for (i in 0 until data.events.size) {
            val t = DateUtil.stringFromDate(
                Date(data.events[i].timestamp * 1000L),
                "yyyy-MM-dd HH:mm:ss"
            )
            str += "time:$t, type:${data.events[i].eventId} ${
                when (data.events[i].eventId) {
                    1 -> "lie prostrate"
                    2 -> "The temperature is too low"
                    3 -> "The temperature is too high"
                    4 -> "Kick the quilt"
                    5 -> "Low respiratory rate"
                    6 -> "High respiratory rate"
                    else -> "unknown"
                }
            }\n"
        }
        binding.showFileContentTv.text = str
    }

}