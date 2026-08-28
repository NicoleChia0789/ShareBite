package com.sharebite.app.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sharebite.app.ui.components.AppScaffold
import com.sharebite.app.ui.charity.CharityDashboardScreen
import com.sharebite.app.ui.charity.BrowseDonationsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.sharebite.app.ui.charity.FoodDetailScreen
import com.sharebite.app.ui.navigation.FoodDetailRoute
import com.sharebite.app.ui.charity.MyRequestsScreen
import com.sharebite.app.ui.charity.RequestDetailScreen
import com.sharebite.app.ui.navigation.RequestDetailRoute
import com.sharebite.app.ui.charity.PostRequestScreen
import com.sharebite.app.ui.navigation.PostRequestRoute
import com.sharebite.app.ui.charity.ApplyForFoodScreen
import com.sharebite.app.ui.navigation.ApplyForFoodRoute
import com.sharebite.app.ui.charity.EditRequestScreen
import com.sharebite.app.ui.navigation.EditRequestRoute
import com.sharebite.app.ui.charity.ProfileScreen
import com.sharebite.app.ui.charity.NotificationsScreen
import com.sharebite.app.ui.navigation.NotificationsRoute
import com.sharebite.app.ui.charity.SavedItemsScreen
import com.sharebite.app.ui.navigation.SavedItemsRoute


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Home.route

    val titleForRoute = when {
        currentRoute == Screen.Home.route -> "ShareBite"
        currentRoute == Screen.Browse.route -> "Browse Donations"
        currentRoute == Screen.Requests.route -> "My Requests"
        currentRoute == Screen.Profile.route -> "Profile"
        currentRoute.startsWith("food_detail") -> "Food Detail"
        currentRoute.startsWith("apply_for_food") -> "Apply for Food"
        currentRoute.startsWith("edit_request") -> "Edit Request"
        currentRoute == PostRequestRoute.route -> "Post a Food Request"
        currentRoute == NotificationsRoute.route -> "Notifications"
        currentRoute == SavedItemsRoute.route -> "Saved Items"
        else -> "ShareBite"
    }

    val isDetailScreen = currentRoute.startsWith("food_detail") ||
            currentRoute.startsWith("apply_for_food") ||
            currentRoute.startsWith("edit_request") ||
            currentRoute == PostRequestRoute.route ||
            currentRoute == NotificationsRoute.route ||
            currentRoute == SavedItemsRoute.route

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
        onNotificationClick = { navController.navigate(NotificationsRoute.route) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                CharityDashboardScreen(
                    paddingValues = paddingValues,
                    onCreateRequest = {
                        navController.navigate(PostRequestRoute.route)
                    },
                    onViewAllClick = {
                        navController.navigate(Screen.Requests.route)
                    }
                )
            }
            composable(Screen.Browse.route) {
                BrowseDonationsScreen(
                    paddingValues = paddingValues,
                    onFoodClick = { id -> navController.navigate(FoodDetailRoute.createRoute(id)) }
                )
            }
            composable(Screen.Requests.route) {
                MyRequestsScreen(
                    paddingValues = paddingValues,
                    onViewClick = { requestId ->
                        navController.navigate(RequestDetailRoute.createRoute(requestId))
                    },
                    onEditClick = { requestId ->
                        navController.navigate(EditRequestRoute.createRoute(requestId))
                    },
                    onWithdrawClick = { requestId ->
                        // Withdraw 弹窗现在是在 MyRequestsScreen 内部处理的，这里不用做事
                    },
                    onRequestAgain = { request ->
                        val listingId = request.listingId?.toLongOrNull()
                        if (listingId != null) {
                            // 当初是针对具体食物申请的，直接带回 Apply For Food 页面重新申请
                            navController.navigate(ApplyForFoodRoute.createRoute(listingId))
                        } else {
                            // 当初是自己发起的需求（没有对应食物），带去 Post Request 页面重新发起
                            navController.navigate(PostRequestRoute.route)
                        }
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    paddingValues = paddingValues,
                    onSavedItemsClick = { navController.navigate(SavedItemsRoute.route) }
                )
            }
            composable(SavedItemsRoute.route) {
                SavedItemsScreen(
                    paddingValues = paddingValues,
                    onItemClick = { id -> navController.navigate(FoodDetailRoute.createRoute(id)) }
                )
            }
            composable(PostRequestRoute.route) {
                PostRequestScreen(
                    paddingValues = paddingValues,
                    onPostSuccess = {
                        // 提交成功后返回上一页（Dashboard）
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
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
                        navController.navigate(EditRequestRoute.createRoute(requestId))
                    },
                    onWithdrawClick = {
                        // TODO: 之后接 Withdraw 确认弹窗
                    }
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

            composable(NotificationsRoute.route) {
                NotificationsScreen(paddingValues = paddingValues)
            }

        }
    }
}