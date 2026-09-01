package com.sharebite.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.sharebite.app.ui.merchant.MerchantAddDonationScreen
import com.sharebite.app.ui.profile.*
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.user.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.sharebite.app.ui.profile.NotificationSettingsScreen

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
        currentRoute == NotificationSettingsRoute.route -> "Notification Settings"
        currentRoute == UserHelpRoute.route -> "Help & Support"
        currentRoute.startsWith("user_food_detail/") -> "Food Detail"
        currentRoute.startsWith("user_make_reservation/") -> "Make Reservation"
        currentRoute.startsWith("user_reservation_detail/") -> "Reservation Detail"

        // Auth routes
        currentRoute == SignInRoute.route -> "Sign In"
        currentRoute == SignUpRoute.route -> "Sign Up"

        // Charity routes
        currentRoute == CharityEditProfileRoute.route -> "Edit Profile"
        currentRoute == CharityHelpRoute.route -> "Help & Support"
        currentRoute.startsWith("food_detail/") -> "Food Detail"
        currentRoute.startsWith("apply_for_food/") -> "Apply for Food"
        currentRoute.startsWith("edit_request/") -> "Edit Request"
        currentRoute == PostRequestRoute.route -> "Post a Food Request"
        currentRoute.startsWith("request_detail/") -> "Request Detail"

        // Merchant routes
        currentRoute == MerchantEditProfileRoute.route -> "Edit Profile"
        currentRoute == MerchantHelpRoute.route -> "Help & Support"
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
            currentRoute == UserEditProfileRoute.route ||
            currentRoute == UserHelpRoute.route ||
            currentRoute == UserAddressesRoute.route ||
            currentRoute == UserAddEditAddressRoute.route ||
            currentRoute == SecurityRoute.route ||
            currentRoute == ChangePasswordRoute.route ||
            currentRoute == ForgotPasswordRoute.route ||
            currentRoute == ResetPasswordRoute.route ||
            currentRoute == NotificationsRoute.route ||
            currentRoute == NotificationSettingsRoute.route ||
            currentRoute == CharityEditProfileRoute.route ||
            currentRoute == CharityHelpRoute.route ||
            currentRoute == MerchantEditProfileRoute.route ||
            currentRoute == MerchantHelpRoute.route ||
            currentRoute == MerchantDonationHistoryRoute.route ||
            currentRoute == MerchantImpactRoute.route ||
            currentRoute == SignInRoute.route ||
            currentRoute == SignUpRoute.route ||
            currentRoute.startsWith("food_detail/") ||
            currentRoute.startsWith("apply_for_food/") ||
            currentRoute.startsWith("edit_request/") ||
            currentRoute.startsWith("request_detail/") ||
            currentRoute == PostRequestRoute.route ||
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
        onLogoutClick = onLogout,
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            // ══════════════════════════════════════
            // Auth 路由
            // ══════════════════════════════════════

            composable(SignInRoute.route) {
                SignIn(
                    onSignInSuccess = { userRole ->
                        navController.navigate(Screen.Home.route) {
                            popUpTo(SignInRoute.route) { inclusive = true }
                        }
                    },
                    onSignUpClick = {
                        navController.navigate(SignUpRoute.route)
                    },
                    onForgotPasswordClick = {
                        navController.navigate(ForgotPasswordRoute.route)
                    }
                )
            }

            composable(SignUpRoute.route) {
                SignUp(
                    onSignUpClick = { userRole ->
                        navController.navigate(Screen.Home.route) {
                            popUpTo(SignUpRoute.route) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.navigate(SignInRoute.route) {
                            popUpTo(SignUpRoute.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(ForgotPasswordRoute.route) {
                ForgotPasswordScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onSendResetLink = { email ->
                        navController.navigate(ResetPasswordRoute.createRoute(email))
                    }
                )
            }

            composable(ResetPasswordRoute.route) {
                val email = it.arguments?.getString("email") ?: ""
                ResetPasswordScreen(
                    paddingValues = paddingValues,
                    email = email,
                    onBack = { navController.popBackStack() },
                    onReset = { _, newPassword ->
                        navController.navigate(SignInRoute.route) {
                            popUpTo(SignInRoute.route) { inclusive = true }
                        }
                    }
                )
            }

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
                        onEditReservation = { listingId, _ ->
                            navController.navigate(UserMakeReservationRoute.createRoute(listingId))
                        },
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
                    onEditClick = { listingId, _ ->
                        navController.navigate(UserMakeReservationRoute.createRoute(listingId)) {
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
                    onDeleteAddress = { /* 内部已处理 */ },
                    onSetDefault = { /* 内部已处理 */ }
                )
            }

            composable(
                route = UserAddEditAddressRoute.route,
                arguments = listOf(navArgument("addressId") {
                    type = NavType.StringType
                    defaultValue = null
                    nullable = true
                })
            ) { backStackEntry ->
                val addressId = backStackEntry.arguments?.getString("addressId")?.toLongOrNull()
                var existingAddress by remember { mutableStateOf<UserAddress?>(null) }
                var isLoading by remember { mutableStateOf<Boolean>(true) }
                val addressRepo = remember { AddressRepository() }

                LaunchedEffect(addressId) {
                    if (addressId != null && addressId > 0) {
                        try {
                            existingAddress = addressRepo.getAddressById(addressId)
                        } catch (e: Exception) {
                            // 忽略
                        }
                    }
                    isLoading = false
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangeAccent)
                    }
                } else {
                    UserEditAddressScreen(
                        paddingValues = paddingValues,
                        onBack = { navController.popBackStack() },
                        onSave = { label, street, city, state, zip, country, phone ->
                            navController.popBackStack()
                        },
                        existingAddress = existingAddress
                    )
                }
            }

            // ══════════════════════════════════════
            // User 安全路由
            // ══════════════════════════════════════

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
                    onSave = { newPassword, currentPassword ->
                        navController.popBackStack()
                    },
                    onForgotPassword = { navController.navigate(ForgotPasswordRoute.route) }
                )
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

            composable(MerchantDonationHistoryRoute.route) {
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

            composable(NotificationsRoute.route) {
                UnifiedNotificationsScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onNotificationClick = { id, type ->
                        when (type) {
                            "reservation" -> {
                                navController.navigate(
                                    UserReservationDetailRoute.createRoute(id)
                                )
                            }
                            "request" -> {
                                navController.navigate(
                                    RequestDetailRoute.createRoute(id)
                                )
                            }
                            else -> {
                                navController.navigate(Screen.Home.route)
                            }
                        }
                    }
                )
            }

            composable(NotificationSettingsRoute.route) {
                NotificationSettingsScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AboutRoute.route) {
                AboutScreen(paddingValues = paddingValues)
            }
        }
    }
}