package com.app.wisatago.transport

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.R
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val transportType = intent.getStringExtra("EXTRA_TRANSPORT_TYPE") ?: "Tiket Transportasi"
        val passengerCount = intent.getIntExtra("EXTRA_PASSENGERS", 1)
        val isReturnTrip = intent.getBooleanExtra("EXTRA_IS_RETURN_TRIP", false)

        // 1. DATA TIKET PERGI
        val pergiName = intent.getStringExtra("EXTRA_PERGI_NAME") ?: "Nama Armada"
        val pergiClass = intent.getStringExtra("EXTRA_PERGI_CLASS") ?: "Kelas"
        val pergiOrigin = intent.getStringExtra("EXTRA_PERGI_ORIGIN") ?: ""
        val pergiDest = intent.getStringExtra("EXTRA_PERGI_DESTINATION") ?: ""
        val pergiDate = intent.getStringExtra("EXTRA_PERGI_DATE") ?: ""
        val pergiDep = intent.getStringExtra("EXTRA_PERGI_DEP_TIME") ?: "00:00"
        val pergiArr = intent.getStringExtra("EXTRA_PERGI_ARR_TIME") ?: "00:00"
        val pergiPrice = intent.getDoubleExtra("EXTRA_PERGI_PRICE", 0.0)

        // Set UI Tiket Pergi
        findViewById<TextView>(R.id.tv_co_transport_type_pergi).text = "Tiket Pergi"
        findViewById<TextView>(R.id.tv_co_transport_name_pergi).text = pergiName
        findViewById<TextView>(R.id.tv_co_class_pergi).text = pergiClass
        findViewById<TextView>(R.id.tv_co_route_pergi).text = "$pergiOrigin ➔ $pergiDest"
        findViewById<TextView>(R.id.tv_co_date_pergi).text = pergiDate
        findViewById<TextView>(R.id.tv_co_time_pergi).text = "$pergiDep ➔ $pergiArr"
        findViewById<TextView>(R.id.tv_co_passenger_pergi).text = "$passengerCount Penumpang"
        findViewById<TextView>(R.id.tv_co_duration_pergi).text = hitungDurasi(pergiDep, pergiArr)

        val seatsPergi = generateRandomSeatsList(passengerCount)
        findViewById<TextView>(R.id.tv_co_seats_pergi).text = "Kursi: ${seatsPergi.joinToString(", ")}"

        // 2. DATA TIKET PULANG (Jika Ada)
        var totalTicketPricePerPerson = pergiPrice

        var seatsPulang: List<String> = emptyList()

        if (isReturnTrip) {
            findViewById<View>(R.id.card_tiket_pulang).visibility = View.VISIBLE

            val pulangName = intent.getStringExtra("EXTRA_PULANG_NAME") ?: "Nama Armada"
            val pulangClass = intent.getStringExtra("EXTRA_PULANG_CLASS") ?: "Kelas"
            val pulangOrigin = intent.getStringExtra("EXTRA_PULANG_ORIGIN") ?: ""
            val pulangDest = intent.getStringExtra("EXTRA_PULANG_DESTINATION") ?: ""
            val pulangDate = intent.getStringExtra("EXTRA_PULANG_DATE") ?: ""
            val pulangDep = intent.getStringExtra("EXTRA_PULANG_DEP_TIME") ?: "00:00"
            val pulangArr = intent.getStringExtra("EXTRA_PULANG_ARR_TIME") ?: "00:00"
            val pulangPrice = intent.getDoubleExtra("EXTRA_PULANG_PRICE", 0.0)

            totalTicketPricePerPerson += pulangPrice // Gabungkan harga 1 orang (Pergi + Pulang)

            // Set UI Tiket Pulang
            findViewById<TextView>(R.id.tv_co_transport_name_pulang).text = pulangName
            findViewById<TextView>(R.id.tv_co_class_pulang).text = pulangClass
            findViewById<TextView>(R.id.tv_co_route_pulang).text = "$pulangOrigin ➔ $pulangDest"
            findViewById<TextView>(R.id.tv_co_date_pulang).text = pulangDate
            findViewById<TextView>(R.id.tv_co_time_pulang).text = "$pulangDep ➔ $pulangArr"
            findViewById<TextView>(R.id.tv_co_passenger_pulang).text = "$passengerCount Penumpang"
            findViewById<TextView>(R.id.tv_co_duration_pulang).text = hitungDurasi(pulangDep, pulangArr)

            seatsPulang = generateRandomSeatsList(passengerCount)
            findViewById<TextView>(R.id.tv_co_seats_pulang).text = "Kursi: ${seatsPulang.joinToString(", ")}"
        }

        // 3. CETAK FORM NAMA PENUMPANG
        val baseNames = mutableListOf("Ahmad Billal", "Raffi Anggi", "Kayla Ishmah", "Raihan Oktoleven", "Angelique Gabriel")
        baseNames.shuffle()
        val assignedNames = mutableListOf<String>()
        for (i in 0 until passengerCount) {
            assignedNames.add(baseNames[i % baseNames.size])
        }


        val llPassengerContainer = findViewById<LinearLayout>(R.id.ll_passenger_container)
        llPassengerContainer.removeAllViews()

        // Daftar list untuk menampung referensi EditText agar bisa di-sync
        val listEtPergi = mutableListOf<EditText>()
        val listEtPulang = mutableListOf<EditText>()

        val totalForms = if (isReturnTrip) passengerCount * 2 else passengerCount

        for (i in 0 until totalForms) {
            val formView = layoutInflater.inflate(R.layout.item_passenger_form, llPassengerContainer, false)
            val tvTitle = formView.findViewById<TextView>(R.id.tv_passenger_title)
            val etName = formView.findViewById<EditText>(R.id.et_passenger_name)

            // Logika untuk menentukan apakah ini form untuk perjalanan Pulang
            val isPulang = isReturnTrip && i >= passengerCount
            val tripLabel = if (isPulang) "Pulang" else "Pergi"

            // Mengambil nomor kursi yang tepat
            val seatNumber = if (isPulang) {
                seatsPulang[i % passengerCount]
            } else {
                seatsPergi[i]
            }

            // Set judul formulir
            tvTitle.text = "Penumpang ${ (i % passengerCount) + 1 } ($tripLabel) - Kursi: $seatNumber"

            // Masukkan nama acak
            etName.setText(assignedNames[i % passengerCount])

            // Simpan referensi ke list untuk keperluan sinkronisasi
            if (isPulang) {
                listEtPulang.add(etName)
            } else {
                listEtPergi.add(etName)
            }

            llPassengerContainer.addView(formView)
        }

        // Jika tiket PP, setiap ketikan di form Pergi akan mengupdate form Pulang
        if (isReturnTrip) {
            // Kita pastikan jumlah list sama
            val minSize = minOf(listEtPergi.size, listEtPulang.size)

            for (i in 0 until minSize) {
                val etPergi = listEtPergi[i]
                val etPulang = listEtPulang[i]

                etPergi.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        // Saat teks di tiket pergi berubah, langsung update tiket pulang
                        if (etPulang.text.toString() != s.toString()) {
                            etPulang.setText(s.toString())
                        }
                    }

                    override fun afterTextChanged(s: android.text.Editable?) {}
                })
            }
        }
        // 4. KALKULASI TOTAL BIAYA KESELURUHAN
        val subTotal = totalTicketPricePerPerson * passengerCount
        val tax = subTotal * 0.12
        val totalAll = subTotal + tax

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        findViewById<TextView>(R.id.tv_co_base_price).text = "Rp ${formatter.format(subTotal)}"
        findViewById<TextView>(R.id.tv_co_tax).text = "Rp ${formatter.format(tax)}"
        findViewById<TextView>(R.id.tv_co_total_all).text = "Rp ${formatter.format(totalAll)}"

        // 5. TOMBOL
        findViewById<ImageButton>(R.id.btn_back_checkout).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btn_co_pay).setOnClickListener {
            Toast.makeText(this, "Memproses pembayaran Rp ${formatter.format(totalAll)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hitungDurasi(berangkat: String, tiba: String): String {
        return try {
            val depParts = berangkat.split(":")
            val arrParts = tiba.split(":")
            val depJam = depParts[0].toInt(); val depMenit = depParts[1].toInt()
            val arrJam = arrParts[0].toInt(); val arrMenit = arrParts[1].toInt()
            var totalMenitBerangkat = (depJam * 60) + depMenit
            var totalMenitTiba = (arrJam * 60) + arrMenit
            if (totalMenitTiba < totalMenitBerangkat) totalMenitTiba += 24 * 60
            val selisihMenit = totalMenitTiba - totalMenitBerangkat
            val jam = selisihMenit / 60; val menit = selisihMenit % 60
            if (menit == 0) "${jam}j" else "${jam}j ${menit}m"
        } catch (e: Exception) { "-" }
    }

    private fun generateRandomSeatsList(count: Int): List<String> {
        val columns = listOf("A", "B", "C", "D")
        val seats = mutableSetOf<String>()
        while (seats.size < count) {
            val row = (1..20).random()
            val col = columns.random()
            seats.add("$row$col")
        }
        return seats.toList()
    }
}