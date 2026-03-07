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

private val HarmonyBlue = Color(0xFF007DFF)
private val HarmonyBlueDark = Color(0xFF3A9BFF)
private val HarmonyGreen = Color(0xFF64BB5C)
private val HarmonyBg = Color(0xFFF1F3F5)
private val HarmonyCard = Color(0xFFFFFFFF)
private val HarmonyOnBg = Color(0xFF191C1E)
private val HarmonyOutline = Color(0xFFE5E6EB)

fun harmonyLightColors() = lightColorScheme(
    primary = HarmonyBlue,
    onPrimary = Color.White,
    secondary = HarmonyGreen,
    onSecondary = Color.White,
    surface = HarmonyCard,
    onSurface = HarmonyOnBg,
    surfaceVariant = Color(0xFFF7F8FA),
    onSurfaceVariant = Color(0xFF5A5D62),
    background = HarmonyBg,
    onBackground = HarmonyOnBg,
    outline = HarmonyOutline,
    outlineVariant = Color(0xFFE5E6EB),
    error = Color(0xFFE84026),
    onError = Color.White,
    surfaceContainerHighest = Color(0xFFE8E9EB)
)

fun harmonyDarkColors() = darkColorScheme(
    primary = HarmonyBlueDark,
    onPrimary = Color.White,
    secondary = HarmonyGreen,
    onSecondary = Color.White,
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E4E6),
    surfaceVariant = Color(0xFF252729),
    onSurfaceVariant = Color(0xFFA0A2A7),
    background = Color(0xFF121314),
    onBackground = Color(0xFFE3E4E6),
    outline = Color(0xFF3A3C40),
    outlineVariant = Color(0xFF3A3C40),
    error = Color(0xFFFF6B6B),
    onError = Color.White,
    surfaceContainerHighest = Color(0xFF303234)
)

fun harmonyTypography() = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
)

fun harmonyShapes() = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(40.dp)
)
