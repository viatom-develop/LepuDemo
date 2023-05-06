package com.example.lpdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView

class DataActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)
        pdfView = findViewById(R.id.document_pdf)
        pdfView.fromBytes(intent.getByteArrayExtra("pdfBytes")).load()
    }
}