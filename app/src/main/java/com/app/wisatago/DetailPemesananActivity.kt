package com.app.wisatago

import android.graphics.Color
import android.os.Bundle
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

        // 🟢 Inisialisasi Tombol Material
        val btnCancel = findViewById<MaterialButton>(R.id.btnCancelBooking)

        // Tarik data dari Intent
        val bookingCode = intent.getStringExtra("BOOKING_CODE") ?: ""
        val productName = intent.getStringExtra("TRANSPORT_NAME") ?: "Produk WisataGO"
        val status = intent.getStringExtra("STATUS") ?: "PENDING"
        val totalAmountRaw = intent.getStringExtra("TOTAL_AMOUNT") ?: "0"
        val origin = intent.getStringExtra("ORIGIN_CITY") ?: "Jakarta"
        val destination = intent.getStringExtra("DEST_CITY") ?: ""
        val departureTimeStr = intent.getStringExtra("DEPARTURE_TIME")

        tvDetailName.text = productName
        tvDetailCode.text = bookingCode

        if (bookingCode.startsWith("TR-") && destination.isNotEmpty()) {
            labelRute.text = "Rute Perjalanan"
            tvDetailRoute.text = "$origin ➔ $destination"
        } else {
            labelRute.text = "Lokasi Destinasi"
            tvDetailRoute.text = origin
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
                tvDetailStatus.setBackgroundColor(Color.parseColor("#FEE2E2")) // 🟢 Merah
                tvDetailStatus.setTextColor(Color.parseColor("#B91C1C")) // 🟢 Merah
            }
            else -> {
                tvDetailStatus.text = statusClean
                tvDetailStatus.setBackgroundColor(Color.parseColor("#FEF3C7"))
                tvDetailStatus.setTextColor(Color.parseColor("#D97706"))
            }
        }

        // ==========================================
        // 🟢 LOGIKA CERDAS TOMBOL BATAL (H-1)
        // ==========================================
        if (statusClean != "SUCCESS") {
            btnCancel.isEnabled = false
            btnCancel.alpha = 0.5f
            btnCancel.text = "Pesanan Tidak Dapat Dibatalkan"
        } else if (!departureTimeStr.isNullOrEmpty()) {
            try {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val departureDate = format.parse(departureTimeStr)
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

                        // Aksi saat tombol ditekan
                        btnCancel.setOnClickListener {
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Batalkan Pesanan?")
                            builder.setMessage("Apakah Anda yakin ingin membatalkan pesanan $bookingCode?\n\nPesanan transportasi hanya dapat dibatalkan maksimal H-1 sebelum keberangkatan.")
                            builder.setIcon(android.R.drawable.ic_dialog_alert)

                            builder.setPositiveButton("Ya, Batalkan") { dialog, _ ->
                                val request = CancelRequest(bookingCode)
                                Toast.makeText(this, "Memproses pembatalan...", Toast.LENGTH_SHORT).show()

                                // Tembak API Batal ke Node.js
                                ApiClient.instance.cancelBooking(request).enqueue(object : Callback<CancelResponse> {
                                    override fun onResponse(call: Call<CancelResponse>, response: Response<CancelResponse>) {
                                        if (response.isSuccessful && response.body()?.message != null) {
                                            Toast.makeText(this@DetailPemesananActivity, response.body()?.message, Toast.LENGTH_LONG).show()

                                            // Matikan tombol langsung di layar jika berhasil
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
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Jika data tanggal keberangkatan gagal ditarik
            btnCancel.isEnabled = false
            btnCancel.alpha = 0.5f
            btnCancel.text = "Waktu Tidak Terbaca dari Database" // 🟢 Teks diubah agar terlihat jika terjadi error data
        }

        btnBack.setOnClickListener { finish() }
    }
}