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
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun MerchantDashboardScreen(
    paddingValues: PaddingValues,
    onAddDonationClick: () -> Unit,
    viewModel: MerchantViewModel = viewModel()
) {
    val listings by viewModel.listings.collectAsState()
    val requests by viewModel.requests.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMockData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Stats Card
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

            // 2. Section Title
            item {
                Text("Your Active Donations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // 3. Mock Listings
            items(listings) { listing ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp))
                        )
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

            // 4. Requests Title
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Charity Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // 5. Mock Requests
            items(requests) { request ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFFE8E8), RoundedCornerShape(50))
                            )
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
                                onClick = { },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text("Decline")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { },
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