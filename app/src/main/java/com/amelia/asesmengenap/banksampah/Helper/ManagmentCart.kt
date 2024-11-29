package com.amelia.asesmengenap.banksampah.Helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amelia.asesmengenap.banksampah.Activity.MainActivity
import com.amelia.asesmengenap.banksampah.Domain.ItemsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ManagmentCart(val context: Context) {

    private val tinyDB = TinyDB(context)

    // LiveData untuk memantau perubahan total barang
    val totalItemsInCart: MutableLiveData<Int> = MutableLiveData(0)

    init {
        updateTotalItemsInCart()
    }

    fun insertItem(item: ItemsModel, onCartUpdated: (() -> Unit)? = null) {
        var listFood = getListCart()
        val existAlready = listFood.any { it.title == item.title }
        val index = listFood.indexOfFirst { it.title == item.title }

        if (existAlready) {
            listFood[index].numberInCart = item.numberInCart
        } else {
            listFood.add(item)
        }
        tinyDB.putListObject("CartList", listFood)

        // Perbarui LiveData
        updateTotalItemsInCart()

        // Callback untuk memperbarui badge
        onCartUpdated?.invoke()

        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<ItemsModel> {
        return tinyDB.getListObject("CartList") ?: arrayListOf()
    }

    fun minusItem(listFood: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (listFood[position].numberInCart == 1) {
            // Tampilkan dialog konfirmasi
            AlertDialog.Builder(context)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin menghapus item ini?")
                .setPositiveButton("Ya") { _, _ ->
                    listFood.removeAt(position)
                    tinyDB.putListObject("CartList", listFood)
                    updateTotalItemsInCart() // Perbarui total setelah penghapusan
                    listener.onChanged()
                }
                .setNegativeButton("Tidak", null)
                .show()
        } else {
            listFood[position].numberInCart--
            tinyDB.putListObject("CartList", listFood)
            updateTotalItemsInCart() // Perbarui total setelah pengurangan
            listener.onChanged()
        }
    }

    fun plusItem(listFood: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        listFood[position].numberInCart++
        tinyDB.putListObject("CartList", listFood)
        updateTotalItemsInCart() // Perbarui total setelah penambahan
        listener.onChanged()
    }


    fun getTotalFee(): Double {
        val listFood = getListCart()
        var fee = 0.0
        for (item in listFood) {
            fee += item.price * item.numberInCart
        }
        return fee
    }

    private fun updateTotalItemsInCart() {
        val listFood = getListCart()
        val totalItems = listFood.sumOf { it.numberInCart }
        totalItemsInCart.postValue(totalItems)
    }
    fun clearCart() {
        tinyDB.putListObject("CartList", arrayListOf<ItemsModel>()) // Save an empty list to clear the cart
    }
}