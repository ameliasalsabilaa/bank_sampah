package com.amelia.asesmengenap.banksampah.ActivityAdmin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amelia.asesmengenap.banksampah.databinding.ActivityEditBinding
import com.google.firebase.database.FirebaseDatabase
import android.text.TextWatcher
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import android.text.Editable
import java.text.NumberFormat

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private val firebaseDatabase = FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val picUrlArray = intent.getStringArrayExtra("picUrl") ?: arrayOf()
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val price = intent.getIntExtra("price", 0)
        val showRecommended = intent.getBooleanExtra("showRecommended", false)
        val itemId = intent.getStringExtra("itemId") ?: ""

        // Set data ke view
        binding.categoryTxt.text = "Ubah $title"
        binding.editTitle.setText(title)
        binding.editDescription.setText(description)
        binding.editPrice.setText(formatPrice(price.toLong()))
        binding.editRecSwitch.isChecked = showRecommended
        binding.editDrive.setText(picUrlArray.joinToString("\n")) // Tampilkan URL sebagai teks multiline

        // Add a TextWatcher to format the price input
        binding.editPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s == null) return
                binding.editPrice.removeTextChangedListener(this)

                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                val parsed = cleanString.toLongOrNull()
                parsed?.let {
                    val formatted = formatPrice(it)
                    binding.editPrice.setText(formatted)
                    binding.editPrice.setSelection(formatted.length)
                }

                binding.editPrice.addTextChangedListener(this)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.editBtn.setOnClickListener {
            val updatedTitle = binding.editTitle.text.toString()
            val updatedDescription = binding.editDescription.text.toString()
            val updatedPriceString = binding.editPrice.text.toString().replace("[^0-9]".toRegex(), "")
            val updatedPrice = if (updatedPriceString.isNotEmpty()) {
                updatedPriceString.toIntOrNull() ?: 0
            } else {
                0 // Default to 0 if input is empty
            }
            val updatedShowRecommended = binding.editRecSwitch.isChecked

            // Split multiline input into a list of URLs
            val updatedPicUrlList = binding.editDrive.text.toString().lines().filter { it.isNotBlank() }

            val updatedFields = hashMapOf<String, Any>(
                "title" to updatedTitle,
                "description" to updatedDescription,
                "picUrl" to updatedPicUrlList,
                "price" to updatedPrice,
                "showRecommended" to updatedShowRecommended
            )

            val itemsRef = firebaseDatabase.getReference("Items")
            itemsRef.child(itemId).updateChildren(updatedFields)
                .addOnSuccessListener {
                    Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
                    clearInputFields()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun formatPrice(value: Long): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("id", "ID")))
        return "${formatter.format(value)}"
    }

    private fun clearInputFields() {
        binding.editDrive.text.clear()
        binding.editTitle.text.clear()
        binding.editDescription.text.clear()
        binding.editPrice.text.clear()
        binding.editRecSwitch.isChecked = false
        finish()
    }
}