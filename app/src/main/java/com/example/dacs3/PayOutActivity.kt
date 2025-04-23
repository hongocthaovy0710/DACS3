package com.example.dacs3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dacs3.databinding.ActivityPayOutBinding

class PayOutActivity : AppCompatActivity() {
    lateinit var binding: ActivityPayOutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPayOutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.PlaceMyOrder.setOnClickListener{
            val bottomSheetDeialog = CongratsBottomSheet()
            bottomSheetDeialog.show(supportFragmentManager,"Test")
        }

    }
}