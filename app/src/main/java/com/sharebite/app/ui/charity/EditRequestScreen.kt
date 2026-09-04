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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.SelectableDates
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRequestScreen(
    requestId: Long,
    paddingValues: PaddingValues,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val repository = remember { CharityRepository() }
    val listingRepository = remember { FoodListingRepository() }
    val coroutineScope = rememberCoroutineScope()

    var original by remember { mutableStateOf<CharityRequest?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    var pickupDate by rememberSaveable { mutableStateOf("") }
    var pickupTime by rememberSaveable { mutableStateOf("") }
    var peopleServed by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var merchantDeadline by rememberSaveable { mutableStateOf("") }

    var hasInitializedForm by rememberSaveable(requestId) { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var timeError by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(requestId) {
        try {
            val data = repository.getMyRequestById(requestId)
            original = data
            data?.listingId?.toLongOrNull()?.let { listingId ->
                merchantDeadline = listingRepository.getListingById(listingId).deadline
            }
            if (!hasInitializedForm) {
                pickupDate = data?.pickupDate ?: ""
                pickupTime = data?.pickupTime ?: ""
                peopleServed = data?.peopleServed?.toString()?.let(::filterQuantityInput) ?: ""
                note = data?.note ?: ""
                hasInitializedForm = true
            }
        } catch (e: Exception) {
            loadError = e.message
        } finally {
            isLoading = false
        }
    }

    fun validateForm(): List<String> {
        val errors = mutableListOf<String>()
        if (!isQuantityValid(peopleServed)) errors.add("People served must be between 1 and ${formatWithThousandSeparator(MAX_QUANTITY)}")
        if (pickupDate.isBlank()) errors.add("Collection date")
        if (pickupTime.isBlank()) errors.add("Collection time")
        if (pickupDate.isNotBlank() && pickupTime.isNotBlank() && isDateTimeInPast(pickupDate, pickupTime)) {
            errors.add("Collection date/time cannot be in the past")
        }
        if (pickupDate.isNotBlank() && pickupTime.isNotBlank() && merchantDeadline.isNotBlank() &&
            !isDateTimeWithinDeadline(pickupDate, pickupTime, merchantDeadline)
        ) {
            errors.add("Collection date/time must be on or before the merchant deadline")
        }
        return errors
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            loadError != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $loadError", color = RedCancelled)
                }
            }
            original == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Request not found.", color = TextGrey)
                }
            }
            original?.let { it.status != "Pending" && !(it.status == "Confirmed" && it.listingId.isNullOrBlank()) } == true -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "This request cannot be edited.",
                        color = TextGrey,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            else -> {
                val data = original!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 40.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // ---------- WARNING BANNER ----------
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Pending requests and accepted standalone requests can be edited",
                                fontSize = 13.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(data.foodName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(data.organisationName, fontSize = 13.sp, color = TextGrey)

                    Spacer(modifier = Modifier.height(16.dp))

                    // ----- Collection date -----
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = pickupDate,
                            onValueChange = { },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Collection date") },
                            shape = RoundedCornerShape(12.dp),
                            enabled = false,
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select date", tint = TextGrey)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
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
                            onValueChange = { },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Collection time") },
                            shape = RoundedCornerShape(12.dp),
                            enabled = false,
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select time", tint = TextGrey)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextDark,
                                disabledBorderColor = TextGrey,
                                disabledLabelColor = TextGrey
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- People served -----
                    OutlinedTextField(
                        value = peopleServed,
                        onValueChange = { peopleServed = filterQuantityInput(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("People served") },
                        isError = peopleServed.isNotBlank() && !isQuantityValid(peopleServed),
                        supportingText = {
                            if (peopleServed.isNotBlank() && !isQuantityValid(peopleServed)) {
                                Text("Enter a quantity from 1 to ${formatWithThousandSeparator(MAX_QUANTITY)}", color = RedCancelled)
                            } else if (peopleServed.isNotBlank()) {
                                Text("= ${formatWithThousandSeparator(peopleServed)} units")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = TextGrey,
                            focusedLabelColor = OrangeAccent,
                            unfocusedLabelColor = TextGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- Note -----
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        label = { Text("Additional note") },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = TextGrey,
                            focusedLabelColor = OrangeAccent,
                            unfocusedLabelColor = TextGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ----- Save changes -----
                    Button(
                        onClick = {
                            val errors = validateForm()
                            if (errors.isNotEmpty()) {
                                errorMessage = "Please check:\n• ${errors.joinToString("\n• ")}"
                                showErrorDialog = true
                                return@Button
                            }
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    repository.updateMyRequest(
                                        requestId = requestId,
                                        quantity = quantityValue(peopleServed) ?: 0,
                                        pickupDate = pickupDate,
                                        pickupTime = formatTimeForDb(pickupTime),  // ← 加上转换
                                        peopleServed = quantityValue(peopleServed) ?: 0,
                                        note = note.ifBlank { null }
                                    )
                                    isSaving = false
                                    onSaveSuccess()
                                } catch (e: Exception) {
                                    isSaving = false
                                    errorMessage = e.message ?: "Failed to save changes"
                                    showErrorDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving && isQuantityValid(peopleServed) &&
                            pickupDate.isNotBlank() && pickupTime.isNotBlank() &&
                            timeError.isBlank()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Save changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ----- Cancel -----
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // ----- Date Picker -----
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseEditDateMillis(pickupDate) ?: System.currentTimeMillis(),
            selectableDates = if (merchantDeadline.isBlank()) FutureOnlySelectableDates
            else DeadlineSelectableDates(merchantDeadline)
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
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = TextGrey) }
                        Button(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = Date(millis)
                                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    pickupDate = format.format(date)
                                    timeError = ""
                                }
                                showDatePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) { Text("OK", color = Color.White) }
                    }
                }
            }
        }
    }

    // ----- Time Picker -----
    if (showTimePicker) {
        val (initialHour, initialMinute) = parseEditTime(pickupTime)
        val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
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

                    if (timeError.isNotBlank()) {
                        Text(
                            text = timeError,
                            color = RedCancelled,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = TextGrey) }
                        Button(
                            onClick = {
                                val hour = timePickerState.hour
                                val minute = timePickerState.minute
                                val amPm = if (hour < 12) "AM" else "PM"
                                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                                val candidate = String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
                                if (pickupDate.isNotBlank() && merchantDeadline.isNotBlank() &&
                                    !isDateTimeWithinDeadline(pickupDate, candidate, merchantDeadline)
                                ) {
                                    timeError = "Selected time must be on or before the merchant deadline"
                                } else {
                                    pickupTime = candidate
                                    timeError = ""
                                    showTimePicker = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) { Text("OK", color = Color.White) }
                    }
                }
            }
        }
    }

    // ----- 错误提示弹窗 -----
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Cannot Save", color = RedCancelled, fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage, color = TextDark) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) { Text("OK", color = Color.White) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun parseEditDateMillis(value: String): Long? = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value)?.time
}.getOrNull()

private fun parseEditTime(value: String): Pair<Int, Int> = runCatching {
    val parsed = SimpleDateFormat("h:mm a", Locale.US).apply { isLenient = false }.parse(value)
        ?: return@runCatching 17 to 0
    val calendar = Calendar.getInstance().apply { time = parsed }
    calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)
}.getOrDefault(17 to 0)
