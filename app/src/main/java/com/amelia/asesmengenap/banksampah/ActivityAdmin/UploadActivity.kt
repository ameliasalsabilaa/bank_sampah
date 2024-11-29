package com.amelia.asesmengenap.banksampah.ActivityAdmin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amelia.asesmengenap.banksampah.Domain.ItemsModel
import com.amelia.asesmengenap.banksampah.databinding.ActivityUploadBinding
import com.google.firebase.database.FirebaseDatabase
import android.text.Editable
import android.text.TextWatcher
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private val firebaseDatabase = FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getParcelableExtra<ItemsModel>("item")
        val categoryId = intent.getStringExtra("categoryId") ?: ""

        // Set categoryText to display the title from ItemsModel
        binding.categoryTxt.text = "Tambah Sampah ${item?.title ?: "Default Title"}"

        binding.submitButton.setOnClickListener {
            uploadItemData(categoryId)
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.priceEditText.addTextChangedListener(object : TextWatcher {
            private var currentText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.toString() != currentText) {
                    binding.priceEditText.removeTextChangedListener(this)

                    // Hapus karakter non-angka sebelum memformat ulang
                    val cleanString = s.toString().replace("[^0-9]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        try {
                            val formatted = formatPrice(cleanString.toLong())
                            currentText = formatted
                            binding.priceEditText.setText(formatted)
                            binding.priceEditText.setSelection(formatted.length) // Tempatkan kursor di akhir
                        } catch (e: NumberFormatException) {
                            // Tangani kesalahan jika angka terlalu besar
                            binding.priceEditText.setText("")
                        }
                    }
                    binding.priceEditText.addTextChangedListener(this)
                }
            }
        })
    }

    private fun formatPrice(value: Long): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("id", "ID")))
        return formatter.format(value)
    }

    private fun uploadItemData(categoryId: String) {
        val imageUrlsString = binding.uploadImg.text.toString()
        val imageUrls = imageUrlsString.split(",")
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val priceString = binding.priceEditText.text.toString().replace("[^0-9]".toRegex(), "")
        val price = if (priceString.isNotEmpty()) {
            priceString.toIntOrNull() ?: 0
        } else {
            0
        }
        val showRecommended = binding.recommendedSwitch.isChecked

        // Validasi input
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Tolong isi semua data.", Toast.LENGTH_SHORT).show()
            return
        }

        val itemId = intent.getStringExtra("itemId") ?: ""

        val item = ItemsModel(
            itemId = itemId,
            picUrl = imageUrls.toMutableList(),
            title = title,
            description = description,
            price = price,
            showRecommended = showRecommended,
            categoryId = categoryId
        )

        val itemsRef = firebaseDatabase.getReference("Items")

        if (itemId.isNotEmpty()) {
            itemsRef.child(itemId).setValue(item)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
                        clearInputFields()
                    } else {
                        Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            val newItemRef = itemsRef.push()
            item.itemId = newItemRef.key ?: ""
            newItemRef.setValue(item)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Item uploaded successfully", Toast.LENGTH_SHORT).show()
                        clearInputFields()
                    } else {
                        Toast.makeText(this, "Failed to upload item", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun clearInputFields() {
        binding.uploadImg.text?.clear()
        binding.titleEditText.text?.clear()
        binding.descriptionEditText.text?.clear()
        binding.priceEditText.text?.clear()
        binding.idCategoryEditText.text.clear()
        binding.recommendedSwitch.isChecked = false
        finish()
    }
}