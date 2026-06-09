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
import com.app.wisatago.R
import com.app.wisatago.Dashboard
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FlightActivity : AppCompatActivity() {

    // 🟢 VARIABEL RAHASIA UNTUK API NODE.JS (Format: YYYY-MM-DD)
    private var apiDatePergi = ""
    private var apiDatePulang = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight)

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
        val tabBus = findViewById<TextView>(R.id.tabBus)

        val calendarPergi = Calendar.getInstance()
        val calendarPulang = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))

        // 🛫 DATA DUMMY BANDARA KITA (Catatan: Jika nanti mau pakai database, ini bisa diganti)
        val daftarBandara = arrayOf("Bandara Soekarno-Hatta (CGK)", "Bandara Internasional Ngurah Rai (DPS)")

        btnBack.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            // 🟢 FLAG ini berfungsi untuk menghapus history halaman lain (seperti halaman pemilihan transport)
            // Jadi saat user menekan tombol "Back" di HP Android dari Dashboard, dia tidak akan kembali ke Pesawat lagi
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        containerOrigin.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Pilih Bandara Asal")
                .setItems(daftarBandara) { _, which ->
                    tvOrigin.text = daftarBandara[which]
                }
                .show()
        }

        containerDestination.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Pilih Bandara Tujuan")
                .setItems(daftarBandara) { _, which ->
                    tvDestination.text = daftarBandara[which]
                }
                .show()
        }

        btnSwapLocation.setOnClickListener {
            val temp = tvOrigin.text.toString()
            tvOrigin.text = tvDestination.text.toString()
            tvDestination.text = temp
        }

        // ==========================================
        // 🟢 KALENDER KEBERANGKATAN (DIKUNCI HARI INI)
        // ==========================================
        btnTanggalPergi.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendarPergi.set(Calendar.YEAR, year)
                    calendarPergi.set(Calendar.MONTH, month)
                    calendarPergi.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // 1. Teks Cantik untuk UI Layar
                    tvTanggalPergi.text = dateFormat.format(calendarPergi.time)

                    // 2. Teks Baku untuk dikirim ke API Server (YYYY-MM-DD)
                    val formatBulan = String.format("%02d", month + 1)
                    val formatHari = String.format("%02d", dayOfMonth)
                    apiDatePergi = "$year-$formatBulan-$formatHari"
                },
                calendarPergi.get(Calendar.YEAR),
                calendarPergi.get(Calendar.MONTH),
                calendarPergi.get(Calendar.DAY_OF_MONTH)
            )
            // 🔒 Kunci agar tidak bisa pesan hari kemarin
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        switchPulangPergi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                btnTanggalPulang.visibility = View.VISIBLE
            } else {
                btnTanggalPulang.visibility = View.GONE
                tvTanggalPulang.text = "DD, 00 0000 0000"
                apiDatePulang = ""
            }
        }

        // ==========================================
        // 🟢 KALENDER KEPULANGAN (DIKUNCI)
        // ==========================================
        btnTanggalPulang.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendarPulang.set(Calendar.YEAR, year)
                    calendarPulang.set(Calendar.MONTH, month)
                    calendarPulang.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    tvTanggalPulang.text = dateFormat.format(calendarPulang.time)

                    val formatBulan = String.format("%02d", month + 1)
                    val formatHari = String.format("%02d", dayOfMonth)
                    apiDatePulang = "$year-$formatBulan-$formatHari"
                },
                calendarPulang.get(Calendar.YEAR),
                calendarPulang.get(Calendar.MONTH),
                calendarPulang.get(Calendar.DAY_OF_MONTH)
            )
            // 🔒 Set minimal tanggal pulang = tanggal pergi (atau hari ini)
            datePickerDialog.datePicker.minDate = calendarPergi.timeInMillis
            datePickerDialog.show()
        }

        btnSelectPassengers.setOnClickListener {
            val opsiPenumpang = arrayOf("1 Dewasa", "2 Dewasa", "3 Dewasa", "4 Dewasa")
            AlertDialog.Builder(this)
                .setTitle("Pilih Jumlah Penumpang")
                .setItems(opsiPenumpang) { _, which ->
                    tvPassengers.text = opsiPenumpang[which]
                }
                .show()
        }

        // Navigasi Tab
        tabKereta.setOnClickListener {
            val intent = Intent(this, TrainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        tabBus.setOnClickListener {
            val intent = Intent(this, BusActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        // 🛫 AKSI CARI TIKET PESAWAT
        btnCariTiket.setOnClickListener {
            val asal = tvOrigin.text.toString().trim()
            val tujuan = tvDestination.text.toString().trim()
            val teksPenumpang = tvPassengers.text.toString().trim()

            if (asal.contains("Pilih") || tujuan.contains("Pilih") || asal.isEmpty() || tujuan.isEmpty()) {
                Toast.makeText(this, "Harap pilih bandara asal dan tujuan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (asal == tujuan) {
                Toast.makeText(this, "Bandara asal dan tujuan tidak boleh sama!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (apiDatePergi.isEmpty()) {
                Toast.makeText(this, "Harap pilih tanggal keberangkatan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (switchPulangPergi.isChecked) {
                if (apiDatePulang.isEmpty()) {
                    Toast.makeText(this, "Harap pilih tanggal kepulangan!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val jumlahPenumpang = teksPenumpang.split(" ")[0].toIntOrNull() ?: 1

            val intent = Intent(this, TicketResultActivity::class.java)

            // 🟢 MENGIRIMKAN IDENTITAS "flight" KE TicketResultActivity
            intent.putExtra("EXTRA_TRANSPORT_TYPE", "flight")
            intent.putExtra("EXTRA_ORIGIN", asal)
            intent.putExtra("EXTRA_DESTINATION", tujuan)

            // 🟢 KITA MENGIRIMKAN FORMAT YYYY-MM-DD UNTUK SERVER
            intent.putExtra("EXTRA_DATE_PERGI", apiDatePergi)
            // (Opsional) Mengirimkan tanggal format cantik untuk sekadar ditampilkan di judul atas TicketResult
            intent.putExtra("EXTRA_DISPLAY_DATE_PERGI", tvTanggalPergi.text.toString())

            if (switchPulangPergi.isChecked) {
                intent.putExtra("EXTRA_DATE_PULANG", apiDatePulang)
                intent.putExtra("EXTRA_DISPLAY_DATE_PULANG", tvTanggalPulang.text.toString())
            }

            intent.putExtra("EXTRA_PASSENGERS", jumlahPenumpang)
            startActivity(intent)
        }
    }
}