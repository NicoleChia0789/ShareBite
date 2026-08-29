package com.sharebite.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.ui.graphics.vector.ImageVector

// ──────────────────────────────────────────────
// 1. 底部导航路由（4 个主页面）
// ──────────────────────────────────────────────
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Browse : Screen("browse", "Browse", Icons.Default.Search)
    object Requests : Screen("requests", "Requests", Icons.Default.Assignment)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(Screen.Home, Screen.Browse, Screen.Requests, Screen.Profile)

// ──────────────────────────────────────────────
// 2. User 特有路由
// ──────────────────────────────────────────────
object UserFoodDetailRoute {
    const val route = "user_food_detail/{listingId}"
    fun createRoute(id: Long) = "user_food_detail/$id"
}

object UserMakeReservationRoute {
    const val route = "user_make_reservation/{listingId}"
    fun createRoute(id: Long) = "user_make_reservation/$id"
}

object UserReservationDetailRoute {
    const val route = "user_reservation_detail/{reservationId}"
    fun createRoute(id: Long) = "user_reservation_detail/$id"
}

object UserSavedFoodRoute {
    const val route = "user_saved_food"
}

object UserEditProfileRoute {
    const val route = "user_edit_profile"
}

// ──────────────────────────────────────────────
// 3. Charity 特有路由
// ──────────────────────────────────────────────
object FoodDetailRoute {
    const val route = "food_detail/{listingId}"
    fun createRoute(id: Long) = "food_detail/$id"
}

object RequestDetailRoute {
    const val route = "request_detail/{requestId}"
    fun createRoute(id: Long) = "request_detail/$id"
}

object PostRequestRoute {
    const val route = "post_request"
}

object ApplyForFoodRoute {
    const val route = "apply_for_food/{listingId}"
    fun createRoute(id: Long) = "apply_for_food/$id"
}

object EditRequestRoute {
    const val route = "edit_request/{requestId}"
    fun createRoute(id: Long) = "edit_request/$id"
}

object AboutRoute {
    const val route = "about"
}


// ──────────────────────────────────────────────
// 4. 共用路由
// ──────────────────────────────────────────────
object NotificationsRoute {
    const val route = "notifications"
}

// ──────────────────────────────────────────────
// 5. Merchant 特有路由 (请添加到 Screen.kt 最底部)
// ──────────────────────────────────────────────
object MerchantDashboardRoute {
    const val route = "merchant_dashboard"
}

object MerchantAddDonationRoute {
    const val route = "merchant_add_donation"
}

object MerchantReservationsRoute {
    const val route = "merchant_reservations"
}

object SavedItemsRoute {
    const val route = "saved_items"
}