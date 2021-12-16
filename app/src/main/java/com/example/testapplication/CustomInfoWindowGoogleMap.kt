package com.example.testapplication

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.custom_map_marker.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomInfoWindowGoogleMap(val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker): View? {

        val mInfoView = (context as Activity).layoutInflater.inflate(R.layout.custom_map_marker, null)
        val mInfoWindow = p0.tag as Photo?

        val date = Date().apply {
            date = mInfoWindow!!.data.toInt()
        }
        val format = SimpleDateFormat(Constants.dateFormat)

        mInfoView.dateMap.text = format.format(date)
        mInfoView.mapImage.setImageBitmap(mInfoWindow!!.image)

        return mInfoView
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }
}