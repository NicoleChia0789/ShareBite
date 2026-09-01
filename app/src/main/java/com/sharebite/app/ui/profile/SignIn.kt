package com.sharebite.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.repository.LoginRepository
import com.sharebite.app.data.session.CurrentUser
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.navigation.NavController

@Composable
fun SignIn(

    onSignInSuccess: (String) -> Unit = {}, // ✅ Pass role only
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = remember { LoginRepository() }
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val textDark = Color(0xFF1E1B16) // ✅ Darker text color for input

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 60.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ShareBite",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Welcome back!",
                fontSize = 15.sp,
                color = Color(0xFF7F8C8D),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Email Address",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = {
                            Text(
                                "jane@example.com",
                                color = Color(0xFFBDC3C7),
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3498DB),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedTextColor = textDark, // ✅ Darker text
                            unfocusedTextColor = textDark, // ✅ Darker text
                            focusedPlaceholderColor = Color(0xFFBDC3C7),
                            unfocusedPlaceholderColor = Color(0xFFBDC3C7)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = textDark // ✅ Darker text
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = {
                            Text(
                                "••••••••",
                                color = Color(0xFFBDC3C7),
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3498DB),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedTextColor = textDark, // ✅ Darker text
                            unfocusedTextColor = textDark, // ✅ Darker text
                            focusedPlaceholderColor = Color(0xFFBDC3C7),
                            unfocusedPlaceholderColor = Color(0xFFBDC3C7)
                        ),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = textDark // ✅ Darker text
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = onForgotPasswordClick,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Forgot password?",
                            fontSize = 13.sp,
                            color = Color(0xFF3498DB),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    val user = authRepository.login(email, password)
                                    CurrentUser.user = user

                                    isLoading = false

                                    // ✅ Clear fields after success
                                    email = ""
                                    password = ""

                                    Toast.makeText(
                                        context,
                                        "Welcome back, ${user.name}!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // ✅ Navigate with role only
                                    onSignInSuccess(user.role)
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7A45),
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
                                text = "Sign In →",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))




                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 13.sp,
                    color = Color(0xFF7F8C8D)
                )
                Text(
                    text = "Sign up",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50),
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}