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
import androidx.compose.runtime.saveable.rememberSaveable
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

    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var showCurrentPassword by rememberSaveable { mutableStateOf(false) }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    var isSaving by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var successMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // ✅ 强密码验证
    val hasMinLength = newPassword.length >= 8
    val hasUppercase = newPassword.any { it.isUpperCase() }
    val hasLowercase = newPassword.any { it.isLowerCase() }
    val hasDigit = newPassword.any { it.isDigit() }
    val hasSpecialChar = newPassword.any { !it.isLetterOrDigit() }

    val isPasswordStrong = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar

    val isCurrentPasswordValid = currentPassword.length >= 8
    val doPasswordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val canSave = isCurrentPasswordValid && isPasswordStrong && doPasswordsMatch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .imePadding(),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ensure your account is using a strong, secure password.",
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

        if (currentPassword.isNotEmpty() && currentPassword.length < 8) {
            Text(
                text = "⚠️ Current password must be at least 8 characters",
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
            placeholder = { Text("Create strong password", color = TextGrey) },
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

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ 密码强度要求
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPasswordStrong) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🔐 Password Requirements:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))

                PasswordRequirement(
                    text = "At least 8 characters",
                    isMet = hasMinLength
                )
                PasswordRequirement(
                    text = "One uppercase letter (A-Z)",
                    isMet = hasUppercase
                )
                PasswordRequirement(
                    text = "One lowercase letter (a-z)",
                    isMet = hasLowercase
                )
                PasswordRequirement(
                    text = "One number (0-9)",
                    isMet = hasDigit
                )
                PasswordRequirement(
                    text = "One special character (!@#$%^&*)",
                    isMet = hasSpecialChar
                )
            }
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
                    text = "Update Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PasswordRequirement(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isMet) GreenConfirmed else TextGrey
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) GreenConfirmed else TextGrey
        )
    }
}