package com.app.wisatago // Pastikan nama package sesuai dengan proyek Anda

import android.content.Intent // 🟢 TAMBAHAN: Wajib di-import untuk fitur perpindahan halaman
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. Inisialisasi Teks & Avatar
        val greetingText = findViewById<TextView>(R.id.greetingText)

        // 2. Inisialisasi Tombol Menu Utama
        val wisataButton = findViewById<LinearLayout>(R.id.wisataButton)
        val transportButton = findViewById<LinearLayout>(R.id.transportButton)

        // 3. Inisialisasi Search Bar
        val searchBar = findViewById<LinearLayout>(R.id.searchBarContainer)

        // ==========================================
        // LOGIKA PENYAPAAN USER (Dari Login)
        // ==========================================
        val username = intent.getStringExtra("USERNAME_KEY")

        if (!username.isNullOrEmpty()) {
            greetingText.text = "Halo, $username! Mau liburan ke mana?"
        } else {
            greetingText.text = "Halo, User! Mau liburan ke mana?"
        }

        // ==========================================
        // AKSI KLIK (ON CLICK LISTENER)
        // ==========================================

        // 🟢 PERBAIKAN UTAMA: Mengarahkan tombol Wisata ke WisataActivity
        wisataButton.setOnClickListener {
            val intent = Intent(this, WisataActivity::class.java)
            startActivity(intent)
        }

        transportButton.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Transportasi...", Toast.LENGTH_SHORT).show()
        }

        searchBar.setOnClickListener {
            Toast.makeText(this, "Membuka pencarian...", Toast.LENGTH_SHORT).show()
        }

        // ==========================================
        // AKSI KLIK TOMBOL KAPSUL TRANSPORTASI
        // ==========================================
        val btnKereta = findViewById<TextView>(R.id.keretaFilterButton)
        val btnPesawat = findViewById<TextView>(R.id.pesawatFilterButton)
        val btnBus = findViewById<TextView>(R.id.busFilterButton)

        btnKereta.setOnClickListener {
            Toast.makeText(this, "Menampilkan tiket Kereta", Toast.LENGTH_SHORT).show()
        }
        btnPesawat.setOnClickListener {
            Toast.makeText(this, "Menampilkan tiket Pesawat", Toast.LENGTH_SHORT).show()
        }
        btnBus.setOnClickListener {
            Toast.makeText(this, "Menampilkan tiket Bus", Toast.LENGTH_SHORT).show()
        }
    }
}