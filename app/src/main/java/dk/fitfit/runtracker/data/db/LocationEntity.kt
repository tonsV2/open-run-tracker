package dk.fitfit.runtracker.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = RunEntity::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocationEntity(
    val runId: Long,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
