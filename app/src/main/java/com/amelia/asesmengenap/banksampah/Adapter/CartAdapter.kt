package com.amelia.asesmengenap.banksampah.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amelia.asesmengenap.banksampah.Activity.DetailActivity
import com.amelia.asesmengenap.banksampah.Activity.MainActivity
import com.amelia.asesmengenap.banksampah.Domain.ItemsModel
import com.amelia.asesmengenap.banksampah.Helper.ChangeNumberItemsListener
import com.amelia.asesmengenap.banksampah.Helper.ManagmentCart
import com.amelia.asesmengenap.banksampah.databinding.ViewholderCartBinding
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val listItemSelected:ArrayList<ItemsModel>,
    context: Context,
    var changeNumberItemsListener: ChangeNumberItemsListener
):RecyclerView.Adapter<CartAdapter.Viewholder>() {
    class Viewholder (val binding: ViewholderCartBinding) : RecyclerView.ViewHolder(binding.root)

    private val managmentCart = ManagmentCart(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.Viewholder {
        val binding = ViewholderCartBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: CartAdapter.Viewholder, position: Int) {
        val item = listItemSelected[position]
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))

        // Format harga untuk feeEachTime dan totalEachItem
        holder.binding.titleTxt.text = item.title
        holder.binding.feeEachTime.text = "Rp ${formatter.format(item.price)}" // Format item.price
        holder.binding.totalEachItem.text = "Rp ${formatter.format((item.numberInCart * item.price).toInt())}" // Format total harga item
        holder.binding.numberItemTxt.text = item.numberInCart.toString()

        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .into(holder.binding.pic)

        holder.binding.plusCartBtn.setOnClickListener {
            managmentCart.plusItem(listItemSelected, position, object : ChangeNumberItemsListener {
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumberItemsListener.onChanged()
                }
            })
        }

        holder.binding.minusCartBtn.setOnClickListener {
            managmentCart.minusItem(listItemSelected, position, object : ChangeNumberItemsListener {
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumberItemsListener.onChanged()
                }
            })
        }
    }


    override fun getItemCount(): Int = listItemSelected.size
}