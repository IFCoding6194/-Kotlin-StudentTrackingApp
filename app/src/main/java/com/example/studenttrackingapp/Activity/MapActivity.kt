package com.example.studenttrackingapp.Activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.studenttrackingapp.R
import com.example.studenttrackingapp.databinding.ActivityMapBinding
import com.example.studenttrackingapp.databinding.ActivityMapViewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.Locale
import androidx.appcompat.widget.SearchView

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Status Bar Color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = resources.getColor(R.color.dark_color)

        binding.backButtonBtn.setOnClickListener {
            onBackPressed()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.addButton.setOnClickListener {
            selectedAddress?.let {
                binding.selectedLocationTv.text = it
                Toast.makeText(this, "Location added: $it", Toast.LENGTH_SHORT).show()
                // Optionally return the result to another activity
                val resultIntent = Intent()
                resultIntent.putExtra("address", it)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } ?: run {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }


        binding.correndLocationImg.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return@setOnClickListener
            }

            val locationManager =
                getSystemService(LOCATION_SERVICE) as android.location.LocationManager
            val isLocationEnabled =
                locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

            if (!isLocationEnabled) {
                Toast.makeText(
                    this,
                    "Please enable location services in your device.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        mMap.clear()
                        mMap.addMarker(
                            MarkerOptions().position(currentLatLng).title("Current Location")
                        )
                        getAddressFromLatLng(currentLatLng)
                    } ?: run {
                        Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        setupSearchView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
            }
        }

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            selectedLatLng = latLng
            getAddressFromLatLng(latLng)
        }
    }

    private fun setupSearchView() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.queryHint = "Search location"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchLocation(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun searchLocation(query: String) {
        val geocoder = Geocoder(this)
        try {
            val addressList = geocoder.getFromLocationName(query, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(latLng).title(address.getAddressLine(0)))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                selectedLatLng = latLng
                selectedAddress = address.getAddressLine(0)
                binding.selectedLocationTv.text = "Selected Location : " + selectedAddress
                Toast.makeText(this, selectedAddress, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Geocoding failed, please try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressFromLatLng(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                selectedAddress = addresses[0].getAddressLine(0)
                binding.selectedLocationTv.text = "Selected Location : " + selectedAddress
                Toast.makeText(this, selectedAddress, Toast.LENGTH_SHORT).show()
            } else {
                selectedAddress = null
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Geocoding failed, please try again", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap)
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
