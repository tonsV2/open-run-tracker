package dk.fitfit.runtracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

@Dao
interface LocationDao {
    @Query("SELECT * FROM LocationEntity WHERE runId = :runId ORDER BY dateTime DESC")
    fun getLocations(runId: Long): LiveData<List<LocationEntity>>

    @Insert
    fun addLocations(locationEntities: List<LocationEntity>)
}
