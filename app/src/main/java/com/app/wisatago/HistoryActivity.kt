package com.app.wisatago

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.api.ApiClient
import com.app.wisatago.api.HistoryResponse
import com.app.wisatago.booking.HistoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvHistory = findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)

        // 🟢 NOTE: Pemanggilan API (muatRiwayatTransaksi) sudah dihapus dari sini
        // dan dipindahkan ke bawah (ke dalam onResume) agar bisa Auto-Refresh.

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        val btnPemesanan = findViewById<ImageView>(R.id.btnPemesanan)
        val btnProfile = findViewById<ImageView>(R.id.btnProfile)

        btnHome.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        btnPemesanan.setOnClickListener {
            Toast.makeText(this, "Anda sudah di halaman Pemesanan", Toast.LENGTH_SHORT).show()
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@HistoryActivity, Dashboard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        })
    }

    // ==========================================
    // 🟢 SIKLUS AUTO-REFRESH (Mantra Rahasianya)
    // ==========================================
    override fun onResume() {
        super.onResume()

        // Kode ini akan selalu dieksekusi setiap kali user melihat halaman ini
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            muatRiwayatTransaksi(userId) // Tarik data terbaru dari server!
        } else {
            Toast.makeText(this, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun muatRiwayatTransaksi(userId: String) {
        ApiClient.instance.getBookingHistory(userId).enqueue(object : Callback<List<HistoryResponse>> {
            override fun onResponse(call: Call<List<HistoryResponse>>, response: Response<List<HistoryResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val dataRiwayat = response.body()!!
                    adapter = HistoryAdapter(dataRiwayat)
                    rvHistory.adapter = adapter
                } else {
                    Toast.makeText(this@HistoryActivity, "Gagal mengambil data riwayat", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<HistoryResponse>>, t: Throwable) {
                Toast.makeText(this@HistoryActivity, "Detail Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                t.printStackTrace()
            }
        })
    }
}