package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*

@Composable
fun UserProfileScreen(
    paddingValues: PaddingValues,
    onEditProfile: () -> Unit,
    onSavedFood: () -> Unit,
    onLogout: () -> Unit
) {
    // 临时硬编码，后续从 Supabase users 表获取
    val userName = "Food Lover"
    val userEmail = "user@example.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        // 头像区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍽️", fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    userName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    userEmail,
                    fontSize = 14.sp,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onEditProfile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Edit Profile")
                }
            }
        }

        // 菜单
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                ProfileMenuItem(
                    icon = "❤️",
                    title = "Saved Food",
                    onClick = onSavedFood
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    icon = "🔔",
                    title = "Notifications",
                    onClick = { /* 通知已在 AppScaffold 中处理 */ }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    icon = "❓",
                    title = "Help & Support",
                    onClick = { /* TODO */ }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    icon = "🚪",
                    title = "Logout",
                    onClick = onLogout,
                    titleColor = RedCancelled
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            "ShareBite v1.0.0",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 12.sp,
            color = TextGrey
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: String,
    title: String,
    onClick: () -> Unit,
    titleColor: Color = TextDark
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            fontSize = 15.sp,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        Text("›", fontSize = 20.sp, color = TextGrey)
    }
}