package com.sharebite.app.ui.profile

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
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val role = CurrentUser.getUserRole()

    // ── 从 SharedPreferences 加载保存的设置 ──
    val prefs = context.getSharedPreferences("notification_prefs", android.content.Context.MODE_PRIVATE)

    // ── 根据不同角色显示不同的开关 ──
    // User 开关
    var userNewSurplus by remember {
        mutableStateOf(prefs.getBoolean("user_new_surplus_alerts", true))
    }
    var userReservationUpdates by remember {
        mutableStateOf(prefs.getBoolean("user_reservation_updates", true))
    }
    var userMessages by remember {
        mutableStateOf(prefs.getBoolean("user_messages", false))
    }

    // Charity 开关
    var charityNewSurplus by remember {
        mutableStateOf(prefs.getBoolean("charity_new_surplus_alerts", true))
    }
    var charityRequestApprovals by remember {
        mutableStateOf(prefs.getBoolean("charity_request_approvals", true))
    }
    var charityPickupReminders by remember {
        mutableStateOf(prefs.getBoolean("charity_pickup_reminders", false))
    }

    // Merchant 开关
    var merchantNewRequests by remember {
        mutableStateOf(prefs.getBoolean("merchant_new_donation_requests", true))
    }
    var merchantPickupConfirmations by remember {
        mutableStateOf(prefs.getBoolean("merchant_pickup_confirmations", true))
    }
    var merchantDailyImpact by remember {
        mutableStateOf(prefs.getBoolean("merchant_daily_impact_summary", false))
    }

    // 通用方法开关
    var pushNotifications by remember {
        mutableStateOf(prefs.getBoolean("push_notifications", true))
    }
    var emailNotifications by remember {
        mutableStateOf(prefs.getBoolean("email_notifications", false))
    }
    var smsAlerts by remember {
        mutableStateOf(prefs.getBoolean("sms_alerts", false))
    }

    var isSaving by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    LaunchedEffect(showToast) {
        if (showToast) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            showToast = false
        }
    }

    fun saveSettings() {
        scope.launch {
            isSaving = true
            try {
                val editor = prefs.edit()

                // 保存 User 设置
                editor.putBoolean("user_new_surplus_alerts", userNewSurplus)
                editor.putBoolean("user_reservation_updates", userReservationUpdates)
                editor.putBoolean("user_messages", userMessages)

                // 保存 Charity 设置
                editor.putBoolean("charity_new_surplus_alerts", charityNewSurplus)
                editor.putBoolean("charity_request_approvals", charityRequestApprovals)
                editor.putBoolean("charity_pickup_reminders", charityPickupReminders)

                // 保存 Merchant 设置
                editor.putBoolean("merchant_new_donation_requests", merchantNewRequests)
                editor.putBoolean("merchant_pickup_confirmations", merchantPickupConfirmations)
                editor.putBoolean("merchant_daily_impact_summary", merchantDailyImpact)

                // 保存通用方法设置
                editor.putBoolean("push_notifications", pushNotifications)
                editor.putBoolean("email_notifications", emailNotifications)
                editor.putBoolean("sms_alerts", smsAlerts)

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
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {


        Text(
            text = "Manage how you receive alerts and updates.",
            fontSize = 14.sp,
            color = TextGrey
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── 根据角色显示不同的 Alert 设置 ──
        when (role) {
            "user" -> {
                Text(
                    text = "🔔 User Alerts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                NotificationToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Reservation Confirmed",
                    description = "Get notified when your reservation has been confirmed",
                    checked = userNewSurplus,
                    onCheckedChange = { userNewSurplus = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                NotificationToggleItem(
                    icon = Icons.Default.CalendarToday,
                    title = "Pickup Completed",
                    description = "Get notified when you have successfully picked up your food",
                    checked = userReservationUpdates,
                    onCheckedChange = { userReservationUpdates = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                NotificationToggleItem(
                    icon = Icons.Default.CalendarToday,
                    title = "Reservation Cancelled",
                    description = "Get notified when your reservation has been cancelled",
                    checked = userReservationUpdates,
                    onCheckedChange = { userReservationUpdates = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

            }

            "charity" -> {
                Text(
                    text = "🛡️ Charity Alerts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                NotificationToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Request Approved",
                    description = "Get notified when your request has been approved",
                    checked = charityNewSurplus,
                    onCheckedChange = { charityNewSurplus = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                NotificationToggleItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Pickup Completed",
                    description = "Get notified when you have successfully picked up your food",
                    checked = charityRequestApprovals,
                    onCheckedChange = { charityRequestApprovals = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                NotificationToggleItem(
                    icon = Icons.Default.Alarm,
                    title = "Request Rejected",
                    description = "Get notified when your request has been rejected",
                    checked = charityPickupReminders,
                    onCheckedChange = { charityPickupReminders = it }
                )
            }

            "merchant" -> {
                Text(
                    text = "🏪 Merchant Alerts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                NotificationToggleItem(
                    icon = Icons.Default.RequestPage,
                    title = "New Food Requests",
                    description = "Get notified when a charity requests your food",
                    checked = merchantNewRequests,
                    onCheckedChange = { merchantNewRequests = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                NotificationToggleItem(
                    icon = Icons.Default.DirectionsCar,
                    title = "New Reservation",
                    description = "Get notified when a user reserves your food items",
                    checked = merchantPickupConfirmations,
                    onCheckedChange = { merchantPickupConfirmations = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                NotificationToggleItem(
                    icon = Icons.Default.DirectionsCar,
                    title = "New Donation Request",
                    description = "Get notified when a charity requests for food",
                    checked = merchantPickupConfirmations,
                    onCheckedChange = { merchantPickupConfirmations = it }
                )
                Spacer(modifier = Modifier.height(8.dp))


            }

            else -> {
                Text("Unknown role", color = TextGrey)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 通用 Notification Method Section ──
        Text(
            text = "📧 Notification Method",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        NotificationToggleItem(
            icon = Icons.Default.PhoneAndroid,
            title = "Push Notifications",
            description = "Receive alerts directly on your device screen.",
            checked = pushNotifications,
            onCheckedChange = { pushNotifications = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        NotificationToggleItem(
            icon = Icons.Default.Email,
            title = "Email Notifications",
            description = "Daily summaries sent to your inbox.",
            checked = emailNotifications,
            onCheckedChange = { emailNotifications = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        NotificationToggleItem(
            icon = Icons.Default.Sms,
            title = "SMS Alerts",
            description = "Text messages for urgent updates.",
            checked = smsAlerts,
            onCheckedChange = { smsAlerts = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Save Button ──
        Button(
            onClick = { saveSettings() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isSaving) OrangeAccent else Color.Gray,
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