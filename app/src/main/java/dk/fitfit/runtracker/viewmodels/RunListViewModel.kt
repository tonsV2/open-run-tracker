package dk.fitfit.runtracker.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.RunRepository
import dk.fitfit.runtracker.data.db.RunEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class RunListViewModel(private val runRepository: RunRepository, private val locationRepository: LocationRepository) : ViewModel() {
    val runs: LiveData<List<RunEntity>> = liveData {
        emitSource(runRepository.getRuns())
    }

    fun delete(runId: Long) = viewModelScope.launch(IO) {
        runRepository.delete(runId)
    }
}
