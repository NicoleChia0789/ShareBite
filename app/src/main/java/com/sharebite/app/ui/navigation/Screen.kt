package com.sharebite.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.ui.graphics.vector.ImageVector

// ──────────────────────────────────────────────
// 1. 底部导航路由
// ──────────────────────────────────────────────
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Browse : Screen("browse", "Browse", Icons.Default.Search)
    object Requests : Screen("requests", "Requests", Icons.Default.Assignment)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(Screen.Home, Screen.Browse, Screen.Requests, Screen.Profile)

// ──────────────────────────────────────────────
// 2. Auth 路由
// ──────────────────────────────────────────────
object SignUpRoute { const val route = "sign_up" }
object SignInRoute { const val route = "sign_in" }

// ──────────────────────────────────────────────
// 3. User 特有路由
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

object UserEditProfileRoute { const val route = "user_edit_profile" }
object UserHelpRoute { const val route = "help" }

// ──────────────────────────────────────────────
// 4. User 地址路由
// ──────────────────────────────────────────────
object UserAddressesRoute { const val route = "user_addresses" }
object UserAddEditAddressRoute { const val route = "user_add_edit_address" }

// ──────────────────────────────────────────────
// 5. 安全路由
// ──────────────────────────────────────────────
object SecurityRoute { const val route = "security" }
object ChangePasswordRoute { const val route = "change_password" }
object ForgotPasswordRoute { const val route = "forgot_password" }

object ResetPasswordRoute {
    const val route = "reset_password"

}

// ──────────────────────────────────────────────
// 6. Charity 路由
// ──────────────────────────────────────────────
object CharityEditProfileRoute { const val route = "charity_edit_profile" }
object CharityHelpRoute { const val route = "charity_help" }

// ──────────────────────────────────────────────
// 7. Merchant 路由
// ──────────────────────────────────────────────
object MerchantEditProfileRoute { const val route = "merchant_edit_profile" }
object MerchantHelpRoute { const val route = "merchant_help" }
object MerchantDonationHistoryRoute { const val route = "merchant_donation_history" }
object MerchantImpactRoute { const val route = "merchant_impact" }

// ──────────────────────────────────────────────
// 8. 食物路由
// ──────────────────────────────────────────────
object FoodDetailRoute {
    const val route = "food_detail/{listingId}"
    fun createRoute(id: Long) = "food_detail/$id"
}

object RequestDetailRoute {
    const val route = "request_detail/{requestId}"
    fun createRoute(id: Long) = "request_detail/$id"
}

object PostRequestRoute { const val route = "post_request" }

object ApplyForFoodRoute {
    const val route = "apply_for_food/{listingId}"
    fun createRoute(id: Long) = "apply_for_food/$id"
}

object EditRequestRoute {
    const val route = "edit_request/{requestId}"
    fun createRoute(id: Long) = "edit_request/$id"
}

object AboutRoute { const val route = "about" }

// ──────────────────────────────────────────────
// 9. 通知路由
// ──────────────────────────────────────────────
object NotificationsRoute {
    const val route = "notifications"
}

object NotificationSettingsRoute {
    const val route = "notification_settings"
}

// ──────────────────────────────────────────────
// 10. Merchant 额外路由
// ──────────────────────────────────────────────
object MerchantDashboardRoute { const val route = "merchant_dashboard" }
object MerchantAddDonationRoute { const val route = "merchant_add_donation" }
object MerchantReservationsRoute { const val route = "merchant_reservations" }
object MerchantListingsRoute { const val route = "merchant_listings" }
object MerchantCharityRequestsRoute { const val route = "merchant_charity_requests" }
object MerchantAllRequestsRoute { const val route = "merchant_all_requests" }
object MerchantUserReservationsRoute { const val route = "merchant_user_reservations" }

object MerchantListingDetailRoute {
    const val route = "merchant_listing_detail/{listingId}"
    fun createRoute(id: Long) = "merchant_listing_detail/$id"
}

object MerchantPickupDetailsRoute {
    const val route = "merchant_pickup_details/{pickupItemId}"
    fun createRoute(id: String) = "merchant_pickup_details/$id"
}

object HistoryRoute {
    const val route = "history"
}