package dk.fitfit.runtracker.viewmodels

import android.location.Location
import androidx.lifecycle.*
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.db.LocationEntity
import dk.fitfit.runtracker.data.db.RunEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

class LocationUpdateViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    private val _runId: MutableLiveData<Long> = MutableLiveData()

    private fun updateRunId(runId: Long) {
        _runId.postValue(runId)
    }

    val receivingLocationUpdates: LiveData<Boolean> = locationRepository.receivingLocationUpdates

    val speed: LiveData<String> = liveData {
        while (true) {
            if (receivingLocationUpdates.value == true) {
                val locationEntity = locations.value?.first()
                if (locationEntity != null) {
                    val speed = locationEntity.speed
                    emit("${(speed * 3600) / 1000}")
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

    val distance: LiveData<Double> = Transformations.switchMap(locations) {
        liveData {
            emit(calculateDistance(it))
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

    // TODO: Move this into RouteUtils?
    private fun calculateDistance(it: List<LocationEntity>): Double {
        val results = floatArrayOf(0f)
        val sum = IntStream
            .range(0, it.size - 1)
            .mapToDouble { i ->
                Location.distanceBetween(
                    it[i].latitude,
                    it[i].longitude,
                    it[i + 1].latitude,
                    it[i + 1].longitude,
                    results
                )
                results[0].toDouble()
            }
            .sum()
        return sum
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
