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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.repository.LoginRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.util.PasswordUtils
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun SignUp(
    selectedRole: String = "User",
    onSignUpClick: (String) -> Unit = {}, // ✅ Pass role only
    onLoginClick: () -> Unit = {},
    onChooseRoleClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = remember { LoginRepository() }
    val coroutineScope = rememberCoroutineScope()

    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) } // ✅ New state
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) } // ✅ New state
    val isPasswordStrong = PasswordUtils.isStrongPassword(password)


    val primaryNavy = Color(0xFF38251B)
    val accentOrange = Color(0xFFFF7A45)
    val softGray = Color(0xFFFFF8EF)
    val cardWhite = Color(0xFFFFFDF9)
    val mutedGray = Color(0xFF826F63)
    val lightGray = Color(0xFF826F63)
    val borderGray = Color(0xFFF0DCCB)
    val placeholderGray = Color(0xFFB4A399)
    val blueAccent = accentOrange
    val textDark = Color(0xFF1E1B16) // ✅ Darker text color for input

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
            AuthHeader(
                title = "Registering as $selectedRole",
                subtitle = "Create your ShareBite account."
            )

            Text(
                text = "Change role",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentOrange,
                modifier = Modifier
                    .clickable(onClick = onChooseRoleClick)
                    .padding(vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = textDark
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color(0xFF7F8C8D)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPasswordStrong) "Strong password ✓" else PasswordUtils.STRONG_PASSWORD_REQUIREMENTS,
                        color = if (isPasswordStrong) Color(0xFF2E7D32) else mutedGray,
                        fontSize = 12.sp
                    )

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
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = textDark
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                            ) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = Color(0xFF7F8C8D)
                                )
                            }
                        }
                    )

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
                                !isPasswordStrong ->
                                    errorMessage = PasswordUtils.STRONG_PASSWORD_REQUIREMENTS
                                password != confirmPassword ->
                                    errorMessage = "Passwords do not match"

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

                                            // ✅ Clear fields after success
                                            fullName = ""
                                            email = ""
                                            password = ""
                                            confirmPassword = ""

                                            Toast.makeText(
                                                context,
                                                "Account created successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // ✅ Navigate with role only
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
                            containerColor = accentOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading
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
