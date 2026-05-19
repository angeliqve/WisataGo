package com.app.wisatago.transport

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
    private lateinit var tvSummaryDate: TextView // 🟢 Variabel baru untuk tanggal
    private lateinit var tvSummaryDetails: TextView
    private lateinit var btnBackList: ImageButton

    // Tambahkan chip filter
    private lateinit var chipHargaTerendah: Chip
    private lateinit var chipKeberangkatanPagi: Chip

    // List untuk menyimpan data master asli dari server
    private var originalList: List<TicketResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_result)

        // 1. Tangkap data dari Intent
        val origin = intent.getStringExtra("EXTRA_ORIGIN") ?: ""
        val destination = intent.getStringExtra("EXTRA_DESTINATION") ?: ""

        // 🟢 Tangkap data tanggal yang dikirim dari TrainActivity
        val datePergi = intent.getStringExtra("EXTRA_DATE_PERGI") ?: "Tanggal tidak diketahui"
        val datePulang = intent.getStringExtra("EXTRA_DATE_PULANG")

        // 2. Hubungkan dengan komponen XML
        tvSummaryLocation = findViewById(R.id.tv_summary_location)
        tvSummaryDate = findViewById(R.id.tv_summary_date) // 🟢 Hubungkan ke XML
        tvSummaryDetails = findViewById(R.id.tv_summary_details)
        btnBackList = findViewById(R.id.btn_back_list)
        rvTrainList = findViewById(R.id.rv_train_list)
        chipHargaTerendah = findViewById(R.id.chip_harga_terendah)
        chipKeberangkatanPagi = findViewById(R.id.chip_keberangkatan_pagi)

        // 3. Set teks untuk Lokasi
        tvSummaryLocation.text = "$origin ➔ $destination"

        // 🟢 4. Set teks untuk Tanggal (Logika Pulang-Pergi)
        if (datePulang != null && datePulang.isNotEmpty() && !datePulang.contains("DD, 00")) {
            // Jika ada tanggal pulang, tampilkan "Pergi - Pulang"
            tvSummaryDate.text = "$datePergi - $datePulang"
        } else {
            // Jika hanya sekali jalan, tampilkan tanggal pergi saja
            tvSummaryDate.text = datePergi
        }

        tvSummaryDetails.text = "Mencari tiket..."

        btnBackList.setOnClickListener {
            finish()
        }

        rvTrainList.layoutManager = LinearLayoutManager(this)
        adapter = TrainTicketAdapter(emptyList()) { tiketTerpilih ->
            Toast.makeText(this, "Memesan tiket: ${tiketTerpilih.train_name} kelas ${tiketTerpilih.class_type}", Toast.LENGTH_SHORT).show()
        }
        rvTrainList.adapter = adapter

        // Setup Listener untuk aksi Filter klik
        setupFilterListeners()

        // Panggil API untuk mencari jadwal reguler
        cariJadwalTiket(origin, destination, datePergi)
    }

    // 🟢 1. Tambahkan parameter tanggalRaw di dalam kurung
    private fun cariJadwalTiket(asal: String, tujuan: String, tanggalRaw: String) {

        // Konversi format "Sel, 19 Mei 2026" menjadi "2026-05-19" agar dimengerti database
        val formatDatabase = konversiTanggalKeFormatDatabase(tanggalRaw)

        // 🟢 2. Masukkan formatDatabase sebagai parameter ketiga di searchTickets
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

    // 🟢 3. Tambahkan fungsi pembantu ini tepat di bawah fungsi cariJadwalTiket
    private fun konversiTanggalKeFormatDatabase(tanggalLokal: String): String {
        return try {
            // Membaca format dari Android (contoh: "Sel, 19 Mei 2026")
            val formatInput = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale("id", "ID"))
            // Mengubah ke format PostgreSQL (contoh: "2026-05-19")
            val formatOutput = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

            val date = formatInput.parse(tanggalLokal)
            formatOutput.format(date!!)
        } catch (e: Exception) {
            "" // Jika terjadi error parsing, kembalikan string kosong
        }
    }

    private fun setupFilterListeners() {
        val filterAction = {
            var filteredList = originalList

            // 1. Jika chip "Harga Terendah" aktif (Checked)
            if (chipHargaTerendah.isChecked) {
                filteredList = filteredList.sortedBy { it.price }
            }

            // 2. Jika chip "Berangkat Pagi" aktif (Jam keberangkatan di bawah 11:00)
            if (chipKeberangkatanPagi.isChecked) {
                filteredList = filteredList.filter { tiket ->
                    val jam = tiket.departure_time.split(":").firstOrNull()?.toIntOrNull() ?: 24
                    jam in 4..11
                }
            }

            // Update data ke RecyclerView adapter
            adapter.updateData(filteredList)
            tvSummaryDetails.text = "${filteredList.size} Tiket Kereta Tersedia"
        }

        chipHargaTerendah.setOnCheckedChangeListener { _, _ -> filterAction() }
        chipKeberangkatanPagi.setOnCheckedChangeListener { _, _ -> filterAction() }
    }
}