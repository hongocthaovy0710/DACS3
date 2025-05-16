package com.example.dacs3.adaptar

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.dacs3.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<String>,
    private val cartItemPrices: MutableList<String>,
    private var cartImages: MutableList<String>,
    private var cartDescriptions: MutableList<String>,
    private val cartQuantity: MutableList<Int>,
    private var cartIngredient: MutableList<String>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    private val cartItemsReference = database.reference.child("cart").child(userId)

    init {
        val cartItemNumber = cartItems.size
        itemQuantities = IntArray(cartItemNumber) { i -> cartQuantity.getOrNull(i) ?: 1 }
    }

    companion object {
        var itemQuantities: IntArray = intArrayOf()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size

    fun getUpdatedItemsQuantities(): MutableList<Int> {
        val itemQuantity = mutableListOf<Int>()
        for (i in itemQuantities.indices) {
            itemQuantity.add(itemQuantities[i])
        }
        return itemQuantity
    }

    inner class CartViewHolder(private val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val quantity = itemQuantities[position]
                cartFoodName.text = cartItems[position]
                cartItemPrice.text = cartItemPrices[position]

                val uriString = cartImages[position]
                if (uriString.isNotEmpty()) {
                    try {
                        val uri = Uri.parse(uriString)
                        Glide.with(context).load(uri).into(cartImage)
                    } catch (e: Exception) {
                        Log.e("CartAdapter", "Error loading image: ${e.message}")
                        cartImage.setImageResource(android.R.drawable.ic_menu_report_image)
                    }
                } else {
                    cartImage.setImageResource(android.R.drawable.ic_menu_report_image)
                }
                cartItemQuantity.text = quantity.toString()

                minusbutton.setOnClickListener { decreaseQuantity(position) }
                plusebutton.setOnClickListener { increaseQuantity(position) }
                deleteButton.setOnClickListener {
                    val itemPosition = adapterPosition
                    if (itemPosition != RecyclerView.NO_POSITION) {
                        deleteItem(itemPosition)
                    }
                }
            }
        }

        private fun increaseQuantity(position: Int) {
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++
                cartQuantity[position] = itemQuantities[position]
                binding.cartItemQuantity.text = itemQuantities[position].toString()
                updateQuantityInFirebase(position)
            }
        }

        private fun decreaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                cartQuantity[position] = itemQuantities[position]
                binding.cartItemQuantity.text = itemQuantities[position].toString()
                updateQuantityInFirebase(position)
            }
        }

        private fun updateQuantityInFirebase(position: Int) {
            cartItemsReference.orderByChild("foodName").equalTo(cartItems[position]).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.child("foodQuantity").setValue(cartQuantity[position])
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartAdapter", "Failed to update quantity: ${error.message}")
                }
            })
        }

        private fun deleteItem(position: Int) {
            getUniqueKeyAtPosition(position) { uniqueKey ->
                if (uniqueKey != null) {
                    removeItem(position, uniqueKey)
                } else {
                    Toast.makeText(context, "Không tìm thấy khóa để xóa", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun removeItem(position: Int, uniqueKey: String) {
            cartItemsReference.child(uniqueKey).removeValue()
                .addOnSuccessListener {
                    if (position < cartItems.size) {
                        cartItems.removeAt(position)
                        cartImages.removeAt(position)
                        cartDescriptions.removeAt(position)
                        cartQuantity.removeAt(position)
                        cartItemPrices.removeAt(position)
                        cartIngredient.removeAt(position)
                        itemQuantities = itemQuantities.filterIndexed { index, _ -> index != position }.toIntArray()
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, cartItems.size)
                        Toast.makeText(context, "Đã xóa món ăn", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Xóa món ăn thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun getUniqueKeyAtPosition(position: Int, onComplete: (String?) -> Unit) {
            cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var uniqueKey: String? = null
                    var currentIndex = 0
                    for (dataSnapshot in snapshot.children) {
                        if (currentIndex == position) {
                            uniqueKey = dataSnapshot.key
                            break
                        }
                        currentIndex++
                    }
                    onComplete(uniqueKey)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartAdapter", "Error getting unique key: ${error.message}")
                    onComplete(null)
                }
            })
        }
    }
}