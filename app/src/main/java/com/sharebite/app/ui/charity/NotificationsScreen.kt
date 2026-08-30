package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
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
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.data.repository.DismissedNotificationRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch

private data class NotificationItem(
    val requestId: Long,
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBg: Color
)

private val notifTabs = listOf("Active", "All")

@Composable
fun NotificationsScreen(paddingValues: PaddingValues) {
    val repository = remember { CharityRepository() }
    val dismissedRepository = remember { DismissedNotificationRepository() }
    val coroutineScope = rememberCoroutineScope()

    var allRequests by remember { mutableStateOf<List<CharityRequest>>(emptyList()) }
    var dismissedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Active") }

    LaunchedEffect(Unit) {
        try {
            allRequests = repository.getMyRequests()
            dismissedIds = dismissedRepository.getMyDismissedIds()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val allNotifications = allRequests
        .filter { it.status != "Pending" }
        .sortedByDescending { it.createdAt }
        .map { toNotificationItem(it) }

    val visibleNotifications = if (selectedTab == "Active") {
        allNotifications.filter { it.requestId !in dismissedIds }
    } else {
        allNotifications
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Notifications", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Updates on your food requests.", fontSize = 13.sp, color = TextGrey)
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifTabs) { tab ->
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
                    Text("Error: $errorMessage", color = RedCancelled)
                }
            }
            visibleNotifications.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = TextGrey, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (selectedTab == "Active") "No new notifications." else "No notifications yet.",
                            color = TextGrey
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visibleNotifications, key = { it.requestId }) { notification ->
                        val isDismissed = notification.requestId in dismissedIds
                        NotificationCard(
                            notification = notification,
                            isDismissed = isDismissed,
                            onDismiss = {
                                coroutineScope.launch {
                                    try {
                                        dismissedRepository.dismiss(notification.requestId)
                                        dismissedIds = dismissedIds + notification.requestId
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun toNotificationItem(request: CharityRequest): NotificationItem {
    val timeShort = request.createdAt.take(16).replace("T", " · ")
    return when (request.status) {
        "Confirmed" -> NotificationItem(
            requestId = request.id,
            title = "Request Confirmed",
            message = "Your request for \"${request.foodName}\" has been confirmed by the donor.",
            time = timeShort,
            icon = Icons.Default.CheckCircle,
            iconColor = GreenConfirmed,
            iconBg = Color(0xFFE8F5E9)
        )
        "Completed" -> NotificationItem(
            requestId = request.id,
            title = "Pickup Completed",
            message = "\"${request.foodName}\" has been successfully collected. Thank you!",
            time = timeShort,
            icon = Icons.Default.CheckCircle,
            iconColor = GreyCompleted,
            iconBg = Color(0xFFF5F5F5)
        )
        "Cancelled" -> NotificationItem(
            requestId = request.id,
            title = "Request Cancelled",
            message = "Your request for \"${request.foodName}\" was cancelled.",
            time = timeShort,
            icon = Icons.Default.Cancel,
            iconColor = RedCancelled,
            iconBg = Color(0xFFFFEBEE)
        )
        else -> NotificationItem(
            requestId = request.id,
            title = "Update",
            message = "\"${request.foodName}\" status changed to ${request.status}.",
            time = timeShort,
            icon = Icons.Default.Info,
            iconColor = OrangeAccent,
            iconBg = Color(0xFFFFF3E0)
        )
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    isDismissed: Boolean,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(notification.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(notification.icon, contentDescription = null, tint = notification.iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(notification.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(2.dp))
                Text(notification.message, fontSize = 13.sp, color = TextGrey)
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.time, fontSize = 11.sp, color = TextGrey)
                if (isDismissed) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Dismissed", fontSize = 10.sp, color = TextGrey, fontWeight = FontWeight.Bold)
                }
            }

            if (!isDismissed) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextGrey, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}