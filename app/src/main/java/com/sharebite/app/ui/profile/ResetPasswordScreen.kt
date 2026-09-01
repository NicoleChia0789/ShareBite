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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*

@Composable
fun ResetPasswordScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onReset: (String) -> Unit

) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val hasMinLength = newPassword.length >= 8
    val hasUppercase = newPassword.any { it.isUpperCase() }
    val hasNumber = newPassword.any { it.isDigit() }
    val isPasswordValid = hasMinLength && hasUppercase && hasNumber
    val doPasswordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val canReset = isPasswordValid && doPasswordsMatch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp)
    ) {


        Spacer(modifier = Modifier.height(40.dp))

        // ── 标题 ──
        Text(
            text = "Reset Password",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter a new password for your account.",
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
            placeholder = { Text("Create new password", color = TextGrey) },
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

        Spacer(modifier = Modifier.height(16.dp))

        // ── Password Requirements ──
        Text(
            text = "Password must contain:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordRequirement(
            text = "At least 8 characters",
            isMet = hasMinLength
        )
        PasswordRequirement(
            text = "One uppercase letter",
            isMet = hasUppercase
        )
        PasswordRequirement(
            text = "One number",
            isMet = hasNumber
        )

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
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isMet) GreenConfirmed else TextGrey
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isMet) GreenConfirmed else TextGrey
        )
    }
}

