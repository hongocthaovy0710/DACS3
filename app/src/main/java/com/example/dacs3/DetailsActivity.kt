package com.example.dacs3

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dacs3.databinding.ActivityDetailsBinding
import com.example.dacs3.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    // Khai báo các biến
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var auth: FirebaseAuth
    private var foodName: String? = null
    private var foodPrice: String? = null
    private var foodDescriptions: String? = null
    private var foodImage: String? = null
    private var foodIngredients: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo binding
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Lấy dữ liệu từ Intent
        foodName = intent.getStringExtra("MenuItemName")
        foodPrice = intent.getStringExtra("MenuItemPrice")
        foodDescriptions = intent.getStringExtra("MenuItemDescription")
        foodImage = intent.getStringExtra("MenuItemImage")
        foodIngredients = intent.getStringExtra("MenuItemIngredients")

        // Hiển thị thông tin món ăn
        with(binding) {
            detailFoodName.text = foodName
            detailDescription.text = foodDescriptions
            detailIngredients.text = foodIngredients
            if (foodImage != null) {
                Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into(detailFoodImage)
            }
        }

        // Xử lý sự kiện nút Back
        binding.imageButton.setOnClickListener {
            finish()
        }

        // Xử lý sự kiện nút Add to Cart
        binding.addItemButton.setOnClickListener {
            addItemToCart()
        }
    }

    // Thêm món ăn vào giỏ hàng
    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: return // Thoát nếu userId null

        // Tạo đối tượng CartItems
        val cartItem = CartItems(
            foodName = foodName,
            foodPrice = foodPrice,
            foodDescription = foodDescriptions,
            foodImage = foodImage,
            foodQuantity = 1,
            foodIngredient = foodIngredients ?: "" // Xử lý null cho foodIngredients
        )

        // Lưu vào Firebase dưới node "cart > userId"
        database.child("cart").child(userId).push().setValue(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Item added to cart successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add item to cart", Toast.LENGTH_SHORT).show()
            }
    }
}