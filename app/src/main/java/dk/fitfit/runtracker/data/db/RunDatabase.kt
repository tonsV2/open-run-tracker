package dk.fitfit.runtracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RunEntity::class, LocationEntity::class], version = 1)
@TypeConverters(MyTypeConverters::class)
abstract class RunDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: RunDatabase? = null

        fun getDatabase(context: Context): RunDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room
                    .databaseBuilder(context, RunDatabase::class.java, "database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
