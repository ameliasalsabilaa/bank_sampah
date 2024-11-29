package com.amelia.asesmengenap.banksampah.Adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.amelia.asesmengenap.banksampah.Activity.ListItemsActivity
import com.amelia.asesmengenap.banksampah.Activity.MainActivity
import com.amelia.asesmengenap.banksampah.Domain.CategoryModel
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.databinding.ViewholderCategoryBinding
import com.bumptech.glide.Glide

class CategoryAdapter(
    private val items: MutableList<CategoryModel>,
    private val onCategorySelected: (String, String) -> Unit, // Callback untuk membuka ListItemsActivity
    private val onCategoryReset: () -> Unit // Callback untuk mereset kategori
) : RecyclerView.Adapter<CategoryAdapter.Viewholder>() {
    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    inner class Viewholder(val binding: ViewholderCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text = item.title

        Glide.with(holder.itemView.context)
            .load(item.picUrl)
            .into(holder.binding.pic)

        if (selectedPosition == position) {
            holder.binding.pic.setBackgroundResource(0)
            holder.binding.mainLayout.setBackgroundResource(R.drawable.category_bg)
            ImageViewCompat.setImageTintList(
                holder.binding.pic,
                ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.teks))
            )
            holder.binding.titleTxt.visibility = View.VISIBLE
            holder.binding.titleTxt.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.teks))
        } else {
            holder.binding.pic.setBackgroundResource(R.drawable.category_bg)
            holder.binding.mainLayout.setBackgroundResource(0)
            ImageViewCompat.setImageTintList(
                holder.binding.pic,
                ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.tekscategory))
            )
            holder.binding.titleTxt.visibility = View.GONE
            holder.binding.titleTxt.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.tekscategory))
        }

        holder.binding.root.setOnClickListener {
            val clickedPosition = holder.adapterPosition
            if (clickedPosition != RecyclerView.NO_POSITION) {
                lastSelectedPosition = selectedPosition
                selectedPosition = clickedPosition
                notifyItemChanged(lastSelectedPosition)
                notifyItemChanged(selectedPosition)

                // Tambahkan delay 700ms sebelum berpindah halaman
                Handler(Looper.getMainLooper()).postDelayed({
                    onCategorySelected(item.id.toString(), item.title)
                }, 700)
            }
        }

    }

    override fun getItemCount(): Int = items.size

    fun resetSelection() {
        if (selectedPosition != -1) {
            lastSelectedPosition = selectedPosition
            selectedPosition = -1
            notifyItemChanged(lastSelectedPosition)
        }
    }
}




