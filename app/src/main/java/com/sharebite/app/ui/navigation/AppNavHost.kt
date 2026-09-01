package com.sharebite.app.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sharebite.app.data.model.UserAddress
import com.sharebite.app.data.repository.AddressRepository
import com.sharebite.app.ui.charity.*
import com.sharebite.app.ui.components.AppScaffold
import com.sharebite.app.ui.merchant.*
import com.sharebite.app.ui.profile.*
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.user.*

@Composable
fun AppNavHost(
    role: String,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Home.route

    // ✅ 正确创建 MerchantViewModel
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val merchantViewModel: MerchantViewModel = viewModel(
        factory = MerchantViewModelFactory(application)
    )

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
        currentRoute == UserAddressesRoute.route -> "Addresses"
        currentRoute == UserAddEditAddressRoute.route -> "Add Address"
        currentRoute.startsWith("user_edit_address/") -> "Edit Address"
        currentRoute == UserEditProfileRoute.route -> "Edit Profile"
        currentRoute == SecurityRoute.route -> "Password & Security"
        currentRoute == ChangePasswordRoute.route -> "Change Password"
        currentRoute == NotificationsRoute.route -> "Notifications"
        currentRoute == NotificationSettingsRoute.route -> "Notification Settings"
        currentRoute == UserHelpRoute.route -> "Help & Support"
        currentRoute.startsWith("user_food_detail/") -> "Food Detail"
        currentRoute.startsWith("user_make_reservation/") -> "Make Reservation"
        currentRoute.startsWith("user_reservation_detail/") -> "Reservation Detail"
        currentRoute == CharityEditProfileRoute.route -> "Edit Profile"
        currentRoute == CharityHelpRoute.route -> "Help & Support"
        currentRoute.startsWith("food_detail/") -> "Food Detail"
        currentRoute.startsWith("apply_for_food/") -> "Apply for Food"
        currentRoute.startsWith("edit_request/") -> "Edit Request"
        currentRoute == PostRequestRoute.route -> "Post a Food Request"
        currentRoute.startsWith("request_detail/") -> "Request Detail"
        currentRoute == MerchantEditProfileRoute.route -> "Edit Profile"
        currentRoute == MerchantHelpRoute.route -> "Help & Support"
        currentRoute == MerchantDonationHistoryRoute.route -> "Donation History"
        currentRoute == MerchantDashboardRoute.route -> "Merchant Dashboard"
        currentRoute == MerchantAddDonationRoute.route -> "Add Donation"
        currentRoute.startsWith("merchant_edit_donation/") -> "Edit Donation"
        currentRoute == MerchantReservationsRoute.route -> "Reservations"
        currentRoute == MerchantListingsRoute.route -> "All Listings"
        currentRoute == MerchantCharityRequestsRoute.route -> "Charity Requests"
        currentRoute == MerchantAllRequestsRoute.route -> "All Charity Requests"
        currentRoute == MerchantUserReservationsRoute.route -> "User Reservations"
        currentRoute == MerchantListingDetailRoute.route -> "Donation Details"
        currentRoute == MerchantPickupDetailsRoute.route -> "Pickup Details"
        currentRoute == MerchantImpactRoute.route -> "Impact"
        currentRoute == AboutRoute.route -> "About ShareBite"
        else -> "ShareBite"
    }

    val isDetailScreen = currentRoute.startsWith("user_food_detail/") ||
            currentRoute == HistoryRoute.route ||
            currentRoute.startsWith("user_make_reservation/") ||
            currentRoute.startsWith("user_reservation_detail/") ||
            currentRoute == UserEditProfileRoute.route ||
            currentRoute == UserHelpRoute.route ||
            currentRoute == UserAddressesRoute.route ||
            currentRoute == UserAddEditAddressRoute.route ||
            currentRoute.startsWith("user_edit_address/") ||
            currentRoute == SecurityRoute.route ||
            currentRoute == ChangePasswordRoute.route ||
            currentRoute == NotificationsRoute.route ||
            currentRoute == NotificationSettingsRoute.route ||
            currentRoute == CharityEditProfileRoute.route ||
            currentRoute == CharityHelpRoute.route ||
            currentRoute == MerchantEditProfileRoute.route ||
            currentRoute == MerchantHelpRoute.route ||
            currentRoute == MerchantDonationHistoryRoute.route ||
            currentRoute == MerchantImpactRoute.route ||
            currentRoute.startsWith("food_detail/") ||
            currentRoute.startsWith("apply_for_food/") ||
            currentRoute.startsWith("edit_request/") ||
            currentRoute.startsWith("request_detail/") ||
            currentRoute == PostRequestRoute.route ||
            currentRoute == AboutRoute.route ||
            currentRoute == MerchantAddDonationRoute.route ||
            currentRoute.startsWith("merchant_edit_donation/") ||
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
                launchSingleTop = true
                restoreState = true
            }
        },
        topBarTitle = titleForRoute,
        showBackButton = isDetailScreen,
        onBackClick = { navController.popBackStack() },
        onNotificationClick = { navController.navigate(NotificationsRoute.route) },
        onLogoutClick = onLogout,
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            // ─── 底部导航 ──────────────────────────────────
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
                        onAddDonationClick = {
                            navController.navigate(MerchantAddDonationRoute.route)
                        },
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
                        onListingClick = { id -> navController.navigate(MerchantListingDetailRoute.createRoute(id)) },
                        onEditClick = { id ->
                            navController.navigate(MerchantEditDonationRoute.createRoute(id))
                        }
                    )
                    else -> Text("Unknown role: $role")
                }
            }

            composable(Screen.Requests.route) {
                when (role) {
                    "user" -> UserMyReservationsScreen(
                        paddingValues = paddingValues,
                        onViewClick = { id -> navController.navigate(UserReservationDetailRoute.createRoute(id)) },
                        onEditReservation = { listingId, reservationId ->
                            navController.navigate(UserMakeReservationRoute.createRoute(listingId, reservationId))
                        },
                        onBrowseClick = { navController.navigate(Screen.Browse.route) }
                    )
                    "charity" -> MyRequestsScreen(
                        paddingValues = paddingValues,
                        onViewClick = { requestId -> navController.navigate(RequestDetailRoute.createRoute(requestId)) },
                        onEditClick = { requestId -> navController.navigate(EditRequestRoute.createRoute(requestId)) },
                        onWithdrawClick = { /* 内部处理 */ },
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
                        onHelpClick = { navController.navigate(UserHelpRoute.route) },
                        onAboutClick = { navController.navigate(AboutRoute.route) },
                        onLogout = onLogout,
                        onReservationHistory = { navController.navigate(HistoryRoute.route) },
                        onEditProfile = { navController.navigate(UserEditProfileRoute.route) },
                        onAddressClick = { navController.navigate(UserAddressesRoute.route) },
                        onSecurityClick = { navController.navigate(SecurityRoute.route) },
                        onNotificationSettingsClick = { navController.navigate(NotificationSettingsRoute.route) }
                    )
                    "charity" -> CharityProfileScreen(
                        paddingValues = paddingValues,
                        onHistoryClick = { navController.navigate(HistoryRoute.route) },
                        onHelpClick = { navController.navigate(CharityHelpRoute.route) },
                        onAboutClick = { navController.navigate(AboutRoute.route) },
                        onLogout = onLogout,
                        onEditProfile = { navController.navigate(CharityEditProfileRoute.route) },
                        onSecurityClick = { navController.navigate(SecurityRoute.route) },
                        onNotificationSettingsClick = { navController.navigate(NotificationSettingsRoute.route) }
                    )
                    "merchant" -> MerchantProfileScreen(
                        paddingValues = paddingValues,
                        onLogout = onLogout,
                        onEditProfile = { navController.navigate(MerchantEditProfileRoute.route) },
                        onDonationHistory = { navController.navigate(MerchantDonationHistoryRoute.route) },
                        onSecurityClick = { navController.navigate(SecurityRoute.route) },
                        onHelpClick = { navController.navigate(MerchantHelpRoute.route) },
                        onAboutClick = { navController.navigate(AboutRoute.route) },
                        onNotificationSettingsClick = { navController.navigate(NotificationSettingsRoute.route) }
                    )
                    else -> Text("Unknown role: $role")
                }
            }

            // ─── User 详情路由 ────────────────────────────
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
                arguments = listOf(
                    navArgument("listingId") { type = NavType.LongType },
                    navArgument("reservationId") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
                val reservationIdStr = backStackEntry.arguments?.getString("reservationId")
                val reservationId = reservationIdStr?.takeIf { it.isNotBlank() }?.toLongOrNull()
                UserMakeReservationScreen(
                    listingId = listingId,
                    reservationId = reservationId,
                    paddingValues = paddingValues,
                    onSuccess = { id ->
                        navController.navigate(UserReservationDetailRoute.createRoute(id)) {
                            popUpTo(UserMakeReservationRoute.createRoute(listingId, reservationId)) { inclusive = true }
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
                    onEditClick = { listingId, resId ->
                        navController.navigate(UserMakeReservationRoute.createRoute(listingId, resId)) {
                            popUpTo(UserReservationDetailRoute.createRoute(reservationId)) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
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

            // ─── 地址路由 ────────────────────────────────
            composable(UserAddressesRoute.route) {
                UserAddressesScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onAddAddress = { navController.navigate(UserAddEditAddressRoute.route) },
                    onEditAddress = { addressId ->
                        navController.navigate(UserEditAddressRoute.createRoute(addressId.toLong()))
                    },
                    onDeleteAddress = { /* 内部处理 */ },
                    onSetDefault = { /* 内部处理 */ }
                )
            }

            // 添加地址（无参数）
            composable(UserAddEditAddressRoute.route) {
                UserEditAddressScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onSave = { _, _, _, _, _, _, _ ->
                        navController.popBackStack()
                    },
                    existingAddress = null
                )
            }

            // 编辑地址（带 addressId）
            composable(
                route = UserEditAddressRoute.route,
                arguments = listOf(navArgument("addressId") { type = NavType.LongType })
            ) { backStackEntry ->
                val addressId = backStackEntry.arguments?.getLong("addressId") ?: 0L
                var existingAddress by remember { mutableStateOf<UserAddress?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                val addressRepo = remember { AddressRepository() }

                LaunchedEffect(addressId) {
                    if (addressId > 0) {
                        try {
                            existingAddress = addressRepo.getAddressById(addressId)
                        } catch (_: Exception) { }
                    }
                    isLoading = false
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangeAccent)
                    }
                } else {
                    UserEditAddressScreen(
                        paddingValues = paddingValues,
                        onBack = { navController.popBackStack() },
                        onSave = { _, _, _, _, _, _, _ ->
                            navController.popBackStack()
                        },
                        existingAddress = existingAddress
                    )
                }
            }

            // ─── 安全路由 ────────────────────────────────
            composable(SecurityRoute.route) {
                SecurityScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onChangePassword = { navController.navigate(ChangePasswordRoute.route) }
                )
            }

            composable(ChangePasswordRoute.route) {
                ChangePasswordScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onSave = { _, _ -> navController.popBackStack() },
                    onForgotPassword = { navController.navigate(ForgotPasswordRoute.route) }
                )
            }

            // ─── Charity 路由 ─────────────────────────────
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
                    onWithdrawClick = { /* 内部处理 */ }
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

            // ─── Merchant 详情路由 ──────────────────────
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

            composable(MerchantDonationHistoryRoute.route) {
                MerchantHistoryScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onViewImpact = { /* TODO */ },
                    onViewReceipt = { /* TODO */ }
                )
            }

            // 添加捐赠（无参数）
            composable(MerchantAddDonationRoute.route) {
                MerchantAddDonationScreen(
                    paddingValues = paddingValues,
                    onSaveSuccess = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    viewModel = merchantViewModel,
                    listingId = null
                )
            }

            // 编辑捐赠（带 listingId）
            composable(
                route = MerchantEditDonationRoute.route,
                arguments = listOf(navArgument("listingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
                MerchantAddDonationScreen(
                    paddingValues = paddingValues,
                    onSaveSuccess = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    viewModel = merchantViewModel,
                    listingId = listingId
                )
            }

            composable(MerchantListingsRoute.route) {
                MerchantAllListingsScreen(
                    paddingValues = paddingValues,
                    viewModel = merchantViewModel,
                    onListingClick = { id -> navController.navigate(MerchantListingDetailRoute.createRoute(id)) },
                    onEditClick = { id ->
                        navController.navigate(MerchantEditDonationRoute.createRoute(id))
                    }
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

            // ─── 共用路由 ────────────────────────────────
            composable(NotificationSettingsRoute.route) {
                NotificationSettingsScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NotificationsRoute.route) {
                UnifiedNotificationsScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onNotificationClick = { id, type ->
                        when (type) {
                            "reservation" -> navController.navigate(UserReservationDetailRoute.createRoute(id))
                            "request" -> navController.navigate(RequestDetailRoute.createRoute(id))
                            else -> navController.navigate(Screen.Home.route)
                        }
                    }
                )
            }

            composable(AboutRoute.route) {
                AboutScreen(paddingValues = paddingValues)
            }
        }
    }
}