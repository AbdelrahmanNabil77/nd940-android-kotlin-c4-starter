package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.AppConstants
import com.udacity.project4.utils.AppConstants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.utils.AppConstants.REQUEST_BACKGROUND_LOCATION_PERMISSION
import com.udacity.project4.utils.AppConstants.REQUEST_ENABLE_GPS
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var geofencingClient: GeofencingClient

    val TAG = "SaveReminderFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            saveReminder()
        }
    }

    private fun saveReminder() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value
        val reminderDataItem = ReminderDataItem(
            title,
            description,
            location,
            latitude,
            longitude
        )

        if (_viewModel.validateEnteredData(reminderDataItem)) {
            if (arePermissionsAndLocationEnabled()) {
                _viewModel.saveReminder(reminderDataItem)
                addGeofence(reminderDataItem)
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(reminderDataItem: ReminderDataItem) {
        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId(reminderDataItem.id)
            // Set the circular region of this geofence.
            .setCircularRegion(
                _viewModel.latitude.value ?: 0.0,
                _viewModel.longitude.value ?: 0.0,
                GEOFENCE_RADIUS_IN_METERS
            )
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()
//        geofencingClient.removeGeofences(_viewModel.geofencePendingIntent("",""))
        // Add the new geofence request with the n•ew geofence
        geofencingClient.addGeofences(
            geofencingRequest,
            _viewModel.geofencePendingIntent(
                reminderDataItem.title ?: "",
                reminderDataItem.description ?: ""
            )
        )?.run {
            addOnSuccessListener {
                // Geofences added.
//                        Toast.makeText(
//                            requireContext(), getText(R.string.geofences_added),
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
                Log.e("Add Geofence", geofence.requestId)
                _viewModel.navigationCommand.postValue(
                    NavigationCommand.Back
                )
            }
            addOnFailureListener {
                // Failed to add geofences.
                Toast.makeText(
                    requireContext(), R.string.geofences_not_added,
                    Toast.LENGTH_SHORT
                ).show()
                if ((it.message != null)) {
                    Log.w(TAG, it.message!!)
                }
            }
        }
    }

    fun showSnackBarForDisabledLocation() {
        Snackbar.make(
            binding.root,
            R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.enable) {
                navigateToEnableGps()
            }.show()

    }

    fun navigateToEnableGps() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, REQUEST_ENABLE_GPS)
    }

    fun showSnackBarForBackgroundLocationPermissionDenied() {
        Snackbar.make(
            binding.root,
            R.string.background_permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.settings) {
                navigateToSettings()
            }.show()
    }

    fun navigateToSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun requestBackgroundLocationPermission() {
        requestPermissions(
            arrayOf<String>(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_BACKGROUND_LOCATION_PERMISSION
        )
    }

    fun showSnackBarForForegroundLocationPermissionDenied() {
        Snackbar.make(
            binding.root,
            R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.settings) {
                navigateToSettings()
            }.show()
    }

    private fun requestFineLocationPermission() {
        requestPermissions(
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            AppConstants.REQUEST_FINE_LOCATION_PERMISSION
        )
    }

    private fun isFineLocationPermissionGranted(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) === PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            requestFineLocationPermission()
            showSnackBarForForegroundLocationPermissionDenied()
            return false
        }
    }

    private fun isBackgroundLocationPermissionGranted(): Boolean {
        if (!runningQOrLater) {
            return true
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) === PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            requestBackgroundLocationPermission()
            showSnackBarForBackgroundLocationPermissionDenied()
            return false
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            return true
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
            return false
        }
    }

    fun arePermissionsAndLocationEnabled(): Boolean {
        return isFineLocationPermissionGranted()
                && isBackgroundLocationPermissionGranted()
                && isLocationEnabled()

    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_ENABLE_GPS,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                saveReminder()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_GPS) {
           saveReminder()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BACKGROUND_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                saveReminder()
            } else if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                showSnackBarForBackgroundLocationPermissionDenied()
            }
        }

        if (requestCode == AppConstants.REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                saveReminder()
            } else if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                showSnackBarForForegroundLocationPermissionDenied()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
