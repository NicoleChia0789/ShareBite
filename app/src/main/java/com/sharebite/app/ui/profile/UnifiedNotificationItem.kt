package com.sharebite.app.ui.profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.data.repository.DismissedNotificationRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
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
)

@Composable
fun UnifiedNotificationsScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onNotificationClick: (Long, String) -> Unit = { _, _ -> }  // (id, type)
) {
    val role = CurrentUser.getUserRole()
    val scope = rememberCoroutineScope()

    val reservationRepo = remember { ReservationRepository() }
    val charityRepo = remember { CharityRepository() }
    val dismissedRepo = remember { DismissedNotificationRepository() }

    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var dismissedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Active") }

    val tabs = listOf("Active", "All")

    // 加载通知
    fun loadNotifications() {
        scope.launch {
            isLoading = true
            try {
                val items = when (role) {
                    "user" -> loadUserNotifications(reservationRepo)
                    "charity" -> loadCharityNotifications(charityRepo)
                    "merchant" -> loadMerchantNotifications(reservationRepo, charityRepo)
                    else -> emptyList()
                }
                notifications = items
                dismissedIds = dismissedRepo.getMyDismissedIds()
            } catch (e: Exception) {
                errorMessage = e.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadNotifications()
    }

    // 过滤通知
    val visibleNotifications = if (selectedTab == "Active") {
        notifications.filter { it.id !in dismissedIds }
    } else {
        notifications
    }

    // ── UI ──
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
                    items(visibleNotifications, key = { it.id }) { notification ->
                        val isDismissed = notification.id in dismissedIds
                        NotificationCard(
                            notification = notification,
                            isDismissed = isDismissed,
                            onDismiss = {
                                scope.launch {
                                    try {
                                        dismissedRepo.dismiss(notification.id)
                                        dismissedIds = dismissedIds + notification.id
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
// ✅ 区分 General Request (merchantId == null) 和 Direct Request (merchantId != null)
// ✅ 显示 User Reservations (Pending)
private suspend fun loadMerchantNotifications(
    reservationRepo: ReservationRepository,
    charityRepo: CharityRepository
): List<NotificationItem> {
    val items = mutableListOf<NotificationItem>()

    // 1️⃣ 获取所有 Charity Requests (只显示 Pending)
    val allRequests = charityRepo.getMyAllRequests()

    // 1a) General Request（公开请求，merchant_id 为 NULL）
    val generalRequests = allRequests
        .filter { it.merchantId == null && it.status == "Pending" }
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
        .filter { it.merchantId != null && it.status == "Pending" }
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
    val reservations = reservationRepo.getMyReservations()
    items.addAll(
        reservations
            .filter { it.status == "Pending" }
            .sortedByDescending { it.createdAt }
            .map { reservation ->
                NotificationItem(
                    id = reservation.id,
                    title = "🧑‍🤝‍🧑 New Reservation",
                    message = "${reservation.userId} reserved ${reservation.quantityNum} ${reservation.unit} of \"${reservation.foodName}\"",                    time = formatTime(reservation.createdAt),
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