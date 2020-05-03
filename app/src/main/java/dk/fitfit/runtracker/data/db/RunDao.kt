package dk.fitfit.runtracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RunDao {
    @Query("SELECT * FROM RunEntity ORDER BY dateTime DESC")
    fun getRuns(): LiveData<List<RunEntity>>

    @Query("SELECT * FROM RunEntity WHERE id=(:id)")
    fun getRun(id: Long): LiveData<RunEntity>

    @Insert
    fun addRun(runEntity: RunEntity): Long

    fun newRun() = addRun(RunEntity())
}
