package com.app.wisatago

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.attraction.WisataActivity
import com.app.wisatago.transport.TrainActivity

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val greetingText = findViewById<TextView>(R.id.greetingText)
        val wisataButton = findViewById<LinearLayout>(R.id.wisataButton)
        val transportButton = findViewById<LinearLayout>(R.id.transportButton)
        val searchBar = findViewById<LinearLayout>(R.id.searchBarContainer)

        val username = intent.getStringExtra("USERNAME_KEY")

        if (!username.isNullOrEmpty()) {
            greetingText.text = "Halo, $username! Mau liburan ke mana?"
        } else {
            greetingText.text = "Halo, User! Mau liburan ke mana?"
        }

        wisataButton.setOnClickListener {
            val intent = Intent(this, WisataActivity::class.java)
            startActivity(intent)
        }

        transportButton.setOnClickListener {
            val intent = Intent(this, TrainActivity::class.java)
            startActivity(intent)
        }

        searchBar.setOnClickListener {
            Toast.makeText(this, "Membuka pencarian...", Toast.LENGTH_SHORT).show()
        }

        val btnKereta = findViewById<TextView>(R.id.keretaFilterButton)
        val btnPesawat = findViewById<TextView>(R.id.pesawatFilterButton)
        val btnBus = findViewById<TextView>(R.id.busFilterButton)

        btnKereta.setOnClickListener {
            val intent = Intent(this, TrainActivity::class.java)
            startActivity(intent)
        }

        btnPesawat.setOnClickListener {
            Toast.makeText(this, "Menampilkan tiket Pesawat", Toast.LENGTH_SHORT).show()
        }

        btnBus.setOnClickListener {
            Toast.makeText(this, "Menampilkan tiket Bus", Toast.LENGTH_SHORT).show()
        }
    }
}