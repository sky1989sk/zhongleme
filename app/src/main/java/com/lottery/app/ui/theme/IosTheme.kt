package com.lottery.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val IosBlue = Color(0xFF007AFF)
private val IosBlueDark = Color(0xFF0A84FF)
private val IosGray = Color(0xFFF2F2F7)
private val IosCard = Color(0xFFFFFFFF)
private val IosOnBg = Color(0xFF000000)
private val IosSeparator = Color(0xFFC6C6C8)
private val IosRed = Color(0xFFFF3B30)
private val IosGreen = Color(0xFF34C759)

fun iosLightColors() = lightColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    secondary = IosGreen,
    onSecondary = Color.White,
    surface = IosCard,
    onSurface = IosOnBg,
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF8E8E93),
    background = IosGray,
    onBackground = IosOnBg,
    outline = IosSeparator,
    outlineVariant = Color(0xFFD1D1D6),
    error = IosRed,
    onError = Color.White,
    surfaceContainerHighest = Color(0xFFE5E5EA)
)

fun iosDarkColors() = darkColorScheme(
    primary = IosBlueDark,
    onPrimary = Color.White,
    secondary = IosGreen,
    onSecondary = Color.White,
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF8E8E93),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    outline = Color(0xFF38383A),
    outlineVariant = Color(0xFF48484A),
    error = Color(0xFFFF453A),
    onError = Color.White,
    surfaceContainerHighest = Color(0xFF3A3A3C)
)

fun iosTypography() = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.35.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.38.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.41).sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.41).sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.24).sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.08).sp
    )
)

fun iosShapes() = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)
