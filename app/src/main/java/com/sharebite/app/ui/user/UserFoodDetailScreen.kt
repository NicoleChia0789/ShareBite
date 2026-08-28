package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.ui.theme.*

@Composable
fun UserFoodDetailScreen(
    listingId: Long,
    paddingValues: PaddingValues,
    onReserveClick: (Long) -> Unit
) {
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

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        }
        errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $errorMessage", color = RedCancelled)
            }
        }
        listing == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Food not found", color = TextGrey)
            }
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
                // 图片
                if (!item.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.photoUrl,
                        contentDescription = item.foodName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFEFE6D9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍱", fontSize = 64.sp)
                    }
                }

                // 内容
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        item.foodName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏪", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.merchantName, fontSize = 16.sp, color = TextDark)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NavSelectedBg
                        ) {
                            Text(
                                item.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (item.status == "available") Color(0xFFE3F5E6) else Color(0xFFFFEDED)
                        ) {
                            Text(
                                item.status.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = if (item.status == "available") GreenConfirmed else RedCancelled,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 信息卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoRow("📦", "Quantity", item.quantity.ifBlank { "N/A" })
                            InfoRow("📅", "Pickup Date", item.pickupDate.ifBlank { "N/A" })
                            InfoRow("⏰", "Deadline", item.deadline.ifBlank { "N/A" })
                            InfoRow("📍", "Address", item.address.ifBlank { "N/A" })
                            if (!item.portionInfo.isNullOrBlank()) {
                                InfoRow("🍽️", "Portion Info", item.portionInfo)
                            }
                            if (item.distanceKm > 0) {
                                InfoRow("📏", "Distance", "${item.distanceKm} km")
                            }
                        }
                    }

                    if (item.description.isNotBlank()) {
                        Text("Description", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(item.description, fontSize = 14.sp, color = TextDark)
                    }

                    if (!item.notes.isNullOrBlank()) {
                        Text("Notes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(item.notes, fontSize = 14.sp, color = TextGrey)
                    }

                    Button(
                        onClick = { onReserveClick(listingId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = item.status == "available"
                    ) {
                        Text(
                            if (item.status == "available") "Reserve Now" else "Not Available",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = TextGrey, modifier = Modifier.width(100.dp))
        Text(value, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
    }
}