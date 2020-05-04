package dk.fitfit.runtracker.viewmodels

import androidx.lifecycle.*
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.db.LocationEntity
import dk.fitfit.runtracker.data.db.RunEntity
import dk.fitfit.runtracker.utils.RouteUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class LocationUpdateViewModel(private val locationRepository: LocationRepository, routeUtils: RouteUtils) : ViewModel() {
    private val _runId: MutableLiveData<Long> = MutableLiveData()

    private fun updateRunId(runId: Long) {
        _runId.postValue(runId)
    }

    val receivingLocationUpdates: LiveData<Boolean> = locationRepository.receivingLocationUpdates

    val speed: LiveData<String> = liveData {
        while (true) {
            if (receivingLocationUpdates.value == true && locations.value?.isEmpty() != true) {
                val locationEntity = locations.value?.first()
                if (locationEntity != null) {
                    val speed = locationEntity.speed
                    emit("%.1f km/h".format((speed * 3600) / 1000))
                }
            }
            delay(1_000)
        }
    }

    private val locations: LiveData<List<LocationEntity>> = Transformations.switchMap(_runId) {
        locationRepository.getLocations(it)
    }

    private val currentRun: LiveData<RunEntity> = Transformations.switchMap(_runId) {
        locationRepository.getRun(it)
    }

    val distance: LiveData<String> = Transformations.switchMap(locations) {
        liveData {
            emit("%.2f km".format(routeUtils.calculateDistance(it) / 1_000))
        }
    }

    val duration: LiveData<String> = Transformations.switchMap(currentRun) {
        liveData {
            while (true) {
                if (receivingLocationUpdates.value == true) {
                    emit(duration(it.startDateTime))
                }
                delay(1_000)
            }
        }
    }

    fun startLocationUpdates() {
        viewModelScope.launch(IO) {
            val runId = locationRepository.startLocationUpdates()
            updateRunId(runId)
        }
    }

    fun stopLocationUpdates() {
        viewModelScope.launch(IO) {
            val runId = _runId.value
            if (runId != null) {
                locationRepository.stopLocationUpdates(runId)
            }
        }
    }

    private fun duration(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        var tempDateTime: LocalDateTime = dateTime

        val hours = tempDateTime.until(now, ChronoUnit.HOURS)
        tempDateTime = tempDateTime.plusHours(hours)

        val minutes = tempDateTime.until(now, ChronoUnit.MINUTES)
        tempDateTime = tempDateTime.plusMinutes(minutes)

        val seconds = tempDateTime.until(now, ChronoUnit.SECONDS)

//        return "$hours:$minutes:$seconds"
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}
