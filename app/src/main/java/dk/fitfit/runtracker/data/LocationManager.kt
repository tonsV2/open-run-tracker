package dk.fitfit.runtracker.data

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import dk.fitfit.runtracker.LocationUpdatesBroadcastReceiver
import dk.fitfit.runtracker.utils.hasPermission
import java.util.concurrent.TimeUnit

private const val TAG = "LocationManager"

class LocationManager(private val context: Context) {
    private val _receivingLocationUpdates: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    val receivingLocationUpdates: LiveData<Boolean>
        get() = _receivingLocationUpdates

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest = LocationRequest().apply {
        interval = TimeUnit.SECONDS.toMillis(10)
        fastestInterval = TimeUnit.SECONDS.toMillis(3)
        maxWaitTime = TimeUnit.SECONDS.toMillis(15)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var locationUpdatePendingIntent: PendingIntent? = null

    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates(runId: Long) {
        Log.d(TAG, "startLocationUpdates()")

        if (!context.hasPermission(ACCESS_FINE_LOCATION)) {
            // TODO: Redirect to grant permission fragment
            Log.e(TAG, "Manifest.permission.ACCESS_FINE_LOCATION not granted!!!")
            return
        }

        locationUpdatePendingIntent = LocationUpdatesBroadcastReceiver.createLocationUpdatePendingIntent(context, runId)

        try {
            _receivingLocationUpdates.postValue(true)
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
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
        if (locationUpdatePendingIntent == null) {
            throw RuntimeException("stopLocationUpdates() called before startLocationUpdates()")
        }

        Log.d(TAG, "stopLocationUpdates()")
        _receivingLocationUpdates.postValue(false)
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
    }
}
