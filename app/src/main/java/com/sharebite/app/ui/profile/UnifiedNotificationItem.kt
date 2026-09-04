package com.sharebite.app.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.data.repository.DismissedNotificationRepository
import com.sharebite.app.data.repository.MerchantRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── 通知数据类型 ───
private data class NotificationItem(
    val id: Long,
    val title: String,
    val message: String,
    val time: String,
    val type: String,  // "reservation", "request"
    val status: String,
    val isGeneral: Boolean = false  // ✅ 标记是否 General Request
) {
    val uniqueKey: String
        get() = "$type:$id"

    // Request IDs remain positive for compatibility with existing dismissed rows.
    // Reservation IDs use the negative namespace so equal IDs cannot collide.
    val dismissedId: Long
        get() = if (type == "reservation") -id else id
}

@Composable
fun UnifiedNotificationsScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    showTopPopup: Boolean = true,
    onNotificationClick: (Long, String) -> Unit = { _, _ -> }  // (id, type)
) {
    val role = CurrentUser.getUserRole()
    val scope = rememberCoroutineScope()

    val reservationRepo = remember { ReservationRepository() }
    val charityRepo = remember { CharityRepository() }
    val merchantRepo = remember { MerchantRepository() }
    val dismissedRepo = remember { DismissedNotificationRepository() }

    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var dismissedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Active") }
    var popupNotification by remember { mutableStateOf<NotificationItem?>(null) }

    val tabs = listOf("Active", "All")

    // 加载通知
    fun loadNotifications() {
        scope.launch {
            isLoading = true
            try {
                val items = when (role) {
                    "user" -> loadUserNotifications(reservationRepo)
                    "charity" -> loadCharityNotifications(charityRepo)
                    "merchant" -> loadMerchantNotifications(merchantRepo)
                    else -> emptyList()
                }
                val dismissed = dismissedRepo.getMyDismissedIds()
                notifications = items
                dismissedIds = dismissed
                popupNotification = items.firstOrNull { it.dismissedId !in dismissed }
            } catch (e: Exception) {
                errorMessage = e.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadNotifications()
    }

    LaunchedEffect(popupNotification?.id, popupNotification?.type) {
        if (popupNotification != null) {
            delay(5_000)
            popupNotification = null
        }
    }

    // 过滤通知
    val visibleNotifications = if (selectedTab == "Active") {
        notifications.filter { it.dismissedId !in dismissedIds }
    } else {
        notifications
    }

    // ── UI ──
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCream)
                .padding(paddingValues)
        ) {


            // Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangeAccent)
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = RedCancelled
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Error: $errorMessage", color = RedCancelled)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { loadNotifications() },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                visibleNotifications.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextGrey
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedTab == "Active") "No new notifications" else "No notifications yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "We'll notify you when something happens",
                                fontSize = 14.sp,
                                color = TextGrey
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(visibleNotifications, key = { it.uniqueKey }) { notification ->
                            val isDismissed = notification.dismissedId in dismissedIds
                            NotificationCard(
                                notification = notification,
                                isDismissed = isDismissed,
                                onDismiss = {
                                    scope.launch {
                                        try {
                                            dismissedRepo.dismiss(notification.dismissedId)
                                            dismissedIds = dismissedIds + notification.dismissedId
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                        }
                                    }
                                },
                                onClick = {
                                    if (!isDismissed) {
                                        onNotificationClick(notification.id, notification.type)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showTopPopup && popupNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .zIndex(10f)
        ) {
            popupNotification?.let { notification ->
                TopNotificationBanner(
                    notification = notification,
                    onClick = {
                        popupNotification = null
                        onNotificationClick(notification.id, notification.type)
                    },
                    onClose = { popupNotification = null }
                )
            }
        }
    }
}

@Composable
private fun TopNotificationBanner(
    notification: NotificationItem,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = OrangeAccent
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = TextGrey,
                    maxLines = 2
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close notification",
                    tint = TextGrey
                )
            }
        }
    }
}

@Composable
fun GlobalNotificationBanner(
    role: String,
    onNotificationClick: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val reservationRepo = remember { ReservationRepository() }
    val charityRepo = remember { CharityRepository() }
    val merchantRepo = remember { MerchantRepository() }
    val dismissedRepo = remember { DismissedNotificationRepository() }

    var popupNotification by remember { mutableStateOf<NotificationItem?>(null) }
    var knownKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var hasCompletedInitialRefresh by remember { mutableStateOf(false) }

    LaunchedEffect(role) {
        while (true) {
            val prefs = context.getSharedPreferences(
                "notification_prefs",
                android.content.Context.MODE_PRIVATE
            )
            val pushEnabled = prefs.getBoolean("push_notifications", true)

            if (pushEnabled && popupNotification == null) {
                try {
                    val items = when (role) {
                        "user" -> loadUserNotifications(reservationRepo)
                        "charity" -> loadCharityNotifications(charityRepo)
                        "merchant" -> loadMerchantNotifications(merchantRepo)
                        else -> emptyList()
                    }
                    val dismissed = dismissedRepo.getMyDismissedIds()
                    val eligibleItems = items.filter {
                        it.dismissedId !in dismissed && isNotificationEnabled(it, role, prefs)
                    }
                    val currentKeys = eligibleItems
                        .map { "${it.type}:${it.id}:${it.status}" }
                        .toSet()
                    val nextNotification = if (hasCompletedInitialRefresh) {
                        eligibleItems.firstOrNull { item ->
                            "${item.type}:${item.id}:${item.status}" !in knownKeys
                        }
                    } else {
                        eligibleItems.firstOrNull()
                    }

                    if (nextNotification != null) {
                        popupNotification = nextNotification
                    }
                    knownKeys = knownKeys + currentKeys
                    hasCompletedInitialRefresh = true
                } catch (_: Exception) {
                    // A failed background refresh should not interrupt the current screen.
                }
            }

            delay(15_000)
        }
    }

    LaunchedEffect(popupNotification?.id, popupNotification?.type) {
        if (popupNotification != null) {
            delay(5_000)
            popupNotification = null
        }
    }

    AnimatedVisibility(
        visible = popupNotification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .zIndex(100f)
    ) {
        popupNotification?.let { notification ->
            TopNotificationBanner(
                notification = notification,
                onClick = {
                    popupNotification = null
                    onNotificationClick(notification.id, notification.type)
                },
                onClose = { popupNotification = null }
            )
        }
    }
}

private fun isNotificationEnabled(
    notification: NotificationItem,
    role: String,
    prefs: android.content.SharedPreferences
): Boolean {
    return when (role) {
        "user" -> when (notification.status) {
            "Confirmed" -> prefs.getBoolean("user_reservation_confirmed", prefs.getBoolean("user_new_surplus_alerts", true))
            "Completed" -> prefs.getBoolean("user_pickup_completed", prefs.getBoolean("user_reservation_updates", true))
            "Cancelled" -> prefs.getBoolean("user_reservation_cancelled", prefs.getBoolean("user_reservation_updates", true))
            else -> true
        }
        "charity" -> when (notification.status) {
            "Confirmed" -> prefs.getBoolean("charity_request_approved", prefs.getBoolean("charity_new_surplus_alerts", true))
            "Completed" -> prefs.getBoolean("charity_pickup_completed", prefs.getBoolean("charity_request_approvals", true))
            "Cancelled" -> prefs.getBoolean("charity_request_rejected", prefs.getBoolean("charity_pickup_reminders", true))
            else -> true
        }
        "merchant" -> when {
            notification.type == "reservation" ->
                prefs.getBoolean("merchant_new_reservations", prefs.getBoolean("merchant_pickup_confirmations", true))
            notification.type == "request" && notification.isGeneral ->
                prefs.getBoolean("merchant_new_food_requests", prefs.getBoolean("merchant_new_donation_requests", true))
            notification.type == "request" ->
                prefs.getBoolean("merchant_new_donation_requests", true)
            else -> true
        }
        else -> true
    }
}

// ─── 加载不同角色的通知 ───

// User: 来自 Reservations
private suspend fun loadUserNotifications(
    reservationRepo: ReservationRepository
): List<NotificationItem> {
    val reservations = reservationRepo.getMyReservations()
    return reservations
        .filter { it.status != "Pending" }
        .map { reservation ->
            val (title, message) = when (reservation.status) {
                "Confirmed" -> Pair(
                    "Reservation Confirmed 🎉",
                    "Your reservation for \"${reservation.foodName}\" has been confirmed!"
                )
                "Completed" -> Pair(
                    "Pickup Completed ✅",
                    "You've successfully picked up \"${reservation.foodName}\". Thank you!"
                )
                "Cancelled" -> Pair(
                    "Reservation Cancelled ❌",
                    "Your reservation for \"${reservation.foodName}\" was cancelled."
                )
                else -> Pair(
                    "Update",
                    "\"${reservation.foodName}\" status: ${reservation.status}"
                )
            }
            NotificationItem(
                id = reservation.id,
                title = title,
                message = message,
                time = formatTime(reservation.createdAt),
                type = "reservation",
                status = reservation.status
            )
        }
}

// Charity: 来自 Charity Requests
private suspend fun loadCharityNotifications(
    charityRepo: CharityRepository
): List<NotificationItem> {
    val requests = charityRepo.getMyRequests()
    return requests
        .filter { it.status != "Pending" }
        .map { request ->
            val (title, message) = when (request.status) {
                "Confirmed" -> Pair(
                    "Request Confirmed 🎉",
                    "Your request for \"${request.foodName}\" has been confirmed!"
                )
                "Completed" -> Pair(
                    "Pickup Completed ✅",
                    "\"${request.foodName}\" has been successfully collected."
                )
                "Cancelled" -> Pair(
                    "Request Cancelled ❌",
                    "Your request for \"${request.foodName}\" was cancelled."
                )
                else -> Pair(
                    "Update",
                    "\"${request.foodName}\" status: ${request.status}"
                )
            }
            NotificationItem(
                id = request.id,
                title = title,
                message = message,
                time = formatTime(request.createdAt),
                type = "request",
                status = request.status
            )
        }
}

// ─── Merchant 通知 ───
// ✅ 只显示 Pending 状态
// ✅ 区分 General Request (merchantId blank) 和 Direct Request (merchantId present)
// ✅ 显示 User Reservations (Pending)
private suspend fun loadMerchantNotifications(
    merchantRepo: MerchantRepository
): List<NotificationItem> {
    val items = mutableListOf<NotificationItem>()

    // 1️⃣ 获取所有 Charity Requests (只显示 Pending)
    val allRequests = (
            merchantRepo.getAllGeneralRequests() + merchantRepo.getMyAllRequests()
            ).distinctBy { it.id }

    // 1a) General Request（公开请求，merchant_id 为 NULL）
    val generalRequests = allRequests
        .filter { it.merchantId.isNullOrBlank() && it.status == "Pending" }
        .sortedByDescending { it.createdAt }
        .map { request ->
            NotificationItem(
                id = request.id,
                title = "📋 New General Request",
                message = "${request.organisationName} requested ${request.quantity} x \"${request.foodName}\"",
                time = formatTime(request.createdAt),
                type = "request",
                status = request.status,
                isGeneral = true
            )
        }
    items.addAll(generalRequests)

    // 1b) Direct Request（针对特定商家的请求，merchant_id 不为 NULL）
    val directRequests = allRequests
        .filter { !it.merchantId.isNullOrBlank() && it.status == "Pending" }
        .sortedByDescending { it.createdAt }
        .map { request ->
            NotificationItem(
                id = request.id,
                title = "📋 New Direct Request",
                message = "${request.organisationName} requested ${request.quantity} x \"${request.foodName}\"",
                time = formatTime(request.createdAt),
                type = "request",
                status = request.status,
                isGeneral = false
            )
        }
    items.addAll(directRequests)

    // 2️⃣ User Reservations（只显示 Pending）
    val reservations = merchantRepo.getMyReservations()
    val userNames = merchantRepo.getReservationUsers().associate { it.id to it.name }
    items.addAll(
        reservations
            .filter { it.status == "Pending" }
            .sortedByDescending { it.createdAt }
            .map { reservation ->
                NotificationItem(
                    id = reservation.id,
                    title = "🧑‍🤝‍🧑 New Reservation",
                    message = "${userNames[reservation.userId].orEmpty().ifBlank { "A user" }} reserved ${reservation.quantityNum} ${reservation.unit} of \"${reservation.foodName}\"",
                    time = formatTime(reservation.createdAt),

                    type = "reservation",
                    status = reservation.status,
                    isGeneral = false
                )
            }
    )

    return items.sortedByDescending { it.time }
}

// ─── Helper Functions ───

private fun formatTime(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return "Recently"
    return try {
        timestamp.take(16).replace("T", " · ")
    } catch (e: Exception) {
        "Recently"
    }
}

private fun getNotificationIcon(status: String, type: String): Pair<ImageVector, Color> {
    return when (status) {
        "Confirmed" -> Pair(Icons.Default.CheckCircle, GreenConfirmed)
        "Completed" -> Pair(Icons.Default.CheckCircle, GreyCompleted)
        "Cancelled" -> Pair(Icons.Default.Cancel, RedCancelled)
        "Pending" -> Pair(Icons.Default.Info, OrangeAccent)
        else -> Pair(Icons.Default.Info, OrangeAccent)
    }
}

// ─── Notification Card ───

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    isDismissed: Boolean,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val (icon, iconColor) = getNotificationIcon(notification.status, notification.type)
    val iconBg = iconColor.copy(alpha = 0.15f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDismissed) Color(0xFFF5F0E8) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDismissed) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDismissed) TextGrey else TextDark
                )

                // ✅ 显示 General/Direct 标签（仅对 Request 类型）
                if (notification.type == "request") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (notification.isGeneral)
                                Color(0xFFE3F5E6)
                            else
                                OrangeAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (notification.isGeneral) "🌍 Public" else "🎯 Direct",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                color = if (notification.isGeneral) GreenConfirmed else OrangeAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = if (isDismissed) TextGrey else TextGrey,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.time,
                    fontSize = 11.sp,
                    color = TextGrey
                )
                if (isDismissed) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dismissed",
                        fontSize = 10.sp,
                        color = TextGrey,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Dismiss button
            if (!isDismissed) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TextGrey,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
