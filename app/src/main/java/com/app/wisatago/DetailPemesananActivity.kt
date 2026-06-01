package com.app.wisatago

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

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

        val bookingCode = intent.getStringExtra("BOOKING_CODE") ?: ""
        val productName = intent.getStringExtra("TRANSPORT_NAME") ?: "Produk WisataGO"
        val status = intent.getStringExtra("STATUS") ?: "PENDING"
        val totalAmountRaw = intent.getStringExtra("TOTAL_AMOUNT") ?: "0"
        val origin = intent.getStringExtra("ORIGIN_CITY") ?: "Jakarta"
        val destination = intent.getStringExtra("DEST_CITY") ?: ""

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

        if (status.uppercase() == "SUCCESS") {
            tvDetailStatus.text = "SUCCESS"
            tvDetailStatus.setBackgroundColor(Color.parseColor("#DCFCE7"))
            tvDetailStatus.setTextColor(Color.parseColor("#15803D"))
        } else {
            tvDetailStatus.text = "ONGOING"
            tvDetailStatus.setBackgroundColor(Color.parseColor("#FEF3C7"))
            tvDetailStatus.setTextColor(Color.parseColor("#D97706"))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}