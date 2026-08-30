package com.sharebite.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sharebite.app.ui.charity.*
import com.sharebite.app.ui.components.AppScaffold
import com.sharebite.app.ui.user.*
import com.sharebite.app.ui.merchant.*
import com.sharebite.app.ui.merchant.MerchantAddDonationScreen


import com.sharebite.app.ui.navigation.Screen
import com.sharebite.app.ui.navigation.UserFoodDetailRoute
import com.sharebite.app.ui.navigation.UserMakeReservationRoute
import com.sharebite.app.ui.navigation.UserReservationDetailRoute
import com.sharebite.app.ui.navigation.UserSavedFoodRoute
import com.sharebite.app.ui.navigation.UserEditProfileRoute
import com.sharebite.app.ui.navigation.UserHelpRoute
import com.sharebite.app.ui.navigation.FoodDetailRoute
import com.sharebite.app.ui.navigation.RequestDetailRoute
import com.sharebite.app.ui.navigation.PostRequestRoute
import com.sharebite.app.ui.navigation.ApplyForFoodRoute
import com.sharebite.app.ui.navigation.EditRequestRoute
import com.sharebite.app.ui.navigation.NotificationsRoute
import com.sharebite.app.ui.navigation.SavedItemsRoute
import com.sharebite.app.ui.navigation.AboutRoute
import com.sharebite.app.ui.navigation.MerchantDashboardRoute
import com.sharebite.app.ui.navigation.MerchantAddDonationRoute
import com.sharebite.app.ui.navigation.MerchantReservationsRoute

@Composable
fun AppNavHost(
    role: String
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Home.route

    val titleForRoute = when {
        currentRoute == Screen.Home.route -> "ShareBite"
        currentRoute == Screen.Browse.route -> when (role) {
            "user" -> "Browse Food"
            "charity" -> "Browse Donations"
            else -> "Browse"
        }
        currentRoute == Screen.Requests.route -> when (role) {
            "user" -> "My Reservations"
            "charity" -> "My Requests"
            else -> "Requests"
        }
        currentRoute == Screen.Profile.route -> "Profile"
        currentRoute.startsWith("user_food_detail/") -> "Food Detail"
        currentRoute.startsWith("user_make_reservation/") -> "Make Reservation"
        currentRoute.startsWith("user_reservation_detail/") -> "Reservation Detail"
        currentRoute == UserSavedFoodRoute.route -> "Saved Food"
        currentRoute == UserEditProfileRoute.route -> "Edit Profile"
        currentRoute == UserHelpRoute.route -> "Help & Support"
        currentRoute.startsWith("food_detail/") -> "Food Detail"
        currentRoute.startsWith("apply_for_food/") -> "Apply for Food"
        currentRoute.startsWith("edit_request/") -> "Edit Request"
        currentRoute == PostRequestRoute.route -> "Post a Food Request"
        currentRoute == NotificationsRoute.route -> "Notifications"
        currentRoute == SavedItemsRoute.route -> "Saved Items"
        currentRoute.startsWith("request_detail/") -> "Request Detail"
        currentRoute == AboutRoute.route -> "About ShareBite"
        currentRoute == MerchantDashboardRoute.route -> "Merchant Dashboard"
        currentRoute == MerchantAddDonationRoute.route -> "Add Donation"
        currentRoute == MerchantReservationsRoute.route -> "Reservations"
        else -> "ShareBite"
    }

    val isDetailScreen = currentRoute.startsWith("user_food_detail/") ||
            currentRoute.startsWith("user_make_reservation/") ||
            currentRoute.startsWith("user_reservation_detail/") ||
            currentRoute == UserSavedFoodRoute.route ||
            currentRoute == UserEditProfileRoute.route ||
            currentRoute == UserHelpRoute.route ||
            currentRoute.startsWith("food_detail/") ||
            currentRoute.startsWith("apply_for_food/") ||
            currentRoute.startsWith("edit_request/") ||
            currentRoute.startsWith("request_detail/") ||
            currentRoute == PostRequestRoute.route ||
            currentRoute == NotificationsRoute.route ||
            currentRoute == SavedItemsRoute.route ||
            currentRoute == AboutRoute.route ||
            currentRoute == MerchantAddDonationRoute.route ||
            currentRoute == MerchantReservationsRoute.route

    AppScaffold(
        currentRoute = currentRoute,
        onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        topBarTitle = titleForRoute,
        showBackButton = isDetailScreen,
        onBackClick = { navController.popBackStack() },
        onNotificationClick = { navController.navigate(NotificationsRoute.route) },
        showSavedButton = role == "user" || role == "charity",
        onSavedFoodClick = {
            when (role) {
                "user" -> navController.navigate(UserSavedFoodRoute.route)
                "charity" -> navController.navigate(SavedItemsRoute.route)
                else -> {}
            }
        },
        role = role  // ✅ 关键：传递角色给 AppScaffold
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            // ══════════════════════════════════════
            // 底部导航路由
            // ══════════════════════════════════════

            composable(Screen.Home.route) {
                when (role) {
                    "user" -> UserDashboardScreen(
                        paddingValues = paddingValues,
                        onBrowseClick = { navController.navigate(Screen.Browse.route) },
                        onFoodClick = { id -> navController.navigate(UserFoodDetailRoute.createRoute(id)) },
                        onViewAllReservations = { navController.navigate(Screen.Requests.route) }
                    )
                    "charity" -> CharityDashboardScreen(
                        paddingValues = paddingValues,
                        onCreateRequest = { navController.navigate(PostRequestRoute.route) },
                        onViewAllClick = { navController.navigate(Screen.Requests.route) },
                        onActivityClick = { requestId -> navController.navigate(RequestDetailRoute.createRoute(requestId)) }
                    )
                    "merchant" -> MerchantDashboardScreen(
                        paddingValues = paddingValues,
                        onAddDonationClick = { navController.navigate(MerchantAddDonationRoute.route) }
                    )
                    else -> Text("Unknown role: $role")
                }
            }

            composable(Screen.Browse.route) {
                when (role) {
                    "user" -> UserBrowseScreen(
                        paddingValues = paddingValues,
                        onFoodClick = { id -> navController.navigate(UserFoodDetailRoute.createRoute(id)) }
                    )
                    "charity" -> BrowseDonationsScreen(
                        paddingValues = paddingValues,
                        onFoodClick = { id -> navController.navigate(FoodDetailRoute.createRoute(id)) }
                    )
                    else -> Text("Unknown role: $role")
                }
            }

            composable(Screen.Requests.route) {
                when (role) {
                    "user" -> UserMyReservationsScreen(
                        paddingValues = paddingValues,
                        onViewClick = { id -> navController.navigate(UserReservationDetailRoute.createRoute(id)) },
                        onBrowseClick = { navController.navigate(Screen.Browse.route) }
                    )
                    "charity" -> MyRequestsScreen(
                        paddingValues = paddingValues,
                        onViewClick = { requestId -> navController.navigate(RequestDetailRoute.createRoute(requestId)) },
                        onEditClick = { requestId -> navController.navigate(EditRequestRoute.createRoute(requestId)) },
                        onWithdrawClick = { /* 内部弹窗处理 */ },
                        onRequestAgain = { request ->
                            val listingId = request.listingId?.toLongOrNull()
                            if (listingId != null) {
                                navController.navigate(ApplyForFoodRoute.createRoute(listingId))
                            } else {
                                navController.navigate(PostRequestRoute.route)
                            }
                        },
                        onCreateRequest = { navController.navigate(PostRequestRoute.route) }
                    )
                    else -> Text("Unknown role: $role")
                }
            }

            composable(Screen.Profile.route) {
                when (role) {
                    "user" -> UserProfileScreen(
                        paddingValues = paddingValues,
                        onSavedFood = { navController.navigate(UserSavedFoodRoute.route) },
                        onHelpClick = { navController.navigate(UserHelpRoute.route) },
                        onAboutClick = { navController.navigate(AboutRoute.route) },
                        onLogout = { /* 待 Auth 实现 */ }
                    )
                    "charity" -> ProfileScreen(
                        paddingValues = paddingValues,
                        onSavedItemsClick = {
                            navController.navigate(SavedItemsRoute.route)
                        },
                        onAboutClick = {
                            navController.navigate(AboutRoute.route)
                        }
                    )
                    else -> Text("Unknown role: $role")
                }
            }

            // ══════════════════════════════════════
            // User 特有详情路由
            // ══════════════════════════════════════

            composable(
                route = UserFoodDetailRoute.route,
                arguments = listOf(navArgument("listingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
                UserFoodDetailScreen(
                    listingId = listingId,
                    paddingValues = paddingValues,
                    onReserveClick = { id -> navController.navigate(UserMakeReservationRoute.createRoute(id)) }
                )
            }

            composable(
                route = UserMakeReservationRoute.route,
                arguments = listOf(navArgument("listingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
                UserMakeReservationScreen(
                    listingId = listingId,
                    paddingValues = paddingValues,
                    onSuccess = { reservationId ->
                        println("🚀 Going to detail page，ID = $reservationId")  // 调试
                        navController.navigate(UserReservationDetailRoute.createRoute(reservationId)) {
                            popUpTo(UserMakeReservationRoute.createRoute(listingId)) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = UserReservationDetailRoute.route,
                arguments = listOf(navArgument("reservationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getLong("reservationId") ?: 0L
                UserReservationDetailScreen(
                    reservationId = reservationId,
                    paddingValues = paddingValues,
                    onEditClick = { listingId -> navController.navigate(UserMakeReservationRoute.createRoute(listingId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(UserSavedFoodRoute.route) {
                UserSavedFoodScreen(
                    paddingValues = paddingValues,
                    onFoodClick = { id -> navController.navigate(UserFoodDetailRoute.createRoute(id)) }
                )
            }

            composable(UserEditProfileRoute.route) {
                UserEditProfileScreen(
                    paddingValues = paddingValues,
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable(UserHelpRoute.route) {
                UserHelpScreen(paddingValues = paddingValues)
            }

            // ══════════════════════════════════════
            // Charity 特有详情路由
            // ══════════════════════════════════════

            composable(PostRequestRoute.route) {
                PostRequestScreen(
                    paddingValues = paddingValues,
                    onPostSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = RequestDetailRoute.route,
                arguments = listOf(navArgument("requestId") { type = NavType.LongType })
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getLong("requestId") ?: 0L
                RequestDetailScreen(
                    requestId = requestId,
                    paddingValues = paddingValues,
                    onEditClick = { navController.navigate(EditRequestRoute.createRoute(requestId)) },
                    onWithdrawClick = { /* 后续接弹窗 */ }
                )
            }

            composable(
                route = FoodDetailRoute.route,
                arguments = listOf(navArgument("listingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
                FoodDetailScreen(
                    listingId = listingId,
                    paddingValues = paddingValues,
                    onApplyClick = { id -> navController.navigate(ApplyForFoodRoute.createRoute(id)) }
                )
            }

            composable(
                route = ApplyForFoodRoute.route,
                arguments = listOf(navArgument("listingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
                ApplyForFoodScreen(
                    listingId = listingId,
                    paddingValues = paddingValues,
                    onSubmitSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = EditRequestRoute.route,
                arguments = listOf(navArgument("requestId") { type = NavType.LongType })
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getLong("requestId") ?: 0L
                EditRequestScreen(
                    requestId = requestId,
                    paddingValues = paddingValues,
                    onSaveSuccess = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            // ══════════════════════════════════════
            // 共用路由
            // ══════════════════════════════════════

            composable(NotificationsRoute.route) {
                NotificationsScreen(paddingValues = paddingValues)
            }

            composable(AboutRoute.route) {
                AboutScreen(paddingValues = paddingValues)
            }

            composable(SavedItemsRoute.route) {
                SavedItemsScreen(
                    paddingValues = paddingValues,
                    onItemClick = { id -> navController.navigate(FoodDetailRoute.createRoute(id)) }
                )
            }

            // ══════════════════════════════════════
            // Merchant 特有路由
            // ══════════════════════════════════════

            composable(MerchantAddDonationRoute.route) {
                MerchantAddDonationScreen(
                    paddingValues = paddingValues,
                    onSaveSuccess = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(MerchantReservationsRoute.route) {
                Text("Reservations Screen Here", modifier = Modifier.padding(paddingValues))
            }
        }
    }
}