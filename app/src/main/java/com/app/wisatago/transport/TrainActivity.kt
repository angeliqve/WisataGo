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
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TrainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train)

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

        val daftarStasiun = arrayOf("Gambir (GMR)", "Bandung (BD)","Cirebon (CN)", "Surabaya Pasar Turi (SGU)", "Tugu (YK)", "Semarang Tawang (SMT)")

        btnBack.setOnClickListener { finish() }

        containerOrigin.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Pilih Stasiun Asal")
                .setItems(daftarStasiun) { _, which ->
                    tvOrigin.text = daftarStasiun[which]
                }
                .show()
        }

        containerDestination.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Pilih Stasiun Tujuan")
                .setItems(daftarStasiun) { _, which ->
                    tvDestination.text = daftarStasiun[which]
                }
                .show()
        }

        btnSwapLocation.setOnClickListener {
            val temp = tvOrigin.text.toString()
            tvOrigin.text = tvDestination.text.toString()
            tvDestination.text = temp
        }

        btnTanggalPergi.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendarPergi.set(Calendar.YEAR, year)
                    calendarPergi.set(Calendar.MONTH, month)
                    calendarPergi.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    tvTanggalPergi.text = dateFormat.format(calendarPergi.time)
                },
                calendarPergi.get(Calendar.YEAR),
                calendarPergi.get(Calendar.MONTH),
                calendarPergi.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        switchPulangPergi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                btnTanggalPulang.visibility = View.VISIBLE
            } else {
                btnTanggalPulang.visibility = View.GONE
                tvTanggalPulang.text = "DD, 00 0000 0000"
            }
        }

        btnTanggalPulang.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendarPulang.set(Calendar.YEAR, year)
                    calendarPulang.set(Calendar.MONTH, month)
                    calendarPulang.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    tvTanggalPulang.text = dateFormat.format(calendarPulang.time)
                },
                calendarPulang.get(Calendar.YEAR),
                calendarPulang.get(Calendar.MONTH),
                calendarPulang.get(Calendar.DAY_OF_MONTH)
            ).show()
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

        tabBus.setOnClickListener {
            val intent = Intent(this, BusActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnCariTiket.setOnClickListener {
            val asal = tvOrigin.text.toString().trim()
            val tujuan = tvDestination.text.toString().trim()
            val tanggalPergi = tvTanggalPergi.text.toString().trim()
            val tanggalPulang = tvTanggalPulang.text.toString().trim()
            val teksPenumpang = tvPassengers.text.toString().trim()

            if (asal.contains("Pilih") || tujuan.contains("Pilih") || asal.isEmpty() || tujuan.isEmpty()) {
                Toast.makeText(this, "Harap pilih lokasi asal dan tujuan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (asal == tujuan) {
                Toast.makeText(this, "Lokasi asal dan tujuan tidak boleh sama!", Toast.LENGTH_SHORT).show()
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

            val intent = Intent(this, TicketResultActivity::class.java)
            intent.putExtra("EXTRA_TRANSPORT_TYPE", "train")
            intent.putExtra("EXTRA_ORIGIN", asal)
            intent.putExtra("EXTRA_DESTINATION", tujuan)
            intent.putExtra("EXTRA_DATE_PERGI", tanggalPergi)

            if (switchPulangPergi.isChecked) {
                intent.putExtra("EXTRA_DATE_PULANG", tanggalPulang)
            }

            intent.putExtra("EXTRA_PASSENGERS", jumlahPenumpang)

            startActivity(intent)
        }
    }
}