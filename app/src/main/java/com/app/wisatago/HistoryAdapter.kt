package com.app.wisatago.booking

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.R
import com.app.wisatago.HistoryResponse
import com.app.wisatago.DetailPemesananActivity
import com.app.wisatago.ApiClient
import com.app.wisatago.CancelRequest
import com.app.wisatago.CancelResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class HistoryAdapter(private val listHistory: List<HistoryResponse>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTransportName: TextView = view.findViewById(R.id.tvItemTransportName)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
        val tvRoute: TextView = view.findViewById(R.id.tvItemRoute)
        val tvBookingCode: TextView = view.findViewById(R.id.tvItemBookingCode)
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
        val tvTotalAmount: TextView = view.findViewById(R.id.tvItemTotalAmount)

        // 🟢 TAMBAHAN: Mengambil ID label Rute dari XML
        val tvLabelRoute: TextView = view.findViewById(R.id.tvLabelRoute)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listHistory[position]

        // Penamaan Produk Utama / Add-on
        val mainProduct = item.transport_name ?: "Produk WisataGO"
        if (!item.addon_wisata.isNullOrEmpty() && item.booking_code.startsWith("TR-")) {
            holder.tvTransportName.text = "$mainProduct (+ ${item.addon_wisata})"
        } else {
            holder.tvTransportName.text = mainProduct
        }

        // 🟢 TAMBAHAN LOGIKA: Ubah Label Rute Menjadi Destinasi jika itu tiket Wisata
        if (item.booking_code.startsWith("WS-")) {
            holder.tvLabelRoute.text = "Destinasi Wisata"
        } else {
            holder.tvLabelRoute.text = "Rute Perjalanan"
        }

        // Tampilan Rute
        val origin = item.origin_city ?: "WisataGO"
        val destination = item.destination_city

        if (!destination.isNullOrEmpty()) {
            if (mainProduct.contains("&")) {
                holder.tvRoute.text = "$origin ⇌ $destination"
            } else {
                holder.tvRoute.text = "$origin ➔ $destination"
            }
        } else {
            holder.tvRoute.text = origin
        }

        holder.tvBookingCode.text = item.booking_code
        holder.tvDate.text = item.booking_date

        // Format Rupiah
        try {
            val rawAmount = item.total_amount.toString().toDoubleOrNull() ?: 0.0
            val amountParsed = rawAmount.toLong()

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            holder.tvTotalAmount.text = formatRupiah.format(amountParsed).replace(",00", "")
        } catch (e: Exception) {
            holder.tvTotalAmount.text = "Rp ${item.total_amount}"
        }

        // =======================================================
        // PEWARNAAN STATUS CERDAS (SUCCESS, CANCELED, ONGOING)
        // =======================================================
        val statusClean = item.status?.uppercase() ?: "PENDING"
        when (statusClean) {
            "SUCCESS" -> {
                holder.tvStatus.text = "SUCCESS"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#DCFCE7")) // Hijau
                holder.tvStatus.setTextColor(Color.parseColor("#15803D"))
            }
            "CANCELED" -> {
                holder.tvStatus.text = "CANCELED"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FEE2E2")) // Merah
                holder.tvStatus.setTextColor(Color.parseColor("#B91C1C"))
            }
            else -> {
                holder.tvStatus.text = "ONGOING"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FEF3C7")) // Kuning
                holder.tvStatus.setTextColor(Color.parseColor("#D97706"))
            }
        }

        // =======================================================
        // 1. KLIK BIASA -> Masuk ke Detail Pemesanan
        // =======================================================
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailPemesananActivity::class.java).apply {
                putExtra("BOOKING_CODE", item.booking_code)
                putExtra("TRANSPORT_NAME", item.transport_name ?: "Produk WisataGO")
                putExtra("STATUS", item.status)
                putExtra("TOTAL_AMOUNT", item.total_amount.toString())
                putExtra("ORIGIN_CITY", item.origin_city ?: "WisataGO")
                putExtra("DEST_CITY", item.destination_city ?: "")
                putExtra("DEPARTURE_TIME", item.departure_time)
                putExtra("ADDON_WISATA", item.addon_wisata)
                putExtra("PASSENGER_INFO", item.passenger_info)

                // 🟢 TAMBAHAN: Mengirimkan Data Nama Pemesan (User) ke Halaman Detail
                putExtra("FULL_NAME", item.full_name)
            }
            context.startActivity(intent)
        }

        // =======================================================
        // 2. TEKAN TAHAN (Long Click) -> Pop-up Batal Pesanan
        // =======================================================
        holder.itemView.setOnLongClickListener {

            if (statusClean == "CANCELED") {
                Toast.makeText(holder.itemView.context, "Pesanan ini sudah dibatalkan.", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }

            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Batalkan Pesanan?")
            builder.setMessage("Apakah Anda yakin ingin membatalkan pesanan ${item.booking_code}?\n\nPesanan transportasi hanya dapat dibatalkan maksimal H-1 sebelum keberangkatan.")
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            builder.setPositiveButton("Ya, Batalkan") { dialog, _ ->
                val request = CancelRequest(item.booking_code)

                Toast.makeText(holder.itemView.context, "Memproses pembatalan...", Toast.LENGTH_SHORT).show()

                ApiClient.instance.cancelBooking(request).enqueue(object : Callback<CancelResponse> {
                    override fun onResponse(call: Call<CancelResponse>, response: Response<CancelResponse>) {
                        if (response.isSuccessful && response.body()?.message != null) {
                            Toast.makeText(holder.itemView.context, response.body()?.message, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(holder.itemView.context, "Gagal: Kesalahan pada sistem.", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<CancelResponse>, t: Throwable) {
                        Toast.makeText(holder.itemView.context, "Koneksi Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
                dialog.dismiss()
            }

            builder.setNegativeButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
            true
        }
    }

    override fun getItemCount(): Int = listHistory.size
}