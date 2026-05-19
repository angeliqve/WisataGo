package com.app.wisatago.attraction

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.wisatago.ApiClient
import com.app.wisatago.R
import com.app.wisatago.attraction.WisataAdapter
import kotlinx.coroutines.launch

class WisataActivity : AppCompatActivity() {

    private lateinit var rvWisata: RecyclerView
    private lateinit var adapter: WisataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wisata)

        // Tombol Back Toolbar kembali ke Halaman Dashboard
        findViewById<ImageView>(R.id.btnBackWisata).setOnClickListener {
            finish()
        }

        rvWisata = findViewById(R.id.rvWisata)
        rvWisata.layoutManager = LinearLayoutManager(this)

        // Inisialisasi awal dengan list kosong agar RecyclerView tidak skip layout saat memuat halaman
        adapter = WisataAdapter(emptyList()) { wisataTerpilih ->
            Toast.makeText(
                this,
                "Membuka pemesanan untuk: ${wisataTerpilih.attractionName}",
                Toast.LENGTH_SHORT
            ).show()
        }
        rvWisata.adapter = adapter

        // Jalankan pengambilan data dari cloud database via Node.js
        ambilDataWisataDariBackend()
    }

    private fun ambilDataWisataDariBackend() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getAttractions()
                if (response.isSuccessful && response.body() != null) {
                    val dataDariDatabase = response.body()!!
                    adapter.updateData(dataDariDatabase)
                } else {
                    val statusCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@WisataActivity, "Gagal ($statusCode): $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@WisataActivity, "Eror Koneksi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}