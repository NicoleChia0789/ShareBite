package com.sharebite.app.ui.charity

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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.sharebite.app.util.searchAddressSuggestions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CharityEditProfileScreen(
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

    val currentName = CurrentUser.getUserName()
    val currentEmail = CurrentUser.getUserEmail()

    // ✅ 使用 rememberSaveable
    var organisationName by rememberSaveable { mutableStateOf(currentName) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var organisationAddress by rememberSaveable { mutableStateOf("") }
    var missionStatement by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf(currentEmail) }
    var avatarUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var addressSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var showAddressSuggestions by rememberSaveable { mutableStateOf(false) }
    var addressVerified by rememberSaveable { mutableStateOf(false) }

    // 验证错误状态
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var addressError by rememberSaveable { mutableStateOf<String?>(null) }
    var missionError by rememberSaveable { mutableStateOf<String?>(null) }

    // Load user data
    LaunchedEffect(Unit) {
        try {
            val user = userRepo.getUser(userId)
            if (user != null) {
                organisationName = user.name.ifBlank { currentName }
                email = user.email.ifBlank { currentEmail }
                phoneNumber = user.phone ?: ""
                organisationAddress = user.address ?: ""
                addressVerified = true
                avatarUrl = user.avatarUrl
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(organisationAddress, showAddressSuggestions) {
        if (!showAddressSuggestions || organisationAddress.trim().length < 3) {
            addressSuggestions = emptyList()
        } else {
            delay(400)
            addressSuggestions = searchAddressSuggestions(context, organisationAddress)
        }
    }

    // 验证函数
    fun validateName(): Boolean {
        nameError = when {
            organisationName.isBlank() -> "Organization name is required"
            organisationName.length < 2 -> "Name must be at least 2 characters"
            organisationName.length > 50 -> "Name must be less than 50 characters"
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
            phoneNumber.isBlank() -> null
            !phoneRegex.matches(phoneNumber) -> "Please enter a valid phone number"
            phoneNumber.length < 7 -> "Phone number is too short"
            else -> null
        }
        return phoneError == null
    }

    fun validateAddress(): Boolean {
        addressError = when {
            organisationAddress.isBlank() -> null
            organisationAddress.length < 5 -> "Please enter a more complete address"
            !addressVerified -> "Select a real address from the search results"
            else -> null
        }
        return addressError == null
    }

    fun validateMission(): Boolean {
        missionError = when {
            missionStatement.isBlank() -> null
            missionStatement.length > 300 -> "Mission statement must be less than 300 characters"
            else -> null
        }
        return missionError == null
    }

    fun validateAll(): Boolean {
        return listOf(
            validateName(),
            validateEmail(),
            validatePhone(),
            validateAddress(),
            validateMission()
        ).all { it }
    }

    val canSave = organisationName.isNotBlank() && email.isNotBlank() && !isSaving

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
            .imePadding()
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
                displayName = organisationName,
                selectedPhotoUri = selectedPhotoUri,
                existingPhotoUrl = avatarUrl,
                onPhotoSelected = { selectedPhotoUri = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap to update logo",
            fontSize = 12.sp,
            color = TextGrey,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

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

        // ✅ Organization Name - 使用 AppTextFieldDefaults
        OutlinedTextField(
            value = organisationName,
            onValueChange = {
                organisationName = it
                if (nameError != null) validateName()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Organization Name") },
            placeholder = { Text("Hopeful Harvest Food Bank") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = nameError != null,
            supportingText = {
                if (nameError != null) {
                    Text(nameError!!, color = RedCancelled)
                }
            },
            colors = AppTextFieldDefaults.colors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Phone Number - 使用 AppTextFieldDefaults
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
                if (phoneError != null) validatePhone()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number") },
            placeholder = { Text("012-3456789") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = phoneError != null,
            supportingText = {
                if (phoneError != null) {
                    Text(phoneError!!, color = RedCancelled)
                }
            },
            colors = AppTextFieldDefaults.colors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Organization Address - 使用 AppTextFieldDefaults
        Column {
            OutlinedTextField(
                value = organisationAddress,
                onValueChange = {
                    organisationAddress = it
                    addressVerified = false
                    showAddressSuggestions = true
                    if (addressError != null) validateAddress()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Organization Address") },
                placeholder = { Text("Search your organization address") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = addressError != null,
                supportingText = {
                    when {
                        addressError != null -> Text(addressError!!, color = RedCancelled)
                        !addressVerified && organisationAddress.isNotBlank() -> Text(
                            "Select a real address from the search results",
                            color = OrangeAccent
                        )
                    }
                },
                colors = AppTextFieldDefaults.colors()
            )
            if (showAddressSuggestions && addressSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column {
                        addressSuggestions.forEach { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        organisationAddress = suggestion
                                        addressVerified = true
                                        showAddressSuggestions = false
                                        addressSuggestions = emptyList()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                color = TextDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Mission Statement - 使用 AppTextFieldDefaults
        OutlinedTextField(
            value = missionStatement,
            onValueChange = {
                if (it.length <= 300) missionStatement = it
                if (missionError != null) validateMission()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            label = { Text("Mission Statement") },
            placeholder = {
                Text("Nourishing our community by rescuing surplus food...")
            },
            shape = RoundedCornerShape(12.dp),
            maxLines = 5,
            isError = missionError != null,
            supportingText = {
                if (missionError != null) {
                    Text(missionError!!, color = RedCancelled)
                }
            },
            colors = AppTextFieldDefaults.colors()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${missionStatement.length} / 300",
                fontSize = 12.sp,
                color = if (missionStatement.length > 280) OrangeAccent else TextGrey
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Email - 使用 AppTextFieldDefaults
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError != null) validateEmail()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Primary Contact Email") },
            placeholder = { Text("hello@hopefulharvest.org") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = emailError != null,
            supportingText = {
                if (emailError != null) {
                    Text(emailError!!, color = RedCancelled)
                }
            },
            colors = AppTextFieldDefaults.colors()
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
                            name = organisationName,
                            email = email,
                            phone = phoneNumber,
                            address = organisationAddress,
                            avatarUrl = savedAvatarUrl
                        )
                        CurrentUser.user?.let { user ->
                            user.name = organisationName
                            user.email = email
                            user.phone = phoneNumber
                            user.address = organisationAddress
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextGrey
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Cancel",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
