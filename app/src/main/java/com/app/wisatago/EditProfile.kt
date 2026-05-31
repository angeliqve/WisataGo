package com.app.wisatago

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfile : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var etFullName: TextInputEditText
    private lateinit var etPhoneNumber: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        btnBack = findViewById(R.id.btnBack)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        etFullName = findViewById(R.id.etFullName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            muatDataLamaUser(userId)
        } else {
            Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSaveProfile.setOnClickListener {
            if (userId != null) {
                prosesUpdateProfile(userId)
            }
        }
    }

    private fun muatDataLamaUser(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.instance.getProfile(userId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!

                        etFullName.setText(user.full_name)

                        etPhoneNumber.setText(user.phone_number ?: "")
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun prosesUpdateProfile(userId: String) {
        val fullNameText = etFullName.text.toString().trim()
        val phoneText = etPhoneNumber.text.toString().trim()

        if (fullNameText.isEmpty() || phoneText.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val requestData = hashMapOf(
            "user_id" to userId,
            "full_name" to fullNameText,
            "phone_number" to phoneText
        )

        ApiClient.instance.updateProfile(requestData).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                    sharedPref.edit().putString("USERNAME", fullNameText).apply()

                    Toast.makeText(this@EditProfile, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()

                    finish()
                } else {
                    Toast.makeText(this@EditProfile, "Gagal memperbarui profil!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@EditProfile, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}