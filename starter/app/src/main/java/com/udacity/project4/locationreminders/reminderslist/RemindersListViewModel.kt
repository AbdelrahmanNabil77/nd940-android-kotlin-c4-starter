package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.utils.AppConstants
import kotlinx.coroutines.launch

class RemindersListViewModel(
    val app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    })
                    remindersList.value = dataList
                }
                is Result.Error ->
                    showSnackBar.value = result.message
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }

    fun clearReminders(){
        showLoading.value = true
        clearGeoFence()
        viewModelScope.launch {
            dataSource.deleteAllReminders()
            showLoading.value = false
            loadReminders()
        }
    }

    fun clearGeoFence(){
        var geofencingClient = LocationServices.getGeofencingClient(app)
        geofencingClient.removeGeofences(geofencePendingIntent())
    }

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    fun geofencePendingIntent(title:String="",description: String=""): PendingIntent {
        val intent = Intent(app, GeofenceBroadcastReceiver::class.java)
        intent.action = AppConstants.ACTION_GEOFENCE_EVENT
        intent.putExtra(AppConstants.REMINDER_TITLE_TO_BROADCAST, title)
        intent.putExtra(AppConstants.REMINDER_DESC_TO_BROADCAST, description)
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getBroadcast(
            app,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}