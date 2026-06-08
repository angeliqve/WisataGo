package com.app.wisatago.attraction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.R
import com.google.android.material.card.MaterialCardView
import java.util.Calendar

class WisataOrderActivity : AppCompatActivity() {

    private var qty = 1
    private var selectedDate = ""
    private var selectedTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wisata_order)

        val wisataId = intent.getStringExtra("EXTRA_WISATA_ID")
        val wisataName = intent.getStringExtra("EXTRA_WISATA_NAME") ?: "Nama Wisata"
        val wisataPrice = intent.getDoubleExtra("EXTRA_WISATA_PRICE", 0.0)

        findViewById<TextView>(R.id.tv_order_wisata_name).text = wisataName

        val tvDate = findViewById<TextView>(R.id.tv_order_date)
        val tvTime = findViewById<TextView>(R.id.tv_order_time)
        val tvQty = findViewById<TextView>(R.id.tv_order_qty)

        // Date Picker
        findViewById<MaterialCardView>(R.id.btn_select_date).setOnClickListener {
            val calendar = Calendar.getInstance()

            // 1. Simpan DatePickerDialog ke dalam sebuah variabel terlebih dahulu
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                selectedDate = "$dayOfMonth/${month + 1}/$year"
                tvDate.text = selectedDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            // 2. Kunci tanggal minimum ke hari ini (waktu sekarang)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()

            // 3. Tampilkan kalender
            datePickerDialog.show()
        }

        // Time Picker
        findViewById<MaterialCardView>(R.id.btn_select_time).setOnClickListener {
            val pilihanJam = arrayOf("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00")

            android.app.AlertDialog.Builder(this)
                .setTitle("Pilih Jam Kunjungan")
                .setItems(pilihanJam) { _, which ->
                    // Mengambil item yang diklik berdasarkan urutannya (which)
                    selectedTime = pilihanJam[which]
                    tvTime.text = selectedTime
                }
                .show()
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
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) return@setOnClickListener // Validasi sederhana

            val intent = Intent(this, CheckoutWisataActivity::class.java).apply {
                putExtra("EXTRA_WISATA_ID", wisataId)
                putExtra("EXTRA_WISATA_NAME", wisataName)
                putExtra("EXTRA_WISATA_PRICE", wisataPrice)
                putExtra("EXTRA_VISIT_DATE", selectedDate)
                putExtra("EXTRA_VISIT_TIME", selectedTime)
                putExtra("EXTRA_TICKET_QTY", qty)
            }
            startActivity(intent)
        }
    }
}