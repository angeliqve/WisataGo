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
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        // 1. Tangkap data dari Intent
        val origin = intent.getStringExtra("EXTRA_ORIGIN") ?: ""
        val destination = intent.getStringExtra("EXTRA_DESTINATION") ?: ""
        val datePergi = intent.getStringExtra("EXTRA_DATE_PERGI") ?: "Tanggal tidak diketahui"
        val datePulang = intent.getStringExtra("EXTRA_DATE_PULANG")

        // 🟢 TANGKAP JUMLAH PENUMPANG (Default 1 jika tidak ada)
        val passengerCount = intent.getIntExtra("EXTRA_PASSENGERS", 1)

        // 🟢 TANGKAP STATUS: Apakah ini sedang mencari tiket pulang?
        val isReturnTrip = intent.getBooleanExtra("EXTRA_IS_RETURN_TRIP", false)

        // 🟢 TANGKAP DATA TIKET PERGI: Disimpan sementara jika ini adalah rute pulang
        val tiketPergiName = intent.getStringExtra("TIKET_PERGI_NAME") ?: ""
        val tiketPergiPrice = intent.getDoubleExtra("TIKET_PERGI_PRICE", 0.0)

        // 2. Hubungkan dengan komponen XML
        tvSummaryLocation = findViewById(R.id.tv_summary_location)
        tvSummaryDate = findViewById(R.id.tv_summary_date)
        tvSummaryDetails = findViewById(R.id.tv_summary_details)
        btnBackList = findViewById(R.id.btn_back_list)
        rvTrainList = findViewById(R.id.rv_train_list)
        chipHargaTerendah = findViewById(R.id.chip_harga_terendah)
        chipKeberangkatanPagi = findViewById(R.id.chip_keberangkatan_pagi)

        // 3. Set teks untuk Lokasi
        tvSummaryLocation.text = "$origin ➔ $destination"

        // 🟢 4. Set teks untuk Tanggal & Petunjuk Visual
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

        btnBackList.setOnClickListener {
            finish()
        }

        rvTrainList.layoutManager = LinearLayoutManager(this)

        // ========================================================
        // 🟢 LOGIKA KLIK TIKET: Penentuan Alur Pulang-Pergi
        // 🟢 TAMBAHAN: Masukkan passengerCount ke dalam Adapter
        // ========================================================
        adapter = TrainTicketAdapter(emptyList(), passengerCount) { tiketTerpilih ->

            // CEK KONDISI: Jika ada tanggal pulang DAN saat ini sedang mencari tiket pergi
            if (!datePulang.isNullOrEmpty() && !datePulang.contains("DD, 00") && !isReturnTrip) {

                Toast.makeText(this, "Menyimpan tiket pergi. Silakan pilih tiket pulang!", Toast.LENGTH_SHORT).show()

                // Buka kembali halaman ini tapi dengan rute yang dibalik
                val intentPulang = Intent(this, TicketResultActivity::class.java)

                intentPulang.putExtra("EXTRA_ORIGIN", destination)
                intentPulang.putExtra("EXTRA_DESTINATION", origin)

                // Tanggal pulang dijadikan parameter utama untuk pencarian
                intentPulang.putExtra("EXTRA_DATE_PERGI", datePulang)
                intentPulang.putExtra("EXTRA_DATE_PULANG", "") // Kosongkan agar tidak looping
                intentPulang.putExtra("EXTRA_IS_RETURN_TRIP", true)

                // Teruskan juga jumlah penumpangnya!
                intentPulang.putExtra("EXTRA_PASSENGERS", passengerCount)

                // Simpan info tiket pergi yang baru saja dipilih
                intentPulang.putExtra("TIKET_PERGI_NAME", tiketTerpilih.train_name)
                intentPulang.putExtra("TIKET_PERGI_PRICE", tiketTerpilih.price)

                startActivity(intentPulang)

            } else {
                // USER MEMILIH SEKALI JALAN ATAU BARU SAJA MEMILIH TIKET PULANG
                Toast.makeText(this, "Melanjutkan ke Pembayaran...", Toast.LENGTH_SHORT).show()

                // TODO: Di sini nanti kita akan memanggil CheckoutActivity
            }
        }
        rvTrainList.adapter = adapter

        setupFilterListeners()

        // Panggil API dengan parameter tanggal pencarian saat ini
        cariJadwalTiket(origin, destination, datePergi)
    }

    private fun cariJadwalTiket(asal: String, tujuan: String, tanggalRaw: String) {
        val formatDatabase = konversiTanggalKeFormatDatabase(tanggalRaw)

        ApiClient.instance.searchTickets(asal, tujuan, formatDatabase).enqueue(object : Callback<List<TicketResponse>> {
            override fun onResponse(
                call: Call<List<TicketResponse>>,
                response: Response<List<TicketResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    originalList = response.body()!!

                    if (originalList.isEmpty()) {
                        tvSummaryDetails.text = "0 Tiket Kereta Tersedia"
                        Toast.makeText(this@TicketResultActivity, "Jadwal tidak ditemukan pada tanggal ini", Toast.LENGTH_SHORT).show()
                    } else {
                        tvSummaryDetails.text = "${originalList.size} Tiket Kereta Tersedia"
                        adapter.updateData(originalList)
                    }
                } else {
                    tvSummaryDetails.text = "Gagal memuat jadwal"
                    Toast.makeText(this@TicketResultActivity, "Gagal memuat data jadwal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TicketResponse>>, t: Throwable) {
                tvSummaryDetails.text = "Koneksi bermasalah"
                Toast.makeText(this@TicketResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun konversiTanggalKeFormatDatabase(tanggalLokal: String): String {
        return try {
            val formatInput = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale("id", "ID"))
            val formatOutput = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = formatInput.parse(tanggalLokal)
            formatOutput.format(date!!)
        } catch (e: Exception) {
            ""
        }
    }

    private fun setupFilterListeners() {
        val filterAction = {
            var filteredList = originalList

            if (chipHargaTerendah.isChecked) {
                filteredList = filteredList.sortedBy { it.price }
            }

            if (chipKeberangkatanPagi.isChecked) {
                filteredList = filteredList.filter { tiket ->
                    val jam = tiket.departure_time.split(":").firstOrNull()?.toIntOrNull() ?: 24
                    jam in 4..11
                }
            }

            adapter.updateData(filteredList)
            tvSummaryDetails.text = "${filteredList.size} Tiket Kereta Tersedia"
        }

        chipHargaTerendah.setOnCheckedChangeListener { _, _ -> filterAction() }
        chipKeberangkatanPagi.setOnCheckedChangeListener { _, _ -> filterAction() }
    }
}