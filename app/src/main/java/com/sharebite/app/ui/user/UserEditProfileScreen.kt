package com.sharebite.app.ui.user

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.saveable.rememberSaveable  // ✅ 添加
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.repository.UserRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import com.sharebite.app.ui.profile.ProfilePhotoPicker
import kotlinx.coroutines.launch

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

    // 键盘控制
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // 获取当前用户信息
    val currentName = CurrentUser.getUserName()
    val currentEmail = CurrentUser.getUserEmail()

    // ✅ 使用 rememberSaveable
    var fullName by rememberSaveable { mutableStateOf(currentName) }
    var bio by rememberSaveable { mutableStateOf("Neighborhood Food Sharer") }
    var email by rememberSaveable { mutableStateOf(currentEmail) }
    var phone by rememberSaveable { mutableStateOf("") }
    var existingAddress by rememberSaveable { mutableStateOf("") }
    var avatarUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var hasInitializedForm by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // 验证错误状态
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var bioError by rememberSaveable { mutableStateOf<String?>(null) }

    // 加载用户数据
    LaunchedEffect(Unit) {
        try {
            val user = userRepo.getUser(userId)
            if (user != null && !hasInitializedForm) {
                fullName = user.name.ifBlank { currentName }
                email = user.email.ifBlank { currentEmail }
                phone = user.phone ?: ""
                existingAddress = user.address ?: ""
                avatarUrl = user.avatarUrl
                hasInitializedForm = true
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    // 验证函数
    fun validateName(): Boolean {
        nameError = when {
            fullName.isBlank() -> "Full name is required"
            fullName.length < 2 -> "Name must be at least 2 characters"
            fullName.length > 50 -> "Name must be less than 50 characters"
            else -> null
        }
        return nameError == null
    }

    fun validateEmail(): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        emailError = when {
            email.isBlank() -> "Email address is required"
            !emailRegex.matches(email) -> "Please enter a valid email address"
            else -> null
        }
        return emailError == null
    }

    fun validatePhone(): Boolean {
        val phoneRegex = Regex("^(\\+?[0-9]{1,4}[-.\\s]?)?[0-9]{7,15}$")
        phoneError = when {
            phone.isBlank() -> null
            !phoneRegex.matches(phone) -> "Please enter a valid phone number"
            phone.length < 7 -> "Phone number is too short"
            else -> null
        }
        return phoneError == null
    }

    fun validateBio(): Boolean {
        bioError = when {
            bio.isBlank() -> null
            bio.length > 100 -> "Bio must be less than 100 characters"
            else -> null
        }
        return bioError == null
    }

    fun validateAll(): Boolean {
        return listOf(
            validateName(),
            validateEmail(),
            validatePhone(),
            validateBio()
        ).all { it }
    }

    val canSave = fullName.isNotBlank() && email.isNotBlank() && !isSaving

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
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .imePadding()  // ✅ 添加键盘适配
            .clickable(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            )
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            ProfilePhotoPicker(
                displayName = fullName,
                selectedPhotoUri = selectedPhotoUri,
                existingPhotoUrl = avatarUrl,
                onPhotoSelected = { selectedPhotoUri = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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

        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                if (nameError != null) validateName()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Full Name", color = TextGrey) },
            placeholder = { Text("Alex Johnson", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = nameError != null,
            supportingText = {
                if (nameError != null) {
                    Text(nameError!!, color = RedCancelled)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent,
                errorBorderColor = RedCancelled,
                errorLabelColor = RedCancelled
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = {
                bio = it
                if (bioError != null) validateBio()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Display Title / Bio", color = TextGrey) },
            placeholder = { Text("Neighborhood Food Sharer", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = bioError != null,
            supportingText = {
                if (bioError != null) {
                    Text(bioError!!, color = RedCancelled)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent,
                errorBorderColor = RedCancelled,
                errorLabelColor = RedCancelled
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError != null) validateEmail()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email Address", color = TextGrey) },
            placeholder = { Text("alex.j@example.com", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = emailError != null,
            supportingText = {
                if (emailError != null) {
                    Text(emailError!!, color = RedCancelled)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent,
                errorBorderColor = RedCancelled,
                errorLabelColor = RedCancelled
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                if (phoneError != null) validatePhone()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number", color = TextGrey) },
            placeholder = { Text("012-3456789", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = phoneError != null,
            supportingText = {
                if (phoneError != null) {
                    Text(phoneError!!, color = RedCancelled)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent,
                errorBorderColor = RedCancelled,
                errorLabelColor = RedCancelled
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!validateAll()) {
                    Toast.makeText(context, "Please fix all errors", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSaving = true
                scope.launch {
                    try {
                        val savedAvatarUrl = selectedPhotoUri?.let {
                            userRepo.uploadProfilePhoto(context, it, userId)
                        } ?: avatarUrl
                        userRepo.updateUser(
                            userId = userId,
                            name = fullName,
                            email = email,
                            phone = phone,
                            address = existingAddress,
                            avatarUrl = savedAvatarUrl
                        )
                        CurrentUser.user?.let { user ->
                            user.name = fullName
                            user.email = email
                            user.phone = phone
                            user.avatarUrl = savedAvatarUrl
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
                containerColor = if (canSave) OrangeAccent else Color.Gray,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = canSave
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
