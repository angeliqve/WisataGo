package com.app.wisatago

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.wisatago.api.ApiClient
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class Profile : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnInformasiAkun: LinearLayout
    private lateinit var btnBantuan: LinearLayout
    private lateinit var btnLogout: LinearLayout
    private lateinit var switchSuara: SwitchCompat
    private lateinit var switchNotifikasi: SwitchCompat
    private lateinit var btnHome: ImageView
    private lateinit var btnPemesanan: ImageView

    private lateinit var imgProfilePic: ImageView

    // ==========================================
    // 🟢 1. MESIN PEMINTA IZIN NOTIFIKASI (UNTUK ANDROID 13+)
    // ==========================================
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            munculkanNotifikasiWisataGO()
            Toast.makeText(this, "Notifikasi Diaktifkan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show()
            switchNotifikasi.isChecked = false
            val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
            sharedPref.edit().putBoolean("SETTING_NOTIF", false).apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnInformasiAkun = findViewById(R.id.btnInformasiAkun)
        btnBantuan = findViewById(R.id.btnBantuan)
        btnLogout = findViewById(R.id.btnLogout)
        switchSuara = findViewById(R.id.switchSuara)
        switchNotifikasi = findViewById(R.id.switchNotifikasi)

        btnHome = findViewById(R.id.btnHome)
        btnPemesanan = findViewById(R.id.btnPemesanan)
        imgProfilePic = findViewById(R.id.imgProfile)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        switchSuara.setOnCheckedChangeListener(null)
        switchNotifikasi.setOnCheckedChangeListener(null)

        switchSuara.isChecked = sharedPref.getBoolean("SETTING_SUARA", false)
        switchNotifikasi.isChecked = sharedPref.getBoolean("SETTING_NOTIF", false)

        // ==========================================
        // 🟢 AKSI TOMBOL SUARA & EFEK (Suara Bawaan HP)
        // ==========================================
        switchSuara.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("SETTING_SUARA", isChecked).apply()

            if (isChecked) {
                try {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
                    ringtone.play()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Toast.makeText(this, "Suara & Efek Diaktifkan", Toast.LENGTH_SHORT).show()
            }
        }

        // ==========================================
        // 🟢 AKSI TOMBOL NOTIFIKASI (Aman dari Blokir Android 13)
        // ==========================================
        switchNotifikasi.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("SETTING_NOTIF", isChecked).apply()

            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                            munculkanNotifikasiWisataGO()
                            Toast.makeText(this, "Notifikasi Diaktifkan", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    munculkanNotifikasiWisataGO()
                    Toast.makeText(this, "Notifikasi Diaktifkan", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // --- Navigasi ---
        btnHome.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        btnPemesanan.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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

    // ==========================================
    // 🟢 FUNGSI PEMBUAT BANYAK NOTIFIKASI DENGAN JEDA
    // ==========================================
    private fun munculkanNotifikasiWisataGO() {
        val channelId = "wisatago_notif_beruntun_07"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Mengambil MP3 dari res/raw
        val customSoundUri = android.net.Uri.parse(
            android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.suara_wisatago
        )

        val intent = Intent(this, Dashboard::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // Membuat Saluran Notifikasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Promo & Update WisataGO"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(customSoundUri, audioAttributes)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // ==========================================
        // 🟢 DAFTAR PESAN YANG AKAN DIKIRIM BERUNTUN
        // ==========================================
        val daftarPromo = listOf(
            Pair("🎉 Selamat Datang di WisataGO!", "Temukan destinasi liburan impianmu bersama kami."),
            Pair("🚂 Diskon 50% Tiket Kereta", "Klaim sekarang sebelum kehabisan! Berlaku untuk semua rute."),
            Pair("🎢 Liburan ke Dufan Yuk!", "Dapatkan promo Buy 1 Get 1 khusus untukmu hari ini.")
        )

        // ==========================================
        // 🟢 MESIN PENGIRIM DENGAN JEDA WAKTU
        // ==========================================
        CoroutineScope(Dispatchers.Main).launch {
            var idNotifikasi = 1001 // ID awal

            for (promo in daftarPromo) {
                val builder = NotificationCompat.Builder(this@Profile, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(promo.first) // Ambil Judul dari daftar di atas
                    .setContentText(promo.second) // Ambil Isi Pesan dari daftar di atas
                    .setStyle(NotificationCompat.BigTextStyle().bigText(promo.second + " Ketuk untuk info lebih lanjut!"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(customSoundUri)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                // Munculkan notifikasinya ke layar!
                notificationManager.notify(idNotifikasi, builder.build())

                // Tambahkan nomor ID agar notifikasi selanjutnya tidak menimpa notif ini
                idNotifikasi++

                // ⏳ TUNGGU 10 DETIK SEBELUM MENGIRIM PESAN SELANJUTNYA
                // 10.000 milidetik = 10 detik
                delay(10000)
            }
        }
    }
    override fun onResume() {
        super.onResume()

        val sharedPref =
            getSharedPreferences("USER_SESSION", MODE_PRIVATE)

        val userId =
            sharedPref.getString("USER_ID", null)

        if (userId != null) {
            ambilDataProfile(userId)
        } else {
            Toast.makeText(
                this,
                "User belum login",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun ambilDataProfile(userId: String) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response =
                    ApiClient.instance.getProfile(userId)

                withContext(Dispatchers.Main) {

                    if (
                        response.isSuccessful &&
                        response.body() != null
                    ) {

                        val user = response.body()!!

                        android.util.Log.d(
                            "PROFILE_DEBUG",
                            "PHONE = ${user.phone_number}"
                        )

                        tvUserName.text = user.full_name
                        tvUserEmail.text = user.email
                        tvUserPhone.text =
                            user.phone_number ?: "-"

                        if (!user.profile_picture.isNullOrEmpty()) {
                            try {
                                val cleanBase64 =
                                    user.profile_picture.replace(
                                        Regex("^data:image/[a-zA-Z]+;base64,"),
                                        ""
                                    )
                                val imageBytes =
                                    Base64.decode(
                                        cleanBase64,
                                        Base64.DEFAULT
                                    )
                                val decodedImage =
                                    BitmapFactory.decodeByteArray(
                                        imageBytes,
                                        0,
                                        imageBytes.size
                                    )
                                imgProfilePic.setImageBitmap(
                                    decodedImage
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    } else {
                        Toast.makeText(
                            this@Profile,
                            "Gagal ambil data profile",
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