package com.amelia.asesmengenap.banksampah.Activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amelia.asesmengenap.banksampah.Domain.ModelPengguna
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.databinding.ActivityDaftarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DaftarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaftarBinding
    private lateinit var firebaseAuth: FirebaseAuth
    var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val passwordEditText = binding.signupPassword

        passwordEditText.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                // Cek apakah ikon mata di sisi kanan disentuh
                if (event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[2].bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        // Tampilkan password
                        passwordEditText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.baseline_lock_reset_24, 0,
                            R.drawable.baseline_visibility_24, 0
                        )
                    } else {
                        // Sembunyikan password
                        passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.baseline_lock_reset_24, 0,
                            R.drawable.baseline_visibility_24, 0
                        )
                    }
                    // Memindahkan cursor ke akhir teks
                    passwordEditText.text?.let { passwordEditText.setSelection(it.length) }
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }

        binding.signupButton.setOnClickListener {
            val username = binding.signupUsn.text.toString()
            val email = binding.signupEmail.text.toString()
            val password = binding.signupPassword.text.toString()
            val alamat = binding.signupAlamat.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && alamat.isNotEmpty()) {
                // Buat ProgressDialog
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Menyesuaikan alamat anda....")
                progressDialog.setCancelable(false)
                progressDialog.show()

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid

                            // Membuat objek ModelUser dengan password
                            val user = ModelPengguna(userId!!, username, alamat, password)

                            // Simpan data pengguna ke Realtime Database
                            val database = FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            val userRef = database.getReference("Pengguna")
                            userRef.child(userId).setValue(user)
                                .addOnSuccessListener {
                                    // Kirim email verifikasi (hanya jika akun berhasil dibuat)
                                    val user = FirebaseAuth.getInstance().currentUser
                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                progressDialog.dismiss()
                                                Toast.makeText(this,
                                                    "Registrasi berhasil!", Toast.LENGTH_LONG).show()
                                                val intent = Intent(this, LoginActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                progressDialog.dismiss() // Tutup ProgressDialog jika ada kegagalan
                                                Toast.makeText(this, "Gagal mengirim email verifikasi", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                                .addOnFailureListener {
                                    progressDialog.dismiss() // Tutup ProgressDialog jika ada kegagalan
                                    Toast.makeText(this, "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Menampilkan pesan error dengan detail
                            progressDialog.dismiss() // Tutup ProgressDialog jika ada kegagalan
                            task.exception?.localizedMessage?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                        }
                    }
            } else {
                Toast.makeText(this, "Data tidak boleh ada yang kosong", Toast.LENGTH_SHORT).show()
            }
        }


        binding.loginText.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }

}