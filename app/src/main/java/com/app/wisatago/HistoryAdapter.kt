package com.app.wisatago.booking

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.R
import com.app.wisatago.HistoryResponse
import com.app.wisatago.DetailPemesananActivity
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listHistory[position]

        holder.tvTransportName.text = item.transport_name ?: "Produk WisataGO"

        val origin = item.origin_city ?: "WisataGO"
        val destination = item.destination_city

        if (!destination.isNullOrEmpty()) {
            holder.tvRoute.text = "$origin ➔ $destination"
        } else {
            holder.tvRoute.text = origin
        }

        holder.tvBookingCode.text = item.booking_code
        holder.tvDate.text = item.booking_date

        try {
            val rawAmount = item.total_amount.toString().toDoubleOrNull() ?: 0.0
            val amountParsed = rawAmount.toLong()

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            holder.tvTotalAmount.text = formatRupiah.format(amountParsed).replace(",00", "")
        } catch (e: Exception) {
            holder.tvTotalAmount.text = "Rp ${item.total_amount}"
        }

        val statusClean = item.status?.uppercase() ?: "PENDING"
        if (statusClean == "SUCCESS") {
            holder.tvStatus.text = "SUCCESS"
            holder.tvStatus.setBackgroundColor(Color.parseColor("#DCFCE7"))
            holder.tvStatus.setTextColor(Color.parseColor("#15803D"))
        } else {
            holder.tvStatus.text = "ONGOING"
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FEF3C7"))
            holder.tvStatus.setTextColor(Color.parseColor("#D97706"))
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailPemesananActivity::class.java).apply {
                putExtra("BOOKING_CODE", item.booking_code)
                putExtra("TRANSPORT_NAME", item.transport_name ?: "Produk WisataGO")
                putExtra("STATUS", item.status)
                putExtra("TOTAL_AMOUNT", item.total_amount.toString())
                putExtra("ORIGIN_CITY", item.origin_city ?: "WisataGO")
                putExtra("DEST_CITY", item.destination_city ?: "")
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listHistory.size
}