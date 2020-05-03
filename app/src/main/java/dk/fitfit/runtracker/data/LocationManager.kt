/*
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.fitfit.runtracker.data

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import dk.fitfit.runtracker.LocationUpdatesBroadcastReceiver
import dk.fitfit.runtracker.hasPermission
import java.util.concurrent.TimeUnit

private const val TAG = "LocationManager"

/**
 * Manages all location related tasks for the app.
 */
class LocationManager(private val context: Context) {

    private val _receivingLocationUpdates: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    /**
     * Status of location updates, i.e., whether the app is actively subscribed to location changes.
     */
    val receivingLocationUpdates: LiveData<Boolean>
        get() = _receivingLocationUpdates

    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // Stores parameters for requests to the FusedLocationProviderApi.
    private val locationRequest: LocationRequest = LocationRequest().apply {
        // Sets the desired interval for active location updates. This interval is inexact. You
        // may not receive updates at all if no location sources are available, or you may
        // receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        //
        // IMPORTANT NOTE: Apps running on "O" devices (regardless of targetSdkVersion) may
        // receive updates less frequently than this interval when the app is no longer in the
        // foreground.
        interval = TimeUnit.SECONDS.toMillis(10)

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        fastestInterval = TimeUnit.SECONDS.toMillis(3)

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        maxWaitTime = TimeUnit.SECONDS.toMillis(15)

        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Creates default PendingIntent for location changes.
     *
     * Note: We use a BroadcastReceiver because on API level 26 and above (Oreo+), Android places
     * limits on Services.
     */
    private var locationUpdatePendingIntent: PendingIntent? = null

    /**
     * Uses the FusedLocationProvider to start location updates if the correct fine locations are
     * approved.
     *
     * @throws SecurityException if ACCESS_FINE_LOCATION permission is removed before the
     * FusedLocationClient's requestLocationUpdates() has been completed.
     */
    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates(runId: Long) {
        Log.d(TAG, "startLocationUpdates()")

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.e(TAG, "Manifest.permission.ACCESS_FINE_LOCATION not granted!!!")
            return
        }

        // remove old intent
        stopLocationUpdates()
        initializeLocationUpdatePendingIntent(runId)
        try {
            _receivingLocationUpdates.postValue(true)
            // If the PendingIntent is the same as the last request (which it always is), this
            // request will replace any requestLocationUpdates() called before.
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
            Log.d(TAG, "startLocationUpdates()")
        } catch (permissionRevoked: SecurityException) {
            _receivingLocationUpdates.postValue(false)

            // Exception only occurs if the user revokes the FINE location permission before
            // requestLocationUpdates() is finished executing (very rare).
            Log.d(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }

    @MainThread
    fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()")
        _receivingLocationUpdates.postValue(false)
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
    }

    private fun initializeLocationUpdatePendingIntent(runId: Long) {
        val intent = Intent(context, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        putExtraWorkAround(intent, runId)

        locationUpdatePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun putExtraWorkAround(intent: Intent, runId: Long) {
//        intent.putExtra("runId", runId)
        val uri = Uri.Builder().scheme("http")
            .authority("workaround.com")
            .appendQueryParameter("runId", runId.toString())
            .build()
        intent.data = uri
    }
}
