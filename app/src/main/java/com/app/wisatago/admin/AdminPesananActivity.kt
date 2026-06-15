package com.app.wisatago.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.Login
import com.app.wisatago.R
import com.app.wisatago.api.ApiClient
import com.app.wisatago.api.HistoryResponse
import com.app.wisatago.booking.HistoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*

class AdminPesananActivity : AppCompatActivity() {

    private lateinit var tvAdminWelcome: TextView
    private lateinit var btnAdminLogout: ImageButton
    private lateinit var btnAdminStats: ImageView
    private lateinit var btnAdminOrders: ImageView
    private lateinit var tvAdminStats: TextView
    private lateinit var tvAdminOrders: TextView
    private lateinit var etAdminSearch: EditText

    // UI List & Loading
    private lateinit var rvAdminAllOrders: RecyclerView
    private lateinit var scrollViewAdmin: NestedScrollView
    private lateinit var pbLoading: ProgressBar

    // Pagination & State
    private lateinit var adminAdapter: HistoryAdapter
    private var listPesananData: MutableList<HistoryResponse> = ArrayList()

    private var currentPage = 1
    private val limitPerPage = 10
    private var isLoading = false
    private var isLastPage = false
    private var searchJob: Job? = null

    // Variabel State Filter
    private var selectedKategori = "Semua"
    private var selectedBulanKode = ""

    // Deklarasi Tombol Chip
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
        scrollViewAdmin = findViewById(R.id.scrollViewAdmin)
        pbLoading = findViewById(R.id.pb_admin_loading)

        rvAdminAllOrders.layoutManager = LinearLayoutManager(this)

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
        findViewById<View>(R.id.indicatorStats).setBackgroundColor(Color.TRANSPARENT)
        findViewById<View>(R.id.indicatorOrders).setBackgroundResource(R.drawable.bg_navbar2)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        tvAdminWelcome.text = "Selamat Datang, ${sharedPref.getString("USERNAME", "Admin")}!"

        // 🟢 MENDETEKSI SAAT ADMIN MENGGESER SAMPAI BAWAH (INFINITE SCROLL)
        scrollViewAdmin.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY >= v.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (!isLoading && !isLastPage) {
                    currentPage++
                    fetchDataDariServer()
                }
            }
        })

        btnAdminStats.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        btnFilterSemua.setOnClickListener { setChipAktif(it as MaterialButton, "Semua") }
        btnFilterWisata.setOnClickListener { setChipAktif(it as MaterialButton, "Wisata") }
        btnFilterKereta.setOnClickListener { setChipAktif(it as MaterialButton, "Kereta Api") }
        btnFilterPesawat.setOnClickListener { setChipAktif(it as MaterialButton, "Pesawat") }
        btnFilterBus.setOnClickListener { setChipAktif(it as MaterialButton, "Bus") }
        btnFilterBulan.setOnClickListener { showFilterBulan() }

        // 🟢 DEBOUNCED SEARCH: Hanya mencari jika admin berhenti mengetik selama 0.5 detik
        etAdminSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    resetPagination()
                }
            }
        })

        btnAdminLogout.setOnClickListener { logoutAdmin() }

        // Load data pertama kali
        resetPagination()
    }

    private fun setChipAktif(btnAktif: MaterialButton, kategori: String) {
        if (selectedKategori == kategori) return
        selectedKategori = kategori

        val semuaBtn = listOf(btnFilterSemua, btnFilterWisata, btnFilterKereta, btnFilterPesawat, btnFilterBus)
        for (btn in semuaBtn) {
            btn.setBackgroundColor(Color.parseColor("#F0F0F0"))
            btn.setTextColor(Color.parseColor("#333333"))
        }

        btnAktif.setBackgroundColor(Color.parseColor("#2DA0F5"))
        btnAktif.setTextColor(Color.parseColor("#FFFFFF"))

        resetPagination()
    }

    private fun showFilterBulan() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_filter_bulan, null)

        val setBulan = { label: String, kode: String ->
            if (selectedBulanKode != kode) {
                selectedBulanKode = kode
                btnFilterBulan.text = ""
                btnFilterBulan.setBackgroundColor(if (label == "Semua Bulan") Color.parseColor("#FFFFFF") else Color.parseColor("#E0E0E0"))
                resetPagination()
            }
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

    private fun resetPagination() {
        currentPage = 1
        isLastPage = false
        listPesananData.clear()
        if (::adminAdapter.isInitialized) {
            adminAdapter.notifyDataSetChanged()
        }
        fetchDataDariServer()
    }

    // 🟢 TARIK DATA CERDAS (PAGE PER PAGE)
    private fun fetchDataDariServer() {
        isLoading = true
        pbLoading.visibility = View.VISIBLE
        val searchTxt = etAdminSearch.text.toString().trim()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.instance.getAllBookingsAdmin(
                    search = searchTxt,
                    category = selectedKategori,
                    month = selectedBulanKode,
                    limit = limitPerPage,
                    page = currentPage
                )

                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    isLoading = false

                    if (response.isSuccessful && response.body() != null) {
                        val newData = response.body()!!

                        // Jika data yang ditarik kurang dari limit, artinya ini halaman terakhir
                        if (newData.size < limitPerPage) isLastPage = true

                        val startSize = listPesananData.size
                        listPesananData.addAll(newData)

                        if (::adminAdapter.isInitialized && currentPage > 1) {
                            adminAdapter.notifyItemRangeInserted(startSize, newData.size)
                        } else {
                            adminAdapter = HistoryAdapter(listPesananData)
                            rvAdminAllOrders.adapter = adminAdapter
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    isLoading = false
                    e.printStackTrace()
                }
            }
        }
    }

    private fun logoutAdmin() {
        AlertDialog.Builder(this)
            .setTitle("Logout Admin").setMessage("Yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                getSharedPreferences("USER_SESSION", MODE_PRIVATE).edit().clear().apply()
                startActivity(Intent(this, Login::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                finish()
            }.setNegativeButton("Batal", null).show()
    }
}