package com.app.wisatago

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.api.ApiClient
import com.app.wisatago.api.ProfileResponse
import com.app.wisatago.api.UpdateProfileRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
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
    private lateinit var tvPhone: TextView

    // 🟢 Komponen Foto Profil
    private lateinit var imgProfile: ShapeableImageView
    private lateinit var tvChangePhoto: TextView

    // 🟢 Variabel Penyimpan Sementara untuk Update
    private var currentBase64Image: String? = null
    private var currentFullName = ""
    private var currentPhone = ""
    private var userId = ""

    // ==========================================
    // 🟢 MESIN PEMANGGIL GALERI & PEMROSES FOTO
    // ==========================================
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val imageStream: InputStream? = contentResolver.openInputStream(it)
                val selectedImage = BitmapFactory.decodeStream(imageStream)

                // Perkecil gambar agar tidak membuat lag (Maksimal 800px)
                val resizedBitmap = resizeBitmap(selectedImage, 800)

                // Pasang ke UI (Lingkaran Foto)
                imgProfile.setImageBitmap(resizedBitmap)

                // Ubah gambar ke teks Base64 lalu Upload
                currentBase64Image = encodeImageToBase64(resizedBitmap)
                uploadProfilePicture()

            } catch (e: Exception) {
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informasi_akun)

        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserId = findViewById(R.id.tvUserId)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvCreatedAt = findViewById(R.id.tvCreatedAt)
        tvPhone = findViewById(R.id.tvPhone)

        btnBack = findViewById(R.id.btnBack)
        btnCopyId = findViewById(R.id.btnCopyId)
        btnChangePasswordMenu = findViewById(R.id.btnChangePasswordMenu)

        // 🟢 Hubungkan UI Foto Profil
        imgProfile = findViewById(R.id.imgProfile)
        tvChangePhoto = findViewById(R.id.tvChangePhoto)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        userId = sharedPref.getString("USER_ID", "") ?: ""

        if (userId.isNotEmpty()) {
            ambilDataUser(userId)
        } else {
            Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show()
        }

        // ==========================================
        // 🟢 AKSI KLIK FOTO PROFIL (Buka Galeri)
        // ==========================================
        imgProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        tvChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
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

                        // 🟢 Simpan data saat ini untuk proses Update Foto nanti
                        currentFullName = user.full_name
                        currentPhone = user.phone_number ?: ""
                        currentBase64Image = user.profile_picture

                        tvUserId.text = user.user_id
                        tvFullName.text = user.full_name
                        tvUserName.text = user.full_name
                        tvEmail.text = user.email
                        tvUserEmail.text = user.email

                        // Logika Format Telepon
                        val rawPhone = user.phone_number ?: ""
                        if (rawPhone.isNotEmpty()) {
                            val digitsOnly = rawPhone.replace("-", "")
                            val formattedPhone = StringBuilder()
                            for (i in digitsOnly.indices) {
                                formattedPhone.append(digitsOnly[i])
                                if ((i + 1) % 4 == 0 && (i + 1) < digitsOnly.length) {
                                    formattedPhone.append("-")
                                }
                            }
                            tvPhone.text = formattedPhone.toString()
                        } else {
                            val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                            tvPhone.text = sharedPref.getString("USER_PHONE", "-")
                        }

                        // Logika Format Tanggal
                        try {
                            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                            val date = parser.parse(user.created_at)
                            tvCreatedAt.text = formatter.format(date!!)
                        } catch (e: Exception) {
                            tvCreatedAt.text = user.created_at
                        }

                        // 🟢 PERBAIKAN 1: TAMPILKAN FOTO DARI DATABASE DENGAN AMAN
                        if (!user.profile_picture.isNullOrEmpty()) {
                            try {
                                // Hapus embel-embel "data:image..." jika ada agar tidak error saat di-decode
                                val cleanBase64 = user.profile_picture.replace(Regex("^data:image/[a-zA-Z]+;base64,"), "")
                                val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                imgProfile.setImageBitmap(decodedImage)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    } else {
                        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                        tvFullName.text = sharedPref.getString("USERNAME", "-")
                        tvUserName.text = sharedPref.getString("USERNAME", "-")
                        tvPhone.text = sharedPref.getString("USER_PHONE", "-")
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

    // ==========================================
    // 🟢 FUNGSI UPLOAD FOTO KE DATABASE
    // ==========================================
    private fun uploadProfilePicture() {
        Toast.makeText(this, "Menyimpan foto profil...", Toast.LENGTH_SHORT).show()

        // 🟢 PERBAIKAN 2: JARING PENGAMAN DATA KOSONG
        // Pastikan nama dan HP tidak pernah kosong agar lolos validasi Node.js
        if (currentFullName.isEmpty()) {
            val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
            currentFullName = sharedPref.getString("USERNAME", "User WisataGO") ?: "User WisataGO"
        }
        if (currentPhone.isEmpty()) {
            val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
            currentPhone = sharedPref.getString("USER_PHONE", "-") ?: "-"
        }

        val updateRequest = UpdateProfileRequest(
            user_id = userId,
            full_name = currentFullName,
            phone_number = currentPhone,
            profile_picture = currentBase64Image // Kirim sandi gambar utuh tanpa enter
        )

        ApiClient.instance.updateProfile(updateRequest).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@InformasiAkun, "Foto Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@InformasiAkun, "Gagal memperbarui foto di server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Toast.makeText(this@InformasiAkun, "Koneksi Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ==========================================
    // 🟢 FUNGSI ALAT BANTU GAMBAR
    // ==========================================
    private fun resizeBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun encodeImageToBase64(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val b = baos.toByteArray()

        // 🟢 PERBAIKAN 3: GUNAKAN NO_WRAP
        // Ini memastikan kode gambar menjadi satu baris panjang tanpa terpotong tombol "Enter"
        // Sehingga struktur JSON tidak hancur saat terbang ke Node.js
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }
}