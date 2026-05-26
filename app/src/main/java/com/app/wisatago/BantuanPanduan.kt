package com.app.wisatago

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class BantuanPanduan : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnCall: LinearLayout
    private lateinit var btnEmail: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bantuan)

        btnBack = findViewById(R.id.btnBack)
        btnCall = findViewById(R.id.btnCall)
        btnEmail = findViewById(R.id.btnEmail)

        btnBack.setOnClickListener {
            finish()
        }

        btnCall.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+6281234567890")

            startActivity(intent)
        }

        btnEmail.setOnClickListener {

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:support@wisatago.com")

            startActivity(intent)
        }
    }
}