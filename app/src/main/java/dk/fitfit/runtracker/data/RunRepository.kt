package dk.fitfit.runtracker.data

import androidx.lifecycle.LiveData
import dk.fitfit.runtracker.data.db.RunDao
import dk.fitfit.runtracker.data.db.RunEntity

class RunRepository(private val runDao: RunDao) {
    fun getRuns(): LiveData<List<RunEntity>> = runDao.getRuns()

    fun getLiveRun(runId: Long): LiveData<RunEntity> = runDao.getLiveRun(runId)

    fun getRun(runId: Long): RunEntity = runDao.getRun(runId)

    fun update(run: RunEntity) = runDao.update(run)
}
