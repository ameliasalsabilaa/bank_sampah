package com.amelia.asesmengenap.banksampah.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.databinding.ViewholderPicBinding
import com.bumptech.glide.Glide

class PicAdapter(val items: MutableList<String>, private val onImageSelected: (String) -> Unit):
    RecyclerView.Adapter<PicAdapter.Viewholder>() {

    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    inner class Viewholder(val binding: ViewholderPicBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderPicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]
        holder.binding.pic.loadImage(item)

        holder.binding.root.setOnClickListener{
            lastSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)
            onImageSelected(item)
        }

        if (selectedPosition == position){
            holder.binding.picLayout.setBackgroundResource(R.drawable.bg_selected_picadapter)
        }else {
            holder.binding.picLayout.setBackgroundResource(R.drawable.bg_remonded)
        }
    }

    override fun getItemCount(): Int = items.size

    fun ImageView.loadImage(url: String) {
        Glide.with(this. context)
            .load(url)
            .into(this)
    }



}