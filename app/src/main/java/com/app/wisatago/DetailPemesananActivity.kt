package com.app.wisatago

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale
import com.app.wisatago.ApiClient
import com.app.wisatago.CancelRequest
import com.app.wisatago.CancelResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailPemesananActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pemesanan)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvDetailName = findViewById<TextView>(R.id.tvDetailName)
        val tvDetailStatus = findViewById<TextView>(R.id.tvDetailStatus)
        val tvDetailCode = findViewById<TextView>(R.id.tvDetailCode)
        val tvDetailRoute = findViewById<TextView>(R.id.tvDetailRoute)
        val tvDetailTotal = findViewById<TextView>(R.id.tvDetailTotal)
        val labelRute = findViewById<TextView>(R.id.labelRute)

        // Inisialisasi Tombol Material
        val btnCancel = findViewById<MaterialButton>(R.id.btnCancelBooking)

        // Tarik data dari Intent
        val bookingCode = intent.getStringExtra("BOOKING_CODE") ?: ""
        val productName = intent.getStringExtra("TRANSPORT_NAME") ?: "Produk WisataGO"
        val status = intent.getStringExtra("STATUS") ?: "PENDING"
        val totalAmountRaw = intent.getStringExtra("TOTAL_AMOUNT") ?: "0"
        val origin = intent.getStringExtra("ORIGIN_CITY") ?: "Jakarta"
        val destination = intent.getStringExtra("DEST_CITY") ?: ""
        val departureTimeStr = intent.getStringExtra("DEPARTURE_TIME")

        val tvDetailAddon = findViewById<TextView>(R.id.tvDetailAddon)
        val addonWisata = intent.getStringExtra("ADDON_WISATA")
        val passengerInfo = intent.getStringExtra("PASSENGER_INFO")

        // 🟢 DATA BARU: Nama Pemesan Khusus Tampilan Admin
        val fullName = intent.getStringExtra("FULL_NAME")

        // Hubungkan dengan UI XML
        val tvDetailTime = findViewById<TextView>(R.id.tvDetailTime)
        val tvDetailPassengers = findViewById<TextView>(R.id.tvDetailPassengers)
        val labelWaktu = findViewById<TextView>(R.id.labelWaktu)
        val labelPenumpang = findViewById<TextView>(R.id.labelPenumpang)

        val labelNamaPemesan = findViewById<TextView>(R.id.labelNamaPemesan)
        val tvDetailNamaPemesan = findViewById<TextView>(R.id.tvDetailNamaPemesan)

        // =====================================================================
        // 🟢 1. LOGIKA TAMPILAN NAMA PEMESAN (KHUSUS DASHBOARD ADMIN)
        // =====================================================================
        if (!fullName.isNullOrEmpty()) {
            labelNamaPemesan.visibility = View.VISIBLE
            tvDetailNamaPemesan.visibility = View.VISIBLE
            tvDetailNamaPemesan.text = fullName
        } else {
            labelNamaPemesan.visibility = View.GONE
            tvDetailNamaPemesan.visibility = View.GONE // Menyembunyikan jika user biasa
        }

        // =====================================================================
        // Logika Format Tanggal dan Jam Cerdas (Pemisahan Wisata & Transport)
        // =====================================================================
        if (!departureTimeStr.isNullOrEmpty()) {
            try {
                if (bookingCode.startsWith("WS-")) {
                    // TIKET WISATA: Hanya ambil YYYY-MM-DD dan hilangkan jamnya
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

                    val dateOnly = departureTimeStr.take(10)
                    val date = inputFormat.parse(dateOnly)

                    tvDetailTime.text = if (date != null) outputFormat.format(date) else dateOnly
                } else {
                    // TIKET TRANSPORTASI: Tetap ambil jam dan menitnya
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale.getDefault())
                    val date = inputFormat.parse(departureTimeStr)
                    tvDetailTime.text = if (date != null) outputFormat.format(date) else departureTimeStr
                }
            } catch (e: Exception) {
                tvDetailTime.text = departureTimeStr
            }
        } else {
            tvDetailTime.text = "Waktu tidak tersedia"
        }

        // =====================================================================
        // Logika Tampilan Penumpang & Pengunjung
        // =====================================================================
        if (bookingCode.startsWith("WS-")) {
            labelPenumpang.text = "Jumlah Tiket"
            tvDetailPassengers.text = if (!passengerInfo.isNullOrEmpty()) passengerInfo else "1 Tiket"
        } else {
            labelPenumpang.text = "Rincian Penumpang"
            if (!passengerInfo.isNullOrEmpty()) {
                tvDetailPassengers.text = passengerInfo
            } else {
                tvDetailPassengers.text = "Data penumpang tidak tersedia"
            }
        }

        // Ubah Judul Label Waktu Jika Ini Tiket Wisata
        if (bookingCode.startsWith("WS-")) {
            labelWaktu.text = "Tanggal Kunjungan"
        } else {
            labelWaktu.text = "Waktu Keberangkatan"
        }

        tvDetailName.text = productName
        tvDetailCode.text = bookingCode

        // Logika Tampilan UI Add-on
        if (!addonWisata.isNullOrEmpty() && bookingCode.startsWith("TR-")) {
            tvDetailAddon.visibility = View.VISIBLE
            tvDetailAddon.text = "+ Add-on: $addonWisata"
        } else {
            tvDetailAddon.visibility = View.GONE
        }

        // =====================================================================
        // 🟢 2. PERBAIKAN LABEL DESTINASI WISATA (BUKAN WISATAGO)
        // =====================================================================
        if (bookingCode.startsWith("TR-") && destination.isNotEmpty()) {
            labelRute.text = "Rute Perjalanan"
            if (productName.contains("&")) {
                tvDetailRoute.text = "$origin ⇌ $destination"
            } else {
                tvDetailRoute.text = "$origin ➔ $destination"
            }
        } else {
            labelRute.text = "Destinasi Wisata"
            tvDetailRoute.text = origin // Otomatis berisi nama tempat wisata hasil query SQL Admin
        }

        try {
            val amountLong = totalAmountRaw.toDoubleOrNull()?.toLong() ?: 0L
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            tvDetailTotal.text = formatRupiah.format(amountLong).replace(",00", "")
        } catch (e: Exception) {
            tvDetailTotal.text = "Rp $totalAmountRaw"
        }

        val statusClean = status.uppercase()
        when (statusClean) {
            "SUCCESS" -> {
                tvDetailStatus.text = "SUCCESS"
                tvDetailStatus.setBackgroundColor(Color.parseColor("#DCFCE7"))
                tvDetailStatus.setTextColor(Color.parseColor("#15803D"))
            }
            "CANCELED" -> {
                tvDetailStatus.text = "CANCELED"
                tvDetailStatus.setBackgroundColor(Color.parseColor("#FEE2E2"))
                tvDetailStatus.setTextColor(Color.parseColor("#B91C1C"))
            }
            else -> {
                tvDetailStatus.text = statusClean
                tvDetailStatus.setBackgroundColor(Color.parseColor("#FEF3C7"))
                tvDetailStatus.setTextColor(Color.parseColor("#D97706"))
            }
        }

        // =====================================================================
        // 🟢 3. LOGIKA TOMBOL BATAL CERDAS (DIPOTONG/DIHAPUS UNTUK ADMIN)
        // =====================================================================
        if (!fullName.isNullOrEmpty()) {
            // JIKA DIAKSES OLEH ADMIN: Sembunyikan total tombol pembatalan
            btnCancel.visibility = View.GONE
        } else if (statusClean != "SUCCESS") {
            btnCancel.isEnabled = false
            btnCancel.alpha = 0.5f
            btnCancel.text = "Pesanan Tidak Dapat Dibatalkan"
        } else if (!departureTimeStr.isNullOrEmpty()) {
            try {
                if (bookingCode.startsWith("WS-")) {
                    val dateOnly = departureTimeStr.take(10)
                    val formatTanggal = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val visitDateObj = formatTanggal.parse(dateOnly)

                    val currentDateStr = formatTanggal.format(java.util.Date())
                    val currentDateObj = formatTanggal.parse(currentDateStr)

                    if (visitDateObj != null && currentDateObj != null) {
                        if (currentDateObj.time >= visitDateObj.time) {
                            btnCancel.isEnabled = false
                            btnCancel.alpha = 0.5f
                            btnCancel.text = "Batas Waktu Batal Habis"
                        } else {
                            btnCancel.isEnabled = true
                            btnCancel.alpha = 1.0f
                        }
                    }
                } else {
                    val formatFull = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val departureDate = formatFull.parse(departureTimeStr)
                    val currentDate = java.util.Date()

                    if (departureDate != null) {
                        val diffMillis = departureDate.time - currentDate.time
                        val diffHours = diffMillis / (1000 * 60 * 60)

                        if (diffHours < 24) {
                            btnCancel.isEnabled = false
                            btnCancel.alpha = 0.5f
                            btnCancel.text = "Batas Waktu Batal Habis (H-1)"
                        } else {
                            btnCancel.isEnabled = true
                            btnCancel.alpha = 1.0f
                        }
                    }
                }

                // Aksi tombol tekan batal (Khusus User Biasa)
                btnCancel.setOnClickListener {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Batalkan Pesanan?")

                    val warningText = if (bookingCode.startsWith("WS-")) {
                        "Apakah Anda yakin ingin membatalkan pesanan $bookingCode?\n\nPesanan wisata hanya dapat dibatalkan maksimal H-1 sebelum hari kunjungan."
                    } else {
                        "Apakah Anda yakin ingin membatalkan pesanan $bookingCode?\n\nPesanan transportasi hanya dapat dibatalkan maksimal 24 jam sebelum keberangkatan."
                    }
                    builder.setMessage(warningText)
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    builder.setPositiveButton("Ya, Batalkan") { dialog, _ ->
                        val request = CancelRequest(bookingCode)
                        Toast.makeText(this, "Memproses pembatalan...", Toast.LENGTH_SHORT).show()

                        ApiClient.instance.cancelBooking(request).enqueue(object : Callback<CancelResponse> {
                            override fun onResponse(call: Call<CancelResponse>, response: Response<CancelResponse>) {
                                if (response.isSuccessful && response.body()?.message != null) {
                                    Toast.makeText(this@DetailPemesananActivity, response.body()?.message, Toast.LENGTH_LONG).show()

                                    btnCancel.isEnabled = false
                                    btnCancel.alpha = 0.5f
                                    btnCancel.text = "Pesanan Dibatalkan"

                                    tvDetailStatus.text = "CANCELED"
                                    tvDetailStatus.setBackgroundColor(Color.parseColor("#FEE2E2"))
                                    tvDetailStatus.setTextColor(Color.parseColor("#B91C1C"))
                                } else {
                                    Toast.makeText(this@DetailPemesananActivity, "Gagal membatalkan pesanan.", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<CancelResponse>, t: Throwable) {
                                Toast.makeText(this@DetailPemesananActivity, "Koneksi Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("Tutup") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // 🟢 PERBAIKAN: Jika data waktu error/kosong, langsung hapus/sembunyikan tombolnya
            btnCancel.visibility = View.GONE
        }

        btnBack.setOnClickListener { finish() }
    }
}