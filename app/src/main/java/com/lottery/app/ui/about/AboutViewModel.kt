package com.lottery.app.ui.about

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lottery.app.BuildConfig
import com.lottery.app.data.UpdateServerPreference
import com.lottery.app.domain.model.ChangelogEntry
import com.lottery.app.infra.remote.UpdateApi
import com.lottery.app.usecase.CheckUpdateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AboutViewModel(
    private val context: Context,
    private val updateApi: UpdateApi,
    private val checkUpdateUseCase: CheckUpdateUseCase,
    private val defaultServerUrl: String
) : ViewModel() {

    private val _changelog = MutableStateFlow<List<ChangelogEntry>>(emptyList())
    val changelog: StateFlow<List<ChangelogEntry>> = _changelog.asStateFlow()

    private val _updateLogs = MutableStateFlow<List<String>>(emptyList())
    val updateLogs: StateFlow<List<String>> = _updateLogs.asStateFlow()

    private val _overrideUrl = MutableStateFlow("")
    val overrideUrl: StateFlow<String> = _overrideUrl.asStateFlow()

    init {
        loadChangelog()
    }

    fun loadChangelog() {
        viewModelScope.launch {
            val baseUrl = withContext(Dispatchers.IO) {
                UpdateServerPreference.getEffectiveBaseUrl(context, defaultServerUrl)
            }
            val list = withContext(Dispatchers.IO) {
                updateApi.fetchChangelog(baseUrl.takeIf { it.isNotBlank() }) ?: emptyList()
            }
            _changelog.value = list
        }
    }

    fun loadOverrideUrl() {
        viewModelScope.launch {
            val url = withContext(Dispatchers.IO) {
                UpdateServerPreference.getOverrideUrl(context)
            }
            _overrideUrl.value = url
        }
    }

    fun setOverrideUrl(url: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                UpdateServerPreference.setOverrideUrl(context, url)
            }
            _overrideUrl.value = url.trim()
        }
    }

    fun checkUpdateWithLogs() {
        viewModelScope.launch {
            _updateLogs.value = emptyList()
            val onLog: (String) -> Unit = { log ->
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    _updateLogs.value = _updateLogs.value + log
                }
            }
            checkUpdateUseCase.check(onLog = onLog)
        }
    }

    class Factory(
        private val context: Context,
        private val updateApi: UpdateApi,
        private val checkUpdateUseCase: CheckUpdateUseCase,
        private val defaultServerUrl: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AboutViewModel(context, updateApi, checkUpdateUseCase, defaultServerUrl) as T
    }
}
