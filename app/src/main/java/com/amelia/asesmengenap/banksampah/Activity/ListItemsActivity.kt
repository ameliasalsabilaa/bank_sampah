package com.amelia.asesmengenap.banksampah.Activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.amelia.asesmengenap.banksampah.ActivityAdmin.UploadActivity
import com.amelia.asesmengenap.banksampah.Adapter.ListItemsAdapter
import com.amelia.asesmengenap.banksampah.ViewModel.MainViewModel
import com.amelia.asesmengenap.banksampah.databinding.ActivityListItemsBinding

class ListItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListItemsBinding
    private val viewModel = MainViewModel()
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getBundle()
        initList()

    }

    private fun initList() {
        binding.apply {
            progressBarList.visibility = View.VISIBLE

            // Menentukan jumlah kolom berdasarkan orientasi layar
            val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val spanCount = if (isLandscape) 4 else 2 // 4 kolom untuk landscape, 2 kolom untuk portrait

            viewList.layoutManager = GridLayoutManager(this@ListItemsActivity, spanCount)
            viewModel.recommend.observe(this@ListItemsActivity, Observer {
                viewList.adapter = ListItemsAdapter(it)
                progressBarList.visibility = View.GONE
            })

            viewModel.loadFiltered(id)
        }
    }


    private fun getBundle() {
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!
        binding.backBtn.setOnClickListener { onBackPressed() }

        binding.categoryTxt.text = "Sampah $title"
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        resultIntent.putExtra("resetCategory", true)
        setResult(RESULT_OK, resultIntent)
        super.onBackPressed()
    }

}