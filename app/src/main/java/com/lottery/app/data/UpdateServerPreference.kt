package com.lottery.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.updateServerDataStore by preferencesDataStore(name = "update_server")

object UpdateServerPreference {
    private val KEY_OVERRIDE_URL = stringPreferencesKey("override_url")

    suspend fun getOverrideUrl(context: Context): String {
        return context.updateServerDataStore.data.first()[KEY_OVERRIDE_URL] ?: ""
    }

    suspend fun setOverrideUrl(context: Context, url: String) {
        context.updateServerDataStore.edit { prefs ->
            prefs[KEY_OVERRIDE_URL] = url.trim()
        }
    }

    suspend fun getEffectiveBaseUrl(context: Context, defaultUrl: String): String {
        val override = context.updateServerDataStore.data.first()[KEY_OVERRIDE_URL] ?: ""
        return override.takeIf { it.isNotBlank() } ?: defaultUrl
    }
}
