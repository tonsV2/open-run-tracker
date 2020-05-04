package dk.fitfit.runtracker.data

import androidx.lifecycle.LiveData
import dk.fitfit.runtracker.data.db.LocationDao
import dk.fitfit.runtracker.data.db.LocationEntity
import dk.fitfit.runtracker.data.db.RunDao
import dk.fitfit.runtracker.data.db.RunEntity
import dk.fitfit.runtracker.utils.RouteUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class LocationRepository(private val runDao: RunDao,
                         private val locationDao: LocationDao,
                         private val locationManager: LocationManager,
                         private val routeUtils: RouteUtils) {

    val receivingLocationUpdates: LiveData<Boolean> = locationManager.receivingLocationUpdates

    fun startLocationUpdates(): Long {
        val runId = runDao.newRun()
        locationManager.startLocationUpdates(runId)
        return runId
    }

    fun stopLocationUpdates(runId: Long) {
        val run = runDao.getRun(runId)
        run.endDataTime = LocalDateTime.now()
        run.distance = routeUtils.calculateDistance(locationDao.getLocations(runId))
        runDao.updateRun(run)
        locationManager.stopLocationUpdates()
    }

    fun addLocations(locationEntities: List<LocationEntity>) {
        CoroutineScope(IO).launch {
            locationDao.addLocations(locationEntities)
        }
    }

    fun getRun(id: Long): LiveData<RunEntity> = runDao.getLiveRun(id)

    fun getLocations(runId: Long): LiveData<List<LocationEntity>> = locationDao.getLiveLocations(runId)
}
