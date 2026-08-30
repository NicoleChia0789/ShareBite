package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserMakeReservationScreen(
    listingId: Long,
    paddingValues: PaddingValues,
    onSuccess: () -> Unit
) {
    val foodRepo = remember { FoodListingRepository() }
    val userId = CurrentUser.getUserId()
    val reservationRepo = remember { ReservationRepository() }
    val scope = rememberCoroutineScope()

    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }

    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    val dateList = remember {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEE d", Locale.getDefault())
        val dates = mutableListOf<Triple<String, String, String>>()
        for (i in 0..6) {
            val date = calendar.time
            dates.add(
                Triple(
                    format.format(date),
                    displayFormat.format(date),
                    SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        dates
    }

    val timeSlots = listOf("10:00 AM", "12:30 PM", "02:00 PM", "04:30 PM")

    LaunchedEffect(listingId) {
        try {
            listing = foodRepo.getListingById(listingId)
            selectedDate = dateList.firstOrNull()?.first
            selectedTime = timeSlots.firstOrNull()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    fun submitReservation() {
        if (isSubmitting || listing == null || selectedDate == null || selectedTime == null) return
        scope.launch {
            isSubmitting = true
            try {
                reservationRepo.createMyReservation(

                    listingId = listingId,
                    merchantId = listing!!.merchantId ?: "",
                    foodName = listing!!.foodName,
                    quantity = listing!!.quantity,
                    pickupDate = selectedDate!!,
                    pickupTime = selectedTime!!,
                    note = note
                )
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
                isSubmitting = false
            }
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            CircularProgressIndicator(color = OrangeAccent)
        }

        listing == null -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            Text("Food not found", color = TextGrey)
        }

        else -> {
            val item = listing!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgCream)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Make Reservation",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(OrangeAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = OrangeAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(item.foodName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = TextGrey
                                )
                                Text(item.merchantName, fontSize = 13.sp, color = TextGrey)
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Date", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(dateList) { (dateStr, displayStr, dayOfWeek) ->
                            val isSelected = dateStr == selectedDate
                            Card(
                                modifier = Modifier
                                    .width(68.dp)
                                    .clickable { selectedDate = dateStr },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) OrangeAccent else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        dayOfWeek,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextGrey,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        displayStr.split(" ").last(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextDark
                                    )
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Available Times", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(timeSlots) { time ->
                            val isSelected = time == selectedTime
                            Card(
                                modifier = Modifier
                                    .clickable { selectedTime = time },
                                shape = RoundedCornerShape(50),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) OrangeAccent else Color.White
                                ),
                                border = if (!isSelected)
                                    androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDC0B8))
                                else null,
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                )
                            ) {
                                Text(
                                    time,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextDark
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    label = { Text("Message for Merchant (Optional)", color = TextGrey) },
                    placeholder = { Text("Let the donor know when you'll be arriving...", color = TextGrey) },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = Color(0xFFDDC0B8)
                    )
                )

                if (errorMessage != null) {
                    Text("Error: $errorMessage", color = RedCancelled, fontSize = 14.sp)
                }

                Button(
                    onClick = { submitReservation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    enabled = !isSubmitting && selectedDate != null && selectedTime != null
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Confirm", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}