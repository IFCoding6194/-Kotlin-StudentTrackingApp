package com.example.studenttrackingapp.DataClass

import android.graphics.Bitmap

data class MapView(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageBitmap: Bitmap? = null
)

