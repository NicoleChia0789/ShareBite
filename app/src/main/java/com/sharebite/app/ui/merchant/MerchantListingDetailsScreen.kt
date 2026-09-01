package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent
import androidx.compose.ui.unit.sp

@Composable
fun MerchantListingDetailScreen(
    paddingValues: PaddingValues,
    listingId: Long,
    viewModel: MerchantViewModel,
    onBack: () -> Unit
) {
    val listings by viewModel.listings.collectAsState()
    val listing = listings.find { it.id == listingId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
            .padding(16.dp)
    ) {
        Text("Donation Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (listing != null) {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFEEE5DB), RoundedCornerShape(16.dp)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("📷", fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(listing.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Category: ${listing.category}", color = Color.Gray)
                    Text("Quantity: ${listing.quantity}", color = Color.Gray)
                    Text("Pickup Date: ${listing.pickupDate}", color = Color.Gray)
                    Text("Pickup Time: ${listing.deadline}", color = Color.Gray)
                    Text("Status: ${listing.status}", color = OrangeAccent)
                    if (!listing.notes.isNullOrEmpty()) {
                        Text("Notes: ${listing.notes}", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Listings", color = Color.White)
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Listing not found.", color = Color.Gray)
        }
    }
}

