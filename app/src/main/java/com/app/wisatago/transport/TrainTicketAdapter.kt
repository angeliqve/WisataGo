package com.app.wisatago.transport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.R
import java.text.NumberFormat
import java.util.Locale

class TrainTicketAdapter(
    private var listTiket: List<TicketResponse>,
    private val onItemClick: (TicketResponse) -> Unit
) : RecyclerView.Adapter<TrainTicketAdapter.TrainViewHolder>() {

    class TrainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTrainName: TextView = itemView.findViewById(R.id.tv_train_name)
        val tvOperatorName: TextView = itemView.findViewById(R.id.tv_operator_name)
        val tvDepartureTime: TextView = itemView.findViewById(R.id.tv_departure_time)
        val tvArrivalTime: TextView = itemView.findViewById(R.id.tv_arrival_time)
        val tvTrainPrice: TextView = itemView.findViewById(R.id.tv_train_price)
        val btnPesanItem: View = itemView.findViewById(R.id.btn_pesan_item)

        // 🟢 Inisialisasi TextView kelas dari item_train.xml
        val tvClassType: TextView = itemView.findViewById(R.id.tv_class_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_train, parent, false)
        return TrainViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int) {
        val tiket = listTiket[position]
        holder.tvTrainName.text = tiket.train_name
        holder.tvOperatorName.text = tiket.operator_name
        holder.tvDepartureTime.text = tiket.departure_time
        holder.tvArrivalTime.text = tiket.arrival_time

        // 🟢 Tempelkan data class_type ke TextView
        holder.tvClassType.text = tiket.class_type ?: "Ekonomi"

        // Menggunakan NumberFormat yang sudah di-import
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
}