package com.amelia.asesmengenap.banksampah.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amelia.asesmengenap.banksampah.Adapter.CartAdapter
import com.amelia.asesmengenap.banksampah.Domain.OrdersModel
import com.amelia.asesmengenap.banksampah.Helper.ChangeNumberItemsListener
import com.amelia.asesmengenap.banksampah.Helper.ManagmentCart
import com.amelia.asesmengenap.banksampah.databinding.ActivityCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.text.NumberFormat
import java.util.Locale


class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var managmentCart: ManagmentCart
    private var tax: Int = 0 // Ubah menjadi Int
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val database = FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)

        setVariable()
        initCartList()

        // Periksa apakah keranjang kosong saat aktivitas dimulai
        if (managmentCart.getListCart().isEmpty()) {
            binding.emptyTxt.visibility = View.VISIBLE
            binding.scrollView3.visibility = View.GONE
            binding.constraintcart.visibility = View.GONE
            binding.button.visibility = View.GONE
        } else {
            calculatorCart()
        }

        lanjutJual()
    }

    private fun lanjutJual() {
        binding.button.setOnClickListener {
            // Cek apakah keranjang kosong
            if (managmentCart.getListCart().isEmpty()) {
                Toast.makeText(this, "Masukkan item ke dalam keranjang terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Yakin ingin melanjutkan penjualan? Pastikan sampah sesuai, bila tidak anda akan kami kenai denda.")
                    .setCancelable(false)
                    .setPositiveButton("Ya") { dialog, id ->
                        if (userId != null) {
                            val cartList = managmentCart.getListCart()
                            var subtotalharga = 0 // Ubah menjadi Int
                            for (item in cartList) {
                                subtotalharga += item.price * item.numberInCart
                            }

                            val pajakongkir = (subtotalharga * 0.02).toInt() + 10 // Ubah menjadi Int
                            val subtotalkeseluruhan = subtotalharga + pajakongkir

                            val orderRef = database.getReference("Pesanan").push()
                            prepareOrderData(userId!!, orderRef, subtotalharga, pajakongkir, subtotalkeseluruhan)
                        } else {
                            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }

                    .setNegativeButton("Tidak") { dialog, id ->
                        dialog.dismiss()
                    }

                val alert = builder.create()
                alert.show()
            }
        }
    }

    private fun prepareOrderData(
        userId: String,
        orderRef: DatabaseReference,
        subtotalharga: Int,
        pajakongkir: Int,
        subtotalkeseluruhan: Int
    ) {
        val userRef = database.getReference("Pengguna").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java) ?: "Unknown"

                val cartList = managmentCart.getListCart()
                val picUrl = ArrayList<String>()
                val namabarang = ArrayList<String>()
                val jumlahsetiapitem = ArrayList<Int>()
                val hargabarang = ArrayList<Int>()

                for (item in cartList) {
                    picUrl.addAll(item.picUrl)
                    namabarang.add(item.title)
                    jumlahsetiapitem.add(item.numberInCart)
                    hargabarang.add(item.price)
                }

                val order = OrdersModel(
                    id = orderRef.key ?: "",
                    idUser = userId,
                    username = username,
                    picUrl = picUrl,
                    namabarang = namabarang,
                    jumlahsetiapitem = jumlahsetiapitem,
                    hargabarang = hargabarang,
                    subtotalharga = subtotalharga,
                    pajakongkir = pajakongkir,
                    subtotalkeseluruhan = subtotalkeseluruhan,
                    perizinan = null
                )

                // Simpan data pesanan ke Firebase
                orderRef.setValue(order)
                    .addOnSuccessListener {
                        // Bersihkan keranjang setelah pesanan disimpan
                        managmentCart.clearCart()

                        // Tampilkan notifikasi sukses
                        Toast.makeText(this@CartActivity, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()


                        // Navigasi ke halaman utama atau halaman lain yang sesuai
                        val intent = Intent(this@CartActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        // Tampilkan pesan error jika gagal menyimpan pesanan
                        Toast.makeText(this@CartActivity, "Gagal menyimpan pesanan: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CartActivity, "Gagal mengambil data pengguna: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initCartList() {
        binding.viewCart.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.viewCart.adapter = CartAdapter(managmentCart.getListCart(), this, object : ChangeNumberItemsListener {
            override fun onChanged() {
                if (managmentCart.getListCart().isEmpty()) {
                    // Keranjang kosong: tampilkan emptyTxt
                    binding.emptyTxt.visibility = View.VISIBLE
                    binding.scrollView3.visibility = View.GONE
                    binding.constraintcart.visibility = View.GONE
                    binding.button.visibility = View.GONE
                } else {
                    // Keranjang tidak kosong: tampilkan elemen keranjang
                    binding.emptyTxt.visibility = View.GONE
                    binding.scrollView3.visibility = View.VISIBLE
                    binding.constraintcart.visibility = View.VISIBLE
                    binding.button.visibility = View.VISIBLE

                    // Perbarui kalkulasi total keranjang
                    calculatorCart()
                }
            }
        })
    }


    private fun setVariable() {
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }
        }
    }

    private fun calculatorCart() {
        val percentTax = 0.02
        val delivery = 5000
        tax = ((managmentCart.getTotalFee() * percentTax).toInt()) // Ubah menjadi Int
        val total = (managmentCart.getTotalFee() + tax + delivery).toInt() // Ubah menjadi Int
        val itemTotal = managmentCart.getTotalFee().toInt() // Ubah menjadi Int

        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        formatter.minimumFractionDigits = 0

        with(binding) {
            totalFeeTxt.text = "Rp ${formatter.format(itemTotal)}"
            taxTxt.text = "Rp ${formatter.format(tax)}"
            deliveryTxt.text = "Rp ${formatter.format(delivery)}"
            totalTxt.text = "Rp ${formatter.format(total)}"
        }
    }
}