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
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.FoodListingRepository
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
    val context = LocalContext.current
    val repository = remember { ReservationRepository() }
    val foodRepo = remember { FoodListingRepository() }
    val scope = rememberCoroutineScope()

    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var foodImage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isDataLoaded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCancelling by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(reservationId) {
        try {
            isLoading = true
            reservation = repository.getReservationById(reservationId)
            if (reservation == null) {
                errorMessage = "Reservation not found (ID: $reservationId)"
            } else {
                // 加载图片
                reservation?.listingId?.let { listingId ->
                    try {
                        val listing = foodRepo.getListingById(listingId)
                        foodImage = listing.photoUrl
                    } catch (e: Exception) { /* 忽略 */ }
                }
                isDataLoaded = true
            }
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
                reservation = repository.getReservationById(reservationId)
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
                "Pending" -> OrangeAccent
                "Cancelled" -> RedCancelled
                "Completed" -> GreyCompleted
                else -> TextGrey
            }
            val statusBg = when (item.status) {
                "Confirmed" -> Color(0xFFE3F5E6)
                "Pending" -> NavSelectedBg
                "Cancelled" -> Color(0xFFFFEDED)
                "Completed" -> Color(0xFFEEEEEE)
                else -> Color(0xFFEEEEEE)
            }

            val step1Completed = true
            val step2Completed = item.status == "Confirmed" || item.status == "Completed"
            val step3Completed = item.status == "Confirmed" || item.status == "Completed"
            val step4Completed = item.status == "Completed"

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
                        if (!foodImage.isNullOrBlank()) {
                            AsyncImage(
                                model = foodImage,
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

                        // ── Status Overlay Badge ──
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
                                    text = item.status,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }

                        // 渐变覆盖 + 标题
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
                                    text = "Merchant ID: ${item.merchantId}",
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 日期时间
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

                                // 地址 + View on Map
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
                                            text = "452 Artisan Way, Northside Arts District",
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
                                                val uri = Uri.parse("geo:0,0?q=452+Artisan+Way")
                                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ── 商家备注（带左边框） ──
                        if (item.note.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF8EE),
                                border = androidx.compose.foundation.BorderStroke(
                                    4.dp,
                                    OrangeAccent
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
                                        text = item.note,
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
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
                                    time = item.createdAt.take(16).replace("T", " · "),
                                    isCompleted = step1Completed,
                                    isLast = false,
                                    icon = Icons.Default.CheckCircle
                                )
                                TimelineStep(
                                    title = "Merchant Reviewed",
                                    time = if (step2Completed) "Review completed" else "Awaiting...",
                                    isCompleted = step2Completed,
                                    isLast = false,
                                    icon = Icons.Default.Visibility
                                )
                                TimelineStep(
                                    title = "Reservation Accepted",
                                    time = if (step3Completed) "Accepted by merchant" else "Pending...",
                                    isCompleted = step3Completed,
                                    isLast = false,
                                    icon = Icons.Default.TaskAlt
                                )
                                TimelineStep(
                                    title = "Ready for Pickup",
                                    time = if (step4Completed) "Pickup completed" else "Awaiting collection",
                                    isCompleted = step4Completed,
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
                        if (item.status == "Pending") {
                            Button(
                                onClick = { onEditClick(item.listingId) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangeAccent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Time",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (item.status == "Pending" || item.status == "Confirmed") {
                            Button(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier
                                    .weight(if (item.status == "Pending") 1f else 1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = RedCancelled
                                ),
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RedCancelled)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cancel",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (item.status != "Pending" && item.status != "Confirmed") {
                            Button(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = TextGrey
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "← Back",
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