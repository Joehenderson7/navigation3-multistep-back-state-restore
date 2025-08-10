package com.example.navigator3example.data.rice

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RiceEntity::class, RiceCalibration::class],
    version = 1,
    exportSchema = false
)
abstract class RiceDatabase : RoomDatabase() {
    abstract fun riceDao(): RiceDao

    companion object {
        @Volatile
        private var INSTANCE: RiceDatabase? = null

        fun getDatabase(context: Context): RiceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RiceDatabase::class.java,
                    "rice_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
