package com.threed.manager.core.data.prefs

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
 * App-wide DataStore<Preferences> for settings / onboarding state.
 * Backed by a single [preferencesDataStore] delegate.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class PreferencesRepository(private val context: Context) {

    private val store get() = context.dataStore

    val settings: Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            renderBackend = prefs[KEY_BACKEND] ?: "Web Spark",
            fpsTarget = prefs[KEY_FPS] ?: 60,
            debugOverlay = prefs[KEY_DEBUG] ?: false,
            sensitivity = prefs[KEY_SENSITIVITY] ?: 1.0f,
            invertX = prefs[KEY_INVERT_X] ?: false,
            invertZ = prefs[KEY_INVERT_Z] ?: false,
            deadbandDeg = prefs[KEY_DEADBAND] ?: 5f,
            reduceMotion = prefs[KEY_REDUCE_MOTION] ?: false,
            boldText = prefs[KEY_BOLD] ?: false,
            onboardingComplete = prefs[KEY_ONBOARDED] ?: false,
            captureGps = prefs[KEY_CAPTURE_GPS] ?: true,
        )
    }

    suspend fun setRenderBackend(value: String) = store.edit { it[KEY_BACKEND] = value }
    suspend fun setFpsTarget(value: Int) = store.edit { it[KEY_FPS] = value }
    suspend fun setDebugOverlay(value: Boolean) = store.edit { it[KEY_DEBUG] = value }
    suspend fun setSensitivity(value: Float) = store.edit { it[KEY_SENSITIVITY] = value }
    suspend fun setInvertX(value: Boolean) = store.edit { it[KEY_INVERT_X] = value }
    suspend fun setInvertZ(value: Boolean) = store.edit { it[KEY_INVERT_Z] = value }
    suspend fun setReduceMotion(value: Boolean) = store.edit { it[KEY_REDUCE_MOTION] = value }
    suspend fun setBoldText(value: Boolean) = store.edit { it[KEY_BOLD] = value }
    suspend fun markOnboardingComplete() = store.edit { it[KEY_ONBOARDED] = true }
    suspend fun setCaptureGps(value: Boolean) = store.edit { it[KEY_CAPTURE_GPS] = value }

    private companion object {
        val KEY_BACKEND = stringPreferencesKey("render_backend")
        val KEY_FPS = intPreferencesKey("fps_target")
        val KEY_DEBUG = booleanPreferencesKey("debug_overlay")
        val KEY_SENSITIVITY = floatPreferencesKey("sensor_sensitivity")
        val KEY_INVERT_X = booleanPreferencesKey("invert_x")
        val KEY_INVERT_Z = booleanPreferencesKey("invert_z")
        val KEY_DEADBAND = floatPreferencesKey("deadband_deg")
        val KEY_REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val KEY_BOLD = booleanPreferencesKey("bold_text")
        val KEY_ONBOARDED = booleanPreferencesKey("onboarding_complete")
        val KEY_CAPTURE_GPS = booleanPreferencesKey("capture_gps")
    }
}

data class AppSettings(
    val renderBackend: String,
    val fpsTarget: Int,
    val debugOverlay: Boolean,
    val sensitivity: Float,
    val invertX: Boolean,
    val invertZ: Boolean,
    val deadbandDeg: Float,
    val reduceMotion: Boolean,
    val boldText: Boolean,
    val onboardingComplete: Boolean,
    val captureGps: Boolean,
)