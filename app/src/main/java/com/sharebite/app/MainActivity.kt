package com.sharebite.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.navigation.AppNavHost
import com.sharebite.app.ui.theme.ShareBiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShareBiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RoleSelectionScreen()
                }
            }
        }
    }
}

@Composable
fun RoleSelectionScreen() {
    var selectedRole by rememberSaveable { mutableStateOf<String?>(null) }

    if (selectedRole != null) {
        AppNavHost(role = selectedRole!!)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Select Your Role",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF7A45)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Choose how you want to use ShareBite", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(40.dp))

            // 三个角色卡片，用 Column 确保每个卡片宽度完全一致
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleCard(
                    label = "🏛️ Charity",
                    description = "Manage food requests and donations",
                    color = Color(0xFF2E7D32),
                    onClick = { selectedRole = "charity" }
                )

                RoleCard(
                    label = "👤 User",
                    description = "Browse and reserve surplus food",
                    color = Color(0xFF1565C0),
                    onClick = { selectedRole = "user" }
                )

                RoleCard(
                    label = "🏪 Merchant",
                    description = "Donate surplus food to the community",
                    color = Color(0xFFE65100),
                    onClick = { selectedRole = "merchant" }
                )
            }
        }
    }
}

@Composable
fun RoleCard(
    label: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧文字
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            // 右侧按钮 - 固定宽度90dp，所有按钮完全一致
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.width(90.dp)
            ) {
                Text("Enter", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}