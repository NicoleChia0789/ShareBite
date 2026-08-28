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
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun UserReservationDetailScreen(
    reservationId: Long,
    paddingValues: PaddingValues,
    onEditClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { ReservationRepository() }
    val scope = rememberCoroutineScope()

    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCancelling by remember { mutableStateOf(false) }

    LaunchedEffect(reservationId) {
        try {
            reservation = repository.getReservationById(reservationId)
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    fun cancelReservation() {
        if (isCancelling) return
        scope.launch {
            isCancelling = true
            try {
                repository.cancelReservation(reservationId)
                // 刷新数据
                reservation = repository.getReservationById(reservationId)
            } catch (e: Exception) {
                errorMessage = e.message
                isCancelling = false
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
        errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $errorMessage", color = RedCancelled)
            }
        }
        reservation == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Reservation not found", color = TextGrey)
            }
        }
        else -> {
            val item = reservation!!
            val statusColor = when (item.status) {
                "Confirmed" -> GreenConfirmed
                "Pending" -> OrangeAccent
                "Cancelled" -> RedCancelled
                "Completed" -> GreyCompleted
                else -> TextGrey
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgCream)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (item.status) {
                            "Confirmed" -> Color(0xFFE3F5E6)
                            "Pending" -> NavSelectedBg
                            "Cancelled" -> Color(0xFFFFEDED)
                            "Completed" -> Color(0xFFEEEEEE)
                            else -> Color(0xFFEEEEEE)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Status", fontSize = 12.sp, color = TextGrey)
                            Text(
                                item.status,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                        Text("📋", fontSize = 32.sp)
                    }
                }

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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow("🍱", "Food", item.foodName)
                        DetailRow("🏪", "Merchant ID", item.merchantId)
                        DetailRow("📦", "Quantity", item.quantity)
                        DetailRow("📅", "Pickup Date", item.pickupDate)
                        DetailRow("⏰", "Pickup Time", item.pickupTime)
                        if (item.note.isNotBlank()) {
                            DetailRow("📝", "Note", item.note)
                        }
                        DetailRow("🆔", "Reservation ID", item.id.toString())
                    }
                }

                if (item.status == "Pending" || item.status == "Confirmed") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (item.status == "Pending") {
                            Button(
                                onClick = { onEditClick(item.listingId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Edit")
                            }
                        }

                        if (item.status == "Pending" || item.status == "Confirmed") {
                            Button(
                                onClick = { cancelReservation() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RedCancelled
                                ),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isCancelling
                            ) {
                                if (isCancelling) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }

                if (item.status == "Cancelled") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEDED)
                        )
                    ) {
                        Text(
                            "This reservation has been cancelled.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = RedCancelled,
                            fontSize = 14.sp
                        )
                    }
                }

                if (item.status == "Completed") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F5E6)
                        )
                    ) {
                        Text(
                            "✅ This reservation has been completed.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = GreenConfirmed,
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = TextGrey
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("← Back")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = TextGrey, modifier = Modifier.width(100.dp))
        Text(value, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
    }
}