package com.amelia.asesmengenap.banksampah.ActivityAdmin

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.amelia.asesmengenap.banksampah.Activity.ListItemsActivity
import com.amelia.asesmengenap.banksampah.Activity.LoginActivity
import com.amelia.asesmengenap.banksampah.Activity.MainActivity
import com.amelia.asesmengenap.banksampah.Adapter.CategoryAdapter
import com.amelia.asesmengenap.banksampah.Adapter.SliderAdapter
import com.amelia.asesmengenap.banksampah.AdapterAdmin.CategoryAdapterAdmin
import com.amelia.asesmengenap.banksampah.AdapterAdmin.RecommendationAdapterAdmin
import com.amelia.asesmengenap.banksampah.Domain.SliderModel
import com.amelia.asesmengenap.banksampah.ViewModel.MainViewModel
import com.amelia.asesmengenap.banksampah.databinding.ActivityMainAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivityAdmin : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private val  viewModel = MainViewModel()
    private lateinit var binding: ActivityMainAdminBinding
    private lateinit var listItemsActivityLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        listItemsActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val shouldReset = result.data?.getBooleanExtra("resetCategory", false) ?: false
                if (shouldReset) {
                    (binding.viewCategory.adapter as? CategoryAdapter)?.resetSelection()
                }
            }
        }

        initBanners()
        initCategory()
        initRecommend()
        kekeranjang()
    }

    private fun kekeranjang() {
        binding.logout.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Konfirmasi Logout")
            builder.setMessage("Apakah Anda yakin ingin logout?")

            builder.setPositiveButton("Ya") { dialogInterface, _ ->
                firebaseAuth.signOut()
                val database = FirebaseDatabase.getInstance()
                val ref = database.getReference("admin_status")
                ref.setValue(false)

                Toast.makeText(this, "Logout Berhasil", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton("Tidak") { dialogInterface, _ ->
                dialogInterface.dismiss() //
            }

            val alertDialog = builder.create()
            alertDialog.show()
        }

        binding.keperizinan.setOnClickListener {
            val intent = Intent(this, PerizinanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecommend() {
        binding.progressBarPopular.visibility = View.VISIBLE

        // Menentukan jumlah kolom berdasarkan orientasi layar
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val spanCount = if (isLandscape) 4 else 2 // 4 kolom untuk landscape, 2 kolom untuk portrait

        binding.viewRecomendation.layoutManager = GridLayoutManager(this@MainActivityAdmin, spanCount)
        viewModel.recommend.observe(this, Observer {
            binding.viewRecomendation.adapter = RecommendationAdapterAdmin(it)
            binding.progressBarPopular.visibility = View.GONE
        })

        viewModel.loadRecommend()
    }


    private fun initBanners() {
        binding.progressBarBanner.visibility = View.VISIBLE

        // Observasi data banner dari ViewModel
        viewModel.banner.observe(this, Observer { banners ->
            setupBanners(banners) // Panggil fungsi untuk men-setup slider
            binding.progressBarBanner.visibility = View.GONE
        })

        // Load data banner dari Firebase
        viewModel.loadBanner()
    }

    private fun setupBanners(banners: List<SliderModel>) {
        // Pasang adapter slider
        binding.viewpagerbanner.adapter = SliderAdapter(banners, binding.viewpagerbanner)

        // Konfigurasi ViewPager2 untuk membuat efek slider lebih menarik
        binding.viewpagerbanner.clipToPadding = false
        binding.viewpagerbanner.clipChildren = false
        binding.viewpagerbanner.offscreenPageLimit = 6
        binding.viewpagerbanner.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
        }
        binding.viewpagerbanner.setPageTransformer(compositePageTransformer)

        // Tampilkan indikator jika lebih dari satu banner
        if (banners.size > 1) {
            binding.dotIndicator.visibility = View.VISIBLE
            binding.dotIndicator.attachTo(binding.viewpagerbanner)
        }

        // Tambahkan looping slider menggunakan OnPageChangeCallback
        binding.viewpagerbanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE

        viewModel.category.observe(this) { categories ->
            binding.viewCategory.layoutManager =
                LinearLayoutManager(this@MainActivityAdmin, LinearLayoutManager.HORIZONTAL, false)

            val adapter = CategoryAdapter(
                categories,
                onCategorySelected = { id, title ->
                    openListItemsActivity(id, title)
                },
                onCategoryReset = {
                    Toast.makeText(this, "Kategori kembali ke posisi awal.", Toast.LENGTH_SHORT).show()
                }
            )

            binding.viewCategory.adapter = adapter
            binding.progressBarCategory.visibility = View.GONE
        }

        viewModel.loadCategory()
    }

    internal fun openListItemsActivity(categoryId: String, categoryTitle: String) {
        val intent = Intent(this, ListItemsActivityAdmin::class.java).apply {
            putExtra("id", categoryId)
            putExtra("title", categoryTitle)
        }
        listItemsActivityLauncher.launch(intent)
    }
}