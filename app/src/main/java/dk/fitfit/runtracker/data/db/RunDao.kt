package dk.fitfit.runtracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RunDao {
    @Query("SELECT * FROM RunEntity ORDER BY startDateTime DESC")
    fun getRuns(): LiveData<List<RunEntity>>

    @Query("SELECT * FROM RunEntity WHERE id = :id")
    fun getLiveRun(id: Long): LiveData<RunEntity>

    @Query("SELECT * FROM RunEntity WHERE id = :id")
    fun getRun(id: Long): RunEntity

    @Update
    fun updateRun(runEntity: RunEntity)

    @Insert
    fun addRun(runEntity: RunEntity): Long

    fun newRun() = addRun(RunEntity())
}
