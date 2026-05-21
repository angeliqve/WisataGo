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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class TicketResultActivity : AppCompatActivity() {

    private lateinit var rvTrainList: RecyclerView
    private lateinit var adapter: TrainTicketAdapter
    private lateinit var tvSummaryLocation: TextView
    private lateinit var tvSummaryDate: TextView
    private lateinit var tvSummaryDetails: TextView
    private lateinit var btnBackList: ImageButton
    private lateinit var chipHargaTerendah: Chip
    private lateinit var chipKeberangkatanPagi: Chip

    private var originalList: List<TicketResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_result)

        val origin = intent.getStringExtra("EXTRA_ORIGIN") ?: ""
        val destination = intent.getStringExtra("EXTRA_DESTINATION") ?: ""
        val datePergi = intent.getStringExtra("EXTRA_DATE_PERGI") ?: "Tanggal tidak diketahui"
        val datePulang = intent.getStringExtra("EXTRA_DATE_PULANG")
        val passengerCount = intent.getIntExtra("EXTRA_PASSENGERS", 1)
        val isReturnTrip = intent.getBooleanExtra("EXTRA_IS_RETURN_TRIP", false)

        tvSummaryLocation = findViewById(R.id.tv_summary_location)
        tvSummaryDate = findViewById(R.id.tv_summary_date)
        tvSummaryDetails = findViewById(R.id.tv_summary_details)
        btnBackList = findViewById(R.id.btn_back_list)
        rvTrainList = findViewById(R.id.rv_train_list)
        chipHargaTerendah = findViewById(R.id.chip_harga_terendah)
        chipKeberangkatanPagi = findViewById(R.id.chip_keberangkatan_pagi)

        tvSummaryLocation.text = "$origin ➔ $destination"

        if (isReturnTrip) {
            tvSummaryDate.text = "$datePergi (Pilih Tiket Pulang)"
        } else {
            if (datePulang != null && datePulang.isNotEmpty() && !datePulang.contains("DD, 00")) {
                tvSummaryDate.text = "$datePergi (Pilih Tiket Pergi)"
            } else {
                tvSummaryDate.text = datePergi
            }
        }

        tvSummaryDetails.text = "Mencari tiket..."
        btnBackList.setOnClickListener { finish() }
        rvTrainList.layoutManager = LinearLayoutManager(this)

        // ========================================================
        // 🟢 LOGIKA KLIK TIKET: PERGI -> PULANG -> CHECKOUT
        // ========================================================
        adapter = TrainTicketAdapter(emptyList(), passengerCount) { tiketTerpilih ->

            if (!datePulang.isNullOrEmpty() && !datePulang.contains("DD, 00") && !isReturnTrip) {
                // TAHAP 1: SIMPAN TIKET PERGI DAN BUKA PENCARIAN TIKET PULANG
                Toast.makeText(
                    this,
                    "Menyimpan tiket pergi. Silakan pilih tiket pulang!",
                    Toast.LENGTH_SHORT
                ).show()

                val intentPulang = Intent(this, TicketResultActivity::class.java)
                intentPulang.putExtra("EXTRA_ORIGIN", destination)
                intentPulang.putExtra("EXTRA_DESTINATION", origin)
                intentPulang.putExtra(
                    "EXTRA_DATE_PERGI",
                    datePulang
                ) // Jadikan tgl pulang sbg tgl utama pencarian
                intentPulang.putExtra("EXTRA_DATE_PULANG", "")
                intentPulang.putExtra("EXTRA_IS_RETURN_TRIP", true)
                intentPulang.putExtra("EXTRA_PASSENGERS", passengerCount)

                // 🟢 SIMPAN SEMUA DATA TIKET PERGI
                intentPulang.putExtra("TIKET_PERGI_NAME", tiketTerpilih.train_name)
                intentPulang.putExtra("TIKET_PERGI_CLASS", tiketTerpilih.class_type)
                intentPulang.putExtra("TIKET_PERGI_DEP_TIME", tiketTerpilih.departure_time)
                intentPulang.putExtra("TIKET_PERGI_ARR_TIME", tiketTerpilih.arrival_time)
                intentPulang.putExtra("TIKET_PERGI_PRICE", tiketTerpilih.price)
                intentPulang.putExtra("TIKET_PERGI_DATE", datePergi)
                intentPulang.putExtra("TIKET_PERGI_SCHEDULE_ID", tiketTerpilih.ticket_id)
                startActivity(intentPulang)

            } else {
                // TAHAP 2: MENUJU CHECKOUT (Kirim Tiket Pergi & Pulang)
                Toast.makeText(this, "Melanjutkan ke Pembayaran...", Toast.LENGTH_SHORT).show()

                val intentCheckout = Intent(this@TicketResultActivity, CheckoutActivity::class.java)
                intentCheckout.putExtra("EXTRA_TRANSPORT_TYPE", "Tiket Kereta Api")
                intentCheckout.putExtra("EXTRA_PASSENGERS", passengerCount)
                intentCheckout.putExtra("EXTRA_IS_RETURN_TRIP", isReturnTrip)

                if (isReturnTrip) {
                    // 🟢 MENGIRIM TIKET PERGI
                    intentCheckout.putExtra(
                        "EXTRA_PERGI_NAME",
                        intent.getStringExtra("TIKET_PERGI_NAME")
                    )
                    intentCheckout.putExtra(
                        "EXTRA_PERGI_SCHEDULE_ID",
                        intent.getStringExtra("TIKET_PERGI_SCHEDULE_ID")
                    )
                    intentCheckout.putExtra(
                        "EXTRA_PERGI_NAME",
                        intent.getStringExtra("TIKET_PERGI_NAME")
                    )
                    intentCheckout.putExtra(
                        "EXTRA_PERGI_CLASS",
                        intent.getStringExtra("TIKET_PERGI_CLASS")
                    )
                    intentCheckout.putExtra(
                        "EXTRA_PERGI_DEP_TIME",
                        intent.getStringExtra("TIKET_PERGI_DEP_TIME")
                    )
                    intentCheckout.putExtra(
                        "EXTRA_PERGI_ARR_TIME",
                        intent.getStringExtra("TIKET_PERGI_ARR_TIME")
                    )
                    intentCheckout.putExtra(
                        "EXTRA_PERGI_PRICE",
                        intent.getDoubleExtra("TIKET_PERGI_PRICE", 0.0)
                    )
                    intentCheckout.putExtra(
                        "EXTRA_PERGI_DATE",
                        intent.getStringExtra("TIKET_PERGI_DATE")
                    )
                    intentCheckout.putExtra(
                        "EXTRA_PERGI_ORIGIN",
                        destination
                    ) // Origin tiket pergi = Destination tiket pulang
                    intentCheckout.putExtra("EXTRA_PERGI_DESTINATION", origin)

                    // 🟢 MENGIRIM TIKET PULANG (Yang baru saja diklik)
                    intentCheckout.putExtra("EXTRA_PULANG_NAME", tiketTerpilih.train_name)
                    intentCheckout.putExtra("EXTRA_PULANG_SCHEDULE_ID", tiketTerpilih.ticket_id)
                    intentCheckout.putExtra("EXTRA_PULANG_NAME", tiketTerpilih.train_name)
                    intentCheckout.putExtra("EXTRA_PULANG_CLASS", tiketTerpilih.class_type)
                    intentCheckout.putExtra("EXTRA_PULANG_DEP_TIME", tiketTerpilih.departure_time)
                    intentCheckout.putExtra("EXTRA_PULANG_ARR_TIME", tiketTerpilih.arrival_time)
                    intentCheckout.putExtra("EXTRA_PULANG_PRICE", tiketTerpilih.price)
                    intentCheckout.putExtra(
                        "EXTRA_PULANG_DATE",
                        datePergi
                    ) // datePergi di hal. ini adalah tgl pulang
                    intentCheckout.putExtra("EXTRA_PULANG_ORIGIN", origin)
                    intentCheckout.putExtra("EXTRA_PULANG_DESTINATION", destination)

                } else {
                    // 🟢 MENGIRIM TIKET PERGI (SATU ARAH)
                    intentCheckout.putExtra("EXTRA_PERGI_NAME", tiketTerpilih.train_name)
                    intentCheckout.putExtra("EXTRA_PERGI_SCHEDULE_ID", tiketTerpilih.ticket_id)
                    intentCheckout.putExtra("EXTRA_PERGI_NAME", tiketTerpilih.train_name)
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
        rvTrainList.adapter = adapter
        setupFilterListeners()
        cariJadwalTiket(origin, destination, datePergi)
    }

    private fun cariJadwalTiket(asal: String, tujuan: String, tanggalRaw: String) {
        val formatDatabase = konversiTanggalKeFormatDatabase(tanggalRaw)
        ApiClient.instance.searchTickets(asal, tujuan, formatDatabase).enqueue(object :
            Callback<List<TicketResponse>> {
            override fun onResponse(call: Call<List<TicketResponse>>, response: Response<List<TicketResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    originalList = response.body()!!
                    if (originalList.isEmpty()) {
                        tvSummaryDetails.text = "0 Tiket Kereta Tersedia"
                        Toast.makeText(this@TicketResultActivity, "Jadwal tidak ditemukan", Toast.LENGTH_SHORT).show()
                    } else {
                        tvSummaryDetails.text = "${originalList.size} Tiket Kereta Tersedia"
                        adapter.updateData(originalList)
                    }
                }
            }
            override fun onFailure(call: Call<List<TicketResponse>>, t: Throwable) {
                tvSummaryDetails.text = "Koneksi bermasalah"
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

    private fun setupFilterListeners() {
        val filterAction = {
            var filteredList = originalList
            if (chipHargaTerendah.isChecked) filteredList = filteredList.sortedBy { it.price }
            if (chipKeberangkatanPagi.isChecked) {
                filteredList = filteredList.filter { (it.departure_time.split(":")[0].toIntOrNull() ?: 24) in 4..11 }
            }
            adapter.updateData(filteredList)
            tvSummaryDetails.text = "${filteredList.size} Tiket Kereta Tersedia"
        }
        chipHargaTerendah.setOnCheckedChangeListener { _, _ -> filterAction() }
        chipKeberangkatanPagi.setOnCheckedChangeListener { _, _ -> filterAction() }
    }
}