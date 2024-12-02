package com.amelia.asesmengenap.banksampah.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amelia.asesmengenap.banksampah.Adapter.HistoryAdapter
import com.amelia.asesmengenap.banksampah.Domain.OrdersModel
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.content.res.Configuration
import androidx.recyclerview.widget.GridLayoutManager

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Ambil UID pengguna saat ini
    private lateinit var database: DatabaseReference
    private lateinit var orderList: ArrayList<OrdersModel>
    private var activeButtonId: Int = R.id.btnFilterAll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup ViewBinding
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Database reference
        database = FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Pesanan")

        // Initialize list and adapter
        orderList = ArrayList()
        adapter = HistoryAdapter(this, orderList) { order, isApproved ->
            handleOrderApproval(order, isApproved)
        }

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        binding.recyclerViewHistory.layoutManager = if (isLandscape) {
            GridLayoutManager(this, 2) // Grid layout with 2 columns for landscape
        } else {
            LinearLayoutManager(this) // Linear layout for portrait
        }
        binding.recyclerViewHistory.adapter = adapter
        adapter.notifyDataSetChanged()
        updateEmptyTextVisibility(orderList.isEmpty(), isFilter = false)

        binding.searchViewOrders.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    filterOrders(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    filterOrders(it)
                }
                return true
            }
        })

        // Filter Buttons Click Listeners
        binding.btnFilterAll.setOnClickListener { onFilterButtonClicked(it.id) { showAllOrders() } }
        binding.btnFilterPending.setOnClickListener { onFilterButtonClicked(it.id) { showPendingOrders() } }
        binding.btnFilterApproved.setOnClickListener { onFilterButtonClicked(it.id) { showApprovedOrders() } }
        binding.btnFilterRejected.setOnClickListener { onFilterButtonClicked(it.id) { showRejectedOrders() } }

        // Load orders from Firebase
        loadOrders()
        intentmenuju()

        // Set initial active button visual
        updateActiveButtonVisual(R.id.btnFilterAll) // Set "Semua" button as selected by default
    }


    private fun intentmenuju() {
        binding.keBeranda.setOnClickListener {
            startActivity(Intent(this@HistoryActivity, MainActivity::class.java))
        }
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this@HistoryActivity, CartActivity::class.java))
        }
        binding.keprofil.setOnClickListener {
            startActivity(Intent(this@HistoryActivity, ProfilActivity::class.java))
        }
    }

    private fun filterOrders(query: String) {
        val filteredList = orderList.filter { order ->
            order.namabarang.any { it.contains(query, ignoreCase = true) } ||
                    order.username.contains(query, ignoreCase = true) ||
                    order.id.contains(query, ignoreCase = true)        ||
                    order.jumlahsetiapitem.any { it.toString().contains(query, ignoreCase = true) } ||
                    order.hargabarang.any { it.toString().contains(query, ignoreCase = true) } ||
                    order.subtotalharga.toString().contains(query, ignoreCase = true) ||
                    order.pajakongkir.toString().contains(query, ignoreCase = true) ||
                    order.subtotalkeseluruhan.toString().contains(query, ignoreCase = true)
        }

        adapter.searchDataList(filteredList)

        updateEmptyTextVisibility(filteredList.isEmpty(), isFilter = true)
    }

    private fun loadOrders() {
        if (currentUserId == null) {
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        database.orderByChild("idUser").equalTo(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(OrdersModel::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }
                }
                adapter.notifyDataSetChanged()
                updateEmptyTextVisibility(orderList.isEmpty(), isFilter = false)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleOrderApproval(order: OrdersModel, isApproved: Boolean) {
        val updatedStatus = if (isApproved) "diizinkan" else "ditolak"
        database.child(order.id).child("perizinan").setValue(isApproved).addOnSuccessListener {
            Toast.makeText(this, "Pesanan ${order.id} telah $updatedStatus.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memperbarui status pesanan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmptyTextVisibility(isEmpty: Boolean, isFilter: Boolean) {
        binding.emptyTextFilter.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE

    }


    // Example: Filter Pending Orders
    private fun showPendingOrders() {
        val pendingOrders = orderList.filter { it.perizinan == null }
        adapter.searchDataList(pendingOrders)
        updateEmptyTextVisibility(pendingOrders.isEmpty(), isFilter = true)
    }

    // Example: Filter Approved Orders
    private fun showApprovedOrders() {
        val approvedOrders = orderList.filter { it.perizinan == true }
        adapter.searchDataList(approvedOrders)
        updateEmptyTextVisibility(approvedOrders.isEmpty(), isFilter = true)
    }

    // Example: Filter Rejected Orders
    private fun showRejectedOrders() {
        val rejectedOrders = orderList.filter { it.perizinan == false }
        adapter.searchDataList(rejectedOrders)
        updateEmptyTextVisibility(rejectedOrders.isEmpty(), isFilter = true)
    }

    // Example: Show All Orders
    private fun showAllOrders() {
        adapter.searchDataList(orderList)
        updateEmptyTextVisibility(orderList.isEmpty(), isFilter = false)
    }


    private fun onFilterButtonClicked(buttonId: Int, filterAction: () -> Unit) {
        if (activeButtonId != buttonId) {
            updateActiveButtonVisual(buttonId)
            filterAction()
        }
    }

    private fun updateActiveButtonVisual(buttonId: Int) {
        // Reset all buttons to their default state
        val buttonIds = listOf(
            R.id.btnFilterAll,
            R.id.btnFilterPending,
            R.id.btnFilterApproved,
            R.id.btnFilterRejected
        )

        buttonIds.forEach { id ->
            val button = findViewById<android.widget.Button>(id)
            if (id == buttonId) {
                // Set selected button style
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.tiga) // Warna tombol aktif
                button.setTextColor(ContextCompat.getColor(this, R.color.tekscategory)) // Warna teks tombol aktif
            } else {
                // Set unselected button style
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.teks)
                button.setTextColor(ContextCompat.getColor(this, R.color.tiga))
            }
        }

        // Update active button ID
        activeButtonId = buttonId
    }

}