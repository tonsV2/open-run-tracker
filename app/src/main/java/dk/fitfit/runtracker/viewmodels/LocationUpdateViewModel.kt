package dk.fitfit.runtracker.viewmodels

import androidx.lifecycle.*
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.db.LocationEntity
import dk.fitfit.runtracker.data.db.RunEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

/**
 * Allows Fragment to observer {@link MyLocation} database, follow the state of location updates,
 * and start/stop receiving location updates.
 */
class LocationUpdateViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    private val _runId: MutableLiveData<Long> = MutableLiveData()

    private fun updateRunId(runId: Long) {
        _runId.postValue(runId)
    }
/*
    fun getLocations(): LiveData<List<LocationEntity>> {
        return Transformations.switchMap(_runId) {
            locationRepository.getLocations(it)
        }
    }
*/

    val locations: LiveData<List<LocationEntity>> = Transformations.switchMap(_runId) {
        locationRepository.getLocations(it)
    }

    val receivingLocationUpdates: LiveData<Boolean> = locationRepository.receivingLocationUpdates

    val currentRun: LiveData<RunEntity> = liveData {
        emitSource(locationRepository.getCurrentRun())
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
}
