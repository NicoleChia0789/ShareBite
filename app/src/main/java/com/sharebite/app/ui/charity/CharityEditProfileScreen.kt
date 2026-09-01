package com.sharebite.app.ui.charity

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
fun CharityEditProfileScreen(
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

    var organisationName by remember { mutableStateOf(currentName) }
    var phoneNumber by remember { mutableStateOf("") }
    var organisationAddress by remember { mutableStateOf("") }
    var missionStatement by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentEmail) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load user data
    LaunchedEffect(Unit) {
        try {
            val user = userRepo.getUser(userId)
            if (user != null) {
                organisationName = user.name.ifBlank { currentName }
                email = user.email.ifBlank { currentEmail }
                phoneNumber = user.phone ?: ""
                organisationAddress = user.address ?: ""
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val canSave = organisationName.isNotBlank() && email.isNotBlank()

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
                    text = if (organisationName.isNotEmpty()) organisationName.take(1).uppercase() else "?",
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

        Text(
            text = "Tap to update logo",
            fontSize = 12.sp,
            color = TextGrey,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

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

        // ── Organization Name ──
        OutlinedTextField(
            value = organisationName,
            onValueChange = { organisationName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Organization Name", color = TextGrey) },
            placeholder = { Text("Hopeful Harvest Food Bank", color = TextGrey) },
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

        Spacer(modifier = Modifier.height(12.dp))

        // ── Organization Address ──
        OutlinedTextField(
            value = organisationAddress,
            onValueChange = { organisationAddress = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Organization Address", color = TextGrey) },
            placeholder = { Text("123 Charity Lane, City, State, ZIP", color = TextGrey) },
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

        // ── Mission Statement ──
        OutlinedTextField(
            value = missionStatement,
            onValueChange = {
                if (it.length <= 300) missionStatement = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            label = { Text("Mission Statement", color = TextGrey) },
            placeholder = {
                Text(
                    "Nourishing our community by rescuing surplus food...",
                    color = TextGrey
                )
            },
            shape = RoundedCornerShape(12.dp),
            maxLines = 5,
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
                text = "${missionStatement.length} / 300",
                fontSize = 12.sp,
                color = if (missionStatement.length > 280) OrangeAccent else TextGrey
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Primary Contact Email ──
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Primary Contact Email", color = TextGrey) },
            placeholder = { Text("hello@hopefulharvest.org", color = TextGrey) },
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

        // ── Save Button ──
        Button(
            onClick = {
                if (!canSave) return@Button
                isSaving = true
                scope.launch {
                    try {
                        userRepo.updateUser(
                            userId = userId,
                            name = organisationName,
                            email = email,
                            phone = phoneNumber,
                            address = organisationAddress
                        )
                        // Update CurrentUser
                        CurrentUser.user?.let { user ->
                            user.name = organisationName
                            user.email = email
                            user.phone = phoneNumber
                            user.address = organisationAddress
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

        Spacer(modifier = Modifier.height(12.dp))

        // ── Cancel Button ──
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