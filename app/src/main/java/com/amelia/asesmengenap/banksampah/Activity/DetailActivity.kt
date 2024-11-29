package com.amelia.asesmengenap.banksampah.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amelia.asesmengenap.banksampah.ActivityAdmin.EditActivity
import com.amelia.asesmengenap.banksampah.Adapter.CartAdapter
import com.amelia.asesmengenap.banksampah.Adapter.PicAdapter
import com.amelia.asesmengenap.banksampah.Domain.ItemsModel
import com.amelia.asesmengenap.banksampah.Helper.ManagmentCart
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private var numberOrder = 1
    private lateinit var managmentCart: ManagmentCart

    private fun cekLogin(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user != null
    }

    private val firebaseDatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/")
    }
    private var itemId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemId = intent.getStringExtra("itemId") ?: ""

        managmentCart = ManagmentCart(this)
        getBundle()
        initList()
        updateCartBadge(managmentCart.totalItemsInCart.value ?: 0) // Pastikan memberikan nilai

        // Observer for LiveData to update cart badge in real-time
        managmentCart.totalItemsInCart.observe(this, { totalItems ->
            updateCartBadge(totalItems)
        })
    }

    // Update cart badge
    fun updateCartBadge(totalItems: Int) {
        val cartBadge = findViewById<TextView>(R.id.cartBadge)
        if (totalItems > 0) {
            cartBadge.visibility = View.VISIBLE
            cartBadge.text = totalItems.toString()
        } else {
            cartBadge.visibility = View.GONE
        }
    }

    private fun initList() {
        val picList = ArrayList<String>()
        for (imageUrl in item.picUrl) {
            picList.add(imageUrl)
        }

        Glide.with(this)
            .load(picList[0])
            .into(binding.img)

        binding.picList.adapter = PicAdapter(picList) { selectedImageUrl ->
            Glide.with(this)
                .load(selectedImageUrl)
                .into(binding.img)
        }
        binding.picList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun getBundle() {
        val item = intent.getParcelableExtra<ItemsModel>("object")!!
        if (item != null) {
            this.item = item
            itemId = item.itemId
        }
        this.item = item

        binding.titleTxt.text = item.title
        binding.descriptionTxt.text = item.description

        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        binding.priceTxt.text = formatter.format(item.price)

        binding.addToCartBtn.setOnClickListener {
            if (cekLogin()) {
                item.numberInCart = numberOrder
                managmentCart.insertItem(item) {
                    // Callback to update cart badge
                    updateCartBadge(managmentCart.totalItemsInCart.value ?: 0)
                }
            } else {
                Toast.makeText(this, "Silahkan Login Terlebih Dahulu", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@DetailActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        binding.cartBtn.setOnClickListener {
            if (cekLogin()) {
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Silahkan Login Terlebih Dahulu", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        binding.backBtn.setOnClickListener { finish() }
    }
}