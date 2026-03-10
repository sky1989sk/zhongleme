package com.lottery.app.ui.about

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lottery.app.BuildConfig
import com.lottery.app.data.QueryServerPreference
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

    private val _queryOverrideUrl = MutableStateFlow("")
    val queryOverrideUrl: StateFlow<String> = _queryOverrideUrl.asStateFlow()

    private val _queryLogs = MutableStateFlow<List<String>>(emptyList())
    val queryLogs: StateFlow<List<String>> = _queryLogs.asStateFlow()

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

    fun loadQueryOverrideUrl() {
        viewModelScope.launch {
            val url = withContext(Dispatchers.IO) {
                QueryServerPreference.getOverrideUrl(context)
            }
            _queryOverrideUrl.value = url
        }
    }

    fun setQueryOverrideUrl(url: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                QueryServerPreference.setOverrideUrl(context, url)
            }
            _queryOverrideUrl.value = url.trim()
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

    fun checkQueryServerWithLogs() {
        viewModelScope.launch {
            _queryLogs.value = emptyList()
            val logs = mutableListOf<String>()
            fun log(msg: String) {
                logs += msg
                _queryLogs.value = logs.toList()
            }
            withContext(Dispatchers.IO) {
                try {
                    val baseUrl = QueryServerPreference.getEffectiveBaseUrl(context, BuildConfig.QUERY_SERVER_BASE_URL)
                    if (baseUrl.isBlank()) {
                        log("未配置查询服务器地址")
                        return@withContext
                    }
                    val url = baseUrl.trimEnd('/') + "/api/ssq/latest"
                    log("请求: $url")
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                    val request = okhttp3.Request.Builder().url(url).get().build()
                    client.newCall(request).execute().use { response ->
                        log("响应: ${response.code} ${response.message}")
                        val body = response.body?.string()
                        if (body.isNullOrBlank()) {
                            log("响应体为空")
                        } else {
                            log("响应体前 200 字符: " + body.take(200))
                        }
                    }
                } catch (e: Exception) {
                    log("失败: ${e.message ?: "unknown"}")
                }
            }
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
