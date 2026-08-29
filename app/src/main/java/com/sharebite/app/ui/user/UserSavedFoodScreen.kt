package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.sharebite.app.data.repository.SavedListingRepository
import com.sharebite.app.ui.theme.*

@Composable
fun UserSavedFoodScreen(
    paddingValues: PaddingValues,
    onFoodClick: (Long) -> Unit
) {
    val userId = "258c6924-0bdd-481f-a6d8-cdafb4e7abe3"
    val savedRepo = remember { SavedListingRepository() }
    val foodRepo = remember { FoodListingRepository() }

    var savedListings by remember { mutableStateOf<List<FoodListing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val savedIds = savedRepo.getSavedListingIds(userId)
            savedListings = savedIds.mapNotNull { id ->
                try { foodRepo.getListingById(id) } catch (e: Exception) { null }
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
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Saved Food", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("You have ${savedListings.size} items saved for later.", fontSize = 13.sp, color = TextGrey)
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }

            errorMessage != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Error: $errorMessage", color = RedCancelled)
            }

            savedListings.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextGrey
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No saved food", fontSize = 16.sp, color = TextGrey)
                    Text("Start saving your favorite food!", fontSize = 14.sp, color = TextGrey)
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedListings) { listing ->
                    SavedFoodCard(
                        listing = listing,
                        onClick = { onFoodClick(listing.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedFoodCard(
    listing: FoodListing,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!listing.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = listing.photoUrl,
                    contentDescription = listing.foodName,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEFE6D9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFD8C4A8)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(listing.foodName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(listing.merchantName, fontSize = 13.sp, color = TextGrey)
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = NavSelectedBg) {
                        Text(
                            listing.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = OrangeAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("Ends: ${listing.deadline}", fontSize = 11.sp, color = RedCancelled)
                    if (listing.distanceKm > 0) {
                        Text("· ${listing.distanceKm} km", fontSize = 11.sp, color = TextGrey)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(36.dp),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Saved",
                        modifier = Modifier.size(20.dp),
                        tint = OrangeAccent
                    )
                }
            }
        }
    }
}