package com.app.wisatago.booking

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.R

class SeatAdapter(
    private val seatList: List<Seat>,
    private val maxSelection: Int,
    private val onSeatSelected: (List<Seat>) -> Unit
) : RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {

    class SeatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvSeat: CardView = view.findViewById(R.id.cvSeat)
        val tvSeatNumber: TextView = view.findViewById(R.id.tvSeatNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seat, parent, false)
        return SeatViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val seat = seatList[position]

        // 1. LOGIKA LORONG KOSONG (AISLE)
        if (seat.type == SeatType.AISLE) {
            holder.cvSeat.visibility = View.INVISIBLE
            holder.cvSeat.setOnClickListener(null)
            return
        }

        // 2. LOGIKA NOMOR BARIS (Angka biru di sebelah kiri)
        if (seat.type == SeatType.ROW_LABEL) {
            holder.cvSeat.visibility = View.VISIBLE
            holder.cvSeat.setCardBackgroundColor(Color.TRANSPARENT)
            holder.cvSeat.cardElevation = 0f // Hilangkan bayangan agar tidak terlihat seperti tombol
            holder.tvSeatNumber.text = seat.id
            holder.tvSeatNumber.setTextColor(Color.parseColor("#1C69A4")) // Biru KAI
            holder.cvSeat.setOnClickListener(null)
            return
        }

        // 3. LOGIKA WARNA & STATUS KURSI
        holder.cvSeat.visibility = View.VISIBLE
        holder.cvSeat.cardElevation = 2f

        when {
            seat.isBooked -> {
                // Terisi: Cokelat KAI, teks disembunyikan
                holder.cvSeat.setCardBackgroundColor(Color.parseColor("#C35914"))
                holder.tvSeatNumber.text = ""
            }
            seat.isSelected -> {
                // Dipilih: Biru KAI, tampilkan teks ID (Hanya ambil hurufnya, misal "1A" jadi "A")
                holder.cvSeat.setCardBackgroundColor(Color.parseColor("#1C69A4"))
                holder.tvSeatNumber.text = seat.id.last().toString()
                holder.tvSeatNumber.setTextColor(Color.parseColor("#FFFFFF"))
            }
            else -> {
                // Tersedia: Abu-abu KAI, teks disembunyikan
                holder.cvSeat.setCardBackgroundColor(Color.parseColor("#E5E5E5"))
                holder.tvSeatNumber.text = ""
            }
        }

        // 4. LOGIKA KLIK KURSI
        holder.cvSeat.setOnClickListener {
            if (seat.isBooked) {
                Toast.makeText(holder.itemView.context, "Kursi sudah terisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentlySelected = seatList.count { it.isSelected }

            if (seat.isSelected) {
                // Batalkan pilihan
                seat.isSelected = false
            } else {
                // Pilih kursi baru
                if (currentlySelected >= maxSelection) {
                    Toast.makeText(holder.itemView.context, "Maksimal pilih $maxSelection kursi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                seat.isSelected = true
            }

            // Perbarui tampilan kotak ini saja
            notifyItemChanged(position)

            // Kirim data ke Activity
            onSeatSelected(seatList.filter { it.isSelected })
        }
    }

    override fun getItemCount(): Int = seatList.size
}