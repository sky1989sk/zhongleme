package com.lottery.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LotteryAppTheme
import com.lottery.app.ui.theme.LocalDesignStyle

@Composable
fun <T> StyledSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    val style = LocalDesignStyle.current
    when (style) {
        DesignStyle.MATERIAL -> MaterialChipRow(items, selectedItem, onItemSelected, label, modifier)
        DesignStyle.HARMONY -> HarmonyCapsuleSegment(items, selectedItem, onItemSelected, label, modifier)
        DesignStyle.IOS26 -> IosSegmentedControl(items, selectedItem, onItemSelected, label, modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> MaterialChipRow(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FilterChip(
                selected = item == selectedItem,
                onClick = { onItemSelected(item) },
                label = { Text(label(item)) }
            )
        }
    }
}

@Composable
private fun <T> HarmonyCapsuleSegment(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)
    val density = LocalDensity.current
    var rowSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onGloballyPositioned { rowSize = it.size }
            .clip(RoundedCornerShape(40.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp)
    ) {
        val itemWidth = with(density) {
            if (items.isNotEmpty() && rowSize.width > 0) {
                ((rowSize.width.toDp() - 6.dp) / items.size)
            } else 0.dp
        }

        val offsetX by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = tween(250), label = "indicator"
        )

        if (itemWidth > 0.dp) {
            Box(
                Modifier
                    .offset(x = offsetX)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(40.dp))
                    .background(MaterialTheme.colorScheme.surface)
            )
        }

        Row(Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200), label = "textColor"
                )
                Box(
                    Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(item) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(item),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> IosSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)
    val density = LocalDensity.current
    var rowSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onGloballyPositioned { rowSize = it.size }
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
            .padding(2.dp)
    ) {
        val itemWidth = with(density) {
            if (items.isNotEmpty() && rowSize.width > 0) {
                ((rowSize.width.toDp() - 4.dp) / items.size)
            } else 0.dp
        }

        val offsetX by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = tween(200), label = "iosIndicator"
        )

        if (itemWidth > 0.dp) {
            Box(
                Modifier
                    .offset(x = offsetX)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(7.dp))
            )
        }

        Row(Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(150), label = "iosTextColor"
                )
                Box(
                    Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(item) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(item),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// region Previews

@Preview(showBackground = true, name = "Segment - Material")
@Composable
private fun PreviewSegmentMaterial() {
    LotteryAppTheme(designStyle = DesignStyle.MATERIAL) {
        StyledSegmentedControl(
            items = listOf("全部", "双色球", "大乐透"),
            selectedItem = "双色球",
            onItemSelected = {},
            label = { it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Segment - Harmony")
@Composable
private fun PreviewSegmentHarmony() {
    LotteryAppTheme(designStyle = DesignStyle.HARMONY) {
        StyledSegmentedControl(
            items = listOf("全部", "双色球", "大乐透"),
            selectedItem = "双色球",
            onItemSelected = {},
            label = { it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Segment - iOS")
@Composable
private fun PreviewSegmentIos() {
    LotteryAppTheme(designStyle = DesignStyle.IOS26) {
        StyledSegmentedControl(
            items = listOf("全部", "双色球", "大乐透"),
            selectedItem = "双色球",
            onItemSelected = {},
            label = { it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

// endregion
