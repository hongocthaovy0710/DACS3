package com.example.dacs3.adaptar

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dacs3.databinding.BuyAgainItemBinding

class RecentBuyAdapter(
    private var context: Context,
    private var foodNameList: ArrayList<String>,
    private var foodImageList: ArrayList<String>,
    private var foodPriceList: ArrayList<String>,
    private var foodQuantityList: ArrayList<Int>
) : RecyclerView.Adapter<RecentBuyAdapter.RecentViewHolder>() {

    // Interface để xử lý sự kiện khi click vào nút "Buy Again"
    interface OnBuyAgainClickListener {
        fun onBuyAgainClick(position: Int)
    }

    private var buyAgainClickListener: OnBuyAgainClickListener? = null

    fun setOnBuyAgainClickListener(listener: OnBuyAgainClickListener) {
        this.buyAgainClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val binding = BuyAgainItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return RecentViewHolder(binding)
    }

    override fun getItemCount(): Int = foodNameList.size

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class RecentViewHolder(private val binding: BuyAgainItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                buyAgainFoodName.text = foodNameList[position]
                buyAgainFoodPrice.text = foodPriceList[position]

                // Hiển thị hình ảnh sử dụng Glide
                if (position < foodImageList.size) {
                    val uriString = foodImageList[position]
                    try {
                        val uri = Uri.parse(uriString)
                        Glide.with(context).load(uri).into(buyAgainFoodImage)
                    } catch (e: Exception) {
                        // Xử lý nếu không thể parse URI
                        e.printStackTrace()
                    }
                }

                // Thêm xử lý sự kiện khi click vào nút "Buy Again"
                buyAgainFoodButton.setOnClickListener {
                    buyAgainClickListener?.onBuyAgainClick(adapterPosition)
                }
            }
        }
    }
}