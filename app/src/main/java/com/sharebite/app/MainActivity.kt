package com.sharebite.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sharebite.app.ui.navigation.AppNavHost
import com.sharebite.app.ui.theme.ShareBiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var selectedRole by remember { mutableStateOf<String?>(null) }

            ShareBiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (selectedRole == null) {
                        RoleSelectionScreen(
                            onRoleSelected = { role ->
                                selectedRole = role
                            }
                        )
                    } else {
                        AppNavHost(role = selectedRole!!)
                    }
                }
            }
        }
    }
}

// 简单的角色选择界面
@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Select Your Role", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRoleSelected("user") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("User")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onRoleSelected("charity") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Charity")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onRoleSelected("merchant") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Merchant")
        }
    }
}