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
import java.text.SimpleDateFormat
import java.util.Locale

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
    val isGeneralRequest = generalRequests.any { it.id == requestId && it.merchantId.isNullOrBlank() }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(request.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (isGeneralRequest) "Charity Need" else "Donation Request",
                            color = OrangeAccent,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DetailValueRow("Quantity", "${request.quantity} portions")
                    DetailValueRow("Collection", formatPickupDateTime(request.pickupDate, request.pickupTime))
                    DetailValueRow("Organization", request.organisationName.ifBlank { "Not provided" })
                    DetailValueRow("People served", request.peopleServed.takeIf { it > 0 }?.toString() ?: "Not provided")
                    DetailValueRow("Submitted", formatSubmittedAt(request.createdAt))
                    DetailValueRow("Note", request.note?.takeIf { it.isNotBlank() } ?: "No additional instructions")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (request.status) {
                "Pending" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ Decline 按钮：红色底白字，宽度 0.6f
                        OutlinedButton(
                            onClick = {
                                if (isGeneralRequest) {
                                    onBack()
                                } else {
                                    actionType = "decline"
                                    showConfirmDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = RedCancelled,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(if (isGeneralRequest) "Back" else "Decline", color = Color.White)
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
                            Text(if (isGeneralRequest) "Offer to Fulfil" else "Approve Request", color = Color.White)
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
                    if (actionType == "approve") {
                        if (isGeneralRequest) "Offer to Fulfil?" else "Approve Request?"
                    } else "Decline Request?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (actionType == "approve")
                        if (isGeneralRequest)
                            "This will assign the request to you and add its pickup to your schedule."
                        else
                            "This will reserve the requested quantity and notify the charity."
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
                    Text(if (actionType == "approve") {
                        if (isGeneralRequest) "Offer Help" else "Approve"
                    } else "Decline")
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
    val usersById by viewModel.reservationUsers.collectAsState()
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(reservation.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("User Reservation", color = OrangeAccent, style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val userName = usersById[reservation.userId]?.name
                        ?.takeIf { it.isNotBlank() }
                        ?: "User"
                    DetailValueRow("Reserved by", userName)
                    DetailValueRow("Quantity", "${reservation.quantityNum} ${reservation.unit}")
                    DetailValueRow("Collection", formatPickupDateTime(reservation.pickupDate, reservation.pickupTime))
                    DetailValueRow("Submitted", formatSubmittedAt(reservation.createdAt))
                    DetailValueRow("Note", reservation.note?.takeIf { it.isNotBlank() } ?: "No additional instructions")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                            modifier = Modifier.weight(1f),
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
                            Text("Approve Reservation", color = Color.White)
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
                    if (actionType == "confirm") "Approve Reservation?" else "Decline Reservation?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (actionType == "confirm")
                        "This will reserve the requested quantity and notify the user."
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

@Composable
private fun DetailValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = Color.Gray, modifier = Modifier.weight(0.42f))
        Text(value, modifier = Modifier.weight(0.58f), fontWeight = FontWeight.Medium)
    }
}

private fun formatPickupDateTime(date: String, time: String): String {
    val raw = "${date.trim()} ${time.trim().substringBefore('+').removeSuffix("Z")}".trim()
    val patterns = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd h:mm a")
    for (pattern in patterns) {
        try {
            val parsed = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(raw)
            if (parsed != null) {
                return SimpleDateFormat("d MMM yyyy · h:mm a", Locale.getDefault()).format(parsed)
            }
        } catch (_: Exception) {
            // Try the next supported database format.
        }
    }
    return "Pickup time unavailable"
}

private fun formatSubmittedAt(value: String?): String {
    if (value.isNullOrBlank()) return "Not provided"
    val normalized = value.trim().substringBefore('+').removeSuffix("Z").replace('T', ' ')
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss.SSSSSS",
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (pattern in patterns) {
        try {
            val parsed = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(normalized)
            if (parsed != null) {
                return SimpleDateFormat("d MMM yyyy · h:mm a", Locale.getDefault()).format(parsed)
            }
        } catch (_: Exception) {
            // Try the next supported Supabase timestamp precision.
        }
    }
    return "Not provided"
}
