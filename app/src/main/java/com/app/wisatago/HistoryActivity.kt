package com.app.wisatago

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.api.ApiClient
import com.app.wisatago.api.HistoryResponse
import com.app.wisatago.booking.HistoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private var listSemuaPesananRaw: List<HistoryResponse> = ArrayList()
    private var listPesananFilter: MutableList<HistoryResponse> = ArrayList()
    private var selectedKategori = "Semua"
    private var selectedBulanKode = ""
    private lateinit var btnFilterBulan: MaterialButton
    private lateinit var btnFilterSemua: MaterialButton
    private lateinit var btnFilterWisata: MaterialButton
    private lateinit var btnFilterKereta: MaterialButton
    private lateinit var btnFilterPesawat: MaterialButton
    private lateinit var btnFilterBus: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvHistory = findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)

        btnFilterBulan = findViewById(R.id.btn_user_filter_bulan)
        btnFilterSemua = findViewById(R.id.btn_user_filter_semua)
        btnFilterWisata = findViewById(R.id.btn_user_filter_wisata)
        btnFilterKereta = findViewById(R.id.btn_user_filter_kereta)
        btnFilterPesawat = findViewById(R.id.btn_user_filter_pesawat)
        btnFilterBus = findViewById(R.id.btn_user_filter_bus)

        btnFilterSemua.setOnClickListener { setChipAktif(it as MaterialButton, "Semua") }
        btnFilterWisata.setOnClickListener { setChipAktif(it as MaterialButton, "Wisata") }
        btnFilterKereta.setOnClickListener { setChipAktif(it as MaterialButton, "Kereta Api") }
        btnFilterPesawat.setOnClickListener { setChipAktif(it as MaterialButton, "Pesawat") }
        btnFilterBus.setOnClickListener { setChipAktif(it as MaterialButton, "Bus") }
        btnFilterBulan.setOnClickListener { showBottomSheetBulan() }

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

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            muatRiwayatTransaksi(userId)
        } else {
            Toast.makeText(this, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun muatRiwayatTransaksi(userId: String) {
        ApiClient.instance.getBookingHistory(userId).enqueue(object : Callback<List<HistoryResponse>> {
            override fun onResponse(call: Call<List<HistoryResponse>>, response: Response<List<HistoryResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    listSemuaPesananRaw = response.body()!!

                    applyCombinedFilter()
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

    private fun setChipAktif(btnAktif: MaterialButton, kategori: String) {
        selectedKategori = kategori
        val semuaBtn = listOf(btnFilterSemua, btnFilterWisata, btnFilterKereta, btnFilterPesawat, btnFilterBus)

        for (btn in semuaBtn) {
            btn.setBackgroundColor(Color.parseColor("#F0F0F0"))
            btn.setTextColor(Color.parseColor("#333333"))
        }

        btnAktif.setBackgroundColor(Color.parseColor("#2DA0F5"))
        btnAktif.setTextColor(Color.parseColor("#FFFFFF"))

        applyCombinedFilter()
    }

    private fun showBottomSheetBulan() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_filter_bulan, null)

        val setBulan = { label: String, kode: String ->
            selectedBulanKode = kode
            btnFilterBulan.text = label
            btnFilterBulan.setBackgroundColor(if (kode.isEmpty()) Color.parseColor("#FFFFFF") else Color.parseColor("#E0E0E0"))
            applyCombinedFilter()
            bottomSheetDialog.dismiss()
        }

        view.findViewById<TextView>(R.id.opt_jan).setOnClickListener { setBulan("Januari", "Jan") }
        view.findViewById<TextView>(R.id.opt_feb).setOnClickListener { setBulan("Februari", "Feb") }
        view.findViewById<TextView>(R.id.opt_mar).setOnClickListener { setBulan("Maret", "Mar") }
        view.findViewById<TextView>(R.id.opt_apr).setOnClickListener { setBulan("April", "Apr") }
        view.findViewById<TextView>(R.id.opt_mei).setOnClickListener { setBulan("Mei", "May") }
        view.findViewById<TextView>(R.id.opt_jun).setOnClickListener { setBulan("Juni", "Jun") }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun applyCombinedFilter() {
        listPesananFilter.clear()

        for (item in listSemuaPesananRaw) {
            val nameTransport = item.transport_name ?: ""
            val bookingCode = item.booking_code ?: ""

            val isCategoryMatch = when (selectedKategori) {
                "Semua" -> true

                "Wisata" -> bookingCode.startsWith("WS-", ignoreCase = true) ||
                        nameTransport.contains("Dufan", true) ||
                        nameTransport.contains("Wisata", true)

                "Kereta Api" -> nameTransport.contains("KAI", true) ||
                        nameTransport.contains("Kereta", true) ||
                        nameTransport.contains("Train", true) ||
                        nameTransport.contains("Argo", true) ||
                        nameTransport.contains("Dharmawangsa", true) ||
                        nameTransport.contains("Sembrani", true) ||
                        nameTransport.contains("Gajayana", true) ||
                        nameTransport.contains("Express", true)

                "Pesawat" -> nameTransport.contains("Air", true) ||
                        nameTransport.contains("Pesawat", true) ||
                        nameTransport.contains("Flight", true) ||
                        nameTransport.contains("Garuda", true) ||
                        nameTransport.contains("Batik", true) ||
                        nameTransport.contains("Citilink", true) ||
                        nameTransport.contains("Lion", true)

                "Bus" -> bookingCode.startsWith("BU-", ignoreCase = true) ||
                        nameTransport.contains("Bus", true) ||
                        nameTransport.contains("Harapan", true) ||
                        nameTransport.contains("PO ", true) ||
                        nameTransport.contains("Trans", true) ||
                        nameTransport.contains("Rosalia", true) ||
                        nameTransport.contains("Sinar", true)

                else -> true
            }

            val isMonthMatch = if (selectedBulanKode.isEmpty()) {
                true
            } else {
                item.booking_date?.contains(selectedBulanKode, ignoreCase = true) == true
            }

            if (isCategoryMatch && isMonthMatch) {
                listPesananFilter.add(item)
            }
        }

        if (::adapter.isInitialized) {
            adapter.updateDataList(listPesananFilter)
        } else {
            adapter = HistoryAdapter(listPesananFilter)
            rvHistory.adapter = adapter
        }
    }
}