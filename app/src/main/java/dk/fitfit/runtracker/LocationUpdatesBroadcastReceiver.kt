package dk.fitfit.runtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.LocationResult
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.db.LocationEntity
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

private const val TAG = "LUBroadcastReceiver"

/**
 * Receiver for handling location updates.
 *
 * For apps targeting API level O and above
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates in the background. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should NOT be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
class LocationUpdatesBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val locationRepository: LocationRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PROCESS_UPDATES) {
            val runId = getRunIdWorkAround(intent)
            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations.map { it.toLocationEntity(runId) }
                if (locations.isNotEmpty()) {
                    locationRepository.addLocations(locations)
                }
            }
        }
    }

    private fun getRunIdWorkAround(intent: Intent): Long {
//            val runId = intent.getLongExtra("runId", 0)
        val runId = intent.data?.getQueryParameter("runId")?.toLong() ?: 0L
        if (runId == 0L) {
            Log.e(TAG, "run id was 0")
        }
        return runId
    }

    companion object {
        const val ACTION_PROCESS_UPDATES = "dk.fitfit.runtracker.action.PROCESS_UPDATES"
    }
}

private fun Location.toLocationEntity(runId: Long): LocationEntity {
    val speed = this.speed

    Log.d(TAG, "Speed (km/h): ${(speed * 3600) / 1000}")
    Log.d(TAG, "Pace (m/km): ${(1000 / 60) / speed}")

    val latitude = this.latitude
    val longitude = this.longitude
    val altitude = this.altitude
    val accuracy = this.accuracy

    Log.d(TAG, "Location: $latitude, $longitude")
    Log.d(TAG, "Accuracy: $accuracy")

    val verticalAccuracy: Float? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val verticalAccuracy = this.verticalAccuracyMeters
        Log.d(TAG, "VerticalAccuracy: $verticalAccuracy")
        verticalAccuracy
    } else {
        null
    }

    return LocationEntity(runId, latitude, longitude, altitude, speed, accuracy, verticalAccuracy, this.time.toLocalDateTime())
}

private fun Long.toLocalDateTime(): LocalDateTime {
    val milliseconds = Instant.ofEpochMilli(this)
    return LocalDateTime.ofInstant(milliseconds, ZoneOffset.UTC)
}
