package com.amelia.asesmengenap.banksampah.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.amelia.asesmengenap.banksampah.Domain.OrdersModel
import com.amelia.asesmengenap.banksampah.R
import com.bumptech.glide.Glide
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.amelia.asesmengenap.banksampah.databinding.ViewholderHistoryBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Locale

class HistoryAdapter(
    private val context: Context,
    private var ordersList: List<OrdersModel>,
    private val onApprovalAction: (OrdersModel, Boolean) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ViewholderHistoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val order = ordersList[position]

        val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        currencyFormatter.maximumFractionDigits = 0

        with(holder.binding) {
            username.text = order.username
            viewholderpic.removeAllViews()

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
                true -> viewholderhistori.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.dua)
                )
                false -> {
                    viewholderhistori.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.tiga)
                    )
                    username.setTextColor(ContextCompat.getColor(context, R.color.teks))
                    taxTxt.setTextColor(ContextCompat.getColor(context, R.color.teks))
                    totalSeluruh.setTextColor(ContextCompat.getColor(context, R.color.teks))
                }
                else -> viewholderhistori.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.satu)
                )
            }
        }
    }


    override fun getItemCount(): Int = ordersList.size

    fun searchDataList(searchList: List<OrdersModel>) {
        ordersList = searchList
        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(val binding: ViewholderHistoryBinding) : RecyclerView.ViewHolder(binding.root)
}
