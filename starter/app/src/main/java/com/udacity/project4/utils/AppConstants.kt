package com.udacity.project4.utils

import java.util.concurrent.TimeUnit

object AppConstants {
    const val USER_EMAIL = "userEmail"
    const val ACTION_GEOFENCE_EVENT = "action.ACTION_GEOFENCE_EVENT"
    const val REMINDER_TITLE_TO_BROADCAST = "REMINDER_TITLE_TO_BROADCAST"
    const val REMINDER_DESC_TO_BROADCAST = "REMINDER_DESC_TO_BROADCAST"
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
}