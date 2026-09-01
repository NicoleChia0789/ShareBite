package com.sharebite.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

// ============================================================
// 1. Bottom Navigation Routes
// ============================================================

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Browse : Screen("browse", "Browse", Icons.Default.Search)
    object Requests : Screen("requests", "Requests", Icons.Default.Assignment)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Browse,
    Screen.Requests,
    Screen.Profile
)

// ============================================================
// 2. Auth Routes
// ============================================================

object SignUpRoute { const val route = "sign_up" }
object SignInRoute { const val route = "sign_in" }

// ============================================================
// 3. User Routes
// ============================================================

object UserFoodDetailRoute {
    const val route = "user_food_detail/{listingId}"
    fun createRoute(id: Long) = "user_food_detail/$id"
}

object UserMakeReservationRoute {
    const val route = "user_make_reservation/{listingId}?reservationId={reservationId}"
    fun createRoute(listingId: Long, reservationId: Long? = null): String {
        return if (reservationId != null && reservationId != 0L) {
            "user_make_reservation/$listingId?reservationId=$reservationId"
        } else {
            "user_make_reservation/$listingId"
        }
    }
}

object UserReservationDetailRoute {
    const val route = "user_reservation_detail/{reservationId}"
    fun createRoute(id: Long) = "user_reservation_detail/$id"
}

object UserEditProfileRoute { const val route = "user_edit_profile" }
object UserHelpRoute { const val route = "help" }
object UserSavedFoodRoute { const val route = "user_saved_food" }

// ============================================================
// 4. User Address Routes
// ============================================================

object UserAddressesRoute { const val route = "user_addresses" }

// ✅ 添加地址（无参数）
object UserAddEditAddressRoute {
    const val route = "user_add_edit_address"
}

// ✅ 编辑地址（带 addressId）
object UserEditAddressRoute {
    const val route = "user_edit_address/{addressId}"
    fun createRoute(addressId: Long) = "user_edit_address/$addressId"
}

// ============================================================
// 5. Security Routes
// ============================================================

object SecurityRoute { const val route = "security" }
object ChangePasswordRoute { const val route = "change_password" }
object ForgotPasswordRoute { const val route = "forgot_password" }
object ResetPasswordRoute { const val route = "reset_password" }

// ============================================================
// 6. Charity Routes
// ============================================================

object CharityEditProfileRoute { const val route = "charity_edit_profile" }
object CharityHelpRoute { const val route = "charity_help" }
object CharityNotificationsRoute { const val route = "charity_notifications" }

// ============================================================
// 7. Food Routes (Charity)
// ============================================================

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

object SavedItemsRoute { const val route = "saved_items" }

// ============================================================
// 8. Common Routes
// ============================================================

object NotificationsRoute { const val route = "notifications" }
object NotificationSettingsRoute { const val route = "notification_settings" }
object AboutRoute { const val route = "about" }
object HistoryRoute { const val route = "history" }

// ============================================================
// 9. Merchant Routes
// ============================================================

object MerchantDashboardRoute { const val route = "merchant_dashboard" }

// ✅ 添加捐赠（无参数）
object MerchantAddDonationRoute {
    const val route = "merchant_add_donation"
}

// ✅ 编辑捐赠（带 listingId）
object MerchantEditDonationRoute {
    const val route = "merchant_edit_donation/{listingId}"
    fun createRoute(listingId: Long) = "merchant_edit_donation/$listingId"
}

object MerchantReservationsRoute { const val route = "merchant_reservations" }
object MerchantListingsRoute { const val route = "merchant_listings" }
object MerchantCharityRequestsRoute { const val route = "merchant_charity_requests" }
object MerchantAllRequestsRoute { const val route = "merchant_all_requests" }
object MerchantUserReservationsRoute { const val route = "merchant_user_reservations" }
object MerchantImpactRoute { const val route = "merchant_impact" }

object MerchantListingDetailRoute {
    const val route = "merchant_listing_detail/{listingId}"
    fun createRoute(id: Long) = "merchant_listing_detail/$id"
}

object MerchantPickupDetailsRoute {
    const val route = "merchant_pickup_details/{pickupItemId}"
    fun createRoute(id: String) = "merchant_pickup_details/$id"
}

object MerchantProfileScreenRoute { const val route = "merchant_profile" }
object MerchantEditProfileRoute { const val route = "merchant_edit_profile" }
object MerchantDonationHistoryRoute { const val route = "merchant_history" }
object MerchantNotificationsRoute { const val route = "merchant_notifications_screen" }
object MerchantHelpRoute { const val route = "merchant_help_screen" }