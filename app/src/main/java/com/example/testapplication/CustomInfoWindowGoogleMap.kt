package com.example.testapplication

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import java.util.*

class CustomInfoWindowGoogleMap(val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker): View? {

        val mInfoView = (context as Activity).layoutInflater.inflate(R.layout.custom_map_marker, null)
        val mInfoWindow = p0.tag as Photo?

        val date = Date().apply {
            date = mInfoWindow!!.data.toInt()
        }
        val format = java.text.SimpleDateFormat("dd.MM.yyyy")

        mInfoView.findViewById<TextView>(R.id.dateMap).text = format.format(date)
        mInfoView.findViewById<ImageView>(R.id.mapImage).setImageBitmap(mInfoWindow!!.image)

        return mInfoView
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }
}