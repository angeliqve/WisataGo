package com.app.wisatago.booking

// 1. Data Induk Pemesanan
data class BookingRequest(
    val user_id: String,
    val booking_code: String,
    val total_amount: Double,
    val tax_amount: Double,
    val payment_method: String,
    val transport_details: List<TransportDetailRequest>?,
    val attraction_details: List<AttractionDetailRequest>? = null // 💡 Ini yang sebelumnya belum ada
)

// 2. Data Rincian Transportasi
data class TransportDetailRequest(
    val schedule_id: String,
    val num_seats: Int,
    val subtotal: Double,
    val passengers: List<PassengerRequest>
)

// 3. Data Penumpang Transportasi
data class PassengerRequest(
    val passenger_name: String,
    val seat_number: String
)

// 4. Data Rincian Wisata (Tabel Booking_Detail_Attraction)
data class AttractionDetailRequest(
    val attraction_id: String,
    val num_tickets: Int,
    val subtotal: Double,
    val visit_date: String? = null, // 🟢 Tambahan baru
    val visit_time: String? = null  // 🟢 Tambahan baru
)

data class ResponseBooking(
    val message: String,
    val booking_code: String,
    val error: String? = null
)

data class Seat(
    val id: String,
    var isSelected: Boolean = false,
    var isBooked: Boolean = false,
    val type: SeatType
)

// 🟢 TAMBAHKAN ROW_LABEL DI SINI
enum class SeatType { SEAT, AISLE, ROW_LABEL }