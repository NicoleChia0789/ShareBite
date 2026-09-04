package com.sharebite.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

private const val TABLET_BREAKPOINT_DP = 600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    topBarTitle: String = "ShareBite",
    onNotificationClick: () -> Unit = {},
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    darkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    role: String = "user", // ✅ 新增 role 参数
    content: @Composable (PaddingValues) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isTablet = screenWidthDp >= TABLET_BREAKPOINT_DP
    var isRailExpanded by rememberSaveable { mutableStateOf(true) }

    val topBar: @Composable () -> Unit = {
        TopAppBar(
            title = { Text(topBarTitle, color = OrangeAccent, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                if (!showBackButton && isTablet && isLandscape) {
                    IconButton(onClick = { isRailExpanded = !isRailExpanded }) {
                        Icon(Icons.Default.Menu, contentDescription = if (isRailExpanded) "Close navigation" else "Open navigation")
                    }
                } else if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                if (!showBackButton) {
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (darkMode) "Switch to light mode" else "Switch to dark mode"
                        )
                    }
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCream)
        )
    }

    if (isTablet && isLandscape && !showBackButton) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(containerColor = BgCream, topBar = topBar) { padding ->
                content(padding)
            }
            if (isRailExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f))
                        .clickable { isRailExpanded = false }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = { if (isRailExpanded) isRailExpanded = false }
                            ) { _, _ -> }
                        }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .background(Color.White)
                ) {
                    TabletNavRail(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate,
                        role = role,
                        expanded = true,
                        onToggleExpanded = { isRailExpanded = false }
                    )
                }
            }
        }
    } else if (isTablet && !showBackButton) {
        Row(modifier = Modifier.fillMaxSize()) {
            TabletNavRail(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                role = role,
                expanded = !isLandscape || isRailExpanded,
                onToggleExpanded = { isRailExpanded = !isRailExpanded }
            )
            Scaffold(containerColor = BgCream, topBar = topBar) { padding ->
                content(padding)
            }
        }
    } else if (!showBackButton) {
        Scaffold(
            containerColor = BgCream,
            topBar = topBar,
            bottomBar = {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    role = role // ✅ 传递 role
                )
            }
        ) { padding ->
            content(padding)
        }
    } else {
        Scaffold(containerColor = BgCream, topBar = topBar) { padding ->
            content(padding)
        }
    }
}
