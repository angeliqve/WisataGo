package com.app.wisatago

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class InformasiAkun : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnCopyId: ImageView
    private lateinit var btnChangePasswordMenu: MaterialButton

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    private lateinit var tvUserId: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvCreatedAt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informasi_akun)

        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserId = findViewById(R.id.tvUserId)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvCreatedAt = findViewById(R.id.tvCreatedAt)

        btnBack = findViewById(R.id.btnBack)
        btnCopyId = findViewById(R.id.btnCopyId)
        btnChangePasswordMenu = findViewById(R.id.btnChangePasswordMenu)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            ambilDataUser(userId)
        } else {
            Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnCopyId.setOnClickListener {
            val textToCopy = tvUserId.text.toString()
            if (textToCopy.isNotEmpty() && textToCopy != "UUID USER") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("User ID WisataGO", textToCopy)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "User ID berhasil disalin!", Toast.LENGTH_SHORT).show()
            }
        }

        btnChangePasswordMenu.setOnClickListener {
            val intent = Intent(this, UbahPassword::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun ambilDataUser(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.instance.getProfile(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!

                        tvUserId.text = user.user_id.toString()
                        tvFullName.text = user.full_name
                        tvUserName.text = user.full_name

                        tvEmail.text = user.email
                        tvUserEmail.text = user.email

                        try {
                            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

                            val date = parser.parse(user.created_at)
                            tvCreatedAt.text = formatter.format(date!!)

                        } catch (e: Exception) {
                            tvCreatedAt.text = user.created_at
                        }

                    } else {
                        Toast.makeText(
                            this@InformasiAkun,
                            "Gagal mengambil data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@InformasiAkun,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}