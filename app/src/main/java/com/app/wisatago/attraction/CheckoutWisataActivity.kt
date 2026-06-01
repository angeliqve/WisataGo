package com.app.wisatago.attraction

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.R
import com.app.wisatago.booking.PaymentActivity
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class CheckoutWisataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_wisata)

        val wisataId = intent.getStringExtra("EXTRA_WISATA_ID") ?: ""
        val wisataName = intent.getStringExtra("EXTRA_WISATA_NAME") ?: "Wisata"
        val ticketPrice = intent.getDoubleExtra("EXTRA_WISATA_PRICE", 0.0)
        val visitDate = intent.getStringExtra("EXTRA_VISIT_DATE") ?: "-"
        val visitTime = intent.getStringExtra("EXTRA_VISIT_TIME") ?: "-"
        val ticketQty = intent.getIntExtra("EXTRA_TICKET_QTY", 1)

        // Set Informasi Tiket
        findViewById<TextView>(R.id.tv_co_wisata_name).text = wisataName
        findViewById<TextView>(R.id.tv_co_wisata_datetime).text = "$visitDate • $visitTime WIB"
        findViewById<TextView>(R.id.tv_co_wisata_qty).text = "$ticketQty Tiket"

        // Kalkulasi Biaya sesuai database (subtotal & tax)
        val subTotal = ticketPrice * ticketQty
        val tax = subTotal * 0.12 // Asumsi pajak 12%
        val totalAll = subTotal + tax

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        findViewById<TextView>(R.id.tv_co_base_price).text = "Rp ${formatter.format(subTotal)}"
        findViewById<TextView>(R.id.tv_co_tax).text = "Rp ${formatter.format(tax)}"
        findViewById<TextView>(R.id.tv_co_total_all).text = "Rp ${formatter.format(totalAll)}"

        findViewById<ImageButton>(R.id.btn_back_checkout).setOnClickListener { finish() }

        // Tombol Bayar
        findViewById<MaterialButton>(R.id.btn_co_pay).setOnClickListener {
            val paymentIntent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("EXTRA_TRANSACTION_TYPE", "WISATA")
                putExtra("EXTRA_WISATA_ID", wisataId)
                putExtra("EXTRA_WISATA_NAME", wisataName)
                putExtra("EXTRA_TICKET_QTY", ticketQty)
                putExtra("EXTRA_SUBTOTAL", subTotal)
                putExtra("EXTRA_TAX", tax)
                putExtra("EXTRA_GRAND_TOTAL", totalAll)
            }
            startActivity(paymentIntent)
        }
    }
}