package com.example.dacs3

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3.adaptar.MenuAdapter
import com.example.dacs3.databinding.FragmentMenuBootomSheetBinding
import com.example.dacs3.model.MenuItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MenuBootomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentMenuBootomSheetBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMenuBootomSheetBinding.inflate(inflater, container, false)

        binding.buttonBack.setOnClickListener {
            dismiss()
        }

        retrieveMenuItems()

        return binding.root
    }

    private fun retrieveMenuItems() {
        database = FirebaseDatabase.getInstance()
        val foodRef = database.reference.child("menu")
        menuItems = mutableListOf()

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let { menuItems.add(it) }
                }
                Log.d("ITEMS", "onDataChange: Data Received")
                //once data receive, set to adapter
                setAdapter()
            }



            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    private fun setAdapter() {
        if (menuItems.isNotEmpty()){
            val adapter = MenuAdapter(menuItems, requireContext())
            binding.menuRecycleView.layoutManager = LinearLayoutManager(requireContext())
            binding.menuRecycleView.adapter = adapter
            Log.d("ITEMS", "setAdapter: data set")
        }else{
            Log.d("ITEMS", "setAdapter: data NOT set")
        }
    }

    companion object {

    }

}