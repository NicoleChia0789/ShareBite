package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import com.sharebite.app.util.formatDateTimeForDisplay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ApplyForFoodScreen(
    listingId: Long,
    paddingValues: PaddingValues,
    onSubmitSuccess: () -> Unit
) {
    val listingRepository = remember { FoodListingRepository() }
    val charityRepository = remember { CharityRepository() }
    val coroutineScope = rememberCoroutineScope()

    // ✅ Get current user's name (auto-filled, read-only)
    val organisationName = CurrentUser.getUserName()

    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var isLoadingListing by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    var peopleServed by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTime by rememberSaveable { mutableStateOf<String?>(null) }

    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val dateList = remember {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displayFormat = SimpleDateFormat("EEE d", Locale.US)
        buildList {
            repeat(7) {
                add(Triple(dateFormat.format(calendar.time), displayFormat.format(calendar.time), displayFormat.format(calendar.time).split(" ").first()))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }
    val allTimeSlots = remember(listing?.pickupTime) { buildCharityPickupSlots(listing?.pickupTime) }
    val deadline = remember(listing?.deadline) { parseCharityDeadline(listing?.deadline.orEmpty()) }
    val availableDates = remember(dateList, deadline) {
        dateList.filter { deadline == null || parseCharityDate(it.first)?.let { date -> !date.after(deadline) } == true }
    }
    val availableTimeSlots = remember(selectedDate, deadline) {
        allTimeSlots.filter { time ->
            val dateTime = parseCharityDateTime(selectedDate, time)
            dateTime != null && (deadline == null || !dateTime.after(deadline))
        }
    }

    LaunchedEffect(listingId, Unit) {
        isLoadingListing = true
        try {
            listing = listingRepository.getListingById(listingId)
            loadError = null
        } catch (e: Exception) {
            loadError = e.message
        } finally {
            isLoadingListing = false
        }
    }

    LaunchedEffect(availableDates, availableTimeSlots) {
        if (selectedDate == null || availableDates.none { it.first == selectedDate }) selectedDate = availableDates.firstOrNull()?.first
        if (selectedTime == null || selectedTime !in availableTimeSlots) selectedTime = availableTimeSlots.firstOrNull()
    }

    fun validateForm(): List<String> {
        val errors = mutableListOf<String>()
        if (organisationName.isBlank()) errors.add("Organisation name")
        if (!isQuantityValid(peopleServed)) errors.add("People served must be between 1 and ${formatWithThousandSeparator(MAX_QUANTITY)}")
        if (selectedDate == null) errors.add("Collection date")
        if (selectedTime == null || selectedTime !in availableTimeSlots) errors.add("Collection time must be before the merchant deadline")
        val requestedUnits = quantityValue(peopleServed) ?: 0
        val remainingUnits = listing?.remainingQuantity ?: 0
        if (requestedUnits > remainingUnits) {
            errors.add("People served cannot be more than ${formatWithThousandSeparator(remainingUnits.toString())} available units")
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
            isLoadingListing -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            loadError != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Request Again unavailable", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (loadError.orEmpty().contains("deadline", true))
                            "This food listing has expired."
                        else
                            "This food listing is no longer available or has no quantity remaining.",
                        color = RedCancelled,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            listing == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Listing not found.", color = TextGrey)
                }
            }
            else -> {
                val data = listing!!
                val requestedUnits = quantityValue(peopleServed) ?: 0
                val exceedsRemaining = requestedUnits > data.remainingQuantity
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 40.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (data.remainingQuantity <= 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Request Again unavailable: no quantity remaining.",
                                modifier = Modifier.padding(14.dp),
                                color = RedCancelled,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ---------- APPLYING FROM BANNER ----------
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardCream)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Applying from", fontSize = 12.sp, color = TextGrey)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${data.foodName} · ${data.merchantName}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ---------- FIXED DETAILS (商家定的，不可改) ----------
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Pickup schedule", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Spacer(modifier = Modifier.height(10.dp))
                            FixedDetailRow(label = "Available quantity", value = "${data.remainingQuantity} ${data.unit ?: "units"}")
                            Text("Select collection date", fontSize = 12.sp, color = TextGrey)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(dateList) { (date, display, day) ->
                                    val available = date in availableDates.map { it.first }
                                    val selected = date == selectedDate
                                    Card(
                                        modifier = Modifier.width(68.dp).clickable(enabled = available) { selectedDate = date },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = when {
                                            selected -> OrangeAccent
                                            available -> Color.White
                                            else -> Color(0xFFE9E0DB)
                                        })
                                    ) {
                                        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(day, fontSize = 11.sp, color = if (selected) Color.White else TextGrey)
                                            Text(display.split(" ").last(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else TextDark)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Select pickup time", fontSize = 12.sp, color = TextGrey)
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                allTimeSlots.chunked(2).forEach { row ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        row.forEach { time ->
                                            val available = time in availableTimeSlots
                                            val selected = time == selectedTime
                                            Card(
                                                modifier = Modifier.weight(1f).clickable(enabled = available) { selectedTime = time },
                                                shape = RoundedCornerShape(50),
                                                colors = CardDefaults.cardColors(containerColor = when {
                                                    selected -> OrangeAccent
                                                    available -> Color.White
                                                    else -> Color(0xFFE9E0DB)
                                                })
                                            ) {
                                                Text(time, Modifier.fillMaxWidth().padding(10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = if (selected) Color.White else if (available) TextDark else TextGrey)
                                            }
                                        }
                                        if (row.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Your Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Auto-filled from your account.",
                        fontSize = 11.sp,
                        color = TextGrey
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ Organisation name - READ ONLY (auto-filled from CurrentUser)
                    OutlinedTextField(
                        value = organisationName,
                        onValueChange = { /* Read-only */ },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Organisation name") },
                        placeholder = { Text("e.g. Hope Charity Home") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = false, // ✅ Read-only
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = TextGrey,
                            focusedLabelColor = OrangeAccent,
                            unfocusedLabelColor = TextGrey,
                            disabledTextColor = TextDark,
                            disabledBorderColor = TextGrey,
                            disabledLabelColor = TextGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- People served -----
                    OutlinedTextField(
                        value = peopleServed,
                        onValueChange = { peopleServed = filterQuantityInput(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("People served") },
                        placeholder = { Text("e.g. 30") },
                        supportingText = {
                            if (peopleServed.isNotBlank()) {
                                Text("= ${formatWithThousandSeparator(peopleServed)} units")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = peopleServed.isNotBlank() && !isQuantityValid(peopleServed),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = AppTextFieldDefaults.colors()
                    )
                    if (peopleServed.isNotBlank() && !isQuantityValid(peopleServed)) {
                        Text(
                            "People served must be at least 1 and no more than 100,000.",
                            fontSize = 12.sp,
                            color = RedCancelled
                        )
                    }
                    if (exceedsRemaining) {
                        Text(
                            text = "Only ${formatWithThousandSeparator(data.remainingQuantity.toString())} ${data.unit ?: "units"} available.",
                            fontSize = 12.sp,
                            color = RedCancelled
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- Additional note -----
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        label = { Text("Additional note (Optional)") },
                        placeholder = { Text("Dietary restrictions, or how you'll use this donation...") },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        colors = AppTextFieldDefaults.colors()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ----- Submit button -----
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
                                    charityRepository.createMyRequestFromListing(
                                        merchantId = data.merchantId,
                                        listingId = data.id.toString(),
                                        foodName = data.foodName,
                                        category = data.category,
                                        quantity = quantityValue(peopleServed) ?: 0,
                                        pickupDate = selectedDate!!,
                                        pickupTime = formatTimeForDb(selectedTime!!),
                                        organisationName = organisationName,
                                        peopleServed = quantityValue(peopleServed) ?: 0,
                                        note = note.ifBlank { null }
                                    )
                                    isSubmitting = false
                                    onSubmitSuccess()
                                } catch (e: Exception) {
                                    isSubmitting = false
                                    errorMessage = e.message ?: "Failed to submit request"
                                    showErrorDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting && !exceedsRemaining && isQuantityValid(peopleServed) && selectedDate != null && selectedTime in availableTimeSlots
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Submit request", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // ----- 错误提示弹窗 -----
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Incomplete Form", color = RedCancelled, fontWeight = FontWeight.Bold) },
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

@Composable
private fun FixedDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextGrey)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

private fun buildCharityPickupSlots(value: String?): List<String> {
    val slots = value.orEmpty().split(',', ';', '|', '\n')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { formatCharityTimeSlot(it) }
        .distinct()
    return slots.ifEmpty { listOf("10:00 AM", "12:30 PM", "02:00 PM", "04:30 PM") }
}

private fun formatCharityTimeSlot(value: String): String {
    val normalized = value.trim().substringBefore('+').removeSuffix("Z")
    listOf("HH:mm:ss", "HH:mm", "hh:mm a").forEach { pattern ->
        runCatching {
            val parsed = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(normalized)
            if (parsed != null) return SimpleDateFormat("hh:mm a", Locale.US).format(parsed)
        }
    }
    return normalized
}

private fun parseCharityDeadline(value: String): Date? {
    val normalized = value.trim().substringBefore('+').removeSuffix("Z").replace('T', ' ')
    return listOf("yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm")
        .firstNotNullOfOrNull { pattern -> runCatching { SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(normalized) }.getOrNull() }
}

private fun parseCharityDate(value: String): Date? = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value)
}.getOrNull()

private fun parseCharityDateTime(date: String?, time: String?): Date? {
    if (date.isNullOrBlank() || time.isNullOrBlank()) return null
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).apply { isLenient = false }.parse("$date $time")
    }.getOrNull()
}
