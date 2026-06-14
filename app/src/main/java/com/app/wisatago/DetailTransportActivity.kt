package com.app.wisatago

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetailTransportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_transport)
        val btnBack = findViewById<ImageView>(R.id.btnBackTransport)
        btnBack.setOnClickListener {
            finish()
        }

        val img = findViewById<ImageView>(R.id.imgTransport)
        val tvNama = findViewById<TextView>(R.id.tvNamaTransport)
        val tvRating = findViewById<TextView>(R.id.tvRatingTransport)
        val tvJenis = findViewById<TextView>(R.id.tvJenisTransport)
        val tvRute = findViewById<TextView>(R.id.tvRuteTransport)
        val tvDeskripsi = findViewById<TextView>(R.id.tvDeskripsiTransport)

        img.setImageResource(intent.getIntExtra("gambar", R.drawable.ic_parahyangan))
        tvNama.text = intent.getStringExtra("nama")
        tvRating.text = "⭐ ${intent.getStringExtra("rating")}"
        tvJenis.text = intent.getStringExtra("jenis")
        tvRute.text = intent.getStringExtra("rute")
        tvDeskripsi.text = intent.getStringExtra("deskripsi")
    }
}