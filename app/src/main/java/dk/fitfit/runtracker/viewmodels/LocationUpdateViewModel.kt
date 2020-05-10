package dk.fitfit.runtracker.viewmodels

import android.util.Log
import androidx.lifecycle.*
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.db.LocationEntity
import dk.fitfit.runtracker.data.db.RunEntity
import dk.fitfit.runtracker.utils.RouteUtils
import dk.fitfit.runtracker.utils.toHHMMSS
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

private const val TAG = "LocationUpdateViewModel"

class LocationUpdateViewModel(private val locationRepository: LocationRepository, routeUtils: RouteUtils) : ViewModel() {
    private val _runId: MutableLiveData<Long> = MutableLiveData()
    val endOfRunStatus: MutableLiveData<EndOfRunStatus> = MutableLiveData()

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
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG, "stopLocationUpdates: $exception")
            endOfRunStatus.postValue(EndOfRunStatus.Exceptional(exception.message.toString()))
        }

        val coroutineScope = CoroutineScope(IO)
        coroutineScope.launch(exceptionHandler) {
            val runId = _runId.value
            if (runId != null) {
                locationRepository.stopLocationUpdates(runId)
                endOfRunStatus.postValue(EndOfRunStatus.Success(runId))
            }
        }
    }

    val receivingLocationUpdates: LiveData<Boolean> = locationRepository.receivingLocationUpdates

    private val speed: LiveData<Float?> = Transformations.switchMap(_runId) {
        liveData {
            emitSource(locationRepository.speed(it))
        }
    }

    val speedString = Transformations.map(speed) {
        if (it != null) {
            "%.1f km/h".format((it * 3600) / 1000)
        } else {
            ""
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
                if (receivingLocationUpdates.value == true && it != null) {
                    val durationString = Duration.between(it.startDateTime, LocalDateTime.now()).toHHMMSS()
                    emit(durationString)
                } else {
                    return@liveData
                }
                delay(1_000)
            }
        }
    }

    sealed class EndOfRunStatus {
        class Exceptional(val message: String): EndOfRunStatus()
        class Success(val runId: Long): EndOfRunStatus()
    }

    companion object {
        private const val LAP_IN_METERS = 1_000
    }
}
