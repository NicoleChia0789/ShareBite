package com.sharebite.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.ui.graphics.vector.ImageVector

// 每个 Screen 都对应底部导航的一个选项
// 之后加新页面（比如 Food Detail）不用加在这里，
// 只有会出现在底部导航栏的 4 个主页面才需要放在这
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Browse : Screen("browse", "Browse", Icons.Default.Search)
    object Requests : Screen("requests", "Requests", Icons.Default.Assignment)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(Screen.Home, Screen.Browse, Screen.Requests, Screen.Profile)

// 不属于底部导航栏的页面，用一般的 object 定义路由字符串
object FoodDetailRoute {
    const val route = "food_detail/{listingId}"
    fun createRoute(listingId: Long) = "food_detail/$listingId"
}

object RequestDetailRoute {
    const val route = "request_detail/{requestId}"
    fun createRoute(requestId: Long) = "request_detail/$requestId"
}

object PostRequestRoute {
    const val route = "post_request"
}

object ApplyForFoodRoute {
    const val route = "apply_for_food/{listingId}"
    fun createRoute(listingId: Long) = "apply_for_food/$listingId"
}

object EditRequestRoute {
    const val route = "edit_request/{requestId}"
    fun createRoute(requestId: Long) = "edit_request/$requestId"
}

object NotificationsRoute {
    const val route = "notifications"
}