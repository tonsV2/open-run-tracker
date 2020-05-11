package dk.fitfit.runtracker.viewmodels

import android.util.Log
import androidx.lifecycle.*
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.db.RunEntity
import dk.fitfit.runtracker.data.db.duration
import dk.fitfit.runtracker.utils.toHHMMSS
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "LocationUpdateViewModel"

class LocationUpdateViewModel(private val locationRepository: LocationRepository) : ViewModel() {
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

        CoroutineScope(IO).launch(exceptionHandler) {
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

    private val currentRun: LiveData<RunEntity> = Transformations.switchMap(_runId) {
        locationRepository.getRun(it)
    }

    val lapProgress: LiveData<Int> = Transformations.switchMap(currentRun) {
        liveData {
            val distance = it.distance
            if (distance != null) {
                val lapMeters = if (distance < LAP_IN_METERS) {
                    distance
                } else {
                    distance % LAP_IN_METERS
                }
                emit(lapMeters.toInt())
            }
        }
    }

    val distance: LiveData<String> = Transformations.switchMap(currentRun) {
        liveData {
            val value = "%.2f km".format(it.distance?.div(1_000))
            emit(value)
        }
    }

    val durationString: LiveData<String> = Transformations.switchMap(currentRun) {
        liveData {
            while (receivingLocationUpdates.value == true && it != null) {
                emit(it.duration().toHHMMSS())
                delay(1_000)
            }
        }
    }

    sealed class EndOfRunStatus {
        class Exceptional(val message: String) : EndOfRunStatus()
        class Success(val runId: Long) : EndOfRunStatus()
    }

    companion object {
        private const val LAP_IN_METERS = 1_000
    }
}
