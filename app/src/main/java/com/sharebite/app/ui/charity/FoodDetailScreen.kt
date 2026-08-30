package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.repository.SavedListingRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.components.SimpleMapView
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.widget.Toast

@Composable
fun FoodDetailScreen(
    listingId: Long,
    paddingValues: PaddingValues,
    onApplyClick: (Long) -> Unit
) {
    val repository = remember { FoodListingRepository() }
    val savedRepository = remember { SavedListingRepository() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // ✅ 从 CurrentUser 获取 userId
    val userId = CurrentUser.user?.id ?: ""

    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(listingId) {
        try {
            listing = repository.getListingById(listingId)
            // ✅ 传入 userId 和 listingId
            listing?.let {
                isSaved = savedRepository.isSaved(userId, it.id)
            }
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
                                "${data.quantity} available",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {

                        // ---------- REAL MAP (OpenStreetMap) ----------
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    val lat = data.latitude ?: 3.1390
                                    val lng = data.longitude ?: 101.6869
                                    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(data.merchantName)})")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                }
                        ) {
                            SimpleMapView(
                                latitude = data.latitude ?: 3.1390,
                                longitude = data.longitude ?: 101.6869,
                                markerTitle = data.merchantName,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = OrangeAccent
                            ) {
                                Text(
                                    "Tap to navigate",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .widthIn(max = 200.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.92f)
                            ) {
                                Text(
                                    data.address ?: "",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    fontSize = 10.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.92f)
                            ) {
                                Text(
                                    "${data.distanceKm} km",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    fontSize = 10.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ---------- TITLE + VERIFIED ----------
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

                        // ---------- DETAIL CARD ----------
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    DetailStat(label = "Quantity", value = data.quantity, modifier = Modifier.weight(1f))
                                    DetailStat(label = "Collect by", value = data.deadline, modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    DetailStat(label = "Distance", value = "${data.distanceKm} km", modifier = Modifier.weight(1f))
                                    Row(
                                        modifier = Modifier.weight(1f),
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

                        // ---------- MERCHANT NOTE ----------
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

                        // ---------- ABOUT THIS DONATION ----------
                        Text("About this donation", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(data.description, fontSize = 13.sp, color = TextGrey, lineHeight = 19.sp)

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

                        // ---------- BUTTONS ----------
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

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        if (isSaved) {
                                            // ✅ 传入 userId 和 listingId
                                            savedRepository.unsaveListing(userId, data.id)
                                            isSaved = false
                                            Toast.makeText(context, "Removed from saved.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            savedRepository.saveListing(userId, data.id)
                                            isSaved = true
                                            Toast.makeText(context, "Saved to your list!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isSaved) RedCancelled else TextDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isSaved) "Saved" else "Save for later",
                                color = if (isSaved) RedCancelled else TextDark,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ---------- IMPACT CARD ----------
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