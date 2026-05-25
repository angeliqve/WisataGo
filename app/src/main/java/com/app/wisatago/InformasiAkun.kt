package com.app.wisatago

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class InformasiAkun : AppCompatActivity() {

    private lateinit var btnBack: ImageView

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    private lateinit var tvUserId: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvPassword: TextView
    private lateinit var btnShowPassword: ImageView
    private lateinit var tvCreatedAt: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informasi_akun)

        btnBack = findViewById(R.id.btnBack)

        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)

        tvUserId = findViewById(R.id.tvUserId)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvRole = findViewById(R.id.tvRole)
        tvCreatedAt = findViewById(R.id.tvCreatedAt)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            ambilDataUser(userId)
        }

        btnBack.setOnClickListener {
            finish()
        }

        tvPassword = findViewById(R.id.tvPassword)
        btnShowPassword = findViewById(R.id.btnShowPassword)

        btnShowPassword.setOnClickListener {

            if (isPasswordVisible) {

                tvPassword.text = "••••••••"

                isPasswordVisible = false

            } else {

                tvPassword.text = "password123"

                isPasswordVisible = true
            }
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

                        tvRole.text = user.role ?: "user"

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