package dk.fitfit.runtracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

private const val ACCURACY_THRESHOLD = 50

private const val LOCATIONS_BY_RUN_ID_SQL = """
    SELECT *
    FROM LocationEntity
    WHERE runId = :runId AND accuracy < $ACCURACY_THRESHOLD
    ORDER BY dateTime DESC
"""

private const val SPEED_OF_LATEST_LOCATION_SQL = """
    SELECT speed
    FROM LocationEntity
    WHERE runId = :runId AND accuracy < $ACCURACY_THRESHOLD
    ORDER BY dateTime
    DESC LIMIT 1
"""

@Dao
interface LocationDao {
    @Query(LOCATIONS_BY_RUN_ID_SQL)
    fun getLiveLocations(runId: Long): LiveData<List<LocationEntity>>

    @Query(LOCATIONS_BY_RUN_ID_SQL)
    fun getLocations(runId: Long): List<LocationEntity>

    @Insert
    fun addLocations(locationEntities: List<LocationEntity>)

    @Query(SPEED_OF_LATEST_LOCATION_SQL)
    fun speed(runId: Long): LiveData<Float?>
}
