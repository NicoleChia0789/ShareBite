package com.sharebite.app.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable  // ✅ 添加
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.repository.LoginRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit,
    onForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loginRepo = remember { LoginRepository() }

    // ✅ 使用 rememberSaveable
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var showCurrentPassword by rememberSaveable { mutableStateOf(false) }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    var isSaving by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var successMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // 验证条件
    val isCurrentPasswordValid = currentPassword.length >= 6
    val isNewPasswordValid = newPassword.length >= 6
    val doPasswordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val canSave = isCurrentPasswordValid && isNewPasswordValid && doPasswordsMatch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .imePadding(),  // ✅ 添加键盘适配
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ensure your account is using a long, random password to stay secure.",
            fontSize = 14.sp,
            color = TextGrey,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── 错误信息 ──
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

        if (successMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F5E6))
            ) {
                Text(
                    text = "✅ $successMessage",
                    modifier = Modifier.padding(16.dp),
                    color = GreenConfirmed,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Current Password ──
        OutlinedTextField(
            value = currentPassword,
            onValueChange = {
                currentPassword = it
                errorMessage = null
                successMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Current Password", color = TextGrey) },
            placeholder = { Text("Enter current password", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                    Icon(
                        imageVector = if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = TextGrey
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark
            )
        )

        if (currentPassword.isNotEmpty() && currentPassword.length < 6) {
            Text(
                text = "⚠️ Password must be at least 6 characters",
                color = RedCancelled,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── New Password ──
        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                errorMessage = null
                successMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New Password", color = TextGrey) },
            placeholder = { Text("Create new password (min 6 characters)", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                    Icon(
                        imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = TextGrey
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark
            )
        )

        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isNewPasswordValid) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isNewPasswordValid) GreenConfirmed else TextGrey
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isNewPasswordValid) "✅ Strong password" else "Must be at least 6 characters",
                fontSize = 12.sp,
                color = if (isNewPasswordValid) GreenConfirmed else TextGrey
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Confirm New Password ──
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
                successMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm New Password", color = TextGrey) },
            placeholder = { Text("Confirm new password", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = TextGrey
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark
            )
        )

        if (confirmPassword.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (doPasswordsMatch) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (doPasswordsMatch) GreenConfirmed else RedCancelled
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (doPasswordsMatch) "✅ Passwords match" else "⚠️ Passwords do not match",
                    fontSize = 12.sp,
                    color = if (doPasswordsMatch) GreenConfirmed else RedCancelled
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Forgot Password?",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = OrangeAccent,
            modifier = Modifier.clickable { onForgotPassword() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Save Button ──
        Button(
            onClick = {
                if (!canSave) return@Button
                isSaving = true
                errorMessage = null
                successMessage = null

                scope.launch {
                    try {
                        val userId = CurrentUser.getUserId()
                        if (userId.isEmpty()) {
                            errorMessage = "User not logged in"
                            isSaving = false
                            return@launch
                        }

                        val success = loginRepo.updatePassword(
                            userId = userId,
                            currentPassword = currentPassword,
                            newPassword = newPassword
                        )

                        if (success) {
                            successMessage = "✅ Password updated successfully!"
                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            isSaving = false
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            onSave(newPassword, currentPassword)
                        } else {
                            errorMessage = "❌ Current password is incorrect"
                            isSaving = false
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to update password"
                        isSaving = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canSave && !isSaving) OrangeAccent else Color.Gray,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = canSave && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Save New Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "📋 Password Requirements:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Minimum 6 characters",
                    fontSize = 12.sp,
                    color = TextGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}