package com.amelia.asesmengenap.banksampah.Activity

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.amelia.asesmengenap.banksampah.ActivityAdmin.MainActivityAdmin
import com.amelia.asesmengenap.banksampah.Adapter.CategoryAdapter
import com.amelia.asesmengenap.banksampah.Adapter.ListItemsAdapter
import com.amelia.asesmengenap.banksampah.Adapter.RecommendationAdapter
import com.amelia.asesmengenap.banksampah.Adapter.SliderAdapter
import com.amelia.asesmengenap.banksampah.Domain.SliderModel
import com.amelia.asesmengenap.banksampah.Helper.ManagmentCart
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.ViewModel.MainViewModel
import com.amelia.asesmengenap.banksampah.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private val viewModel = MainViewModel()
    private lateinit var binding: ActivityMainBinding
    private lateinit var managmentCart: ManagmentCart
    private lateinit var listItemsActivityLauncher: ActivityResultLauncher<Intent>

    private fun cekLogin(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        managmentCart = ManagmentCart(this)

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("admin_status")

        ref.get().addOnSuccessListener { snapshot ->
            val isAdminLoggedIn = snapshot.getValue(Boolean::class.java) ?: false
            if (isAdminLoggedIn) {
                // Admin masih login
                val intent = Intent(this@MainActivity, MainActivityAdmin::class.java)
                startActivity(intent)
                finish()
            }
        }


        val whatsappIcon: ImageView = findViewById(R.id.whatsapp)
        whatsappIcon.setOnClickListener {
            openWhatsApp("+6281328262140")
        }

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

        displayUsername()

    }

    private fun openWhatsApp(phoneNumber: String) {
        val uri = Uri.parse("https://wa.me/${phoneNumber.replace("+", "")}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayUsername() {
        val user = firebaseAuth.currentUser

        if (user != null) {
            // Jika pengguna sedang login, ambil username dari Firebase
            val userId = user.uid
            val database = FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val userRef = database.getReference("Pengguna").child(userId)

            userRef.get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.child("username").value as? String
                if (username != null) {
                    binding.tvUsername.text = username
                } else {
                    binding.tvUsername.text = "di Bank Sampah!" // Jika username kosong
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch username", Toast.LENGTH_SHORT).show()
                binding.tvUsername.text = "di Bank Sampah!" // Fallback jika gagal mengambil data
            }
        } else {
            // Jika tidak ada pengguna yang sedang login
            binding.tvUsername.text = "di Bank Sampah!"
        }
    }


    private fun kekeranjang() {
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
        binding.keprofil.setOnClickListener {
            if (cekLogin()) {
                val intent = Intent(this, ProfilActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Silahkan Login Terlebih Dahulu", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        binding.keHistory.setOnClickListener {
            if (cekLogin()) {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Silahkan Login Terlebih Dahulu", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun initRecommend() {
        binding.progressBarPopular.visibility = View.VISIBLE

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val spanCount = if (isLandscape) 4 else 2 // 4 kolom untuk landscape, 2 kolom untuk portrait

        binding.viewRecomendation.layoutManager = GridLayoutManager(this@MainActivity, spanCount)
        viewModel.recommend.observe(this, Observer {
            binding.viewRecomendation.adapter = RecommendationAdapter(it)
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
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)

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

    // Fungsi untuk membuka ListItemsActivity
    internal fun openListItemsActivity(categoryId: String, categoryTitle: String) {
        val intent = Intent(this, ListItemsActivity::class.java).apply {
            putExtra("id", categoryId)
            putExtra("title", categoryTitle)
        }
        listItemsActivityLauncher.launch(intent)
    }
}