package dk.fitfit.runtracker.data

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import dk.fitfit.runtracker.data.db.LocationDao
import dk.fitfit.runtracker.data.db.LocationEntity
import dk.fitfit.runtracker.data.db.RunDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

private const val TAG = "LocationRepository"

/**
 * Access point for database (MyLocation data) and location APIs (start/stop location updates and
 * checking location update status).
 */
class LocationRepository(private val runDao: RunDao, private val locationDao: LocationDao, private val locationManager: LocationManager) {
    private var runId: Long = 0

    /**
     * Returns all recorded locations from database.
     */
    fun getLocations(runId: Long): LiveData<List<LocationEntity>> = locationDao.getLocations(runId)

    /**
     * Returns all recorded locations from current run.
     */
    fun getLocations(): LiveData<List<LocationEntity>> = locationDao.getLocations(runId)

    /**
     * Adds list of locations to the database.
     */
    fun addLocations(locationEntities: List<LocationEntity>) {
        CoroutineScope(IO).launch {
            locationDao.addLocations(locationEntities)
        }
    }

    /**
     * Status of whether the app is actively subscribed to location changes.
     */
    val receivingLocationUpdates: LiveData<Boolean> = locationManager.receivingLocationUpdates

    /**
     * Subscribes to location updates.
     */
    @MainThread
    fun startLocationUpdates() {
        CoroutineScope(IO).launch {
            runId = runDao.newRun()
            Log.d(TAG, "New run id: $runId")
            locationManager.startLocationUpdates(runId)
        }
    }

    /**
     * Un-subscribes from location updates.
     */
    @MainThread
    fun stopLocationUpdates() = locationManager.stopLocationUpdates()
}
