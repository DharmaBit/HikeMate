package com.dharmabit.hikemate.utils

object Constants {
    const val DATABASE_NAME = "hikemate_database"
    const val LOCATION_SERVICE_ID = 175
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val TIMER_UPDATE_INTERVAL = 50L

    const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    // Measurement units
    const val METERS_PER_MILE = 1609.344f
    const val METERS_PER_KILOMETER = 1000f
}