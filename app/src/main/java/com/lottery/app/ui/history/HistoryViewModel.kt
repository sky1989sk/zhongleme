package com.lottery.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lottery.app.domain.model.HistoryRecord
import com.lottery.app.domain.model.LotteryType
import com.lottery.app.domain.model.TicketCheckState
import com.lottery.app.domain.model.WonStatus
import com.lottery.app.usecase.CheckTicketUseCase
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
    private val updateWonStatusUseCase: UpdateWonStatusUseCase,
    private val checkTicketUseCase: CheckTicketUseCase
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

    private val _checkStates = MutableStateFlow<Map<Long, TicketCheckState>>(emptyMap())
    val checkStates: StateFlow<Map<Long, TicketCheckState>> = _checkStates.asStateFlow()

    fun setFilter(type: LotteryType?) {
        _filterType.value = type
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            deleteHistoryUseCase.deleteRecord(id)
            // 清理已缓存的查询结果
            _checkStates.update { it - id }
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
            _checkStates.value = emptyMap()
        }
    }

    fun checkTicket(record: HistoryRecord) {
        val current = _checkStates.value[record.id]
        // 防呆：正在加载或已成功则不重复调用
        if (current is TicketCheckState.Loading || current is TicketCheckState.Success) return

        _checkStates.update { it + (record.id to TicketCheckState.Loading) }
        viewModelScope.launch {
            val result = checkTicketUseCase(record)
            _checkStates.update { it + (record.id to result) }
        }
    }

    class Factory(
        private val queryHistoryUseCase: QueryHistoryUseCase,
        private val deleteHistoryUseCase: DeleteHistoryUseCase,
        private val updateWonStatusUseCase: UpdateWonStatusUseCase,
        private val checkTicketUseCase: CheckTicketUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(
                queryHistoryUseCase,
                deleteHistoryUseCase,
                updateWonStatusUseCase,
                checkTicketUseCase
            ) as T
        }
    }
}
