package com.app.wisatago
import com.google.gson.annotations.SerializedName
data class LoginRequest(
    val email: String,
    val password: String
)


data class LoginResponse(
    val message: String,
    val user_id: String,
    val role: String?,
    val full_name: String?
)

data class ProfileResponse(
    val user_id: String,
    val full_name: String,
    val email: String,
    val role: String?,
    val created_at: String?,
    val phone_number: String?,
    val profile_picture: String?
)

data class UpdateProfileRequest(
    val user_id: String,
    val full_name: String,
    val phone_number: String,
    val profile_picture: String? // Tambahan untuk foto profil
)
data class SignUpRequest(
    val full_name: String,
    val email: String,
    val password: String,
    val phone_number: String
)

data class SignUpResponse(
    val message: String
)

data class ResponseBooking(
    val message: String,
    val booking_code: String,
    val error: String? = null
)

data class HistoryResponse(
    val booking_id: String?,
    val booking_code: String,
    val booking_date: String?,
    val status: String?,
    val total_amount: String?,
    val transport_name: String?,
    val origin_city: String?,
    val destination_city: String?,

    @SerializedName("departure_time")
    val departure_time: String? = null,

    // 🟢 TAMBAHKAN INI
    @SerializedName("addon_wisata")
    val addon_wisata: String? = null,

    @SerializedName("passenger_info")
    val passenger_info: String? = null

)

data class CancelRequest(
    val booking_code: String
)

data class CancelResponse(
    val message: String?,
    val error: String?
)