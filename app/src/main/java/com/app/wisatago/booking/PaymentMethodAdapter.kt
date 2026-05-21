package com.app.wisatago.booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.R

// Data Class untuk menyimpan nama dan icon
data class PaymentMethod(val name: String, val iconResId: Int)

class PaymentMethodAdapter(
    private val paymentMethods: List<PaymentMethod>,
    private val onMethodSelected: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentMethodAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ID ini sudah disesuaikan dengan item_payment_method.xml milikmu
        val imgIcon: ImageView = itemView.findViewById(R.id.img_payment_icon)
        val tvName: TextView = itemView.findViewById(R.id.tv_payment_name)

        fun bind(paymentMethod: PaymentMethod) {
            tvName.text = paymentMethod.name
            imgIcon.setImageResource(paymentMethod.iconResId)

            // Ketika item diklik, kirim data kembali
            itemView.setOnClickListener {
                onMethodSelected(paymentMethod)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_method, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(paymentMethods[position])
    }

    override fun getItemCount(): Int = paymentMethods.size
}