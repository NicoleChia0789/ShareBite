package com.sharebite.app.ui.charity

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
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
import com.sharebite.app.ui.theme.*
import com.sharebite.app.util.formatDateTimeForDisplay
import kotlinx.coroutines.launch

@Composable
fun FoodDetailScreen(
    listingId: Long,
    paddingValues: PaddingValues,
    onApplyClick: (Long) -> Unit
) {
    val repository = remember { FoodListingRepository() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(listingId) {
        try {
            listing = repository.getListingById(listingId)
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $errorMessage", color = RedCancelled)
                }
            }
            listing == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Listing not found.", color = TextGrey)
                }
            }
            else -> {
                val data = listing!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // ---------- PHOTO HEADER ----------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFFEFE6D9))
                    ) {
                        if (!data.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = data.photoUrl,
                                contentDescription = data.foodName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color(0xFFD8C4A8),
                                modifier = Modifier
                                    .size(56.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.9f)
                        ) {
                            Text(
                                "${data.totalQuantity} ${data.unit ?: ""} available",  // ✅ 新字段
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {

                        // ---------- GOOGLE MAP ----------
                        val location = LatLng(
                            data.latitude ?: 3.1390,
                            data.longitude ?: 101.6869
                        )
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
                                    title = data.merchantName,
                                    snippet = data.address,
                                    onClick = {
                                        openNavigation(context, data.latitude, data.longitude, data.merchantName)
                                        true
                                    }
                                )
                            }

                            // ---------- 左上角：店名 ----------
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.92f)
                            ) {
                                Text(
                                    data.merchantName,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    maxLines = 1
                                )
                            }

                            // ---------- 左下角：导航按钮 ----------
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .clickable {
                                        openNavigation(context, data.latitude, data.longitude, data.merchantName)
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // ---------- 其余内容（TITLE, DETAIL CARD, etc.）保持不变 ----------
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(data.foodName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text(data.merchantName, fontSize = 13.sp, color = TextGrey)
                            }
                            Text("Free", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenConfirmed)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    DetailStat(label = "Quantity", value = "${data.totalQuantity} ${data.unit ?: ""}", modifier = Modifier.weight(1f))
                                    DetailStat(label = "Collect by", value = formatDateTimeForDisplay(data.pickupTime ?: data.deadline), modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = GreenConfirmed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Verified", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenConfirmed)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!data.notes.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = OrangeAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Merchant Note", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(data.notes, fontSize = 13.sp, color = TextGrey)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text("About this donation", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(data.description ?: "", fontSize = 13.sp, color = TextGrey, lineHeight = 19.sp)

                        if (!data.portionInfo.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Portion size", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(data.portionInfo, fontSize = 13.sp, color = TextGrey)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoTag("Zero Waste")
                            InfoTag(data.category)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { onApplyClick(data.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply for this food", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F2))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Every request helps reduce food waste and supports your community.",
                                    fontSize = 12.sp,
                                    color = TextDark,
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { 0.65f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = GreenConfirmed,
                                    trackColor = Color(0xFFDCEEDD)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// ---------- 辅助函数 ----------
private fun openNavigation(context: android.content.Context, lat: Double?, lng: Double?, merchantName: String) {
    val latitude = lat ?: 3.1390
    val longitude = lng ?: 101.6869
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(merchantName)})")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

@Composable
private fun DetailStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = TextGrey)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
private fun InfoTag(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF1F8F2)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 11.sp,
            color = GreenConfirmed,
            fontWeight = FontWeight.Bold
        )
    }
}
