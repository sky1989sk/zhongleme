package com.lottery.app.domain.model

data class TicketCheckResult(
    val index: Int,
    val isWin: Boolean,
    val level: String?,
    val amount: Long?,
    val hitRed: Int,
    val hitBlue: Int,
    val remark: String?
)

sealed class TicketCheckState {
    object Idle : TicketCheckState()
    object Loading : TicketCheckState()
    object NotSupported : TicketCheckState()
    object NoIssueNumber : TicketCheckState()
    data class Success(val results: List<TicketCheckResult>) : TicketCheckState()
    data class Error(val message: String) : TicketCheckState()
}
