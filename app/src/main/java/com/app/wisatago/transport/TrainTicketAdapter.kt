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

class TrainTicketAdapter(
    private var listTiket: List<TicketResponse>,
    private val passengerCount: Int,
    private val onItemClick: (TicketResponse) -> Unit
) : RecyclerView.Adapter<TrainTicketAdapter.TrainViewHolder>() {

    class TrainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTrainName: TextView = itemView.findViewById(R.id.tv_train_name)
        val tvOperatorName: TextView = itemView.findViewById(R.id.tv_operator_name)
        val tvDepartureTime: TextView = itemView.findViewById(R.id.tv_departure_time)
        val tvArrivalTime: TextView = itemView.findViewById(R.id.tv_arrival_time)
        val tvTrainPrice: TextView = itemView.findViewById(R.id.tv_train_price)
        val tvClassType: TextView = itemView.findViewById(R.id.tv_class_type)

        val tvSeatStatus: TextView = itemView.findViewById(R.id.tv_seat_status)
        val tvNextDay: TextView = itemView.findViewById(R.id.tv_next_day)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration) // 🟢 Inisialisasi Durasi
        val btnPesanItem: MaterialButton = itemView.findViewById(R.id.btn_pesan_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_train, parent, false)
        return TrainViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int) {
        val tiket = listTiket[position]

        val berangkat = tiket.departure_time
        val tiba = tiket.arrival_time

        holder.tvTrainName.text = tiket.train_name
        holder.tvOperatorName.text = tiket.operator_name
        holder.tvDepartureTime.text = berangkat
        holder.tvArrivalTime.text = tiba
        holder.tvClassType.text = tiket.class_type ?: "Ekonomi"

        // 🟢 1. LOGIKA DURASI & +1 HARI
        if (tiba < berangkat) {
            holder.tvNextDay.visibility = View.VISIBLE
        } else {
            holder.tvNextDay.visibility = View.GONE
        }

        // Memanggil fungsi hitung durasi dan menampilkannya
        holder.tvDuration.text = hitungDurasi(berangkat, tiba)

        // 🟢 2. LOGIKA KETERSEDIAAN KURSI & TOMBOL
        val sisaKursi = tiket.available_seats

        if (sisaKursi == 0 || sisaKursi < passengerCount) {
            holder.tvSeatStatus.text = "Tiket Habis"
            holder.tvSeatStatus.setTextColor(Color.parseColor("#F44336")) // Merah
            holder.btnPesanItem.isEnabled = false
            holder.btnPesanItem.text = "Habis"
            holder.btnPesanItem.setBackgroundColor(Color.parseColor("#E0E0E0")) // Abu-abu
        } else if (sisaKursi <= 100) {
            holder.tvSeatStatus.text = "Sisa $sisaKursi kursi"
            holder.tvSeatStatus.setTextColor(Color.parseColor("#FF9800")) // Oranye
            holder.btnPesanItem.isEnabled = true
            holder.btnPesanItem.text = "Pesan"
            holder.btnPesanItem.setBackgroundColor(Color.parseColor("#00A3FF")) // Biru
        } else {
            holder.tvSeatStatus.text = "Tersedia"
            holder.tvSeatStatus.setTextColor(Color.parseColor("#4CAF50")) // Hijau
            holder.btnPesanItem.isEnabled = true
            holder.btnPesanItem.text = "Pesan"
            holder.btnPesanItem.setBackgroundColor(Color.parseColor("#00A3FF")) // Biru
        }

        // 🟢 3. FORMAT HARGA & AKSI KLIK
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        holder.tvTrainPrice.text = "Rp ${formatter.format(tiket.price)}"

        holder.btnPesanItem.setOnClickListener {
            onItemClick(tiket)
        }
    }

    override fun getItemCount(): Int = listTiket.size

    fun updateData(newList: List<TicketResponse>) {
        listTiket = newList
        notifyDataSetChanged()
    }

    // ==========================================
    // 🟢 FUNGSI PEMBANTU: MENGHITUNG DURASI
    // ==========================================
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

            // Jika jam tiba lebih kecil, berarti sudah ganti hari (+24 jam dalam menit)
            if (totalMenitTiba < totalMenitBerangkat) {
                totalMenitTiba += 24 * 60
            }

            val selisihMenit = totalMenitTiba - totalMenitBerangkat
            val jam = selisihMenit / 60
            val menit = selisihMenit % 60

            if (menit == 0) {
                "${jam}j"
            } else {
                "${jam}j ${menit}m"
            }
        } catch (e: Exception) {
            "-"
        }
    }
}