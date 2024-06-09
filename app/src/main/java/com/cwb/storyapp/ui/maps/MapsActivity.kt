package com.cwb.storyapp.ui.maps

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cwb.storyapp.R
import com.cwb.storyapp.api.ApiConfig
import com.cwb.storyapp.api.Story
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.cwb.storyapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var storyRepository: StoryRepository
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra("TOKEN")

        if (token == null) {
            finish()
            return
        }

        val apiService = ApiConfig.getApiService(token!!)
        storyRepository = StoryRepository(apiService)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        lifecycleScope.launch {
            val stories = storyRepository.getStoriesWithLocation()
            addMarkers(stories)
        }

        getMyLocation()
    }

    private fun addMarkers(stories: List<Story>) {
        for (story in stories) {
            if (story.lat != null && story.lon != null) {
                val latLng = LatLng(story.lat, story.lon)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(story.name)
                    .snippet(story.description)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                mMap.addMarker(markerOptions)
            }
        }

        if (stories.isNotEmpty()) {
            val firstStory = stories[0]
            val firstLatLng = LatLng(firstStory.lat, firstStory.lon)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 10f))
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }
    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
