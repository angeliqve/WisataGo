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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🟢 Menggunakan XML yang sama!
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

        // 🟢 PENTING: Sembunyikan Layout Statistik, Tampilkan Layout Pesanan
        findViewById<View>(R.id.layout_tab_statistik).visibility = View.GONE
        findViewById<View>(R.id.layout_tab_pesanan).visibility = View.VISIBLE

        // 🟢 Set Warna Tombol Navbar Aktif
        btnAdminStats.setImageResource(R.drawable.icon_home_blue) // Ikon abu/biru
        btnAdminOrders.setImageResource(R.drawable.icon_order_white) // Ikon putih
        tvAdminStats.setTextColor(Color.parseColor("#0A4181"))
        tvAdminOrders.setTextColor(Color.parseColor("#FFFFFF"))

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val adminName = sharedPref.getString("USERNAME", "Admin")
        tvAdminWelcome.text = "Selamat Datang, $adminName!"

        // Panggil fungsi penarik data daftar pemesanan
        ambilDataTransaksi()

        // 🟢 PINDAH KEMBALI KE ACTIVITY DASHBOARD (STATISTIK)
        btnAdminStats.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        // Pencarian Pesanan
        etAdminSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDataPemesanan(s.toString())
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
                        listPesananFilter.clear()
                        listPesananFilter.addAll(listSemuaPesananRaw)

                        adminAdapter = HistoryAdapter(listPesananFilter)
                        rvAdminAllOrders.adapter = adminAdapter
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun filterDataPemesanan(query: String) {
        listPesananFilter.clear()
        if (query.isEmpty()) {
            listPesananFilter.addAll(listSemuaPesananRaw)
        } else {
            val kataKunci = query.lowercase().trim()
            for (item in listSemuaPesananRaw) {
                if (item.booking_code?.lowercase()?.contains(kataKunci) == true ||
                    item.transport_name?.lowercase()?.contains(kataKunci) == true ||
                    item.origin_city?.lowercase()?.contains(kataKunci) == true) {
                    listPesananFilter.add(item)
                }
            }
        }
        if (::adminAdapter.isInitialized) {
            adminAdapter.notifyDataSetChanged()
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