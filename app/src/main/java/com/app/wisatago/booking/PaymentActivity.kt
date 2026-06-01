package com.app.wisatago.booking

import android.content.Intent
import com.app.wisatago.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private var selectedPaymentMethod = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // 1. AMBIL DATA DARI CHECKOUT
        val transportType = intent.getStringExtra("EXTRA_TRANSPORT_TYPE") ?: ""
        val transactionType = intent.getStringExtra("EXTRA_TRANSACTION_TYPE") ?: transportType

        val pergiName = intent.getStringExtra("EXTRA_PERGI_NAME") ?: "Nama Armada"
        val origin = intent.getStringExtra("EXTRA_ORIGIN") ?: ""
        val destination = intent.getStringExtra("EXTRA_DESTINATION") ?: ""
        val passengerCount = intent.getIntExtra("EXTRA_PASSENGERS", 1)
        val isReturnTrip = intent.getBooleanExtra("EXTRA_IS_RETURN_TRIP", false)

        // Data Khusus Wisata
        val wisataId = intent.getStringExtra("EXTRA_WISATA_ID") ?: ""
        val ticketQty = intent.getIntExtra("EXTRA_TICKET_QTY", 1)

        val pricePergiTotal = intent.getDoubleExtra("EXTRA_PRICE_PERGI_TOTAL", 0.0)
        val subTotal = intent.getDoubleExtra("EXTRA_SUBTOTAL", 0.0)
        val tax = intent.getDoubleExtra("EXTRA_TAX", 0.0)
        val grandTotal = intent.getDoubleExtra("EXTRA_GRAND_TOTAL", 0.0)

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

        // 2. SET DATA KE TAMPILAN
        val tvTransportName = findViewById<TextView>(R.id.tv_pay_transport_name)
        val tvRouteSummary = findViewById<TextView>(R.id.tv_pay_route_summary)
        val layoutPergi = findViewById<RelativeLayout>(R.id.layout_pay_pergi)
        val layoutPulang = findViewById<RelativeLayout>(R.id.layout_pay_pulang)
        val tvLabelSubtotal = findViewById<TextView>(R.id.tv_label_pay_subtotal)

        if (transactionType == "WISATA") {
            val wisataName = intent.getStringExtra("EXTRA_WISATA_NAME") ?: "Wisata"
            tvTransportName.text = wisataName
            tvRouteSummary.visibility = View.GONE
            layoutPergi.visibility = View.GONE
            layoutPulang.visibility = View.GONE
            tvLabelSubtotal.text = "Harga Tiket (x$ticketQty)"
        } else {
            layoutPergi.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_label_pay_pergi).text = "Tiket Pergi (x$passengerCount)"
            val displayPricePergi = pricePergiTotal * 1.12
            findViewById<TextView>(R.id.tv_pay_price_pergi).text = "Rp ${formatter.format(pricePergiTotal)}"

            if (isReturnTrip) {
                val pulangName = intent.getStringExtra("EXTRA_PULANG_NAME") ?: ""
                val pricePulangTotal = intent.getDoubleExtra("EXTRA_PRICE_PULANG_TOTAL", 0.0)

                tvTransportName.text = "$pergiName & $pulangName"
                tvRouteSummary.text = "$origin ➔ $destination\nPulang Pergi • $passengerCount Penumpang"
                tvRouteSummary.visibility = View.VISIBLE

                layoutPulang.visibility = View.VISIBLE
                findViewById<TextView>(R.id.tv_label_pay_pulang).text = "Tiket Pulang (x$passengerCount)"
                val displayPricePulang = pricePulangTotal * 1.12
                findViewById<TextView>(R.id.tv_pay_price_pulang).text = "Rp ${formatter.format(pricePulangTotal)}"
            } else {
                tvTransportName.text = pergiName
                tvRouteSummary.text = "$origin ➔ $destination\nSekali Jalan • $passengerCount Penumpang"
                tvRouteSummary.visibility = View.VISIBLE
                layoutPulang.visibility = View.GONE
            }
            tvLabelSubtotal.text = "Total Harga Tiket"
        }

        findViewById<TextView>(R.id.tv_pay_subtotal).text = "Rp ${formatter.format(subTotal)}"
        findViewById<TextView>(R.id.tv_pay_tax).text = "Rp ${formatter.format(tax)}"
        findViewById<TextView>(R.id.tv_pay_grand_total).text = "Rp ${formatter.format(grandTotal)}"

        // 3. METODE PEMBAYARAN
        val cardSelectPayment = findViewById<View>(R.id.card_select_payment)
        val tvPaymentMethodName = findViewById<TextView>(R.id.tv_payment_method_name)
        val tvPaymentAccountNumber = findViewById<TextView>(R.id.tv_payment_account_number)
        val imgPaymentIcon = findViewById<ImageView>(R.id.img_payment_method_icon)

        val paymentList = listOf(
            PaymentMethod("BCA Virtual Account", R.drawable.logo_bca),
            PaymentMethod("BNI Virtual Account", R.drawable.logo_bni),
            PaymentMethod("Mandiri Virtual Account", R.drawable.logo_mandiri),
            PaymentMethod("Gopay", R.drawable.logo_gopay),
            PaymentMethod("OVO", R.drawable.logo_ovo),
            PaymentMethod("DANA", R.drawable.logo_dana),
            PaymentMethod("QRIS", R.drawable.logo_qris)
        )

        cardSelectPayment.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val bottomSheetView = layoutInflater.inflate(R.layout.layout_payment_selector, null)
            val rvPaymentMethods = bottomSheetView.findViewById<RecyclerView>(R.id.rv_payment_methods)
            rvPaymentMethods.layoutManager = LinearLayoutManager(this)

            val adapter = PaymentMethodAdapter(paymentList) { selectedMethod ->
                selectedPaymentMethod = selectedMethod.name
                tvPaymentMethodName.text = selectedMethod.name
                imgPaymentIcon.setImageResource(selectedMethod.iconResId)

                val randomSuffix = (100000..999999).random()
                val instruction = when (selectedPaymentMethod) {
                    "BCA Virtual Account" -> "8077 00$randomSuffix"
                    "BNI Virtual Account" -> "8810 11$randomSuffix"
                    "Mandiri Virtual Account" -> "89508 22$randomSuffix"
                    "Gopay", "OVO", "DANA", "ShopeePay" -> "Terhubung dengan Nomor HP Anda"
                    "QRIS" -> "Scan QR Code yang akan muncul setelah klik Bayar"
                    else -> "Klik untuk memilih"
                }
                tvPaymentAccountNumber.text = instruction
                tvPaymentAccountNumber.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
                bottomSheetDialog.dismiss()
            }
            rvPaymentMethods.adapter = adapter
            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        findViewById<ImageButton>(R.id.btn_back_payment).setOnClickListener { finish() }

        // 4. PROSES PEMBAYARAN & PENGIRIMAN DATA
        findViewById<MaterialButton>(R.id.btn_finish_payment).setOnClickListener {
            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(this, "Silakan pilih metode pembayaran terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefix = when {
                transactionType == "WISATA" -> "WS"
                transactionType.contains("Kereta", ignoreCase = true) -> "TR"
                transactionType.contains("Pesawat", ignoreCase = true) -> "AI"
                transactionType.contains("Bus", ignoreCase = true) -> "BU"
                else -> "WGO"
            }

            val randomCode = "$prefix-${(100000..999999).random()}"

            // 💡 Inisialisasi list untuk dititipkan ke payload
            val transportDetailsList = mutableListOf<TransportDetailRequest>()
            val attractionDetailsList = mutableListOf<AttractionDetailRequest>()

            if (transactionType == "WISATA") {
                // 💡 Masukkan data ke list wisata
                attractionDetailsList.add(
                    AttractionDetailRequest(
                        attraction_id = wisataId,
                        num_tickets = ticketQty,
                        subtotal = grandTotal
                    )
                )
            } else {
                val passengerNames = intent.getStringArrayListExtra("EXTRA_PASSENGER_NAMES") ?: arrayListOf()
                val seatsPergi = intent.getStringArrayListExtra("EXTRA_SEATS_PERGI") ?: arrayListOf()
                val seatsPulang = intent.getStringArrayListExtra("EXTRA_SEATS_PULANG") ?: arrayListOf()

                val listPenumpangPergi = mutableListOf<PassengerRequest>()
                for (i in 0 until passengerCount) {
                    listPenumpangPergi.add(
                        PassengerRequest(
                            passenger_name = passengerNames.getOrElse(i) { "Penumpang ${i + 1}" },
                            seat_number = seatsPergi.getOrElse(i) { "-" }
                        )
                    )
                }

                transportDetailsList.add(

                    TransportDetailRequest(
                        schedule_id = intent.getStringExtra("EXTRA_PERGI_SCHEDULE_ID") ?: "",
                        num_seats = passengerCount,
                        subtotal = pricePergiTotal,
                        passengers = listPenumpangPergi
                    )
                )

                if (isReturnTrip) {
                    val listPenumpangPulang = mutableListOf<PassengerRequest>()
                    for (i in 0 until passengerCount) {
                        listPenumpangPulang.add(
                            PassengerRequest(
                                passenger_name = passengerNames.getOrElse(i) { "Penumpang ${i + 1}" },
                                seat_number = seatsPulang.getOrElse(i) { "-" }
                            )
                        )
                    }

                    transportDetailsList.add(
                        TransportDetailRequest(
                            schedule_id = intent.getStringExtra("EXTRA_PULANG_SCHEDULE_ID") ?: "",
                            num_seats = passengerCount,
                            subtotal = intent.getDoubleExtra("EXTRA_PRICE_PULANG_TOTAL", 0.0),
                            passengers = listPenumpangPulang
                        )
                    )
                }
            }

            val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
            val realUserId = sharedPref.getString("USER_ID", "") ?: ""

            // 💡 BUNGKUS SEMUA DATA (Kirim List Wisata juga)
            val bookingPayload = BookingRequest(
                user_id = realUserId,
                booking_code = randomCode,
                total_amount = grandTotal,
                tax_amount = tax,
                payment_method = selectedPaymentMethod,
                transport_details = if (transactionType != "WISATA") transportDetailsList else null,
                attraction_details = if (transactionType == "WISATA") attractionDetailsList else null
            )

            val btnPay = findViewById<MaterialButton>(R.id.btn_finish_payment)
            btnPay.isEnabled = false
            btnPay.text = "Memproses..."

            ApiClient.instance.createBooking(bookingPayload).enqueue(object : Callback<ResponseBooking> {
                override fun onResponse(call: Call<ResponseBooking>, response: Response<ResponseBooking>) {
                    btnPay.isEnabled = true
                    btnPay.text = "Bayar Sekarang"

                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@PaymentActivity, response.body()?.message ?: "Berhasil!", Toast.LENGTH_SHORT).show()
                        btnPay.text = "Berhasil! Mengalihkan..."

                        Handler(Looper.getMainLooper()).postDelayed({
                            val successIntent = Intent(this@PaymentActivity, PaymentSuccessActivity::class.java).apply {
                                putExtra("EXTRA_BOOKING_CODE", response.body()?.booking_code ?: randomCode)
                                putExtra("EXTRA_GRAND_TOTAL", grandTotal)
                                putExtra("EXTRA_IS_RETURN_TRIP", isReturnTrip)

                                if (transactionType == "WISATA") {
                                    putExtra("EXTRA_PERGI_NAME", intent.getStringExtra("EXTRA_WISATA_NAME"))
                                    putExtra("EXTRA_TICKET_QTY", ticketQty)
                                } else {
                                    putExtra("EXTRA_PERGI_NAME", pergiName)
                                    putExtra("EXTRA_PULANG_NAME", intent.getStringExtra("EXTRA_PULANG_NAME"))
                                    putExtra("EXTRA_ORIGIN", origin)
                                    putExtra("EXTRA_DESTINATION", destination)
                                    putExtra("EXTRA_PASSENGERS", passengerCount)
                                }
                            }
                            startActivity(successIntent)
                            finish()
                        }, 2000)
                    } else {
                        Toast.makeText(this@PaymentActivity, "Gagal: Kesalahan pada server.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBooking>, t: Throwable) {
                    btnPay.isEnabled = true
                    btnPay.text = "Bayar Sekarang"
                    Toast.makeText(this@PaymentActivity, "Koneksi Bermasalah: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}