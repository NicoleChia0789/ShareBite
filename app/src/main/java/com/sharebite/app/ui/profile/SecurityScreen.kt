package com.sharebite.app.ui.profile

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
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*

@Composable
fun SecurityScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit = {},
    onChangePassword: () -> Unit = {},


) {
    val userEmail = CurrentUser.getUserEmail()
    val userRole = CurrentUser.getUserRole()

    val maskedEmail = if (userEmail.isNotEmpty()) {
        val parts = userEmail.split('@')
        if (parts.size == 2) {
            val name = parts[0]
            val domain = parts[1]
            if (name.length > 2) {
                "${name.take(1)}${"*".repeat(name.length - 2)}${name.takeLast(1)}@$domain"
            } else {
                userEmail
            }
        } else {
            userEmail
        }
    } else {
        "j***@example.com"
    }

    // Role-based subtitle
    val roleSubtitle = when (userRole) {
        "user" -> "Protect your account and food donation history."
        "charity" -> "Protect your organization's account and food requests."
        "merchant" -> "Protect your business account and donation listings."
        else -> "Protect your account and personal information."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))


        // ── Subtitle ──
        Text(
            text = roleSubtitle,
            fontSize = 14.sp,
            color = TextGrey
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Security Status Card ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GreenConfirmed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = GreenConfirmed
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Your Account is Secure",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenConfirmed
                    )
                    Text(
                        text = "We're keeping an eye out for any suspicious activity.",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Section: LOGIN & RECOVERY ──
        Text(
            text = "LOGIN & RECOVERY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextGrey,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Change Password ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChangePassword() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(OrangeAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = OrangeAccent
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Change Password",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Text(
                        text = "Last changed 3 months ago",
                        fontSize = 12.sp,
                        color = TextGrey
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = TextGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))




        // ── Info Card ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "For security reasons, you'll need to verify your identity before making changes to your account credentials.",
                    fontSize = 13.sp,
                    color = Color(0xFFE65100),
                    lineHeight = 18.sp
                )
            }
        }
    }
}