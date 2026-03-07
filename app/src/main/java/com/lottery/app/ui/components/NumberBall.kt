package com.lottery.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.tooling.preview.Preview
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LocalDesignStyle
import com.lottery.app.ui.theme.LotteryAppTheme
import com.lottery.app.ui.theme.LotteryBlueBall
import com.lottery.app.ui.theme.LotteryDanBall
import com.lottery.app.ui.theme.LotteryRedBall

enum class BallType { RED, BLUE, DAN }

@Composable
fun NumberBall(
    number: Int,
    ballType: BallType,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val style = LocalDesignStyle.current
    val bgColor = when (ballType) {
        BallType.RED -> LotteryRedBall
        BallType.BLUE -> LotteryBlueBall
        BallType.DAN -> LotteryDanBall
    }

    when (style) {
        DesignStyle.MATERIAL -> MaterialBall(number, bgColor, size, modifier)
        DesignStyle.HARMONY -> HarmonyBall(number, bgColor, size, modifier)
        DesignStyle.IOS26 -> IosBall(number, bgColor, size, modifier)
    }
}

@Composable
private fun MaterialBall(number: Int, bgColor: Color, size: Dp, modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(bgColor)
    ) {
        Text(
            text = String.format("%02d", number),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp
        )
    }
}

@Composable
private fun HarmonyBall(number: Int, bgColor: Color, size: Dp, modifier: Modifier) {
    val gradient = Brush.verticalGradient(
        colors = listOf(bgColor.copy(alpha = 0.85f), bgColor)
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(0.5.dp, CircleShape)
            .clip(CircleShape)
            .background(gradient)
    ) {
        Text(
            text = String.format("%02d", number),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size.value * 0.36f).sp
        )
    }
}

@Composable
private fun IosBall(number: Int, bgColor: Color, size: Dp, modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor.copy(alpha = 0.15f))
            .border(1.dp, bgColor.copy(alpha = 0.5f), CircleShape)
    ) {
        Text(
            text = String.format("%02d", number),
            color = bgColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size.value * 0.38f).sp
        )
    }
}

// region Previews

@Composable
private fun BallPreviewRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        NumberBall(number = 7, ballType = BallType.RED)
        NumberBall(number = 16, ballType = BallType.BLUE)
        NumberBall(number = 3, ballType = BallType.DAN)
    }
}

@Preview(showBackground = true, name = "Ball - Material")
@Composable
private fun PreviewBallMaterial() {
    LotteryAppTheme(designStyle = DesignStyle.MATERIAL) { BallPreviewRow() }
}

@Preview(showBackground = true, name = "Ball - Harmony")
@Composable
private fun PreviewBallHarmony() {
    LotteryAppTheme(designStyle = DesignStyle.HARMONY) { BallPreviewRow() }
}

@Preview(showBackground = true, name = "Ball - iOS")
@Composable
private fun PreviewBallIos() {
    LotteryAppTheme(designStyle = DesignStyle.IOS26) { BallPreviewRow() }
}

// endregion
