package dk.fitfit.runtracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

private const val ACCURACY_THRESHOLD = 50
private const val sql = "SELECT * FROM LocationEntity WHERE runId = :runId AND accuracy < $ACCURACY_THRESHOLD ORDER BY dateTime DESC"

@Dao
interface LocationDao {
    @Query(sql)
    fun getLiveLocations(runId: Long): LiveData<List<LocationEntity>>

    @Query(sql)
    fun getLocations(runId: Long): List<LocationEntity>

    @Insert
    fun addLocations(locationEntities: List<LocationEntity>)
}
