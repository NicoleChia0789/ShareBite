package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun UserMakeReservationScreen(
    listingId: Long,
    paddingValues: PaddingValues,
    onSuccess: () -> Unit
) {
    val foodRepo = remember { FoodListingRepository() }
    val reservationRepo = remember { ReservationRepository() }
    val scope = rememberCoroutineScope()

    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var selectedPickupDate by remember { mutableStateOf("") }
    var selectedPickupTime by remember { mutableStateOf("") }

    LaunchedEffect(listingId) {
        try {
            listing = foodRepo.getListingById(listingId)
            // 只设置日期，时间让用户自己输入
            selectedPickupDate = listing?.pickupDate ?: ""
            // 不设置 pickupTime，因为 FoodListing 没有这个字段
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    fun submitReservation() {
        if (isSubmitting || listing == null) return
        scope.launch {
            isSubmitting = true
            try {
                reservationRepo.createReservation(
                    userId = "test-user-id", // 临时
                    listingId = listingId,
                    merchantId = listing!!.merchantId ?: "",
                    foodName = listing!!.foodName,
                    quantity = listing!!.quantity,
                    pickupDate = selectedPickupDate,
                    pickupTime = selectedPickupTime,  // 用户输入的时间
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
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        }
        listing == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Food not found", color = TextGrey)
            }
        }
        else -> {
            val item = listing!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgCream)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Confirm Reservation",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Food: ${item.foodName}", fontWeight = FontWeight.Bold)
                        Text("Merchant: ${item.merchantName}")
                        Text("Category: ${item.category}")
                        Text("Quantity: ${item.quantity}")
                    }
                }

                OutlinedTextField(
                    value = selectedPickupDate,
                    onValueChange = { selectedPickupDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Pickup Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent
                    )
                )

                OutlinedTextField(
                    value = selectedPickupTime,
                    onValueChange = { selectedPickupTime = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Pickup Time") },
                    placeholder = { Text("HH:MM AM/PM") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    label = { Text("Note (Optional)") },
                    placeholder = { Text("Any special requests?") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent
                    )
                )

                if (errorMessage != null) {
                    Text(
                        "Error: $errorMessage",
                        color = RedCancelled,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = { submitReservation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Confirm Reservation", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { /* 返回由导航处理 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = TextGrey
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}