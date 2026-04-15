package com.dodamsoft.ajangajang.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajang_prefs")

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val activeChildId: Long? = null,
)

class AppPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ACTIVE_CHILD_ID = longPreferencesKey("active_child_id")
    }

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            activeChildId = prefs[Keys.ACTIVE_CHILD_ID]?.takeIf { it >= 0L },
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setActiveChildId(id: Long?) {
        context.dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(Keys.ACTIVE_CHILD_ID)
            } else {
                prefs[Keys.ACTIVE_CHILD_ID] = id
            }
        }
    }
}
