package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.repository.SavedListingRepository
import com.sharebite.app.ui.theme.*

@Composable
fun SavedItemsScreen(
    paddingValues: PaddingValues,
    onItemClick: (Long) -> Unit
) {
    // ⚠️ 临时 userId，后续替换为真实 Charity 用户 ID
    val userId = "charity-temp-id"

    val savedRepository = remember { SavedListingRepository() }
    val listingRepository = remember { FoodListingRepository() }

    var savedListings by remember { mutableStateOf<List<FoodListing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            // ✅ 传入 userId
            val savedIds = savedRepository.getSavedListingIds(userId)
            savedListings = savedIds.mapNotNull { id ->
                try {
                    listingRepository.getListingById(id)
                } catch (e: Exception) {
                    null // 万一那笔食物已经被删除了，就跳过，不影响其他项目显示
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Saved Items", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Donations you've saved for later.", fontSize = 13.sp, color = TextGrey)
        }

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
            savedListings.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = TextGrey,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No saved items yet.", color = TextGrey)
                        Text("Tap the heart icon on a food detail page to save it here.", fontSize = 12.sp, color = TextGrey)
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedListings) { listing ->
                        SavedItemCard(listing = listing, onClick = { onItemClick(listing.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedItemCard(listing: FoodListing, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFEFE6D9), shape = RoundedCornerShape(10.dp))
            ) {
                if (!listing.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = listing.photoUrl,
                        contentDescription = listing.foodName,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent, shape = RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(listing.foodName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(listing.merchantName, fontSize = 12.sp, color = TextGrey)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextGrey,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${listing.distanceKm}km", fontSize = 11.sp, color = TextGrey)
                }
            }
        }
    }
}