package com.example.dacs3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dacs3.databinding.FragmentMenuBootomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MenuBootomSheetFragment :BottomSheetDialogFragment() {
        private lateinit var binding:FragmentMenuBootomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMenuBootomSheetBinding.inflate(inflater,container,false)

        binding.buttonBack.setOnClickListener{
                dismiss()
        }

//        val menuFoodName = listOf()

        return binding.root
    }

    companion object {

    }
}