package com.app.wisatago

// Struktur data yang dikirim ke Node.js saat tombol masuk diklik
data class LoginRequest(
    val email: String,
    val password: String
)

// Struktur data yang diterima dari Node.js saat login berhasil
data class LoginResponse(
    val message: String,
    val user_id: String,
    val role: String?,
    val full_name: String?
)
// Struktur data yang akan dikirim ke Node.js saat mendaftar
data class SignUpRequest(
    val full_name: String,
    val email: String,
    val password: String,
    val phone_number: String
)

// Struktur data jawaban dari Node.js setelah berhasil mendaftar
data class SignUpResponse(
    val message: String
)

data class ResponseBooking(
    val message: String,
    val booking_code: String,
    val error: String? = null
)