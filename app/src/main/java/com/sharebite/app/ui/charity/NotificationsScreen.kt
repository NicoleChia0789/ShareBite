package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
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
import com.sharebite.app.ui.theme.*

// 一则通知的资料结构（这里是根据 CharityRequest 的状态即时"推算"出来的，不是存在数据库里的真实通知记录）
private data class NotificationItem(
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBg: Color
)

@Composable
fun NotificationsScreen(paddingValues: PaddingValues) {
    val repository = remember { CharityRepository() }

    var allRequests by remember { mutableStateOf<List<CharityRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            allRequests = repository.getRequests()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    // 只有「状态有变化」的申请（不是 Pending）才算是一则通知
    // Pending 代表「还没人处理」，没有值得通知的新事件
    val notifications = allRequests
        .filter { it.status != "Pending" }
        .sortedByDescending { it.createdAt }
        .map { request -> toNotificationItem(request) }

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
            notifications.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = TextGrey,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No notifications yet.", color = TextGrey)
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationCard(notification)
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
            title = "Request Confirmed",
            message = "Your request for \"${request.foodName}\" has been confirmed by the donor.",
            time = timeShort,
            icon = Icons.Default.CheckCircle,
            iconColor = GreenConfirmed,
            iconBg = Color(0xFFE8F5E9)
        )
        "Completed" -> NotificationItem(
            title = "Pickup Completed",
            message = "\"${request.foodName}\" has been successfully collected. Thank you!",
            time = timeShort,
            icon = Icons.Default.CheckCircle,
            iconColor = GreyCompleted,
            iconBg = Color(0xFFF5F5F5)
        )
        "Cancelled" -> NotificationItem(
            title = "Request Cancelled",
            message = "Your request for \"${request.foodName}\" was cancelled.",
            time = timeShort,
            icon = Icons.Default.Cancel,
            iconColor = RedCancelled,
            iconBg = Color(0xFFFFEBEE)
        )
        else -> NotificationItem(
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
private fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Icon(
                    notification.icon,
                    contentDescription = null,
                    tint = notification.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(notification.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(2.dp))
                Text(notification.message, fontSize = 13.sp, color = TextGrey)
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.time, fontSize = 11.sp, color = TextGrey)
            }
        }
    }
}