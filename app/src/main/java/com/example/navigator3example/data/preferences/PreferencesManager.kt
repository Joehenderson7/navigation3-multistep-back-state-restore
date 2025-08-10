package com.example.navigator3example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Centralized preferences manager using Jetpack DataStore (Preferences).
 *
 * Stored preferences:
 * - dark theme enabled (Boolean)
 * - selected gauge serial number (String)
 * - rice calibration value (Float)
 *
 * Usage example:
 *
 * val prefs = PreferencesManager.get(context)
 * lifecycleScope.launch {
 *     prefs.setDarkThemeEnabled(true)
 * }
 * val isDarkFlow = prefs.darkThemeEnabled
 */
class PreferencesManager private constructor(private val dataStore: DataStore<Preferences>) {

    // Keys
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme_enabled")
        val GAUGE_SERIAL = stringPreferencesKey("gauge_serial_number")
        val RICE_CALIBRATION = floatPreferencesKey("rice_calibration")
        // New: last selected rice calibration id (Room id)
        val LAST_RICE_CALIBRATION_ID = androidx.datastore.preferences.core.longPreferencesKey("last_rice_calibration_id")
        // New: correction factor stored as string (to preserve exact user input formatting)
        val CORRECTION_FACTOR = stringPreferencesKey("correction_factor")
        val NEXT_DENSITY_TEST_NUMBER = intPreferencesKey("next_density_test_number")
    }

    // Flows (with defaults)
    val darkThemeEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DARK_THEME] ?: false
    }

    val gaugeSerialNumber: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.GAUGE_SERIAL] ?: ""
    }

    val riceCalibration: Flow<Float> = dataStore.data.map { prefs ->
        prefs[Keys.RICE_CALIBRATION] ?: 0f
    }

    // New: Flow for last selected rice calibration id (-1L means none saved)
    val lastRiceCalibrationId: Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_RICE_CALIBRATION_ID] ?: -1L
    }

    // New: Flow for last used correction factor (empty means not set)
    val correctionFactor: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.CORRECTION_FACTOR] ?: ""
    }

    // New: Flow for next density test number (default 1)
    val nextDensityTestNumber: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.NEXT_DENSITY_TEST_NUMBER] ?: 1
    }

    // Setters
    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setGaugeSerialNumber(serial: String) {
        dataStore.edit { it[Keys.GAUGE_SERIAL] = serial }
    }

    suspend fun setRiceCalibration(value: Float) {
        dataStore.edit { it[Keys.RICE_CALIBRATION] = value }
    }

    // New: set last selected rice calibration id
    suspend fun setLastRiceCalibrationId(id: Long) {
        dataStore.edit { it[Keys.LAST_RICE_CALIBRATION_ID] = id }
    }

    // New: set correction factor string
    suspend fun setCorrectionFactor(value: String) {
        dataStore.edit { it[Keys.CORRECTION_FACTOR] = value }
    }

    // New: set next density test number explicitly
    suspend fun setNextDensityTestNumber(value: Int) {
        dataStore.edit { it[Keys.NEXT_DENSITY_TEST_NUMBER] = value }
    }

    // New: increment next density test number atomically
    suspend fun incrementNextDensityTestNumber() {
        dataStore.edit { prefs ->
            val current = prefs[Keys.NEXT_DENSITY_TEST_NUMBER] ?: 1
            prefs[Keys.NEXT_DENSITY_TEST_NUMBER] = current + 1
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private const val STORE_NAME = "user_prefs"

        // Application-wide DataStore instance via Context extension
        private val Context.dataStore by preferencesDataStore(name = STORE_NAME)

        fun get(context: Context): PreferencesManager = PreferencesManager(context.dataStore)
    }
}
