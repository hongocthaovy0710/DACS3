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

    init {
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        val cartItemNumber = cartItems.size

        itemQuantities = IntArray(cartItemNumber) { 1 }
        cartItemsReference = database.reference.child("user").child(userId).child("CartItems")
    }
    companion object {
        private var itemQuantities: IntArray = intArrayOf()
        private lateinit var cartItemsReference: DatabaseReference
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartImages.size



    fun getUpdatedItemsQuantities(): MutableList<Int> {
        val itemQuantity = mutableListOf<Int>()
        itemQuantity.addAll(cartQuantity)
        return itemQuantity


    }


    inner class CartViewHolder(private val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val quantity = itemQuantities[position]
                cartFoodName.text = cartItems[position]
                cartItemPrice.text = cartItemPrices[position]



                //load image using glide
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
                Log.d("CartAdapter", "uriString: $uriString")
                cartItemQuantity.text = quantity.toString()

                minusbutton.setOnClickListener {
                    deceaseQuantity(position)

                }
                plusebutton.setOnClickListener {
                    increaseQuantity(position)

                }
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
            }

        }

        private fun deceaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                cartQuantity[position] = itemQuantities[position]
                binding.cartItemQuantity.text = itemQuantities[position].toString()
            }

        }

        private fun deleteItem(position: Int) {
           val positionRetriever = position
            getUniqueKeyAtPosition(positionRetriever){ uniqueKey ->
                if (uniqueKey != null) {
                    removeItem(position, uniqueKey)
                }


            }
        }

        private fun removeItem(position: Int, uniqueKey: String) {
            if (uniqueKey != null) {
                cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
                    cartItems.removeAt(position)
                    cartImages.removeAt(position)
                    cartDescriptions.removeAt(position)
                    cartQuantity.removeAt(position)
                    cartItemPrices.removeAt(position)
                    cartIngredient.removeAt(position)
                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show()

                    // update itemQuantities
                    itemQuantities = itemQuantities.filterIndexed { index, i -> index != position }.toIntArray()
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartItems.size)


                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                }

            }
        }

        private fun getUniqueKeyAtPosition(positionRetriever: Int , onComplete: (String?) -> Unit) {
            cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var uniqueKey: String? = null
                    //loop for snap shot children
                    snapshot.children.forEachIndexed{
                        index, dataSnapshot ->
                        if (index == positionRetriever) {
                            uniqueKey = dataSnapshot.key
                            return@forEachIndexed
                        }
                    }
                    onComplete(uniqueKey)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            } )
        }

    }
}