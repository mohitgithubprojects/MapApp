package com.example.mapboxapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Marker(val mMap: GoogleMap, val context: Context) {

    var firstTime: Boolean? = null
    var droneMarker: com.google.android.gms.maps.model.Marker?

    init {
        droneMarker = null
        firstTime = true
    }

    fun addMarker(bhopal: LatLng, title: String) {
        if (firstTime == true) {
            droneMarker = mMap.addMarker(
                MarkerOptions().position(bhopal).title(title)
                    .icon(bitmapDescriptorFromVector(context, R.drawable.ic_drone))
            )
            firstTime = false
        } else {
            droneMarker!!.position = bhopal
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

}