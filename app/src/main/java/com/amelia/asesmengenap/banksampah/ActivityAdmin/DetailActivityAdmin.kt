package com.amelia.asesmengenap.banksampah.ActivityAdmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amelia.asesmengenap.banksampah.Activity.CartActivity
import com.amelia.asesmengenap.banksampah.Activity.LoginActivity
import com.amelia.asesmengenap.banksampah.Adapter.PicAdapter
import com.amelia.asesmengenap.banksampah.Domain.ItemsModel
import com.amelia.asesmengenap.banksampah.Helper.ManagmentCart
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.databinding.ActivityDetailAdminBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import androidx.recyclerview.widget.RecyclerView


class DetailActivityAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityDetailAdminBinding
    private lateinit var item: ItemsModel
    private var numberOrder = 1
    private lateinit var managmentCart: ManagmentCart

    // Animation
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }
    private var clicked = false

    private val firebaseDatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/")
    }
    private var itemId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemId = intent.getStringExtra("itemId") ?: ""
        managmentCart = ManagmentCart(this)
        getBundle()
        initList()
        setupDeleteButton()
        setupFirebaseListener()
    }

    private fun setupFirebaseListener() {
        if (itemId.isNotEmpty()) {
            val itemRef = firebaseDatabase.getReference("Items").child(itemId)

            itemRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val updatedItem = snapshot.getValue(ItemsModel::class.java)
                        if (updatedItem != null) {
                            item = updatedItem
                            updateUIWithItemData()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DetailActivityAdmin,
                        "Failed to load data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun updateUIWithItemData() {
        binding.titleTxt.text = item.title
        binding.descriptionTxt.text = item.description

        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        binding.priceTxt.text = formatter.format(item.price)

        if (!isDestroyed && !isFinishing) {
            if (item.picUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(item.picUrl[0])
                    .into(binding.img)
            }

            val picList = ArrayList(item.picUrl)
            binding.picList.adapter = PicAdapter(picList) { selectedImageUrl ->
                if (!isDestroyed && !isFinishing) {
                    Glide.with(this)
                        .load(selectedImageUrl)
                        .into(binding.img)
                }
            }
        }
    }

    private fun setupDeleteButton() {
        binding.hapusBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { _, _ ->
                    if (itemId.isNotEmpty()) {
                        val itemRef = firebaseDatabase.getReference("Items").child(itemId)
                        itemRef.removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Item ID not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun initList() {
        val picList = ArrayList<String>()
        for (imageUrl in item.picUrl) {
            picList.add(imageUrl)
        }

        if (!isDestroyed && !isFinishing) {
            Glide.with(this)
                .load(picList[0])
                .into(binding.img)
        }

        binding.picList.adapter = PicAdapter(picList) { selectedImageUrl ->
            if (!isDestroyed && !isFinishing) {
                Glide.with(this)
                    .load(selectedImageUrl)
                    .into(binding.img)
            }
        }
        binding.picList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun getBundle() {
        val item = intent.getParcelableExtra<ItemsModel>("object")
        if (item != null) {
            this.item = item
            itemId = item.itemId
        }

        binding.titleTxt.text = item?.title
        binding.descriptionTxt.text = item?.description
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        binding.priceTxt.text = formatter.format(item?.price)

        binding.ubahBtn.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java).apply {
                putExtra("title", item?.title)
                putExtra("description", item?.description)
                putExtra("price", item?.price)
                putExtra("showRecommended", item?.showRecommended)
                putExtra("itemId", item?.itemId)
                putExtra("picUrl", item?.picUrl?.toTypedArray())
            }
            startActivity(intent)
        }

        binding.backBtn.setOnClickListener { finish() }

        binding.tambahBtn.setOnClickListener {
            onAddButtonClicked()
        }
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.ubahBtn.visibility = View.VISIBLE
            binding.hapusBtn.visibility = View.VISIBLE
        } else {
            binding.ubahBtn.visibility = View.INVISIBLE
            binding.hapusBtn.visibility = View.INVISIBLE
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.ubahBtn.startAnimation(fromBottom)
            binding.hapusBtn.startAnimation(fromBottom)
            binding.tambahBtn.startAnimation(rotateOpen)
        } else {
            binding.ubahBtn.startAnimation(toBottom)
            binding.hapusBtn.startAnimation(toBottom)
            binding.tambahBtn.startAnimation(rotateClose)
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            binding.ubahBtn.isClickable = true
            binding.hapusBtn.isClickable = true
        } else {
            binding.ubahBtn.isClickable = false
            binding.hapusBtn.isClickable = false
        }
    }
}
