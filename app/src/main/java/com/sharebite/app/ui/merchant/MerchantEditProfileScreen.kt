package com.sharebite.app.ui.merchant

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.repository.UserRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
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

    val currentName = CurrentUser.getUserName()
    val currentEmail = CurrentUser.getUserEmail()

    var businessName by remember { mutableStateOf(currentName) }
    var category by remember { mutableStateOf("") }
    var businessAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var briefDescription by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentEmail) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load user data
    LaunchedEffect(Unit) {
        try {
            val user = userRepo.getUser(userId)
            if (user != null) {
                businessName = user.name.ifBlank { currentName }
                email = user.email.ifBlank { currentEmail }
                phoneNumber = user.phone ?: ""
                businessAddress = user.address ?: ""
                // If you have a category or description field in your user model
                // category = user.category ?: ""
                // briefDescription = user.description ?: ""
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val canSave = businessName.isNotBlank() && email.isNotBlank()

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
        Spacer(modifier = Modifier.height(8.dp))

        // ── Back Button + Title ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark
                )
            }
            Text(
                text = "Edit Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Avatar / Logo ──
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
                    text = if (businessName.isNotEmpty()) businessName.take(1).uppercase() else "?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // Camera overlay
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
                            contentDescription = "Change Logo",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Business Name + Tagline ──
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

        // ── Divider ──
        Divider(
            color = Color(0xFFDDC0B8).copy(alpha = 0.3f),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Error Message ──
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

        // ── Business Name ──
        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Business Name", color = TextGrey) },
            placeholder = { Text("Sunset Bakery", color = TextGrey) },
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

        // ── Category ──
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Category", color = TextGrey) },
            placeholder = { Text("Bakery & Cafe", color = TextGrey) },
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

        // ── Business Address ──
        OutlinedTextField(
            value = businessAddress,
            onValueChange = { businessAddress = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Business Address", color = TextGrey) },
            placeholder = { Text("123 Doughnut Lane, Breadville", color = TextGrey) },
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
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number", color = TextGrey) },
            placeholder = { Text("(555) 123-4567", color = TextGrey) },
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

        // ── Brief Description ──
        OutlinedTextField(
            value = briefDescription,
            onValueChange = {
                if (it.length <= 200) briefDescription = it
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFFDDC0B8),
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent
            )
        )

        // Character counter
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

        // ── Save Changes Button ──
        Button(
            onClick = {
                if (!canSave) return@Button
                isSaving = true
                scope.launch {
                    try {
                        userRepo.updateUser(
                            userId = userId,
                            name = businessName,
                            email = email,
                            phone = phoneNumber,
                            address = businessAddress
                        )
                        // Update CurrentUser
                        CurrentUser.user?.let { user ->
                            user.name = businessName
                            user.email = email
                            user.phone = phoneNumber
                            user.address = businessAddress
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