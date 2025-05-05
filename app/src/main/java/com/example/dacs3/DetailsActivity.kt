package com.example.dacs3

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dacs3.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private var foodName: String? = null
    private var foodPrice: String? = null
    private var foodDescriptions: String? = null
    private var foodImage: String? = null
    private var foodIngredients: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get data from intent
        foodName = intent.getStringExtra("MenuItemName")
        foodPrice = intent.getStringExtra("MenuItemPrice")
        foodDescriptions = intent.getStringExtra("MenuItemDescription")
        foodImage = intent.getStringExtra("MenuItemImage")
        foodIngredients = intent.getStringExtra("MenuItemIngredients")

        with(binding) {
            detailFoodName.text = foodName
            detailDescription.text = foodDescriptions
            detailIngredients.text = foodIngredients
            Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into(detailFoodImage)
        }


        binding.imageButton.setOnClickListener {
            finish()
        }
    }
}