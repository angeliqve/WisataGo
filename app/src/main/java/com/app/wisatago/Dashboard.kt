package com.app.wisatago

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.attraction.WisataActivity
import com.app.wisatago.transport.TrainActivity
import com.google.android.material.card.MaterialCardView

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val greetingText = findViewById<TextView>(R.id.greetingText)
        val wisataButton = findViewById<LinearLayout>(R.id.wisataButton)
        val transportButton = findViewById<LinearLayout>(R.id.transportButton)

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        val btnPemesanan = findViewById<ImageView>(R.id.btnPemesanan)
        val btnProfile = findViewById<ImageView>(R.id.btnProfile)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", null) ?: intent.getStringExtra("USERNAME_KEY")

        if (!username.isNullOrEmpty()) {
            greetingText.text = "Halo, $username! Mau liburan ke mana?"
            if (sharedPref.getString("USERNAME", null) == null) {
                sharedPref.edit().putString("USERNAME", username).apply()
            }
        } else {
            greetingText.text = "Halo, User! Mau liburan ke mana?"
        }

        wisataButton.setOnClickListener {
            startActivity(Intent(this, WisataActivity::class.java))
        }

        transportButton.setOnClickListener {
            startActivity(Intent(this, TrainActivity::class.java))
        }

        btnHome.setOnClickListener {
            Toast.makeText(this, "Anda sudah di Home", Toast.LENGTH_SHORT).show()
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        btnPemesanan.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // Card Wisata
        findViewById<MaterialCardView>(R.id.tourCard1).setOnClickListener {
            val intent = Intent(this, DetailWisataActivity::class.java)
            intent.putExtra("nama", "Pantai Kuta - Bali")
            intent.putExtra("rating", "4.8")
            intent.putExtra("alamat", "Badung, Bali")
            intent.putExtra("deskripsi", "Pantai Kuta dikenal secara luas sebagai destinasi favorit bagi wisatawan " +
                    "mancanegara dan telah menjadi salah satu obyek wisata unggulan di Pulau Dewata sejak awal tahun 1970-an. " +
                    "Berhadapan langsung dengan Samudra Hindia, Pantai Kuta menawarkan panorama sunset yang sangat memukau dan " +
                    "menjadi kebalikan dari Pantai Sanur yang dikenal sebagai pantai sunrise. Bersamaan dengan pasir putih lembut" +
                    " dan ombak landai sehingga kondusif untuk kegiatan rekreasi keluarga dan surfing pemula.")
            intent.putExtra("harga", "Gratis")
            intent.putExtra("jam_buka", "24 jam")
            intent.putExtra("gambar", R.drawable.tour_image_pantai)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.tourCard2).setOnClickListener {
            val intent = Intent(this, DetailWisataActivity::class.java)
            intent.putExtra("nama", "Candi Borobudur")
            intent.putExtra("rating", "4.8")
            intent.putExtra("alamat", "Magelang, Jawa Tengah")
            intent.putExtra("deskripsi", "Candi Borobudur  adalah salah satu situs warisan dunia UNESCO yang paling " +
                    "terkenal di dunia. Terletak di Kabupaten Magelang, Jawa Tengah, Indonesia, candi ini merupakan candi Buddha terbesar " +
                    "di dunia dan menjadi salah satu tujuan wisata paling populer di Asia Tenggara.")
            intent.putExtra("harga", "Rp 50.000")
            intent.putExtra("jam_buka", "06:30 - 17:00 WIB")
            intent.putExtra("gambar", R.drawable.tour_image_borobudur)
            startActivity(intent)
        }

        // Card Transportasi
        findViewById<MaterialCardView>(R.id.transportasiCard1).setOnClickListener {
            val intent = Intent(this, DetailTransportActivity::class.java)
            intent.putExtra("jenis", "Kereta")
            intent.putExtra("nama", "KA Anggrek")
            intent.putExtra("rating", "4.8")
            intent.putExtra("rute", "Gambir - Surabaya Pasar Turi")
            intent.putExtra("deskripsi", "Kereta Api (KA) Anggrek adalah layanan kereta api penumpang antarkota kelas eksekutif " +
                    "dan compartment suite tertinggi yang dioperasikan oleh Kereta Api Indonesia dengan relasi Surabaya Pasarturi–Gambir melalui lintas utara Jawa.")
            intent.putExtra("gambar", R.drawable.ic_anggrek)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.transportasiCard2).setOnClickListener {
            val intent = Intent(this, DetailTransportActivity::class.java)
            intent.putExtra("jenis", "Pesawat")
            intent.putExtra("nama", "Garuda Indonesia")
            intent.putExtra("rating", "4.9")
            intent.putExtra("rute", "Jakarta (CGK) - Denpasar (DPS)")
            intent.putExtra("deskripsi", "PT Garuda Indonesia (Persero) Tbk adalah maskapai penerbangan nasional Indonesia yang melayani lebih dari 90 destinasi " +
                    "domestik dan internasional. Sejak beroperasi pada 26 Januari 1949, Maskapai ini menghadirkan layanan penerbangan kelas dunia dengan mengedepankan keramahtamahan khas Indonesia.")
            intent.putExtra("gambar", R.drawable.ic_garuda)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.transportasiCard3).setOnClickListener {
            val intent = Intent(this, DetailTransportActivity::class.java)
            intent.putExtra("jenis", "Bus")
            intent.putExtra("nama", "Rosalia Indah")
            intent.putExtra("rating", "4.9")
            intent.putExtra("rute", "Jakarta - Surabaya")
            intent.putExtra("deskripsi", "Bus Rosalia Indah adalah salah satu perusahaan otobus (PO) terbesar di Indonesia yang berpusat di Karanganyar, Jawa Tengah. " +
                    "Terkenal dengan rekor MURI armada Double Decker terbanyak, PO ini mengutamakan kenyamanan dan keselamatan melalui fasilitas premium, " +
                    "layanan pramugara/i, dan servis makan di restoran eksklusif.")
            intent.putExtra("gambar", R.drawable.ic_bus_rosalia)
            startActivity(intent)
        }
    }
}