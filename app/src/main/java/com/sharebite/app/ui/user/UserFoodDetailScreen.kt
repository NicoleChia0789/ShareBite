package com.sharebite.app.ui.user

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.sharebite.app.data.repository.SavedListingRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
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

    // ⚠️ 临时硬编码测试用户 ID，之后换成 CurrentUser.user?.id ?: ""
    val userId = "258c6924-0bdd-481f-a6d8-cdafb4e7abe3"

    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var isSaved by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isDataLoaded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val savedRepository = remember { SavedListingRepository() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listingId) {
        if (!isDataLoaded || listing?.id != listingId) {
            try {
                listing = repository.getListingById(listingId)
                isDataLoaded = true
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    // 加载收藏状态
    LaunchedEffect(listing?.id) {
        listing?.let {
            try {
                isSaved = savedRepository.isSaved(userId, it.id)
            } catch (e: Exception) {
                // 静默失败
            }
        }
    }

    val countdownText = remember(listing?.deadline) {
        listing?.let {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val deadline = format.parse(it.deadline)
                val now = Date()
                if (deadline != null && deadline.after(now)) {
                    val diff = deadline.time - now.time
                    val hours = (diff / (1000 * 60 * 60)).toInt()
                    val minutes = ((diff % (1000 * 60 * 60)) / (1000 * 60)).toInt()
                    "${hours}h ${minutes}m"
                } else "Expired"
            } catch (e: Exception) {
                "N/A"
            }
        } ?: "N/A"
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

                        // ── 右上角收藏按钮 ──
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(44.dp),
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.85f),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                if (isSaved) {
                                                    savedRepository.unsaveListing(userId, item.id)
                                                    isSaved = false
                                                    Toast.makeText(context, "Removed from saved.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    savedRepository.saveListing(userId, item.id)
                                                    isSaved = true
                                                    Toast.makeText(context, "Saved to your list!", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isSaved) "Saved" else "Save",
                                        tint = if (isSaved) RedCancelled else TextDark,  // ✅ 红色 / 灰色
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // ── 底部渐变文字覆盖 ──
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

                        // 食物名称 + 商家名
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

                        // ── 分类 + Status Badge ──
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
                                InfoRow(Icons.Default.Category, "Quantity", item.quantity.ifBlank { "N/A" })
                                InfoRow(Icons.Default.DateRange, "Pickup Date", item.pickupDate.ifBlank { "N/A" })
                                InfoRow(Icons.Default.Schedule, "Deadline", item.deadline.ifBlank { "N/A" })
                                InfoRow(Icons.Default.LocationOn, "Address", item.address.ifBlank { "N/A" })
                                if (!item.portionInfo.isNullOrBlank()) {
                                    InfoRow(Icons.Default.Restaurant, "Portion Info", item.portionInfo)
                                }
                                if (item.distanceKm > 0) {
                                    InfoRow(Icons.Default.Map, "Distance", "${item.distanceKm} km")
                                }
                            }
                        }

                        if (item.description.isNotBlank()) {
                            Text(
                                text = "About this donation",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(text = item.description, fontSize = 14.sp, color = TextDark)
                        }

                        // ── 商家备注（带左边框） ──
                        if (!item.notes.isNullOrBlank()) {
                            Text(
                                text = "Merchant's Note",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF8EE),
                                border = androidx.compose.foundation.BorderStroke(
                                    4.dp,
                                    OrangeAccent
                                )
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

                        // ── Map ──
                        if (item.latitude != null && item.longitude != null &&
                            item.latitude != 0.0 && item.longitude != 0.0
                        ) {
                            Text(
                                text = "Pickup Location",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            val location = LatLng(item.latitude, item.longitude)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(location, 15f)
                            }

                            GoogleMap(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                cameraPositionState = cameraPositionState,
                                uiSettings = MapUiSettings(
                                    scrollGesturesEnabled = false,
                                    zoomControlsEnabled = false,
                                    zoomGesturesEnabled = false
                                )
                            ) {
                                Marker(
                                    state = MarkerState(position = location),
                                    title = item.foodName,
                                    snippet = item.address
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedButton(
                                onClick = {
                                    val uri = Uri.parse("google.navigation:q=${item.latitude},${item.longitude}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        val browserUri = Uri.parse(
                                            "https://www.google.com/maps/dir/?api=1&destination=${item.latitude},${item.longitude}"
                                        )
                                        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = OrangeAccent
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Get Directions", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                        enabled = item.status == "available"
                    ) {
                        Text(
                            text = if (item.status == "available") "Reserve Now" else "Not Available",
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