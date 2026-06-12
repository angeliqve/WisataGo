package com.app.wisatago.api

import com.app.wisatago.admin.AdminStatsResponse
import com.app.wisatago.attraction.Wisata
import com.app.wisatago.booking.BookingRequest
import com.app.wisatago.booking.ResponseBooking
import com.app.wisatago.transport.BusSchedule
import com.app.wisatago.transport.FlightSchedule
import com.app.wisatago.transport.LocationResponse
import com.app.wisatago.transport.TicketResponse
import com.app.wisatago.transport.TrainSchedule
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import com.app.wisatago.admin.ActivityLogResponse

interface ApiService {
    // Menghubungkan langsung ke app.post('/login') di Node.js
    @POST("/login")
    suspend fun prosesLoginUser(@Body requestBody: LoginRequest): Response<LoginResponse>

    // Pintu Sign Up
    @POST("/signup")
    suspend fun prosesDaftarUser(@Body requestBody: SignUpRequest): Response<SignUpResponse>

    // Jalur mengambil list data wisata dari database via Node.js
    @GET("/attractions")
    suspend fun getAttractions(): Response<List<Wisata>>

    // Rute untuk mengambil daftar lokasi (stasiun/bandara/terminal)
    @GET("/locations")
    fun getLocations(
        @Query("type") type: String
    ): Call<List<LocationResponse>>

    // Rute untuk mencari jadwal kereta berdasarkan query
    @GET("/search-trains")
    fun searchTrains(
        @Query("origin") origin: String,
        @Query("destination") destination: String
    ): Call<List<TrainSchedule>>

    // Rute untuk mencari tiket KERETA berdasarkan Asal, Tujuan, dan Tanggal
    @GET("/search-tickets")
    fun searchTickets(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("date") date: String
    ): Call<List<TicketResponse>>

    // Rute untuk mencari jadwal BUS berdasarkan Asal, Tujuan, dan Tanggal
    @GET("/bus/schedules")
    fun searchBusSchedules(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("date") date: String
    ): Call<List<BusSchedule>>

    // Mencari jadwal PESAWAT berdasarkan Asal, Tujuan, dan Tanggal
    @GET("/flight/schedules")
    fun searchFlightSchedules(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("date") date: String
    ): Call<List<FlightSchedule>>

    // Rute untuk mendapatkan detail jadwal BUS berdasarkan schedule_id
    @GET("/bus/detail")
    fun getBusDetail(
        @Query("schedule_id") scheduleId: String
    ): Call<BusSchedule>

    // 🟢 Rute untuk membuat pemesanan baru
    @POST("/create-booking")
    fun createBooking(@Body request: BookingRequest): Call<ResponseBooking>

    @GET("/profile")
    suspend fun getProfile(
        @Query("user_id") userId: String
    ): Response<ProfileResponse>

    @POST("/update-password")
    fun updatePassword(
        @Body request: Map<String, String>
    ): Call<ResponseBody>

    @PUT("/update-profile")
    fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Call<ProfileResponse>
    @GET("/booking-history")
    fun getBookingHistory(
        @Query("user_id") userId: String
    ): Call<List<HistoryResponse>>

    @GET("api/admin/dashboard-stats")
    suspend fun getAdminDashboardStats(): Response<AdminStatsResponse>
    @GET("api/admin/bookings")
    suspend fun getAllBookingsAdmin(): Response<List<HistoryResponse>>
    @POST("cancel-booking")
    fun cancelBooking(@Body request: CancelRequest): Call<CancelResponse>
    @GET("com/app/wisatago/admin/dashboard-stats")
    fun getAdminStats(): Call<AdminStatsResponse>

    @GET("com/app/wisatago/admin/activity-logs")
    fun getActivityLogs(): Call<ActivityLogResponse>
}