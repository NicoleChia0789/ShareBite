package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.data.session.CurrentUser
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun MerchantDashboardScreen(
    paddingValues: PaddingValues,
    onAddDonationClick: () -> Unit,
    viewModel: MerchantViewModel = viewModel()
) {
    val listings by viewModel.listings.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val merchantName = CurrentUser.getUserName()

    // ✅ Load merchant's own data
    LaunchedEffect(Unit) {
        viewModel.loadMerchantData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Welcome Header
                item {
                    Column {
                        Text(
                            "Welcome, $merchantName! 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B16)
                        )
                        Text(
                            "Manage your donations and requests.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                // 2. Stats Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E8)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("TOTAL IMPACT", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("1,240", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Text("LBS DIVERTED", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        }
                    }
                }

                // 3. Active Donations (Merchant's own listings)
                if (listings.isNotEmpty()) {
                    item {
                        Text("Your Active Donations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(listings) { listing ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp)))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(listing.foodName, fontWeight = FontWeight.SemiBold)
                                    Text("${listing.quantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
                                }
                                OutlinedButton(onClick = {}, modifier = Modifier.height(36.dp)) {
                                    Text("View")
                                }
                            }
                        }
                    }
                }

                // 4. Charity Requests (Merchant's own pending requests)
                if (requests.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Charity Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(requests) { request ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(40.dp).background(Color(0xFFFFE8E8), RoundedCornerShape(50)))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(request.organisationName, fontWeight = FontWeight.Bold)
                                        Text("Requesting: ${request.quantity} x ${request.foodName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(request.status, color = if (request.status == "Pending") OrangeAccent else Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { viewModel.updateRequestStatus(request.id, "Cancelled") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                    ) {
                                        Text("Decline")
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = { viewModel.updateRequestStatus(request.id, "Confirmed") },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Approve", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. User Reservations (Merchant's own reservations)
                if (reservations.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("User Reservations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(reservations) { reservation ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(40.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(50)))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("User: ${reservation.userId.take(8)}", fontWeight = FontWeight.Bold)
                                        Text("Reserved: ${reservation.quantity} x ${reservation.foodName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text("${reservation.pickupDate} at ${reservation.pickupTime}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(reservation.status, color = if (reservation.status == "Pending") OrangeAccent else Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.updateReservationStatus(reservation.id, "Confirmed") },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Confirm Pickup", color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Empty state
                if (listings.isEmpty() && requests.isEmpty() && reservations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No donations or requests yet.", color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap the + button to add your first donation.", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // FAB Button
        FloatingActionButton(
            onClick = onAddDonationClick,
            containerColor = OrangeAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Donation", tint = Color.White)
        }
    }
}