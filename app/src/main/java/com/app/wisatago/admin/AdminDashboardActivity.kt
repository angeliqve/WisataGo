package com.app.wisatago.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.Login
import com.app.wisatago.R

import com.app.wisatago.api.ApiClient
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
import com.github.mikephil.charting.formatter.ValueFormatter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvAdminWelcome: TextView
    private lateinit var btnAdminLogout: ImageButton

    // Navbar
    private lateinit var btnAdminStats: ImageView
    private lateinit var btnAdminOrders: ImageView
    private lateinit var tvAdminStats: TextView
    private lateinit var tvAdminOrders: TextView

    // Ringkasan Atas
    private lateinit var tvAdminTotalTransaksi: TextView
    private lateinit var tvAdminPemesananBaru: TextView
    private lateinit var tvAdminPendapatanHari: TextView
    private lateinit var tvAdminPendapatanBulan: TextView

    // Diagram Utama
    private lateinit var lineChartPendapatan: LineChart
    private lateinit var pieChartStatus: PieChart
    private lateinit var barChartProduk: BarChart

    // Diagram Baru & Log
    private lateinit var barChartWisata: BarChart
    private lateinit var barChartTransport: BarChart
    private lateinit var barChartDaerah: BarChart
    private lateinit var rvAdminLogs: RecyclerView
    private lateinit var logAdapter: AdminLogAdapter

    // 🟢 MESIN REFRESH OTOMATIS (REAL-TIME UNTUK KESELURUHAN)
    private val autoRefreshHandler = Handler(Looper.getMainLooper())
    private val autoRefreshRunnable = object : Runnable {
        override fun run() {
            ambilDataStatistik()  // Refresh Angka & Grafik
            ambilDataLogs()       // Refresh Daftar Log Aktivitas
            autoRefreshHandler.postDelayed(this, 10000) // Ulangi tiap 10 detik
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        barChartWisata = findViewById(R.id.barChartWisata)
        barChartTransport = findViewById(R.id.barChartTransport)
        barChartDaerah = findViewById(R.id.barChartDaerah)

        rvAdminLogs = findViewById(R.id.rvAdminLogs)
        rvAdminLogs.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.layout_tab_statistik).visibility = View.VISIBLE
        findViewById<View>(R.id.layout_tab_pesanan).visibility = View.GONE

        btnAdminStats.setImageResource(R.drawable.icon_home_white)
        btnAdminOrders.setImageResource(R.drawable.icon_order_blue)
        tvAdminStats.setTextColor(Color.parseColor("#FFFFFF"))
        tvAdminOrders.setTextColor(Color.parseColor("#0A4181"))
        findViewById<View>(R.id.indicatorStats).setBackgroundResource(R.drawable.bg_navbar2)
        findViewById<View>(R.id.indicatorOrders).setBackgroundColor(Color.TRANSPARENT)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val adminName = sharedPref.getString("USERNAME", "Admin")
        tvAdminWelcome.text = "Selamat Datang, $adminName!"

        // Navigasi Pindah ke Halaman Pesanan
        btnAdminOrders.setOnClickListener {
            startActivity(Intent(this, AdminPesananActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        // Logika Logout
        btnAdminLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout Admin")
                .setMessage("Yakin ingin keluar dari panel admin WisataGO?")
                .setPositiveButton("Ya") { _, _ ->
                    sharedPref.edit().clear().apply()
                    startActivity(Intent(this, Login::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                .setNegativeButton("Batal", null).show()
        }
    }

    // 🟢 NYALAKAN REFRESH OTOMATIS SAAT HALAMAN DIBUKA
    override fun onResume() {
        super.onResume()
        autoRefreshHandler.post(autoRefreshRunnable)
    }

    // 🟢 MATIKAN REFRESH OTOMATIS SAAT APLIKASI DITUTUP (Hemat Baterai)
    override fun onPause() {
        super.onPause()
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable)
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

                            // Update Angka Keseluruhan
                            tvAdminTotalTransaksi.text = statsData.ringkasan.totalTransaksi ?: "0"
                            tvAdminPemesananBaru.text = statsData.ringkasan.pemesananBaru ?: "0"

                            val hariIni = statsData.ringkasan.pendapatanHariIni?.toDoubleOrNull() ?: 0.0
                            tvAdminPendapatanHari.text = formatRp.format(hariIni).replace(",00", "")

                            val bulanIni = statsData.ringkasan.totalPendapatan?.toDoubleOrNull() ?: 0.0
                            tvAdminPendapatanBulan.text = formatRp.format(bulanIni).replace(",00", "")

                            // Update Semua Grafik Secara Halus
                            setupLineChart(statsData.bulanan)
                            setupPieChart(statsData.status)
                            setupBarChart(statsData.populer)
                            setupBarChartWisata(statsData.wisata ?: emptyList())
                            setupBarChartTransport(statsData.transport ?: emptyList())
                            setupBarChartDaerah(statsData.daerah ?: emptyList())
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun ambilDataLogs() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.instance.getActivityLogs().execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val logData = response.body()!!.data
                        if (logData.isNotEmpty()) {
                            if (::logAdapter.isInitialized) {
                                logAdapter.updateData(logData) // Update List secara halus
                            } else {
                                logAdapter = AdminLogAdapter(logData)
                                rvAdminLogs.adapter = logAdapter
                            }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // =========================================================
    // FUNGSI SETUP DIAGRAM (Dengan Logika "Anti-Berkedip")
    // =========================================================

    private fun setupLineChart(dataBulanan: List<StatsBulanan>) {
        val isFirstLoad = lineChartPendapatan.data == null
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        for ((index, item) in dataBulanan.withIndex()) {
            entries.add(Entry(index.toFloat(), item.total.toFloatOrNull() ?: 0f))
            labels.add(item.bulan)
        }

        val dataSet = LineDataSet(entries, "Pendapatan").apply {
            color = Color.parseColor("#35A1F8")
            lineWidth = 3f
            circleRadius = 5f
            setCircleColor(Color.parseColor("#35A1F8"))
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        lineChartPendapatan.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.textColor = Color.parseColor("#1E1E1E")
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.parseColor("#757575")
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            axisLeft.textColor = Color.parseColor("#757575")
            axisRight.isEnabled = false
            if (isFirstLoad) animateY(1000) // Animasi hanya saat pertama buka
            invalidate()
        }
    }

    private fun setupPieChart(dataStatus: List<StatsStatus>) {
        val isFirstLoad = pieChartStatus.data == null
        val entries = ArrayList<PieEntry>()
        for (item in dataStatus) entries.add(PieEntry(item.jumlah.toFloatOrNull() ?: 0f, item.status))

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#FFC107"), Color.parseColor("#F44336"))
            valueTextColor = Color.parseColor("#1E1E1E")
            valueTextSize = 11f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
            }
        }

        pieChartStatus.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 45f
            setHoleColor(Color.TRANSPARENT)
            legend.textColor = Color.parseColor("#1E1E1E")
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            if (isFirstLoad) animateY(1000)
            invalidate()
        }
    }

    private fun setupBarChart(dataPopuler: List<StatsPopuler>) {
        val isFirstLoad = barChartProduk.data == null
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, item) in dataPopuler.withIndex()) {
            entries.add(BarEntry(index.toFloat(), item.jumlah.toFloatOrNull() ?: 0f))
            labels.add(item.produk)
        }

        val dataSet = BarDataSet(entries, "Total Pemesanan").apply {
            color = Color.parseColor("#35A1F8")
            valueTextColor = Color.parseColor("#1E1E1E")
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() { override fun getFormattedValue(value: Float): String = value.toInt().toString() }
        }

        barChartProduk.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.textColor = Color.parseColor("#1E1E1E")
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.parseColor("#757575")
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            axisLeft.textColor = Color.parseColor("#757575")
            axisRight.isEnabled = false
            if (isFirstLoad) animateY(1000)
            invalidate()
        }
    }

    private fun setupBarChartWisata(dataList: List<StatsKategori>) {
        if (dataList.isEmpty()) { barChartWisata.clear(); return }
        val isFirstLoad = barChartWisata.data == null
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, item) in dataList.withIndex()) {
            entries.add(BarEntry(index.toFloat(), item.jumlah.toFloatOrNull() ?: 0f))
            labels.add(item.kategori)
        }

        val dataSet = BarDataSet(entries, "Total Pemesanan Tiket").apply {
            color = Color.parseColor("#FF9800")
            valueTextColor = Color.parseColor("#1E1E1E")
            valueTextSize = 11f
            valueFormatter = object : ValueFormatter() { override fun getFormattedValue(value: Float): String = value.toInt().toString() }
        }

        barChartWisata.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.textColor = Color.parseColor("#1E1E1E")
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.parseColor("#757575")
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            axisLeft.textColor = Color.parseColor("#757575")
            axisRight.isEnabled = false
            if (isFirstLoad) animateY(1000)
            invalidate()
        }
    }

    private fun setupBarChartTransport(dataList: List<StatsKategori>) {
        if (dataList.isEmpty()) { barChartTransport.clear(); return }
        val isFirstLoad = barChartTransport.data == null
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, item) in dataList.withIndex()) {
            entries.add(BarEntry(index.toFloat(), item.jumlah.toFloatOrNull() ?: 0f))
            labels.add(item.kategori)
        }

        val dataSet = BarDataSet(entries, "Total Pemesanan Transport").apply {
            color = Color.parseColor("#4CAF50")
            valueTextColor = Color.parseColor("#1E1E1E")
            valueTextSize = 11f
            valueFormatter = object : ValueFormatter() { override fun getFormattedValue(value: Float): String = value.toInt().toString() }
        }

        barChartTransport.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.textColor = Color.parseColor("#1E1E1E")
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.parseColor("#757575")
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            axisLeft.textColor = Color.parseColor("#757575")
            axisRight.isEnabled = false
            if (isFirstLoad) animateY(1000)
            invalidate()
        }
    }

    private fun setupBarChartDaerah(dataList: List<StatsDaerahResponse>) {
        if (dataList.isEmpty()) { barChartDaerah.clear(); return }
        val isFirstLoad = barChartDaerah.data == null
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, item) in dataList.withIndex()) {
            entries.add(BarEntry(index.toFloat(), item.jumlah.toFloatOrNull() ?: 0f))
            labels.add(item.daerah)
        }

        val dataSet = BarDataSet(entries, "Total Kunjungan").apply {
            color = Color.parseColor("#9C27B0")
            valueTextColor = Color.parseColor("#1E1E1E")
            valueTextSize = 11f
            valueFormatter = object : ValueFormatter() { override fun getFormattedValue(value: Float): String = value.toInt().toString() }
        }

        barChartDaerah.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.textColor = Color.parseColor("#1E1E1E")
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.parseColor("#757575")
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            axisLeft.textColor = Color.parseColor("#757575")
            axisRight.isEnabled = false
            if (isFirstLoad) animateY(1000)
            invalidate()
        }
    }
}