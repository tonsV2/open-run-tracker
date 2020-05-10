package dk.fitfit.runtracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.LocalDateTime

@Entity
data class RunEntity(
    val startDateTime: LocalDateTime = LocalDateTime.now(),
    var endDataTime: LocalDateTime? = null,
    var distance: Double? = null,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)

fun RunEntity.duration(): Duration {
    return if (endDataTime != null) {
        Duration.between(startDateTime, endDataTime)
    } else {
        Duration.ZERO
    }
}
