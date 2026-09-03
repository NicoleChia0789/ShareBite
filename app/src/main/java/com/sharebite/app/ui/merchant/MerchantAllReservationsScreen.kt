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
fun MerchantAllReservationsScreen(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onReservationClick: (Long) -> Unit
) {
    val reservations by viewModel.reservations.collectAsState()
    val usersById by viewModel.reservationUsers.collectAsState()
    var selectedFilter by remember { mutableStateOf("Pending") }

    val filteredReservations = when (selectedFilter) {
        "Approved" -> reservations.filter { it.status == "Confirmed" }
        "Declined" -> reservations.filter { it.status == "Cancelled" }
        else -> reservations.filter { it.status == "Pending" }
    }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(BgCream)) {
        Text("User Reservations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedFilter == "Pending", onClick = { selectedFilter = "Pending" }, label = { Text("Pending") })
            FilterChip(selected = selectedFilter == "Approved", onClick = { selectedFilter = "Approved" }, label = { Text("Approved") })
            FilterChip(selected = selectedFilter == "Declined", onClick = { selectedFilter = "Declined" }, label = { Text("Declined") })
        }

        if (filteredReservations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No reservations in this category.", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredReservations) { reservation ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(50)))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        usersById[reservation.userId]?.name?.takeIf { it.isNotBlank() }
                                            ?: "Individual user",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("${reservation.quantityNum} x ${reservation.foodName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(reservation.status, color = if (reservation.status == "Pending") OrangeAccent else Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { onReservationClick(reservation.id) }, colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent), modifier = Modifier.fillMaxWidth()) {
                                Text("View Details", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
