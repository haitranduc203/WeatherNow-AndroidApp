package com.example.weathernow.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.weathernow.core.common.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executor
import kotlin.coroutines.resume

data class DeviceCoordinates(
    val latitude: Double,
    val longitude: Double
)

class LocationFreshnessValidator(
    private val clock: () -> Long = System::currentTimeMillis,
    val maxAgeMillis: Long = 5 * 60 * 1000L // 5 minutes
) {
    fun isFresh(locationTimeMillis: Long): Boolean {
        if (locationTimeMillis <= 0L) return false
        val now = clock()
        val age = now - locationTimeMillis
        return age in 0..maxAgeMillis
    }
}

interface DeviceLocationDataSource {
    suspend fun getCurrentCoordinates(): Resource<DeviceCoordinates>
}

class AndroidDeviceLocationDataSource(
    private val context: Context,
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context),
    private val freshnessValidator: LocationFreshnessValidator = LocationFreshnessValidator(),
    private val timeoutMillis: Long = 15_000L
) : DeviceLocationDataSource {

    private val directExecutor = Executor { it.run() }

    override suspend fun getCurrentCoordinates(): Resource<DeviceCoordinates> {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return Resource.Error("Location permission is required")
        }

        return try {
            // 1. Try fresh lastLocation first
            val lastLocation = awaitLastLocation()
            if (lastLocation != null && freshnessValidator.isFresh(lastLocation.time)) {
                return Resource.Success(DeviceCoordinates(lastLocation.latitude, lastLocation.longitude))
            }

            // 2. Request a current location fix with timeout
            val currentLocation = withTimeoutOrNull(timeoutMillis) {
                awaitCurrentLocation()
            }
            if (currentLocation != null) {
                Resource.Success(currentLocation)
            } else {
                Resource.Error("Unable to obtain device location. Try searching manually.")
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Resource.Error(
                e.message ?: "Unable to obtain device location. Try searching manually.",
                cause = e
            )
        }
    }

    @Suppress("MissingPermission") // Permission is checked in getCurrentCoordinates
    private suspend fun awaitLastLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener(directExecutor) { location ->
                    if (cont.isActive) {
                        cont.resume(location)
                    }
                }
                .addOnFailureListener(directExecutor) {
                    if (cont.isActive) {
                        cont.resume(null)
                    }
                }
                .addOnCanceledListener(directExecutor) {
                    if (cont.isActive) {
                        cont.resume(null)
                    }
                }
        }

    @Suppress("MissingPermission") // Permission is checked in getCurrentCoordinates
    private suspend fun awaitCurrentLocation(): DeviceCoordinates? {
        val cts = CancellationTokenSource()
        return suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { cts.cancel() }
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cts.token
            )
                .addOnSuccessListener(directExecutor) { location ->
                    if (cont.isActive) {
                        if (location != null) {
                            cont.resume(DeviceCoordinates(location.latitude, location.longitude))
                        } else {
                            cont.resume(null)
                        }
                    }
                }
                .addOnFailureListener(directExecutor) {
                    if (cont.isActive) {
                        cont.resume(null)
                    }
                }
                .addOnCanceledListener(directExecutor) {
                    if (cont.isActive) {
                        cont.resume(null)
                    }
                }
        }
    }
}
