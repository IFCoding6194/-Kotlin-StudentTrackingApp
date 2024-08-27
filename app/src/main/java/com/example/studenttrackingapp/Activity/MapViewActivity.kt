package com.example.studenttrackingapp.Activity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.studenttrackingapp.R
import com.example.studenttrackingapp.databinding.ActivityMapViewBinding
import com.example.studenttrackingapp.DataClass.MapView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference


class MapViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapViewBinding
    private lateinit var mapViewList: MutableList<MapView>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set Status Bar Color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = resources.getColor(R.color.statusbar_color)

        binding.backButtonBtn.setOnClickListener {
            onBackPressed()
        }

        mapViewList = mutableListOf()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("students")
        FirebaseApp.initializeApp(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->
            fetchStudentData { mapViewList ->
                if (mapViewList.isEmpty()) {
                    println("MapViewActivity : No students found in Firebase")
                }
                for (mapView in mapViewList) {
                    val studentLocation = LatLng(mapView.latitude, mapView.longitude)

                    val markerOptions =
                        MarkerOptions().position(studentLocation).title(mapView.name).icon(
                            BitmapDescriptorFactory.fromBitmap(
                                createCustomMarker(
                                    mapView.imageBitmap ?: getDefaultBitmap(), mapView.name
                                )
                            )
                        )

                    googleMap.addMarker(markerOptions)
                }

                if (mapViewList.isNotEmpty()) {
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(mapViewList[0].latitude, mapViewList[0].longitude), 10f
                        )
                    )
                }
            }
        }
    }

    private fun fetchStudentData(onDataFetched: (List<MapView>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            onDataFetched(emptyList())
            return
        }

        val ref = database.child("Users").child(userId).child("students")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println("MapViewActivity : Data snapshot  {$snapshot}")
                mapViewList.clear()
                if (snapshot.exists()) {
                    for (studentSnapshot in snapshot.children) {
                        val name = studentSnapshot.child("name").getValue(String::class.java) ?: ""
                        val location =
                            studentSnapshot.child("location").getValue(String::class.java) ?: ""
                        println("MapViewActivity  Geocoding location: {$location}")

                        val geocoder = Geocoder(this@MapViewActivity)
                        val addresses = geocoder.getFromLocationName(location, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val geocodedLocation = addresses[0]
                            val latitude = geocodedLocation.latitude
                            val longitude = geocodedLocation.longitude

                            val imageString = studentSnapshot.child("profileImageBase64")
                                .getValue(String::class.java)
                            val imageBitmap = decodeBase64ToBitmap(imageString)

                            val mapView = MapView(name, latitude, longitude, imageBitmap)
                            mapViewList.add(mapView)
                        } else {
                            println("MapViewActivity Could not geocode location: ${location}")
                        }
                    }
                    onDataFetched(mapViewList)
                } else {
                    onDataFetched(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase: Failed to read data: ${error.toException()}")
            }
        })
    }

    private fun createCustomMarker(studentImage: Bitmap, studentName: String): Bitmap {
        val markerView = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.custom_marker,
            null
        )

        val markerImage = markerView.findViewById<ImageView>(R.id.marker_image)
        val markerName = markerView.findViewById<TextView>(R.id.marker_name)

        markerImage.setImageBitmap(studentImage)
        markerName.text = studentName

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
        markerView.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(
            markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)
        return bitmap
    }

    private fun decodeBase64ToBitmap(encodedString: String?): Bitmap? {
        return encodedString?.let {
            val decodedString = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }

    private fun getDefaultBitmap(): Bitmap {
        return android.graphics.BitmapFactory.decodeResource(resources, R.drawable.bg_circle)
    }
}
