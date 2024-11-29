package com.amelia.asesmengenap.banksampah.AdapterAdmin

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.amelia.asesmengenap.banksampah.Domain.OrdersModel
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.databinding.ViewholderPerizinanBinding
import com.bumptech.glide.Glide
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Locale

class OrdersAdapter(
    private val context: Context,
    private var ordersList: List<OrdersModel>,
    private val onApprovalAction: (OrdersModel, Boolean) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val binding = ViewholderPerizinanBinding.inflate(LayoutInflater.from(context), parent, false)
        return OrdersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = ordersList[position]

        with(holder.binding) {
            username.text = order.username
            viewholderpic.removeAllViews()

            // Formatter untuk mata uang Indonesia
            val currencyFormatter = NumberFormat.getInstance(Locale("id", "ID"))

            // Loop untuk setiap produk
            for (i in order.namabarang.indices) {
                val productView = LayoutInflater.from(context).inflate(R.layout.item_product, viewholderpic, false)

                val pic = productView.findViewById<ImageView>(R.id.pic)
                val titleTxt = productView.findViewById<TextView>(R.id.titleTxt)
                val hargasatuan = productView.findViewById<TextView>(R.id.hargasatuan)
                val numberItemTxt = productView.findViewById<TextView>(R.id.numberItemTxt)
                val subtotalhargasatuan = productView.findViewById<TextView>(R.id.subtotalhargasatuan)

                // Set text untuk nama barang, harga satuan, jumlah, dan subtotal
                titleTxt.text = order.namabarang[i]
                hargasatuan.text = currencyFormatter.format(order.hargabarang[i]) // Format harga satuan
                numberItemTxt.text = order.jumlahsetiapitem[i].toString()
                subtotalhargasatuan.text = currencyFormatter.format(order.jumlahsetiapitem[i] * order.hargabarang[i]) // Format subtotal

                // Ubah warna teks hargasatuan jika perizinan false
                if (order.perizinan == false) {
                    hargasatuan.setTextColor(ContextCompat.getColor(context, R.color.teks))
                } else if (order.perizinan == null){
                    titleTxt.setTextColor(ContextCompat.getColor(context, R.color.teks))
                    hargasatuan.setTextColor(ContextCompat.getColor(context, R.color.teks))
                } else {
                    hargasatuan.setTextColor(ContextCompat.getColor(context, R.color.teks))
                }

                // Load gambar menggunakan Glide
                if (i < order.picUrl.size) {
                    Glide.with(context).load(order.picUrl[i]).into(pic)
                }

                // Tambahkan tampilan produk ke dalam viewholderpic
                viewholderpic.addView(productView)
            }

            taxTxt.text = currencyFormatter.format(order.pajakongkir)
            totalSeluruh.text = currencyFormatter.format(order.subtotalkeseluruhan)

            // Atur warna latar belakang CardView berdasarkan status perizinan
            when (order.perizinan) {
                true -> viewholderperizinan.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.dua)
                )
                false -> {
                    viewholderperizinan.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.tiga)
                    )
                    username.setTextColor(ContextCompat.getColor(context, R.color.teks))
                    taxTxt.setTextColor(ContextCompat.getColor(context, R.color.teks))
                    totalSeluruh.setTextColor(ContextCompat.getColor(context, R.color.teks))
                }
                else -> viewholderperizinan.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.satu)
                )
            }

            // Atur tombol btnyes berdasarkan status perizinan
            if (order.perizinan == true) {
                btnyes.isEnabled = false
                btnyes.text = "Selesai"
            } else {
                btnyes.isEnabled = true
                btnyes.text = "Setujui"
            }

            // Atur tombol btnno berdasarkan status perizinan
            if (order.perizinan == false) {
                btnNo.isEnabled = false
                btnNo.text = "Selesai"
            } else {
                btnNo.isEnabled = true
                btnNo.text = "Tolak"
            }

            // Logika untuk tombol btnyes
            btnyes.setOnClickListener {
                val builder = android.app.AlertDialog.Builder(context)
                builder.setTitle("Konfirmasi")
                builder.setMessage("Apakah Anda yakin ingin menyetujui penjualan ini?")

                builder.setPositiveButton("Ya") { dialog, _ ->
                    dialog.dismiss()

                    val orderRef = FirebaseDatabase.getInstance()
                        .getReference("Pesanan")
                        .child(order.id)

                    orderRef.child("perizinan").setValue(true).addOnSuccessListener {
                        onApprovalAction(order, true)
                    }.addOnFailureListener {
                        Toast.makeText(context, "Gagal memperbarui status pesanan.", Toast.LENGTH_SHORT).show()
                    }
                }

                builder.setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }

                builder.show()
            }

            // Logika untuk tombol btnno
            btnNo.setOnClickListener {
                val builder = android.app.AlertDialog.Builder(context)
                builder.setTitle("Konfirmasi")
                builder.setMessage("Apakah Anda yakin ingin menolak penjualan ini?")

                builder.setPositiveButton("Ya") { dialog, _ ->
                    dialog.dismiss()

                    val orderRef = FirebaseDatabase.getInstance()
                        .getReference("Pesanan")
                        .child(order.id)

                    orderRef.child("perizinan").setValue(false).addOnSuccessListener {
                        onApprovalAction(order, false)

                        // Disable tombol dan ubah teks menjadi "Ditolak"
                        btnNo.isEnabled = false
                        btnNo.text = "Ditolak"
                        btnNo.setBackgroundColor(
                            context.resources.getColor(R.color.filter_inactive_bg, context.theme)
                        )
                    }.addOnFailureListener {
                        Toast.makeText(context, "Gagal memperbarui status pesanan.", Toast.LENGTH_SHORT).show()
                    }
                }

                builder.setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }

                builder.show()
            }
        }
    }



    override fun getItemCount(): Int = ordersList.size

    fun searchDataList(searchList: List<OrdersModel>) {
        ordersList = ArrayList(searchList)
        notifyDataSetChanged()
    }

    inner class OrdersViewHolder(val binding: ViewholderPerizinanBinding) : RecyclerView.ViewHolder(binding.root)
}
