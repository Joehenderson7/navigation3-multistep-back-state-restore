package com.example.navigator3example.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [StandardEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StandardDatabase : RoomDatabase() {
    
    abstract fun standardDao(): StandardDao
    
    companion object {
        @Volatile
        private var INSTANCE: StandardDatabase? = null
        
        fun getDatabase(context: Context): StandardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StandardDatabase::class.java,
                    "standard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}