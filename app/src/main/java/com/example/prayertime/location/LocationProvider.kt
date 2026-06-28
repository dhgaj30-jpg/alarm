package com.example.prayertime.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class LocationProvider(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLatLng(): Pair<Double, Double>? {
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            location?.let { it.latitude to it.longitude }
        } catch (_: SecurityException) {
            null
        } catch (_: Throwable) {
            null
        }
    }
}
