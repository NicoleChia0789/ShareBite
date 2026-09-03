package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.DialogProperties
import com.sharebite.app.data.session.CurrentUser

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun PostRequestScreen(
    paddingValues: PaddingValues,
    onPostSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { CharityRepository() }
    val coroutineScope = rememberCoroutineScope()
    val organisationName = CurrentUser.getUserName()

    // 表单状态
    var foodName by rememberSaveable { mutableStateOf("") }
    var selectedCategories by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var quantity by rememberSaveable { mutableStateOf("") }
    var pickupDate by rememberSaveable { mutableStateOf("") }
    var pickupTime by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 错误弹窗状态
    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    fun toggleCategory(category: String) {
        selectedCategories = if (selectedCategories.contains(category)) {
            selectedCategories.filter { it != category }
        } else {
            selectedCategories + category
        }
    }

    // 验证表单，返回错误信息列表
    fun validateForm(): List<String> {
        val errors = mutableListOf<String>()
        if (foodName.isBlank()) errors.add("Food Item or Type")
        if (selectedCategories.isEmpty()) errors.add("Category (select at least one)")
        if (!isQuantityValid(quantity)) {
            errors.add("Quantity must be between 1 and ${formatWithThousandSeparator(MAX_QUANTITY)}")
        }
        if (pickupDate.isBlank()) errors.add("Preferred collection date")
        if (pickupTime.isBlank()) errors.add("Collection time")
        if (pickupDate.isNotBlank() && pickupTime.isNotBlank() && isDateTimeInPast(pickupDate, pickupTime)) {
            errors.add("Collection date/time cannot be in the past")
        }
        return errors
    }

    fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return format.format(calendar.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        Text(
            "Tell the community what your organisation needs...",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Request Details",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ----- Food Item or Type -----
        OutlinedTextField(
            value = foodName,
            onValueChange = { foodName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Food Item or Type") },
            placeholder = { Text("e.g. Fresh Garden Vegetables") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = TextGrey,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent,
                unfocusedLabelColor = TextGrey
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ----- Category -----
        Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextGrey)
        Spacer(modifier = Modifier.height(6.dp))

        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            foodCategoryOptions.forEach { category ->
                FilterChip(
                    selected = selectedCategories.contains(category),
                    onClick = { toggleCategory(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = BgCream,
                        disabledLabelColor = TextGrey
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ----- Quantity -----
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = filterQuantityInput(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Quantity requested") },
            placeholder = { Text("e.g. 50") },
            supportingText = {
                if (quantity.isNotBlank()) {
                    Text("= ${formatWithThousandSeparator(quantity)} units")
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = TextGrey,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent,
                unfocusedLabelColor = TextGrey
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ----- Preferred collection date -----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            OutlinedTextField(
                value = pickupDate,
                onValueChange = { /* 只读 */ },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Preferred collection date") },
                placeholder = { Text("mm/dd/yyyy") },
                shape = RoundedCornerShape(12.dp),
                enabled = false,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select date",
                        tint = TextGrey
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = TextGrey,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedLabelColor = OrangeAccent,
                    unfocusedLabelColor = TextGrey,
                    disabledTextColor = TextDark,
                    disabledBorderColor = TextGrey,
                    disabledLabelColor = TextGrey
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ----- Collection time -----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true }
        ) {
            OutlinedTextField(
                value = pickupTime,
                onValueChange = { /* 只读 */ },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Collection time") },
                placeholder = { Text("--:-- --") },
                shape = RoundedCornerShape(12.dp),
                enabled = false,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select time",
                        tint = TextGrey
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = TextGrey,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedLabelColor = OrangeAccent,
                    unfocusedLabelColor = TextGrey,
                    disabledTextColor = TextDark,
                    disabledBorderColor = TextGrey,
                    disabledLabelColor = TextGrey
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ----- Add a note -----
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            label = { Text("Add a note (Optional)") },
            placeholder = { Text("Any specific dietary requirements or delivery instructions?") },
            shape = RoundedCornerShape(12.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = TextGrey,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedLabelColor = OrangeAccent,
                unfocusedLabelColor = TextGrey
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ----- Post Request Button -----
        Button(
            onClick = {
                val errors = validateForm()
                if (errors.isNotEmpty()) {
                    errorMessage = "Please fill in all required fields:\n• ${errors.joinToString("\n• ")}"
                    showErrorDialog = true
                    return@Button
                }

                isSubmitting = true
                coroutineScope.launch {
                    try {
                        repository.createMyRequest(
                            foodName = foodName,
                            category = selectedCategories.joinToString(","),
                            quantity = quantityValue(quantity) ?: 0,
                            pickupDate = pickupDate,
                            pickupTime = formatTimeForDb(pickupTime),  // ← 加上转换
                            organisationName = organisationName,
                            peopleServed = 30,
                            note = note
                        )
                        isSubmitting = false
                        onPostSuccess()
                    } catch (e: Exception) {
                        isSubmitting = false
                        snackbarMessage = e.message ?: "Failed to create request"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    "Post Request",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardCream)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    "\"Your requests help us direct surplus where it matters most.\"",
                    fontSize = 14.sp,
                    color = TextGrey,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // ----- Date Picker Dialog -----
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
            selectableDates = FutureOnlySelectableDates
        )

        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .padding(vertical = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        "Select Date",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = TextGrey)
                        }
                        Button(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = java.util.Date(millis)
                                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    pickupDate = format.format(date)
                                }
                                showDatePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text("OK", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // ----- Time Picker Dialog -----
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 17,
            initialMinute = 0,
            is24Hour = false
        )
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select Time", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel", color = TextGrey)
                        }
                        Button(
                            onClick = {
                                val hour = timePickerState.hour
                                val minute = timePickerState.minute
                                val amPm = if (hour < 12) "AM" else "PM"
                                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                                pickupTime = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
                                showTimePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text("OK", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // ----- 错误提示弹窗（AlertDialog）-----
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Incomplete Form", color = RedCancelled, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    errorMessage,
                    color = TextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ----- Snackbar 错误提示（用于提交失败的情况）-----
    if (snackbarMessage != null) {
        Snackbar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            action = {
                TextButton(onClick = { snackbarMessage = null }) {
                    Text("Dismiss", color = Color.White)
                }
            },
            containerColor = RedCancelled,
            contentColor = Color.White
        ) {
            Text(snackbarMessage!!)
        }
    }
}
