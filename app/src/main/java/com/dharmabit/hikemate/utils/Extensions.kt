package com.dharmabit.hikemate.utils

import android.location.Location
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun Long.getFormattedStopWatchTime(includeMillis: Boolean = false): String {
    var milliseconds = this
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    milliseconds -= TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

    if (!includeMillis) {
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds"
    }
    milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
    milliseconds /= 10

    return "${if(hours < 10) "0" else ""}$hours:" +
            "${if(minutes < 10) "0" else ""}$minutes:" +
            "${if(seconds < 10) "0" else ""}$seconds:" +
            "${if(milliseconds < 10) "0" else ""}$milliseconds"
}

fun Float.formatDistance(): String {
    return if (this < 1000f) {
        "${(this * 10).toInt() / 10f} m"
    } else {
        "${(this / 100).toInt() / 10f} km"
    }
}

fun Float.formatSpeed(): String {
    return "${(this * 3.6 * 10).toInt() / 10f} km/h"
}

fun Date.formatDate(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(this)
}

fun Date.formatTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(this)
}

fun calculateDistance(locations: List<Location>): Float {
    var distance = 0f
    for (i in 1 until locations.size) {
        distance += locations[i-1].distanceTo(locations[i])
    }
    return distance
}

fun calculateAverageSpeed(distance: Float, timeInMillis: Long): Float {
    return if (timeInMillis > 0) {
        (distance / (timeInMillis / 1000f))
    } else 0f
}