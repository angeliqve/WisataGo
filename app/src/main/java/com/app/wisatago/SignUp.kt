package com.app.wisatago

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // 1. Sinkronisasi dengan ID yang ada di activity_sign_up.xml Anda
        val etNama = findViewById<EditText>(R.id.etNamaDaftar)
        val etEmail = findViewById<EditText>(R.id.etEmailDaftar)
        val etPassword = findViewById<EditText>(R.id.etPasswordDaftar)
        val btnDaftar = findViewById<LinearLayout>(R.id.btnTombolDaftar)
        val tvSudahPunyaAkun = findViewById<TextView>(R.id.tvSudahPunyaAkun)

        // 2. Logika ketika tombol "Daftar" diklik
        btnDaftar.setOnClickListener {
            val namaInput = etNama.text.toString().trim()
            val emailInput = etEmail.text.toString().trim()
            val passInput = etPassword.text.toString().trim()

            // Karena di desain XML tidak ada kolom nomor HP, kita isi default dengan tanda strip (-)
            val nomorHpDefault = "-"

            if (namaInput.isEmpty() || emailInput.isEmpty() || passInput.isEmpty()) {
                Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Mendaftarkan akun...", Toast.LENGTH_SHORT).show()
            jalankanFungsiSignUp(namaInput, emailInput, passInput, nomorHpDefault)
        }

        // 3. Logika ketika teks "Sudah Punya Akun? Log In" diklik
        tvSudahPunyaAkun.setOnClickListener {
            finish() // Menutup halaman Sign Up dan otomatis kembali ke halaman Login
        }
    }

    // Fungsi pengiriman data pendaftaran menggunakan Retrofit
    private fun jalankanFungsiSignUp(nama: String, email: String, pass: String, phone: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Bungkus data input ke dalam model Request
                val dataRequest = SignUpRequest(nama, email, pass, phone)

                // Tembak data ke Node.js melalui ApiClient
                val respon = ApiClient.instance.prosesDaftarUser(dataRequest)

                withContext(Dispatchers.Main) {
                    if (respon.isSuccessful && respon.body() != null) {
                        Toast.makeText(this@SignUp, "Pendaftaran Berhasil! Silakan Login", Toast.LENGTH_LONG).show()

                        // Menutup halaman Sign Up agar pengguna langsung kembali ke layar Login
                        finish()
                    } else {
                        // Respon gagal dari server (misal email sudah terdaftar)
                        Toast.makeText(this@SignUp, "Gagal Daftar! Email mungkin sudah digunakan.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Terjadi masalah jaringan atau server Node.js mati
                    Toast.makeText(this@SignUp, "Gagal terhubung ke server backend", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}