package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun UserProfileScreen(
    paddingValues: PaddingValues,
    onSavedFood: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit
) {
    val userName = CurrentUser.getUserName()
    val userEmail = CurrentUser.getUserEmail()
    val userPhone = "Not set yet"
    val userAddress = "Not set yet"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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