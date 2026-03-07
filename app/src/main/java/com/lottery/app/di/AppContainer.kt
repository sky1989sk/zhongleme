package com.lottery.app.di

import android.content.Context
import com.lottery.app.domain.generator.LotteryGenerator
import com.lottery.app.domain.repository.HistoryRepository
import com.lottery.app.domain.repository.PrizeResultRepository
import com.lottery.app.infra.algorithm.DefaultRandomGenerator
import com.lottery.app.infra.local.RoomHistoryRepository
import com.lottery.app.infra.local.db.AppDatabase
import com.lottery.app.infra.remote.StubPrizeResultRepository
import com.lottery.app.usecase.DeleteHistoryUseCase
import com.lottery.app.usecase.GenerateNumbersUseCase
import com.lottery.app.usecase.QueryHistoryUseCase
import com.lottery.app.usecase.UpdateWonStatusUseCase

class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val lotteryGenerator: LotteryGenerator = DefaultRandomGenerator()

    val historyRepository: HistoryRepository = RoomHistoryRepository(database.historyDao())

    val prizeResultRepository: PrizeResultRepository = StubPrizeResultRepository()

    val generateNumbersUseCase by lazy {
        GenerateNumbersUseCase(lotteryGenerator, historyRepository)
    }

    val queryHistoryUseCase by lazy {
        QueryHistoryUseCase(historyRepository)
    }

    val deleteHistoryUseCase by lazy {
        DeleteHistoryUseCase(historyRepository)
    }

    val updateWonStatusUseCase by lazy {
        UpdateWonStatusUseCase(historyRepository)
    }
}
