package com.sharebite.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.navigation.Screen
import com.sharebite.app.ui.navigation.getBottomNavItems
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
        val items = getBottomNavItems(role)

        items.forEach { screen ->
            val label = when (screen) {
                Screen.Browse -> {
                    when (role) {
                        "merchant" -> "My Listings"
                        else -> screen.label
                    }
                }
                Screen.Requests -> {
                    when (role) {
                        "user" -> "Reservations"
                        "merchant" -> "Orders"
                        else -> screen.label
                    }
                }
                Screen.CharityPlaza -> "Charity Requests"  // ✅ 改名
                else -> screen.label
            }

            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                icon = { Icon(screen.icon, contentDescription = label) },
                label = {
                    Text(
                        label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp  // ✅ 统一字号，防止跑位
                    )
                },
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