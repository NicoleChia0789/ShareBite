package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

@Composable
fun MerchantRequestDetailScreen(
    paddingValues: PaddingValues,
    requestId: Long,
    viewModel: MerchantViewModel,
    onBack: () -> Unit,
    onApprove: () -> Unit,
    onDecline: () -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val request = requests.find { it.id == requestId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
            .padding(16.dp)
    ) {
        Text("Request Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (request != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(request.organisationName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Requesting: ${request.quantity} x ${request.foodName}", color = Color.Gray)
                    Text("Status: ${request.status}", color = if (request.status == "Pending") OrangeAccent else Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            when (request.status) {
                "Pending" -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onDecline,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Decline")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Approve", color = Color.White)
                        }
                    }
                }
                "Confirmed" -> {
                    StatusNotice(
                        text = "You approved this request. The charity has been notified.",
                        color = Color(0xFF2E7D32)
                    )
                }
                "Cancelled" -> {
                    StatusNotice(
                        text = "You declined this request.",
                        color = Color.Red
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Request not found.", color = Color.Gray)
        }
    }
}

@Composable
fun MerchantReservationDetailScreen(
    paddingValues: PaddingValues,
    reservationId: Long,
    viewModel: MerchantViewModel,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onDecline: () -> Unit  // <-- Ensure this is here!
) {
    val reservations by viewModel.reservations.collectAsState()
    val reservation = reservations.find { it.id == reservationId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
            .padding(16.dp)
    ) {
        Text("Reservation Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (reservation != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("User: ${reservation.userId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reserved: ${reservation.quantity} x ${reservation.foodName}", color = Color.Gray)
                    Text("Pickup: ${reservation.pickupDate} at ${reservation.pickupTime}", color = Color.Gray)
                    Text("Status: ${reservation.status}", color = if (reservation.status == "Pending") OrangeAccent else Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            when (reservation.status) {
                "Pending" -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onDecline,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Decline")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm Pickup", color = Color.White)
                        }
                    }
                }
                "Confirmed" -> {
                    StatusNotice(
                        text = "Pickup confirmed for this reservation.",
                        color = Color(0xFF2E7D32)
                    )
                }
                "Cancelled" -> {
                    StatusNotice(
                        text = "This reservation was declined.",
                        color = Color.Red
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Reservation not found.", color = Color.Gray)
        }
    }
}

@Composable
private fun StatusNotice(text: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
    }
}