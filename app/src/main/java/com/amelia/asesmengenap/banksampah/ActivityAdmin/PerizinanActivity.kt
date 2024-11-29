package com.amelia.asesmengenap.banksampah.ActivityAdmin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import com.amelia.asesmengenap.banksampah.AdapterAdmin.OrdersAdapter
import com.amelia.asesmengenap.banksampah.Domain.OrdersModel
import com.amelia.asesmengenap.banksampah.R
import com.amelia.asesmengenap.banksampah.databinding.ActivityPerizinanBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class PerizinanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerizinanBinding
    private lateinit var database: DatabaseReference
    private lateinit var orderList: ArrayList<OrdersModel>
    private lateinit var adapter: OrdersAdapter
    private var activeButtonId: Int = R.id.btnFilterAll // ID tombol aktif saat ini

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup ViewBinding
        binding = ActivityPerizinanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Database reference
        database = FirebaseDatabase.getInstance("https://banksampahamelia-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Pesanan")

        // Initialize list and adapter
        orderList = ArrayList()
        adapter = OrdersAdapter(this, orderList) { order, isApproved ->
            handleOrderApproval(order, isApproved)
        }

        // Setup RecyclerView
        binding.recyclerViewOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewOrders.adapter = adapter

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

        binding.backBtn.setOnClickListener {
            finish()
        }

        // Filter Buttons Click Listeners
        binding.btnFilterAll.setOnClickListener { onFilterButtonClicked(it.id) { showAllOrders() } }
        binding.btnFilterPending.setOnClickListener { onFilterButtonClicked(it.id) { showPendingOrders() } }
        binding.btnFilterApproved.setOnClickListener { onFilterButtonClicked(it.id) { showApprovedOrders() } }
        binding.btnFilterRejected.setOnClickListener { onFilterButtonClicked(it.id) { showRejectedOrders() } }

        // Load orders from Firebase
        loadOrders()

        // Set initial active button visual
        updateActiveButtonVisual(R.id.btnFilterAll)
    }

    private fun filterOrders(query: String) {
        val filteredList = orderList.filter { order ->
            order.namabarang.any { it.contains(query, ignoreCase = true) } || // Cari di nama barang
                    order.username.contains(query, ignoreCase = true) ||             // Cari di username
                    order.id.contains(query, ignoreCase = true) ||
                    order.jumlahsetiapitem.toString().contains(query, ignoreCase = true) ||
                    order.hargabarang.toString().contains(query, ignoreCase = true) ||
                    formatCurrency(order.subtotalharga).contains(query, ignoreCase = true) ||
                    formatCurrency(order.pajakongkir).contains(query, ignoreCase = true) ||
                    formatCurrency(order.subtotalkeseluruhan).contains(query, ignoreCase = true)
        }

        adapter.searchDataList(filteredList)

        updateEmptyTextVisibility(filteredList.isEmpty(), isFilter = true)
    }

    private fun loadOrders() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(OrdersModel::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PerizinanActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleOrderApproval(order: OrdersModel, isApproved: Boolean) {
        val updatedStatus = if (isApproved) "diizinkan" else "ditolak"
        database.child(order.id).child("perizinan").setValue(isApproved).addOnSuccessListener {
            Toast.makeText(this, "Pesanan ${order.username} telah $updatedStatus.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memperbarui status pesanan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmptyTextVisibility(isEmpty: Boolean, isFilter: Boolean) {
        binding.emptyTextFilter.visibility = if (isEmpty) View.VISIBLE else View.GONE
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

    private fun formatCurrency(value: Int): String {
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        return "Rp ${formatter.format(value)}"
    }
}
