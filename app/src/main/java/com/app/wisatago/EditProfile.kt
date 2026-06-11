    package com.app.wisatago

    import android.os.Bundle
    import android.text.Editable
    import android.text.TextWatcher
    import android.widget.ImageView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import com.app.wisatago.api.ApiClient
    import com.app.wisatago.api.ProfileResponse
    import com.app.wisatago.api.UpdateProfileRequest
    import com.google.android.material.button.MaterialButton
    import com.google.android.material.textfield.TextInputEditText
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    // 🟢 okhttp3.ResponseBody sudah dihapus dari sini
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

            etPhoneNumber.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false
                private val hyphen = "-"

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    isUpdating = true

                    var current = s.toString().replace(Regex("[^0-9]"), "")
                    val formatted = StringBuilder()

                    for (i in current.indices) {
                        formatted.append(current[i])
                        if ((i + 1) % 4 == 0 && (i + 1) < current.length) {
                            formatted.append(hyphen)
                        }
                    }

                    etPhoneNumber.setText(formatted.toString())
                    etPhoneNumber.setSelection(formatted.length)
                    isUpdating = false
                }
            })

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

                            var nomorHp = user.phone_number ?: ""

                            if (nomorHp.isEmpty()) {
                                val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                                nomorHp = sharedPref.getString("USER_PHONE", "") ?: ""
                            }

                            etPhoneNumber.setText(nomorHp)

                        } else {
                            val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                            etFullName.setText(sharedPref.getString("USERNAME", ""))
                            etPhoneNumber.setText(sharedPref.getString("USER_PHONE", ""))
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                        etFullName.setText(sharedPref.getString("USERNAME", ""))
                        etPhoneNumber.setText(sharedPref.getString("USER_PHONE", ""))
                    }
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

            val cleanPhone = phoneText.replace("-", "")
            val regexPhone = Regex("^(08|628)[0-9]{8,11}$")

            if (!cleanPhone.matches(regexPhone)) {
                Toast.makeText(this, "Format nomor tidak valid! Harus nomor HP Indonesia (Contoh: 08123456789)", Toast.LENGTH_LONG).show()
                return
            }

            // ==========================================
            // 🟢 PERBAIKAN 1: Gunakan Data Class UpdateProfileRequest
            // ==========================================
            val requestData = UpdateProfileRequest(
                user_id = userId,
                full_name = fullNameText,
                phone_number = phoneText,
                profile_picture = null // Biarkan null, karena pengubahan foto ada di InformasiAkun
            )

            // ==========================================
            // 🟢 PERBAIKAN 2: Ubah ResponseBody menjadi ProfileResponse
            // ==========================================
            ApiClient.instance.updateProfile(requestData).enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                    if (response.isSuccessful) {
                        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("USERNAME", fullNameText)
                            putString("USER_PHONE", phoneText)
                        }.apply()

                        Toast.makeText(this@EditProfile, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditProfile, "Gagal memperbarui profil di server!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Toast.makeText(this@EditProfile, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }