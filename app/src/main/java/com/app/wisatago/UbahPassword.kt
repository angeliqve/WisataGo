package com.app.wisatago

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UbahPassword : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnSavePassword: MaterialButton
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubah_password)

        btnBack = findViewById(R.id.btnBack)
        btnSavePassword = findViewById(R.id.btnSavePassword)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnBack.setOnClickListener {
            finish()
        }

        btnSavePassword.setOnClickListener {
            prosesUbahPassword()
        }
    }

    private fun prosesUbahPassword() {
        val currentPasswordText = etCurrentPassword.text.toString().trim()
        val newPasswordText = etNewPassword.text.toString().trim()
        val confirmPasswordText = etConfirmPassword.text.toString().trim()

        if (currentPasswordText.isEmpty() || newPasswordText.isEmpty() || confirmPasswordText.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPasswordText.length < 6) {
            Toast.makeText(this, "Password baru minimal 6 karakter!", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPasswordText != confirmPasswordText) {
            Toast.makeText(this, "Konfirmasi password baru tidak cocok!", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId == null) {
            Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show()
            return
        }

        val requestData = hashMapOf(
            "user_id" to userId,
            "current_password" to currentPasswordText,
            "new_password" to newPasswordText
        )

        ApiClient.instance.updatePassword(requestData).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UbahPassword, "Password berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@UbahPassword, "Gagal: Password saat ini salah!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UbahPassword, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}