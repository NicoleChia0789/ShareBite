package com.sharebite.app.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("unused")
class LocationUtils(private val activity: Activity) {
    private val context: Context = activity
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val settingsClient: SettingsClient =
        LocationServices.getSettingsClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * ✅ 检查 GPS 是否开启，如果未开启则弹出系统对话框
     * @return true 表示 GPS 已开启或用户同意开启，false 表示用户拒绝
     */
    suspend fun checkLocationSettings(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            // ✅ 使用 LocationRequest.Builder（API 31+）或兼容旧版本
            val locationRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                LocationRequest.Builder(LocationRequest.PRIORITY_HIGH_ACCURACY, 0L)
                    .setDurationMillis(5000L)
                    .setMaxUpdates(1)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    numUpdates = 1
                    interval = 0
                }
            }

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

            val task = settingsClient.checkLocationSettings(builder.build())

            task.addOnSuccessListener {
                continuation.resume(true)
            }.addOnFailureListener { exception ->
                // ✅ 正确处理：使用 ApiException 和 statusCode
                when (val e = exception as? ApiException) {
                    null -> continuation.resume(false)
                    else -> {
                        when (e.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                // ✅ GPS 未开启，弹窗让用户开启
                                try {
                                    // ✅ 正确使用 ResolvableApiException
                                    val resolvable = e as ResolvableApiException
                                    resolvable.startResolutionForResult(activity, 1001)
                                    continuation.resume(false)
                                } catch (ex: Exception) {
                                    continuation.resume(false)
                                }
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                continuation.resume(false)
                            }
                            else -> continuation.resume(false)
                        }
                    }
                }
            }
        }
    }

    suspend fun getLastKnownLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            if (!hasLocationPermission()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        _currentLocation.value = it
                        continuation.resume(it)
                    } ?: continuation.resume(null)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    @Suppress("DEPRECATION")
    suspend fun getCurrentLocation(): Location? {
        return withTimeoutOrNull(8000L) {
            suspendCancellableCoroutine { continuation ->
                if (!hasLocationPermission()) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                val locationRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    LocationRequest.Builder(LocationRequest.PRIORITY_HIGH_ACCURACY, 0L)
                        .setDurationMillis(5000L)
                        .setMaxUpdates(1)
                        .build()
                } else {
                    LocationRequest.create().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        numUpdates = 1
                        interval = 0
                    }
                }

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation
                        if (location != null) {
                            _currentLocation.value = location
                            continuation.resume(location)
                        } else {
                            continuation.resume(null)
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }

                    override fun onLocationAvailability(availability: LocationAvailability) {
                        if (!availability.isLocationAvailable) {
                            continuation.resume(null)
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }
                }

                try {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }
                } catch (_: SecurityException) {
                    continuation.resume(null)
                }
            }
        } ?: getLastKnownLocation()
    }

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return (results[0] / 1000f).toDouble()
    }
}