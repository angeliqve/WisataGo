package com.app.wisatago // Sesuaikan jika Anda menaruhnya di dalam folder khusus seperti com.app.wisatago.models

import com.google.gson.annotations.SerializedName

data class AdminStatsResponse(
    val success: Boolean,
    val data: AdminStatsData?
)

data class AdminStatsData(
    val ringkasan: StatsRingkasan,
    val status: List<StatsStatus>,
    val bulanan: List<StatsBulanan>,
    val populer: List<StatsPopuler>
)

data class StatsRingkasan(
    @SerializedName("total_transaksi") val totalTransaksi: String?,
    @SerializedName("total_pendapatan") val totalPendapatan: String?,
    @SerializedName("pemesanan_baru") val pemesananBaru: String?,
    @SerializedName("pendapatan_hari_ini") val pendapatanHariIni: String?
)

data class StatsStatus(val status: String, val jumlah: String)
data class StatsBulanan(val bulan: String, val total: String)
data class StatsPopuler(val produk: String, val jumlah: String)