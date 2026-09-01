package com.sharebite.app

import android.os.Bundle
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
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.navigation.AppNavHost
import com.sharebite.app.ui.profile.ForgotPasswordScreen
import com.sharebite.app.ui.navigation.ForgotPasswordRoute
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

    // ✅ 新增：控制是否显示 ForgotPassword 页面
    var showForgotPassword by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAuthenticated = CurrentUser.isLoggedIn()
        userRole = CurrentUser.user?.role ?: ""
    }

    // ✅ 如果显示 ForgotPassword 页面，优先显示
    if (showForgotPassword) {
        ForgotPasswordScreen(
            paddingValues = PaddingValues(),  // 或者从外部传入
            onBack = {
                showForgotPassword = false  // 返回登录页
            },
            onSendResetLink = { email ->
                // ✅ 发送重置链接
                // 这里可以调用 API 发送重置邮件
                // 成功后跳转回登录页或显示成功消息
                showForgotPassword = false
            }
        )
        return
    }

    // ✅ 如果已认证，进入主界面
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
                    // ✅ 跳转到 Forgot Password 页面
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