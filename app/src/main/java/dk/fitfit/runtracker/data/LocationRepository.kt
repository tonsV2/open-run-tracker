package dk.fitfit.runtracker.data

import androidx.lifecycle.LiveData
import dk.fitfit.runtracker.data.db.*
import dk.fitfit.runtracker.utils.RouteUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private const val durationThreshold = 30
private const val distanceThreshold = 50.0

class LocationRepository(
    private val runDao: RunDao,
    private val locationDao: LocationDao,
    private val locationManager: LocationManager,
    private val routeUtils: RouteUtils
) {

    val receivingLocationUpdates: LiveData<Boolean> = locationManager.receivingLocationUpdates

    fun startLocationUpdates(): Long {
        val runId = runDao.newRun()
        locationManager.startLocationUpdates(runId)
        return runId
    }

    fun stopLocationUpdates(runId: Long) {
        locationManager.stopLocationUpdates()

        val run = runDao.getRun(runId)
        run.endDataTime = LocalDateTime.now()
        val locations = locationDao.getLocations(runId)
        val distance = routeUtils.calculateDistance(locations)
        val duration = run.duration().seconds

        if (duration < durationThreshold) {
            runDao.delete(run)
            throw RunNotLoggedException("Duration too short! Only $duration seconds logged, threshold is $durationThreshold")
        }

        if (distance < distanceThreshold) {
            runDao.delete(run)
            throw RunNotLoggedException("Distance too short! Only $distance meters logged, threshold is $distanceThreshold")
        }

        run.distance = distance

        run.ascent = routeUtils.calculateascent(locations)
        run.descent = routeUtils.calculateDescent(locations)

        runDao.update(run)
    }

    fun addLocations(locationEntities: List<LocationEntity>) {
        CoroutineScope(IO).launch {
            locationDao.addLocations(locationEntities)
        }
    }

    fun getRun(id: Long): LiveData<RunEntity> = runDao.getLiveRun(id)

    fun getLiveLocations(runId: Long): LiveData<List<LocationEntity>> = locationDao.getLiveLocations(runId)

    fun getLocations(runId: Long): List<LocationEntity> = locationDao.getLocations(runId)

    fun speed(runId: Long): LiveData<Float?> = locationDao.speed(runId)
}

class RunNotLoggedException(message: String) : RuntimeException(message)
