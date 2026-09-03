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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidthDp >= TABLET_BREAKPOINT_DP

    val topBar: @Composable () -> Unit = {
        TopAppBar(
            title = { Text(topBarTitle, color = OrangeAccent, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                if (showBackButton) {
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

    if (isTablet && !showBackButton) {
        Row(modifier = Modifier.fillMaxSize()) {
            TabletNavRail(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                role = role // ✅ 传递 role
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
