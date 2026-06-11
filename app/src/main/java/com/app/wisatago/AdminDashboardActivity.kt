package com.app.wisatago

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.api.ApiClient

import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvAdminWelcome: TextView
    private lateinit var btnAdminLogout: ImageButton

    private lateinit var btnAdminStats: ImageView
    private lateinit var btnAdminOrders: ImageView
    private lateinit var tvAdminStats: TextView
    private lateinit var tvAdminOrders: TextView

    private lateinit var tvAdminTotalTransaksi: TextView
    private lateinit var tvAdminPemesananBaru: TextView
    private lateinit var tvAdminPendapatanHari: TextView
    private lateinit var tvAdminPendapatanBulan: TextView

    private lateinit var lineChartPendapatan: LineChart
    private lateinit var pieChartStatus: PieChart
    private lateinit var barChartProduk: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🟢 Menggunakan XML yang sama
        setContentView(R.layout.activity_admin_dashboard)

        tvAdminWelcome = findViewById(R.id.tv_admin_welcome)
        btnAdminLogout = findViewById(R.id.btn_admin_logout)
        btnAdminStats = findViewById(R.id.btnAdminStats)
        btnAdminOrders = findViewById(R.id.btnAdminOrders)
        tvAdminStats = findViewById(R.id.tvAdminStats)
        tvAdminOrders = findViewById(R.id.tvAdminOrders)

        tvAdminTotalTransaksi = findViewById(R.id.tvAdminTotalTransaksi)
        tvAdminPemesananBaru = findViewById(R.id.tvAdminPemesananBaru)
        tvAdminPendapatanHari = findViewById(R.id.tvAdminPendapatanHari)
        tvAdminPendapatanBulan = findViewById(R.id.tvAdminPendapatanBulan)

        lineChartPendapatan = findViewById(R.id.lineChartPendapatan)
        pieChartStatus = findViewById(R.id.pieChartStatus)
        barChartProduk = findViewById(R.id.barChartProduk)

        // 🟢 PENTING: Sembunyikan Layout Pesanan, Tampilkan Layout Statistik
        findViewById<View>(R.id.layout_tab_pesanan).visibility = View.GONE
        findViewById<View>(R.id.layout_tab_statistik).visibility = View.VISIBLE

        // 🟢 Set Warna Tombol Navbar Aktif
        btnAdminStats.setImageResource(R.drawable.icon_home_white)
        btnAdminOrders.setImageResource(R.drawable.icon_order_blue)
        tvAdminStats.setTextColor(Color.parseColor("#FFFFFF"))
        tvAdminOrders.setTextColor(Color.parseColor("#0A4181"))

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val adminName = sharedPref.getString("USERNAME", "Admin")
        tvAdminWelcome.text = "Selamat Datang, $adminName!"

        // Panggil fungsi penarik data khusus statistik
        ambilDataStatistik()

        // 🟢 PINDAH KE ACTIVITY PESANAN
        btnAdminOrders.setOnClickListener {
            startActivity(Intent(this, AdminPesananActivity::class.java))
            overridePendingTransition(0, 0) // Menghilangkan animasi transisi agar terasa seperti pindah tab
            finish()
        }

        btnAdminLogout.setOnClickListener { logoutAdmin() }
    }

    private fun ambilDataStatistik() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.instance.getAdminStats().execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val statsData = response.body()!!.data
                        if (statsData != null) {
                            val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                            tvAdminTotalTransaksi.text = statsData.ringkasan.totalTransaksi ?: "0"
                            tvAdminPemesananBaru.text = statsData.ringkasan.pemesananBaru ?: "0"

                            val hariIni = statsData.ringkasan.pendapatanHariIni?.toDoubleOrNull() ?: 0.0
                            tvAdminPendapatanHari.text = formatRp.format(hariIni).replace(",00", "")

                            val bulanIni = statsData.ringkasan.totalPendapatan?.toDoubleOrNull() ?: 0.0
                            tvAdminPendapatanBulan.text = formatRp.format(bulanIni).replace(",00", "")

                            setupLineChart(statsData.bulanan)
                            setupPieChart(statsData.status)
                            setupBarChart(statsData.populer)
                        }
                    } else {
                        Toast.makeText(this@AdminDashboardActivity, "Gagal muat diagram", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Error API Statistik", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupLineChart(dataBulanan: List<StatsBulanan>) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        for ((index, item) in dataBulanan.withIndex()) {
            entries.add(Entry(index.toFloat(), item.total.toFloatOrNull() ?: 0f))
            labels.add(item.bulan)
        }
        val dataSet = LineDataSet(entries, "Pendapatan").apply {
            color = Color.WHITE; lineWidth = 3f; circleRadius = 5f; setCircleColor(Color.WHITE)
            mode = LineDataSet.Mode.CUBIC_BEZIER; setDrawValues(false)
        }
        lineChartPendapatan.apply {
            data = LineData(dataSet); description.isEnabled = false; legend.textColor = Color.WHITE
            xAxis.valueFormatter = IndexAxisValueFormatter(labels); xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE; xAxis.setDrawGridLines(false); xAxis.granularity = 1f
            axisLeft.textColor = Color.WHITE; axisRight.isEnabled = false
            animateY(1000); invalidate()
        }
    }

    private fun setupPieChart(dataStatus: List<StatsStatus>) {
        val entries = ArrayList<PieEntry>()
        for (item in dataStatus) {
            entries.add(PieEntry(item.jumlah.toFloatOrNull() ?: 0f, item.status))
        }
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#FFC107"), Color.parseColor("#F44336"))
            valueTextColor = Color.WHITE; valueTextSize = 14f
            valueFormatter = object : ValueFormatter() { override fun getFormattedValue(value: Float): String = "${value.toInt()}%" }
        }
        pieChartStatus.apply {
            data = PieData(dataSet); description.isEnabled = false; isDrawHoleEnabled = true; holeRadius = 45f
            setHoleColor(Color.TRANSPARENT); legend.textColor = Color.WHITE; setUsePercentValues(true)
            setDrawEntryLabels(false); animateY(1000); invalidate()
        }
    }

    private fun setupBarChart(dataPopuler: List<StatsPopuler>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        for ((index, item) in dataPopuler.withIndex()) {
            entries.add(BarEntry(index.toFloat(), item.jumlah.toFloatOrNull() ?: 0f))
            labels.add(item.produk)
        }
        val dataSet = BarDataSet(entries, "Total Pemesanan").apply {
            color = Color.parseColor("#80FFFFFF"); valueTextColor = Color.WHITE; valueTextSize = 12f
            valueFormatter = object : ValueFormatter() { override fun getFormattedValue(value: Float): String = value.toInt().toString() }
        }
        barChartProduk.apply {
            data = BarData(dataSet); description.isEnabled = false; legend.textColor = Color.WHITE
            xAxis.valueFormatter = IndexAxisValueFormatter(labels); xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE; xAxis.setDrawGridLines(false); xAxis.granularity = 1f
            axisLeft.textColor = Color.WHITE; axisRight.isEnabled = false
            animateY(1000); invalidate()
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