package com.dharmabit.hikemate.activities

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.dharmabit.hikemate.R
import com.dharmabit.hikemate.data.database.HikeDatabase
import com.dharmabit.hikemate.data.database.entities.Hike
import com.dharmabit.hikemate.data.database.entities.TrackPoint
import com.dharmabit.hikemate.data.repository.HikeRepository
import com.dharmabit.hikemate.databinding.ActivityRecordHikeBinding
import com.dharmabit.hikemate.services.LocationService
import com.dharmabit.hikemate.services.Polylines
import com.dharmabit.hikemate.utils.Constants.ACTION_PAUSE_SERVICE
import com.dharmabit.hikemate.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.dharmabit.hikemate.utils.Constants.ACTION_STOP_SERVICE
import com.dharmabit.hikemate.utils.Constants.MAPVIEW_BUNDLE_KEY
import com.dharmabit.hikemate.utils.LocationUtils
import com.dharmabit.hikemate.utils.calculateAverageSpeed
import com.dharmabit.hikemate.utils.calculateDistance
import com.dharmabit.hikemate.utils.formatDistance
import com.dharmabit.hikemate.utils.formatSpeed
import com.dharmabit.hikemate.utils.getFormattedStopWatchTime
import com.dharmabit.hikemate.viewmodels.HikeViewModel
import java.util.*
import com.dharmabit.hikemate.services.Polyline
import com.dharmabit.hikemate.viewmodels.ViewModelFactory
import com.dharmabit.hikemate.utils.formatDate

class RecordHikeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityRecordHikeBinding
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    private var currentDistance = 0f
    private var currentSpeed = 0f
    private var maxSpeed = 0f
    private var allLocations = mutableListOf<Location>()

    private lateinit var hikeViewModel: HikeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordHikeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Record Hike"

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize ViewModel
        val database = HikeDatabase.getDatabase(this)
        val repository = HikeRepository(database.getHikeDao())
        hikeViewModel = ViewModelProvider(this, ViewModelFactory(repository))[HikeViewModel::class.java]

        // Save map state
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        subscribeToObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.btnFinishRun.setOnClickListener {
            showFinishRunDialog()
        }
    }

    private fun subscribeToObservers() {
        LocationService.isTracking.observe(this) {
            updateTracking(it)
        }

        LocationService.pathPoints.observe(this) {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        }

        LocationService.timeRunInMillis.observe(this) {
            curTimeInMillis = it
            val formattedTime = curTimeInMillis.getFormattedStopWatchTime()
            binding.tvTimer.text = formattedTime
        }

        LocationService.totalDistance.observe(this) { distance ->
            currentDistance = distance
            binding.tvDistance.text = distance.formatDistance()
        }

        LocationService.currentLocation.observe(this) { location ->
            location?.let {
                allLocations.add(it)
                currentSpeed = it.speed
                if (currentSpeed > maxSpeed) {
                    maxSpeed = currentSpeed
                }
                binding.tvSpeed.text = currentSpeed.formatSpeed()
            }
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            binding.btnToggleRun.text = "Resume"
            binding.btnFinishRun.visibility = android.view.View.VISIBLE
        } else if (isTracking) {
            binding.btnToggleRun.text = "Pause"
            binding.btnFinishRun.visibility = android.view.View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            val lastLocation = pathPoints.last().last()
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(lastLocation.latitude, lastLocation.longitude),
                    15f
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(LatLng(pos.latitude, pos.longitude))
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = LatLng(
                pathPoints.last()[pathPoints.last().size - 2].latitude,
                pathPoints.last()[pathPoints.last().size - 2].longitude
            )
            val lastLatLng = LatLng(
                pathPoints.last().last().latitude,
                pathPoints.last().last().longitude
            )
            val polylineOptions = PolylineOptions()
                .color(getColor(R.color.colorPrimary))
                .width(8f)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(getColor(R.color.colorPrimary))
                .width(8f)
            for (pos in polyline) {
                polylineOptions.add(LatLng(pos.latitude, pos.longitude))
            }
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(this, LocationService::class.java).also {
            it.action = action
            startService(it)
        }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        addAllPolylines()
    }

    private fun showFinishRunDialog() {
        AlertDialog.Builder(this)
            .setTitle("Finish Hike")
            .setMessage("Are you sure you want to finish this hike?")
            .setPositiveButton("Yes") { _, _ ->
                finishRun()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun finishRun() {
        sendCommandToService(ACTION_STOP_SERVICE)

        if (allLocations.isNotEmpty() && curTimeInMillis > 0L) {
            val distance = calculateDistance(allLocations)
            val avgSpeed = calculateAverageSpeed(distance, curTimeInMillis)
            val elevationGain = LocationUtils.calculateElevationGain(allLocations)
            val maxElevation = LocationUtils.calculateMaxElevation(allLocations)
            val minElevation = LocationUtils.calculateMinElevation(allLocations)

            // Calculate estimated calories (rough estimation)
            val calories = ((distance / 1000) * 60).toInt() // 60 calories per km

            val hike = Hike(
                name = "Hike ${Date().formatDate()}",
                date = Date(),
                duration = curTimeInMillis,
                distance = distance,
                averageSpeed = avgSpeed,
                maxSpeed = maxSpeed,
                elevationGain = elevationGain,
                maxElevation = maxElevation,
                minElevation = minElevation,
                calories = calories
            )

            val trackPoints = allLocations.map { location ->
                TrackPoint(
                    hikeId = 0, // This Will be updated in ViewModel
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    timestamp = Date(location.time),
                    speed = location.speed,
                    accuracy = location.accuracy
                )
            }

            hikeViewModel.insertHike(hike, trackPoints)
        }

        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_record_hike, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_center_map -> {
                moveCameraToUser()
                true
            }
            R.id.action_zoom_out -> {
                zoomToSeeWholeTrack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (isTracking) {
            AlertDialog.Builder(this)
                .setTitle("Stop Recording?")
                .setMessage("Your current hike will be lost. Are you sure you want to stop?")
                .setPositiveButton("Yes") { _, _ ->
                    sendCommandToService(ACTION_STOP_SERVICE)
                    super.onBackPressed()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}