//package com.sharebite.app.ui.merchant
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.sharebite.app.ui.theme.BgCream
//import com.sharebite.app.ui.theme.OrangeAccent
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun MerchantListingDetailScreen(
//    paddingValues: PaddingValues,
//    listingId: Long,
//    viewModel: MerchantViewModel,
//    onBack: () -> Unit
//) {
//    val listings by viewModel.listings.collectAsState()
//    val listing = listings.find { it.id == listingId }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(paddingValues)
//            .background(BgCream)
//            .padding(16.dp)
//    ) {
//        Text("Donation Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
//
//        if (listing != null) {
//            // Image Placeholder
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .background(Color(0xFFEEE5DB), RoundedCornerShape(16.dp)),
//                contentAlignment = androidx.compose.ui.Alignment.Center
//            ) {
//                Text("📷", fontSize = 48.sp)
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Card(
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(listing.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text("Category: ${listing.category}", color = Color.Gray)
//                    Text("Quantity: ${listing.quantity}", color = Color.Gray)
//                    Text("Pickup Date: ${listing.pickupDate}", color = Color.Gray)
//                    Text("Pickup Time: ${listing.deadline}", color = Color.Gray)
//                    Text("Status: ${listing.status}", color = OrangeAccent)
//                    if (!listing.notes.isNullOrEmpty()) {
//                        Text("Notes: ${listing.notes}", color = Color.Gray)
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            Button(
//                onClick = onBack,
//                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Back to Listings", color = Color.White)
//            }
//        } else {
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("Listing not found.", color = Color.Gray)
//        }
//    }
//}

package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
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
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent



@Composable
fun MerchantListingDetailScreen(
    paddingValues: PaddingValues,
    listingId: Long,
    viewModel: MerchantViewModel,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val listings by viewModel.listings.collectAsState()
    val listing = listings.find { it.id == listingId }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Back Button ──
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        if (listing != null) {
            // ── Large Photo ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0E8)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (listing.photoUrl != null) {
                    AsyncImage(
                        model = listing.photoUrl,
                        contentDescription = listing.foodName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📷", fontSize = 64.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Details Card ──
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Food name + status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            listing.foodName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF1E1B16)
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (listing.status == "available") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                if (listing.status == "available") "Active" else "Inactive",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (listing.status == "available") Color(0xFF2E7D32) else Color.Red
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Details rows
                    DetailRow("Category", listing.category)
                    DetailRow("Quantity", "${listing.remainingQuantity} units left")
                    DetailRow("Total Quantity", "${listing.totalQuantity} units")
                    DetailRow("Pickup Date", listing.pickupDate)
                    DetailRow("Pickup Time", listing.deadline)
                    DetailRow("Address", listing.address)
                    if (!listing.notes.isNullOrBlank()) {
                        DetailRow("Notes", listing.notes)
                    }
                    if (!listing.description.isNullOrBlank()) {
                        DetailRow("Description", listing.description)
                    }
                    DetailRow("Status", if (listing.status == "available") "Active" else "Inactive")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Action Buttons ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Edit Button
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Delete Button
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Back button (alternative)
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Listings", color = Color.Gray)
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Listing not found.", color = Color.Gray)
            }
        }
    }

    // ── Delete Confirmation Dialog ──
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove listing?") },
            text = { Text("This action cannot be undone. The listing will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteListing(listingId)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Remove", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E1B16)
            )
        }
    }
}

