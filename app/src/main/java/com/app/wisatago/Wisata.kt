package com.app.wisatago

import com.google.gson.annotations.SerializedName

data class Wisata(
    @SerializedName("attraction_id") val attractionId: String,
    @SerializedName("attraction_name") val attractionName: String,
    @SerializedName("location_id") val locationId: Int,
    @SerializedName("ticket_price") val ticketPrice: Double
)