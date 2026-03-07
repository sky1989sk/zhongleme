package com.lottery.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lottery.app.domain.model.HistoryRecord
import com.lottery.app.domain.model.LotteryType
import com.lottery.app.domain.model.WonStatus
import com.lottery.app.usecase.DeleteHistoryUseCase
import com.lottery.app.usecase.QueryHistoryUseCase
import com.lottery.app.usecase.UpdateWonStatusUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val records: List<HistoryRecord> = emptyList(),
    val filterType: LotteryType? = null,
    val showClearConfirm: Boolean = false
)

class HistoryViewModel(
    private val queryHistoryUseCase: QueryHistoryUseCase,
    private val deleteHistoryUseCase: DeleteHistoryUseCase,
    private val updateWonStatusUseCase: UpdateWonStatusUseCase
) : ViewModel() {

    private val _filterType = MutableStateFlow<LotteryType?>(null)

    val uiState: StateFlow<HistoryUiState> = _filterType.flatMapLatest { type ->
        val flow = if (type != null) {
            queryHistoryUseCase.getHistoryByType(type)
        } else {
            queryHistoryUseCase.getAllHistory()
        }
        flow.map { records ->
            HistoryUiState(records = records, filterType = type)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    private val _showClearConfirm = MutableStateFlow(false)
    val showClearConfirm: StateFlow<Boolean> = _showClearConfirm.asStateFlow()

    fun setFilter(type: LotteryType?) {
        _filterType.value = type
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            deleteHistoryUseCase.deleteRecord(id)
        }
    }

    fun updateWonStatus(recordId: Long, status: WonStatus) {
        viewModelScope.launch {
            updateWonStatusUseCase(recordId, status)
        }
    }

    fun requestClearAll() {
        _showClearConfirm.value = true
    }

    fun dismissClearConfirm() {
        _showClearConfirm.value = false
    }

    fun confirmClearAll() {
        viewModelScope.launch {
            deleteHistoryUseCase.clearAll()
            _showClearConfirm.value = false
        }
    }

    class Factory(
        private val queryHistoryUseCase: QueryHistoryUseCase,
        private val deleteHistoryUseCase: DeleteHistoryUseCase,
        private val updateWonStatusUseCase: UpdateWonStatusUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(queryHistoryUseCase, deleteHistoryUseCase, updateWonStatusUseCase) as T
        }
    }
}
