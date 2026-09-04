package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.data.model.FoodListing
import java.text.NumberFormat
import java.util.*
import com.sharebite.app.util.formatDateTimeForDisplay

@Composable
fun MerchantAllListingsScreen(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onListingClick: (Long) -> Unit,
    onAddDonationClick: () -> Unit
) {
    val listings by viewModel.listings.collectAsState()
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var deadlineOrder by rememberSaveable { mutableStateOf("Ascending") }
    val listingListState = rememberLazyListState()

    val filteredListings = remember(listings, deadlineOrder) {
        if (deadlineOrder == "Descending") listings.sortedByDescending { it.deadline }
        else listings.sortedBy { it.deadline }
    }

    LaunchedEffect(Unit) {
        viewModel.loadMerchantData()
    }

    LaunchedEffect(deadlineOrder) {
        listingListState.scrollToItem(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLandscape) {
            Row(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(260.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Manage your community food contributions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text("Filter by deadline", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    FilterChip(
                        selected = deadlineOrder == "Ascending",
                        onClick = { deadlineOrder = "Ascending" },
                        label = { Text("Deadline ↑") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChip(
                        selected = deadlineOrder == "Descending",
                        onClick = { deadlineOrder = "Descending" },
                        label = { Text("Deadline ↓") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ListingResults(
                    listings = filteredListings,
                    listState = listingListState,
                    onListingClick = onListingClick,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Manage your community food contributions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text("Filter by deadline", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = deadlineOrder == "Ascending", onClick = { deadlineOrder = "Ascending" }, label = { Text("Deadline ↑") })
                        FilterChip(selected = deadlineOrder == "Descending", onClick = { deadlineOrder = "Descending" }, label = { Text("Deadline ↓") })
                    }
                }
                ListingResults(
                    listings = filteredListings,
                    listState = listingListState,
                    onListingClick = onListingClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ✅ Floating Action Button – Add Donation
        FloatingActionButton(
            onClick = onAddDonationClick,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Donation", tint = Color.White)
        }
    }
}

@Composable
private fun ListingResults(
    listings: List<FoodListing>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onListingClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (listings.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No listings found.", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text("Tap the + button to add your first donation.", color = Color.Gray, fontSize = 14.sp)
            }
        }
    } else {
        Column(modifier) {
            Text(
                "YOUR ACTIVE DONATIONS (${listings.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listings, key = { it.id }) { listing ->
                    ListingCardSimplified(listing = listing, onClick = { onListingClick(listing.id) })
                }
            }
        }
    }
}

// ─── Simplified Listing Card ──────────────────────────────────────

@Composable
fun ListingCardSimplified(
    listing: FoodListing,
    onClick: () -> Unit
) {
    val remainingText = "${NumberFormat.getNumberInstance(Locale.US).format(listing.remainingQuantity)} units left"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Photo ──
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F0E8)),
                    contentAlignment = Alignment.Center
                ) {
                    if (listing.photoUrl != null) {
                        AsyncImage(
                            model = listing.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Fastfood,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // ── Content ──
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        listing.foodName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Deadline:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(formatDateTimeForDisplay(listing.deadline), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            listing.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(remainingText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // ── Description (truncated) ──
            if (!listing.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    listing.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── View Details button ──
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("View Details →", fontWeight = FontWeight.Medium)
            }
        }
    }
}

