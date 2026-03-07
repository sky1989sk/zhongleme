package com.lottery.app.ui.lottery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lottery.app.domain.model.*
import com.lottery.app.usecase.GenerateNumbersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LotteryUiState(
    val lotteryType: LotteryType = LotteryType.SSQ,
    val playType: PlayType = PlayType.STANDARD,
    val strategy: GeneratorStrategy = GeneratorStrategy.PURE_RANDOM,
    val standardCount: Int = 5,
    val multipleFrontCount: Int = 0,
    val multipleBackCount: Int = 0,
    val danTuoFrontDan: Int = 0,
    val danTuoFrontTuo: Int = 0,
    val danTuoBackDan: Int = 0,
    val danTuoBackTuo: Int = 0,
    val lastResult: GenerateResult? = null,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
) {
    val rule: LotteryRule get() = LotteryRule.of(lotteryType)
}

class LotteryViewModel(
    private val generateNumbersUseCase: GenerateNumbersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LotteryUiState())
    val uiState: StateFlow<LotteryUiState> = _uiState.asStateFlow()

    init {
        resetParamsForCurrentType()
    }

    fun selectLotteryType(type: LotteryType) {
        _uiState.update { it.copy(lotteryType = type, lastResult = null, errorMessage = null) }
        resetParamsForCurrentType()
    }

    fun selectPlayType(playType: PlayType) {
        _uiState.update { it.copy(playType = playType, lastResult = null, errorMessage = null) }
        resetParamsForCurrentType()
    }

    fun selectStrategy(strategy: GeneratorStrategy) {
        _uiState.update { it.copy(strategy = strategy) }
    }

    fun updateStandardCount(count: Int) {
        _uiState.update { it.copy(standardCount = count.coerceIn(1, 50)) }
    }

    fun updateMultipleFrontCount(count: Int) {
        val rule = _uiState.value.rule
        _uiState.update {
            it.copy(multipleFrontCount = count.coerceIn(rule.frontMultipleMin, rule.frontMultipleMax))
        }
    }

    fun updateMultipleBackCount(count: Int) {
        val rule = _uiState.value.rule
        _uiState.update {
            it.copy(multipleBackCount = count.coerceIn(rule.backMultipleMin, rule.backMultipleMax))
        }
    }

    fun updateDanTuoFrontDan(count: Int) {
        val rule = _uiState.value.rule
        _uiState.update {
            it.copy(danTuoFrontDan = count.coerceIn(1, rule.frontDanMax))
        }
    }

    fun updateDanTuoFrontTuo(count: Int) {
        val rule = _uiState.value.rule
        val minTuo = rule.frontPickCount + 1 - _uiState.value.danTuoFrontDan
        val maxTuo = rule.frontMax - _uiState.value.danTuoFrontDan
        _uiState.update {
            it.copy(danTuoFrontTuo = count.coerceIn(minTuo.coerceAtLeast(2), maxTuo))
        }
    }

    fun updateDanTuoBackDan(count: Int) {
        val rule = _uiState.value.rule
        _uiState.update {
            it.copy(danTuoBackDan = count.coerceIn(0, rule.backDanMax))
        }
    }

    fun updateDanTuoBackTuo(count: Int) {
        val rule = _uiState.value.rule
        val state = _uiState.value
        val minTuo = if (state.danTuoBackDan > 0) {
            rule.backPickCount + 1 - state.danTuoBackDan
        } else {
            rule.backPickCount
        }
        _uiState.update {
            it.copy(danTuoBackTuo = count.coerceIn(minTuo.coerceAtLeast(1), rule.backMax - state.danTuoBackDan))
        }
    }

    fun generate() {
        val state = _uiState.value
        if (state.isGenerating) return

        _uiState.update { it.copy(isGenerating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val result: GenerateResult = when (state.playType) {
                    PlayType.STANDARD -> generateNumbersUseCase.generateStandard(
                        state.lotteryType, state.standardCount, state.strategy
                    )
                    PlayType.MULTIPLE -> generateNumbersUseCase.generateMultiple(
                        state.lotteryType,
                        MultipleConfig(state.multipleFrontCount, state.multipleBackCount)
                    )
                    PlayType.DAN_TUO -> generateNumbersUseCase.generateDanTuo(
                        state.lotteryType,
                        DanTuoConfig(
                            frontDanCount = state.danTuoFrontDan,
                            frontTuoCount = state.danTuoFrontTuo,
                            backDanCount = state.danTuoBackDan,
                            backTuoCount = state.danTuoBackTuo
                        )
                    )
                }
                _uiState.update { it.copy(lastResult = result, isGenerating = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isGenerating = false, errorMessage = "生成失败: ${e.message}")
                }
            }
        }
    }

    private fun resetParamsForCurrentType() {
        val rule = _uiState.value.rule
        _uiState.update {
            it.copy(
                multipleFrontCount = rule.frontMultipleMin,
                multipleBackCount = rule.backMultipleMin,
                danTuoFrontDan = 1,
                danTuoFrontTuo = rule.frontPickCount,
                danTuoBackDan = if (rule.backDanMax > 0) 1 else 0,
                danTuoBackTuo = rule.backPickCount
            )
        }
    }

    class Factory(
        private val generateNumbersUseCase: GenerateNumbersUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LotteryViewModel(generateNumbersUseCase) as T
        }
    }
}
