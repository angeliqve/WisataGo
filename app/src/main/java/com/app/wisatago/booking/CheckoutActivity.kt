package com.app.wisatago.booking

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

        val pergiName = intent.getStringExtra("EXTRA_PERGI_NAME") ?: "Nama Armada"
        val pergiClass = intent.getStringExtra("EXTRA_PERGI_CLASS") ?: "Kelas"
        val pergiOrigin = intent.getStringExtra("EXTRA_PERGI_ORIGIN") ?: ""
        val pergiDest = intent.getStringExtra("EXTRA_PERGI_DESTINATION") ?: ""
        val pergiDate = intent.getStringExtra("EXTRA_PERGI_DATE") ?: ""
        val pergiDep = intent.getStringExtra("EXTRA_PERGI_DEP_TIME") ?: "00:00"
        val pergiArr = intent.getStringExtra("EXTRA_PERGI_ARR_TIME") ?: "00:00"
        val pergiPrice = intent.getDoubleExtra("EXTRA_PERGI_PRICE", 0.0)
        val pergiScheduleId = intent.getStringExtra("EXTRA_PERGI_SCHEDULE_ID") ?: ""

        val transportIcon = when {
            transportType.contains("Bus", ignoreCase = true) -> "🚌"
            transportType.contains("Kereta", ignoreCase = true) -> "🚆"
            transportType.contains("Pesawat", ignoreCase = true) -> "✈️"
            else -> "🎫"
        }

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
        val listEtPhonePergi = mutableListOf<EditText>()
        val listEtPhonePulang = mutableListOf<EditText>()

        val totalForms = if (isReturnTrip) passengerCount * 2 else passengerCount

        for (i in 0 until totalForms) {
            val formView = layoutInflater.inflate(R.layout.item_passenger_form, llPassengerContainer, false)
            val tvTitle = formView.findViewById<TextView>(R.id.tv_passenger_title)
            val etName = formView.findViewById<EditText>(R.id.et_passenger_name)
            val etPhone = formView.findViewById<EditText>(R.id.et_passenger_phone)

            val isPulang = isReturnTrip && i >= passengerCount
            val tripLabel = if (isPulang) "Pulang" else "Pergi"

            val seatNumber = if (isPulang) {
                seatsPulang[i % passengerCount]
            } else {
                seatsPergi[i]
            }

            val tripIcon = if (isPulang) "🔁" else transportIcon
            tvTitle.text = "$tripIcon Penumpang ${(i % passengerCount) + 1} ($tripLabel) - Kursi: $seatNumber"

            etName.setText(assignedNames[i % passengerCount])

            etPhone.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false
                private val hyphen = "-"

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return

                    val inputString = s.toString()
                    var current = inputString.replace(Regex("[^0-9]"), "")

                    if (current.length > 13) {
                        current = current.substring(0, 13)
                    }

                    val formatted = StringBuilder()
                    for (k in current.indices) {
                        formatted.append(current[k])
                        if ((k + 1) % 4 == 0 && (k + 1) < current.length) {
                            formatted.append(hyphen)
                        }
                    }

                    if (formatted.toString() == inputString) {
                        return
                    }

                    isUpdating = true
                    etPhone.setText(formatted.toString())
                    etPhone.setSelection(formatted.length)
                    isUpdating = false
                }
            })

            if (isPulang) {
                listEtPulang.add(etName)
                listEtPhonePulang.add(etPhone)
            } else {
                listEtPergi.add(etName)
                listEtPhonePergi.add(etPhone)
            }

            llPassengerContainer.addView(formView)
        }

        if (isReturnTrip) {
            val minSize = minOf(listEtPergi.size, listEtPulang.size)
            for (i in 0 until minSize) {
                val etPergi = listEtPergi[i]
                val etPulang = listEtPulang[i]
                val etPhonePergi = listEtPhonePergi[i]
                val etPhonePulang = listEtPhonePulang[i]

                etPergi.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (etPulang.text.toString() != s.toString()) {
                            etPulang.setText(s.toString())
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })

                etPhonePergi.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (etPhonePulang.text.toString() != s.toString()) {
                            etPhonePulang.setText(s.toString())
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })
            }
        }

        val subTotal = totalTicketPricePerPerson * passengerCount
        val tax = subTotal * 0.12
        val totalAll = subTotal + tax

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        findViewById<TextView>(R.id.tv_co_base_price).text = "Rp ${formatter.format(subTotal)}"
        findViewById<TextView>(R.id.tv_co_tax).text = "Rp ${formatter.format(tax)}"
        findViewById<TextView>(R.id.tv_co_total_all).text = "Rp ${formatter.format(totalAll)}"

        findViewById<ImageButton>(R.id.btn_back_checkout).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btn_co_pay).setOnClickListener {

            for (i in 0 until passengerCount) {
                val phoneText = listEtPhonePergi[i].text.toString().trim()

                if (phoneText.isEmpty()) {
                    Toast.makeText(this, "Nomor telepon Penumpang ${i + 1} wajib diisi!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val cleanPhone = phoneText.replace("-", "")
                val regexPhone = Regex("^(08|628)[0-9]{8,11}$")

                if (!cleanPhone.matches(regexPhone)) {
                    Toast.makeText(this, "Format nomor Penumpang ${i + 1} tidak valid! Harus diawali 08/628", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            val paymentIntent = Intent(this, PaymentActivity::class.java)
            paymentIntent.putExtra("EXTRA_TRANSPORT_TYPE", transportType)

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

            paymentIntent.putStringArrayListExtra("EXTRA_PASSENGER_NAMES", daftarNamaPenumpang)
            paymentIntent.putStringArrayListExtra("EXTRA_SEATS_PERGI", daftarKursiPergi)
            if (isReturnTrip) {
                paymentIntent.putStringArrayListExtra("EXTRA_SEATS_PULANG", daftarKursiPulang)
            }

            paymentIntent.putExtra("EXTRA_PERGI_SCHEDULE_ID", pergiScheduleId)
            paymentIntent.putExtra("EXTRA_PERGI_NAME", pergiName)
            paymentIntent.putExtra("EXTRA_ORIGIN", pergiOrigin)
            paymentIntent.putExtra("EXTRA_DESTINATION", pergiDest)
            paymentIntent.putExtra("EXTRA_PASSENGERS", passengerCount)
            paymentIntent.putExtra("EXTRA_IS_RETURN_TRIP", isReturnTrip)

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