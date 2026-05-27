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

class BusTicketAdapter(
    private var listTiket: List<BusSchedule>,
    private val passengerCount: Int,
    private val onItemClick: (BusSchedule) -> Unit
) : RecyclerView.Adapter<BusTicketAdapter.BusViewHolder>() {

    class BusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBusName: TextView = itemView.findViewById(R.id.tv_bus_name)
        val tvCompanyName: TextView = itemView.findViewById(R.id.tv_company_name)
        val tvDepartureTime: TextView = itemView.findViewById(R.id.tv_departure_time)
        val tvArrivalTime: TextView = itemView.findViewById(R.id.tv_arrival_time)
        val tvBusPrice: TextView = itemView.findViewById(R.id.tv_bus_price)
        val tvClassType: TextView = itemView.findViewById(R.id.tv_class_type)
        val tvSeatStatus: TextView = itemView.findViewById(R.id.tv_seat_status)
        val tvNextDay: TextView = itemView.findViewById(R.id.tv_next_day)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        val btnPesanItem: MaterialButton = itemView.findViewById(R.id.btn_pesan_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus, parent, false)
        return BusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val tiket = listTiket[position]

        val berangkat = tiket.departure_time
        val tiba = tiket.arrival_time

        holder.tvBusName.text = tiket.company_name
        holder.tvCompanyName.text = tiket.company_name
        holder.tvDepartureTime.text = berangkat
        holder.tvArrivalTime.text = tiba
        holder.tvClassType.text = tiket.class_type

        if (tiba < berangkat) {
            holder.tvNextDay.visibility = View.VISIBLE
        } else {
            holder.tvNextDay.visibility = View.GONE
        }

        holder.tvDuration.text = hitungDurasi(berangkat, tiba)

        val sisaKursi = tiket.available_seats

        when {
            sisaKursi == 0 || sisaKursi < passengerCount -> {
                holder.tvSeatStatus.text = "Tiket Habis"
                holder.tvSeatStatus.setTextColor(Color.parseColor("#F44336"))
                holder.btnPesanItem.isEnabled = false
                holder.btnPesanItem.text = "Habis"
                holder.btnPesanItem.setBackgroundColor(Color.parseColor("#E0E0E0"))
            }
            sisaKursi <= 20 -> {
                holder.tvSeatStatus.text = "Sisa $sisaKursi kursi"
                holder.tvSeatStatus.setTextColor(Color.parseColor("#FF9800"))
                holder.btnPesanItem.isEnabled = true
                holder.btnPesanItem.text = "Pesan"
                holder.btnPesanItem.setBackgroundColor(Color.parseColor("#00A3FF"))
            }
            else -> {
                holder.tvSeatStatus.text = "Tersedia"
                holder.tvSeatStatus.setTextColor(Color.parseColor("#4CAF50"))
                holder.btnPesanItem.isEnabled = true
                holder.btnPesanItem.text = "Pesan"
                holder.btnPesanItem.setBackgroundColor(Color.parseColor("#00A3FF"))
            }
        }

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        holder.tvBusPrice.text = "Rp ${formatter.format(tiket.price)}"

        holder.btnPesanItem.setOnClickListener {
            onItemClick(tiket)
        }
    }

    override fun getItemCount(): Int = listTiket.size

    fun updateData(newList: List<BusSchedule>) {
        listTiket = newList
        notifyDataSetChanged()
    }

    private fun hitungDurasi(berangkat: String, tiba: String): String {
        return try {
            val depParts = berangkat.split(":")
            val arrParts = tiba.split(":")

            val depJam = depParts[0].toInt()
            val depMenit = depParts[1].toInt()
            val arrJam = arrParts[0].toInt()
            val arrMenit = arrParts[1].toInt()

            var totalMenitBerangkat = (depJam * 60) + depMenit
            var totalMenitTiba = (arrJam * 60) + arrMenit

            if (totalMenitTiba < totalMenitBerangkat) {
                totalMenitTiba += 24 * 60
            }

            val selisihMenit = totalMenitTiba - totalMenitBerangkat
            val jam = selisihMenit / 60
            val menit = selisihMenit % 60

            when {
                menit == 0 -> "${jam}j"
                jam == 0 -> "${menit}m"
                else -> "${jam}j ${menit}m"
            }
        } catch (e: Exception) {
            "-"
        }
    }
}