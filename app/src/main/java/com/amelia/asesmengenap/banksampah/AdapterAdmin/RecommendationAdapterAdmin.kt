package com.amelia.asesmengenap.banksampah.AdapterAdmin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amelia.asesmengenap.banksampah.Activity.DetailActivity
import com.amelia.asesmengenap.banksampah.ActivityAdmin.DetailActivityAdmin
import com.amelia.asesmengenap.banksampah.Domain.ItemsModel
import com.amelia.asesmengenap.banksampah.databinding.ViewholderCategoryBinding
import com.amelia.asesmengenap.banksampah.databinding.ViewholderRecommendBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class RecommendationAdapterAdmin(val items: MutableList<ItemsModel>)
    :RecyclerView.Adapter<RecommendationAdapterAdmin.Viewholder>() {

    class Viewholder(val binding: ViewholderRecommendBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecommendationAdapterAdmin.Viewholder {
        val binding =
            ViewholderRecommendBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationAdapterAdmin.Viewholder, position: Int) {
        val item = items[position]

        with(holder.binding){
            val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            currencyFormatter.maximumFractionDigits = 0
            titleTxt.text = item.title
            priceTxt.text = currencyFormatter.format(item.price)

            Glide.with(holder.itemView.context)
                .load(item.picUrl[0])
                .into(pic)

            root.setOnClickListener{
                val intent = Intent(holder.itemView.context, DetailActivityAdmin::class.java).apply {
                    putExtra("object", item)
                }
                ContextCompat.startActivity(holder.itemView.context,intent,null)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}