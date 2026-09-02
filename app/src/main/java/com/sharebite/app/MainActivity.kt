package com.sharebite.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sharebite.app.data.repository.LoginRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.navigation.AppNavHost
import com.sharebite.app.ui.profile.ForgotPasswordScreen
import com.sharebite.app.ui.profile.ResetPasswordScreen
import com.sharebite.app.ui.profile.SignIn
import com.sharebite.app.ui.profile.SignUp
import com.sharebite.app.ui.theme.ShareBiteTheme
import kotlinx.coroutines.launch

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
    var showForgotPassword by rememberSaveable { mutableStateOf(false) }
    var showResetPassword by rememberSaveable { mutableStateOf(false) }
    var resetEmail by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val loginRepo = remember { LoginRepository() }

    LaunchedEffect(Unit) {
        isAuthenticated = CurrentUser.isLoggedIn()
        userRole = CurrentUser.user?.role ?: ""
    }

    // ✅ ResetPassword 页面（认证前）
    if (showResetPassword) {
        ResetPasswordScreen(
            paddingValues = PaddingValues(0.dp),
            email = resetEmail,
            onBack = {
                showResetPassword = false
                showForgotPassword = true
            },
            onReset = { email, newPassword ->
                coroutineScope.launch {
                    try {
                        val success = loginRepo.updatePasswordByEmail(email, newPassword)
                        if (success) {
                            Toast.makeText(context, "Password reset successfully!", Toast.LENGTH_SHORT).show()
                            showResetPassword = false
                            showForgotPassword = false
                        } else {
                            Toast.makeText(context, "Failed to reset password", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        return
    }

    // ✅ ForgotPassword 页面（认证前）
    if (showForgotPassword) {
        ForgotPasswordScreen(
            paddingValues = PaddingValues(0.dp),
            onBack = {
                showForgotPassword = false
            },
            onSendResetLink = { email ->
                resetEmail = email.trim()
                showForgotPassword = false
                showResetPassword = true
            }
        )
        return
    }

    // ✅ 已认证 → 进入主应用
    if (isAuthenticated && userRole.isNotEmpty()) {
        AppNavHost(
            role = userRole,
            onLogout = {
                CurrentUser.logout()
                isAuthenticated = false
                userRole = ""
                showSignIn = false
            }
        )
    } else {
        // ✅ 未认证 → 显示登录/注册
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
                    showForgotPassword = true
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