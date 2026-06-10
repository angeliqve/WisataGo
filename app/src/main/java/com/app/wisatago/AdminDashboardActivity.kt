package com.app.wisatago

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.booking.HistoryAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvAdminWelcome: TextView
    private lateinit var btnAdminLogout: ImageButton
    private lateinit var btnAdminStats: ImageView
    private lateinit var btnAdminOrders: ImageView
    private lateinit var tvAdminStats: TextView
    private lateinit var tvAdminOrders: TextView
    private lateinit var layoutStatistik: View
    private lateinit var layoutPesanan: View
    private lateinit var etAdminSearch: EditText
    private lateinit var rvAdminAllOrders: RecyclerView
    private lateinit var adminAdapter: HistoryAdapter
    private var listSemuaPesananRaw: List<HistoryResponse> = ArrayList()
    private var listPesananFilter: MutableList<HistoryResponse> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        tvAdminWelcome = findViewById(R.id.tv_admin_welcome)
        btnAdminLogout = findViewById(R.id.btn_admin_logout)

        btnAdminStats = findViewById(R.id.btnAdminStats)
        btnAdminOrders = findViewById(R.id.btnAdminOrders)
        tvAdminStats = findViewById(R.id.tvAdminStats)
        tvAdminOrders = findViewById(R.id.tvAdminOrders)

        layoutStatistik = findViewById(R.id.layout_tab_statistik)
        layoutPesanan = findViewById(R.id.layout_tab_pesanan)

        etAdminSearch = findViewById(R.id.et_admin_search)
        rvAdminAllOrders = findViewById(R.id.rv_admin_all_orders)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val adminName = sharedPref.getString("USERNAME", "Admin")
        tvAdminWelcome.text = "Selamat Datang, $adminName!"

        rvAdminAllOrders.layoutManager = LinearLayoutManager(this)

        muatDataStatistikDanTransaksi()

        btnAdminStats.setOnClickListener {
            layoutStatistik.visibility = View.VISIBLE
            layoutPesanan.visibility = View.GONE

            btnAdminStats.setImageResource(R.drawable.icon_home_white)
            btnAdminOrders.setImageResource(R.drawable.icon_order_blue)
            tvAdminStats.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            tvAdminOrders.setTextColor(android.graphics.Color.parseColor("#0A4181"))
        }

        btnAdminOrders.setOnClickListener {
            layoutStatistik.visibility = View.GONE
            layoutPesanan.visibility = View.VISIBLE

            btnAdminStats.setImageResource(R.drawable.icon_home_blue)
            btnAdminOrders.setImageResource(R.drawable.icon_order_white)
            tvAdminStats.setTextColor(android.graphics.Color.parseColor("#0A4181"))
            tvAdminOrders.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
        }

        etAdminSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDataPemesanan(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAdminLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout Admin")
                .setMessage("Yakin ingin keluar dari panel admin WisataGO?")
                .setPositiveButton("Ya") { _, _ ->
                    sharedPref.edit().clear().apply()
                    Toast.makeText(this, "Logout admin berhasil", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun muatDataStatistikDanTransaksi() {
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

                        findViewById<TextView>(R.id.tv_stat_total_booking).text = listSemuaPesananRaw.size.toString()

                        var totalIncome = 0.0
                        for (booking in listSemuaPesananRaw) {
                            if (booking.status?.uppercase() == "SUCCESS") {
                                val amount = booking.total_amount.toString().toDoubleOrNull() ?: 0.0
                                totalIncome += amount
                            }
                        }

                        val formatter = java.text.NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        findViewById<TextView>(R.id.tv_stat_income).text = formatter.format(totalIncome).replace(",00", "")

                    } else {
                        Toast.makeText(this@AdminDashboardActivity, "Gagal mengambil data dari server", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Koneksi gagal atau server mati: ${e.message}", Toast.LENGTH_LONG).show()
                }
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
                val kodeBookingMatch = item.booking_code?.lowercase()?.contains(kataKunci) == true
                val namaArmadaMatch = item.transport_name?.lowercase()?.contains(kataKunci) == true
                val kotaAsalMatch = item.origin_city?.lowercase()?.contains(kataKunci) == true

                if (kodeBookingMatch || namaArmadaMatch || kotaAsalMatch) {
                    listPesananFilter.add(item)
                }
            }
        }

        if (::adminAdapter.isInitialized) {
            adminAdapter.notifyDataSetChanged()
        }
    }
}