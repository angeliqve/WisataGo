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

        // 1. Sinkronisasi dengan ID yang ada di activity_sign_up.xml
        val etNama = findViewById<EditText>(R.id.etNamaDaftar)
        val etEmail = findViewById<EditText>(R.id.etEmailDaftar)
        val etPassword = findViewById<EditText>(R.id.etPasswordDaftar)
        val etNoTelepon = findViewById<EditText>(R.id.etNoTeleponDaftar) // Kolom nomor telepon
        val btnDaftar = findViewById<LinearLayout>(R.id.btnTombolDaftar)
        val tvSudahPunyaAkun = findViewById<TextView>(R.id.tvSudahPunyaAkun)

        // 2. Logika ketika tombol "Daftar" diklik
        btnDaftar.setOnClickListener {
            val namaInput = etNama.text.toString().trim()
            val emailInput = etEmail.text.toString().trim()
            val passInput = etPassword.text.toString().trim()
            val noTeleponInput = etNoTelepon.text.toString().trim()

            // Validasi field kosong
            if (namaInput.isEmpty() || emailInput.isEmpty() || passInput.isEmpty() || noTeleponInput.isEmpty()) {
                Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Bersihkan nomor telepon dari karakter non-digit (spasi, tanda hubung, dll)
            val cleanedPhone = noTeleponInput.replace(Regex("[^0-9]"), "")

            val validasiNoTelp = validasiNomorTelepon(cleanedPhone)
            if (!validasiNoTelp.isValid) {
                Toast.makeText(this, validasiNoTelp.pesan, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Mendaftarkan akun...", Toast.LENGTH_SHORT).show()
            jalankanFungsiSignUp(namaInput, emailInput, passInput, cleanedPhone)
        }

        // 3. Logika ketika teks "Sudah Punya Akun? Log In" diklik
        tvSudahPunyaAkun.setOnClickListener {
            finish() // Menutup halaman Sign Up dan otomatis kembali ke halaman Login
        }
    }

    private fun validasiNomorTelepon(nomorTelepon: String): ValidasiTeleponResult {
        // Regex untuk nomor HP Indonesia: diawali 08 atau 628, diikuti 8-11 digit angka
        val regexPhone = Regex("^(08|628)[0-9]{8,11}$")

        return if (nomorTelepon.matches(regexPhone)) {
            ValidasiTeleponResult(true, "Nomor telepon valid")
        } else {
            ValidasiTeleponResult(false, "Format nomor tidak valid! Harus nomor HP Indonesia (Contoh: 08123456789 atau 628123456789)")
        }
    }

    private data class ValidasiTeleponResult(
        val isValid: Boolean,
        val pesan: String
    )

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
                        Toast.makeText(this@SignUp, "Gagal Daftar! Email atau nama mungkin sudah digunakan.", Toast.LENGTH_SHORT).show()
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