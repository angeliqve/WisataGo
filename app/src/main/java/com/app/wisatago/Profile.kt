package com.app.wisatago

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Profile : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    private lateinit var btnHome: ImageView
    private lateinit var btnPemesanan: ImageView
    private lateinit var btnLogout: LinearLayout

    private lateinit var btnInformasiAkun: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)

        btnHome = findViewById(R.id.btnHome)
        btnPemesanan = findViewById(R.id.btnPemesanan)
        btnLogout = findViewById(R.id.btnLogout)
        btnInformasiAkun = findViewById(R.id.btnInformasiAkun)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            ambilDataProfile(userId)
        } else {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

        btnPemesanan.setOnClickListener {
            Toast.makeText(this, "Halaman Pemesanan", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin logout dari akun?")
                .setPositiveButton("Ya") { _, _ ->

                    sharedPref.edit().clear().apply()

                    Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        val btnInformasiAkun = findViewById<LinearLayout>(R.id.btnInformasiAkun)

        btnInformasiAkun.setOnClickListener {
            startActivity(Intent(this, InformasiAkun::class.java))
        }
    }

    private fun ambilDataProfile(userId: String) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response = ApiClient.instance.getProfile(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {

                        val user = response.body()!!

                        tvUserName.text = user.full_name
                        tvUserEmail.text = user.email

                    } else {

                        Toast.makeText(
                            this@Profile,
                            "Gagal: ${response.errorBody()?.string()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(
                        this@Profile,
                        "Server error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}