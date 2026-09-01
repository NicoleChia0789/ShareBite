package com.sharebite.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.repository.LoginRepository
import com.sharebite.app.data.session.CurrentUser
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.Warning
import com.sharebite.app.ui.theme.GreenConfirmed
import com.sharebite.app.ui.theme.RedCancelled
import com.sharebite.app.ui.theme.TextDark
import com.sharebite.app.ui.theme.TextGrey

@Composable
fun SignUp(
    onSignUpClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = remember { LoginRepository() }
    val coroutineScope = rememberCoroutineScope()

    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf("User") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // ✅ 强密码验证
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    val isPasswordStrong = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    val doPasswordsMatch = password.isNotEmpty() && password == confirmPassword
    val canCreateAccount = fullName.isNotBlank() &&
            email.isNotBlank() &&
            isPasswordStrong &&
            doPasswordsMatch

    val roles = listOf("User", "Merchant", "Charity")

    val primaryNavy = Color(0xFF2C3E50)
    val accentOrange = Color(0xFFFF7A45)
    val softGray = Color(0xFFF8F9FA)
    val cardWhite = Color(0xFFFFFFFF)
    val mutedGray = Color(0xFF7F8C8D)
    val lightGray = Color(0xFF95A5A6)
    val borderGray = Color(0xFFE0E0E0)
    val placeholderGray = Color(0xFFBDC3C7)
    val blueAccent = Color(0xFF3498DB)
    val textDark = Color(0xFF1E1B16)

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(softGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp)
                .padding(bottom = 80.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── App Header ──
            Text(
                text = "ShareBite",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryNavy,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Create your account",
                fontSize = 15.sp,
                color = mutedGray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Join our community to start sharing today.",
                fontSize = 13.sp,
                color = lightGray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Role Selection ──
            Text(
                text = "I am signing up as a:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = primaryNavy,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                roles.forEach { role ->
                    val isSelected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) primaryNavy else Color.White
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) primaryNavy else borderGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedRole = role }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = role,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else primaryNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Card Container ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // ── Full Name ──
                    Text(
                        text = "Full Name",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = {
                            Text(
                                "Jane Doe",
                                color = placeholderGray,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = blueAccent,
                            unfocusedBorderColor = borderGray,
                            focusedTextColor = textDark,
                            unfocusedTextColor = textDark,
                            focusedPlaceholderColor = placeholderGray,
                            unfocusedPlaceholderColor = placeholderGray
                        ),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = textDark
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Email ──
                    Text(
                        text = "Email Address",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = {
                            Text(
                                "jane@example.com",
                                color = placeholderGray,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = blueAccent,
                            unfocusedBorderColor = borderGray,
                            focusedTextColor = textDark,
                            unfocusedTextColor = textDark,
                            focusedPlaceholderColor = placeholderGray,
                            unfocusedPlaceholderColor = placeholderGray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = textDark
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Password ──
                    Text(
                        text = "Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = {
                            Text(
                                "*********",
                                color = placeholderGray,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = blueAccent,
                            unfocusedBorderColor = borderGray,
                            focusedTextColor = textDark,
                            unfocusedTextColor = textDark,
                            focusedPlaceholderColor = placeholderGray,
                            unfocusedPlaceholderColor = placeholderGray
                        ),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = textDark
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // ✅ 密码强度要求
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPasswordStrong) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🔐 Password Requirements:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))

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

                    Spacer(modifier = Modifier.height(6.dp))

                    // ── Confirm Password ──
                    Text(
                        text = "Confirm Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = {
                            Text(
                                "*********",
                                color = placeholderGray,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = blueAccent,
                            unfocusedBorderColor = borderGray,
                            focusedTextColor = textDark,
                            unfocusedTextColor = textDark,
                            focusedPlaceholderColor = placeholderGray,
                            unfocusedPlaceholderColor = placeholderGray
                        ),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = textDark
                        )
                    )

                    if (confirmPassword.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (doPasswordsMatch) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (doPasswordsMatch) GreenConfirmed else RedCancelled
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (doPasswordsMatch) "✅ Passwords match" else "⚠️ Passwords do not match",
                                fontSize = 11.sp,
                                color = if (doPasswordsMatch) GreenConfirmed else RedCancelled
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Create Account Button ──
                    Button(
                        onClick = {
                            when {
                                fullName.isBlank() -> errorMessage = "Please enter your full name"
                                email.isBlank() -> errorMessage = "Please enter your email"
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                    errorMessage = "Please enter a valid email"
                                !isPasswordStrong -> {
                                    errorMessage = "Please create a strong password (8+ chars, uppercase, lowercase, number, special character)"
                                }
                                !doPasswordsMatch -> errorMessage = "Passwords do not match"
                                else -> {
                                    errorMessage = null
                                    isLoading = true

                                    coroutineScope.launch {
                                        try {
                                            val roleLower = selectedRole.lowercase()
                                            val newUser = authRepository.register(
                                                email = email,
                                                password = password,
                                                name = fullName,
                                                role = roleLower
                                            )
                                            CurrentUser.user = newUser
                                            isLoading = false
                                            fullName = ""
                                            email = ""
                                            password = ""
                                            confirmPassword = ""

                                            Toast.makeText(
                                                context,
                                                "Account created successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onSignUpClick(newUser.role)
                                        } catch (e: Exception) {
                                            isLoading = false
                                            errorMessage = e.message
                                            Toast.makeText(
                                                context,
                                                "Error: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canCreateAccount && !isLoading) accentOrange else Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = canCreateAccount && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Account →",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Login Link ──
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 13.sp,
                    color = mutedGray
                )
                Text(
                    text = "Log in",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryNavy,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}

@Composable
private fun PasswordRequirement(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (isMet) GreenConfirmed else TextGrey
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (isMet) GreenConfirmed else TextGrey
        )
    }
}