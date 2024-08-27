package com.example.studenttrackingapp.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studenttrackingapp.R
import com.example.studenttrackingapp.Utils.ImageUtils
import com.example.studenttrackingapp.databinding.ActivityStudentDetailsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.location.Geocoder
import android.location.Address
import java.util.Locale

class StudentDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityStudentDetailsBinding
    private lateinit var googleMap: GoogleMap
    private var location: String? = null
    private var studentImage: String? = null
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Status Bar Color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = resources.getColor(R.color.dark_color)

        geocoder = Geocoder(this, Locale.getDefault())

        val studentName = intent.getStringExtra("name")
        val standard = intent.getStringExtra("classNumber")
        val section = intent.getStringExtra("section")
        val school = intent.getStringExtra("schoolName")
        val gender = intent.getStringExtra("gender")
        val dob = intent.getStringExtra("dob")
        val bloodGroup = intent.getStringExtra("bloodGroup")
        val fatherName = intent.getStringExtra("fatherName")
        val motherName = intent.getStringExtra("motherName")
        val contactNo = intent.getStringExtra("parentContactNo")
        val emergencyContactNo = intent.getStringExtra("emergencyNumber")
        val address1 = intent.getStringExtra("address1")
        val address2 = intent.getStringExtra("address2")
        val city = intent.getStringExtra("city")
        val state = intent.getStringExtra("state")
        val zip = intent.getStringExtra("zip")
        location = intent.getStringExtra("location")
        studentImage = intent.getStringExtra("studentImg")

        println("StudentDetailsActivity location : {$location}")

        binding.studentNameTv.text = studentName
        binding.standardTv.text = standard
        binding.sectionTv.text = section
        binding.schoolTv.text = school
        binding.genderTv.text = gender
        binding.dobTv.text = dob
        binding.bloodTv.text = bloodGroup
        binding.fatherNameTv.text = fatherName
        binding.motherNameTv.text = motherName
        binding.contactNoTv.text = contactNo
        binding.emergencyContactNoTv.text = emergencyContactNo
        binding.address1Tv.text = address1
        binding.address2Tv.text = address2
        binding.cityTv.text = city
        binding.stateTv.text = state
        binding.zipTv.text = zip

        val profileImageBase64 = studentImage
        if (profileImageBase64 != null && profileImageBase64.isNotEmpty()) {
            val bitmap = ImageUtils.decodeBase64(profileImageBase64)
            Glide.with(this)
                .load(bitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.studentImg)
        } else {
            binding.studentImg.setImageResource(R.drawable.profile)
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.backButton.setOnClickListener {
            onBackPressed()
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Convert address to LatLng
        val latLng = getLatLngFromAddress(location)

        if (latLng != null) {
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title("Student Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

            googleMap.addMarker(markerOptions)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            googleMap.setOnMarkerClickListener { marker ->
                showLocationDetails()
                true
            }
        } else {
            Toast.makeText(this, "Unable to parse location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLatLngFromAddress(address: String?): LatLng? {
        return try {
            address?.let {
                val addressList: List<Address>? = geocoder.getFromLocationName(it, 1)
                if (addressList != null && addressList.isNotEmpty()) {
                    val location = addressList[0]
                    LatLng(location.latitude, location.longitude)
                } else {
                    println("StudentDetailsActivity  : Address not found")
                    null
                }
            }
        } catch (e: Exception) {
            println("StudentDetailsActivity  : Error parsing address{$e}")
            null
        }
    }

    private fun showLocationDetails() {
        Toast.makeText(this, "Location: $location", Toast.LENGTH_LONG).show()
    }
}
