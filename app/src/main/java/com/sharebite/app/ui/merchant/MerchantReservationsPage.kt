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
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

@Composable
fun MerchantReservationsPage(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onViewAllRequests: () -> Unit,
    onViewAllReservations: () -> Unit,
    onPickupDetailsClick: (String) -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()

    // ONLY show items that are Confirmed/Approved (ready for pickup)
    val confirmedRequests = requests.filter { it.status == "Confirmed" }
    val confirmedReservations = reservations.filter { it.status == "Confirmed" }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(BgCream)) {
        Text("Pickup Requests", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(confirmedRequests) { request ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(request.organisationName, fontWeight = FontWeight.Bold)
                        Text("${request.quantity} x ${request.foodName}", color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { onPickupDetailsClick("req_${request.id}") }, colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent), modifier = Modifier.fillMaxWidth()) {
                            Text("View Pickup Details", color = Color.White)
                        }
                    }
                }
            }

            items(confirmedReservations) { reservation ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("User: ${reservation.userId.take(8)}", fontWeight = FontWeight.Bold)
                        Text("${reservation.quantity} x ${reservation.foodName}", color = Color.Gray)
                        Text("Pickup: ${reservation.pickupDate} at ${reservation.pickupTime}", color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { onPickupDetailsClick("res_${reservation.id}") }, colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent), modifier = Modifier.fillMaxWidth()) {
                            Text("View Pickup Details", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}