package dk.fitfit.runtracker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.RunRepository

class RunSummaryViewModel(private val runRepository: RunRepository, private val locationRepository: LocationRepository) : ViewModel() {
    fun getRun(runId: Long) = liveData {
        emitSource(runRepository.getLiveRun(runId))
    }

    fun getLocations(runId: Long) = liveData {
        emitSource(locationRepository.getLiveLocations(runId))
    }
}
