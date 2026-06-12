package com.app.wisatago.transport

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.Dashboard
import com.app.wisatago.R
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus)

        val btnTanggalPergi = findViewById<View>(R.id.btn_tanggal_pergi)
        val btnTanggalPulang = findViewById<View>(R.id.btn_tanggal_pulang)
        val tvTanggalPergi = findViewById<TextView>(R.id.tv_tanggal_pergi)
        val tvTanggalPulang = findViewById<TextView>(R.id.tv_tanggal_pulang)
        val switchPulangPergi = findViewById<SwitchMaterial>(R.id.switch_pulang_pergi)

        val btnSelectPassengers = findViewById<LinearLayout>(R.id.btnSelectPassengers)
        val tvPassengers = findViewById<TextView>(R.id.tvPassengers)

        val tvOrigin = findViewById<TextView>(R.id.tvOrigin)
        val tvDestination = findViewById<TextView>(R.id.tvDestination)

        val containerOrigin = findViewById<LinearLayout>(R.id.containerOrigin)
        val containerDestination = findViewById<LinearLayout>(R.id.containerDestination)

        val btnCariTiket = findViewById<LinearLayout>(R.id.btnCariTiket)
        val btnSwapLocation = findViewById<View>(R.id.btnSwapLocation)
        val btnBack = findViewById<View>(R.id.btnBack)

        val tabKereta = findViewById<TextView>(R.id.tabKereta)
        val tabPesawat = findViewById<TextView>(R.id.tabPesawat)
        val tabBus = findViewById<TextView>(R.id.tabBus)

        val calendarPergi = Calendar.getInstance()
        val calendarPulang = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))

        // =================================================================
        // 🟢 ISI OTOMATIS TANGGAL PERGI DENGAN HARI INI
        // =================================================================
        val tanggalHariIni = dateFormat.format(calendarPergi.time)
        tvTanggalPergi.text = tanggalHariIni
        // =================================================================

        val daftarKotaBus = arrayOf(
            "Jakarta - Terminal Pulo Gebang",
            "Surabaya - Terminal Purabaya (Bungurasih)",
            "Yogyakarta - Terminal Giwangan",
            "Cirebon - Terminal Harjamukti",
            "Denpasar - Terminal Mengwi"
        )

        // =================================================================
        // 🟢 PERBAIKAN: AKSI TOMBOL BACK LANGSUNG KE DASHBOARD
        // =================================================================
        btnBack.setOnClickListener {
            kembaliKeDashboard()
        }

        tabKereta.setOnClickListener {
            startActivity(Intent(this, TrainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
        tabPesawat.setOnClickListener {
            val intent = Intent(this, FlightActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        containerOrigin.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Pilih Kota Asal")
                .setItems(daftarKotaBus) { _, which ->
                    tvOrigin.text = daftarKotaBus[which]
                }
                .show()
        }

        containerDestination.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Pilih Kota Tujuan")
                .setItems(daftarKotaBus) { _, which ->
                    tvDestination.text = daftarKotaBus[which]
                }
                .show()
        }

        btnSwapLocation.setOnClickListener {
            val temp = tvOrigin.text.toString()
            tvOrigin.text = tvDestination.text.toString()
            tvDestination.text = temp
        }

        // =================================================================
        // KALENDER PERGI (Min: Hari Ini, Max: H+45)
        // =================================================================
        btnTanggalPergi.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendarPergi.set(Calendar.YEAR, year)
                    calendarPergi.set(Calendar.MONTH, month)
                    calendarPergi.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    tvTanggalPergi.text = dateFormat.format(calendarPergi.time)

                    // Reset tanggal pulang jika mendahului tanggal pergi yang baru
                    if (switchPulangPergi.isChecked && calendarPulang.timeInMillis < calendarPergi.timeInMillis) {
                        tvTanggalPulang.text = "DD, 00 0000 0000"
                    }
                },
                calendarPergi.get(Calendar.YEAR),
                calendarPergi.get(Calendar.MONTH),
                calendarPergi.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

            val maxCalendar = Calendar.getInstance()
            maxCalendar.add(Calendar.DAY_OF_YEAR, 45)
            datePickerDialog.datePicker.maxDate = maxCalendar.timeInMillis

            datePickerDialog.show()
        }

        // =================================================================
        // LOGIKA SWITCH PULANG PERGI
        // =================================================================
        switchPulangPergi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Tampilkan kotak tanggal pulang
                btnTanggalPulang.visibility = View.VISIBLE

                // Set default tanggal pulang (Tanggal Pergi + 1 Hari) agar tidak kosong
                // Menggunakan waktu dari calendarPergi saat ini, lalu ditambah 1 hari
                calendarPulang.timeInMillis = calendarPergi.timeInMillis
                calendarPulang.add(Calendar.DAY_OF_MONTH, 1) // Tambah 1 hari

                tvTanggalPulang.text = dateFormat.format(calendarPulang.time)
            } else {
                // Sembunyikan dan reset ke placeholder
                btnTanggalPulang.visibility = View.GONE
                tvTanggalPulang.text = "DD, 00 0000 0000"
            }
        }

        // =================================================================
        // KALENDER PULANG (Min: Tanggal Pergi, Max: H+45)
        // =================================================================
        btnTanggalPulang.setOnClickListener {
            val isPergiSelected = !tvTanggalPergi.text.toString().contains("DD, 00")

            if (!isPergiSelected) {
                Toast.makeText(this, "Silakan pilih Tanggal Pergi terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendarPulang.set(Calendar.YEAR, year)
                    calendarPulang.set(Calendar.MONTH, month)
                    calendarPulang.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    tvTanggalPulang.text = dateFormat.format(calendarPulang.time)
                },
                calendarPergi.get(Calendar.YEAR),
                calendarPergi.get(Calendar.MONTH),
                calendarPergi.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.minDate = calendarPergi.timeInMillis

            val maxCalendar = Calendar.getInstance()
            maxCalendar.add(Calendar.DAY_OF_YEAR, 45)
            datePickerDialog.datePicker.maxDate = maxCalendar.timeInMillis

            datePickerDialog.show()
        }

        btnSelectPassengers.setOnClickListener {
            val opsiPenumpang = arrayOf("1 Dewasa", "2 Dewasa", "3 Dewasa", "4 Dewasa", "5 Dewasa")
            AlertDialog.Builder(this)
                .setTitle("Pilih Jumlah Penumpang")
                .setItems(opsiPenumpang) { _, which ->
                    tvPassengers.text = opsiPenumpang[which]
                }
                .show()
        }

        btnCariTiket.setOnClickListener {
            val asal = tvOrigin.text.toString().trim()
            val tujuan = tvDestination.text.toString().trim()
            val tanggalPergi = tvTanggalPergi.text.toString().trim()
            val tanggalPulang = tvTanggalPulang.text.toString().trim()
            val teksPenumpang = tvPassengers.text.toString().trim()

            if (asal.contains("Pilih") || tujuan.contains("Pilih") || asal.isEmpty() || tujuan.isEmpty()) {
                Toast.makeText(this, "Harap pilih kota asal dan tujuan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (asal == tujuan) {
                Toast.makeText(this, "Kota asal dan tujuan tidak boleh sama!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tanggalPergi.isEmpty() || tanggalPergi.contains("Pilih") || tanggalPergi.contains("DD, 00")) {
                Toast.makeText(this, "Harap pilih tanggal keberangkatan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (switchPulangPergi.isChecked) {
                if (tanggalPulang.isEmpty() || tanggalPulang.contains("Pilih") || tanggalPulang.contains("DD, 00")) {
                    Toast.makeText(this, "Harap pilih tanggal kepulangan!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val jumlahPenumpang = teksPenumpang.split(" ")[0].toIntOrNull() ?: 1

            val asalKota = asal.split(" - ")[0].trim()
            val tujuanKota = tujuan.split(" - ")[0].trim()

            val intent = Intent(this, TicketResultActivity::class.java)
            intent.putExtra("EXTRA_TRANSPORT_TYPE", "bus")
            intent.putExtra("EXTRA_ORIGIN", asalKota)
            intent.putExtra("EXTRA_DESTINATION", tujuanKota)
            intent.putExtra("EXTRA_DATE_PERGI", tanggalPergi)

            if (switchPulangPergi.isChecked) {
                intent.putExtra("EXTRA_DATE_PULANG", tanggalPulang)
            }

            intent.putExtra("EXTRA_PASSENGERS", jumlahPenumpang)
            startActivity(intent)
        }
    }

    // =================================================================
    // 🟢 HANDLING TOMBOL BACK SISTEM HP (NAVBAR BAWAH ANDROID)
    // =================================================================
    override fun onBackPressed() {
        super.onBackPressed()
        kembaliKeDashboard()
    }

    // Fungsi pembantu agar kode tidak berulang
    private fun kembaliKeDashboard() {
        val intent = Intent(this, Dashboard::class.java)
        // CLEAR_TOP akan menghancurkan TrainActivity yang menggantung di memori
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}