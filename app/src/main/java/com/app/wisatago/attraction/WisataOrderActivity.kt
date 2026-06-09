package com.app.wisatago.attraction

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.R
import com.google.android.material.card.MaterialCardView
import java.util.Calendar
import java.util.Locale

class WisataOrderActivity : AppCompatActivity() {

    private var qty = 1
    private var selectedDate = "" // Akan menyimpan format YYYY-MM-DD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wisata_order)

        val wisataId = intent.getStringExtra("EXTRA_WISATA_ID")
        val wisataName = intent.getStringExtra("EXTRA_WISATA_NAME") ?: "Nama Wisata"
        val wisataPrice = intent.getDoubleExtra("EXTRA_WISATA_PRICE", 0.0)

        findViewById<TextView>(R.id.tv_order_wisata_name).text = wisataName

        val tvDate = findViewById<TextView>(R.id.tv_order_date)
        val tvQty = findViewById<TextView>(R.id.tv_order_qty)

        // Date Picker
        findViewById<MaterialCardView>(R.id.btn_select_date).setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // 🟢 PERBAIKAN: Format wajib YYYY-MM-DD agar PostgreSQL tidak terbalik membacanya
                val formatBulan = String.format(Locale.getDefault(), "%02d", month + 1)
                val formatHari = String.format(Locale.getDefault(), "%02d", dayOfMonth)

                selectedDate = "$year-$formatBulan-$formatHari"

                // Tampilan di UI layar HP
                tvDate.text = "$formatHari/$formatBulan/$year"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        // Stepper Jumlah Tiket
        findViewById<ImageButton>(R.id.btn_qty_minus).setOnClickListener {
            if (qty > 1) {
                qty--
                tvQty.text = qty.toString()
            }
        }
        findViewById<ImageButton>(R.id.btn_qty_plus).setOnClickListener {
            qty++
            tvQty.text = qty.toString()
        }

        findViewById<ImageButton>(R.id.btn_back_order).setOnClickListener {
            finish()
        }

        // Navigasi ke Checkout
        findViewById<Button>(R.id.btn_lanjut_checkout).setOnClickListener {
            if (selectedDate.isEmpty()) return@setOnClickListener // Validasi hanya untuk tanggal

            val intent = Intent(this, CheckoutWisataActivity::class.java).apply {
                putExtra("EXTRA_WISATA_ID", wisataId)
                putExtra("EXTRA_WISATA_NAME", wisataName)
                putExtra("EXTRA_WISATA_PRICE", wisataPrice)
                putExtra("EXTRA_VISIT_DATE", selectedDate) // Data yang dikirim adalah YYYY-MM-DD
                putExtra("EXTRA_TICKET_QTY", qty)
            }
            startActivity(intent)
        }
    }
}