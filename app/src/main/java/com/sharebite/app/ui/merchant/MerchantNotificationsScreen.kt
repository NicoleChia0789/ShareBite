package com.sharebite.app.ui.merchant

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
fun MerchantNotificationsScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── 从 SharedPreferences 加载保存的设置 ──
    val prefs = context.getSharedPreferences("notification_prefs", android.content.Context.MODE_PRIVATE)

    // ── Merchant Alert Switches ──
    var newDonationRequests by remember {
        mutableStateOf(prefs.getBoolean("merchant_new_donation_requests", true))
    }
    var pickupConfirmations by remember {
        mutableStateOf(prefs.getBoolean("merchant_pickup_confirmations", true))
    }
    var dailyImpactSummary by remember {
        mutableStateOf(prefs.getBoolean("merchant_daily_impact_summary", false))
    }

    // ── Notification Method Switches ──
    var pushNotifications by remember {
        mutableStateOf(prefs.getBoolean("merchant_push_notifications", true))
    }
    var emailNotifications by remember {
        mutableStateOf(prefs.getBoolean("merchant_email_notifications", false))
    }
    var smsAlerts by remember {
        mutableStateOf(prefs.getBoolean("merchant_sms_alerts", false))
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

        // ── Merchant Alerts Section ──
        Text(
            text = "Merchant Alerts",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── New Donation Requests ──
        NotificationToggleItem(
            icon = Icons.Default.RequestPage,
            title = "New Donation Requests",
            description = "Get notified instantly when a neighbor requests a food item.",
            checked = newDonationRequests,
            onCheckedChange = { newDonationRequests = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Pickup Confirmations ──
        NotificationToggleItem(
            icon = Icons.Default.DirectionsCar,
            title = "Pickup Confirmations",
            description = "Alerts when someone is on their way to pick up a donation.",
            checked = pickupConfirmations,
            onCheckedChange = { pickupConfirmations = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Daily Impact Summary ──
        NotificationToggleItem(
            icon = Icons.Default.Insights,
            title = "Daily Impact Summary",
            description = "Receive a daily wrap-up of food saved and people helped.",
            checked = dailyImpactSummary,
            onCheckedChange = { dailyImpactSummary = it }
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
                        editor.putBoolean("merchant_new_donation_requests", newDonationRequests)
                        editor.putBoolean("merchant_pickup_confirmations", pickupConfirmations)
                        editor.putBoolean("merchant_daily_impact_summary", dailyImpactSummary)
                        editor.putBoolean("merchant_push_notifications", pushNotifications)
                        editor.putBoolean("merchant_email_notifications", emailNotifications)
                        editor.putBoolean("merchant_sms_alerts", smsAlerts)
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