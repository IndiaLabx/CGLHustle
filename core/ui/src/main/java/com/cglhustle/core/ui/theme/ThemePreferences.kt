package com.cglhustle.core.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val isDarkMode: Flow<Boolean> = context.themeDataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: false // Default to light mode as requested
        }

    suspend fun setDarkMode(isDark: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }
}
