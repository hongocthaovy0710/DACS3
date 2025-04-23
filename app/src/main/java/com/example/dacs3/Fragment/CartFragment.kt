package com.example.dacs3.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3.CongratsBottomSheet
import com.example.dacs3.PayOutActivity
import com.example.dacs3.R
import com.example.dacs3.adaptar.CartAdapter
import com.example.dacs3.databinding.FragmentCartBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CartFragment : Fragment() {
    private  lateinit var binding: FragmentCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)



        val cartFoodName  = listOf("Burger","Sandwich","momo","item","sandwich","momo")
        val cartItemPrice = listOf("$5","$6","$8","$9","$10","$10")
        val cartImage = listOf(
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3,
            R.drawable.menu4,
            R.drawable.menu5,
            R.drawable.menu6
        )
        val adapter = CartAdapter(ArrayList(cartFoodName),ArrayList(cartItemPrice),ArrayList(cartImage))
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = adapter

        binding.proceedButton.setOnClickListener{
            val intent = Intent(requireContext(),PayOutActivity::class.java)
            startActivity(intent)
        }



        return binding.root
    }



    companion object {


    }
}