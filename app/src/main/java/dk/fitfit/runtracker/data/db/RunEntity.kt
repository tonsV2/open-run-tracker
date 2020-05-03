package dk.fitfit.runtracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class RunEntity(
    val dateTime: LocalDateTime = LocalDateTime.now(),
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
