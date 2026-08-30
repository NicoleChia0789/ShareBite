package com.sharebite.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.navigation.AppNavHost
import com.sharebite.app.ui.profile.SignIn
import com.sharebite.app.ui.profile.SignUp
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
                    ShareBiteApp()
                }
            }
        }
    }
}

@Composable
fun ShareBiteApp() {
    var isAuthenticated by rememberSaveable { mutableStateOf(CurrentUser.isLoggedIn()) }
    var userRole by rememberSaveable { mutableStateOf(CurrentUser.user?.role ?: "") }
    var showSignIn by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAuthenticated = CurrentUser.isLoggedIn()
        userRole = CurrentUser.user?.role ?: ""
    }

    // ✅ If authenticated, navigate to role-specific dashboard
    if (isAuthenticated && userRole.isNotEmpty()) {
        AppNavHost(
            role = userRole, // ✅ Pass role: "charity", "user", or "merchant"
            onLogout = {
                CurrentUser.logout()
                isAuthenticated = false
                userRole = ""
                showSignIn = false
            }
        )
    } else {
        if (showSignIn) {
            SignIn(
                onSignInSuccess = { role ->
                    userRole = role
                    isAuthenticated = true
                },
                onSignUpClick = {
                    showSignIn = false
                },
                onForgotPasswordClick = {
                    // Navigate to forgot password
                }
            )
        } else {
            SignUp(
                onSignUpClick = { role ->
                    userRole = role
                    isAuthenticated = true
                },
                onLoginClick = {
                    showSignIn = true
                }
            )
        }
    }
}
