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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.UserAddress
import com.sharebite.app.data.repository.AddressRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun UserEditAddressScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onSave: (String, String, String, String, String, String, String) -> Unit,
    existingAddress: UserAddress? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val addressRepo = remember { AddressRepository() }

    var label by rememberSaveable { mutableStateOf(existingAddress?.label ?: "") }
    var street by rememberSaveable { mutableStateOf(existingAddress?.street ?: "") }
    var city by rememberSaveable { mutableStateOf(existingAddress?.city ?: "") }
    var state by rememberSaveable { mutableStateOf(existingAddress?.state ?: "") }
    var zip by rememberSaveable { mutableStateOf(existingAddress?.zip ?: "") }
    var country by rememberSaveable { mutableStateOf(existingAddress?.country ?: "Malaysia") }
    var phone by rememberSaveable { mutableStateOf(existingAddress?.phone ?: "") }
    var isDefault by remember { mutableStateOf(existingAddress?.isDefault ?: false) }
    var isSaving by remember { mutableStateOf(false) }

    // Validation states
    var labelError by remember { mutableStateOf<String?>(null) }
    var streetError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var stateError by remember { mutableStateOf<String?>(null) }
    var zipError by remember { mutableStateOf<String?>(null) }
    var countryError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val isEditMode = existingAddress != null

    // Malaysia state list
    val malaysiaStates = listOf(
        "Johor", "Kedah", "Kelantan", "Melaka", "Negeri Sembilan",
        "Pahang", "Perak", "Perlis", "Pulau Pinang", "Sabah",
        "Sarawak", "Selangor", "Terengganu", "Kuala Lumpur",
        "Labuan", "Putrajaya"
    )

    fun validateLabel(): Boolean {
        labelError = when {
            label.isBlank() -> "Label is required"
            label.length < 2 -> "Label must be at least 2 characters"
            else -> null
        }
        return labelError == null
    }

    fun validateStreet(): Boolean {
        streetError = when {
            street.isBlank() -> "Street address is required"
            street.length < 3 -> "Street address must be at least 3 characters"
            else -> null
        }
        return streetError == null
    }

    fun validateCity(): Boolean {
        cityError = when {
            city.isBlank() -> "City is required"
            city.length < 2 -> "City must be at least 2 characters"
            else -> null
        }
        return cityError == null
    }

    fun validateState(): Boolean {
        stateError = when {
            state.isBlank() -> "State is required"
            !malaysiaStates.contains(state) -> "Please enter a valid Malaysian state"
            else -> null
        }
        return stateError == null
    }

    fun validateZip(): Boolean {
        zipError = when {
            zip.isBlank() -> "Postcode is required"
            !zip.matches(Regex("^[0-9]{5}$")) -> "Enter a valid 5-digit Malaysian postcode"
            else -> null
        }
        return zipError == null
    }

    fun validateCountry(): Boolean {
        countryError = when {
            country.isBlank() -> "Country is required"
            !country.equals("Malaysia", ignoreCase = true) -> "Country should be Malaysia"
            else -> null
        }
        return countryError == null
    }

    fun validatePhone(): Boolean {
        phoneError = when {
            phone.isBlank() -> "Phone number is required"
            !phone.matches(Regex("^(\\+?6?01)[0-46-9]-*[0-9]{7,8}$")) ->
                "Enter a valid Malaysian phone number (e.g., 012-3456789)"
            else -> null
        }
        return phoneError == null
    }

    fun validateAll(): Boolean {
        return listOf(
            validateLabel(),
            validateStreet(),
            validateCity(),
            validateState(),
            validateZip(),
            validateCountry(),
            validatePhone()
        ).all { it }
    }

    fun handleSave() {
        if (!validateAll()) {
            Toast.makeText(context, "Please fix all errors", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isSaving = true
            try {
                if (isEditMode) {
                    val success = addressRepo.updateAddress(
                        addressId = existingAddress!!.id,
                        label = label,
                        street = street,
                        city = city,
                        state = state,
                        zip = zip,
                        country = country,
                        phone = phone,
                        isDefault = isDefault
                    )
                    if (success) {
                        Toast.makeText(context, "Address updated!", Toast.LENGTH_SHORT).show()
                        onSave(label, street, city, state, zip, country, phone)
                    } else {
                        Toast.makeText(context, "Failed to update address", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val newId = addressRepo.addAddress(
                        label = label,
                        street = street,
                        city = city,
                        state = state,
                        zip = zip,
                        country = country,
                        phone = phone,
                        isDefault = isDefault
                    )
                    if (newId > 0) {
                        Toast.makeText(context, "Address added!", Toast.LENGTH_SHORT).show()
                        onSave(label, street, city, state, zip, country, phone)
                    } else {
                        Toast.makeText(context, "Failed to add address", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            isSaving = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
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
                text = if (isEditMode) "Edit Address" else "Add Address",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Label ──
        OutlinedTextField(
            value = label,
            onValueChange = {
                label = it
                if (labelError != null) validateLabel()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Label (e.g. Home, Work)", color = TextGrey) },
            placeholder = { Text("Home", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = labelError != null,
            supportingText = {
                if (labelError != null) {
                    Text(labelError!!, color = RedCancelled)
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

        // ── Street Address ──
        OutlinedTextField(
            value = street,
            onValueChange = {
                street = it
                if (streetError != null) validateStreet()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Street Address", color = TextGrey) },
            placeholder = { Text("123 Jalan Ampang", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = streetError != null,
            supportingText = {
                if (streetError != null) {
                    Text(streetError!!, color = RedCancelled)
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

        // ── City / State / Postcode ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = {
                    city = it
                    if (cityError != null) validateCity()
                },
                modifier = Modifier.weight(2f),
                label = { Text("City", color = TextGrey) },
                placeholder = { Text("Kuala Lumpur", color = TextGrey) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = cityError != null,
                supportingText = {
                    if (cityError != null) {
                        Text(cityError!!, color = RedCancelled, fontSize = 10.sp)
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
            OutlinedTextField(
                value = state,
                onValueChange = {
                    state = it
                    if (stateError != null) validateState()
                },
                modifier = Modifier.weight(1.2f),
                label = { Text("State", color = TextGrey) },
                placeholder = { Text("Selangor", color = TextGrey) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = stateError != null,
                supportingText = {
                    if (stateError != null) {
                        Text(stateError!!, color = RedCancelled, fontSize = 10.sp)
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
            OutlinedTextField(
                value = zip,
                onValueChange = {
                    zip = it
                    if (zipError != null) validateZip()
                },
                modifier = Modifier.weight(0.8f),
                label = { Text("Postcode", color = TextGrey) },
                placeholder = { Text("50000", color = TextGrey) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = zipError != null,
                supportingText = {
                    if (zipError != null) {
                        Text(zipError!!, color = RedCancelled, fontSize = 10.sp)
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Country ──
        OutlinedTextField(
            value = country,
            onValueChange = {
                country = it
                if (countryError != null) validateCountry()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Country", color = TextGrey) },
            placeholder = { Text("Malaysia", color = TextGrey) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = countryError != null,
            supportingText = {
                if (countryError != null) {
                    Text(countryError!!, color = RedCancelled)
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

        // ── Phone Number ──
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
            onClick = { handleSave() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isSaving) OrangeAccent else Color.Gray,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = if (isEditMode) "Update Address" else "Add Address",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}