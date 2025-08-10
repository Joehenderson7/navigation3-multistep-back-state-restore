package com.example.navigator3example.data.densities

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DensityTestEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DensityTestDatabase : RoomDatabase() {
    abstract fun densityTestDao(): DensityTestDao

    companion object {
        @Volatile
        private var INSTANCE: DensityTestDatabase? = null

        fun getDatabase(context: Context): DensityTestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DensityTestDatabase::class.java,
                    "density_tests_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}