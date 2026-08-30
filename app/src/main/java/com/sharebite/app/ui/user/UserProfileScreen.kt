package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.repository.UserRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*

@Composable
fun UserProfileScreen(
    paddingValues: PaddingValues,
    onSavedFood: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit
) {
    val userId = "258c6924-0bdd-481f-a6d8-cdafb4e7abe3"
    val userRepo = remember { UserRepository() }

    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("Not set yet") }
    var userAddress by remember { mutableStateOf("Not set yet") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val user = userRepo.getUser(userId)
            if (user != null) {
                userName = user.name.ifBlank { "User" }
                userEmail = user.email.ifBlank { "No email" }
                userPhone = user.phone?.ifBlank { "Not set yet" } ?: "Not set yet"
                userAddress = user.address?.ifBlank { "Not set yet" } ?: "Not set yet"
                println("✅ User loaded: $userName, $userEmail")
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
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── 显示错误信息 ──
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = userEmail,
                    fontSize = 13.sp,
                    color = TextGrey
                )
                // ✅ 显示当前使用的 userId（方便调试）
                Text(
                    text = "ID: $userId",
                    fontSize = 10.sp,
                    color = TextGrey.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
        ) {
            Column {
                ProfileInfoRow(Icons.Default.Home, "Address", userAddress)
                Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))
                ProfileInfoRow(Icons.Default.Phone, "Phone", userPhone)
                Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))
                ProfileInfoRow(Icons.Default.Info, "Role", "User")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
        ) {
            Column {
                ProfileMenuRow("Saved Food", onSavedFood)
                Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))
                ProfileMenuRow("Help & Support", onHelpClick)
                Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))
                ProfileMenuRow("About ShareBite", onAboutClick)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = RedCancelled
            ),
            shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(1.dp, RedCancelled)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Out",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = TextGrey
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextGrey,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark
        )
    }
}

@Composable
private fun ProfileMenuRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextDark,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = TextGrey
        )
    }
}