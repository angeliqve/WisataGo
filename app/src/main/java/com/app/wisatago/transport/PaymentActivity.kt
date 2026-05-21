package com.app.wisatago.transport

import android.os.Bundle
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
        val pergiName = intent.getStringExtra("EXTRA_PERGI_NAME") ?: "Nama Armada"
        val origin = intent.getStringExtra("EXTRA_ORIGIN") ?: ""
        val destination = intent.getStringExtra("EXTRA_DESTINATION") ?: ""
        val passengerCount = intent.getIntExtra("EXTRA_PASSENGERS", 1)
        val isReturnTrip = intent.getBooleanExtra("EXTRA_IS_RETURN_TRIP", false)

        val pricePergiTotal = intent.getDoubleExtra("EXTRA_PRICE_PERGI_TOTAL", 0.0)
        val subTotal = intent.getDoubleExtra("EXTRA_SUBTOTAL", 0.0)
        val tax = intent.getDoubleExtra("EXTRA_TAX", 0.0)
        val grandTotal = intent.getDoubleExtra("EXTRA_GRAND_TOTAL", 0.0)

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

        // 2. SET DATA KE TAMPILAN
        val tvTransportName = findViewById<TextView>(R.id.tv_pay_transport_name)
        val tvRouteSummary = findViewById<TextView>(R.id.tv_pay_route_summary)
        val layoutPulang = findViewById<RelativeLayout>(R.id.layout_pay_pulang)

        findViewById<TextView>(R.id.tv_label_pay_pergi).text = "Tiket Pergi (x$passengerCount)"
        findViewById<TextView>(R.id.tv_pay_price_pergi).text = "Rp ${formatter.format(pricePergiTotal)}"

        if (isReturnTrip) {
            val pulangName = intent.getStringExtra("EXTRA_PULANG_NAME") ?: ""
            val pricePulangTotal = intent.getDoubleExtra("EXTRA_PRICE_PULANG_TOTAL", 0.0)

            tvTransportName.text = "$pergiName & $pulangName"
            tvRouteSummary.text = "$origin ➔ $destination\nPulang Pergi • $passengerCount Penumpang"

            layoutPulang.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_label_pay_pulang).text = "Tiket Pulang (x$passengerCount)"
            findViewById<TextView>(R.id.tv_pay_price_pulang).text = "Rp ${formatter.format(pricePulangTotal)}"
        } else {
            tvTransportName.text = pergiName
            tvRouteSummary.text = "$origin ➔ $destination\nSekali Jalan • $passengerCount Penumpang"
            layoutPulang.visibility = View.GONE
        }

        findViewById<TextView>(R.id.tv_pay_subtotal).text = "Rp ${formatter.format(subTotal)}"
        findViewById<TextView>(R.id.tv_pay_tax).text = "Rp ${formatter.format(tax)}"
        findViewById<TextView>(R.id.tv_pay_grand_total).text = "Rp ${formatter.format(grandTotal)}"

        // ==========================================
        // 3. LOGIKA BOTTOM SHEET METODE PEMBAYARAN
        // ==========================================
        val cardSelectPayment = findViewById<View>(R.id.card_select_payment)
        val tvPaymentMethodName = findViewById<TextView>(R.id.tv_payment_method_name)
        val tvPaymentAccountNumber = findViewById<TextView>(R.id.tv_payment_account_number)
        val imgPaymentIcon = findViewById<ImageView>(R.id.img_payment_method_icon)

        // 🟢 Daftar Bank dan E-Wallet (Sudah diganti menggunakan logo buatan sendiri di folder drawable)
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

                // 🟢 Generate Nomor Rekening / VA Acak yang disesuaikan dengan Bank
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
                // Ubah warna text jadi biru tua agar terlihat seperti nomor rekening yang bisa disalin
                tvPaymentAccountNumber.setTextColor(resources.getColor(android.R.color.holo_blue_dark))

                bottomSheetDialog.dismiss()
            }

            rvPaymentMethods.adapter = adapter
            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        // ==========================================
        // 4. AKSI TOMBOL BAYAR
        // ==========================================
        findViewById<ImageButton>(R.id.btn_back_payment).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btn_finish_payment).setOnClickListener {
            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(this, "Silakan pilih metode pembayaran terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Memproses pembayaran Rp ${formatter.format(grandTotal)} via $selectedPaymentMethod...", Toast.LENGTH_LONG).show()
                // TODO: Arahkan ke halaman Sukses/E-Tiket
            }
        }
    }
}