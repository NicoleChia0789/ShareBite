package com.sharebite.app.ui.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun UserNotificationsScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── 从 SharedPreferences 加载保存的设置 ──
    val prefs = context.getSharedPreferences("notification_prefs", android.content.Context.MODE_PRIVATE)

    // ── 开关状态 ──
    var newSurplusAlerts by remember {
        mutableStateOf(prefs.getBoolean("user_new_surplus_alerts", true))
    }
    var reservationUpdates by remember {
        mutableStateOf(prefs.getBoolean("user_reservation_updates", true))
    }
    var messages by remember {
        mutableStateOf(prefs.getBoolean("user_messages", false))
    }

    var pushNotifications by remember {
        mutableStateOf(prefs.getBoolean("user_push_notifications", true))
    }
    var emailNotifications by remember {
        mutableStateOf(prefs.getBoolean("user_email_notifications", false))
    }
    var smsAlerts by remember {
        mutableStateOf(prefs.getBoolean("user_sms_alerts", false))
    }

    var isSaving by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // 显示 Toast
    LaunchedEffect(showToast) {
        if (showToast) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            showToast = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── 副标题 ──
        Text(
            text = "Manage how Sharebite communicates with you.",
            fontSize = 14.sp,
            color = TextGrey
        )
        Text(
            text = "Stay informed about local surplus food and community updates.",
            fontSize = 14.sp,
            color = TextGrey
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── User Alerts Section ──
        Text(
            text = "User Alerts",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── New Surplus Alerts ──
        NotificationToggleItem(
            icon = Icons.Default.Notifications,
            title = "New Surplus Alerts",
            description = "Get notified instantly when local businesses post new items nearby",
            checked = newSurplusAlerts,
            onCheckedChange = { newSurplusAlerts = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Reservation Updates ──
        NotificationToggleItem(
            icon = Icons.Default.CalendarToday,
            title = "Reservation Updates",
            description = "Updates on your claimed food items and pickup times",
            checked = reservationUpdates,
            onCheckedChange = { reservationUpdates = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Messages ──
        NotificationToggleItem(
            icon = Icons.Default.Message,
            title = "Messages",
            description = "Direct messages from neighbors coordinating pickups",
            checked = messages,
            onCheckedChange = { messages = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Notification Method Section ──
        Text(
            text = "Notification Method",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Push Notifications ──
        NotificationToggleItem(
            icon = Icons.Default.PhoneAndroid,
            title = "Push Notifications",
            description = "Receive alerts directly on your device screen.",
            checked = pushNotifications,
            onCheckedChange = { pushNotifications = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Email Notifications ──
        NotificationToggleItem(
            icon = Icons.Default.Email,
            title = "Email Notifications",
            description = "Daily summaries and important alerts sent to your inbox.",
            checked = emailNotifications,
            onCheckedChange = { emailNotifications = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── SMS Alerts ──
        NotificationToggleItem(
            icon = Icons.Default.Sms,
            title = "SMS Alerts",
            description = "Text messages for urgent pickup updates.",
            checked = smsAlerts,
            onCheckedChange = { smsAlerts = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Save Changes Button ──
        Button(
            onClick = {
                scope.launch {
                    isSaving = true
                    try {
                        val editor = prefs.edit()
                        editor.putBoolean("user_new_surplus_alerts", newSurplusAlerts)
                        editor.putBoolean("user_reservation_updates", reservationUpdates)
                        editor.putBoolean("user_messages", messages)
                        editor.putBoolean("user_push_notifications", pushNotifications)
                        editor.putBoolean("user_email_notifications", emailNotifications)
                        editor.putBoolean("user_sms_alerts", smsAlerts)
                        editor.apply()

                        isSaving = false
                        toastMessage = "Notification settings saved!"
                        showToast = true
                    } catch (e: Exception) {
                        isSaving = false
                        toastMessage = "Failed to save: ${e.message}"
                        showToast = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeAccent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NotificationToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) Color(0xFFFFF8EE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── 图标 ──
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (checked) OrangeAccent.copy(alpha = 0.15f) else Color(0xFFF5F0E8),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (checked) OrangeAccent else TextGrey
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── 标题 + 描述 ──
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextGrey,
                    maxLines = 2
                )
            }

            // ── 开关 ──
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = OrangeAccent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFDDC0B8)
                )
            )
        }
    }
}