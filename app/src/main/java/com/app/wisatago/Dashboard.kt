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
        val btnProfile = findViewById<ImageView>(R.id.btnProfile)

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

        btnProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }
}