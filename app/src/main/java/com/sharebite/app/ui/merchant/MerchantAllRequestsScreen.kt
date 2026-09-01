package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

@Composable
fun MerchantAllRequestsScreen(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onRequestClick: (Long) -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    var selectedFilter by remember { mutableStateOf("Pending") }

    val filteredRequests = when (selectedFilter) {
        "Approved" -> requests.filter { it.status == "Confirmed" }
        "Declined" -> requests.filter { it.status == "Cancelled" }
        else -> requests.filter { it.status == "Pending" }
    }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(BgCream)) {
        Text("Charity Requests", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedFilter == "Pending", onClick = { selectedFilter = "Pending" }, label = { Text("Pending") })
            FilterChip(selected = selectedFilter == "Approved", onClick = { selectedFilter = "Approved" }, label = { Text("Approved") })
            FilterChip(selected = selectedFilter == "Declined", onClick = { selectedFilter = "Declined" }, label = { Text("Declined") })
        }

        if (filteredRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No charity requests in this category.", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredRequests) { request ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFFFE8E8), RoundedCornerShape(50)))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(request.organisationName, fontWeight = FontWeight.Bold)
                                    Text("${request.quantity} x ${request.foodName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(request.status, color = if (request.status == "Pending") OrangeAccent else Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { onRequestClick(request.id) }, colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent), modifier = Modifier.fillMaxWidth()) {
                                Text("View Details", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}