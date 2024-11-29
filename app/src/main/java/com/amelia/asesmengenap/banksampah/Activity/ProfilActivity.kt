package com.amelia.asesmengenap.banksampah.Activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amelia.asesmengenap.banksampah.Domain.ModelPengguna
import com.amelia.asesmengenap.banksampah.Domain.OrdersModel
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.databinding.ActivityProfilBinding
import com.amelia.asesmengenap.banksampah.databinding.AlertEditpenggunaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef =
            FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Pengguna")

        loadUserData()
        intentmenuju()
        displayUsername()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Ambil data pengguna berdasarkan userId
            databaseRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").getValue(String::class.java) ?: "N/A"
                        val alamat = snapshot.child("alamat").getValue(String::class.java) ?: "N/A"
                        val email = currentUser.email ?: "N/A" // Dapatkan email langsung dari auth

                        // Tampilkan data di TextView
                        binding.tvUsername.text = username
                        binding.tvAlamat.text = alamat
                        binding.tvEmail.text = email
                    } else {
                        Toast.makeText(this@ProfilActivity, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfilActivity", "Database error: ${error.message}")
                    Toast.makeText(this@ProfilActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Pengguna belum login", Toast.LENGTH_SHORT).show()
        }

        binding.btnEdit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogBinding = AlertEditpenggunaBinding.inflate(layoutInflater)
            builder.setView(dialogBinding.root)

            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Hilangkan background putih
            dialog.show()

            // Ambil UID pengguna yang sedang login
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid

            if (userId != null) {
                val databaseRef = FirebaseDatabase.getInstance().getReference("Pengguna/$userId")
                databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Ambil data dari database
                            val username = snapshot.child("username").getValue(String::class.java) ?: ""
                            val alamat = snapshot.child("alamat").getValue(String::class.java) ?: ""
                            val password = snapshot.child("password").getValue(String::class.java) ?: ""
                            val email = currentUser.email ?: ""

                            // Tampilkan data di dialog
                            dialogBinding.editUsername.setText(username)
                            dialogBinding.editAddress.setText(alamat)
                            dialogBinding.editPassword.setText(password) // Menampilkan kata sandi lama
                            dialogBinding.editBox.setText(email)
                        } else {
                            Toast.makeText(this@ProfilActivity, "Data pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ProfilActivity", "Database error: ${error.message}")
                        Toast.makeText(this@ProfilActivity, "Gagal mengambil data pengguna.", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Pengguna tidak login.", Toast.LENGTH_SHORT).show()
            }

            var isPasswordVisible = false // Status awal password (disembunyikan)

            // Logika toggle untuk melihat password
            dialogBinding.editPassword.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    // Cek apakah klik pada drawableEnd
                    if (event.rawX >= (dialogBinding.editPassword.right - dialogBinding.editPassword.compoundDrawables[2].bounds.width())) {
                        isPasswordVisible = !isPasswordVisible

                        // Toggle inputType untuk melihat password
                        if (isPasswordVisible) {
                            dialogBinding.editPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            dialogBinding.editPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, getDrawable(R.drawable.baseline_visibility_24), null
                            )
                        } else {
                            dialogBinding.editPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                            dialogBinding.editPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, getDrawable(R.drawable.baseline_password_24), null
                            )
                        }
                        dialogBinding.editPassword.setSelection(dialogBinding.editPassword.text.length) // Tetap di akhir teks
                        return@setOnTouchListener true
                    }
                }
                false
            }

            // Tombol "Ubah"
            dialogBinding.btnUbah.setOnClickListener {
                val username = dialogBinding.editUsername.text.toString()
                val email = dialogBinding.editBox.text.toString()
                val password = dialogBinding.editPassword.text.toString()
                val address = dialogBinding.editAddress.text.toString()

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty()) {
                    Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedPengguna = ModelPengguna(
                        idUser = email,
                        username = username,
                        alamat = address,
                        password = password
                    )

                    val databaseRef = FirebaseDatabase.getInstance().getReference("Pengguna/$userId")
                    databaseRef.setValue(updatedPengguna).addOnSuccessListener {
                        Toast.makeText(this, "Data berhasil diperbarui!", Toast.LENGTH_SHORT).show()

                        loadUserData()
                        dialog.dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Gagal memperbarui data.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Tombol "Batal"
            dialogBinding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }
        }


        binding.logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Konfirmasi Logout")
            builder.setMessage("Apakah Anda yakin ingin logout?")

            builder.setPositiveButton("Ya") { dialogInterface, _ ->
                firebaseAuth.signOut()
                Toast.makeText(this, "Logout Berhasil", Toast.LENGTH_SHORT).show()

//                menuju ke halaman yang diinginkan setelah logout
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton("Tidak") { dialogInterface, _ ->
                dialogInterface.dismiss() //
            }

            val alertDialog = builder.create()
            alertDialog.show()
        }

    }

    private fun displayUsername() {
        val user = firebaseAuth.currentUser

        if (user != null) {
            val userId = user.uid

            val database = FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val userRef = database.getReference("Pengguna").child(userId)

            userRef.get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.child("username").value as? String
                if (username != null) {
                    binding.tvUsernamee.text = username
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun intentmenuju() {
        binding.keBeranda.setOnClickListener {
            startActivity(Intent(this@ProfilActivity, MainActivity::class.java))
        }
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this@ProfilActivity, CartActivity::class.java))
        }
        binding.keHistory.setOnClickListener {
            startActivity(Intent(this@ProfilActivity, HistoryActivity::class.java))
        }
        binding.tvEmail.setOnClickListener {
            val email = binding.tvEmail.text.toString() // Ambil teks dari TextView
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email") // Gunakan URI mailto
            }
            try {
                startActivity(intent) // Buka aplikasi email
            } catch (e: Exception) {
                Toast.makeText(this, "Tidak ada aplikasi email yang tersedia.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun loadUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Ambil data pengguna berdasarkan userId
            databaseRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").getValue(String::class.java) ?: "N/A"
                        val alamat = snapshot.child("alamat").getValue(String::class.java) ?: "N/A"
                        val email = currentUser.email ?: "N/A" // Dapatkan email langsung dari auth

                        // Tampilkan data pengguna di TextView
                        binding.tvUsername.text = username
                        binding.tvAlamat.text = alamat
                        binding.tvEmail.text = email

                        // Ambil dan hitung saldo berdasarkan OrdersModel
                        calculateSaldo(userId)
                    } else {
                        Toast.makeText(this@ProfilActivity, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfilActivity", "Database error: ${error.message}")
                    Toast.makeText(this@ProfilActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Pengguna belum login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateSaldo(userId: String) {
        val ordersRef = FirebaseDatabase.getInstance().getReference("Pesanan")

        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalSaldo = 0

                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(OrdersModel::class.java)
                    if (order != null && order.idUser == userId && order.perizinan == true) {
                        totalSaldo += order.subtotalkeseluruhan.toInt() // Pastikan nilai adalah Int
                    }
                }

                val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                currencyFormatter.maximumFractionDigits = 0

                binding.tvSaldo.text = currencyFormatter.format(totalSaldo)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfilActivity", "Database error: ${error.message}")
                Toast.makeText(this@ProfilActivity, "Gagal menghitung saldo", Toast.LENGTH_SHORT).show()
            }
        })
    }



}