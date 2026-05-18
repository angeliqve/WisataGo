package com.app.wisatago

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView // Wajib di-import untuk desain modern

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. Inisialisasi View berdasarkan ID di XML terbaru
        val greetingText = findViewById<TextView>(R.id.greetingText)

        // Perubahan Tipe Komponen: Sekarang menggunakan MaterialCardView
        val wisataButton = findViewById<MaterialCardView>(R.id.wisataButton)
        val transportButton = findViewById<MaterialCardView>(R.id.transportButton)

        // Kolom pencarian sekarang menggunakan LinearLayout utama
        val searchBar = findViewById<LinearLayout>(R.id.searchBarContainer)

        // 2. Menangkap data username yang dikirim dari halaman Login
        val username = intent.getStringExtra("USERNAME_KEY")

        // 3. Mengatur teks sapaan dinamis (Disesuaikan dengan kalimat di desain XML)
        if (!username.isNullOrEmpty()) {
            greetingText.text = "Halo, $username! Mau liburan ke mana?"
        } else {
            // Nilai default jika gagal memuat nama
            greetingText.text = "Halo, User! Mau liburan ke mana?"
        }

        // ==========================================
        // 4. MEMBERIKAN AKSI KLIK (ON CLICK LISTENER)
        // ==========================================

        // Aksi klik Menu Wisata
        wisataButton.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Wisata...", Toast.LENGTH_SHORT).show()
            // TODO: Tambahkan Intent ke halaman daftar Wisata di sini
        }

        // Aksi klik Menu Transportasi
        transportButton.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Transportasi...", Toast.LENGTH_SHORT).show()
            // TODO: Tambahkan Intent ke halaman pemesanan Transportasi di sini
        }

        // Aksi klik Kolom Pencarian (Search Bar)
        searchBar.setOnClickListener {
            Toast.makeText(this, "Membuka fitur pencarian...", Toast.LENGTH_SHORT).show()
            // TODO: Tambahkan logika pencarian destinasi di sini
        }

        // ==========================================
        // BONUS: Aksi Klik untuk Filter Transportasi (Bentuk Kapsul)
        // ==========================================
        val btnKereta = findViewById<TextView>(R.id.keretaFilterButton)
        val btnPesawat = findViewById<TextView>(R.id.pesawatFilterButton)
        val btnBus = findViewById<TextView>(R.id.busFilterButton)

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