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
import androidx.compose.ui.unit.dp
import com.sharebite.app.ui.navigation.bottomNavItems
import com.sharebite.app.ui.theme.NavSelectedBg
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.theme.TextGrey

@Composable
fun TabletNavRail(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 12.dp),
        containerColor = Color.White
    ) {
        bottomNavItems.forEach { screen ->
            NavigationRailItem(
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
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