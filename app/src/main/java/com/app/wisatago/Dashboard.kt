package com.app.wisatago // Pastikan nama package sesuai dengan proyek Anda

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.attraction.WisataActivity
import com.app.wisatago.transport.TrainActivity
import android.content.Intent

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. Inisialisasi Teks & Avatar
        val greetingText = findViewById<TextView>(R.id.greetingText)

        // 2. Inisialisasi Tombol Menu Utama
        // INI OBATNYA: Sekarang kita menggunakan LinearLayout, BUKAN MaterialCardView lagi!
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

        wisataButton.setOnClickListener {
            // 🟢 Pindah ke menu Wisata
            // Catatan: Ganti "WisataActivity" jika nama file halaman wisatamu berbeda!
            val intent = Intent(this, WisataActivity::class.java)
            startActivity(intent)
        }

        transportButton.setOnClickListener {
            // 🟢 Pindah ke menu Transportasi (TrainActivity)
            val intent = Intent(this, TrainActivity::class.java)
            startActivity(intent)
        }

        searchBar.setOnClickListener {
            Toast.makeText(this, "Membuka pencarian...", Toast.LENGTH_SHORT).show()
        }

    }
}