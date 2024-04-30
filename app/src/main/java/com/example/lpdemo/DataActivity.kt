package com.example.lpdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lpdemo.databinding.ActivityDataBinding

class DataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.documentPdf.fromBytes(intent.getByteArrayExtra("pdfBytes")).load()
    }
}