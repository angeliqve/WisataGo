package com.app.wisatago

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Profile : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnInformasiAkun: LinearLayout
    private lateinit var btnBantuan: LinearLayout
    private lateinit var btnLogout: LinearLayout
    private lateinit var switchSuara: SwitchCompat
    private lateinit var switchNotifikasi: SwitchCompat
    private lateinit var btnHome: ImageView
    private lateinit var btnPemesanan: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnInformasiAkun = findViewById(R.id.btnInformasiAkun)
        btnBantuan = findViewById(R.id.btnBantuan)
        btnLogout = findViewById(R.id.btnLogout)
        switchSuara = findViewById(R.id.switchSuara)
        switchNotifikasi = findViewById(R.id.switchNotifikasi)

        btnHome = findViewById(R.id.btnHome)
        btnPemesanan = findViewById(R.id.btnPemesanan)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            ambilDataProfile(userId)
        } else {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        btnPemesanan.setOnClickListener {
            Toast.makeText(this, "Halaman Pemesanan", Toast.LENGTH_SHORT).show()
        }

        btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Halaman Edit Profil", Toast.LENGTH_SHORT).show()
        }

        btnInformasiAkun.setOnClickListener {
            startActivity(Intent(this, InformasiAkun::class.java))
        }

        btnBantuan.setOnClickListener {
            startActivity(Intent(this, BantuanPanduan::class.java))
        }

        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        switchSuara.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "Aktif" else "Nonaktif"
            Toast.makeText(this, "Suara $status", Toast.LENGTH_SHORT).show()
        }

        switchNotifikasi.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "Aktif" else "Nonaktif"
            Toast.makeText(this, "Notifikasi $status", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin logout dari akun?")
                .setPositiveButton("Ya") { _, _ ->
                    sharedPref.edit().clear().apply()
                    Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
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
                        Toast.makeText(this@Profile, "Gagal ambil data: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Profile, "Server error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}