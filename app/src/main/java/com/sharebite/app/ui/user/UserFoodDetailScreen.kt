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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserFoodDetailScreen(
    listingId: Long,
    paddingValues: PaddingValues,
    onReserveClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FoodListingRepository() }
    val reservationRepo = remember { ReservationRepository() }  // ✅ 新增
    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaved by remember { mutableStateOf(false) }
    var hasActiveReservation by remember { mutableStateOf(false) }  // ✅ 新增

    LaunchedEffect(listingId) {
        try {
            listing = repository.getListingById(listingId)
            // ✅ 检查是否已有活跃预订
            val allReservations = reservationRepo.getMyReservations()
            hasActiveReservation = allReservations.any {
                it.listingId == listingId && (it.status == "Pending" || it.status == "Confirmed")
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val countdownText = remember(listing?.deadline) {
        listing?.let {
            try {
                val deadline = parseFoodDetailDeadline(it.deadline)
                val now = Date()
                if (deadline != null && deadline.after(now)) {
                    val diff = deadline.time - now.time
                    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
                    val hours = ((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)).toInt()
                    if (days > 0) "${days}d ${hours}h" else "${hours}h"
                } else "Expired"
            } catch (e: Exception) {
                "N/A"
            }
        } ?: "N/A"
    }
    val deadlineText = remember(listing?.deadline) {
        formatFoodDetailDeadline(listing?.deadline.orEmpty())
    }

    val viewersCount = remember { (2..5).random() }

    when {
        isLoading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OrangeAccent)
        }

        errorMessage != null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Text(text = "Error: $errorMessage", color = RedCancelled)
        }

        listing == null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Text(text = "Food not found", color = TextGrey)
        }

        else -> {
            val item = listing!!

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
                    // ── 图片区域 ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        if (!item.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = item.photoUrl,
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
                                    modifier = Modifier.size(64.dp),
                                    tint = Color(0xFFD8C4A8)
                                )
                            }
                        }

                        // ── 底部渐变 ──
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
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
                                fontSize = 22.sp,
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
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = item.merchantName,
                                    fontSize = 14.sp,
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
                        // ── "X people viewing" ──
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "👁",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$viewersCount people viewing this now",
                                fontSize = 13.sp,
                                color = TextGrey
                            )
                        }

                        // ── 分类 + Status ──
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = NavSelectedBg
                            ) {
                                Text(
                                    text = item.category,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = OrangeAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (item.status == "available") Color(0xFFE3F5E6) else Color(0xFFFFEDED)
                            ) {
                                Text(
                                    text = if (item.status == "available") "Available" else "Not Available",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = if (item.status == "available") GreenConfirmed else RedCancelled,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // ── Countdown Timer ──
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (countdownText.contains("Expired")) Color(0xFFFFEDED) else Color(0xFFFFF3E0)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (countdownText.contains("Expired")) RedCancelled else OrangeAccent
                                )
                                Column {
                                    Text(
                                        text = if (countdownText.contains("Expired")) "Expired" else "Time remaining",
                                        fontSize = 12.sp,
                                        color = TextGrey
                                    )
                                    Text(
                                        text = countdownText,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (countdownText.contains("Expired")) RedCancelled else OrangeAccent
                                    )
                                    Text(
                                        text = "Deadline: $deadlineText",
                                        fontSize = 12.sp,
                                        color = TextGrey
                                    )
                                }
                            }
                        }

                        // ── 信息卡片 ──
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
                                InfoRow(
                                    Icons.Default.Category,
                                    "Remaining",
                                    "${item.remainingQuantity} ${item.unit ?: "units"}".ifBlank { "N/A" }
                                )
                                InfoRow(Icons.Default.LocationOn, "Address", item.address.ifBlank { "N/A" })
                                if (!item.portionInfo.isNullOrBlank()) {
                                    InfoRow(Icons.Default.Restaurant, "Portion Info", item.portionInfo)
                                }
                            }
                        }

                        if (item.description.isNullOrBlank()) {
                            Text(
                                text = "About this donation",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(text = item.description?: "", fontSize = 14.sp, color = TextDark)
                        }

                        // ── ✅ 商家备注（有背景色） ──
                        if (!item.notes.isNullOrBlank()) {
                            Text(
                                text = "Merchant's Note",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF8EE)  // 浅橙色背景
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Text(
                                    text = item.notes,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                            }
                        }

                        Text(
                            text = "Pickup Location",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        // ── Map ──
                        if (item.latitude != null && item.longitude != null &&
                            item.latitude != 0.0 && item.longitude != 0.0
                        ) {
                            val location = LatLng(item.latitude, item.longitude)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(location, 15f)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    uiSettings = MapUiSettings(
                                        zoomControlsEnabled = true,
                                        myLocationButtonEnabled = false
                                    ),
                                    properties = MapProperties(isMyLocationEnabled = false)
                                ) {
                                    Marker(
                                        state = MarkerState(position = location),
                                        title = item.merchantName,
                                        snippet = item.address,
                                        onClick = {
                                            openFoodDetailNavigation(context, item.latitude, item.longitude, item.merchantName)
                                            true
                                        }
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.92f)
                                ) {
                                    Text(
                                        item.merchantName,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        maxLines = 1
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                        .clickable {
                                            openFoodDetailNavigation(context, item.latitude, item.longitude, item.merchantName)
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = OrangeAccent
                                ) {
                                    Text(
                                        "Tap to navigate",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ── 底部固定 Reserve 按钮 ──
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = BgCream,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { onReserveClick(listingId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = item.status == "available" && item.remainingQuantity > 0 && !hasActiveReservation  // ✅ 添加条件
                    ) {
                        Text(
                            text = when {
                                item.status != "available" -> "Not Available"
                                item.remainingQuantity <= 0 -> "Fully Reserved"
                                hasActiveReservation -> "Already Reserved"
                                else -> "Reserve Now"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
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

private fun parseFoodDetailDeadline(deadline: String): Date? {
    if (deadline.isBlank()) return null

    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )

    formats.forEach { format ->
        try {
            format.isLenient = false
            return format.parse(deadline)
        } catch (_: Exception) {
        }
    }
    return null
}

private fun formatFoodDetailDeadline(deadline: String): String {
    val parsed = parseFoodDetailDeadline(deadline) ?: return deadline.ifBlank { "N/A" }
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(parsed)
}

private fun openFoodDetailNavigation(
    context: android.content.Context,
    lat: Double?,
    lng: Double?,
    merchantName: String
) {
    val latitude = lat ?: 3.1390
    val longitude = lng ?: 101.6869
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(merchantName)})")
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}
