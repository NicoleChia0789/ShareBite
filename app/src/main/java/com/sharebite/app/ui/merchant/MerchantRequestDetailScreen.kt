package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.GreenConfirmed
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.theme.RedCancelled
import com.sharebite.app.data.model.FoodListing
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Charity Request Detail ──────────────────────────────────────

@Composable
fun MerchantRequestDetailScreen(
    paddingValues: PaddingValues,
    requestId: Long,
    viewModel: MerchantViewModel,
    onBack: () -> Unit,
    onApprove: () -> Unit,
    onDecline: (String) -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val generalRequests by viewModel.generalRequests.collectAsState()
    val listings by viewModel.listings.collectAsState()
    val request = requests.find { it.id == requestId } ?: generalRequests.find { it.id == requestId }
    val listing = request?.listingId?.toLongOrNull()?.let { id -> listings.find { it.id == id } }
    val isGeneralRequest = request != null && request.listingId.isNullOrBlank()
    val isClaimedBazaarRequest = isGeneralRequest && !request?.merchantId.isNullOrBlank()
    // Only a request tied to a merchant listing can be an update to a
    // previously approved detail. Bazaar/general requests use a different flow.
    val isUpdatedLinkedRequest = request?.detailsChanged == true && !isGeneralRequest
    // Charity requests use one unit per person served. Keep the old quantity as
    // a fallback for requests created before peopleServed became the source of truth.
    val requestedUnits = request?.peopleServed?.takeIf { it > 0 } ?: request?.quantity ?: 0

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var actionType by rememberSaveable { mutableStateOf("") }
    var cancellationReason by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Request Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (request != null) {
            if (isUpdatedLinkedRequest) {
                StatusNotice(
                    text = "Charity has changed this confirmed request. Review the new quantity and pickup time before approving.",
                    color = OrangeAccent
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            FoodContextCard(
                listing = listing,
                foodName = request.foodName,
                quantityLabel = "$requestedUnits units requested"
            )
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
                    DetailValueRow("Quantity", "$requestedUnits units")
                    DetailValueRow("Collection", formatPickupDateTime(request.pickupDate, request.pickupTime))
                    DetailValueRow("Organization", request.organisationName.ifBlank { "Not provided" })
                    DetailValueRow(
                        "People served",
                        request.peopleServed.takeIf { it > 0 }?.let { "$it people" } ?: "Not provided"
                    )
                    DetailValueRow("Submitted", formatSubmittedAt(request.createdAt))
                    DetailValueRow("Note", request.note?.takeIf { it.isNotBlank() } ?: "No additional instructions")
                    if (request.status == "Cancelled" && !request.cancellationReason.isNullOrBlank()) {
                        DetailValueRow("Cancellation reason", request.cancellationReason!!)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {
                // An edited request must be reviewed again before it can return
                // to the pickup flow, even when its previous status was Confirmed.
                isUpdatedLinkedRequest -> {
                    StatusNotice(
                        text = "Review the updated request and choose whether to approve or reject it.",
                        color = OrangeAccent
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                actionType = "decline"
                                cancellationReason = ""
                                showConfirmDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancelled),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                actionType = "approve"
                                showConfirmDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Approve", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                request.status == "Pending" && !isGeneralRequest -> {
                    StatusNotice(
                        text = "This request is pending pickup confirmation.",
                        color = OrangeAccent
                    )
                    Button(
                        onClick = { viewModel.updateRequestStatus(request.id, "Completed") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Mark as Picked Up", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { actionType = "decline"; cancellationReason = ""; showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancelled),
                        shape = RoundedCornerShape(50)
                    ) { Text("Cancel Pickup", fontWeight = FontWeight.Bold) }
                }
                request.status == "Pending" && isClaimedBazaarRequest -> {
                    StatusNotice(
                        text = "This Bazaar request is waiting for pickup confirmation.",
                        color = OrangeAccent
                    )
                    Button(
                        onClick = { viewModel.updateRequestStatus(request.id, "Completed") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Mark as Picked Up", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                request.status == "Pending" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ Decline 按钮：红色底白字，宽度 0.6f
                        if (!isGeneralRequest) {
                            OutlinedButton(
                                onClick = {
                                    actionType = "decline"
                                    cancellationReason = ""
                                    showConfirmDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = RedCancelled,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50)
                            ) { Text("Cancel", color = Color.White) }
                        }
                        // ✅ Approve 按钮：橙色，宽度 1f
                        Button(
                            onClick = {
                                actionType = "approve"
                                showConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            modifier = Modifier.weight(if (isGeneralRequest) 1f else 1f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(if (isGeneralRequest) "Offer to Fulfil" else "Approve Request", color = Color.White)
                        }
                    }
                }
                request.status == "Confirmed" -> {
                    StatusNotice(
                        text = if (isPickupOverdue(request.pickupDate, request.pickupTime))
                            "This pickup is overdue and has been moved to history."
                        else
                            "Pickup confirmed. Mark it as picked up after collection.",
                        color = Color(0xFF2E7D32)
                    )
                    if (!isPickupOverdue(request.pickupDate, request.pickupTime)) {
                        Button(
                            onClick = { viewModel.updateRequestStatus(request.id, "Completed") },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenConfirmed),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Mark as Picked Up", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = { actionType = "decline"; cancellationReason = ""; showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancelled),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Cancel Pickup", fontWeight = FontWeight.Bold)
                    }
                }
                request.status == "Completed" -> {
                    StatusNotice("This pickup has been completed.", GreenConfirmed)
                }
                request.status == "Overdue" -> {
                    StatusNotice("This pickup is overdue and is shown in history.", RedCancelled)
                }
                request.status == "Cancelled" -> {
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (actionType == "approve")
                            if (isGeneralRequest)
                                "This will assign the request to you and add its pickup to your schedule."
                            else
                                "This request is already confirmed."
                        else
                            "This will cancel the request. This action cannot be undone."
                    )
                    if (actionType != "approve") {
                        OutlinedTextField(
                            value = cancellationReason,
                            onValueChange = { cancellationReason = it },
                            label = { Text("Cancellation reason") },
                            placeholder = { Text("Why are you cancelling?") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            isError = cancellationReason.isBlank()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (actionType == "approve") onApprove() else onDecline(cancellationReason.trim())
                        showConfirmDialog = false
                    },
                    enabled = actionType == "approve" || cancellationReason.isNotBlank(),
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
    onDecline: (String) -> Unit
) {
    val reservations by viewModel.reservations.collectAsState()
    val usersById by viewModel.reservationUsers.collectAsState()
    val listings by viewModel.listings.collectAsState()
    val reservation = reservations.find { it.id == reservationId }
    val listing = reservation?.let { current -> listings.find { it.id == current.listingId } }

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var actionType by rememberSaveable { mutableStateOf("") }
    var cancellationReason by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
            .padding(16.dp)
    ) {
        Text("Reservation Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (reservation != null) {
            FoodContextCard(
                listing = listing,
                foodName = reservation.foodName,
                quantityLabel = "${reservation.quantityNum} ${reservation.unit} reserved"
            )
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
                    if (reservation.status == "Cancelled" && !reservation.cancellationReason.isNullOrBlank()) {
                        DetailValueRow("Cancellation reason", reservation.cancellationReason!!)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {
                reservation.status == "Pending" -> {
                    StatusNotice(
                        text = "This reservation is pending pickup confirmation.",
                        color = OrangeAccent
                    )
                    Button(
                        onClick = { viewModel.updateReservationStatus(reservation.id, "Completed") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Mark as Picked Up", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { actionType = "decline"; cancellationReason = ""; showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancelled),
                        shape = RoundedCornerShape(50)
                    ) { Text("Cancel Pickup", fontWeight = FontWeight.Bold) }
                }
                reservation.status == "Confirmed" -> {
                    StatusNotice(
                        text = if (isPickupOverdue(reservation.pickupDate, reservation.pickupTime))
                            "This pickup is overdue and has been moved to history."
                        else
                            "Pickup confirmed. Mark it as picked up after collection.",
                        color = Color(0xFF2E7D32)
                    )
                    if (!isPickupOverdue(reservation.pickupDate, reservation.pickupTime)) {
                        Button(
                            onClick = { viewModel.updateReservationStatus(reservation.id, "Completed") },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenConfirmed),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Mark as Picked Up", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = { actionType = "decline"; cancellationReason = ""; showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancelled),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Cancel Pickup", fontWeight = FontWeight.Bold)
                    }
                }
                reservation.status == "Completed" -> {
                    StatusNotice("This pickup has been completed.", GreenConfirmed)
                }
                reservation.status == "Overdue" -> {
                    StatusNotice("This pickup is overdue and is shown in history.", RedCancelled)
                }
                reservation.status == "Cancelled" -> {
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (actionType == "confirm")
                            "This reservation is already confirmed."
                        else
                            "This will cancel the reservation. This action cannot be undone."
                    )
                    if (actionType != "confirm") {
                        OutlinedTextField(
                            value = cancellationReason,
                            onValueChange = { cancellationReason = it },
                            label = { Text("Cancellation reason") },
                            placeholder = { Text("Why are you cancelling?") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            isError = cancellationReason.isBlank()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (actionType == "confirm") onConfirm() else onDecline(cancellationReason.trim())
                        showConfirmDialog = false
                    },
                    enabled = actionType == "confirm" || cancellationReason.isNotBlank(),
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
                return SimpleDateFormat("d MMM yyyy · h:mm a", Locale.US).format(parsed)
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
                return SimpleDateFormat("d MMM yyyy · h:mm a", Locale.US).format(parsed)
            }
        } catch (_: Exception) {
            // Try the next supported Supabase timestamp precision.
        }
    }
    return "Not provided"
}

private fun isPickupOverdue(date: String, time: String): Boolean {
    val formats = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd h:mm a")
    val pickup = formats.firstNotNullOfOrNull { format ->
        runCatching { SimpleDateFormat(format, Locale.ENGLISH).apply { isLenient = false }.parse("$date $time") }.getOrNull()
    } ?: return false
    return pickup.before(Date())
}

@Composable
private fun FoodContextCard(listing: FoodListing?, foodName: String, quantityLabel: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(88.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFE7D6)),
                contentAlignment = Alignment.Center
            ) {
                if (!listing?.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = listing?.photoUrl,
                        contentDescription = foodName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Food", color = OrangeAccent, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(foodName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(quantityLabel, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                listing?.let {
                    Text("${it.remainingQuantity} remaining", color = OrangeAccent, style = MaterialTheme.typography.bodySmall)
                    if (it.pickupDate.isNotBlank() || !it.pickupTime.isNullOrBlank()) {
                        Text("Pickup: ${formatPickupDateTime(it.pickupDate, it.pickupTime.orEmpty())}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
