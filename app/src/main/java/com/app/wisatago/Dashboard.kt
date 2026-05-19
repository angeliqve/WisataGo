package com.app.wisatago

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. Inisialisasi View berdasarkan ID di XML terbaru
        val greetingText = findViewById<TextView>(R.id.greetingText)

        // 🟢 PERBAIKAN: Disesuaikan dengan XML kamu yang menggunakan LinearLayout
        val wisataButton = findViewById<LinearLayout>(R.id.wisataButton)
        val transportButton = findViewById<LinearLayout>(R.id.transportButton)
        val searchBar = findViewById<LinearLayout>(R.id.searchBarContainer)

        // 2. Menangkap data username yang dikirim dari halaman Login
        val username = intent.getStringExtra("USERNAME_KEY")

        // 3. Mengatur teks sapaan dinamis
        if (!username.isNullOrEmpty()) {
            greetingText.text = "Halo, $username! Mau liburan ke mana?"
        } else {
            greetingText.text = "Halo, User! Mau liburan ke mana?"
        }

        // ==========================================
        // 4. MEMBERIKAN AKSI KLIK (ON CLICK LISTENER)
        // ==========================================

        // Aksi klik Menu Wisata
        wisataButton.setOnClickListener {
            val intent = Intent(this, WisataActivity::class.java)
            startActivity(intent)
        }

        // Aksi klik Menu Transportasi
        transportButton.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Transportasi...", Toast.LENGTH_SHORT).show()
        }

        // Aksi klik Kolom Pencarian (Search Bar)
        searchBar.setOnClickListener {
            Toast.makeText(this, "Membuka fitur pencarian...", Toast.LENGTH_SHORT).show()
        }

        // ==========================================
        // 5. PERBAIKAN: Filter Transportasi (Bentuk Kapsul di XML adalah LinearLayout)
        // ==========================================
        val btnKereta = findViewById<LinearLayout>(R.id.keretaFilterButton)
        val btnPesawat = findViewById<LinearLayout>(R.id.pesawatFilterButton)
        val btnBus = findViewById<LinearLayout>(R.id.busFilterButton)

        btnKereta.setOnClickListener {
            Toast.makeText(this, "Menampilkan tiket Kereta Api", Toast.LENGTH_SHORT).show()
        }
        btnPesawat.setOnClickListener {
            Toast.makeText(this, "Menampilkan tiket Pesawat", Toast.LENGTH_SHORT).show()
        }
        btnBus.setOnClickListener {
            Toast.makeText(this, "Menampilkan tiket Bus", Toast.LENGTH_SHORT).show()
        }
    }
}