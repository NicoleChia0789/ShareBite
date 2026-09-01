package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

@Composable
fun MerchantDashboardScreen(
    paddingValues: PaddingValues,
    onAddDonationClick: () -> Unit,
    onViewAllListings: () -> Unit,
    onViewAllRequests: () -> Unit,
    onViewAllReservations: () -> Unit,
    onViewImpact: () -> Unit,
    onRequestClick: (Long) -> Unit,
    onReservationClick: (Long) -> Unit,
    viewModel: MerchantViewModel
){
    val listings by viewModel.listings.collectAsState()
    val generalRequests by viewModel.generalRequests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val username = CurrentUser.getUserName()

    LaunchedEffect(generalRequests) {
        println("🔍 UI - General Requests count: ${generalRequests.size}")
        generalRequests.forEach { request ->
            println("   - ${request.foodName} | status: ${request.status}")
        }
    }

    LaunchedEffect(Unit) {

        viewModel.loadGeneralRequests()
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Welcome Header
                item {
                    Column {
                        Text(
                            "Welcome, $username! 👋",
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

                // 2b. View Impact Report
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewImpact() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFFFF1E8), RoundedCornerShape(50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📊", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Impact Report", fontWeight = FontWeight.Bold)
                                    Text(
                                        "See your lifetime donations & badges",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Text("View →", color = OrangeAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 3. Active Donations
                if (listings.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your Active Donations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = onViewAllListings) {
                                Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    items(listings.take(2)) { listing ->
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

                // 4. General Charity Requests (from charities, no specific merchant)
                if (generalRequests.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🍽️ General Food Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = onViewAllRequests) {
                                Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    items(generalRequests.take(2)) { request ->
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
                                        Text("${request.quantity} x ${request.foodName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        request.status,
                                        color = if (request.status == "Pending") OrangeAccent else Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { onRequestClick(request.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
                                ) {
                                    Text("View Details")
                                }
                            }
                        }
                    }
                }

                // 5. User Reservations
                if (reservations.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("User Reservations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = onViewAllReservations) {
                                Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    items(reservations.take(2)) { reservation ->
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
                                    Text(
                                        reservation.status,
                                        color = if (reservation.status == "Pending") OrangeAccent else Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { onReservationClick(reservation.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
                                ) {
                                    Text("View Details")
                                }
                            }
                        }
                    }
                }

                // Empty state
                if (listings.isEmpty() && generalRequests.isEmpty() && reservations.isEmpty()) {
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