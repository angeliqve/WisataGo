package com.app.wisatago

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET // 🟢 Wajib import GET
import retrofit2.http.POST

interface ApiService {
    // Menghubungkan langsung ke app.post('/login') di Node.js
    @POST("/login")
    suspend fun prosesLoginUser(@Body requestBody: LoginRequest): Response<LoginResponse>

    // Pintu Sign Up
    @POST("/signup")
    suspend fun prosesDaftarUser(@Body requestBody: SignUpRequest): Response<SignUpResponse>

    // 🟢 TAMBAHKAN INI: Jalur mengambil list data wisata dari database via Node.js
    @GET("/attractions")
    suspend fun getAttractions(): Response<List<Wisata>>
}