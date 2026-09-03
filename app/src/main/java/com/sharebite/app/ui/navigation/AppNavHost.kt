package com.sharebite.app.ui.navigation

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.sharebite.app.data.repository.LoginRepository
import com.sharebite.app.ui.charity.*
import com.sharebite.app.ui.components.AppScaffold
import com.sharebite.app.ui.merchant.*
import com.sharebite.app.ui.profile.*
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.user.*
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    val loginRepository = remember { LoginRepository() }
    var resetEmail by rememberSaveable { mutableStateOf("") }
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
        currentRoute == MerchantPickupScheduleRoute.route -> "Pickup Schedule"
        currentRoute == AboutRoute.route -> "About ShareBite"
        else -> "ShareBite"
    }

    val isDetailScreen = currentRoute.startsWith("user_food_detail/") ||
            currentRoute == HistoryRoute.route ||
            currentRoute.startsWith("user_make_reservation/") ||
            currentRoute.startsWith("user_reservation_detail/") ||
            currentRoute == UserEditProfileRoute.route ||
            currentRoute == UserHelpRoute.route ||
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
            currentRoute == MerchantPickupScheduleRoute.route ||
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

    Box(modifier = Modifier.fillMaxSize()) {
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
            role = role, // 传递角色
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
                            onFoodClick = { id ->
                                navController.navigate(
                                    UserFoodDetailRoute.createRoute(
                                        id
                                    )
                                )
                            },
                            onViewAllReservations = { navController.navigate(Screen.Requests.route) },
                            onReservationClick = { id ->
                                navController.navigate(UserReservationDetailRoute.createRoute(id))
                            }
                        )

                        "charity" -> CharityDashboardScreen(
                            paddingValues = paddingValues,
                            onCreateRequest = { navController.navigate(PostRequestRoute.route) },
                            onViewAllClick = { navController.navigate(Screen.Requests.route) },
                            onActivityClick = { requestId ->
                                navController.navigate(
                                    RequestDetailRoute.createRoute(requestId)
                                )
                            }
                        )

                        "merchant" -> MerchantDashboardScreen(
                            paddingValues = paddingValues,
                            onAddDonationClick = {
                                navController.navigate(MerchantAddDonationRoute.route)
                            },
                            onViewAllListings = { navController.navigate(MerchantListingsRoute.route) },
                            onViewAllRequests = { navController.navigate(Screen.Requests.route) },
                            onViewAllReservations = {
                                navController.navigate(
                                    MerchantUserReservationsRoute.route
                                )
                            },
                            onViewImpact = { navController.navigate(MerchantImpactRoute.route) },
                            onViewPickupHistory = {
                                navController.navigate(
                                    MerchantPickupScheduleRoute.createRoute(
                                        showHistory = true
                                    )
                                )
                            },
                            onViewPickupSchedule = {
                                navController.navigate(MerchantPickupScheduleRoute.createRoute())
                            },
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
                            onFoodClick = { id ->
                                navController.navigate(
                                    UserFoodDetailRoute.createRoute(
                                        id
                                    )
                                )
                            }
                        )

                        "charity" -> BrowseDonationsScreen(
                            paddingValues = paddingValues,
                            onFoodClick = { id ->
                                navController.navigate(
                                    FoodDetailRoute.createRoute(
                                        id
                                    )
                                )
                            }
                        )

                        "merchant" -> MerchantAllListingsScreen(
                            paddingValues = paddingValues,
                            viewModel = merchantViewModel,
                            onListingClick = { id ->
                                navController.navigate(
                                    MerchantListingDetailRoute.createRoute(id)
                                )
                            },
                            onAddDonationClick = {
                                navController.navigate(MerchantAddDonationRoute.route)
                            }


                        )

                        else -> Text("Unknown role: $role")
                    }
                }

                composable(Screen.Requests.route) {
                    when (role) {
                        "user" -> UserMyReservationsScreen(
                            paddingValues = paddingValues,
                            onViewClick = { id ->
                                navController.navigate(
                                    UserReservationDetailRoute.createRoute(
                                        id
                                    )
                                )
                            },
                            onEditReservation = { listingId, reservationId ->
                                navController.navigate(
                                    UserMakeReservationRoute.createRoute(
                                        listingId,
                                        reservationId
                                    )
                                )
                            },
                            onBrowseClick = { navController.navigate(Screen.Browse.route) }
                        )

                        "charity" -> MyRequestsScreen(
                            paddingValues = paddingValues,
                            onViewClick = { requestId ->
                                navController.navigate(
                                    RequestDetailRoute.createRoute(
                                        requestId
                                    )
                                )
                            },
                            onEditClick = { requestId ->
                                navController.navigate(
                                    EditRequestRoute.createRoute(
                                        requestId
                                    )
                                )
                            },
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
                            onCharityClick = { id -> navController.navigate("merchant_request_detail/$id") },
                            onReservationClick = { id -> navController.navigate("merchant_reservation_detail/$id") },
                            onViewAllRequests = { /* 不再使用 */ },
                            onViewAllReservations = { /* 不再使用 */ },
                            onPickupScheduleClick = {
                                navController.navigate(MerchantPickupScheduleRoute.createRoute())
                            },
                            onPickupClick = { pickupItemId ->
                                navController.navigate(
                                    MerchantPickupDetailsRoute.createRoute(
                                        pickupItemId
                                    )
                                )
                            }
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
                            onSecurityClick = { navController.navigate(SecurityRoute.route) },
                            onNotificationSettingsClick = {
                                navController.navigate(
                                    NotificationSettingsRoute.route
                                )
                            }
                        )

                        "charity" -> CharityProfileScreen(
                            paddingValues = paddingValues,
                            onHistoryClick = { navController.navigate(HistoryRoute.route) },
                            onHelpClick = { navController.navigate(CharityHelpRoute.route) },
                            onAboutClick = { navController.navigate(AboutRoute.route) },
                            onLogout = onLogout,
                            onEditProfile = { navController.navigate(CharityEditProfileRoute.route) },
                            onSecurityClick = { navController.navigate(SecurityRoute.route) },
                            onNotificationSettingsClick = {
                                navController.navigate(
                                    NotificationSettingsRoute.route
                                )
                            }
                        )

                        "merchant" -> MerchantProfileScreen(
                            paddingValues = paddingValues,
                            onLogout = onLogout,
                            onEditProfile = { navController.navigate(MerchantEditProfileRoute.route) },
                            onDonationHistory = {
                                navController.navigate(
                                    MerchantDonationHistoryRoute.route
                                )
                            },
                            onSecurityClick = { navController.navigate(SecurityRoute.route) },
                            onHelpClick = { navController.navigate(MerchantHelpRoute.route) },
                            onAboutClick = { navController.navigate(AboutRoute.route) },
                            onNotificationSettingsClick = {
                                navController.navigate(
                                    NotificationSettingsRoute.route
                                )
                            }
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
                        onReserveClick = { id ->
                            navController.navigate(
                                UserMakeReservationRoute.createRoute(
                                    id
                                )
                            )
                        }
                    )
                }

                composable(
                    route = UserMakeReservationRoute.route,
                    arguments = listOf(
                        navArgument("listingId") { type = NavType.LongType },
                        navArgument("reservationId") {
                            type = NavType.StringType; defaultValue = ""
                        }
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
                                popUpTo(
                                    UserMakeReservationRoute.createRoute(
                                        listingId,
                                        reservationId
                                    )
                                ) { inclusive = true }
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
                            navController.navigate(
                                UserMakeReservationRoute.createRoute(
                                    listingId,
                                    resId
                                )
                            ) {
                                popUpTo(UserReservationDetailRoute.createRoute(reservationId)) {
                                    inclusive = true
                                }
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

                composable(ForgotPasswordRoute.route) {
                    ForgotPasswordScreen(
                        paddingValues = paddingValues,
                        onBack = { navController.popBackStack() },
                        onSendResetLink = { email ->
                            resetEmail = email.trim()
                            navController.navigate(ResetPasswordRoute.route)
                        }
                    )
                }

                composable(ResetPasswordRoute.route) {
                    ResetPasswordScreen(
                        paddingValues = paddingValues,
                        email = resetEmail,
                        onBack = { navController.popBackStack() },
                        onReset = { email, newPassword ->
                            coroutineScope.launch {
                                val success = loginRepository.updatePasswordByEmail(
                                    email = email.trim(),
                                    newPassword = newPassword
                                )
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Password reset successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack(
                                        SecurityRoute.route,
                                        inclusive = false
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No account was found, or the password could not be reset.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
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
                                "user" -> navController.navigate(
                                    UserReservationDetailRoute.createRoute(
                                        id
                                    )
                                )

                                "charity" -> navController.navigate(
                                    RequestDetailRoute.createRoute(
                                        id
                                    )
                                )

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
                        onEditClick = {
                            navController.navigate(
                                EditRequestRoute.createRoute(
                                    requestId
                                )
                            )
                        },
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
                        onApplyClick = { id ->
                            navController.navigate(
                                ApplyForFoodRoute.createRoute(
                                    id
                                )
                            )
                        }
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
                        onListingClick = { id ->
                            navController.navigate(
                                MerchantListingDetailRoute.createRoute(
                                    id
                                )
                            )
                        },
                        onAddDonationClick = {
                            navController.navigate(MerchantAddDonationRoute.route)

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
                            merchantViewModel.approveRequest(requestId)
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
                            merchantViewModel.approveReservation(reservationId)
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
                    route = MerchantPickupScheduleRoute.route,
                    arguments = listOf(navArgument("showHistory") {
                        type = NavType.BoolType
                        defaultValue = false
                    })
                ) { backStackEntry ->
                    MerchantPickupScheduleScreen(
                        paddingValues = paddingValues,
                        viewModel = merchantViewModel,
                        initialShowHistory = backStackEntry.arguments?.getBoolean("showHistory")
                            ?: false,
                        onPickupClick = { pickupItemId ->
                            navController.navigate(
                                MerchantPickupDetailsRoute.createRoute(
                                    pickupItemId
                                )
                            )
                        }
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
                        onBack = { navController.popBackStack() },
                        onEditClick = {
                            navController.navigate(MerchantEditDonationRoute.createRoute(listingId))
                        },
                        onDeleteClick = {
                            // Delete is handled inside the screen
                        }
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

                composable(Screen.CharityPlaza.route) {
                    when (role) {
                        "merchant" -> MerchantCharityPlazaScreen(
                            paddingValues = paddingValues,
                            viewModel = merchantViewModel,
                            onRequestClick = { id ->
                                navController.navigate("merchant_request_detail/$id")
                            }
                        )

                        else -> {
                            // 非 merchant 角色跳转到 Browse 或 Home
                            navController.navigate(Screen.Browse.route)
                        }
                    }
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
                        showTopPopup = false,
                        onNotificationClick = { id, type ->
                            val route = when {
                                role == "merchant" && type == "reservation" -> "merchant_reservation_detail/$id"
                                role == "merchant" && type == "request" -> "merchant_request_detail/$id"
                                role == "user" && type == "reservation" -> UserReservationDetailRoute.createRoute(
                                    id
                                )

                                role == "charity" && type == "request" -> RequestDetailRoute.createRoute(
                                    id
                                )

                                else -> Screen.Home.route
                            }
                            navController.navigate(route) { launchSingleTop = true }
                        }
                    )
                }

                composable(AboutRoute.route) {
                    AboutScreen(paddingValues = paddingValues)
                }
            }
        }



        GlobalNotificationBanner(
            role = role,
            onNotificationClick = { id, type ->
                val route = when {
                    role == "merchant" && type == "reservation" -> "merchant_reservation_detail/$id"
                    role == "merchant" && type == "request" -> "merchant_request_detail/$id"
                    role == "user" && type == "reservation" -> UserReservationDetailRoute.createRoute(
                        id
                    )

                    role == "charity" && type == "request" -> RequestDetailRoute.createRoute(id)
                    else -> NotificationsRoute.route
                }
                navController.navigate(route) { launchSingleTop = true }
            }
        )




    }
}
