package com.example.navigator3example.data.nuke

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NukeGaugeCalibration::class],
    version = 2,
    exportSchema = false
)
abstract class NukeGaugeDatabase : RoomDatabase() {
    abstract fun nukeGaugeDao(): NukeGaugeDao

    companion object {
        @Volatile
        private var INSTANCE: NukeGaugeDatabase? = null

        // Migration from version 1 (weightA/weightB) to version 2 (serialNumber)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table with the v2 schema
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS nukeCalibrations_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "serialNumber TEXT NOT NULL DEFAULT '', " +
                        "date INTEGER NOT NULL, " +
                        "timestamp INTEGER NOT NULL)"
                )

                // Copy over id, date, timestamp from old table; set serialNumber to empty string
                // Old table had columns: id, weightA, weightB, date, timestamp
                database.execSQL(
                    "INSERT INTO nukeCalibrations_new (id, serialNumber, date, timestamp) " +
                        "SELECT id, '' AS serialNumber, date, timestamp FROM nukeCalibrations"
                )

                // Drop old table and rename new to original name
                database.execSQL("DROP TABLE nukeCalibrations")
                database.execSQL("ALTER TABLE nukeCalibrations_new RENAME TO nukeCalibrations")
            }
        }

        fun getDatabase(context: Context): NukeGaugeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NukeGaugeDatabase::class.java,
                    "nuke_gauge_database"
                ).addMigrations(MIGRATION_1_2)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
