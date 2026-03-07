package com.lottery.app.ui.history

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lottery.app.domain.model.*
import com.lottery.app.ui.components.*
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LocalDesignStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val showClearConfirm by viewModel.showClearConfirm.collectAsState()
    val style = LocalDesignStyle.current

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (style == DesignStyle.HARMONY) 20.dp else 16.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "历史记录",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (state.records.isNotEmpty()) {
                IconButton(onClick = viewModel::requestClearAll) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "清空全部",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        FilterSection(
            selected = state.filterType,
            onSelect = viewModel::setFilter,
            modifier = Modifier.padding(horizontal = if (style == DesignStyle.HARMONY) 20.dp else 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.records.isEmpty()) {
            EmptyHistoryHint(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = if (style == DesignStyle.HARMONY) 20.dp else 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(
                    if (style == DesignStyle.HARMONY) 16.dp else 12.dp
                )
            ) {
                items(
                    items = state.records,
                    key = { it.id }
                ) { record ->
                    HistoryItem(
                        record = record,
                        onDelete = { viewModel.deleteRecord(record.id) },
                        onWonStatusChange = { status ->
                            viewModel.updateWonStatus(record.id, status)
                        }
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearConfirm,
            title = { Text("确认清空") },
            text = { Text("确定要清空所有历史记录吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmClearAll,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("清空") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissClearConfirm) { Text("取消") }
            }
        )
    }
}

@Composable
private fun FilterSection(
    selected: LotteryType?,
    onSelect: (LotteryType?) -> Unit,
    modifier: Modifier = Modifier
) {
    data class FilterItem(val type: LotteryType?, val label: String)

    val filterItems = listOf(FilterItem(null, "全部")) +
        LotteryType.entries.map { FilterItem(it, it.displayName) }

    StyledSegmentedControl(
        items = filterItems,
        selectedItem = filterItems.first { it.type == selected },
        onItemSelected = { onSelect(it.type) },
        label = { it.label },
        modifier = modifier
    )
}

@Composable
private fun HistoryItem(
    record: HistoryRecord,
    onDelete: () -> Unit,
    onWonStatusChange: (WonStatus) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    StyledCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${record.lotteryType.displayName} · ${record.playType.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (record.issueNumber.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "第${record.issueNumber}期",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = formatTime(record.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val result = record.result) {
                is GenerateResult.StandardResult -> {
                    result.numbers.forEachIndexed { index, num ->
                        if (index > 0) Spacer(modifier = Modifier.height(4.dp))
                        NumberRow(lotteryType = record.lotteryType, number = num)
                    }
                }
                is GenerateResult.MultipleResult -> {
                    NumberRow(lotteryType = record.lotteryType, number = result.numbers)
                }
                is GenerateResult.DanTuoResult -> {
                    DanTuoCompactView(
                        lotteryType = record.lotteryType,
                        danTuo = result.danTuo
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "中奖状态：",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box {
                    WonStatusChip(
                        status = record.wonStatus,
                        onClick = { showStatusMenu = true }
                    )
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        WonStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.displayName) },
                                onClick = {
                                    onWonStatusChange(status)
                                    showStatusMenu = false
                                },
                                leadingIcon = {
                                    WonStatusDot(status)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WonStatusChip(
    status: WonStatus,
    onClick: () -> Unit
) {
    val (bgColor, textColor) = when (status) {
        WonStatus.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        WonStatus.WON -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        WonStatus.NOT_WON -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WonStatusDot(status: WonStatus) {
    val color = when (status) {
        WonStatus.UNKNOWN -> MaterialTheme.colorScheme.outline
        WonStatus.WON -> MaterialTheme.colorScheme.primary
        WonStatus.NOT_WON -> MaterialTheme.colorScheme.error
    }
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}

@Composable
private fun DanTuoCompactView(
    lotteryType: LotteryType,
    danTuo: DanTuoNumber
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "胆",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(24.dp)
            )
            BallFlowRow(numbers = danTuo.frontDan, ballType = BallType.DAN)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "拖",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(24.dp)
            )
            BallFlowRow(numbers = danTuo.frontTuo, ballType = BallType.RED)
        }
        if (danTuo.backDan.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "胆",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(24.dp)
                )
                BallFlowRow(numbers = danTuo.backDan, ballType = BallType.DAN)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (danTuo.backDan.isNotEmpty()) "拖" else "",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(24.dp)
            )
            BallFlowRow(numbers = danTuo.backTuo, ballType = BallType.BLUE)
        }
    }
}

@Composable
private fun EmptyHistoryHint(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无历史记录\n去「选号生成」生成一些号码吧",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
