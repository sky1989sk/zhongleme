package com.lottery.app.ui.lottery

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lottery.app.domain.model.*
import com.lottery.app.ui.components.*
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LocalDesignStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotteryScreen(
    viewModel: LotteryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val style = LocalDesignStyle.current

    val contentPadding = when (style) {
        DesignStyle.HARMONY -> 20.dp
        DesignStyle.IOS26 -> 16.dp
        else -> 16.dp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        Text(
            text = "选号生成",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(if (style == DesignStyle.HARMONY) 20.dp else 16.dp))

        Text(
            text = "彩种",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        StyledSegmentedControl(
            items = LotteryType.entries.toList(),
            selectedItem = state.lotteryType,
            onItemSelected = viewModel::selectLotteryType,
            label = { it.displayName }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "玩法",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        StyledSegmentedControl(
            items = PlayType.entries.toList(),
            selectedItem = state.playType,
            onItemSelected = viewModel::selectPlayType,
            label = { it.displayName }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "随机策略",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        StyledSegmentedControl(
            items = GeneratorStrategy.entries.toList(),
            selectedItem = state.strategy,
            onItemSelected = viewModel::selectStrategy,
            label = { it.displayName }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (state.playType) {
            PlayType.STANDARD -> StandardParamsSection(
                count = state.standardCount,
                onCountChange = viewModel::updateStandardCount
            )
            PlayType.MULTIPLE -> MultipleParamsSection(
                state = state,
                onFrontCountChange = viewModel::updateMultipleFrontCount,
                onBackCountChange = viewModel::updateMultipleBackCount
            )
            PlayType.DAN_TUO -> DanTuoParamsSection(
                state = state,
                onFrontDanChange = viewModel::updateDanTuoFrontDan,
                onFrontTuoChange = viewModel::updateDanTuoFrontTuo,
                onBackDanChange = viewModel::updateDanTuoBackDan,
                onBackTuoChange = viewModel::updateDanTuoBackTuo
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        StyledButton(
            onClick = viewModel::generate,
            enabled = !state.isGenerating
        ) {
            if (state.isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("生成号码", style = MaterialTheme.typography.titleMedium)
            }
        }

        state.errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = state.lastResult != null,
            enter = fadeIn() + expandVertically()
        ) {
            state.lastResult?.let { result ->
                ResultSection(lotteryType = state.lotteryType, result = result)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "免责声明：本应用生成的号码仅供娱乐参考，不构成任何投注建议，中奖概率与手动选号完全一致。请理性购彩。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun StandardParamsSection(
    count: Int,
    onCountChange: (Int) -> Unit
) {
    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("生成注数", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            CounterRow(value = count, min = 1, max = 50, onChange = onCountChange)
        }
    }
}

@Composable
private fun MultipleParamsSection(
    state: LotteryUiState,
    onFrontCountChange: (Int) -> Unit,
    onBackCountChange: (Int) -> Unit
) {
    val rule = state.rule
    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${state.lotteryType.frontLabel}数量（${rule.frontMultipleMin}-${rule.frontMultipleMax}）",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            CounterRow(
                value = state.multipleFrontCount,
                min = rule.frontMultipleMin,
                max = rule.frontMultipleMax,
                onChange = onFrontCountChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${state.lotteryType.backLabel}数量（${rule.backMultipleMin}-${rule.backMultipleMax}）",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            CounterRow(
                value = state.multipleBackCount,
                min = rule.backMultipleMin,
                max = rule.backMultipleMax,
                onChange = onBackCountChange
            )
        }
    }
}

@Composable
private fun DanTuoParamsSection(
    state: LotteryUiState,
    onFrontDanChange: (Int) -> Unit,
    onFrontTuoChange: (Int) -> Unit,
    onBackDanChange: (Int) -> Unit,
    onBackTuoChange: (Int) -> Unit
) {
    val rule = state.rule
    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${state.lotteryType.frontLabel}胆码（1-${rule.frontDanMax}）",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            CounterRow(
                value = state.danTuoFrontDan,
                min = 1,
                max = rule.frontDanMax,
                onChange = onFrontDanChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            val minFrontTuo = (rule.frontPickCount + 1 - state.danTuoFrontDan).coerceAtLeast(2)
            Text(
                text = "${state.lotteryType.frontLabel}拖码（${minFrontTuo}-${rule.frontMax - state.danTuoFrontDan}）",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            CounterRow(
                value = state.danTuoFrontTuo,
                min = minFrontTuo,
                max = rule.frontMax - state.danTuoFrontDan,
                onChange = onFrontTuoChange
            )

            if (rule.backDanMax > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${state.lotteryType.backLabel}胆码（0-${rule.backDanMax}）",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                CounterRow(
                    value = state.danTuoBackDan,
                    min = 0,
                    max = rule.backDanMax,
                    onChange = onBackDanChange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val minBackTuo = if (state.danTuoBackDan > 0) {
                (rule.backPickCount + 1 - state.danTuoBackDan).coerceAtLeast(1)
            } else {
                rule.backPickCount
            }
            val backLabel = if (state.danTuoBackDan > 0) {
                "${state.lotteryType.backLabel}拖码（${minBackTuo}-${rule.backMax - state.danTuoBackDan}）"
            } else {
                "${state.lotteryType.backLabel}（${rule.backPickCount}-${rule.backMax}）"
            }
            Text(text = backLabel, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            CounterRow(
                value = state.danTuoBackTuo,
                min = minBackTuo,
                max = rule.backMax - state.danTuoBackDan,
                onChange = onBackTuoChange
            )
        }
    }
}

@Composable
private fun CounterRow(
    value: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit
) {
    val style = LocalDesignStyle.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledIconButton(
            onClick = { onChange(value - 1) },
            enabled = value > min,
            modifier = Modifier.size(36.dp),
            shape = when (style) {
                DesignStyle.HARMONY -> MaterialTheme.shapes.extraLarge
                DesignStyle.IOS26 -> MaterialTheme.shapes.small
                else -> MaterialTheme.shapes.small
            }
        ) { Text("-") }
        Text(
            text = "$value",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        FilledIconButton(
            onClick = { onChange(value + 1) },
            enabled = value < max,
            modifier = Modifier.size(36.dp),
            shape = when (style) {
                DesignStyle.HARMONY -> MaterialTheme.shapes.extraLarge
                DesignStyle.IOS26 -> MaterialTheme.shapes.small
                else -> MaterialTheme.shapes.small
            }
        ) { Text("+") }
    }
}

@Composable
private fun ResultSection(
    lotteryType: LotteryType,
    result: GenerateResult
) {
    Column {
        Text(
            text = "生成结果",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (result) {
            is GenerateResult.StandardResult -> {
                StandardResultCard(lotteryType = lotteryType, numbers = result.numbers)
            }
            is GenerateResult.MultipleResult -> {
                MultipleResultCard(lotteryType = lotteryType, numbers = result.numbers)
            }
            is GenerateResult.DanTuoResult -> {
                DanTuoResultCard(lotteryType = lotteryType, danTuo = result.danTuo)
            }
        }
    }
}
