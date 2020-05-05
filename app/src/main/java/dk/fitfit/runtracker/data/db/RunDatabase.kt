package dk.fitfit.runtracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RunEntity::class, LocationEntity::class], version = 1)
@TypeConverters(MyTypeConverters::class)
abstract class RunDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao
    abstract fun locationDao(): LocationDao
}
