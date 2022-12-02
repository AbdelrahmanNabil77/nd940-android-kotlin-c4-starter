package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.AppConstants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.AppConstants.REMINDER_DESC_TO_BROADCAST
import com.udacity.project4.utils.AppConstants.REMINDER_TITLE_TO_BROADCAST
import com.udacity.project4.utils.sendNotification

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    val TAG = "GeoFenceReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                Toast.makeText(context,"Geofence error: ${geofencingEvent.errorCode}",
                Toast.LENGTH_LONG).show()
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(TAG,"ON RECEIVE lat: ${geofencingEvent.triggeringLocation.latitude} " +
                        "lon: ${geofencingEvent.triggeringLocation.longitude}")
                val fenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences[0].requestId
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                sendNotification(context,
                    ReminderDataItem("title","Desc","${fenceId}",
                    geofencingEvent.triggeringLocation.latitude,geofencingEvent.triggeringLocation.longitude))
            }
        }

    }
}