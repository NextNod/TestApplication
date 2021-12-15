package com.example.testapplication.ui.main.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.CustomInfoWindowGoogleMap
import com.example.testapplication.LocalData
import com.example.testapplication.R
import com.example.testapplication.databinding.FragmentGalleryBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync { map ->
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            map.clear()

            val customInfoWindow = CustomInfoWindowGoogleMap(root.context)
            map.setInfoWindowAdapter(customInfoWindow)

            LocalData.apply {
                val ltln = LatLng(list!![0].lat, list!![0].lng)
                map.moveCamera(CameraUpdateFactory.newLatLng(ltln))
            }

            LocalData.list!!.forEach {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.lat, it.lng))
                        .title("Pricol")
                )!!.tag = it
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}