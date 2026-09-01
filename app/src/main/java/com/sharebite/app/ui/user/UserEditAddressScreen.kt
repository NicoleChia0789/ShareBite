package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*

@Composable
fun UserEditAddressScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onSave: (String, String, String, String, String, String, String) -> Unit,
    existingAddress: Address? = null
) {
    var label by remember { mutableStateOf(existingAddress?.label ?: "") }
    var street by remember { mutableStateOf(existingAddress?.street ?: "") }
    var city by remember { mutableStateOf(existingAddress?.city ?: "") }
    var state by remember { mutableStateOf(existingAddress?.state ?: "") }
    var zip by remember { mutableStateOf(existingAddress?.zip ?: "") }
    var country by remember { mutableStateOf(existingAddress?.country ?: "United States") }
    var phone by remember { mutableStateOf(existingAddress?.phone ?: "") }
    var isDefault by remember { mutableStateOf(existingAddress?.isDefault ?: false) }

    val isEditMode = existingAddress != null
    val canSave = label.isNotBlank() && street.isNotBlank() && city.isNotBlank() &&
            state.isNotBlank() && zip.isNotBlank() && phone.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {


        Spacer(modifier = Modifier.height(20.dp))

        // ── Label ──
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Label (e.g. Home, Work)", color = TextGrey) },
            placeholder = { Text("Home", color = TextGrey) },
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

        // ── Street Address ──
        OutlinedTextField(
            value = street,
            onValueChange = { street = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Street Address", color = TextGrey) },
            placeholder = { Text("123 Maple Street", color = TextGrey) },
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

        // ── City / State / Zip ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                modifier = Modifier.weight(2f),
                label = { Text("City", color = TextGrey) },
                placeholder = { Text("Brooklyn", color = TextGrey) },
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
            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                modifier = Modifier.weight(1f),
                label = { Text("State", color = TextGrey) },
                placeholder = { Text("NY", color = TextGrey) },
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
            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it },
                modifier = Modifier.weight(1f),
                label = { Text("Zip", color = TextGrey) },
                placeholder = { Text("11201", color = TextGrey) },
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Country ──
        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Country", color = TextGrey) },
            placeholder = { Text("United States", color = TextGrey) },
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

        Spacer(modifier = Modifier.height(12.dp))

        // ── Set as Default Checkbox ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isDefault,
                onCheckedChange = { isDefault = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = OrangeAccent,
                    uncheckedColor = Color(0xFFDDC0B8)
                )
            )
            Text(
                text = "Set as default address",
                fontSize = 14.sp,
                color = TextDark
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Save Button ──
        Button(
            onClick = {
                onSave(label, street, city, state, zip, country, phone)
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
            Text(
                text = if (isEditMode) "Update Address" else "Add Address",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}