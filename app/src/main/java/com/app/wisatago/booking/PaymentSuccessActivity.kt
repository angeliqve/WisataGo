package com.app.wisatago.booking

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.Dashboard
import com.app.wisatago.R
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        // =========================================================================
        // 1. TANGKAP DATA ASLI DARI INTENT
        // =========================================================================
        val bookingCode = intent.getStringExtra("EXTRA_BOOKING_CODE") ?: "TR-000000"
        val pergiName = intent.getStringExtra("EXTRA_PERGI_NAME") ?: "Tiket Transportasi"
        val pulangName = intent.getStringExtra("EXTRA_PULANG_NAME") ?: ""
        val origin = intent.getStringExtra("EXTRA_ORIGIN") ?: "Asal"
        val dest = intent.getStringExtra("EXTRA_DESTINATION") ?: "Tujuan"
        val isReturnTrip = intent.getBooleanExtra("EXTRA_IS_RETURN_TRIP", false)
        val grandTotal = intent.getDoubleExtra("EXTRA_GRAND_TOTAL", 0.0)

        // Ambil jumlah (Bisa berupa jumlah tiket wisata ATAU jumlah penumpang transportasi)
        val qty = intent.getIntExtra("EXTRA_TICKET_QTY", intent.getIntExtra("EXTRA_PASSENGERS", 1))

        // =========================================================================
        // 2. RANGKAI NAMA DAN DETAIL RUTE SECARA DINAMIS
        // =========================================================================
        // Jika data Asal/Tujuan masih "Asal" (default) atau kosong, berarti ini transaksi Wisata
        val detailPesanan = if (origin == "Asal" || origin.isEmpty()) {
            "$pergiName\n($qty Tiket)"
        } else {
            val transportName = if (isReturnTrip && pulangName.isNotEmpty()) "$pergiName & $pulangName" else pergiName
            val tripType = if (isReturnTrip) "Pulang Pergi" else "Sekali Jalan"
            "$transportName\n$origin ➔ $dest\n($tripType • $qty Penumpang)"
        }

        // 3. FORMAT HARGA RUPIAH
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        val formattedPrice = "Rp ${formatter.format(grandTotal)}"

        // 4. DAPATKAN TANGGAL & JAM SAAT INI
        val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale("id", "ID"))
        val currentDateAndTime = sdf.format(Date())

        // =========================================================================
        // 5. SET SEMUA TEKS KE LAYAR
        // =========================================================================
        findViewById<TextView>(R.id.tv_success_date_time).text = currentDateAndTime
        findViewById<TextView>(R.id.tv_booking_code).text = bookingCode
        findViewById<TextView>(R.id.tv_success_transport_name).text = detailPesanan
        findViewById<TextView>(R.id.tv_success_total_price).text = formattedPrice

        // 6. AKSI TOMBOL KEMBALI
        findViewById<MaterialButton>(R.id.btn_back_dashboard).setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}