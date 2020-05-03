package dk.fitfit.runtracker.data.db

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.UUID

class MyTypeConverters {
    @TypeConverter
    fun fromLocalDateTime(localDateTime: LocalDateTime?): Long? = localDateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(millisSinceEpoch: Long?): LocalDateTime? {
        return millisSinceEpoch?.let {
            val epochMilli = Instant.ofEpochMilli(it)
            LocalDateTime.ofInstant(epochMilli, ZoneOffset.UTC)
        }
    }
}
