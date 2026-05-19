package com.app.wisatago.transport
import com.google.gson.annotations.SerializedName
// Model untuk menangkap daftar kota (Dropdown "Dari" dan "Ke")
data class LocationResponse(
    @SerializedName("display_name")
    val display_name: String
)

// Model untuk menangkap hasil pencarian jadwal kereta dari database
data class TrainSchedule(
    val schedule_id: String,
    val train_name: String,
    val operator_name: String,
    val origin_city: String,
    val destination_city: String,
    val departure_time: String,
    val price: Double
)

// Tambahkan ini di bawahnya:
data class TicketResponse(
    val ticket_id: String,
    val train_name: String,
    val operator_name: String,
    val departure_time: String,
    val arrival_time: String,
    val origin: String,
    val destination: String,
    val price: Double,
    val class_type: String?, // 🟢 Wajib ditambahkan di sini
    val available_seats: Int
)

