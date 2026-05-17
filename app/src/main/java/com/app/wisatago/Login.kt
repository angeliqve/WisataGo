package com.app.wisatago
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val button1: View = findViewById(R.id.rl822png1s6q)
        button1.setOnClickListener {
            println("Pressed")
        }
    }
}