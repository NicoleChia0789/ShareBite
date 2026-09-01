package com.sharebite.app.ui.charity

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
fun CharityNotificationsScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── 从 SharedPreferences 加载保存的设置 ──
    val prefs = context.getSharedPreferences("notification_prefs", android.content.Context.MODE_PRIVATE)

    // ── Charity Alert Switches ──
    var newSurplusAlerts by remember {
        mutableStateOf(prefs.getBoolean("charity_new_surplus_alerts", true))
    }
    var requestApprovals by remember {
        mutableStateOf(prefs.getBoolean("charity_request_approvals", true))
    }
    var pickupReminders by remember {
        mutableStateOf(prefs.getBoolean("charity_pickup_reminders", false))
    }

    // ── Notification Method Switches ──
    var pushNotifications by remember {
        mutableStateOf(prefs.getBoolean("charity_push_notifications", true))
    }
    var emailNotifications by remember {
        mutableStateOf(prefs.getBoolean("charity_email_notifications", false))
    }
    var smsAlerts by remember {
        mutableStateOf(prefs.getBoolean("charity_sms_alerts", false))
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

        // ── Subtitle ──
        Text(
            text = "Manage how you receive alerts about available food and requests.",
            fontSize = 14.sp,
            color = TextGrey
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Charity Alerts Section ──
        Text(
            text = "🛡️ Charity Alerts",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── New Surplus Alerts ──
        NotificationToggleItem(
            icon = Icons.Default.Notifications,
            title = "New Surplus Alerts",
            description = "Get notified instantly when local businesses post large quantities of surplus food.",
            checked = newSurplusAlerts,
            onCheckedChange = { newSurplusAlerts = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Request Approvals ──
        NotificationToggleItem(
            icon = Icons.Default.CheckCircle,
            title = "Request Approvals",
            description = "Receive updates when your food requests are approved or modified by donors.",
            checked = requestApprovals,
            onCheckedChange = { requestApprovals = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Pickup Reminders ──
        NotificationToggleItem(
            icon = Icons.Default.Alarm,
            title = "Pickup Reminders",
            description = "Helpful reminders 1 hour before your scheduled pickup window opens.",
            checked = pickupReminders,
            onCheckedChange = { pickupReminders = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Notification Method Section ──
        Text(
            text = "📧 Notification Method",
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
                        editor.putBoolean("charity_new_surplus_alerts", newSurplusAlerts)
                        editor.putBoolean("charity_request_approvals", requestApprovals)
                        editor.putBoolean("charity_pickup_reminders", pickupReminders)
                        editor.putBoolean("charity_push_notifications", pushNotifications)
                        editor.putBoolean("charity_email_notifications", emailNotifications)
                        editor.putBoolean("charity_sms_alerts", smsAlerts)
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
            // ── Icon ──
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

            // ── Title + Description ──
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

            // ── Switch ──
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