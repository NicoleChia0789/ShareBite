package com.sharebite.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sharebite.app.ui.navigation.Screen
import com.sharebite.app.ui.navigation.bottomNavItems
import com.sharebite.app.ui.theme.NavSelectedBg
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.theme.TextGrey

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    role: String = "user"
) {
    NavigationBar(containerColor = Color.White) {
        bottomNavItems.forEach { screen ->
            val label = when (screen) {
                Screen.Requests -> {
                    when (role) {
                        "user" -> "Reservations"
                        "charity" -> "Requests"
                        else -> "Requests"
                    }
                }
                else -> screen.label
            }
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                icon = { Icon(screen.icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OrangeAccent,
                    selectedTextColor = OrangeAccent,
                    unselectedIconColor = TextGrey,
                    unselectedTextColor = TextGrey,
                    indicatorColor = NavSelectedBg
                )
            )
        }
    }
}