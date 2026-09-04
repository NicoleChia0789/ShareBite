package com.sharebite.app.ui.merchant

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.sharebite.app.data.repository.UserRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import com.sharebite.app.ui.profile.ProfilePhotoPicker
import com.sharebite.app.util.searchAddressSuggestions
import com.sharebite.app.util.LocationUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MerchantEditProfileScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val userId = CurrentUser.getUserId()
    val userRepo = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    val locationUtils = remember { LocationUtils(context as Activity) }

    // 键盘控制
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val currentName = CurrentUser.getUserName()
    val currentEmail = CurrentUser.getUserEmail()

    // ✅ 使用 rememberSaveable
    var businessName by rememberSaveable { mutableStateOf(currentName) }
    var businessAddress by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var briefDescription by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf(currentEmail) }
    var avatarUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var addressSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var showAddressSuggestions by rememberSaveable { mutableStateOf(false) }
    var addressVerified by rememberSaveable { mutableStateOf(false) }
    var addressLocationMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showGpsPrompt by rememberSaveable { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun detectBusinessAddress() {
        scope.launch {
            addressLocationMessage = "Detecting your address..."
            if (!locationUtils.checkLocationSettings()) {
                addressLocationMessage = "Please enable GPS and try again."
                return@launch
            }
            val location = locationUtils.getCurrentLocation() ?: locationUtils.getLastKnownLocation()
            if (location == null) {
                addressLocationMessage = "Could not detect your address. Please try again."
                return@launch
            }
            @Suppress("DEPRECATION")
            val detectedAddress = Geocoder(context, java.util.Locale.ENGLISH)
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?.getAddressLine(0)
                ?.trim()
            if (!detectedAddress.isNullOrBlank()) {
                businessAddress = detectedAddress
                addressVerified = true
                showAddressSuggestions = false
                addressSuggestions = emptyList()
                addressLocationMessage = "Address detected from GPS."
            } else {
                addressLocationMessage = "Could not convert your location into an address."
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) showGpsPrompt = true
        else addressLocationMessage = "Location permission denied."
    }

    // 验证错误状态
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var addressError by rememberSaveable { mutableStateOf<String?>(null) }
    var descriptionError by rememberSaveable { mutableStateOf<String?>(null) }

    // Load user data
    LaunchedEffect(Unit) {
        try {
            val user = userRepo.getUser(userId)
            if (user != null) {
                businessName = user.name.ifBlank { currentName }
                email = user.email.ifBlank { currentEmail }
                phoneNumber = user.phone ?: ""
                businessAddress = user.address ?: ""
                addressVerified = true
                avatarUrl = user.avatarUrl
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(businessAddress, showAddressSuggestions) {
        if (!showAddressSuggestions || businessAddress.trim().length < 3) {
            addressSuggestions = emptyList()
        } else {
            delay(400)
            addressSuggestions = searchAddressSuggestions(context, businessAddress)
        }
    }

    // 验证函数
    fun validateName(): Boolean {
        nameError = when {
            businessName.isBlank() -> "Business name is required"
            businessName.length < 2 -> "Name must be at least 2 characters"
            businessName.length > 50 -> "Name must be less than 50 characters"
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
            businessAddress.isBlank() -> null
            businessAddress.length < 5 -> "Please enter a more complete address"
            !addressVerified -> "Select a real address from the search results"
            else -> null
        }
        return addressError == null
    }

    fun validateDescription(): Boolean {
        descriptionError = when {
            briefDescription.isBlank() -> null
            briefDescription.length > 200 -> "Description must be less than 200 characters"
            else -> null
        }
        return descriptionError == null
    }

    fun validateAll(): Boolean {
        return listOf(
            validateName(),
            validateEmail(),
            validatePhone(),
            validateAddress(),
            validateDescription()
        ).all { it }
    }

    val canSave = businessName.isNotBlank() && email.isNotBlank() && !isSaving

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
                displayName = businessName,
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
                text = businessName.ifBlank { "Business Name" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = briefDescription.ifBlank { "Local Artisan Breads & Pastries" },
                fontSize = 13.sp,
                color = TextGrey,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider(
            color = Color(0xFFDDC0B8).copy(alpha = 0.3f),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

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
            value = businessName,
            onValueChange = {
                businessName = it
                if (nameError != null) validateName()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Business Name", color = TextGrey) },
            placeholder = { Text("Sunset Bakery", color = TextGrey) },
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

        Column {
            OutlinedTextField(
                value = businessAddress,
                onValueChange = {
                    businessAddress = it
                    addressVerified = false
                    showAddressSuggestions = true
                    if (addressError != null) validateAddress()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Business Address", color = TextGrey) },
                placeholder = { Text("Search your business address", color = TextGrey) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = addressError != null,
                supportingText = {
                    when {
                        addressError != null -> Text(addressError!!, color = RedCancelled)
                        !addressVerified && businessAddress.isNotBlank() -> Text(
                            "Select a real address from the search results",
                            color = OrangeAccent
                        )
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
                                        businessAddress = suggestion
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
            addressLocationMessage?.let {
                Text(
                    text = it,
                    color = if (it.contains("detected")) GreenConfirmed else OrangeAccent,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = briefDescription,
            onValueChange = {
                if (it.length <= 200) briefDescription = it
                if (descriptionError != null) validateDescription()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            label = { Text("Brief Description", color = TextGrey) },
            placeholder = {
                Text(
                    "We bake fresh sourdough and pastries daily, committed to sharing our surplus with the community.",
                    color = TextGrey
                )
            },
            shape = RoundedCornerShape(12.dp),
            maxLines = 4,
            isError = descriptionError != null,
            supportingText = {
                if (descriptionError != null) {
                    Text(descriptionError!!, color = RedCancelled)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${briefDescription.length} / 200",
                fontSize = 12.sp,
                color = if (briefDescription.length > 180) OrangeAccent else TextGrey
            )
        }

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
                            name = businessName,
                            email = email,
                            phone = phoneNumber,
                            address = businessAddress,
                            avatarUrl = savedAvatarUrl
                        )
                        CurrentUser.user?.let { user ->
                            user.name = businessName
                            user.email = email
                            user.phone = phoneNumber
                            user.address = businessAddress
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
