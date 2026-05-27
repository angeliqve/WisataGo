package com.app.wisatago.booking

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.R
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher

class CheckoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val transportType = intent.getStringExtra("EXTRA_TRANSPORT_TYPE") ?: "Tiket Transportasi"
        val passengerCount = intent.getIntExtra("EXTRA_PASSENGERS", 1)
        val isReturnTrip = intent.getBooleanExtra("EXTRA_IS_RETURN_TRIP", false)

        // 1. DATA TIKET PERGI (Support Kereta & Bus)
        val pergiName = intent.getStringExtra("EXTRA_PERGI_NAME") ?: "Nama Armada"
        val pergiClass = intent.getStringExtra("EXTRA_PERGI_CLASS") ?: "Kelas"
        val pergiOrigin = intent.getStringExtra("EXTRA_PERGI_ORIGIN") ?: ""
        val pergiDest = intent.getStringExtra("EXTRA_PERGI_DESTINATION") ?: ""
        val pergiDate = intent.getStringExtra("EXTRA_PERGI_DATE") ?: ""
        val pergiDep = intent.getStringExtra("EXTRA_PERGI_DEP_TIME") ?: "00:00"
        val pergiArr = intent.getStringExtra("EXTRA_PERGI_ARR_TIME") ?: "00:00"
        val pergiPrice = intent.getDoubleExtra("EXTRA_PERGI_PRICE", 0.0)
        val pergiScheduleId = intent.getStringExtra("EXTRA_PERGI_SCHEDULE_ID") ?: ""

        // 🟢 TAMBAHAN: Untuk Bus (bisa custom icon atau title)
        val transportIcon = when {
            transportType.contains("Bus", ignoreCase = true) -> "🚌"
            transportType.contains("Kereta", ignoreCase = true) -> "🚆"
            transportType.contains("Pesawat", ignoreCase = true) -> "✈️"
            else -> "🎫"
        }

        // Set UI Tiket Pergi dengan icon transportasi
        findViewById<TextView>(R.id.tv_co_transport_type_pergi).text = "$transportIcon Tiket Pergi"
        findViewById<TextView>(R.id.tv_co_transport_name_pergi).text = pergiName
        findViewById<TextView>(R.id.tv_co_class_pergi).text = pergiClass
        findViewById<TextView>(R.id.tv_co_route_pergi).text = "$pergiOrigin ➔ $pergiDest"
        findViewById<TextView>(R.id.tv_co_date_pergi).text = pergiDate
        findViewById<TextView>(R.id.tv_co_time_pergi).text = "$pergiDep ➔ $pergiArr"
        findViewById<TextView>(R.id.tv_co_passenger_pergi).text = "$passengerCount Penumpang"
        findViewById<TextView>(R.id.tv_co_duration_pergi).text = hitungDurasi(pergiDep, pergiArr)

        val seatsPergi = generateRandomSeatsList(passengerCount)
        findViewById<TextView>(R.id.tv_co_seats_pergi).text = "Kursi: ${seatsPergi.joinToString(", ")}"

        // 2. DATA TIKET PULANG (Jika Ada) - Support Bus juga
        var totalTicketPricePerPerson = pergiPrice
        var seatsPulang: List<String> = emptyList()
        var pulangScheduleId = ""

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
            pulangScheduleId = intent.getStringExtra("EXTRA_PULANG_SCHEDULE_ID") ?: ""

            totalTicketPricePerPerson += pulangPrice

            // Set UI Tiket Pulang dengan icon transportasi
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

        val listEtPergi = mutableListOf<EditText>()
        val listEtPulang = mutableListOf<EditText>()

        val totalForms = if (isReturnTrip) passengerCount * 2 else passengerCount

        for (i in 0 until totalForms) {
            val formView = layoutInflater.inflate(R.layout.item_passenger_form, llPassengerContainer, false)
            val tvTitle = formView.findViewById<TextView>(R.id.tv_passenger_title)
            val etName = formView.findViewById<EditText>(R.id.et_passenger_name)

            val isPulang = isReturnTrip && i >= passengerCount
            val tripLabel = if (isPulang) "Pulang" else "Pergi"

            val seatNumber = if (isPulang) {
                seatsPulang[i % passengerCount]
            } else {
                seatsPergi[i]
            }

            // Tambahkan icon transportasi di judul form
            val tripIcon = if (isPulang) "🔁" else transportIcon
            tvTitle.text = "$tripIcon Penumpang ${(i % passengerCount) + 1} ($tripLabel) - Kursi: $seatNumber"

            etName.setText(assignedNames[i % passengerCount])

            if (isPulang) {
                listEtPulang.add(etName)
            } else {
                listEtPergi.add(etName)
            }

            llPassengerContainer.addView(formView)
        }

        // Sinkronisasi nama penumpang PP
        if (isReturnTrip) {
            val minSize = minOf(listEtPergi.size, listEtPulang.size)
            for (i in 0 until minSize) {
                val etPergi = listEtPergi[i]
                val etPulang = listEtPulang[i]

                etPergi.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (etPulang.text.toString() != s.toString()) {
                            etPulang.setText(s.toString())
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })
            }
        }

        // 4. KALKULASI TOTAL BIAYA
        val subTotal = totalTicketPricePerPerson * passengerCount
        val tax = subTotal * 0.12
        val totalAll = subTotal + tax

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        findViewById<TextView>(R.id.tv_co_base_price).text = "Rp ${formatter.format(subTotal)}"
        findViewById<TextView>(R.id.tv_co_tax).text = "Rp ${formatter.format(tax)}"
        findViewById<TextView>(R.id.tv_co_total_all).text = "Rp ${formatter.format(totalAll)}"

        // 5. TOMBOL BACK
        findViewById<ImageButton>(R.id.btn_back_checkout).setOnClickListener { finish() }

        // 6. TOMBOL BAYAR
        findViewById<MaterialButton>(R.id.btn_co_pay).setOnClickListener {
            val paymentIntent = Intent(this, PaymentActivity::class.java)

            // Kirim tipe transportasi
            paymentIntent.putExtra("EXTRA_TRANSPORT_TYPE", transportType)

            // Ambil nama penumpang dari form
            val daftarNamaPenumpang = ArrayList<String>()
            val daftarKursiPergi = ArrayList<String>()
            val daftarKursiPulang = ArrayList<String>()

            for (i in 0 until passengerCount) {
                val nama = listEtPergi[i].text.toString().ifEmpty { "Penumpang ${i + 1}" }
                daftarNamaPenumpang.add(nama)
                daftarKursiPergi.add(seatsPergi[i])

                if (isReturnTrip) {
                    daftarKursiPulang.add(seatsPulang[i])
                }
            }

            // Kirim data ke PaymentActivity
            paymentIntent.putStringArrayListExtra("EXTRA_PASSENGER_NAMES", daftarNamaPenumpang)
            paymentIntent.putStringArrayListExtra("EXTRA_SEATS_PERGI", daftarKursiPergi)
            if (isReturnTrip) {
                paymentIntent.putStringArrayListExtra("EXTRA_SEATS_PULANG", daftarKursiPulang)
            }

            // Kirim identitas perjalanan
            paymentIntent.putExtra("EXTRA_PERGI_SCHEDULE_ID", pergiScheduleId)
            paymentIntent.putExtra("EXTRA_PERGI_NAME", pergiName)
            paymentIntent.putExtra("EXTRA_ORIGIN", pergiOrigin)
            paymentIntent.putExtra("EXTRA_DESTINATION", pergiDest)
            paymentIntent.putExtra("EXTRA_PASSENGERS", passengerCount)
            paymentIntent.putExtra("EXTRA_IS_RETURN_TRIP", isReturnTrip)

            // Hitung & Kirim Harga
            val totalPergi = pergiPrice * passengerCount
            paymentIntent.putExtra("EXTRA_PRICE_PERGI_TOTAL", totalPergi)

            if (isReturnTrip) {
                val pulangName = intent.getStringExtra("EXTRA_PULANG_NAME") ?: ""
                val pulangPrice = intent.getDoubleExtra("EXTRA_PULANG_PRICE", 0.0)
                val totalPulang = pulangPrice * passengerCount

                paymentIntent.putExtra("EXTRA_PULANG_SCHEDULE_ID", pulangScheduleId)
                paymentIntent.putExtra("EXTRA_PULANG_NAME", pulangName)
                paymentIntent.putExtra("EXTRA_PRICE_PULANG_TOTAL", totalPulang)
            }

            // Kirim rincian total
            paymentIntent.putExtra("EXTRA_SUBTOTAL", subTotal)
            paymentIntent.putExtra("EXTRA_TAX", tax)
            paymentIntent.putExtra("EXTRA_GRAND_TOTAL", totalAll)

            startActivity(paymentIntent)
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