package com.app.wisatago

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class WisataAdapter(
    private var listWisata: List<Wisata>,
    private val onPesanClick: (Wisata) -> Unit
) : RecyclerView.Adapter<WisataAdapter.WisataViewHolder>() {

    class WisataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaWisata)
        val tvHarga: TextView = view.findViewById(R.id.tvHarga)
        val btnPesan: Button = view.findViewById(R.id.btnPesanWisata)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wisata, parent, false)
        return WisataViewHolder(view)
    }

    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        val wisata = listWisata[position]

        // Mengubah Double angka mentah dari database menjadi IDR Rupiah rapi
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        holder.tvNama.text = wisata.attractionName
        holder.tvHarga.text = formatRupiah.format(wisata.ticketPrice)

        // Deteksi klik pada tombol Pesan
        holder.btnPesan.setOnClickListener { onPesanClick(wisata) }
    }

    override fun getItemCount(): Int = listWisata.size

    fun updateData(newList: List<Wisata>) {
        this.listWisata = newList
        notifyDataSetChanged()
    }
}