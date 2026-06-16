package com.app.wisatago.admin // Sesuaikan jika Anda menaruhnya di dalam folder khusus

import com.google.gson.annotations.SerializedName

data class AdminStatsResponse(
    val success: Boolean,
    val data: AdminStatsData?
)

data class AdminStatsData(
    val ringkasan: StatsRingkasan,
    val status: List<StatsStatus>,
    val bulanan: List<StatsBulanan>,
    val populer: List<StatsPopuler>,

    // 🟢 TAMBAHAN 3 WADAH BARU UNTUK CHART
    @SerializedName("transport") val transport: List<StatsKategori>? = null,
    @SerializedName("daerah") val daerah: List<StatsDaerahResponse>? = null,
    @SerializedName("wisata") val wisata: List<StatsKategori>? = null
)

data class StatsRingkasan(
    @SerializedName("total_transaksi") val totalTransaksi: String?,
    @SerializedName("total_pendapatan") val totalPendapatan: String?,
    @SerializedName("pemesanan_baru") val pemesananBaru: String?,
    @SerializedName("pendapatan_hari_ini") val pendapatanHariIni: String?,
    val total_kereta: String?,
    val total_bus: String?,
    val total_pesawat: String?,
    val total_wisata: String?
)

data class StatsStatus(val status: String, val jumlah: String)
data class StatsBulanan(val bulan: String, val total: String)
data class StatsPopuler(val produk: String, val jumlah: String)

// ==========================================
// 🟢 MODEL DATA BARU UNTUK CHART KATEGORI
// ==========================================

data class StatsKategori(
    @SerializedName("kategori") val kategori: String,
    @SerializedName("jumlah") val jumlah: String
)

data class StatsDaerahResponse(
    @SerializedName("daerah") val daerah: String,
    @SerializedName("jumlah") val jumlah: String
)

data class ActivityLogResponse(
    val success: Boolean,
    val data: List<ActivityLog>
)

data class ActivityLog(
    val log_id: Int,
    val action_type: String,
    val description: String,
    val time_formatted: String,
    val user_name: String
)