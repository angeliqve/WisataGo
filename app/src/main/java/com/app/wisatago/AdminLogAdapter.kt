package com.app.wisatago

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminLogAdapter(private var logList: List<ActivityLog>) : RecyclerView.Adapter<AdminLogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAction: TextView = view.findViewById(R.id.tvLogAction)
        val tvTime: TextView = view.findViewById(R.id.tvLogTime)
        val tvDesc: TextView = view.findViewById(R.id.tvLogDescription)
        val tvUser: TextView = view.findViewById(R.id.tvLogUser)
        val ivIcon: ImageView = view.findViewById(R.id.ivLogIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]

        holder.tvAction.text = log.action_type
        holder.tvDesc.text = log.description
        holder.tvTime.text = log.time_formatted
        holder.tvUser.text = "👤 ${log.user_name}"

        // Berikan warna teks berbeda berdasarkan jenis aksi
        when (log.action_type) {
            "BOOKING" -> holder.tvAction.setTextColor(Color.parseColor("#4CAF50")) // Hijau
            "CANCEL_BOOKING" -> holder.tvAction.setTextColor(Color.parseColor("#F44336")) // Merah
            "SIGNUP" -> holder.tvAction.setTextColor(Color.parseColor("#35A1F8")) // Biru
            "UPDATE_PROFILE", "UPDATE_PASSWORD" -> holder.tvAction.setTextColor(Color.parseColor("#FF9800")) // Oranye
            else -> holder.tvAction.setTextColor(Color.parseColor("#1E1E1E")) // Hitam Default
        }
    }

    override fun getItemCount(): Int = logList.size

    // =======================================================
    // 🟢 FUNGSI INI YANG SEBELUMNYA HILANG / BELUM ADA
    // =======================================================
    fun updateData(newData: List<ActivityLog>) {
        this.logList = newData
        notifyDataSetChanged()
    }
}