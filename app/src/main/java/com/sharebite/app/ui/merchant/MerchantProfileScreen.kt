package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.repository.UserRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import com.sharebite.app.ui.profile.ProfileAvatar

@Composable
fun MerchantProfileScreen(
    paddingValues: PaddingValues,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    onDonationHistory: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onNotificationSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val userId = CurrentUser.getUserId()
    val userName = CurrentUser.getUserName()
    val userEmail = CurrentUser.getUserEmail()
    val userRole = CurrentUser.getUserRole()

    val userRepo = remember { UserRepository() }

    var fullName by remember { mutableStateOf(userName) }
    var email by remember { mutableStateOf(userEmail) }
    var phone by remember { mutableStateOf("Not set yet") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("Not set yet") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val user = userRepo.getUser(userId)
            if (user != null) {
                fullName = user.name.ifBlank { userName }
                email = user.email.ifBlank { userEmail }
                phone = user.phone?.ifBlank { "Not set yet" } ?: "Not set yet"
                avatarUrl = user.avatarUrl
                address = user.address?.ifBlank { "Not set yet" } ?: "Not set yet"
                println("✅ Merchant user loaded: $fullName, $email")
            } else if (userName.isNotEmpty()) {
                fullName = userName
                email = userEmail
            } else {
                errorMessage = "User not found"
                println("❌ User not found for ID: $userId")
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load profile: ${e.message}"
            println("❌ Error loading user: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Error Message ──
        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDED))
            ) {
                Text(
                    text = "⚠️ $errorMessage",
                    modifier = Modifier.padding(16.dp),
                    color = RedCancelled,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Avatar + Name + Email + Role ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(displayName = fullName, photoUrl = avatarUrl)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = fullName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = TextGrey
                )
                Text(
                    text = "Role: ${userRole.ifBlank { "Merchant" }}",
                    fontSize = 11.sp,
                    color = OrangeAccent,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Merchant Badge ──
        Surface(
            shape = RoundedCornerShape(50),
            color = OrangeAccent.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = OrangeAccent
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "VERIFIED MERCHANT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Edit Profile Button ──
        OutlinedButton(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(1.dp, OrangeAccent),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Main Menu Card ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // 1. Donation History
                ProfileMenuItem(
                    icon = Icons.Default.History,
                    title = "Donation History",
                    subtitle = "View past contributions",
                    onClick = onDonationHistory
                )
                Divider(color = Color(0xFFEFE6D9), modifier = Modifier.padding(horizontal = 16.dp))

                // 2. Account Security
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Account Security",
                    subtitle = "Manage your account credentials",
                    onClick = onSecurityClick
                )
                Divider(color = Color(0xFFEFE6D9), modifier = Modifier.padding(horizontal = 16.dp))

                // 3. Notification Settings
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Notification Settings",
                    subtitle = "Alerts for donations and messages",
                    onClick = onNotificationSettingsClick
                )
                Divider(color = Color(0xFFEFE6D9), modifier = Modifier.padding(horizontal = 16.dp))

                // 4. Help & Support
                ProfileMenuItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "FAQs and contact us",
                    onClick = onHelpClick
                )
                Divider(color = Color(0xFFEFE6D9), modifier = Modifier.padding(horizontal = 16.dp))

                // 5. About ShareBite
                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "About ShareBite",
                    subtitle = "Version and app information",
                    onClick = onAboutClick
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Logout Button ──
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancelled),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, RedCancelled)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Logout",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Footer ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ShareBite v2.4.1",
                fontSize = 12.sp,
                color = TextGrey
            )
            Text(
                text = "Made with heart in the neighborhood.",
                fontSize = 12.sp,
                color = TextGrey
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Profile Menu Item ──
@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(OrangeAccent.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = OrangeAccent
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextGrey
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = TextGrey
        )
    }
}
