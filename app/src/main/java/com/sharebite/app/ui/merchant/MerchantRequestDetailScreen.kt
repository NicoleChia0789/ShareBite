package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.GreenConfirmed
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.theme.RedCancelled

// ─── Charity Request Detail ──────────────────────────────────────

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
    val generalRequests by viewModel.generalRequests.collectAsState()
    val request = requests.find { it.id == requestId } ?: generalRequests.find { it.id == requestId }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("") }

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
                    // ✅ 食物名作为标题
                    Text(request.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Requested by: ${request.organisationName}", color = Color.Gray)
                    Text("Quantity: ${request.quantity}", color = Color.Gray)
                    Text("📅 Pickup Date: ${request.pickupDate}", color = Color.Gray)
                    Text("⏰ Pickup Time: ${request.pickupTime}", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            when (request.status) {
                "Pending" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ Decline 按钮：红色底白字，宽度 0.6f
                        OutlinedButton(
                            onClick = {
                                actionType = "decline"
                                showConfirmDialog = true
                            },
                            modifier = Modifier.weight(0.6f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = RedCancelled,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Decline", color = Color.White)
                        }
                        // ✅ Approve 按钮：橙色，宽度 1f
                        Button(
                            onClick = {
                                actionType = "approve"
                                showConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50)
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

    // ✅ 确认对话框
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    if (actionType == "approve") "Approve Request?" else "Decline Request?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (actionType == "approve")
                        "This will confirm the request. The charity will be notified."
                    else
                        "This will reject the request. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (actionType == "approve") onApprove() else onDecline()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (actionType == "approve") GreenConfirmed else RedCancelled
                    )
                ) {
                    Text(if (actionType == "approve") "Approve" else "Decline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ─── Reservation Detail ──────────────────────────────────────────

@Composable
fun MerchantReservationDetailScreen(
    paddingValues: PaddingValues,
    reservationId: Long,
    viewModel: MerchantViewModel,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onDecline: () -> Unit
) {
    val reservations by viewModel.reservations.collectAsState()
    val reservation = reservations.find { it.id == reservationId }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("") }

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
                    // ✅ 食物名作为标题
                    Text(reservation.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Reserved by: ${reservation.userId}", color = Color.Gray)
                    Text("Quantity: ${reservation.quantityNum} × ${reservation.unit}", color = Color.Gray)
                    Text("📅 Pickup Date: ${reservation.pickupDate}", color = Color.Gray)
                    Text("⏰ Pickup Time: ${reservation.pickupTime}", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            when (reservation.status) {
                "Pending" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ Decline 按钮：红色底白字，宽度 0.6f
                        OutlinedButton(
                            onClick = {
                                actionType = "decline"
                                showConfirmDialog = true
                            },
                            modifier = Modifier.weight(0.6f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = RedCancelled,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Decline", color = Color.White)
                        }
                        // ✅ Confirm 按钮：橙色，宽度 1f
                        Button(
                            onClick = {
                                actionType = "confirm"
                                showConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50)
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

    // ✅ 确认对话框
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    if (actionType == "confirm") "Confirm Pickup?" else "Decline Reservation?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (actionType == "confirm")
                        "This will confirm the pickup. The user will be notified."
                    else
                        "This will reject the reservation. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (actionType == "confirm") onConfirm() else onDecline()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (actionType == "confirm") GreenConfirmed else RedCancelled
                    )
                ) {
                    Text(if (actionType == "confirm") "Confirm" else "Decline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ─── Status Notice ──────────────────────────────────────────────

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