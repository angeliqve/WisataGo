package com.app.wisatago.transport

import android.content.Intent
import android.os.Bundle
import android.view.View
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
    private lateinit var chipHargaTerendah: Chip
    private lateinit var chipKeberangkatanPagi: Chip

    // Data untuk Kereta
    private var trainList: List<TicketResponse> = emptyList()
    private lateinit var trainAdapter: TrainTicketAdapter

    // Data untuk Bus
    private var busList: List<BusSchedule> = emptyList()
    private lateinit var busAdapter: BusTicketAdapter

    // Data untuk Pesawat
    private var flightList: List<FlightSchedule> = emptyList()
    private lateinit var flightAdapter: FlightTicketAdapter

    // Variable umum
    private var transportType: String = "train"  // "train" atau "bus"
    private var origin: String = ""
    private var destination: String = ""
    private var datePergi: String = ""
    private var datePulang: String? = null
    private var passengerCount: Int = 1
    private var isReturnTrip: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_result)

        // Baca jenis transportasi
        transportType = intent.getStringExtra("EXTRA_TRANSPORT_TYPE") ?: "train"

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
        chipHargaTerendah = findViewById(R.id.chip_harga_terendah)
        chipKeberangkatanPagi = findViewById(R.id.chip_keberangkatan_pagi)

        tvSummaryLocation.text = "$origin ➔ $destination"

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

        // Setup adapter berdasarkan jenis transportasi
        if (transportType == "bus") {
            // TAMPILAN BUS
            busAdapter = BusTicketAdapter(emptyList(), passengerCount) { tiketTerpilih ->
                handleBusTicketClick(tiketTerpilih)
            }
            rvTicketList.adapter = busAdapter

            tvSummaryDetails.text = "Mencari bus..."
            cariJadwalBus(origin, destination, datePergi)

        } else if (transportType == "flight" || transportType == "pesawat") {
            // TAMPILAN PESAWAT
            flightAdapter = FlightTicketAdapter(emptyList(), passengerCount) { tiketTerpilih ->
                handleFlightTicketClick(tiketTerpilih)
            }
            rvTicketList.adapter = flightAdapter

            tvSummaryDetails.text = "Mencari penerbangan..."
            cariJadwalPesawat(origin, destination, datePergi)

        } else {
            // TAMPILAN KERETA
            trainAdapter = TrainTicketAdapter(emptyList(), passengerCount) { tiketTerpilih ->
                handleTrainTicketClick(tiketTerpilih)
            }
            rvTicketList.adapter = trainAdapter

            tvSummaryDetails.text = "Mencari tiket kereta..."
            cariJadwalKereta(origin, destination, datePergi)
        }

        setupFilterListeners()
    }

    // handle klik tiket kereta
    private fun handleTrainTicketClick(tiketTerpilih: TicketResponse) {
        val datePulangLocal = datePulang

        if (!datePulangLocal.isNullOrEmpty() && !datePulangLocal.contains("DD, 00") && !isReturnTrip) {
            Toast.makeText(
                this,
                "Menyimpan tiket pergi. Silakan pilih tiket pulang!",
                Toast.LENGTH_SHORT
            ).show()

            val intentPulang = Intent(this, TicketResultActivity::class.java)
            intentPulang.putExtra("EXTRA_TRANSPORT_TYPE", "train")
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
            Toast.makeText(this, "Melanjutkan ke Pembayaran...", Toast.LENGTH_SHORT).show()

            val intentCheckout = Intent(this@TicketResultActivity, CheckoutActivity::class.java)
            intentCheckout.putExtra("EXTRA_TRANSPORT_TYPE", "Tiket Kereta Api")
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

    // Handle klik tiket bus
    private fun handleBusTicketClick(tiketTerpilih: BusSchedule) {
        val datePulangLocal = datePulang

        if (!datePulangLocal.isNullOrEmpty() && !datePulangLocal.contains("DD, 00") && !isReturnTrip) {
            Toast.makeText(
                this,
                "Menyimpan tiket pergi. Silakan pilih tiket pulang!",
                Toast.LENGTH_SHORT
            ).show()

            val intentPulang = Intent(this, TicketResultActivity::class.java)
            intentPulang.putExtra("EXTRA_TRANSPORT_TYPE", "bus")
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

    private fun handleFlightTicketClick(tiketTerpilih: FlightSchedule) {
        val datePulangLocal = datePulang

        if (!datePulangLocal.isNullOrEmpty() && !datePulangLocal.contains("DD, 00") && !isReturnTrip) {
            Toast.makeText(this, "Menyimpan tiket pergi. Silakan pilih tiket pulang!", Toast.LENGTH_SHORT).show()

            val intentPulang = Intent(this, TicketResultActivity::class.java)
            intentPulang.putExtra("EXTRA_TRANSPORT_TYPE", "flight")
            intentPulang.putExtra("EXTRA_ORIGIN", destination)
            intentPulang.putExtra("EXTRA_DESTINATION", origin)
            intentPulang.putExtra("EXTRA_DATE_PERGI", datePulangLocal)
            intentPulang.putExtra("EXTRA_DATE_PULANG", "")
            intentPulang.putExtra("EXTRA_IS_RETURN_TRIP", true)
            intentPulang.putExtra("EXTRA_PASSENGERS", passengerCount)

            intentPulang.putExtra("TIKET_PERGI_NAME", tiketTerpilih.airline_name)
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
            intentCheckout.putExtra("EXTRA_TRANSPORT_TYPE", "Tiket Pesawat")
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

                intentCheckout.putExtra("EXTRA_PULANG_NAME", tiketTerpilih.airline_name)
                intentCheckout.putExtra("EXTRA_PULANG_SCHEDULE_ID", tiketTerpilih.schedule_id)
                intentCheckout.putExtra("EXTRA_PULANG_CLASS", tiketTerpilih.class_type)
                intentCheckout.putExtra("EXTRA_PULANG_DEP_TIME", tiketTerpilih.departure_time)
                intentCheckout.putExtra("EXTRA_PULANG_ARR_TIME", tiketTerpilih.arrival_time)
                intentCheckout.putExtra("EXTRA_PULANG_PRICE", tiketTerpilih.price)
                intentCheckout.putExtra("EXTRA_PULANG_DATE", datePergi)
                intentCheckout.putExtra("EXTRA_PULANG_ORIGIN", origin)
                intentCheckout.putExtra("EXTRA_PULANG_DESTINATION", destination)

            } else {
                intentCheckout.putExtra("EXTRA_PERGI_NAME", tiketTerpilih.airline_name)
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

    private fun cariJadwalKereta(asal: String, tujuan: String, tanggalRaw: String) {
        val formatDatabase = konversiTanggalKeFormatDatabase(tanggalRaw)
        ApiClient.instance.searchTickets(asal, tujuan, formatDatabase).enqueue(object :
            Callback<List<TicketResponse>> {
            override fun onResponse(call: Call<List<TicketResponse>>, response: Response<List<TicketResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    trainList = response.body()!!
                    if (trainList.isEmpty()) {
                        tvSummaryDetails.text = "0 Tiket Kereta Tersedia"
                        Toast.makeText(this@TicketResultActivity, "Jadwal kereta tidak ditemukan", Toast.LENGTH_SHORT).show()
                    } else {
                        tvSummaryDetails.text = "${trainList.size} Tiket Kereta Tersedia"
                        trainAdapter.updateData(trainList)
                    }
                }
            }
            override fun onFailure(call: Call<List<TicketResponse>>, t: Throwable) {
                tvSummaryDetails.text = "Koneksi bermasalah"
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
                    if (busList.isEmpty()) {
                        tvSummaryDetails.text = "0 Bus Tersedia"
                        Toast.makeText(this@TicketResultActivity, "Jadwal bus tidak ditemukan", Toast.LENGTH_SHORT).show()
                    } else {
                        tvSummaryDetails.text = "${busList.size} Bus Tersedia"
                        busAdapter.updateData(busList)
                    }
                }
            }
            override fun onFailure(call: Call<List<BusSchedule>>, t: Throwable) {
                tvSummaryDetails.text = "Koneksi bermasalah"
                Toast.makeText(this@TicketResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cariJadwalPesawat(asal: String, tujuan: String, tanggalRaw: String) {
        val formatDatabase = konversiTanggalKeFormatDatabase(tanggalRaw)

        ApiClient.instance.searchFlightSchedules(asal, tujuan, formatDatabase).enqueue(object :
            Callback<List<FlightSchedule>> {
            override fun onResponse(call: Call<List<FlightSchedule>>, response: Response<List<FlightSchedule>>) {
                if (response.isSuccessful && response.body() != null) {
                    flightList = response.body()!!
                    if (flightList.isEmpty()) {
                        tvSummaryDetails.text = "0 Penerbangan Tersedia"
                        Toast.makeText(this@TicketResultActivity, "Jadwal pesawat tidak ditemukan", Toast.LENGTH_SHORT).show()
                    } else {
                        tvSummaryDetails.text = "${flightList.size} Penerbangan Tersedia"
                        flightAdapter.updateData(flightList)
                    }
                }
            }
            override fun onFailure(call: Call<List<FlightSchedule>>, t: Throwable) {
                tvSummaryDetails.text = "Koneksi bermasalah"
                Toast.makeText(this@TicketResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun konversiTanggalKeFormatDatabase(tanggalRaw: String): String {
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Coba Format 1: Dari menu Pesawat (Contoh: "Sen, 15 Jun 2026")
        try {
            val inputFormat1 = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
            val date1 = inputFormat1.parse(tanggalRaw)
            if (date1 != null) return outputFormat.format(date1)
        } catch (e: Exception) { }

        // Coba Format 2: Dari menu Kereta/Bus (Contoh: "15/06/2026")
        try {
            val inputFormat2 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date2 = inputFormat2.parse(tanggalRaw)
            if (date2 != null) return outputFormat.format(date2)
        } catch (e: Exception) { }

        // Jika semua format gagal, kembalikan teks aslinya agar API tetap berjalan
        return tanggalRaw
    }

    private fun setupFilterListeners() {
        val filterAction = {
            if (transportType == "bus") {
                applyBusFilters()
            } else if (transportType == "flight" || transportType == "pesawat") {
                applyFlightFilters()
            } else {
                applyTrainFilters()
            }
        }
        chipHargaTerendah.setOnCheckedChangeListener { _, _ -> filterAction() }
        chipKeberangkatanPagi.setOnCheckedChangeListener { _, _ -> filterAction() }
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
        busAdapter.updateData(filteredList)
        tvSummaryDetails.text = "${filteredList.size} Bus Tersedia"
    }

    private fun applyFlightFilters() {
        var filteredList = flightList
        if (chipHargaTerendah.isChecked) {
            filteredList = filteredList.sortedBy { it.price }
        }
        if (chipKeberangkatanPagi.isChecked) {
            filteredList = filteredList.filter {
                (it.departure_time.split(":")[0].toIntOrNull() ?: 24) in 4..11
            }
        }
        flightAdapter.updateData(filteredList)
        tvSummaryDetails.text = "${filteredList.size} Penerbangan Tersedia"
    }
}