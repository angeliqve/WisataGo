package com.app.wisatago.booking

// 1. Data Induk Pemesanan
data class BookingRequest(
    val user_id: String,           // Didapat dari session login
    val booking_code: String,      // Contoh: WGO-987654
    val total_amount: Double,      // Total Harga + Pajak
    val tax_amount: Double,        // Total Pajak
    val payment_method: String,    // BCA Virtual Account, dll
    val transport_details: List<TransportDetailRequest> // Bisa 1 (Pergi) atau 2 (Pergi & Pulang)
)

// 2. Data Rincian Transportasi (Tabel BOOKING_DETAIL_TRANSPORT)
data class TransportDetailRequest(
    val schedule_id: String,       // ID jadwal tiket dari API (bukan cuma nama keretanya)
    val num_seats: Int,            // Jumlah Penumpang
    val subtotal: Double,          // Harga satuan x num_seats
    val passengers: List<PassengerRequest> // Daftar penumpangnya
)

// 3. Data Penumpang (Tabel TRANSPORT_PASSENGERS)
data class PassengerRequest(
    val passenger_name: String,    // Contoh: Ahmad Billal
    val seat_number: String        // Contoh: 14A
)

// Tambahkan ini di bagian paling bawah file:
data class ResponseBooking(
    val message: String,
    val booking_code: String,
    val error: String? = null
)