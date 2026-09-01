package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.repository.UserRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun UserEditProfileScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val userId = CurrentUser.getUserId()
    val userRepo = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    // 获取当前用户信息
    val currentName = CurrentUser.getUserName()
    val currentEmail = CurrentUser.getUserEmail()

    var fullName by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf("Neighborhood Food Sharer") }
    var email by remember { mutableStateOf(currentEmail) }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 加载用户数据
    LaunchedEffect(Unit) {
        try {
            val user = userRepo.getUser(userId)
            if (user != null) {
                fullName = user.name.ifBlank { currentName }
                email = user.email.ifBlank { currentEmail }
                phone = user.phone ?: ""
                address = user.address ?: ""
                // 如果用户有自定义 bio，可以在这里设置
                // bio = user.bio ?: "Neighborhood Food Sharer"
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val canSave = fullName.isNotBlank() && email.isNotBlank()

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // ── Avatar ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (fullName.isNotEmpty()) fullName.take(1).uppercase() else "?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // 相机图标（覆盖层）
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(OrangeAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 姓名和角色显示 ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (fullName.isNotEmpty()) fullName else "Your Name",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = if (bio.isNotEmpty()) bio else "Bio",
                fontSize = 14.sp,
                color = TextGrey
            )
        }

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

        // ── Full Name ──
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Full Name", color = TextGrey) },
            placeholder = { Text("Alex Johnson", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Display Title / Bio ──
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Display Title / Bio", color = TextGrey) },
            placeholder = { Text("Neighborhood Food Sharer", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Email Address ──
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email Address", color = TextGrey) },
            placeholder = { Text("alex.j@example.com", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Phone Number ──
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number", color = TextGrey) },
            placeholder = { Text("+1 (555) 123-4567", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Save Changes Button ──
        Button(
            onClick = {
                if (!canSave) return@Button
                isSaving = true
                scope.launch {
                    try {
                        userRepo.updateUser(
                            userId = userId,
                            name = fullName,
                            email = email,
                            phone = phone,
                            address = address
                        )
                        // 更新 CurrentUser
                        CurrentUser.user?.let { user ->
                            user.name = fullName
                            user.email = email
                            user.phone = phone
                            user.address = address
                        }
                        isSaving = false
                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        onSaveSuccess()
                    } catch (e: Exception) {
                        isSaving = false
                        errorMessage = e.message
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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
                    text = "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}