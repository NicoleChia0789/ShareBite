package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.sharebite.app.data.session.CurrentUser
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
    val userId = CurrentUser.getUserId()
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCancelling by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(reservationId) {
        try {
            reservation = repository.getMyReservationById(reservationId)
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
                repository.cancelMyReservation(reservationId)
                reservation = repository.getMyReservationById(reservationId)
                showCancelDialog = false
            } catch (e: Exception) {
                errorMessage = e.message
                isCancelling = false
            }
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            CircularProgressIndicator(color = OrangeAccent)
        }

        errorMessage != null -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            Text(
                text = "Error: $errorMessage",
                color = RedCancelled
            )
        }

        reservation == null -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            Text(
                text = "Reservation not found",
                color = TextGrey
            )
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Status",
                                fontSize = 12.sp,
                                color = TextGrey
                            )
                            Text(
                                text = item.status,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = statusColor
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow(Icons.Default.Fastfood, "Food", item.foodName)
                        DetailRow(Icons.Default.Storefront, "Merchant ID", item.merchantId)
                        DetailRow(Icons.Default.Category, "Quantity", item.quantity)
                        DetailRow(Icons.Default.DateRange, "Pickup Date", item.pickupDate)
                        DetailRow(Icons.Default.Schedule, "Pickup Time", item.pickupTime)
                        if (item.note.isNotBlank()) {
                            DetailRow(Icons.Default.NoteAdd, "Note", item.note)
                        }
                        DetailRow(Icons.Default.Info, "Reservation ID", item.id.toString())
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
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Edit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (item.status == "Pending" || item.status == "Confirmed") {
                            Button(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = RedCancelled
                                ),
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RedCancelled)
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                when {
                    item.status == "Cancelled" -> Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDED))
                    ) {
                        Text(
                            text = "This reservation has been cancelled.",
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            color = RedCancelled,
                            fontSize = 14.sp
                        )
                    }

                    item.status == "Completed" -> Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F5E6))
                    ) {
                        Text(
                            text = "✅ This reservation has been completed.",
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            color = GreenConfirmed,
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = TextGrey
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "← Back",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = "Cancel this reservation?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "Canceling this reservation will return the items to the community. This action cannot be undone.",
                    fontSize = 14.sp,
                    color = TextGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = { cancelReservation() },
                    colors = ButtonDefaults.buttonColors(containerColor = RedCancelled),
                    shape = RoundedCornerShape(50),
                    enabled = !isCancelling
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Yes, Cancel",
                            color = Color.White
                        )
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelDialog = false },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Keep",
                        color = TextDark
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TextGrey
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextGrey,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium
        )
    }
}