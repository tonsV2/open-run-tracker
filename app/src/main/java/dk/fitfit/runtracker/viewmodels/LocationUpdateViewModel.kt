package dk.fitfit.runtracker.viewmodels

import androidx.lifecycle.*
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.db.LocationEntity
import dk.fitfit.runtracker.data.db.RunEntity
import dk.fitfit.runtracker.utils.RouteUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class LocationUpdateViewModel(private val locationRepository: LocationRepository, routeUtils: RouteUtils) : ViewModel() {
    private val _runId: MutableLiveData<Long> = MutableLiveData()

    val runId: Long?
        get() = _runId.value

    private fun updateRunId(runId: Long) {
        _runId.postValue(runId)
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

    private val distanceMeters: LiveData<Double> = Transformations.switchMap(locations) {
        liveData {
            emit(routeUtils.calculateDistance(it))
        }
    }

    val lapProgress: LiveData<Int> = Transformations.switchMap(distanceMeters) {
        liveData {
            val lapMeters = if (it < LAP_IN_METERS) {
                it
            } else {
                it % LAP_IN_METERS
            }
            emit(lapMeters.toInt())
        }
    }

    val distance: LiveData<String> = Transformations.switchMap(distanceMeters) {
        liveData {
            emit("%.2f km".format(it / 1_000))
        }
    }

    private val currentRun: LiveData<RunEntity> = Transformations.switchMap(_runId) {
        locationRepository.getRun(it)
    }

    val duration: LiveData<String> = Transformations.switchMap(currentRun) {
        liveData {
            while (true) {
                if (receivingLocationUpdates.value == true) {
                    val dur = Duration.between(it.startDateTime, LocalDateTime.now())
                    val seconds = dur.toMillis() / 1_000
                    emit("%02d:%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60, (seconds % 60)))
                }
                delay(1_000)
            }
        }
    }

    companion object {
        private const val LAP_IN_METERS = 1_000
    }
}
