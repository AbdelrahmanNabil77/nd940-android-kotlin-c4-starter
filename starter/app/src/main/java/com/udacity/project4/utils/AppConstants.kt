package com.udacity.project4.utils

import java.util.concurrent.TimeUnit

object AppConstants {
    const val USER_EMAIL = "userEmail"
    const val ACTION_GEOFENCE_EVENT = "action.ACTION_GEOFENCE_EVENT"
    const val REMINDER_TITLE_TO_BROADCAST = "REMINDER_TITLE_TO_BROADCAST"
    const val REMINDER_DESC_TO_BROADCAST = "REMINDER_DESC_TO_BROADCAST"
    const val GEOFENCE_RADIUS_IN_METERS = 100f

    const val REQUEST_FINE_LOCATION_PERMISSION = 2
    const val REQUEST_BACKGROUND_LOCATION_PERMISSION = 3
    const val REQUEST_ENABLE_GPS = 4
    const val INTENT_TO_SETTINGS=5
}