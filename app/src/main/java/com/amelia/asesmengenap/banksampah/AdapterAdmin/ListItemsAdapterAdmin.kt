package com.amelia.asesmengenap.banksampah.AdapterAdmin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amelia.asesmengenap.banksampah.ActivityAdmin.DetailActivityAdmin
import com.amelia.asesmengenap.banksampah.Domain.ItemsModel
import com.amelia.asesmengenap.banksampah.databinding.ViewholderCategoryBinding
import com.amelia.asesmengenap.banksampah.databinding.ViewholderRecommendBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class ListItemsAdapterAdmin(private var items: MutableList<ItemsModel>)
    : RecyclerView.Adapter<ListItemsAdapterAdmin.Viewholder>() {

    class Viewholder(val binding: ViewholderRecommendBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListItemsAdapterAdmin.Viewholder {
        val binding =
            ViewholderRecommendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: ListItemsAdapterAdmin.Viewholder, position: Int) {
        val item = items[position]

        with(holder.binding) {
            val formatter = NumberFormat.getInstance(Locale("id", "ID"))
            titleTxt.text = item.title
            priceTxt.text = formatter.format(item.price)

            Glide.with(holder.itemView.context)
                .load(item.picUrl[0])
                .into(pic)

            root.setOnClickListener {
                val intent = Intent(holder.itemView.context, DetailActivityAdmin::class.java).apply {
                    putExtra("object", item)
                }
                ContextCompat.startActivity(holder.itemView.context, intent, null)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // Fungsi untuk memperbarui data adapter
    fun updateData(newItems: MutableList<ItemsModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
