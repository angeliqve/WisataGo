package com.app.wisatago

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Sinkronisasi ID XML dengan Objek Kotlin
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<LinearLayout>(R.id.rl822png1s6q)

        // TARGET: Langsung menembak ke TextView target di activity_login.xml
        val tvSignUpDirect = findViewById<TextView>(R.id.rmbtfuctunlg)

        // 2. Logika eksekusi Otentikasi Masuk
        btnLogin.setOnClickListener {
            val emailInput = etUsername.text.toString().trim()
            val passwordInput = etPassword.text.toString().trim()

            if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            jalankanFungsiLogin(emailInput, passwordInput)
        }

        // 3. Logika Transisi Halaman ke Sign Up
        tvSignUpDirect.setOnClickListener {
            Log.d("WisataGO_Klik", "SISTEM MENDETEKSI: TextView Berhasil Disentuh!")
            Toast.makeText(this, "Membuka halaman pendaftaran...", Toast.LENGTH_SHORT).show()

            try {
                val intent = Intent(this, SignUp::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("WisataGO_Klik", "GAGAL KARENA: ${e.message}")
                Toast.makeText(this, "Error SignUp: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun jalankanFungsiLogin(email: String, pass: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataRequest = LoginRequest(email, pass)
                val respon = ApiClient.instance.prosesLoginUser(dataRequest)

                withContext(Dispatchers.Main) {
                    if (respon.isSuccessful && respon.body() != null) {
                        val hasilData = respon.body()!!

                        Toast.makeText(this@Login, "Selamat datang, ${hasilData.full_name}!", Toast.LENGTH_SHORT).show()

                        // --- BAGIAN YANG DITAMBAHKAN (Koneksi ke Dashboard) ---
                        val intent = Intent(this@Login, Dashboard::class.java)
                        // Membawa nama pengguna ke DashboardActivity
                        intent.putExtra("USERNAME_KEY", hasilData.full_name)
                        startActivity(intent)
                        finish() // Menutup halaman login agar user tidak bisa kembali dengan tombol back
                        // -------------------------------------------------------

                    } else {
                        Toast.makeText(this@Login, "Login Gagal! Periksa akun kembali.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Login, "Gagal terhubung ke server backend", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}