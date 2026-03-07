package com.lottery.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lottery.app.domain.model.*

@Composable
fun StandardResultCard(
    lotteryType: LotteryType,
    numbers: List<LotteryNumber>,
    modifier: Modifier = Modifier
) {
    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${lotteryType.displayName} · 普通",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            numbers.forEachIndexed { index, num ->
                if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                NumberRow(lotteryType = lotteryType, number = num)
            }
        }
    }
}

@Composable
fun MultipleResultCard(
    lotteryType: LotteryType,
    numbers: LotteryNumber,
    modifier: Modifier = Modifier
) {
    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${lotteryType.displayName} · 复式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = lotteryType.frontLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            BallFlowRow(numbers = numbers.frontNumbers, ballType = BallType.RED)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = lotteryType.backLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            BallFlowRow(numbers = numbers.backNumbers, ballType = BallType.BLUE)
        }
    }
}

@Composable
fun DanTuoResultCard(
    lotteryType: LotteryType,
    danTuo: DanTuoNumber,
    modifier: Modifier = Modifier
) {
    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${lotteryType.displayName} · 胆拖",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${lotteryType.frontLabel}胆码",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            BallFlowRow(numbers = danTuo.frontDan, ballType = BallType.DAN)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${lotteryType.frontLabel}拖码",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            BallFlowRow(numbers = danTuo.frontTuo, ballType = BallType.RED)

            if (danTuo.backDan.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${lotteryType.backLabel}胆码",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                BallFlowRow(numbers = danTuo.backDan, ballType = BallType.DAN)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (danTuo.backDan.isNotEmpty()) "${lotteryType.backLabel}拖码" else lotteryType.backLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            BallFlowRow(numbers = danTuo.backTuo, ballType = BallType.BLUE)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberRow(
    lotteryType: LotteryType,
    number: LotteryNumber,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        number.frontNumbers.forEach { num ->
            NumberBall(number = num, ballType = BallType.RED, size = 36.dp)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier.height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "|", color = MaterialTheme.colorScheme.outlineVariant)
        }
        Spacer(modifier = Modifier.width(4.dp))
        number.backNumbers.forEach { num ->
            NumberBall(number = num, ballType = BallType.BLUE, size = 36.dp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BallFlowRow(
    numbers: List<Int>,
    ballType: BallType,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        numbers.forEach { num ->
            NumberBall(number = num, ballType = ballType, size = 36.dp)
        }
    }
}

@Composable
fun BallRow(
    numbers: List<Int>,
    ballType: BallType,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        numbers.forEach { num ->
            NumberBall(number = num, ballType = ballType, size = 36.dp)
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}
