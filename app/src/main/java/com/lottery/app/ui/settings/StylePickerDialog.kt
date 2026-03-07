package com.lottery.app.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lottery.app.ui.theme.DesignStyle

@Composable
fun StylePickerDialog(
    currentStyle: DesignStyle,
    onStyleSelected: (DesignStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择界面风格",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DesignStyle.entries.forEach { style ->
                    StylePreviewCard(
                        style = style,
                        isSelected = style == currentStyle,
                        onClick = { onStyleSelected(style) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun StylePreviewCard(
    style: DesignStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(200), label = "borderColor"
    )

    val config = stylePreviewConfig(style)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(config.previewShape)
                        .background(config.accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = config.icon,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = style.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = config.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private data class StylePreviewConfig(
    val accentColor: Color,
    val previewShape: androidx.compose.ui.graphics.Shape,
    val icon: String,
    val description: String
)

private fun stylePreviewConfig(style: DesignStyle) = when (style) {
    DesignStyle.MATERIAL -> StylePreviewConfig(
        accentColor = Color(0xFF1E88E5),
        previewShape = RoundedCornerShape(12.dp),
        icon = "M",
        description = "Material Design 3 风格"
    )
    DesignStyle.HARMONY -> StylePreviewConfig(
        accentColor = Color(0xFF007DFF),
        previewShape = RoundedCornerShape(20.dp),
        icon = "H",
        description = "鸿蒙 HarmonyOS 风格"
    )
    DesignStyle.IOS26 -> StylePreviewConfig(
        accentColor = Color(0xFF007AFF),
        previewShape = RoundedCornerShape(10.dp),
        icon = "i",
        description = "iOS 26 Liquid Glass 风格"
    )
}
