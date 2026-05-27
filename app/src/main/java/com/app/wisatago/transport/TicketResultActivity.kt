package com.app.wisatago.transport

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.ApiClient
import com.app.wisatago.R
import com.app.wisatago.booking.CheckoutActivity
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class TicketResultActivity : AppCompatActivity() {

    private lateinit var rvTicketList: RecyclerView
    private lateinit var tvSummaryLocation: TextView
    private lateinit var tvSummaryDate: TextView
    private lateinit var tvSummaryDetails: TextView
    private lateinit var btnBackList: ImageButton
    private lateinit var tabLayout: TabLayout
    private lateinit var chipHargaTerendah: Chip
    private lateinit var chipKeberangkatanPagi: Chip
    private lateinit var chipBusPremium: Chip

    // Data untuk Kereta
    private var trainList: List<TicketResponse> = emptyList()
    private lateinit var trainAdapter: TrainTicketAdapter

    // Data untuk Bus
    private var busList: List<BusSchedule> = emptyList()
    private lateinit var busAdapter: BusTicketAdapter

    // Variable umum
    private var currentTab: String = "train" // "train" atau "bus"
    private var origin: String = ""
    private var destination: String = ""
    private var datePergi: String = ""
    private var datePulang: String? = null
    private var passengerCount: Int = 1
    private var isReturnTrip: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_result)

        origin = intent.getStringExtra("EXTRA_ORIGIN") ?: ""
        destination = intent.getStringExtra("EXTRA_DESTINATION") ?: ""
        datePergi = intent.getStringExtra("EXTRA_DATE_PERGI") ?: "Tanggal tidak diketahui"
        datePulang = intent.getStringExtra("EXTRA_DATE_PULANG")
        passengerCount = intent.getIntExtra("EXTRA_PASSENGERS", 1)
        isReturnTrip = intent.getBooleanExtra("EXTRA_IS_RETURN_TRIP", false)

        tvSummaryLocation = findViewById(R.id.tv_summary_location)
        tvSummaryDate = findViewById(R.id.tv_summary_date)
        tvSummaryDetails = findViewById(R.id.tv_summary_details)
        btnBackList = findViewById(R.id.btn_back_list)
        rvTicketList = findViewById(R.id.rv_train_list)
        tabLayout = findViewById(R.id.tab_layout_transport)
        chipHargaTerendah = findViewById(R.id.chip_harga_terendah)
        chipKeberangkatanPagi = findViewById(R.id.chip_keberangkatan_pagi)
        chipBusPremium = findViewById(R.id.chip_bus_premium)

        // Sembunyikan chip bus premium dulu (akan muncul jika tab bus dipilih)
        chipBusPremium.visibility = android.view.View.GONE

        tvSummaryLocation.text = "$origin ➔ $destination"

        // 🔥 PERBAIKAN 1: Gunakan variabel lokal untuk menghindari smart cast error
        val datePulangLocal = datePulang

        if (isReturnTrip) {
            tvSummaryDate.text = "$datePergi (Pilih Tiket Pulang)"
        } else {
            if (datePulangLocal != null && datePulangLocal.isNotEmpty() && !datePulangLocal.contains("DD, 00")) {
                tvSummaryDate.text = "$datePergi (Pilih Tiket Pergi)"
            } else {
                tvSummaryDate.text = datePergi
            }
        }

        btnBackList.setOnClickListener { finish() }
        rvTicketList.layoutManager = LinearLayoutManager(this)

        // ==========================================
        // INISIALISASI ADAPTER (TETAP TERPISAH)
        // ==========================================

        // Adapter untuk KERETA
        trainAdapter = TrainTicketAdapter(emptyList(), passengerCount) { tiketTerpilih ->
            handleTrainTicketClick(tiketTerpilih)
        }

        // Adapter untuk BUS
        busAdapter = BusTicketAdapter(emptyList(), passengerCount) { tiketTerpilih ->
            handleBusTicketClick(tiketTerpilih)
        }

        // Set default adapter (kereta dulu)
        rvTicketList.adapter = trainAdapter

        // ==========================================
        // TAB LAYOUT UNTUK SWITCH KERETA/BUS
        // ==========================================
        tabLayout.addTab(tabLayout.newTab().setText("🚆 Kereta"))
        tabLayout.addTab(tabLayout.newTab().setText("🚌 Bus"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentTab = "train"
                        rvTicketList.adapter = trainAdapter
                        chipBusPremium.visibility = android.view.View.GONE
                        tvSummaryDetails.text = if (trainList.isNotEmpty()) "${trainList.size} Tiket Kereta Tersedia" else "Mencari tiket kereta..."
                        if (trainList.isNotEmpty()) {
                            applyTrainFilters()
                        } else {
                            cariJadwalKereta(origin, destination, datePergi)
                        }
                    }
                    1 -> {
                        currentTab = "bus"
                        rvTicketList.adapter = busAdapter
                        chipBusPremium.visibility = android.view.View.VISIBLE
                        tvSummaryDetails.text = if (busList.isNotEmpty()) "${busList.size} Bus Tersedia" else "Mencari bus..."
                        if (busList.isNotEmpty()) {
                            applyBusFilters()
                        } else {
                            cariJadwalBus(origin, destination, datePergi)
                        }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Load data kereta dulu
        tvSummaryDetails.text = "Mencari tiket kereta..."
        cariJadwalKereta(origin, destination, datePergi)

        // Load data bus di background
        cariJadwalBus(origin, destination, datePergi)

        setupFilterListeners()
    }

    // ==========================================
    // HANDLE KLIK TIKET KERETA
    // ==========================================

    private fun handleTrainTicketClick(tiketTerpilih: TicketResponse) {
        // 🔥 PERBAIKAN 2: Gunakan variabel lokal
        val datePulangLocal = datePulang

        if (!datePulangLocal.isNullOrEmpty() && !datePulangLocal.contains("DD, 00") && !isReturnTrip) {
            // TAHAP 1: SIMPAN TIKET PERGI DAN BUKA PENCARIAN TIKET PULANG
            Toast.makeText(
                this,
                "Menyimpan tiket pergi. Silakan pilih tiket pulang!",
                Toast.LENGTH_SHORT
            ).show()

            val intentPulang = Intent(this, TicketResultActivity::class.java)
            intentPulang.putExtra("EXTRA_ORIGIN", destination)
            intentPulang.putExtra("EXTRA_DESTINATION", origin)
            intentPulang.putExtra("EXTRA_DATE_PERGI", datePulangLocal)
            intentPulang.putExtra("EXTRA_DATE_PULANG", "")
            intentPulang.putExtra("EXTRA_IS_RETURN_TRIP", true)
            intentPulang.putExtra("EXTRA_PASSENGERS", passengerCount)

            intentPulang.putExtra("TIKET_PERGI_NAME", tiketTerpilih.train_name)
            intentPulang.putExtra("TIKET_PERGI_CLASS", tiketTerpilih.class_type)
            intentPulang.putExtra("TIKET_PERGI_DEP_TIME", tiketTerpilih.departure_time)
            intentPulang.putExtra("TIKET_PERGI_ARR_TIME", tiketTerpilih.arrival_time)
            intentPulang.putExtra("TIKET_PERGI_PRICE", tiketTerpilih.price)
            intentPulang.putExtra("TIKET_PERGI_DATE", datePergi)
            intentPulang.putExtra("TIKET_PERGI_SCHEDULE_ID", tiketTerpilih.ticket_id)
            startActivity(intentPulang)

        } else {
            // TAHAP 2: MENUJU CHECKOUT
            Toast.makeText(this, "Melanjutkan ke Pembayaran...", Toast.LENGTH_SHORT).show()

            val intentCheckout = Intent(this@TicketResultActivity, CheckoutActivity::class.java)
            intentCheckout.putExtra("EXTRA_TRANSPORT_TYPE", "Tiket Kereta Api")
            intentCheckout.putExtra("EXTRA_PASSENGERS", passengerCount)
            intentCheckout.putExtra("EXTRA_IS_RETURN_TRIP", isReturnTrip)

            if (isReturnTrip) {
                // Kirim tiket pergi
                intentCheckout.putExtra("EXTRA_PERGI_NAME", intent.getStringExtra("TIKET_PERGI_NAME"))
                intentCheckout.putExtra("EXTRA_PERGI_SCHEDULE_ID", intent.getStringExtra("TIKET_PERGI_SCHEDULE_ID"))
                intentCheckout.putExtra("EXTRA_PERGI_CLASS", intent.getStringExtra("TIKET_PERGI_CLASS"))
                intentCheckout.putExtra("EXTRA_PERGI_DEP_TIME", intent.getStringExtra("TIKET_PERGI_DEP_TIME"))
                intentCheckout.putExtra("EXTRA_PERGI_ARR_TIME", intent.getStringExtra("TIKET_PERGI_ARR_TIME"))
                intentCheckout.putExtra("EXTRA_PERGI_PRICE", intent.getDoubleExtra("TIKET_PERGI_PRICE", 0.0))
                intentCheckout.putExtra("EXTRA_PERGI_DATE", intent.getStringExtra("TIKET_PERGI_DATE"))
                intentCheckout.putExtra("EXTRA_PERGI_ORIGIN", destination)
                intentCheckout.putExtra("EXTRA_PERGI_DESTINATION", origin)

                // Kirim tiket pulang
                intentCheckout.putExtra("EXTRA_PULANG_NAME", tiketTerpilih.train_name)
                intentCheckout.putExtra("EXTRA_PULANG_SCHEDULE_ID", tiketTerpilih.ticket_id)
                intentCheckout.putExtra("EXTRA_PULANG_CLASS", tiketTerpilih.class_type)
                intentCheckout.putExtra("EXTRA_PULANG_DEP_TIME", tiketTerpilih.departure_time)
                intentCheckout.putExtra("EXTRA_PULANG_ARR_TIME", tiketTerpilih.arrival_time)
                intentCheckout.putExtra("EXTRA_PULANG_PRICE", tiketTerpilih.price)
                intentCheckout.putExtra("EXTRA_PULANG_DATE", datePergi)
                intentCheckout.putExtra("EXTRA_PULANG_ORIGIN", origin)
                intentCheckout.putExtra("EXTRA_PULANG_DESTINATION", destination)

            } else {
                // Satu arah
                intentCheckout.putExtra("EXTRA_PERGI_NAME", tiketTerpilih.train_name)
                intentCheckout.putExtra("EXTRA_PERGI_SCHEDULE_ID", tiketTerpilih.ticket_id)
                intentCheckout.putExtra("EXTRA_PERGI_CLASS", tiketTerpilih.class_type)
                intentCheckout.putExtra("EXTRA_PERGI_DEP_TIME", tiketTerpilih.departure_time)
                intentCheckout.putExtra("EXTRA_PERGI_ARR_TIME", tiketTerpilih.arrival_time)
                intentCheckout.putExtra("EXTRA_PERGI_PRICE", tiketTerpilih.price)
                intentCheckout.putExtra("EXTRA_PERGI_DATE", datePergi)
                intentCheckout.putExtra("EXTRA_PERGI_ORIGIN", origin)
                intentCheckout.putExtra("EXTRA_PERGI_DESTINATION", destination)
            }

            startActivity(intentCheckout)
        }
    }

    // ==========================================
    // HANDLE KLIK TIKET BUS
    // ==========================================

    private fun handleBusTicketClick(tiketTerpilih: BusSchedule) {
        // 🔥 PERBAIKAN 3: Gunakan variabel lokal
        val datePulangLocal = datePulang

        if (!datePulangLocal.isNullOrEmpty() && !datePulangLocal.contains("DD, 00") && !isReturnTrip) {
            Toast.makeText(
                this,
                "Menyimpan tiket pergi. Silakan pilih tiket pulang!",
                Toast.LENGTH_SHORT
            ).show()

            val intentPulang = Intent(this, TicketResultActivity::class.java)
            intentPulang.putExtra("EXTRA_ORIGIN", destination)
            intentPulang.putExtra("EXTRA_DESTINATION", origin)
            intentPulang.putExtra("EXTRA_DATE_PERGI", datePulangLocal)
            intentPulang.putExtra("EXTRA_DATE_PULANG", "")
            intentPulang.putExtra("EXTRA_IS_RETURN_TRIP", true)
            intentPulang.putExtra("EXTRA_PASSENGERS", passengerCount)

            intentPulang.putExtra("TIKET_PERGI_NAME", tiketTerpilih.company_name)
            intentPulang.putExtra("TIKET_PERGI_CLASS", tiketTerpilih.class_type)
            intentPulang.putExtra("TIKET_PERGI_DEP_TIME", tiketTerpilih.departure_time)
            intentPulang.putExtra("TIKET_PERGI_ARR_TIME", tiketTerpilih.arrival_time)
            intentPulang.putExtra("TIKET_PERGI_PRICE", tiketTerpilih.price)
            intentPulang.putExtra("TIKET_PERGI_DATE", datePergi)
            intentPulang.putExtra("TIKET_PERGI_SCHEDULE_ID", tiketTerpilih.schedule_id)
            startActivity(intentPulang)

        } else {
            Toast.makeText(this, "Melanjutkan ke Pembayaran...", Toast.LENGTH_SHORT).show()

            val intentCheckout = Intent(this@TicketResultActivity, CheckoutActivity::class.java)
            intentCheckout.putExtra("EXTRA_TRANSPORT_TYPE", "Tiket Bus")
            intentCheckout.putExtra("EXTRA_PASSENGERS", passengerCount)
            intentCheckout.putExtra("EXTRA_IS_RETURN_TRIP", isReturnTrip)

            if (isReturnTrip) {
                intentCheckout.putExtra("EXTRA_PERGI_NAME", intent.getStringExtra("TIKET_PERGI_NAME"))
                intentCheckout.putExtra("EXTRA_PERGI_SCHEDULE_ID", intent.getStringExtra("TIKET_PERGI_SCHEDULE_ID"))
                intentCheckout.putExtra("EXTRA_PERGI_CLASS", intent.getStringExtra("TIKET_PERGI_CLASS"))
                intentCheckout.putExtra("EXTRA_PERGI_DEP_TIME", intent.getStringExtra("TIKET_PERGI_DEP_TIME"))
                intentCheckout.putExtra("EXTRA_PERGI_ARR_TIME", intent.getStringExtra("TIKET_PERGI_ARR_TIME"))
                intentCheckout.putExtra("EXTRA_PERGI_PRICE", intent.getDoubleExtra("TIKET_PERGI_PRICE", 0.0))
                intentCheckout.putExtra("EXTRA_PERGI_DATE", intent.getStringExtra("TIKET_PERGI_DATE"))
                intentCheckout.putExtra("EXTRA_PERGI_ORIGIN", destination)
                intentCheckout.putExtra("EXTRA_PERGI_DESTINATION", origin)

                intentCheckout.putExtra("EXTRA_PULANG_NAME", tiketTerpilih.company_name)
                intentCheckout.putExtra("EXTRA_PULANG_SCHEDULE_ID", tiketTerpilih.schedule_id)
                intentCheckout.putExtra("EXTRA_PULANG_CLASS", tiketTerpilih.class_type)
                intentCheckout.putExtra("EXTRA_PULANG_DEP_TIME", tiketTerpilih.departure_time)
                intentCheckout.putExtra("EXTRA_PULANG_ARR_TIME", tiketTerpilih.arrival_time)
                intentCheckout.putExtra("EXTRA_PULANG_PRICE", tiketTerpilih.price)
                intentCheckout.putExtra("EXTRA_PULANG_DATE", datePergi)
                intentCheckout.putExtra("EXTRA_PULANG_ORIGIN", origin)
                intentCheckout.putExtra("EXTRA_PULANG_DESTINATION", destination)

            } else {
                intentCheckout.putExtra("EXTRA_PERGI_NAME", tiketTerpilih.company_name)
                intentCheckout.putExtra("EXTRA_PERGI_SCHEDULE_ID", tiketTerpilih.schedule_id)
                intentCheckout.putExtra("EXTRA_PERGI_CLASS", tiketTerpilih.class_type)
                intentCheckout.putExtra("EXTRA_PERGI_DEP_TIME", tiketTerpilih.departure_time)
                intentCheckout.putExtra("EXTRA_PERGI_ARR_TIME", tiketTerpilih.arrival_time)
                intentCheckout.putExtra("EXTRA_PERGI_PRICE", tiketTerpilih.price)
                intentCheckout.putExtra("EXTRA_PERGI_DATE", datePergi)
                intentCheckout.putExtra("EXTRA_PERGI_ORIGIN", origin)
                intentCheckout.putExtra("EXTRA_PERGI_DESTINATION", destination)
            }

            startActivity(intentCheckout)
        }
    }

    // ==========================================
    // API CALLS
    // ==========================================

    private fun cariJadwalKereta(asal: String, tujuan: String, tanggalRaw: String) {
        val formatDatabase = konversiTanggalKeFormatDatabase(tanggalRaw)
        ApiClient.instance.searchTickets(asal, tujuan, formatDatabase).enqueue(object :
            Callback<List<TicketResponse>> {
            override fun onResponse(call: Call<List<TicketResponse>>, response: Response<List<TicketResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    trainList = response.body()!!
                    if (currentTab == "train") {
                        if (trainList.isEmpty()) {
                            tvSummaryDetails.text = "0 Tiket Kereta Tersedia"
                            Toast.makeText(this@TicketResultActivity, "Jadwal kereta tidak ditemukan", Toast.LENGTH_SHORT).show()
                        } else {
                            tvSummaryDetails.text = "${trainList.size} Tiket Kereta Tersedia"
                            trainAdapter.updateData(trainList)
                        }
                    } else {
                        // Update data di background
                        trainAdapter.updateData(trainList)
                    }
                }
            }
            override fun onFailure(call: Call<List<TicketResponse>>, t: Throwable) {
                if (currentTab == "train") {
                    tvSummaryDetails.text = "Koneksi bermasalah"
                }
            }
        })
    }

    private fun cariJadwalBus(asal: String, tujuan: String, tanggalRaw: String) {
        val formatDatabase = konversiTanggalKeFormatDatabase(tanggalRaw)
        ApiClient.instance.searchBusSchedules(asal, tujuan, formatDatabase).enqueue(object :
            Callback<List<BusSchedule>> {
            override fun onResponse(call: Call<List<BusSchedule>>, response: Response<List<BusSchedule>>) {
                if (response.isSuccessful && response.body() != null) {
                    busList = response.body()!!
                    if (currentTab == "bus") {
                        if (busList.isEmpty()) {
                            tvSummaryDetails.text = "0 Bus Tersedia"
                            Toast.makeText(this@TicketResultActivity, "Jadwal bus tidak ditemukan", Toast.LENGTH_SHORT).show()
                        } else {
                            tvSummaryDetails.text = "${busList.size} Bus Tersedia"
                            busAdapter.updateData(busList)
                        }
                    } else {
                        // Update data di background
                        busAdapter.updateData(busList)
                    }
                }
            }
            override fun onFailure(call: Call<List<BusSchedule>>, t: Throwable) {
                if (currentTab == "bus") {
                    tvSummaryDetails.text = "Koneksi bermasalah"
                    Toast.makeText(this@TicketResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun konversiTanggalKeFormatDatabase(tanggalLokal: String): String {
        return try {
            val formatInput = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
            val formatOutput = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            formatOutput.format(formatInput.parse(tanggalLokal)!!)
        } catch (e: Exception) { "" }
    }

    // ==========================================
    // FILTER LISTENERS
    // ==========================================

    private fun setupFilterListeners() {
        val filterAction = {
            if (currentTab == "bus") {
                applyBusFilters()
            } else {
                applyTrainFilters()
            }
        }
        chipHargaTerendah.setOnCheckedChangeListener { _, _ -> filterAction() }
        chipKeberangkatanPagi.setOnCheckedChangeListener { _, _ -> filterAction() }
        chipBusPremium.setOnCheckedChangeListener { _, _ ->
            if (currentTab == "bus") applyBusFilters()
        }
    }

    private fun applyTrainFilters() {
        var filteredList = trainList
        if (chipHargaTerendah.isChecked) {
            filteredList = filteredList.sortedBy { it.price }
        }
        if (chipKeberangkatanPagi.isChecked) {
            filteredList = filteredList.filter {
                (it.departure_time.split(":")[0].toIntOrNull() ?: 24) in 4..11
            }
        }
        trainAdapter.updateData(filteredList)
        tvSummaryDetails.text = "${filteredList.size} Tiket Kereta Tersedia"
    }

    private fun applyBusFilters() {
        var filteredList = busList
        if (chipHargaTerendah.isChecked) {
            filteredList = filteredList.sortedBy { it.price }
        }
        if (chipKeberangkatanPagi.isChecked) {
            filteredList = filteredList.filter {
                (it.departure_time.split(":")[0].toIntOrNull() ?: 24) in 4..11
            }
        }
        if (chipBusPremium.isChecked) {
            filteredList = filteredList.filter {
                it.class_type == "Premium" || it.class_type == "VIP"
            }
        }
        busAdapter.updateData(filteredList)
        tvSummaryDetails.text = "${filteredList.size} Bus Tersedia"
    }
}