package com.app.wisatago

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.imgLogo)

        logo.alpha = 0f

        logo.animate()
            .alpha(1f)
            .setDuration(1500)
            .start()

        // Handler untuk menahan layar splash selama 4 detik
        Handler(Looper.getMainLooper()).postDelayed({

            // PERBAIKAN UTAMA: Arahkan ke Login::class.java, BUKAN MainActivity
            val intentKeLogin = Intent(this, Login::class.java)
            startActivity(intentKeLogin)

            // Tutup SplashActivity agar tidak bisa kembali ke halaman loading saat tombol back ditekan
            finish()

        }, 4000)
    }
}