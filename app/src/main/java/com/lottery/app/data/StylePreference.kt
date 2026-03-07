package com.lottery.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lottery.app.ui.theme.DesignStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object StylePreference {
    private val KEY_DESIGN_STYLE = stringPreferencesKey("design_style")

    fun designStyleFlow(context: Context): Flow<DesignStyle> =
        context.dataStore.data.map { prefs ->
            val name = prefs[KEY_DESIGN_STYLE] ?: DesignStyle.MATERIAL.name
            try {
                DesignStyle.valueOf(name)
            } catch (_: Exception) {
                DesignStyle.MATERIAL
            }
        }

    suspend fun setDesignStyle(context: Context, style: DesignStyle) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DESIGN_STYLE] = style.name
        }
    }
}
