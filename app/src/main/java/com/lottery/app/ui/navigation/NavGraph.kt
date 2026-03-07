package com.lottery.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lottery.app.di.AppContainer
import com.lottery.app.ui.history.HistoryScreen
import com.lottery.app.ui.history.HistoryViewModel
import com.lottery.app.ui.lottery.LotteryScreen
import com.lottery.app.ui.lottery.LotteryViewModel
import com.lottery.app.ui.settings.StylePickerDialog
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LocalDesignStyle

enum class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Lottery("lottery", "选号", Icons.Filled.Casino),
    History("history", "历史", Icons.Filled.History)
}

@Composable
fun LotteryNavHost(
    container: AppContainer,
    currentStyle: DesignStyle,
    onStyleChange: (DesignStyle) -> Unit
) {
    val navController = rememberNavController()
    val screens = Screen.entries
    val style = LocalDesignStyle.current
    var showStylePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            StyleSwitchTopBar(
                currentStyle = currentStyle,
                onStyleClick = { showStylePicker = true }
            )
        },
        bottomBar = {
            when (style) {
                DesignStyle.MATERIAL -> MaterialBottomBar(navController, screens)
                DesignStyle.HARMONY -> HarmonyBottomBar(navController, screens)
                DesignStyle.IOS26 -> IosBottomBar(navController, screens)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Lottery.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Lottery.route) {
                val vm: LotteryViewModel = viewModel(
                    factory = LotteryViewModel.Factory(container.generateNumbersUseCase)
                )
                LotteryScreen(viewModel = vm)
            }
            composable(Screen.History.route) {
                val vm: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(
                        container.queryHistoryUseCase,
                        container.deleteHistoryUseCase,
                        container.updateWonStatusUseCase
                    )
                )
                HistoryScreen(viewModel = vm)
            }
        }
    }

    if (showStylePicker) {
        StylePickerDialog(
            currentStyle = currentStyle,
            onStyleSelected = { onStyleChange(it) },
            onDismiss = { showStylePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyleSwitchTopBar(
    currentStyle: DesignStyle,
    onStyleClick: () -> Unit
) {
    val style = LocalDesignStyle.current
    TopAppBar(
        title = {
            Text(
                text = "蝌蚪云-中了么",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        actions = {
            TextButton(onClick = onStyleClick) {
                Icon(
                    Icons.Filled.Palette,
                    contentDescription = "切换风格",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = currentStyle.displayName,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        colors = when (style) {
            DesignStyle.HARMONY -> TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
            DesignStyle.IOS26 -> TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
            )
            else -> TopAppBarDefaults.topAppBarColors()
        }
    )
}

@Composable
private fun MaterialBottomBar(
    navController: androidx.navigation.NavHostController,
    screens: List<Screen>
) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        screens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun HarmonyBottomBar(
    navController: androidx.navigation.NavHostController,
    screens: List<Screen>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                    animationSpec = tween(200), label = "harmonyTabBg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200), label = "harmonyTabColor"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(40.dp))
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            screen.icon,
                            contentDescription = screen.label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        if (isSelected) {
                            Text(
                                text = screen.label,
                                color = contentColor,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IosBottomBar(
    navController: androidx.navigation.NavHostController,
    screens: List<Screen>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(150), label = "iosTabColor"
                )

                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(horizontal = 28.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        screen.icon,
                        contentDescription = screen.label,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = screen.label,
                        color = contentColor,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
