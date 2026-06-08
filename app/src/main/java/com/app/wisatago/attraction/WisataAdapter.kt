package com.app.wisatago.attraction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.R
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
        // 💡 Tambahkan ImageView untuk gambar wisata (pastikan ID di item_wisata.xml adalah imgWisata)
        val imgWisata: ImageView = view.findViewById(R.id.imgWisata)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wisata, parent, false)
        return WisataViewHolder(view)
    }

    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        val wisata = listWisata[position]

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        holder.tvNama.text = wisata.attractionName
        holder.tvHarga.text = formatRupiah.format(wisata.ticketPrice)

        // 💡 1. Cocokkan nama wisata dari database dengan gambar di folder drawable
        val gambarLokal = when (wisata.attractionName) {
            "Dunia Fantasi (Dufan)" -> R.drawable.dufan
            "Monumen Nasional (Monas)" -> R.drawable.monas
            "Taman Mini Indonesia Indah (TMII)" -> R.drawable.tmii
            "Candi Prambanan" -> R.drawable.prambanan
            "Kebun Binatang Surabaya" -> R.drawable.kebunsurabaya
            "Monumen Kapal Selam" -> R.drawable.monkapalselam
            "Keraton Yogyakarta" -> R.drawable.keraton
            "Keraton Kasepuhan" -> R.drawable.kasepuhan
            "Goa Sunyaragi" -> R.drawable.sunyaragi
            "Garuda Wisnu Kencana (GWK)" -> R.drawable.gwk
            "Bali Zoo" -> R.drawable.balizoo

            // Gambar default jika nama wisata tidak ada di atas
            else -> R.drawable.ic_launcher_background
        }

        // 💡 2. Masukkan gambar lokal tersebut menggunakan Glide
        holder.imgWisata.setImageResource(gambarLokal)

        // 3. Fungsi saat tombol pesan diklik
        holder.btnPesan.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, WisataOrderActivity::class.java)
            intent.putExtra("EXTRA_WISATA_ID", wisata.attractionId)
            intent.putExtra("EXTRA_WISATA_NAME", wisata.attractionName)
            intent.putExtra("EXTRA_WISATA_PRICE", wisata.ticketPrice)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listWisata.size

    fun updateData(newList: List<Wisata>) {
        this.listWisata = newList
        notifyDataSetChanged()
    }
}