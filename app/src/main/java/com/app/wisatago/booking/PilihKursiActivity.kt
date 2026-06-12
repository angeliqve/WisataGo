package com.app.wisatago.booking

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout

class PilihKursiActivity : AppCompatActivity() {

    private lateinit var btnSimpanKursi: MaterialButton
    private lateinit var btnBack: TextView
    private lateinit var rvSeats: RecyclerView
    private lateinit var tabLayoutGerbong: TabLayout
    private lateinit var llHeaderKursi: LinearLayout

    private val gerbongData = mutableMapOf<Int, MutableList<Seat>>()
    private val currentDisplayList = mutableListOf<Seat>()
    private lateinit var adapter: SeatAdapter
    private var maxSeatsAllowed = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_kursi)

        maxSeatsAllowed = intent.getIntExtra("EXTRA_MAX_SEATS", 1)
        val transportType = intent.getStringExtra("EXTRA_TRANSPORT_TYPE") ?: "Kereta"
        val transportClass = intent.getStringExtra("EXTRA_TRAIN_CLASS") ?: "Eksekutif"

        rvSeats = findViewById(R.id.rvSeats)
        tabLayoutGerbong = findViewById(R.id.tabLayoutGerbong)
        btnSimpanKursi = findViewById(R.id.btnSimpanKursi)
        btnBack = findViewById(R.id.btnBack)
        llHeaderKursi = findViewById(R.id.llHeaderKursi)

        btnBack.setOnClickListener { finish() }

        // =======================================================
        // 🟢 1. LOGIKA PENENTUAN JENIS KENDARAAN & SUSUNAN
        // =======================================================
        var spanCount = 6 // Default 2-2 (Nomor, A, B, Lorong, C, D)
        var layoutType = "2-2"
        var tabCount = 8
        var prefixTab = "EKS"

        if (transportType.contains("Bus", ignoreCase = true)) {
            tabCount = 1 // Bus biasanya hanya 1 lantai/gerbong
            prefixTab = "BUS"
            findViewById<TextView>(R.id.btnBack).text = "❮ Batal"

            // Ubah Judul Header Atas
            (btnBack.parent as? android.widget.RelativeLayout)?.getChildAt(1)?.let {
                (it as TextView).text = "Pilih Kursi Bus"
            }

            if (transportClass.contains("Ekonomi", ignoreCase = true)) {
                spanCount = 7 // 3-2 (Nomor, A, B, C, Lorong, D, E)
                layoutType = "3-2"
            } else {
                spanCount = 6 // 2-2 Premium
                layoutType = "2-2"
            }
        } else {
            // Logika Kereta (Sudah disempurnakan)
            prefixTab = when {
                transportClass.contains("Ekonomi", ignoreCase = true) -> "EKO"
                transportClass.contains("Luxury", ignoreCase = true) -> "LUX"
                transportClass.contains("Bisnis", ignoreCase = true) -> "BIS"
                else -> "EKS"
            }
        }

        // =======================================================
        // 🟢 2. CETAK HURUF HEADER (A, B, C...) OTOMATIS
        // =======================================================
        cetakHeaderHuruf(spanCount, layoutType)

        // =======================================================
        // 🟢 3. GENERATE DATA KURSI
        // =======================================================
        initDataKendaraan(tabCount, layoutType)

        // 4. Setup RecyclerView
        rvSeats.layoutManager = GridLayoutManager(this, spanCount)

        adapter = SeatAdapter(currentDisplayList, maxSeatsAllowed) { kursiTerpilih ->
            val totalDipilih = gerbongData.values.flatten().count { it.isSelected }
            btnSimpanKursi.text = "Simpan ($totalDipilih/$maxSeatsAllowed Kursi)"
        }
        rvSeats.adapter = adapter
        btnSimpanKursi.text = "Simpan (0/$maxSeatsAllowed Kursi)"

        // 5. Buat Tab
        for (i in 1..tabCount) {
            val tabName = if (transportType.contains("Bus", true)) "Lantai Utama" else "$prefixTab-$i"
            tabLayoutGerbong.addTab(tabLayoutGerbong.newTab().setText(tabName))
        }

        tampilkanGerbong(1)

        tabLayoutGerbong.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tampilkanGerbong((tab?.position ?: 0) + 1)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 6. Tombol Simpan
        btnSimpanKursi.setOnClickListener {
            val semuaPilihan = gerbongData.values.flatten().filter { it.isSelected }

            if (semuaPilihan.size != maxSeatsAllowed) {
                Toast.makeText(this, "Anda harus memilih tepat $maxSeatsAllowed kursi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val arrayKursiTerpilih = semuaPilihan.map { it.id }.toTypedArray()
            val resultIntent = android.content.Intent().apply { putExtra("SELECTED_SEATS", arrayKursiTerpilih) }
            setResult(android.app.Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun cetakHeaderHuruf(spanCount: Int, layoutType: String) {
        llHeaderKursi.removeAllViews()
        llHeaderKursi.weightSum = spanCount.toFloat()

        fun addTextView(teks: String) {
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = teks
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#FF9800"))
                setTypeface(null, Typeface.BOLD)
            }
            llHeaderKursi.addView(tv)
        }

        addTextView("") // Kolom untuk Angka
        if (layoutType == "3-2") {
            addTextView("A"); addTextView("B"); addTextView("C")
            addTextView("") // Lorong
            addTextView("D"); addTextView("E")
        } else {
            addTextView("A"); addTextView("B")
            addTextView("") // Lorong
            addTextView("C"); addTextView("D")
        }
    }

    private fun initDataKendaraan(tabCount: Int, layoutType: String) {
        for (ruangan in 1..tabCount) {
            val seatList = mutableListOf<Seat>()
            val totalRows = if (layoutType == "3-2") 10 else 23 // Bus = 10 Baris, Kereta = 23 Baris

            for (row in 1..totalRows) {
                seatList.add(Seat(id = row.toString(), type = SeatType.ROW_LABEL))

                if (layoutType == "3-2") {
                    // Susunan Bus Ekonomi 3-2
                    seatList.add(Seat(id = "${ruangan}-${row}A", type = SeatType.SEAT, isBooked = Math.random() > 0.5))
                    seatList.add(Seat(id = "${ruangan}-${row}B", type = SeatType.SEAT, isBooked = Math.random() > 0.5))
                    seatList.add(Seat(id = "${ruangan}-${row}C", type = SeatType.SEAT, isBooked = Math.random() > 0.5))
                    seatList.add(Seat(id = "AISLE_${ruangan}_${row}", type = SeatType.AISLE))
                    seatList.add(Seat(id = "${ruangan}-${row}D", type = SeatType.SEAT, isBooked = Math.random() > 0.5))
                    seatList.add(Seat(id = "${ruangan}-${row}E", type = SeatType.SEAT, isBooked = Math.random() > 0.5))
                } else {
                    // Susunan 2-2 (Kereta / Bus Premium)
                    seatList.add(Seat(id = "${ruangan}-${row}A", type = SeatType.SEAT, isBooked = Math.random() > 0.5))
                    seatList.add(Seat(id = "${ruangan}-${row}B", type = SeatType.SEAT, isBooked = Math.random() > 0.5))
                    seatList.add(Seat(id = "AISLE_${ruangan}_${row}", type = SeatType.AISLE))
                    seatList.add(Seat(id = "${ruangan}-${row}C", type = SeatType.SEAT, isBooked = Math.random() > 0.5))
                    seatList.add(Seat(id = "${ruangan}-${row}D", type = SeatType.SEAT, isBooked = Math.random() > 0.5))
                }
            }
            gerbongData[ruangan] = seatList
        }
    }

    private fun tampilkanGerbong(gerbongKe: Int) {
        currentDisplayList.clear()
        gerbongData[gerbongKe]?.let { currentDisplayList.addAll(it) }
        adapter.notifyDataSetChanged()
    }
}