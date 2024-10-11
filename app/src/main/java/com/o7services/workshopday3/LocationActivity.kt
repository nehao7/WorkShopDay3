package com.o7services.workshopday3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.Locale

class LocationActivity : AppCompatActivity() {

    var tvlocation:TextView?=null
    var tvlat:TextView?=null
    var tvlong:TextView?=null
    var btnSensor:Button?=null
    var pgbar:ProgressBar?=null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tvlocation=findViewById(R.id.tvlocation)
        tvlat=findViewById(R.id.tvLatitude)
        tvlong=findViewById(R.id.tvLongitude)
        btnSensor=findViewById(R.id.btnSensor)
        pgbar=findViewById(R.id.progress)

        if (checkPermissions()) {
            println("Permission checked")
            getLastLocation()
        } else {
          //  binding.progress.visibility = View.GONE
            requestPermissions()
        }
        btnSensor?.setOnClickListener {
            startActivity(Intent(this,SensorActivity::class.java))
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
           this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation()
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun getLastLocation() {
        pgbar?.visibility = View.VISIBLE
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                pgbar?.visibility=View.GONE
                if(location!=null){
                    var userLong = location.longitude
                    var userLat = location.latitude
                    Log.e("address", "onCreate:${getCompleteAddressString(userLat,userLong)} ", )
                    var address=getCompleteAddressString(userLat,userLong)
                    tvlocation?.setText(address)
                    tvlat?.setText(userLat.toString())
                    tvlong?.setText(userLong.toString())
                }
            }
    }
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if
                    (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]

                val addressString = address.getAddressLine(0) // Get the main address line

                // Remove the Place ID from the address string if it's present
                val placeIdIndex = addressString.indexOf(" ")
                if (placeIdIndex != -1) {
                    return addressString.substring(placeIdIndex + 1)
                } else {
                    return addressString
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "No address found"
    }

}