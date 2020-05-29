package dk.fitfit.runtracker.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.RunRepository
import dk.fitfit.runtracker.data.db.duration
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

// TODO: This is work in progress

// Don't calculate both speed and pace. Only calculate one and do so dependent on weather metrics or imperial is selected
// Or possible make an additional setting for this... Or even better get this from the users existing settings

// Show the unused properties of RunSummary

class RunSummary(
    val startDateTime: LocalDateTime,

    val duration: Duration,
    val distance: Double,

    var ascend: Double,
    var descent: Double,

    val minSpeed: Float,
    val avgSpeed: Float,
    val maxSpeed: Float,

    val minPace: Float,
    val avgPace: Float,
    val maxPace: Float,

    val minAltitude: Double,
    val maxAltitude: Double
)

class RunSummaryViewModel(private val runRepository: RunRepository, private val locationRepository: LocationRepository) : ViewModel() {
    val summary = MutableLiveData<RunSummary>()

    fun loadSummary(runId: Long) {
        viewModelScope.launch(IO) {
            val run = runRepository.getRun(runId)
            val locations = locationRepository.getLocations(runId)

            val startDateTime: LocalDateTime = run.startDateTime

            val ascend: Double = run.ascend ?: 0.0
            val descent: Double = run.descent ?: 0.0

            val distanceMeters = run.distance ?: 0.0
            val durationSeconds = run.duration().seconds
            val speed = (distanceMeters / durationSeconds).toFloat()

            val minSpeed = (locations.minBy { it.speed }?.speed?.times(KM_PER_H_COEFFICIENT)) ?: 0f
            val avgSpeed = speed * KM_PER_H_COEFFICIENT
            val maxSpeed = (locations.maxBy { it.speed }?.speed?.times(KM_PER_H_COEFFICIENT)) ?: 0f

            val minPace = (1000 / 60).div(locations.minBy { it.speed }?.speed ?: 0f)
            val avgPace = (1000 / 60) / speed
            val maxPace = (1000 / 60).div(locations.maxBy { it.speed }?.speed ?: 0f)

            val minAltitude = locations.minBy { it.altitude }?.altitude ?: 0.0
            val maxAltitude = locations.maxBy { it.altitude }?.altitude ?: 0.0

            val runSummary = RunSummary(
                startDateTime,
                run.duration(),
                distanceMeters,
                ascend,
                descent,
                minSpeed,
                avgSpeed,
                maxSpeed,
                minPace,
                avgPace,
                maxPace,
                minAltitude,
                maxAltitude
            )

            summary.postValue(runSummary)
        }
    }

    fun getLocations(runId: Long) = liveData {
        emitSource(locationRepository.getLiveLocations(runId))
    }

    companion object {
        private const val KM_PER_H_COEFFICIENT = 3.6f
    }
}
