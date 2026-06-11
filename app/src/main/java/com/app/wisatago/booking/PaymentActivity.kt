package com.app.wisatago.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.api.ApiClient
import com.app.wisatago.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private var selectedPaymentMethod = ""

    // 🟢 Variabel Pelacak Status Add-on
    private var isAddonSelected = false
    private var addonId = ""
    private var addonName = ""
    private var addonPriceBase = 0.0
    private var addonDate = ""
    private var addonTime = ""

    // 🟢 Variabel Kalkulator Dinamis (Bisa Berubah)
    private var dynamicSubtotal = 0.0
    private var dynamicTax = 0.0
    private var dynamicGrandTotal = 0.0

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

        val wisataId = intent.getStringExtra("EXTRA_WISATA_ID") ?: ""
        val ticketQty = intent.getIntExtra("EXTRA_TICKET_QTY", 1)

        val pricePergiTotal = intent.getDoubleExtra("EXTRA_PRICE_PERGI_TOTAL", 0.0)
        val pricePulangTotal = intent.getDoubleExtra("EXTRA_PRICE_PULANG_TOTAL", 0.0)

        // Set nilai awal
        val subTotal = intent.getDoubleExtra("EXTRA_SUBTOTAL", 0.0)
        val tax = intent.getDoubleExtra("EXTRA_TAX", 0.0)
        val grandTotal = intent.getDoubleExtra("EXTRA_GRAND_TOTAL", 0.0)

        // Setel Kalkulator ke Harga Awal
        dynamicSubtotal = subTotal
        dynamicTax = tax
        dynamicGrandTotal = grandTotal

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

        // 2. SET DATA KE TAMPILAN
        val tvTransportName = findViewById<TextView>(R.id.tv_pay_transport_name)
        val tvRouteSummary = findViewById<TextView>(R.id.tv_pay_route_summary)
        val layoutPergi = findViewById<RelativeLayout>(R.id.layout_pay_pergi)
        val layoutPulang = findViewById<RelativeLayout>(R.id.layout_pay_pulang)
        val tvLabelSubtotal = findViewById<TextView>(R.id.tv_label_pay_subtotal)

        // UI Komponen Rincian Add-on
        val layoutAddon = findViewById<RelativeLayout>(R.id.layout_pay_addon)
        val tvLabelPayAddon = findViewById<TextView>(R.id.tv_label_pay_addon)
        val tvPriceAddon = findViewById<TextView>(R.id.tv_pay_price_addon)

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
            findViewById<TextView>(R.id.tv_pay_price_pergi).text = "Rp ${formatter.format(pricePergiTotal * 1.12)}"

            if (isReturnTrip) {
                val pulangName = intent.getStringExtra("EXTRA_PULANG_NAME") ?: ""

                tvTransportName.text = "$pergiName & $pulangName"
                tvRouteSummary.text = "$origin ➔ $destination\nPulang Pergi • $passengerCount Penumpang"
                tvRouteSummary.visibility = View.VISIBLE

                layoutPulang.visibility = View.VISIBLE
                findViewById<TextView>(R.id.tv_label_pay_pulang).text = "Tiket Pulang (x$passengerCount)"
                findViewById<TextView>(R.id.tv_pay_price_pulang).text = "Rp ${formatter.format(pricePulangTotal * 1.12)}"
            } else {
                tvTransportName.text = pergiName
                tvRouteSummary.text = "$origin ➔ $destination\nSekali Jalan • $passengerCount Penumpang"
                tvRouteSummary.visibility = View.VISIBLE
                layoutPulang.visibility = View.GONE
            }
            tvLabelSubtotal.text = "Total Harga Tiket"
        }

        // Tampilkan Harga Total Awal
        val tvSubtotalText = findViewById<TextView>(R.id.tv_pay_subtotal)
        val tvTaxText = findViewById<TextView>(R.id.tv_pay_tax)
        val tvGrandTotalText = findViewById<TextView>(R.id.tv_pay_grand_total)

        tvSubtotalText.text = "Rp ${formatter.format(dynamicSubtotal)}"
        tvTaxText.text = "Rp ${formatter.format(dynamicTax)}"
        tvGrandTotalText.text = "Rp ${formatter.format(dynamicGrandTotal)}"


        // =========================================================================
        // 🟢 3. SMART RECOMMENDATION ADD-ON WISATA BERDASARKAN DESTINASI
        // =========================================================================
        val cardAddon = findViewById<MaterialCardView>(R.id.card_addon_wisata)
        val tvSelectedAddon = findViewById<TextView>(R.id.tv_selected_addon)

        val destLower = destination.lowercase(Locale.getDefault())
        val displayCity = when {
            destLower.contains("jakarta") || destLower.contains("gambir") || destLower.contains("pulo gebang") -> "Jakarta"
            destLower.contains("bandung") || destLower.contains("bandung") || destLower.contains("bandung") -> "Bandung"
            destLower.contains("surabaya") || destLower.contains("pasar turi") || destLower.contains("purabaya") -> "Surabaya"
            destLower.contains("yogyakarta") || destLower.contains("tugu") || destLower.contains("giwangan") -> "Yogyakarta"
            destLower.contains("cirebon") || destLower.contains("harjamukti") -> "Cirebon"
            destLower.contains("denpasar") || destLower.contains("bali") || destLower.contains("mengwi") -> "Bali"
            else -> destination.split(" ")[0]
        }
        // Sembunyikan Add-on jika ini adalah pesanan tiket wisata murni
        if (transactionType == "WISATA") {
            cardAddon.visibility = View.GONE
        } else {
            cardAddon.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_addon_subtitle).text = "Lengkapi perjalananmu di $displayCity!"
        }

        cardAddon.setOnClickListener {
            val destAman = destination.lowercase(Locale.getDefault()).trim()

            // LOGIKA PINTAR REKOMENDASI WISATA
            val (daftarWisata, hargaWisata, mockIds) = when {
                destAman.contains("jakarta") || destAman.contains("gambir") || destAman.contains("pulo gebang") -> Triple(
                    arrayOf("Taman Mini Indonesia Indah (Rp 25.000)", "Dunia Fantasi / Dufan (Rp 275.000)", "Monumen Nasional / Monas (Rp 15.000)", "Batal Pakai Add-on"),
                    arrayOf(25000.0, 275000.0, 15000.0, 0.0),
                    // 🚨 PERHATIAN: GANTI TEKS DI BAWAH DENGAN UUID PENUH DARI NEON DB ANDA
                    arrayOf("8a645721-235a-4709-80d1-82f09ef79b69", "996f9ede-ff71-45a1-9325-1bd02dfbd260", "ce6cc1fc-8d2e-431b-9b2e-2072002b344f", "")
                )
                destAman.contains("surabaya") || destAman.contains("pasar turi") || destAman.contains("purabaya") -> Triple(
                    arrayOf("Kebun Binatang Surabaya (Rp 20.000)", "Monumen Kapal Selam (Rp 15.000)", "Batal Pakai Add-on"),
                    arrayOf(20000.0, 15000.0, 0.0),
                    arrayOf("59e69fcd-901c-49cb-a3d8-0e39f22b464d", "d56837cd-473d-4a10-811a-6dab38376dde", "")
                )
                destAman.contains("yogyakarta") || destAman.contains("tugu") || destAman.contains("giwangan") -> Triple(
                    arrayOf("Keraton Yogyakarta (Rp 15.000)", "Candi Prambanan (Rp 50.000)", "Batal Pakai Add-on"),
                    arrayOf(15000.0, 50000.0, 0.0),
                    arrayOf("49d402a0-fa3b-442f-8a26-8445c64042e3", "d9ac4343-ed56-4296-b8bc-785d5580ace6", "")
                )
                destAman.contains("cirebon") || destAman.contains("harjamukti") -> Triple(
                    arrayOf("Goa Sunyaragi (Rp 15.000)", "Keraton Kasepuhan (Rp 20.000)", "Batal Pakai Add-on"),
                    arrayOf(15000.0, 20000.0, 0.0),
                    arrayOf("16f39520-756e-447f-9ba9-d9348934ed31", "70c6aaec-4f5e-465d-91de-aed6e4420fce", "")
                )
                destAman.contains("denpasar") || destAman.contains("bali") || destAman.contains("mengwi") -> Triple(
                    arrayOf("Bali Zoo (Rp 110.000)", "Garuda Wisnu Kencana (Rp 125.000)", "Batal Pakai Add-on"),
                    arrayOf(110000.0, 125000.0, 0.0),
                    arrayOf("4a3a8a9a-1005-4c87-b0a9-78ed0dac552a", "bbaab698-9468-4a08-bd7c-093c268f0c68", "")
                )
                else -> Triple(
                    arrayOf("Belum ada wisata di kota ini", "Batal Pakai Add-on"),
                    arrayOf(0.0, 0.0),
                    arrayOf("", "")
                )
            }

            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Pilih Wisata di $displayCity")
                .setIcon(android.R.drawable.ic_dialog_map) // 💡 Tambahan Ikon Peta di Judul
                .setSingleChoiceItems(daftarWisata, -1) { dialog, which ->

                    dialog.dismiss() // 💡 Wajib ditambahkan agar dialog langsung tertutup saat dipilih

                    // Jika user membatalkan Add-on
                    if (which == daftarWisata.size - 1 || mockIds[which].isEmpty()) {
                        isAddonSelected = false
                        layoutAddon.visibility = View.GONE
                        tvSelectedAddon.text = "Klik untuk memilih wisata, tanggal, & jam"

                        // Kembalikan ke Harga Semula
                        dynamicSubtotal = subTotal
                        dynamicTax = tax
                        dynamicGrandTotal = grandTotal

                        findViewById<TextView>(R.id.tv_pay_subtotal).text = "Rp ${formatter.format(dynamicSubtotal)}"
                        findViewById<TextView>(R.id.tv_pay_tax).text = "Rp ${formatter.format(dynamicTax)}"
                        findViewById<TextView>(R.id.tv_pay_grand_total).text = "Rp ${formatter.format(dynamicGrandTotal)}"

                        return@setSingleChoiceItems // 💡 Sesuaikan label return
                    }

                    addonName = daftarWisata[which].split(" (")[0]
                    addonPriceBase = hargaWisata[which]
                    addonId = mockIds[which]

                    // ========================================================
                    // 📅 🟢 KUNCI TANGGAL: HANYA HARI INI s/d 30 HARI KE DEPAN
                    // ========================================================
                    val calendar = Calendar.getInstance()
                    val datePickerDialog = DatePickerDialog(this@PaymentActivity, { _, year, month, day ->
                        // Format tanggal jadi YYYY-MM-DD
                        val formatBulan = String.format("%02d", month + 1)
                        val formatHari = String.format("%02d", day)
                        addonDate = "$year-$formatBulan-$formatHari"
                        addonTime = "" // Jam sengaja dikosongkan

                        // 🟢 INSTANT UPDATE UI & KALKULATOR TOTAL
                        isAddonSelected = true
                        tvSelectedAddon.text = "$addonName\n📅 $addonDate"

                        val totalAddonBase = addonPriceBase * passengerCount
                        val totalAddonTax = totalAddonBase * 0.12

                        dynamicSubtotal = subTotal + totalAddonBase
                        dynamicTax = tax + totalAddonTax
                        dynamicGrandTotal = dynamicSubtotal + dynamicTax

                        // Tampilkan Biaya Addon di Rincian
                        layoutAddon.visibility = View.VISIBLE
                        tvLabelPayAddon.text = "Add-on: $addonName (x$passengerCount)"
                        tvPriceAddon.text = "Rp ${formatter.format(totalAddonBase + totalAddonTax)}"

                        // Perbarui Total Keseluruhan
                        findViewById<TextView>(R.id.tv_pay_subtotal).text = "Rp ${formatter.format(dynamicSubtotal)}"
                        findViewById<TextView>(R.id.tv_pay_tax).text = "Rp ${formatter.format(dynamicTax)}"
                        findViewById<TextView>(R.id.tv_pay_grand_total).text = "Rp ${formatter.format(dynamicGrandTotal)}"

                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

                    // 🔒 Batas Minimal: Hari ini (dikurangi 1 detik untuk mencegah error timezone)
                    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

                    // 🔒 Batas Maksimal: 30 Hari dari hari ini
                    val maxCalendar = Calendar.getInstance()
                    maxCalendar.add(Calendar.DAY_OF_MONTH, 30)
                    datePickerDialog.datePicker.maxDate = maxCalendar.timeInMillis

                    // Tampilkan kalender
                    datePickerDialog.show()
                }
                .show()
        }


        // =========================================================================
        // 4. METODE PEMBAYARAN
        // =========================================================================
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

        // =========================================================================
        // 5. PROSES PEMBAYARAN & PENGIRIMAN DATA KE DATABASE
        // =========================================================================
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

            val transportDetailsList = mutableListOf<TransportDetailRequest>()
            val attractionDetailsList = mutableListOf<AttractionDetailRequest>()

            // A. Susun Tiket Transportasi Atau Wisata Utama
            if (transactionType == "WISATA") {
                attractionDetailsList.add(
                    AttractionDetailRequest(
                        attraction_id = wisataId,
                        num_tickets = ticketQty,
                        subtotal = grandTotal,
                        visit_date = intent.getStringExtra("EXTRA_VISIT_DATE")
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

                // 🟢 B. SISIPKAN ADD-ON WISATA KE DALAM KERANJANG BELANJA
                if (isAddonSelected) {
                    attractionDetailsList.add(
                        AttractionDetailRequest(
                            attraction_id = addonId,
                            num_tickets = passengerCount,
                            subtotal = addonPriceBase * passengerCount,
                            visit_date = addonDate,
                            visit_time = addonTime
                        )
                    )
                }
            }

            val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
            val realUserId = sharedPref.getString("USER_ID", "") ?: ""

            // 💡 Bungkus Semua Data dengan Harga Keseluruhan (Dynamic)
            val bookingPayload = BookingRequest(
                user_id = realUserId,
                booking_code = randomCode,
                total_amount = dynamicGrandTotal,
                tax_amount = dynamicTax,
                payment_method = selectedPaymentMethod,
                transport_details = if (transportDetailsList.isNotEmpty()) transportDetailsList else null,
                attraction_details = if (attractionDetailsList.isNotEmpty()) attractionDetailsList else null
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
                                putExtra("EXTRA_GRAND_TOTAL", dynamicGrandTotal)
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
                    btnPay.text = "Bayar fSekarang"
                    Toast.makeText(this@PaymentActivity, "Koneksi Bermasalah: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}