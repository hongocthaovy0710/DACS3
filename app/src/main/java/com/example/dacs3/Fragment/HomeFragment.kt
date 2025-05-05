package com.example.dacs3.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.dacs3.MenuBootomSheetFragment
import com.example.dacs3.R
import com.example.dacs3.adaptar.MenuAdapter
import com.example.dacs3.adaptar.PopulerAdapter
import com.example.dacs3.databinding.FragmentHomeBinding
import com.example.dacs3.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database:FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)

        binding.viewAllMenu.setOnClickListener{
            val bottomSheetDialog = MenuBootomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager,"Test")

        }

        // retraieve display popular menu items
        retraieveAndDisplayPopularItems()

        return binding.root


    }

    private fun retraieveAndDisplayPopularItems() {
        database =FirebaseDatabase.getInstance()
        val foodRef:DatabaseReference = database.reference.child("menu")
        menuItems = mutableListOf()

        foodRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children){
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let { menuItems.add(it) }

                }
                randomPopularItems()
            }



            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun randomPopularItems() {
        val index =menuItems.indices.toList().shuffled()
        val numItemToShow =6
        val subsetMenuItems = index.take(numItemToShow).map { menuItems[it] }

        setPupularItemsAdatper(subsetMenuItems)
    }

    private fun setPupularItemsAdatper(subsetMenuItems: List<MenuItem>) {
        val adapter = MenuAdapter(subsetMenuItems,requireContext())
        binding.PopulerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.PopulerRecyclerView.adapter= adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.banner1,ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner2,ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner3,ScaleTypes.FIT))

        val imageSlider = binding.imageSlider
        imageSlider.setImageList(imageList)
        imageSlider.setImageList(imageList,ScaleTypes.FIT)
        imageSlider.setItemClickListener(object :ItemClickListener{
            override fun doubleClick(position: Int) {
                TODO("Not yet implemented")
            }
// luot banner qua lai
            override fun onItemSelected(position: Int) {
                val itemPosition = imageList[position]
                val itemMessage = "Select Image $position"
                Toast.makeText(requireContext(),itemMessage,Toast.LENGTH_SHORT).show()
            }

        })


    }

}