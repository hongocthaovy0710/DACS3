package com.example.dacs3.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3.CongratsBottomSheet
import com.example.dacs3.PayOutActivity
import com.example.dacs3.R
import com.example.dacs3.adaptar.CartAdapter
import com.example.dacs3.databinding.FragmentCartBinding
import com.example.dacs3.model.CartItems
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var foodNames: MutableList<String>
    private lateinit var foodPrices: MutableList<String>
    private lateinit var foodDescriptions: MutableList<String>
    private lateinit var foodImagesUri: MutableList<String>
    private lateinit var foodIngredients: MutableList<String>
    private lateinit var quantity: MutableList<Int>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String
    private lateinit var binding: FragmentCartBinding
    private lateinit var cartReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        retrieveCartItems()

        binding.proceedButton.setOnClickListener {
            getOrderItemsDetails()
        }
        return binding.root
    }

    private fun getOrderItemsDetails() {
        val orderIdReference: DatabaseReference = database.reference.child("cart").child(userId)
        val foodName = mutableListOf<String>()
        val foodPrice = mutableListOf<String>()
        val foodImage = mutableListOf<String>()
        val foodDescription = mutableListOf<String>()
        val foodIngredient = mutableListOf<String>()
        val foodQuantities = cartAdapter.getUpdatedItemsQuantities()

        orderIdReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val orderItems = foodSnapshot.getValue(CartItems::class.java)
                    orderItems?.foodName?.let { foodName.add(it) }
                    orderItems?.foodPrice?.let { foodPrice.add(it) }
                    orderItems?.foodDescription?.let { foodDescription.add(it) }
                    orderItems?.foodImage?.let { foodImage.add(it) }
                    orderItems?.foodIngredient?.let { foodIngredient.add(it) }
                }
                orderNow(foodName, foodPrice, foodImage, foodDescription, foodIngredient, foodQuantities)
            }

            private fun orderNow(
                foodName: MutableList<String>,
                foodPrice: MutableList<String>,
                foodImage: MutableList<String>,
                foodDescription: MutableList<String>,
                foodIngredient: MutableList<String>,
                foodQuantities: MutableList<Int>
            ) {
                if (isAdded && context != null) {
                    val intent = Intent(requireContext(), PayOutActivity::class.java)
                    intent.putExtra("FoodItemName", foodName as ArrayList<String>)
                    intent.putExtra("FoodItemPrice", foodPrice as ArrayList<String>)
                    intent.putExtra("FoodItemImage", foodImage as ArrayList<String>)
                    intent.putExtra("FoodItemDescription", foodDescription as ArrayList<String>)
                    intent.putExtra("FoodItemIngredient", foodIngredient as ArrayList<String>)
                    intent.putExtra("FoodItemQuantities", foodQuantities as ArrayList<Int>)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Order making Failed. Please Try Again", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun retrieveCartItems() {
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""
        cartReference = database.reference.child("cart").child(userId)

        foodNames = mutableListOf()
        foodPrices = mutableListOf()
        foodDescriptions = mutableListOf()
        foodImagesUri = mutableListOf()
        foodIngredients = mutableListOf()
        quantity = mutableListOf()

        cartReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    foodNames.clear()
                    foodPrices.clear()
                    foodDescriptions.clear()
                    foodImagesUri.clear()
                    foodIngredients.clear()
                    quantity.clear()

                    for (foodSnapshot in snapshot.children) {
                        val cartItems = foodSnapshot.getValue(CartItems::class.java)
                        cartItems?.foodName?.let { foodNames.add(it) }
                        cartItems?.foodPrice?.let { foodPrices.add(it) }
                        cartItems?.foodDescription?.let { foodDescriptions.add(it) }
                        cartItems?.foodImage?.let { foodImagesUri.add(it) }
                        cartItems?.foodQuantity?.let { quantity.add(it) }
                        cartItems?.foodIngredient?.let { foodIngredients.add(it) }
                    }
                    setAdapter()
                } catch (e: Exception) {
                    Log.e("CartFragment", "Error in onDataChange: ${e.message}", e)
                    Toast.makeText(context, "Lỗi khi tải dữ liệu giỏ hàng", Toast.LENGTH_SHORT).show()
                }
            }

            private fun setAdapter() {
                try {
                    cartAdapter = CartAdapter(
                        requireContext(),
                        foodNames,
                        foodPrices,
                        foodImagesUri,
                        foodDescriptions,
                        quantity,
                        foodIngredients
                    )
                    binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.cartRecyclerView.adapter = cartAdapter
                } catch (e: Exception) {
                    Log.e("CartFragment", "Error in setAdapter: ${e.message}", e)
                    Toast.makeText(context, "Lỗi khi hiển thị giỏ hàng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartFragment", "Firebase error: ${error.message}")
                Toast.makeText(context, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
    }
}