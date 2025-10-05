package com.dharmabit.hikemate.utils

import android.Manifest
import android.content.Context
import android.location.Location
import pub.devrel.easypermissions.EasyPermissions

object LocationUtils {

    fun hasLocationPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    fun calculateElevationGain(locations: List<Location>): Float {
        if (locations.size < 2) return 0f

        var totalGain = 0f
        for (i in 1 until locations.size) {
            val elevationDiff = locations[i].altitude - locations[i-1].altitude
            if (elevationDiff > 0) {
                totalGain += elevationDiff.toFloat()
            }
        }
        return totalGain
    }

    fun calculateMaxElevation(locations: List<Location>): Double {
        return locations.maxOfOrNull { it.altitude } ?: 0.0
    }

    fun calculateMinElevation(locations: List<Location>): Double {
        return locations.minOfOrNull { it.altitude } ?: 0.0
    }
}