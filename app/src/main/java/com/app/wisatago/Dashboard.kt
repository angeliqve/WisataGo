package com.app.wisatago // Nama package sudah disesuaikan dengan file Login

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Pastikan nama ini sesuai dengan nama file XML kamu (misal: activity_dashboard)
        setContentView(R.layout.activity_dashboard)

        // 1. Inisialisasi View berdasarkan ID di XML
        val greetingText: TextView = findViewById(R.id.greetingText)

        // 2. Menangkap data username yang dikirim dari halaman Login
        // Menggunakan kunci "USERNAME_KEY" yang dikirim dari Login
        val username = intent.getStringExtra("USERNAME_KEY")

        // 3. Mengatur teks sapaan dinamis
        if (!username.isNullOrEmpty()) {
            greetingText.text = "Halo, $username!"
        } else {
            // Nilai default jika karena suatu hal username gagal dikirim
            greetingText.text = "Halo, User!"
        }

        // ==========================================
        // CONTOH INISIALISASI TOMBOL LAIN SESUAI XML
        // ==========================================

        val wisataButton: LinearLayout = findViewById(R.id.wisataButton)
        val transportButton: LinearLayout = findViewById(R.id.transportButton)
        val searchButton: FrameLayout = findViewById(R.id.searchIconContainer)

        // Memberikan aksi klik pada tombol Wisata
        wisataButton.setOnClickListener {
            Toast.makeText(this, "Menu Wisata Diklik", Toast.LENGTH_SHORT).show()
            // Intent ke halaman Wisata di sini
        }

        // Memberikan aksi klik pada tombol Transportasi
        transportButton.setOnClickListener {
            Toast.makeText(this, "Menu Transportasi Diklik", Toast.LENGTH_SHORT).show()
            // Intent ke halaman Transportasi di sini
        }

        // Memberikan aksi klik pada tombol Search
        searchButton.setOnClickListener {
            Toast.makeText(this, "Membuka pencarian...", Toast.LENGTH_SHORT).show()
            // Aksi pencarian di sini
        }
    }
}