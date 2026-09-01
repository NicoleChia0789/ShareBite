package com.sharebite.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.sharebite.app.ui.profile.*

@Composable
fun AppNavHost(
    role: String,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Home.route
    val merchantViewModel: MerchantViewModel = viewModel()

    val titleForRoute = when {
        currentRoute == Screen.Home.route -> "ShareBite"
        currentRoute == Screen.Browse.route -> when (role) {
            "user" -> "Browse Food"
            "charity" -> "Browse Donations"
            "merchant" -> "My Listings"
            else -> "Browse"
        }
        currentRoute == Screen.Requests.route -> when (role) {
            "user" -> "My Reservations"
            "charity" -> "My Requests"
            "merchant" -> "Requests & Reservations"
            else -> "Requests"
        }
        currentRoute == Screen.Profile.route -> "Profile"
        currentRoute == HistoryRoute.route -> when (role) {
            "user" -> "History"
            "charity" -> "Request History"
            else -> "History"
        }
        // User routes
        currentRoute == UserAddressesRoute.route -> "Addresses"
        currentRoute == UserAddEditAddressRoute.route -> "Edit Address"
        currentRoute == UserEditProfileRoute.route -> "Edit Profile"
        currentRoute == SecurityRoute.route -> "Password & Security"
        currentRoute == ChangePasswordRoute.route -> "Change Password"
        currentRoute == ForgotPasswordRoute.route -> "Forgot Password"
        currentRoute == ResetPasswordRoute.route -> "Reset Password"
        currentRoute == NotificationsRoute.route -> "Notifications"
        currentRoute == UserSavedFoodRoute.route -> "Saved Food"
        currentRoute == UserHelpRoute.route -> "Help & Support"
        currentRoute.startsWith("user_food_detail/") -> "Food Detail"
        currentRoute.startsWith("user_make_reservation/") -> "Make Reservation"
        currentRoute.startsWith("user_reservation_detail/") -> "Reservation Detail"

        // Charity routes
        currentRoute == CharityEditProfileRoute.route -> "Edit Profile"
        currentRoute == CharityHelpRoute.route -> "Help & Support"
        currentRoute == CharityNotificationsRoute.route -> "Notifications"
        currentRoute.startsWith("food_detail/") -> "Food Detail"
        currentRoute.startsWith("apply_for_food/") -> "Apply for Food"
        currentRoute.startsWith("edit_request/") -> "Edit Request"
        currentRoute == PostRequestRoute.route -> "Post a Food Request"
        currentRoute == SavedItemsRoute.route -> "Saved Items"
        currentRoute.startsWith("request_detail/") -> "Request Detail"

        // Merchant routes
        currentRoute == MerchantEditProfileRoute.route -> "Edit Profile"
        currentRoute == MerchantHelpRoute.route -> "Help & Support"
        currentRoute == MerchantNotificationsRoute.route -> "Notifications"
        currentRoute == MerchantDonationHistoryRoute.route -> "Donation History"
        currentRoute == MerchantDashboardRoute.route -> "Merchant Dashboard"
        currentRoute == MerchantAddDonationRoute.route -> "Add Donation"
        currentRoute == MerchantReservationsRoute.route -> "Reservations"
        currentRoute == MerchantListingsRoute.route -> "All Listings"
        currentRoute == MerchantCharityRequestsRoute.route -> "Charity Requests"
        currentRoute == MerchantAllRequestsRoute.route -> "All Charity Requests"
        currentRoute == MerchantUserReservationsRoute.route -> "User Reservations"
        currentRoute == MerchantListingDetailRoute.route -> "Donation Details"
        currentRoute == MerchantPickupDetailsRoute.route -> "Pickup Details"
        currentRoute == MerchantImpactRoute.route -> "Impact"

        // Shared routes
        currentRoute == AboutRoute.route -> "About ShareBite"
        else -> "ShareBite"
    }

    val isDetailScreen = currentRoute.startsWith("user_food_detail/") ||
            currentRoute == HistoryRoute.route ||
            currentRoute.startsWith("user_make_reservation/") ||
            currentRoute.startsWith("user_reservation_detail/") ||
            currentRoute == UserSavedFoodRoute.route ||
            currentRoute == UserEditProfileRoute.route ||
            currentRoute == UserHelpRoute.route ||
            currentRoute == UserAddressesRoute.route ||
            currentRoute == UserAddEditAddressRoute.route ||
            currentRoute == SecurityRoute.route ||
            currentRoute == ChangePasswordRoute.route ||
            currentRoute == ForgotPasswordRoute.route ||
            currentRoute == ResetPasswordRoute.route ||
            currentRoute == NotificationsRoute.route ||
            currentRoute == CharityEditProfileRoute.route ||
            currentRoute == CharityHelpRoute.route ||
            currentRoute == CharityNotificationsRoute.route ||
            currentRoute == MerchantEditProfileRoute.route ||
            currentRoute == MerchantHelpRoute.route ||
            currentRoute == MerchantNotificationsRoute.route ||
            currentRoute == MerchantDonationHistoryRoute.route ||
            currentRoute == MerchantImpactRoute.route ||
            currentRoute.startsWith("food_detail/") ||
            currentRoute.startsWith("apply_for_food/") ||
            currentRoute.startsWith("edit_request/") ||
            currentRoute.startsWith("request_detail/") ||
            currentRoute == PostRequestRoute.route ||
            currentRoute == SavedItemsRoute.route ||
            currentRoute == AboutRoute.route ||
            currentRoute == MerchantAddDonationRoute.route ||
            currentRoute == MerchantReservationsRoute.route ||
            currentRoute == MerchantListingsRoute.route ||
            currentRoute == MerchantCharityRequestsRoute.route ||
            currentRoute == MerchantAllRequestsRoute.route ||
            currentRoute == MerchantUserReservationsRoute.route ||
            currentRoute == MerchantListingDetailRoute.route ||
            currentRoute == MerchantPickupDetailsRoute.route


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
        }
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
                        onAddDonationClick = { navController.navigate(MerchantAddDonationRoute.route) },
                        onViewAllListings = { navController.navigate(MerchantListingsRoute.route) },
                        onViewAllRequests = { navController.navigate(MerchantAllRequestsRoute.route) },
                        onViewAllReservations = { navController.navigate(MerchantUserReservationsRoute.route) },
                        onViewImpact = { navController.navigate(MerchantImpactRoute.route) },
                        onRequestClick = { id -> navController.navigate("merchant_request_detail/$id") },
                        onReservationClick = { id -> navController.navigate("merchant_reservation_detail/$id") },
                        viewModel = merchantViewModel
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
                    "merchant" -> MerchantAllListingsScreen(
                        paddingValues = paddingValues,
                        viewModel = merchantViewModel,
                        onListingClick = { id -> navController.navigate(MerchantListingDetailRoute.createRoute(id)) }
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
                    "merchant" -> MerchantReservationsPage(
                        paddingValues = paddingValues,
                        viewModel = merchantViewModel,
                        onViewAllRequests = { navController.navigate(MerchantAllRequestsRoute.route) },
                        onViewAllReservations = { navController.navigate(MerchantUserReservationsRoute.route) },
                        onPickupDetailsClick = { id -> navController.navigate(MerchantPickupDetailsRoute.createRoute(id)) }
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
                        onLogout = onLogout,
                        onReservationHistory = { navController.navigate(HistoryRoute.route) } ,
                        onEditProfile = { navController.navigate(UserEditProfileRoute.route) },
                        onAddressClick = { navController.navigate(UserAddressesRoute.route) },
                        onSecurityClick = { navController.navigate(SecurityRoute.route) },
                        onNotificationClick = { navController.navigate(NotificationsRoute.route) }
                    )
                    "charity" -> CharityProfileScreen(
                        paddingValues = paddingValues,
                        onSavedItemsClick = { navController.navigate(SavedItemsRoute.route) },
                        onHistoryClick = { navController.navigate(HistoryRoute.route) } ,
                        onHelpClick = { navController.navigate(CharityHelpRoute.route) },
                        onAboutClick = { navController.navigate(AboutRoute.route) },
                        onLogout = onLogout,
                        onEditProfile = { navController.navigate(CharityEditProfileRoute.route) },
                        onSecurityClick = { navController.navigate(SecurityRoute.route) },
                        onNotificationClick = { navController.navigate(CharityNotificationsRoute.route) }
                    )
                    "merchant" -> MerchantProfileScreen(
                        paddingValues = paddingValues,
                        onLogout = onLogout,
                        onEditProfile = { navController.navigate(MerchantEditProfileRoute.route) },
                        onDonationHistory = { navController.navigate(MerchantDonationHistoryRoute.route) },
                        onSecurityClick = { navController.navigate(SecurityRoute.route) },
                        onNotificationClick = { navController.navigate(MerchantNotificationsRoute.route) },
                        onHelpClick = { navController.navigate(MerchantHelpRoute.route) },
                        onAboutClick = { navController.navigate(AboutRoute.route) }
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
                        println("🚀 Going to detail page, ID = $reservationId")
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
                    onEditClick = { listingId ->
                        // Navigate to make reservation with the listing ID
                        navController.navigate(UserMakeReservationRoute.createRoute(listingId)) {
                            // This ensures we don't keep the detail screen in back stack
                            popUpTo(UserReservationDetailRoute.createRoute(reservationId)) { inclusive = true }
                        }
                    },
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
                    onSaveSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(UserHelpRoute.route) {
                UserHelpScreen(paddingValues = paddingValues)
            }

            // ══════════════════════════════════════
            // User 地址路由
            // ══════════════════════════════════════

            composable(UserAddressesRoute.route) {
                UserAddressesScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onAddAddress = { navController.navigate(UserAddEditAddressRoute.route) },
                    onEditAddress = { addressId ->
                        navController.navigate("${UserAddEditAddressRoute.route}?addressId=$addressId")
                    },
                    onDeleteAddress = { /* TODO: 删除地址 */ },
                    onSetDefault = { /* TODO: 设为默认地址 */ }
                )
            }

            composable(UserAddEditAddressRoute.route) {
                val addressId = it.arguments?.getString("addressId")
                val existingAddress = null // TODO: 根据 addressId 获取地址

                UserEditAddressScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onSave = { label, street, city, state, zip, country, phone ->
                        // TODO: 保存地址
                        navController.popBackStack()
                    },
                    existingAddress = existingAddress
                )
            }

            // ══════════════════════════════════════
            // User 安全路由
            // ══════════════════════════════════════

            composable(SecurityRoute.route) {
                SecurityScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onChangePassword = { navController.navigate(ChangePasswordRoute.route) },

                )
            }

            composable(ChangePasswordRoute.route) {
                ChangePasswordScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onSave = { newPassword, currentPassword ->
                        // TODO: 保存新密码
                        navController.popBackStack()
                    },
                    onForgotPassword = { navController.navigate(ForgotPasswordRoute.route) }
                )
            }

            composable(ForgotPasswordRoute.route) {
                ForgotPasswordScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onSendResetLink = { email ->
                        // 🟢 This is all you need!
                        navController.navigate(ResetPasswordRoute.route) {
                            popUpTo(ForgotPasswordRoute.route) { inclusive = true }
                        }
                        // Optionally: Call API here to send reset email
                    },
                )
            }

            composable(ResetPasswordRoute.route) {
                ResetPasswordScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onReset = { newPassword ->
                        // TODO: Call API with new password
                        // On success, navigate to Profile
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                        }
                    },
                )
            }

            // ══════════════════════════════════════
            // User 通知路由
            // ══════════════════════════════════════

            composable(NotificationsRoute.route) {
                when (role) {
                    "user" -> UserNotificationsScreen(
                        paddingValues = paddingValues,
                        onBack = { navController.popBackStack() }
                    )
                    "charity" -> CharityNotificationsScreen(
                        paddingValues = paddingValues,
                        onBack = { navController.popBackStack() }
                    )
                    "merchant" -> MerchantNotificationsScreen(
                        paddingValues = paddingValues,
                        onBack = { navController.popBackStack() }
                    )
                    else -> UserNotificationsScreen(
                        paddingValues = paddingValues,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // ══════════════════════════════════════
            // Charity 特有详情路由
            // ══════════════════════════════════════

            composable(CharityEditProfileRoute.route) {
                CharityEditProfileScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable(CharityHelpRoute.route) {
                CharityHelpScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(CharityNotificationsRoute.route) {
                CharityNotificationsScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(HistoryRoute.route) {
                HistoryScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onViewDetails = { id ->
                        when (role) {
                            "user" -> navController.navigate(UserReservationDetailRoute.createRoute(id))
                            "charity" -> navController.navigate(RequestDetailRoute.createRoute(id))
                            else -> navController.popBackStack()
                        }
                    }
                )
            }

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
            // Merchant 特有详情路由
            // ══════════════════════════════════════

            composable(MerchantEditProfileRoute.route) {
                MerchantEditProfileScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable(MerchantHelpRoute.route) {
                MerchantHelpScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(MerchantNotificationsRoute.route) {
                MerchantNotificationsScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(MerchantDonationHistoryRoute.route) {
                // TODO: Add Donation History Screen
                Text("Donation History", modifier = Modifier.padding(paddingValues))
            }



            composable(MerchantAddDonationRoute.route) {
                MerchantAddDonationScreen(
                    paddingValues = paddingValues,
                    onSaveSuccess = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    viewModel = merchantViewModel
                )
            }

            composable(MerchantListingsRoute.route) {
                MerchantAllListingsScreen(
                    paddingValues = paddingValues,
                    viewModel = merchantViewModel,
                    onListingClick = { id -> navController.navigate(MerchantListingDetailRoute.createRoute(id)) }
                )
            }

            composable(MerchantAllRequestsRoute.route) {
                MerchantAllRequestsScreen(
                    paddingValues = paddingValues,
                    viewModel = merchantViewModel,
                    onRequestClick = { id -> navController.navigate("merchant_request_detail/$id") }
                )
            }

            composable(MerchantUserReservationsRoute.route) {
                MerchantAllReservationsScreen(
                    paddingValues = paddingValues,
                    viewModel = merchantViewModel,
                    onReservationClick = { id -> navController.navigate("merchant_reservation_detail/$id") }
                )
            }

            composable(
                route = "merchant_request_detail/{requestId}",
                arguments = listOf(navArgument("requestId") { type = NavType.LongType })
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getLong("requestId") ?: 0L
                MerchantRequestDetailScreen(
                    paddingValues = paddingValues,
                    requestId = requestId,
                    viewModel = merchantViewModel,
                    onBack = { navController.popBackStack() },
                    onApprove = {
                        merchantViewModel.updateRequestStatus(requestId, "Confirmed")
                        navController.popBackStack()
                    },
                    onDecline = {
                        merchantViewModel.updateRequestStatus(requestId, "Cancelled")
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "merchant_reservation_detail/{reservationId}",
                arguments = listOf(navArgument("reservationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getLong("reservationId") ?: 0L
                MerchantReservationDetailScreen(
                    paddingValues = paddingValues,
                    reservationId = reservationId,
                    viewModel = merchantViewModel,
                    onBack = { navController.popBackStack() },
                    onConfirm = {
                        merchantViewModel.updateReservationStatus(reservationId, "Confirmed")
                        navController.popBackStack()
                    },
                    onDecline = {
                        merchantViewModel.updateReservationStatus(reservationId, "Cancelled")
                        navController.popBackStack()
                    }
                )
            }

            composable(MerchantImpactRoute.route) {
                MerchantImpactReportScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = MerchantListingDetailRoute.route,
                arguments = listOf(navArgument("listingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
                MerchantListingDetailScreen(
                    paddingValues = paddingValues,
                    listingId = listingId,
                    viewModel = merchantViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = MerchantPickupDetailsRoute.route,
                arguments = listOf(navArgument("pickupItemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val pickupItemId = backStackEntry.arguments?.getString("pickupItemId") ?: ""
                MerchantPickupDetailsScreen(
                    paddingValues = paddingValues,
                    pickupItemId = pickupItemId,
                    viewModel = merchantViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // ══════════════════════════════════════
            // 共用路由
            // ══════════════════════════════════════

            composable(AboutRoute.route) {
                AboutScreen(paddingValues = paddingValues)
            }

            composable(SavedItemsRoute.route) {
                SavedItemsScreen(
                    paddingValues = paddingValues,
                    onItemClick = { id -> navController.navigate(FoodDetailRoute.createRoute(id)) }
                )
            }
        }
    }
}