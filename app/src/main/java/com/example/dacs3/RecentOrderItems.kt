package com.example.dacs3

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3.adaptar.RecentBuyAdapter
import com.example.dacs3.databinding.ActivityRecentOrderItemsBinding
import com.example.dacs3.model.CartItems
import com.example.dacs3.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RecentOrderItems : AppCompatActivity() {
    private lateinit var binding: ActivityRecentOrderItemsBinding
    private var listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()
    private lateinit var adapter: RecentBuyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentOrderItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listOfOrderItem = intent.getSerializableExtra("RecentBuyOrderItem") as? ArrayList<OrderDetails> ?: arrayListOf()

        binding.backButton.setOnClickListener {
            finish()
        }

        setAdapter()
    }

    private fun setAdapter() {
        val foodNames = arrayListOf<String>()
        val foodImages = arrayListOf<String>()
        val foodPrices = arrayListOf<String>()
        val foodQuantities = arrayListOf<Int>()

        if (listOfOrderItem.isNotEmpty()) {
            val selectedOrder = listOfOrderItem[0]
            selectedOrder.foodNames?.let { names ->
                foodNames.addAll(names)
                selectedOrder.foodImages?.let { images ->
                    val minSize = minOf(names.size, images.size)
                    for (i in 0 until minSize) {
                        foodImages.add(images[i])
                    }
                    if (images.size < names.size) {
                        repeat(names.size - images.size) { foodImages.add("") }
                    }
                } ?: run { repeat(names.size) { foodImages.add("") } }

                selectedOrder.foodPrices?.let { prices ->
                    val minSize = minOf(names.size, prices.size)
                    for (i in 0 until minSize) {
                        foodPrices.add(prices[i])
                    }
                    if (prices.size < names.size) {
                        repeat(names.size - prices.size) { foodPrices.add("$0") }
                    }
                } ?: run { repeat(names.size) { foodPrices.add("$0") } }

                selectedOrder.foodQuantities?.let { quantities ->
                    val minSize = minOf(names.size, quantities.size)
                    for (i in 0 until minSize) {
                        foodQuantities.add(quantities[i])
                    }
                    if (quantities.size < names.size) {
                        repeat(names.size - quantities.size) { foodQuantities.add(1) }
                    }
                } ?: run { repeat(names.size) { foodQuantities.add(1) } }
            }
        } else {
            Toast.makeText(this, "Không tìm thấy chi tiết đơn hàng", Toast.LENGTH_SHORT).show()
        }

        adapter = RecentBuyAdapter(this, foodNames, foodImages, foodPrices, foodQuantities)
        adapter.setOnBuyAgainClickListener(object : RecentBuyAdapter.OnBuyAgainClickListener {
            override fun onBuyAgainClick(position: Int) {
                try {
                    if (position >= 0 && position < foodNames.size) {
                        addItemToCart(position)
                    } else {
                        Toast.makeText(this@RecentOrderItems, "Vị trí không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("RecentOrderItems", "Error in onBuyAgainClick: ${e.message}", e)
                    Toast.makeText(this@RecentOrderItems, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                }
            }
        })
        binding.recyclerViewRecentBuy.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRecentBuy.adapter = adapter
    }

    private fun addItemToCart(position: Int) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thực hiện chức năng này", Toast.LENGTH_SHORT).show()
            return
        }

        if (listOfOrderItem.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu đơn hàng", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val selectedOrder = listOfOrderItem[0]
            if (selectedOrder.foodNames.isNullOrEmpty() || position >= (selectedOrder.foodNames?.size ?: 0)) {
                Toast.makeText(this, "Dữ liệu sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show()
                return
            }

            val cartItem = CartItems(
                foodName = selectedOrder.foodNames?.getOrNull(position) ?: "",
                foodPrice = selectedOrder.foodPrices?.getOrNull(position) ?: "$0",
                foodImage = selectedOrder.foodImages?.getOrNull(position) ?: "",
                foodDescription = "",
                foodIngredient = "",
                foodQuantity = selectedOrder.foodQuantities?.getOrNull(position) ?: 1
            )

            val database = FirebaseDatabase.getInstance()
            val cartRef = database.reference.child("cart").child(userId)
            val cartItemId = cartRef.push().key

            if (cartItemId == null) {
                Toast.makeText(this, "Không thể tạo khóa cho sản phẩm", Toast.LENGTH_SHORT).show()
                return
            }

            cartRef.child(cartItemId).setValue(cartItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("RecentOrderItems", "Failed to add item to cart: ${e.message}", e)
                    Toast.makeText(this, "Thêm vào giỏ hàng thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("RecentOrderItems", "Error in addItemToCart: ${e.message}", e)
            Toast.makeText(this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
        }
    }
}