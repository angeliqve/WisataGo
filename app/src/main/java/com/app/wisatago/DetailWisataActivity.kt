package com.app.wisatago

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailWisataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_wisata)
        val btnBack = findViewById<ImageView>(R.id.btnBackWisata)
        btnBack.setOnClickListener {
            finish()
        }

        val img = findViewById<ImageView>(R.id.imgWisata)
        val tvNama = findViewById<TextView>(R.id.tvNamaWisata)
        val tvRating = findViewById<TextView>(R.id.tvRating)
        val tvAlamat = findViewById<TextView>(R.id.tvLokasiSingkat)
        val tvHarga = findViewById<TextView>(R.id.tvHarga)
        val tvDeskripsi = findViewById<TextView>(R.id.tvDeskripsi)
        val tvJamBuka = findViewById<TextView>(R.id.tvJamBuka)

        img.setImageResource(intent.getIntExtra("gambar", R.drawable.tour_image_borobudur))
        tvNama.text = intent.getStringExtra("nama")
        tvRating.text = "⭐ ${intent.getStringExtra("rating")}"
        tvAlamat.text = intent.getStringExtra("alamat")
        tvHarga.text = intent.getStringExtra("harga")
        tvDeskripsi.text = intent.getStringExtra("deskripsi")
        tvJamBuka.text = intent.getStringExtra("jam_buka")
    }
}