package com.example.dacs3.Fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dacs3.RecentOrderItems
import com.example.dacs3.adaptar.BuyAgainAdapter
import com.example.dacs3.databinding.FragmentHistoryBinding
import com.example.dacs3.model.CartItems
import com.example.dacs3.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private var listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        retrieveBuyHistory()

        binding.recentbuyitem.setOnClickListener {
            seeItemsRecentBuy()
        }
        binding.receivedButton.setOnClickListener {
            updateOrderStatus()
        }

        return binding.root
    }

    private fun updateOrderStatus() {
        val itemPushKey = listOfOrderItem[0].itemPushKey
        val completeOrderReference = database.reference.child("CompletedOrder").child(itemPushKey!!)
        completeOrderReference.child("paymentReceived").setValue(true)
    }

    private fun seeItemsRecentBuy() {
        if (listOfOrderItem.isNotEmpty()) {
            val intent = Intent(requireContext(), RecentOrderItems::class.java)
            intent.putExtra("RecentBuyOrderItem", listOfOrderItem)
            startActivity(intent)
        }
    }

    private fun retrieveBuyHistory() {
        binding.recentbuyitem.visibility = View.INVISIBLE
        userId = auth.currentUser?.uid ?: ""

        val buyItemReference: DatabaseReference = database.reference.child("user").child(userId).child("BuyHistory")
        val shortingQuery = buyItemReference.orderByChild("currentTime")

        shortingQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (buySnapshot in snapshot.children) {
                    val buyHistoryItem = buySnapshot.getValue(OrderDetails::class.java)
                    buyHistoryItem?.let {
                        listOfOrderItem.add(it)
                    }
                }
                listOfOrderItem.reverse()
                if (listOfOrderItem.isNotEmpty()) {
                    setDataInRecentBuyItem()
                    setPreviousBuyItemsRecyclerView()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Không thể tải lịch sử mua hàng", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setDataInRecentBuyItem() {
        binding.recentbuyitem.visibility = View.VISIBLE
        val recentOrderItem = listOfOrderItem.firstOrNull()
        recentOrderItem?.let {
            with(binding) {
                buyAgainFoodName.text = it.foodNames?.firstOrNull() ?: ""
                buyAgainFoodPrice.text = it.foodPrices?.firstOrNull() ?: ""
                val image = it.foodImages?.firstOrNull() ?: ""
                Glide.with(requireContext()).load(image).into(buyAgainFoodImage)

                val isOrderIsAccepted = listOfOrderItem[0].orderAccepted
                Log.d("TAG", "setDataInRecentBuyItem: $isOrderIsAccepted")
                if (isOrderIsAccepted) {
                    orderStutus.background.setTint(Color.GREEN)
                    receivedButton.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setPreviousBuyItemsRecyclerView() {
        val buyAgainFoodName = mutableListOf<String>()
        val buyAgainFoodPrice = mutableListOf<String>()
        val buyAgainFoodImage = mutableListOf<String>()
        val orderIndexList = mutableListOf<Int>()

        for (i in 1 until listOfOrderItem.size) {
            listOfOrderItem[i].foodNames?.firstOrNull()?.let {
                buyAgainFoodName.add(it)
                orderIndexList.add(i)

                listOfOrderItem[i].foodPrices?.firstOrNull()?.let { price ->
                    buyAgainFoodPrice.add(price)

                    listOfOrderItem[i].foodImages?.firstOrNull()?.let { image ->
                        buyAgainFoodImage.add(image)
                    }
                }
            }
        }

        val rv = binding.BuyAgainRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())
        buyAgainAdapter = BuyAgainAdapter(buyAgainFoodName, buyAgainFoodPrice, buyAgainFoodImage, requireContext())

        buyAgainAdapter.setOnBuyAgainClickListener(object : BuyAgainAdapter.OnBuyAgainClickListener {
            override fun onBuyAgainClick(position: Int) {
                val orderIndex = orderIndexList[position]
                addToCart(orderIndex)
            }
        })

        buyAgainAdapter.setOnItemClickListener(object : BuyAgainAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val orderIndex = orderIndexList[position]
                showOrderDetails(orderIndex)
            }
        })

        rv.adapter = buyAgainAdapter
    }

    private fun showOrderDetails(orderIndex: Int) {
        val selectedOrder = listOfOrderItem[orderIndex]
        val intent = Intent(requireContext(), RecentOrderItems::class.java)
        val orderItemList = ArrayList<OrderDetails>()
        orderItemList.add(selectedOrder)
        intent.putExtra("RecentBuyOrderItem", orderItemList)
        startActivity(intent)
    }

    private fun addToCart(orderIndex: Int) {
        val selectedOrder = listOfOrderItem[orderIndex]

        if (selectedOrder.foodNames.isNullOrEmpty() ||
            selectedOrder.foodPrices.isNullOrEmpty() ||
            selectedOrder.foodImages.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Không thể thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show()
            return
        }

        userId = auth.currentUser?.uid ?: ""
        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để thực hiện chức năng này", Toast.LENGTH_SHORT).show()
            return
        }

        val cartRef = database.reference.child("cart").child(userId)
        var successCount = 0

        for (i in selectedOrder.foodNames!!.indices) {
            if (i < selectedOrder.foodPrices!!.size && i < selectedOrder.foodImages!!.size) {
                val cartItem = CartItems(
                    foodName = selectedOrder.foodNames!![i],
                    foodPrice = selectedOrder.foodPrices!![i],
                    foodImage = selectedOrder.foodImages!![i],
                    foodDescription = "",
                    foodIngredient = "",
                    foodQuantity = selectedOrder.foodQuantities?.getOrNull(i) ?: 1
                )

                val cartItemId = cartRef.push().key
                if (cartItemId != null) {
                    cartRef.child(cartItemId).setValue(cartItem)
                        .addOnSuccessListener {
                            successCount++
                            if (successCount == selectedOrder.foodNames!!.size) {
                                Toast.makeText(requireContext(), "Đã thêm tất cả sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Thêm sản phẩm vào giỏ hàng thất bại", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}