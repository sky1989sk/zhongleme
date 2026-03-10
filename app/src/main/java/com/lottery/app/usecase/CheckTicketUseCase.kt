package com.lottery.app.usecase

import com.lottery.app.domain.model.GenerateResult
import com.lottery.app.domain.model.HistoryRecord
import com.lottery.app.domain.model.TicketCheckResult
import com.lottery.app.domain.model.TicketCheckState
import com.lottery.app.infra.remote.CheckTicketApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheckTicketUseCase(
    private val api: CheckTicketApi,
    private val getEffectiveBaseUrl: suspend () -> String
) {

    suspend operator fun invoke(record: HistoryRecord): TicketCheckState {
        if (record.issueNumber.isBlank()) {
            return TicketCheckState.NoIssueNumber
        }

        val tickets = when (val result = record.result) {
            is GenerateResult.DanTuoResult -> return TicketCheckState.NotSupported
            is GenerateResult.StandardResult -> result.numbers
            is GenerateResult.MultipleResult -> listOf(result.numbers)
        }

        if (tickets.isEmpty()) {
            return TicketCheckState.Error("号码数据为空")
        }

        return withContext(Dispatchers.IO) {
            val baseUrl = getEffectiveBaseUrl()
            when (val result = api.checkTickets(baseUrl, record.lotteryType, record.issueNumber, tickets)) {
                is CheckTicketApi.CheckTicketResult.Error ->
                    TicketCheckState.Error(result.message)
                is CheckTicketApi.CheckTicketResult.Success ->
                    TicketCheckState.Success(
                        results = result.items.map { item ->
                            TicketCheckResult(
                                index = item.index,
                                isWin = item.isWin,
                                level = item.level,
                                amount = item.amount,
                                hitRed = item.hitRed,
                                hitBlue = item.hitBlue,
                                remark = item.remark
                            )
                        }
                    )
            }
        }
    }
}
