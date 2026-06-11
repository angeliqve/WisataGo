package com.app.wisatago

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.wisatago.api.ApiClient
import com.app.wisatago.api.SignUpRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val etNama = findViewById<EditText>(R.id.etNamaDaftar)
        val etEmail = findViewById<EditText>(R.id.etEmailDaftar)
        val etPassword = findViewById<EditText>(R.id.etPasswordDaftar)
        val etNoTelepon = findViewById<EditText>(R.id.etNoTeleponDaftar)

        val btnDaftar = findViewById<LinearLayout>(R.id.btnTombolDaftar)
        val tvSudahPunyaAkun = findViewById<TextView>(R.id.tvSudahPunyaAkun)

        // Default selalu diawali 08
        etNoTelepon.setText("08")
        etNoTelepon.setSelection(etNoTelepon.text.length)

        etNoTelepon.addTextChangedListener(object : TextWatcher {

            private var isUpdating = false

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {

                if (isUpdating) return

                isUpdating = true

                var input = s.toString()

                // Hapus karakter selain angka
                input = input.replace(Regex("[^0-9]"), "")

                // Tidak boleh menghapus awalan 08
                if (input.isEmpty()) {
                    input = "08"
                }

                if (input.length == 1) {
                    input = "08"
                }

                if (!input.startsWith("08")) {
                    input = "08"
                }

                // Maksimal 12 digit
                if (input.length > 12) {
                    input = input.substring(0, 12)
                }

                if (input != etNoTelepon.text.toString()) {
                    etNoTelepon.setText(input)
                    etNoTelepon.setSelection(input.length)
                }

                isUpdating = false
            }
        })

        btnDaftar.setOnClickListener {

            val namaInput = etNama.text.toString().trim()
            val emailInput = etEmail.text.toString().trim()
            val passwordInput = etPassword.text.toString().trim()
            val nomorTeleponInput = etNoTelepon.text.toString().trim()

            if (
                namaInput.isEmpty() ||
                emailInput.isEmpty() ||
                passwordInput.isEmpty() ||
                nomorTeleponInput.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Semua kolom wajib diisi!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val hasilValidasi =
                validasiNomorTelepon(nomorTeleponInput)

            if (!hasilValidasi.isValid) {

                Toast.makeText(
                    this,
                    hasilValidasi.pesan,
                    Toast.LENGTH_LONG
                ).show()

                // Reset hanya field nomor telepon
                etNoTelepon.setText("08")
                etNoTelepon.setSelection(etNoTelepon.text.length)
                etNoTelepon.requestFocus()

                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Mendaftarkan akun...",
                Toast.LENGTH_SHORT
            ).show()

            jalankanFungsiSignUp(
                namaInput,
                emailInput,
                passwordInput,
                nomorTeleponInput
            )
        }

        tvSudahPunyaAkun.setOnClickListener {
            finish()
        }
    }

    private fun validasiNomorTelepon(
        nomorTelepon: String
    ): ValidasiTeleponResult {

        if (!nomorTelepon.startsWith("08")) {
            return ValidasiTeleponResult(
                false,
                "Nomor telepon harus diawali 08"
            )
        }

        if (nomorTelepon.length != 12) {
            return ValidasiTeleponResult(
                false,
                "Nomor telepon harus tepat 12 digit"
            )
        }

        return ValidasiTeleponResult(
            true,
            "Nomor telepon valid"
        )
    }

    data class ValidasiTeleponResult(
        val isValid: Boolean,
        val pesan: String
    )

    private fun jalankanFungsiSignUp(
        nama: String,
        email: String,
        pass: String,
        phone: String
    ) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val request =
                    SignUpRequest(
                        nama,
                        email,
                        pass,
                        phone
                    )

                val response =
                    ApiClient.instance.prosesDaftarUser(request)

                withContext(Dispatchers.Main) {

                    if (
                        response.isSuccessful &&
                        response.body() != null
                    ) {

                        Toast.makeText(
                            this@SignUp,
                            "Pendaftaran Berhasil! Silakan Login",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()

                    } else {

                        Toast.makeText(
                            this@SignUp,
                            "Gagal Daftar! Email atau nama mungkin sudah digunakan.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(
                        this@SignUp,
                        "Gagal terhubung ke server backend",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}