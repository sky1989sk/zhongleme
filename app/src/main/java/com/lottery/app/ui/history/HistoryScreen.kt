package com.lottery.app.ui.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val checkStates by viewModel.checkStates.collectAsState()
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
                        checkState = checkStates[record.id] ?: TicketCheckState.Idle,
                        onDelete = { viewModel.deleteRecord(record.id) },
                        onWonStatusChange = { status ->
                            viewModel.updateWonStatus(record.id, status)
                        },
                        onCheckTicket = { viewModel.checkTicket(record) }
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
    checkState: TicketCheckState,
    onDelete: () -> Unit,
    onWonStatusChange: (WonStatus) -> Unit,
    onCheckTicket: () -> Unit
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
                    if (record.drawDate.isNotEmpty()) {
                        Text(
                            text = "开奖日期：${record.drawDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            CheckTicketSection(
                record = record,
                checkState = checkState,
                onCheckTicket = onCheckTicket
            )
        }
    }
}

@Composable
private fun CheckTicketSection(
    record: HistoryRecord,
    checkState: TicketCheckState,
    onCheckTicket: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (checkState) {
            is TicketCheckState.Idle -> {
                CheckTicketButton(
                    record = record,
                    isLoading = false,
                    onClick = onCheckTicket
                )
            }
            is TicketCheckState.Loading -> {
                CheckTicketButton(
                    record = record,
                    isLoading = true,
                    onClick = {}
                )
            }
            is TicketCheckState.NotSupported -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "胆拖玩法暂不支持在线查询",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is TicketCheckState.NoIssueNumber -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "无期号信息，无法查询",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is TicketCheckState.Error -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = checkState.message,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onCheckTicket,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "重新查询",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            is TicketCheckState.Success -> {
                CheckTicketResults(
                    lotteryType = record.lotteryType,
                    results = checkState.results
                )
            }
        }
    }
}

@Composable
private fun CheckTicketButton(
    record: HistoryRecord,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val isDanTuo = record.result is GenerateResult.DanTuoResult
    val hasIssue = record.issueNumber.isNotEmpty()
    val enabled = !isLoading && !isDanTuo && hasIssue

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        AnimatedContent(targetState = isLoading, label = "checkBtnContent") { loading ->
            if (loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "查询中...",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "在线查询中奖",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckTicketResults(
    lotteryType: LotteryType,
    results: List<TicketCheckResult>
) {
    val winCount = results.count { it.isWin }
    val totalAmount = results.filter { it.isWin }.sumOf { (it.amount ?: 0L) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 汇总标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    if (winCount > 0)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (winCount > 0) "恭喜！共 $winCount 注中奖" else "很遗憾，本期未中奖",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (winCount > 0)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (winCount > 0 && totalAmount > 0) {
                Text(
                    text = "￥${formatAmount(totalAmount)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // 每注详情
        results.forEachIndexed { idx, result ->
            val isLast = idx == results.lastIndex
            TicketResultRow(
                result = result,
                lotteryType = lotteryType,
                showIndex = results.size > 1,
                isLast = isLast
            )
        }
    }
}

@Composable
private fun TicketResultRow(
    result: TicketCheckResult,
    lotteryType: LotteryType,
    showIndex: Boolean,
    isLast: Boolean
) {
    val bgColor = if (result.isWin)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surface

    val shape = if (isLast)
        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
    else
        RoundedCornerShape(0.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showIndex) {
                    Text(
                        text = "第${result.index}注",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp)
                    )
                }
                if (result.isWin && result.level != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = result.level,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Text(
                        text = "未中奖",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (result.isWin && (result.amount ?: 0L) > 0) {
                Text(
                    text = "￥${formatAmount(result.amount!!)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        val hitLabel = when (lotteryType) {
            LotteryType.SSQ -> "命中红球 ${result.hitRed} 个，蓝球 ${result.hitBlue} 个"
            LotteryType.DLT -> "命中前区 ${result.hitRed} 个，后区 ${result.hitBlue} 个"
        }
        Text(
            text = hitLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!result.remark.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = result.remark,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }

    if (!isLast) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )
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

private fun formatAmount(amount: Long): String {
    return when {
        amount >= 100_000_000 -> "${amount / 100_000_000}亿"
        amount >= 10_000 -> "${amount / 10_000}万"
        else -> amount.toString()
    }
}
