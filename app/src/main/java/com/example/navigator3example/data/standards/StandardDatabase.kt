package com.example.navigator3example.data.standards

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(
    entities = [StandardEntity::class],
    version = 2,
    exportSchema = false
)
abstract class StandardDatabase : RoomDatabase() {
    
    abstract fun standardDao(): StandardDao
    
    companion object {
        @Volatile
        private var INSTANCE: StandardDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE standards ADD COLUMN gaugeSN TEXT NOT NULL DEFAULT ''")
            }
        }
        
        fun getDatabase(context: Context): StandardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StandardDatabase::class.java,
                    "standard_database"
                ).addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}