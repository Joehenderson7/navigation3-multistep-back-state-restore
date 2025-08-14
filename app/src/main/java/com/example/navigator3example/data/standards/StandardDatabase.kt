package com.example.navigator3example.data.standards

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.navigator3example.data.nuke.NukeGaugeCalibration
import com.example.navigator3example.data.nuke.NukeGaugeDao

@Database(
    entities = [StandardEntity::class, NukeGaugeCalibration::class],
    version = 3,
    exportSchema = false
)
abstract class StandardDatabase : RoomDatabase() {

    abstract fun standardDao(): StandardDao
    abstract fun nukeGaugeDao(): NukeGaugeDao

    companion object {
        @Volatile
        private var INSTANCE: StandardDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE standards ADD COLUMN gaugeSN TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Ensure parent table exists in this DB (previously managed in a separate DB)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS nukeCalibrations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "serialNumber TEXT NOT NULL DEFAULT '', " +
                        "date INTEGER NOT NULL, " +
                        "timestamp INTEGER NOT NULL)"
                )

                // Recreate standards table to add FK constraint to nukeCalibrations(id)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS standards_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "serialNumber TEXT NOT NULL, " +
                        "date INTEGER NOT NULL, " +
                        "densityCount INTEGER NOT NULL, " +
                        "moistureCount INTEGER NOT NULL, " +
                        "timestamp INTEGER NOT NULL, " +
                        "gaugeSN TEXT NOT NULL, " +
                        "calibrationId INTEGER, " +
                        "FOREIGN KEY(calibrationId) REFERENCES nukeCalibrations(id) ON UPDATE NO ACTION ON DELETE SET NULL)"
                )

                // Copy existing data (no calibrationId values previously)
                database.execSQL(
                    "INSERT INTO standards_new (id, serialNumber, date, densityCount, moistureCount, timestamp, gaugeSN) " +
                        "SELECT id, serialNumber, date, densityCount, moistureCount, timestamp, gaugeSN FROM standards"
                )

                database.execSQL("DROP TABLE standards")
                database.execSQL("ALTER TABLE standards_new RENAME TO standards")

                // Create indices to support FK and lookups
                database.execSQL("CREATE INDEX IF NOT EXISTS index_standards_calibrationId ON standards(calibrationId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_standards_serialNumber ON standards(serialNumber)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_standards_gaugeSN ON standards(gaugeSN)")
            }
        }

        fun getDatabase(context: Context): StandardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StandardDatabase::class.java,
                    "standard_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}