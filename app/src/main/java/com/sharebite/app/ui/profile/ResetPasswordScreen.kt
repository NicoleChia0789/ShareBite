package com.sharebite.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*

@Composable
fun ResetPasswordScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onReset: (String) -> Unit
) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    // ✅ 强密码验证
    val hasMinLength = newPassword.length >= 8
    val hasUppercase = newPassword.any { it.isUpperCase() }
    val hasLowercase = newPassword.any { it.isLowerCase() }
    val hasDigit = newPassword.any { it.isDigit() }
    val hasSpecialChar = newPassword.any { !it.isLetterOrDigit() }

    val isPasswordStrong = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    val doPasswordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val canReset = isPasswordStrong && doPasswordsMatch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp)
    ) {
        Text(
            text = "Create a strong password for your account.",
            fontSize = 14.sp,
            color = TextGrey
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── New Password ──
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
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
            onValueChange = { confirmPassword = it },
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

        Spacer(modifier = Modifier.height(24.dp))

        // ── Reset Password Button ──
        Button(
            onClick = { onReset(newPassword) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canReset) OrangeAccent else Color.Gray,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = canReset
        ) {
            Text(
                text = "Reset Password",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
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