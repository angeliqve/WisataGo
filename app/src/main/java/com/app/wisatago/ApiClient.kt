package com.app.wisatago

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Jika IP laptop server berubah, CUKUP GANTI DI SINI SATU KALI SAJA
    private const val BASE_URL = "http://10.0.2.2:3000"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Mengurus konversi JSON otomatis
            .build()
            .create(ApiService::class.java)
    }
}