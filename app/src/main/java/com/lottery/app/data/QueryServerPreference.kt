package com.lottery.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.queryServerDataStore by preferencesDataStore(name = "query_server")

object QueryServerPreference {
    private val KEY_OVERRIDE_URL = stringPreferencesKey("override_url")

    suspend fun getOverrideUrl(context: Context): String {
        return context.queryServerDataStore.data.first()[KEY_OVERRIDE_URL] ?: ""
    }

    suspend fun setOverrideUrl(context: Context, url: String) {
        context.queryServerDataStore.edit { prefs ->
            prefs[KEY_OVERRIDE_URL] = url.trim()
        }
    }

    suspend fun getEffectiveBaseUrl(context: Context, defaultUrl: String): String {
        val override = context.queryServerDataStore.data.first()[KEY_OVERRIDE_URL] ?: ""
        return override.takeIf { it.isNotBlank() } ?: defaultUrl
    }
}

