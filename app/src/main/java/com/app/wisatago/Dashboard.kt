package com.app.wisatago

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.attraction.WisataActivity
import com.app.wisatago.transport.TrainActivity
import android.content.Intent
import android.widget.ImageView

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val greetingText = findViewById<TextView>(R.id.greetingText)
        val wisataButton = findViewById<LinearLayout>(R.id.wisataButton)
        val transportButton = findViewById<LinearLayout>(R.id.transportButton)
        val searchBar = findViewById<LinearLayout>(R.id.searchBarContainer)

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

        searchBar.setOnClickListener {
            Toast.makeText(this, "Membuka pencarian...", Toast.LENGTH_SHORT).show()
        }

        btnHome.setOnClickListener {
            Toast.makeText(this, "Anda sudah di Home", Toast.LENGTH_SHORT).show()
        }

        btnPemesanan.setOnClickListener {
            Toast.makeText(this, "Halaman Pemesanan", Toast.LENGTH_SHORT).show()
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}