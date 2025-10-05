package com.dharmabit.hikemate.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import com.dharmabit.hikemate.databinding.ActivityHikeDetailBinding
import com.dharmabit.hikemate.utils.formatDate
import com.dharmabit.hikemate.utils.formatDistance
import com.dharmabit.hikemate.utils.formatSpeed
import com.dharmabit.hikemate.utils.formatTime
import com.dharmabit.hikemate.utils.getFormattedStopWatchTime
import com.dharmabit.hikemate.viewmodels.HikeViewModel
import kotlinx.coroutines.launch
import com.dharmabit.hikemate.viewmodels.ViewModelFactory

class HikeDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityHikeDetailBinding
    private var map: GoogleMap? = null
    private lateinit var hikeViewModel: HikeViewModel
    private var currentHike: Hike? = null
    private var trackPoints = listOf<TrackPoint>()

    companion object {
        const val EXTRA_HIKE_ID = "EXTRA_HIKE_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHikeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Hike Details"

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize ViewModel
        val database = HikeDatabase.getDatabase(this)
        val repository = HikeRepository(database.getHikeDao())
        hikeViewModel = ViewModelProvider(this, ViewModelFactory(repository))[HikeViewModel::class.java]

        val hikeId = intent.getLongExtra(EXTRA_HIKE_ID, -1)
        if (hikeId != -1L) {
            loadHikeDetails(hikeId)
        }
    }

    private fun loadHikeDetails(hikeId: Long) {
        lifecycleScope.launch {
            currentHike = hikeViewModel.getHikeById(hikeId)
            trackPoints = hikeViewModel.getTrackPointsByHikeId(hikeId)

            currentHike?.let { hike ->
                displayHikeDetails(hike)
                drawRoute()
            }
        }
    }

    private fun displayHikeDetails(hike: Hike) {
        binding.apply {
            tvHikeName.text = hike.name
            tvHikeDate.text = "${hike.date.formatDate()} at ${hike.date.formatTime()}"
            tvDuration.text = hike.duration.getFormattedStopWatchTime()
            tvDistance.text = hike.distance.formatDistance()
            tvAvgSpeed.text = hike.averageSpeed.formatSpeed()
            tvMaxSpeed.text = hike.maxSpeed.formatSpeed()
            tvElevationGain.text = "${hike.elevationGain.toInt()} m"
            tvMaxElevation.text = "${hike.maxElevation.toInt()} m"
            tvMinElevation.text = "${hike.minElevation.toInt()} m"
            tvCalories.text = "${hike.calories} kcal"
        }
    }

    private fun drawRoute() {
        if (trackPoints.isNotEmpty()) {
            val polylineOptions = PolylineOptions()
                .color(getColor(R.color.colorPrimary))
                .width(8f)

            val boundsBuilder = LatLngBounds.Builder()

            trackPoints.forEach { trackPoint ->
                val latLng = LatLng(trackPoint.latitude, trackPoint.longitude)
                polylineOptions.add(latLng)
                boundsBuilder.include(latLng)
            }

            map?.addPolyline(polylineOptions)

            // Zoom to show entire route
            val bounds = boundsBuilder.build()
            val padding = 100
            map?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        drawRoute()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_hike_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_delete_hike -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Hike")
            .setMessage("Are you sure you want to delete this hike? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                currentHike?.let { hike ->
                    hikeViewModel.deleteHike(hike)
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}