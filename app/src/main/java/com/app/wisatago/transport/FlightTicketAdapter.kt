package com.app.wisatago.transport

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.R
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class FlightTicketAdapter(
    private var listTiket: List<FlightSchedule>,
    private val passengerCount: Int,
    private val onItemClick: (FlightSchedule) -> Unit
) : RecyclerView.Adapter<FlightTicketAdapter.FlightViewHolder>() {

    class FlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // PERHATIAN: Kita menggunakan ID dari layout item_train.xml agar tidak perlu buat XML baru
        val tvAirlineName: TextView = itemView.findViewById(R.id.tv_train_name)
        val tvFlightCode: TextView = itemView.findViewById(R.id.tv_operator_name)
        val tvDepartureTime: TextView = itemView.findViewById(R.id.tv_departure_time)
        val tvArrivalTime: TextView = itemView.findViewById(R.id.tv_arrival_time)
        val tvFlightPrice: TextView = itemView.findViewById(R.id.tv_train_price)
        val tvClassType: TextView = itemView.findViewById(R.id.tv_class_type)
        val tvSeatStatus: TextView = itemView.findViewById(R.id.tv_seat_status)
        val btnPesanItem: MaterialButton = itemView.findViewById(R.id.btn_pesan_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        // Menggunakan item_train sebagai template desain kartunya
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_train, parent, false)
        return FlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val tiket = listTiket[position]

        // Menempelkan data pesawat ke komponen UI
        holder.tvAirlineName.text = tiket.airline_name
        holder.tvFlightCode.text = tiket.flight_code
        holder.tvDepartureTime.text = tiket.departure_time
        holder.tvArrivalTime.text = tiket.arrival_time
        holder.tvClassType.text = tiket.class_type

        // Format Harga ke Rupiah
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        holder.tvFlightPrice.text = "Rp ${formatter.format(tiket.price)}"

        // Logika Pengecekan Sisa Kursi
        if (tiket.available_seats == 0 || tiket.available_seats < passengerCount) {
            holder.tvSeatStatus.text = "Habis"
            holder.tvSeatStatus.setTextColor(Color.parseColor("#F44336")) // Merah
            holder.btnPesanItem.isEnabled = false
            holder.btnPesanItem.text = "Habis"
            holder.btnPesanItem.setBackgroundColor(Color.parseColor("#E0E0E0")) // Abu-abu
        } else {
            holder.tvSeatStatus.text = "Tersedia (${tiket.available_seats})"
            holder.tvSeatStatus.setTextColor(Color.parseColor("#4CAF50")) // Hijau
            holder.btnPesanItem.isEnabled = true
            holder.btnPesanItem.text = "Pesan"
            holder.btnPesanItem.setBackgroundColor(Color.parseColor("#00A3FF")) // Biru
        }

        // Aksi saat tombol pesan ditekan
        holder.btnPesanItem.setOnClickListener {
            onItemClick(tiket)
        }
    }

    override fun getItemCount(): Int = listTiket.size

    fun updateData(newList: List<FlightSchedule>) {
        listTiket = newList
        notifyDataSetChanged()
    }
}