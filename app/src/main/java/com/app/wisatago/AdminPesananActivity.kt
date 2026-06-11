package com.app.wisatago

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.api.ApiClient
import com.app.wisatago.api.HistoryResponse
import com.app.wisatago.booking.HistoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminPesananActivity : AppCompatActivity() {

    private lateinit var tvAdminWelcome: TextView
    private lateinit var btnAdminLogout: ImageButton

    private lateinit var btnAdminStats: ImageView
    private lateinit var btnAdminOrders: ImageView
    private lateinit var tvAdminStats: TextView
    private lateinit var tvAdminOrders: TextView

    private lateinit var etAdminSearch: EditText
    private lateinit var rvAdminAllOrders: RecyclerView
    private lateinit var adminAdapter: HistoryAdapter
    private var listSemuaPesananRaw: List<HistoryResponse> = ArrayList()
    private var listPesananFilter: MutableList<HistoryResponse> = ArrayList()

    // 🟢 Variabel State Filter
    private var selectedKategori = "Semua"
    private var selectedBulanLabel = "Semua"
    private var selectedBulanKode = "" // Kosong = Semua bulan

    // 🟢 Deklarasi Tombol Chip
    private lateinit var btnFilterBulan: MaterialButton
    private lateinit var btnFilterSemua: MaterialButton
    private lateinit var btnFilterWisata: MaterialButton
    private lateinit var btnFilterKereta: MaterialButton
    private lateinit var btnFilterPesawat: MaterialButton
    private lateinit var btnFilterBus: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        tvAdminWelcome = findViewById(R.id.tv_admin_welcome)
        btnAdminLogout = findViewById(R.id.btn_admin_logout)
        btnAdminStats = findViewById(R.id.btnAdminStats)
        btnAdminOrders = findViewById(R.id.btnAdminOrders)
        tvAdminStats = findViewById(R.id.tvAdminStats)
        tvAdminOrders = findViewById(R.id.tvAdminOrders)

        etAdminSearch = findViewById(R.id.et_admin_search)
        rvAdminAllOrders = findViewById(R.id.rv_admin_all_orders)
        rvAdminAllOrders.layoutManager = LinearLayoutManager(this)

        // Hubungkan Chip UI
        btnFilterBulan = findViewById(R.id.btn_filter_bulan)
        btnFilterSemua = findViewById(R.id.btn_filter_semua)
        btnFilterWisata = findViewById(R.id.btn_filter_wisata)
        btnFilterKereta = findViewById(R.id.btn_filter_kereta)
        btnFilterPesawat = findViewById(R.id.btn_filter_pesawat)
        btnFilterBus = findViewById(R.id.btn_filter_bus)

        findViewById<View>(R.id.layout_tab_statistik).visibility = View.GONE
        findViewById<View>(R.id.layout_tab_pesanan).visibility = View.VISIBLE

        btnAdminStats.setImageResource(R.drawable.icon_home_blue)
        btnAdminOrders.setImageResource(R.drawable.icon_order_white)
        tvAdminStats.setTextColor(Color.parseColor("#0A4181"))
        tvAdminOrders.setTextColor(Color.parseColor("#FFFFFF"))

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        tvAdminWelcome.text = "Selamat Datang, ${sharedPref.getString("USERNAME", "Admin")}!"

        ambilDataTransaksi()

        btnAdminStats.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        // ==========================================
        // 🟢 LOGIKA ON-CLICK FILTER KATEGORI
        // ==========================================
        btnFilterSemua.setOnClickListener { setChipAktif(it as MaterialButton, "Semua") }
        btnFilterWisata.setOnClickListener { setChipAktif(it as MaterialButton, "Wisata") }
        btnFilterKereta.setOnClickListener { setChipAktif(it as MaterialButton, "Kereta Api") }
        btnFilterPesawat.setOnClickListener { setChipAktif(it as MaterialButton, "Pesawat") }
        btnFilterBus.setOnClickListener { setChipAktif(it as MaterialButton, "Bus") }

        // ==========================================
        // 🟢 LOGIKA ON-CLICK BOTTOM SHEET BULAN
        // ==========================================
        btnFilterBulan.setOnClickListener { showFilterBulan() }

        etAdminSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyCombinedFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAdminLogout.setOnClickListener { logoutAdmin() }
    }

    private fun ambilDataTransaksi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.instance.getAllBookingsAdmin()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        listSemuaPesananRaw = response.body()!!
                        applyCombinedFilter() // Terapkan filter di awal
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Mengubah warna chip yang sedang dipilih
    private fun setChipAktif(btnAktif: MaterialButton, kategori: String) {
        selectedKategori = kategori
        val semuaBtn = listOf(btnFilterSemua, btnFilterWisata, btnFilterKereta, btnFilterPesawat, btnFilterBus)

        // Reset semua tombol menjadi abu-abu
        for (btn in semuaBtn) {
            btn.setBackgroundColor(Color.parseColor("#F0F0F0"))
            btn.setTextColor(Color.parseColor("#333333"))
        }

        // Set tombol yang diklik menjadi biru
        btnAktif.setBackgroundColor(Color.parseColor("#2DA0F5"))
        btnAktif.setTextColor(Color.parseColor("#FFFFFF"))

        applyCombinedFilter()
    }

    // Memunculkan Dialog Bottom Sheet
    private fun showFilterBulan() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_filter_bulan, null)

        val optJan = view.findViewById<TextView>(R.id.opt_jan)
        val optFeb = view.findViewById<TextView>(R.id.opt_feb)
        val optMar = view.findViewById<TextView>(R.id.opt_mar)
        val optApr = view.findViewById<TextView>(R.id.opt_apr)
        val optMei = view.findViewById<TextView>(R.id.opt_mei)
        val optJun = view.findViewById<TextView>(R.id.opt_jun)

        val setBulan = { label: String, kode: String ->
            selectedBulanLabel = label
            selectedBulanKode = kode

            // 🟢 Teks dihapus total agar murni hanya menampilkan ikon
            btnFilterBulan.text = ""

            // 🟢 Ubah warna background menjadi Abu-abu muda saat filter aktif
            btnFilterBulan.setBackgroundColor(if (label == "Semua Bulan") Color.parseColor("#FFFFFF") else Color.parseColor("#E0E0E0"))

            applyCombinedFilter()
            bottomSheetDialog.dismiss()
        }

        optJan.setOnClickListener { setBulan("Januari", "Jan") }
        optFeb.setOnClickListener { setBulan("Februari", "Feb") }
        optMar.setOnClickListener { setBulan("Maret", "Mar") }
        optApr.setOnClickListener { setBulan("April", "Apr") }
        optMei.setOnClickListener { setBulan("Mei", "May") } // May di PostgreSQL
        optJun.setOnClickListener { setBulan("Juni", "Jun") }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    // 🟢 Logika Sentral Penggabungan 3 Filter (Teks, Kategori, Bulan)
    private fun applyCombinedFilter() {
        val query = etAdminSearch.text.toString().lowercase().trim()
        listPesananFilter.clear()

        for (item in listSemuaPesananRaw) {

            // 1. Filter Pencarian Teks
            val isSearchMatch = query.isEmpty() ||
                    item.booking_code?.lowercase()?.contains(query) == true ||
                    item.transport_name?.lowercase()?.contains(query) == true ||
                    item.origin_city?.lowercase()?.contains(query) == true ||
                    item.full_name?.lowercase()?.contains(query) == true // Bisa dicari dengan nama pemesan

            // 2. Filter Kategori (Mendeteksi armada dari namanya)
            val nameTransport = item.transport_name ?: ""
            val isCategoryMatch = when(selectedKategori) {
                "Semua" -> true
                "Wisata" -> item.booking_code?.startsWith("WS-") == true
                "Kereta Api" -> item.booking_code?.startsWith("TR-") == true &&
                        (nameTransport.contains("KAI", true) || nameTransport.contains("Jaya", true) || nameTransport.contains("Argo", true) || nameTransport.contains("Express", true))
                "Pesawat" -> item.booking_code?.startsWith("TR-") == true &&
                        (nameTransport.contains("Air", true) || nameTransport.contains("Garuda", true) || nameTransport.contains("Citilink", true) || nameTransport.contains("Batik", true))
                "Bus" -> item.booking_code?.startsWith("TR-") == true &&
                        (nameTransport.contains("Bus", true) || nameTransport.contains("PO", true) || nameTransport.contains("Trans", true) || nameTransport.contains("Rosalia", true) || nameTransport.contains("Sinar", true))
                else -> true
            }

            // 3. Filter Bulan (Mencari singkatan bahasa inggris di tanggal SQL: '11 Jun 2026')
            val isMonthMatch = if (selectedBulanKode.isEmpty()) {
                true
            } else {
                item.booking_date?.contains(selectedBulanKode, ignoreCase = true) == true
            }

            // Jika lulus semua kriteria filter, tampilkan!
            if (isSearchMatch && isCategoryMatch && isMonthMatch) {
                listPesananFilter.add(item)
            }
        }

        if (::adminAdapter.isInitialized) {
            adminAdapter = HistoryAdapter(listPesananFilter)
            rvAdminAllOrders.adapter = adminAdapter
        } else {
            adminAdapter = HistoryAdapter(listPesananFilter)
            rvAdminAllOrders.adapter = adminAdapter
        }
    }

    private fun logoutAdmin() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout Admin").setMessage("Yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                getSharedPreferences("USER_SESSION", MODE_PRIVATE).edit().clear().apply()
                startActivity(Intent(this, Login::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                finish()
            }.setNegativeButton("Batal", null).show()
    }
}