package dk.fitfit.runtracker.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dk.fitfit.runtracker.data.RunRepository
import dk.fitfit.runtracker.data.db.RunEntity

class RunListViewModel(private val runRepository: RunRepository) : ViewModel() {
    val runs: LiveData<List<RunEntity>> = liveData {
        emitSource(runRepository.getRuns())
    }
}
