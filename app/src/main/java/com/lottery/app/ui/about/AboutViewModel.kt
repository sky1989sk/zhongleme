package com.lottery.app.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lottery.app.domain.model.ChangelogEntry
import com.lottery.app.infra.remote.UpdateApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AboutViewModel(
    private val updateApi: UpdateApi
) : ViewModel() {

    private val _changelog = MutableStateFlow<List<ChangelogEntry>>(emptyList())
    val changelog: StateFlow<List<ChangelogEntry>> = _changelog.asStateFlow()

    init {
        loadChangelog()
    }

    fun loadChangelog() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                updateApi.fetchChangelog() ?: emptyList()
            }
            _changelog.value = list
        }
    }

    class Factory(private val updateApi: UpdateApi) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AboutViewModel(updateApi) as T
    }
}
