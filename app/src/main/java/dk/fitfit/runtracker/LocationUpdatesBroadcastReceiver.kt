package dk.fitfit.runtracker

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
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
        val runId = intent.data?.getQueryParameter(RUN_ID_PARAMETER)?.toLong() ?: 0L
        if (runId == 0L) {
            throw RuntimeException("Run id can't be 0")
        }
        return runId
    }

    companion object {
        private const val ACTION_PROCESS_UPDATES = "dk.fitfit.runtracker.action.PROCESS_UPDATES"
        private const val RUN_ID_PARAMETER = "run-id"

        fun createLocationUpdatePendingIntent(context: Context, runId: Long): PendingIntent {
            val intent = Intent(context, LocationUpdatesBroadcastReceiver::class.java)
            intent.action = ACTION_PROCESS_UPDATES
            putExtraWorkAround(intent, runId)

            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun putExtraWorkAround(intent: Intent, runId: Long) {
//        intent.putExtra("runId", runId)
            val uri = Uri.Builder().scheme("http")
                .authority("workaround.com")
                .appendQueryParameter(RUN_ID_PARAMETER, runId.toString())
                .build()
            intent.data = uri
        }
    }
}

private fun Location.toLocationEntity(runId: Long): LocationEntity {
    val latitude = this.latitude
    val longitude = this.longitude
    val altitude = this.altitude
    val speed = this.speed
    val accuracy = this.accuracy
    val verticalAccuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val verticalAccuracy = this.verticalAccuracyMeters
        verticalAccuracy
    } else {
        null
    }
    val time = this.time.toLocalDateTime()

    Log.d(TAG, "Location: $latitude, $longitude")
    Log.d(TAG, "Speed (km/h): ${(speed * 3600) / 1000}")
    Log.d(TAG, "Pace (m/km): ${(1000 / 60) / speed}")
    Log.d(TAG, "Accuracy: $accuracy")
    Log.d(TAG, "VerticalAccuracy: $verticalAccuracy")
    Log.d(TAG, "Time: $time")

    return LocationEntity(runId, latitude, longitude, altitude, speed, accuracy, verticalAccuracy, time)
}

private fun Long.toLocalDateTime(): LocalDateTime {
    val milliseconds = Instant.ofEpochMilli(this)
    return LocalDateTime.ofInstant(milliseconds, ZoneOffset.UTC)
}
