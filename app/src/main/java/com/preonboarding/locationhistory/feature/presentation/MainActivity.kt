package com.preonboarding.locationhistory.feature.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.preonboarding.locationhistory.LocationHistoryApp
import com.preonboarding.locationhistory.R
import com.preonboarding.locationhistory.base.BaseActivity
import com.preonboarding.locationhistory.data.entity.toFormatDate
import com.preonboarding.locationhistory.data.entity.toMapItem
import com.preonboarding.locationhistory.databinding.ActivityMainBinding
import com.preonboarding.locationhistory.feature.history.presentation.HistoryDialog
import com.preonboarding.locationhistory.feature.map.presentation.CustomBalloonAdapter
import com.preonboarding.locationhistory.feature.set.SetTimeDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

const val ADDRESS = 0
const val MAX_RESULT = 1

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val ACCESS_FINE_LOCATION = 1000
    private val HISTORY_DIALOG = 11
    private val SETTING_DIALOG = 12

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var locationManager: LocationManager
    private val gpsLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            startTracking()
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    private lateinit var balloonAdapter: CustomBalloonAdapter

    private val historyDialog by lazy {
        HistoryDialog(mainViewModel)
    }
    private val setDialog = SetTimeDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        permissionCheck()
        initView()
        observeToObservable()
    }

    private fun initView() {
        balloonAdapter = CustomBalloonAdapter(layoutInflater, mainViewModel, this)
        binding.apply {
            btnHistory.setOnClickListener {
                historyDialog.show(
                    supportFragmentManager,
                    getString(R.string.history_dialog)
                )
                mainViewModel.setDialogState(true, HISTORY_DIALOG)
            }
            btnSetting.setOnClickListener {
                setDialog.show(
                    supportFragmentManager,
                    getString(R.string.setting_dialog)
                )
                mainViewModel.setDialogState(true, SETTING_DIALOG)
            }
            btnAddress.setOnClickListener {
                if (checkLocationService()) {
                    startTracking()
                    val builder = AlertDialog.Builder(
                        this@MainActivity,
                        R.style.DialogTheme
                    )
                    val location = getCurrentLocation()
                    val detailAddress = getDetailAddress(location.first, location.second)
                    builder.apply {
                        setTitle(getString(R.string.currentPosition))
                        setMessage(detailAddress)
                    }.show()
                } else {
                    showToastMessage(getString(R.string.ask_gps_on))
                }
            }
            mapView.setCalloutBalloonAdapter(balloonAdapter)
        }
    }

    private fun permissionCheck() {
        binding.mapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없는 상태
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                denyAccess(getString(R.string.ask_location_permission), getString(R.string.cancel))
            } else {
                if (LocationHistoryApp.prefs.isFirst) {
                    // 최초 권한 요청
                    LocationHistoryApp.prefs.isFirst = false
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                } else {
                    // 다시 묻지 않음 클릭 (앱 정보 화면으로 이동)
                    denyAccess(
                        getString(R.string.ask_location_permission),
                        getString(R.string.go_setting)
                    )
                }
            }
        }
    }

    private fun denyAccess(setMessage: String, positiveBtn: String) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage(setMessage)
            setPositiveButton(positiveBtn) { dialog, which ->
                ActivityCompat.requestPermissions(
                    Activity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESS_FINE_LOCATION
                )
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            }
            show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun observeToObservable() {
        mainViewModel.setTime.observe(this@MainActivity) { time ->
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                time.toString().toLong(),
                10F,
                gpsLocationListener
            )//콜백으로 설정
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.historyFromDate.collect { historyList ->
                    updateMarkerList(historyList.toMapItem())
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.dialogState.collect { state ->
                    if (!state.isDialogShowed) {
                        when (state.dialogTag) {
                            HISTORY_DIALOG -> {
                                historyDialog.dismiss()
                            }
                            SETTING_DIALOG -> {
                                setDialog.dismiss()
                            }
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.selectedMarker.collectLatest { markerId ->
                    if (!mainViewModel.dialogState.value.isDialogShowed) {
                        val poiItem = binding.mapView.findPOIItemByTag(markerId)
                        binding.mapView.apply {
                            selectPOIItem(poiItem, true)
                            currentLocationTrackingMode =
                                MapView.CurrentLocationTrackingMode.TrackingModeOff
                        }
                    }
                }
            }
        }
    }

    private fun updateMarkerList(
        markerList: List<MapPOIItem>
    ) {
        binding.mapView.removeAllPOIItems()
        binding.mapView.addPOIItems(markerList.toTypedArray())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToastMessage(getString(R.string.location_permission_access))
            } else {
                showToastMessage(getString(R.string.location_permission_denied))
                permissionCheck()
            }
        }
    }

    private fun checkLocationService(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        binding.mapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        val position = getCurrentLocation()
        if (position != Pair(0.0, 0.0)) {
            val uNowPosition = MapPoint.mapPointWithGeoCoord(position.first, position.second)
            val marker = MapPOIItem().apply {
                itemName = getString(R.string.currentPosition)
                mapPoint = uNowPosition
                markerType = MapPOIItem.MarkerType.BluePin
                selectedMarkerType = MapPOIItem.MarkerType.RedPin
            }
            mainViewModel.saveHistory(getCurrentTime(), position.first, position.second)
            updateHistory()
            Toast.makeText(
                this,
                "lat: ${position.first}, long: ${position.second}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                getString(R.string.failed_to_get_location),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(): Pair<Double, Double> {
        val userNowLocation: Location? =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        val uLatitude: Double = userNowLocation?.latitude ?: 0.0
        val uLongitude: Double = userNowLocation?.longitude ?: 0.0
        return Pair(uLatitude, uLongitude)
    }

    private fun getDetailAddress(uLatitude: Double, uLongitude: Double): String {
        val geocoder = Geocoder(this)
        val convertAddress = geocoder
            .getFromLocation(uLatitude, uLongitude, MAX_RESULT)

        if (convertAddress.isEmpty()) {
            return getString(R.string.no_detail_location)
        } else {
            return convertAddress.get(ADDRESS).getAddressLine(ADDRESS).toString()
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

    private fun updateHistory() {
        mainViewModel.getHistoryFromDate(getCurrentTime().toFormatDate())
    }
}
