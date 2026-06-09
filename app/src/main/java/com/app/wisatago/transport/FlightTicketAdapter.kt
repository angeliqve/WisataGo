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
        val tvAirlineName: TextView = itemView.findViewById(R.id.tv_train_name)
        val tvFlightCode: TextView = itemView.findViewById(R.id.tv_operator_name)
        val tvDepartureTime: TextView = itemView.findViewById(R.id.tv_departure_time)
        val tvArrivalTime: TextView = itemView.findViewById(R.id.tv_arrival_time)
        val tvFlightPrice: TextView = itemView.findViewById(R.id.tv_train_price)
        val tvClassType: TextView = itemView.findViewById(R.id.tv_class_type)
        val tvSeatStatus: TextView = itemView.findViewById(R.id.tv_seat_status)

        // 🟢 Tambahkan inisialisasi Durasi dan Next Day
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        val tvNextDay: TextView = itemView.findViewById(R.id.tv_next_day)
        val btnPesanItem: MaterialButton = itemView.findViewById(R.id.btn_pesan_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_train, parent, false)
        return FlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val tiket = listTiket[position]

        // Ekstrak jam saja (misal "14:00") jika data dari DB berupa "2026-06-15 14:00:00"
        val berangkat = formatJam(tiket.departure_time)
        val tiba = formatJam(tiket.arrival_time)

        holder.tvAirlineName.text = tiket.airline_name
        holder.tvFlightCode.text = tiket.flight_code
        holder.tvDepartureTime.text = berangkat
        holder.tvArrivalTime.text = tiba
        holder.tvClassType.text = tiket.class_type

        // 🟢 LOGIKA DURASI & +1 HARI
        if (tiba < berangkat) {
            holder.tvNextDay.visibility = View.VISIBLE
        } else {
            holder.tvNextDay.visibility = View.GONE
        }

        // Panggil fungsi hitung durasi
        holder.tvDuration.text = hitungDurasi(berangkat, tiba)

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

    // ==========================================
    // 🟢 FUNGSI PEMBANTU
    // ==========================================

    // Memastikan format yang dihitung hanya "HH:mm"
    private fun formatJam(waktu: String): String {
        return try {
            if (waktu.contains(" ")) {
                waktu.split(" ")[1].substring(0, 5) // Memotong "2026-06-15 14:00:00" jadi "14:00"
            } else if (waktu.length >= 5) {
                waktu.substring(0, 5) // Memotong "14:00:00" jadi "14:00"
            } else {
                waktu
            }
        } catch (e: Exception) {
            waktu
        }
    }

    // Menghitung selisih jam dan menit
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

            // Jika jam tiba lebih kecil dari berangkat (ganti hari)
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