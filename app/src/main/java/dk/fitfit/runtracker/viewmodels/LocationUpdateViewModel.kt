package dk.fitfit.runtracker.viewmodels

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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationUpdateViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    private val _runId: MutableLiveData<Long> = MutableLiveData()

    private fun updateRunId(runId: Long) {
        _runId.postValue(runId)
    }

    val receivingLocationUpdates: LiveData<Boolean> = locationRepository.receivingLocationUpdates

    private val locations: LiveData<List<LocationEntity>> = Transformations.switchMap(_runId) {
        locationRepository.getLocations(it)
    }

    private val currentRun: LiveData<RunEntity> = Transformations.switchMap(_runId) {
        locationRepository.getRun(it)
    }

    // TODO: Move business logic down to the repository
    val distance: LiveData<Double> = Transformations.switchMap(locations) {
        liveData {
            val sum = IntStream.range(0, it.size - 1)
                .mapToDouble { i ->
                    distance(
                        it[i].latitude,
                        it[i].longitude,
                        it[i + 1].latitude,
                        it[i + 1].longitude
                    )
                }
                .sum()
            emit(sum)

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
        val runId = _runId.value
        if (runId != null) {
            locationRepository.stopLocationUpdates(runId)
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

    // Inspiration: https://stackoverflow.com/a/837957/672009
    private fun distance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0 //meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
