package dk.fitfit.runtracker.data

import android.util.Log
import androidx.lifecycle.LiveData
import dk.fitfit.runtracker.data.db.LocationDao
import dk.fitfit.runtracker.data.db.LocationEntity
import dk.fitfit.runtracker.data.db.RunDao
import dk.fitfit.runtracker.data.db.RunEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private const val TAG = "LocationRepository"

class LocationRepository(private val runDao: RunDao, private val locationDao: LocationDao, private val locationManager: LocationManager) {
    val receivingLocationUpdates: LiveData<Boolean> = locationManager.receivingLocationUpdates

    fun startLocationUpdates(): Long {
        val runId = runDao.newRun()
        Log.d(TAG, "New run id: $runId")
        locationManager.startLocationUpdates(runId)
        return runId
    }

    fun stopLocationUpdates(runId: Long) {
        val run = runDao.getRun(runId)
        run.endDataTime = LocalDateTime.now()
        runDao.updateRun(run)
        locationManager.stopLocationUpdates()
    }

    fun addLocations(locationEntities: List<LocationEntity>) {
        CoroutineScope(IO).launch {
            locationDao.addLocations(locationEntities)
        }
    }

    fun getRun(id: Long): LiveData<RunEntity> = runDao.getLiveRun(id)

    fun getLocations(runId: Long): LiveData<List<LocationEntity>> = locationDao.getLocations(runId)
}
