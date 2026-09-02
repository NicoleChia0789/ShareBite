package com.sharebite.app.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.navigation.Screen
import com.sharebite.app.ui.navigation.getBottomNavItems
import com.sharebite.app.ui.theme.NavSelectedBg
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.theme.TextGrey

@Composable
fun TabletNavRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    role: String = "user"
) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 12.dp),
        containerColor = Color.White
    ) {
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

            NavigationRailItem(
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                icon = { Icon(screen.icon, contentDescription = label) },
                label = {
                    Text(
                        label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationRailItemDefaults.colors(
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