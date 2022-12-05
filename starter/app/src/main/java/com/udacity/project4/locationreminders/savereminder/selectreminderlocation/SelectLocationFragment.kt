package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.AppConstants.INTENT_TO_SETTINGS
import com.udacity.project4.utils.AppConstants.REQUEST_ENABLE_GPS
import com.udacity.project4.utils.AppConstants.REQUEST_FINE_LOCATION_PERMISSION
import com.udacity.project4.utils.DialogButtons
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.showAlertDialog
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    val TAG = "SelectLocationFragment"
    private lateinit var map: GoogleMap

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var selectedPosition = LatLng(0.0, 0.0)
    private var selectedLocationTag = ""

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.confirmPoiButton.setOnClickListener {
            onLocationSelected()
            requireActivity().onBackPressed()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.latitude.postValue(selectedPosition.latitude)
        _viewModel.longitude.postValue(selectedPosition.longitude)
        _viewModel.reminderSelectedLocationStr.postValue(selectedLocationTag)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)
        if (isLocationEnabled()) {
            enableMyLocation()
            getCurrentLocation()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun isFineLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isFineLocationPermissionGranted()) {
            map.setMyLocationEnabled(true)
        } else {
            requestFineLocationPermission()
        }
    }

    private fun requestFineLocationPermission() {
        requestPermissions(
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_FINE_LOCATION_PERMISSION
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_GPS) {
            if (isLocationEnabled()) {
                enableMyLocation()
                getCurrentLocation()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
                getCurrentLocation()
            } else if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                showSnackBarForLocationPermissionDenied()
            }
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
            showSnackBarForDisabledLocation()
            return false
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (isFineLocationPermissionGranted()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                val location: Location? = task.result
                if (location != null) {
                    selectedPosition = LatLng(location.latitude, location.longitude)
                    selectedLocationTag = getString(R.string.dropped_pin)
                    onLocationSelected()
                    val zoom = 15f
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPosition, zoom))
                    map.addMarker(
                        MarkerOptions()
                            .position(selectedPosition)
                    )
                }
            }
        } else {
            requestFineLocationPermission()
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            selectedPosition = poi.latLng
            selectedLocationTag = poi.name
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.clear()
            selectedPosition = latLng
            selectedLocationTag = getString(R.string.dropped_pin)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)

            )
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

    fun showSnackBarForLocationPermissionDenied() {
        val snackbar = Snackbar.make(
            binding.root,
            R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.settings) {
                navigateToSettings()
            }
        snackbar.show()
    }

    fun navigateToSettings() {
        startActivityForResult(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }, INTENT_TO_SETTINGS)
    }


}
