package com.app.wisatago

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    // Menghubungkan langsung ke app.post('/login') di Node.js
    @POST("/login")
    suspend fun prosesLoginUser(@Body requestBody: LoginRequest): Response<LoginResponse>
    // Pintu Sign Up (Tambahkan ini)
    @POST("/signup")
    suspend fun prosesDaftarUser(@Body requestBody: SignUpRequest): Response<SignUpResponse>
}