package dk.fitfit.runtracker.data

import androidx.lifecycle.LiveData
import dk.fitfit.runtracker.data.db.RunDao
import dk.fitfit.runtracker.data.db.RunEntity

class RunRepository(private val runDao: RunDao) {
    fun getRuns(): LiveData<List<RunEntity>> = runDao.getRuns()

    fun getRun(runId: Long): RunEntity = runDao.getRun(runId)

    fun delete(runId: Long) = runDao.delete(runId)
}
