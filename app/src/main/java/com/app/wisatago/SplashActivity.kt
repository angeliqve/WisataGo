package com.app.wisatago

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoSplash = findViewById<VideoView>(R.id.videoSplash)

        val videoPath = "android.resource://" + packageName + "/" + R.raw.splash_video
        val uri = Uri.parse(videoPath)
        videoSplash.setVideoURI(uri)

        // 🟢 PERBAIKAN: Trik untuk membuat video penuh (Center Crop)
        videoSplash.setOnPreparedListener { mp ->
            val videoRatio = mp.videoWidth.toFloat() / mp.videoHeight.toFloat()
            val screenRatio = videoSplash.width.toFloat() / videoSplash.height.toFloat()
            val scale = videoRatio / screenRatio

            // Lakukan zoom pada video agar menyentuh seluruh ujung layar
            if (scale >= 1f) {
                videoSplash.scaleX = scale
                videoSplash.scaleY = scale
            } else {
                videoSplash.scaleX = 1f / scale
                videoSplash.scaleY = 1f / scale
            }

            // Mulai putar video setelah ukurannya dipaskan
            mp.start()
        }

        videoSplash.setOnCompletionListener {
            val intentKeLogin = Intent(this, Login::class.java)
            startActivity(intentKeLogin)
            finish()
        }
    }
}