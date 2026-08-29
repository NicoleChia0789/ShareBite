package com.sharebite.app.ui.user

import android.content.Intent
import android.net.Uri
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

    val remainingHours = remember(listing) {
        listing?.let {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val deadline = format.parse(it.deadline)
                val now = Date()
                if (deadline != null && deadline.after(now)) {
                    val diff = deadline.time - now.time
                    (diff / (1000 * 60 * 60)).toInt()
                } else 0
            } catch (e: Exception) {
                0
            }
        } ?: 0
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            CircularProgressIndicator(color = OrangeAccent)
        }

        errorMessage != null -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            Text(text = "Error: $errorMessage", color = RedCancelled)
        }

        listing == null -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            Text(text = "Food not found", color = TextGrey)
        }

        else -> {
            val item = listing!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgCream)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                if (!item.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.photoUrl,
                        contentDescription = item.foodName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color(0xFFEFE6D9)),
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (remainingHours > 0 && remainingHours <= 24) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEDED)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = RedCancelled
                                )
                                Text(
                                    text = "Expires in $remainingHours hour${if (remainingHours > 1) "s" else ""}",
                                    fontSize = 14.sp,
                                    color = RedCancelled,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = item.foodName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = TextGrey
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.merchantName, fontSize = 16.sp, color = TextDark)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(50), color = NavSelectedBg) {
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

                    if (!item.notes.isNullOrBlank()) {
                        Text(
                            text = "Merchant's Note",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(text = item.notes, fontSize = 14.sp, color = TextGrey)
                    }

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

                        Button(
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = OrangeAccent
                            ),
                            shape = RoundedCornerShape(50),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
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

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F2))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = GreenConfirmed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Every request helps reduce food waste",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By picking up this donation, you're supporting your community and the planet.",
                                fontSize = 12.sp,
                                color = TextGrey
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onReserveClick(listingId) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
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
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
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
        Text(text = label, fontSize = 13.sp, color = TextGrey, modifier = Modifier.width(100.dp))
        Text(text = value, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
    }
}