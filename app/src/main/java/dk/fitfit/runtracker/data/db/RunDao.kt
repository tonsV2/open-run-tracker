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
    fun update(runEntity: RunEntity)

    @Insert
    fun insert(runEntity: RunEntity): Long

    fun newRun() = insert(RunEntity())

    @Delete
    fun delete(runEntity: RunEntity)

    @Query("DELETE FROM RunEntity WHERE id = :runId")
    fun delete(runId: Long)
}
