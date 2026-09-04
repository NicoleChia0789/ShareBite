package com.sharebite.app.ui.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun UserReservationDetailScreen(
    reservationId: Long,
    paddingValues: PaddingValues,
    onEditClick: (Long, Long) -> Unit,  // ✅ 改为 (listingId, reservationId)
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ReservationRepository() }
    val foodRepo = remember { FoodListingRepository() }
    val scope = rememberCoroutineScope()

    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCancelling by remember { mutableStateOf(false) }
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    var cancellationReason by rememberSaveable { mutableStateOf("") }
    var foodListing by remember { mutableStateOf<FoodListing?>(null) }

    LaunchedEffect(reservationId) {
        try {
            reservation = repository.getMyReservationById(reservationId)
            reservation?.listingId?.let { listingId ->
                try {
                    val listing = foodRepo.getListingById(listingId)
                    foodListing = listing
                } catch (e: Exception) {
                    // 忽略
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    fun cancelReservation(reason: String) {
        if (isCancelling) return
        scope.launch {
            isCancelling = true
            try {
                repository.cancelMyReservation(reservationId, reason)
                reservation = repository.getMyReservationById(reservationId)
                showCancelDialog = false
            } catch (e: Exception) {
                errorMessage = e.message
                isCancelling = false
            }
        }
    }

    when {
        isLoading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OrangeAccent)
        }

        errorMessage != null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Text(text = "Error: $errorMessage", color = RedCancelled)
        }

        reservation == null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Text(text = "Reservation not found", color = TextGrey)
        }

        else -> {
            val item = reservation!!
            val statusColor = when (item.status) {
                "Confirmed" -> GreenConfirmed
                "Pending" -> Color(0xFFE65100)
                "Cancelled" -> RedCancelled
                "Completed" -> GreyCompleted
                else -> TextGrey
            }
            val statusBg = when (item.status) {
                "Confirmed" -> Color(0xFFE8F5E9)
                "Pending" -> Color(0xFFFFF3E0)
                "Cancelled" -> Color(0xFFFFEBEE)
                "Completed" -> Color(0xFFF5F5F5)
                else -> Color(0xFFF5F5F5)
            }
            val statusHeadline = when (item.status) {
                "Pending", "Confirmed" -> if (item.pickupDate == getReservationTodayDateString()) {
                    "PICK UP TODAY"
                } else {
                    "PICK UP ON ${formatReservationShortDate(item.pickupDate).uppercase()}"
                }
                "Completed" -> "COLLECTED ON ${formatReservationShortDate(item.pickupDate).uppercase()}"
                "Cancelled" -> "CANCELLED"
                else -> item.status.uppercase()
            }

            val step1Completed = true
            val step2Completed = item.status == "Confirmed" || item.status == "Completed"
            val step3Completed = item.status == "Completed"

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    // ── 顶部图片 ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (!foodListing?.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = foodListing?.photoUrl,
                                contentDescription = item.foodName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFEFE6D9))
                                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fastfood,
                                    contentDescription = "Food",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFFD8C4A8)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            shape = RoundedCornerShape(50),
                            color = statusBg,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (item.status) {
                                        "Confirmed" -> Icons.Default.CheckCircle
                                        "Completed" -> Icons.Default.CheckCircle
                                        "Cancelled" -> Icons.Default.Cancel
                                        else -> Icons.Default.Schedule
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = statusColor
                                )
                                Text(
                                    text = item.status.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = statusHeadline,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = item.foodName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Merchant: ${foodListing?.merchantName?.takeIf { it.isNotBlank() } ?: "Merchant"}",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // ── 分类 Tags ──
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = NavSelectedBg
                            ) {
                                Text(
                                    text = "Bakery",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = OrangeAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFE3F5E6)
                            ) {
                                Text(
                                    text = "Vegetarian",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = GreenConfirmed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // ── Pickup Logistics Card ──
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DED5))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(OrangeAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = OrangeAccent
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Scheduled Date & Time",
                                            fontSize = 12.sp,
                                            color = TextGrey
                                        )
                                        Text(
                                            text = "${item.pickupDate} · ${item.pickupTime}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                    }
                                }

                                Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(OrangeAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = OrangeAccent
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Pickup Location",
                                            fontSize = 12.sp,
                                            color = TextGrey
                                        )
                                        Text(
                                            text = foodListing?.address?.ifBlank { "Pickup address not available" } ?: "Pickup address not available",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "View on Map",
                                            fontSize = 12.sp,
                                            color = OrangeAccent,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable {
                                                val uri = Uri.parse("geo:0,0?q=${Uri.encode(foodListing?.address.orEmpty())}")
                                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (!item.note.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(0xFFE8DED5)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = "Note from Merchant",
                                        fontSize = 12.sp,
                                        color = TextGrey
                                    )
                                    Text(
                                        text = item.note ?: "",
                                        fontSize = 14.sp,
                                        color = TextDark
                                    )
                                }
                            }
                        }

                        // ── Reservation Progress Timeline ──
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DED5))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                Text(
                                    text = "Reservation Progress",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                TimelineStep(
                                    title = "Reservation Received",
                                    time = item.createdAt?.take(16)?.replace("T", " · ") ?: "Pending",
                                    isCompleted = step1Completed,
                                    isLast = false,
                                    icon = Icons.Default.CheckCircle
                                )
                                TimelineStep(
                                    title = "Reservation Accepted",
                                    time = if (step3Completed) "Accepted by merchant" else "Pending...",
                                    isCompleted = step2Completed,
                                    isLast = false,
                                    icon = Icons.Default.TaskAlt
                                )
                                TimelineStep(
                                    title = "Ready for Pickup",
                                    time = if (step3Completed) "Pickup completed" else "Awaiting collection",
                                    isCompleted = step3Completed,
                                    isLast = true,
                                    icon = Icons.Default.Storefront
                                )
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

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ── 底部固定两个按钮 ──
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = BgCream,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (item.status == "Pending" || item.status == "Confirmed") {
                            Button(
                                onClick = { openReservationDirections(context, foodListing) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent, contentColor = Color.White),
                                shape = RoundedCornerShape(50),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Directions", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancelled),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RedCancelled),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (item.status != "Pending" && item.status != "Confirmed") {
                            Button(
                                onClick = {
                                    if (item.status == "Completed") {
                                        onEditClick(item.listingId, 0L)
                                    } else {
                                        onBack()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = if (item.status == "Completed") GreenConfirmed else TextGrey
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (item.status == "Completed") "Reserve Again" else "Back",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Canceling this reservation will return the items to the community. This action cannot be undone.",
                        fontSize = 14.sp,
                        color = TextGrey
                    )
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
            },
            confirmButton = {
                Button(
                    onClick = { cancelReservation(cancellationReason.trim()) },
                    colors = ButtonDefaults.buttonColors(containerColor = RedCancelled),
                    shape = RoundedCornerShape(50),
                    enabled = !isCancelling && cancellationReason.isNotBlank()
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(text = "Yes, Cancel", color = Color.White)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelDialog = false },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(text = "Keep", color = TextDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun TimelineStep(
    title: String,
    time: String,
    isCompleted: Boolean,
    isLast: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isCompleted) GreenConfirmed else Color(0xFFDDC0B8),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            color = if (isCompleted) GreenConfirmed else Color(0xFFDDC0B8)
                        )
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .padding(bottom = if (isLast) 0.dp else 8.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCompleted) TextDark else TextGrey
            )
            Text(
                text = time,
                fontSize = 12.sp,
                color = TextGrey
            )
        }
    }
}

private fun formatReservationShortDate(dateStr: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val outputFormat = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.US)
        val date = inputFormat.parse(dateStr)
        if (date != null) outputFormat.format(date) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}

private fun getReservationTodayDateString(): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
}

private fun openReservationDirections(context: android.content.Context, listing: FoodListing?) {
    val uri = if (listing?.latitude != null && listing.longitude != null) {
        Uri.parse("geo:${listing.latitude},${listing.longitude}?q=${listing.latitude},${listing.longitude}(${Uri.encode(listing.merchantName)})")
    } else {
        Uri.parse("geo:0,0?q=${Uri.encode(listing?.address.orEmpty())}")
    }
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}
