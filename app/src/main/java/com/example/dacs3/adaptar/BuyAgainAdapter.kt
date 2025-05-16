package com.example.dacs3.adaptar

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dacs3.databinding.BuyAgainItemBinding

class BuyAgainAdapter(
    private val buyAgainFoodName: MutableList<String>,
    private val buyAgainFoodPrice: MutableList<String>,
    private val buyAgainFoodImage: MutableList<String>,
    private var requireContext: Context
) : RecyclerView.Adapter<BuyAgainAdapter.BuyAgainViewHolder>() {

    // Interface để xử lý sự kiện khi click vào nút "Buy Again"
    interface OnBuyAgainClickListener {
        fun onBuyAgainClick(position: Int)
    }

    // Interface để xử lý sự kiện khi click vào item
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var buyAgainClickListener: OnBuyAgainClickListener? = null
    private var itemClickListener: OnItemClickListener? = null

    fun setOnBuyAgainClickListener(listener: OnBuyAgainClickListener) {
        this.buyAgainClickListener = listener
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        holder.bind(
            buyAgainFoodName[position],
            buyAgainFoodPrice[position],
            buyAgainFoodImage[position],
            position
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding =
            BuyAgainItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyAgainViewHolder(binding)
    }

    override fun getItemCount(): Int = buyAgainFoodName.size

    inner class BuyAgainViewHolder(private val binding: BuyAgainItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(foodName: String, foodPrice: String, foodImage: String, position: Int) {
            binding.buyAgainFoodName.text = foodName
            binding.buyAgainFoodPrice.text = foodPrice
            val uriString = foodImage
            val uri = Uri.parse(uriString)
            Glide.with(requireContext).load(uri).into(binding.buyAgainFoodImage)

            // Xử lý sự kiện khi click vào nút "Buy Again"
            binding.buyAgainFoodButton.setOnClickListener {
                buyAgainClickListener?.onBuyAgainClick(position)
            }

            // Xử lý sự kiện khi click vào item
            binding.root.setOnClickListener {
                itemClickListener?.onItemClick(position)
            }
        }
    }
}