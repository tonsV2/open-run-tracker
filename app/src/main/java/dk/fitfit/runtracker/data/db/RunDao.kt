package dk.fitfit.runtracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

private const val SELECT_RUN_BY_ID_SQL = "SELECT * FROM RunEntity WHERE id = :id"

@Dao
interface RunDao {
    @Query("SELECT * FROM RunEntity ORDER BY startDateTime DESC")
    fun getRuns(): LiveData<List<RunEntity>>

    @Query(SELECT_RUN_BY_ID_SQL)
    fun getLiveRun(id: Long): LiveData<RunEntity>

    @Query(SELECT_RUN_BY_ID_SQL)
    fun getRun(id: Long): RunEntity

    @Update
    fun updateRun(runEntity: RunEntity)

    @Insert
    fun addRun(runEntity: RunEntity): Long

    fun newRun() = addRun(RunEntity())

    @Delete
    fun delete(runEntity: RunEntity)
}
