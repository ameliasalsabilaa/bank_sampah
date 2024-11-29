package com.amelia.asesmengenap.banksampah.ActivityAdmin

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.amelia.asesmengenap.banksampah.Adapter.ListItemsAdapter
import com.amelia.asesmengenap.banksampah.AdapterAdmin.ListItemsAdapterAdmin
import com.amelia.asesmengenap.banksampah.Domain.ItemsModel
import com.amelia.asesmengenap.banksampah.ViewModel.MainViewModel
import com.amelia.asesmengenap.banksampah.databinding.ActivityListItemsAdminBinding

class ListItemsActivityAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityListItemsAdminBinding
    private val viewModel = MainViewModel()
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListItemsAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getBundle()
        initList()

    }

    private fun initList() {
        binding.apply {
            progressBarList.visibility = View.VISIBLE

            val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val spanCount = if (isLandscape) 4 else 2

            viewList.layoutManager = GridLayoutManager(this@ListItemsActivityAdmin, spanCount)
            viewModel.recommend.observe(this@ListItemsActivityAdmin, Observer {
                viewList.adapter = ListItemsAdapterAdmin(it)
                progressBarList.visibility = View.GONE
            })

            viewModel.loadFiltered(id)
        }
    }

    private fun getBundle() {
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!
        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.fab.setOnClickListener {
            val item = ItemsModel(
                itemId = "",
                title = title,
                categoryId = id // Pass the category ID
            )
            val intent = Intent(this, UploadActivity::class.java).apply {
                putExtra("categoryId", id) // Pass category ID
                putExtra("item", item) // Pass ItemsModel object
            }
            startActivity(intent)
        }

        binding.categoryTxt.text = "Sampah $title"
    }

    override fun onBackPressed() {
        // Kirim hasil kembali ke MainActivity
        val resultIntent = Intent()
        resultIntent.putExtra("resetCategory", true)
        setResult(RESULT_OK, resultIntent)
        super.onBackPressed()
    }
}