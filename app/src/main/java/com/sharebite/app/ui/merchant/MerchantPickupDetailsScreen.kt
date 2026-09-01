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

@Composable
fun MerchantPickupDetailsScreen(
    paddingValues: PaddingValues,
    pickupItemId: String,
    viewModel: MerchantViewModel,
    onBack: () -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()

    val itemType = pickupItemId.split("_")[0]
    val itemId = pickupItemId.split("_")[1].toLong()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
            .padding(16.dp)
    ) {
        Text("Pickup Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (itemType == "req") {
            val request = requests.find { it.id == itemId }
            if (request != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(request.organisationName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Item: ${request.quantity} x ${request.foodName}", color = Color.Gray)
                        Text("Status: Approved", color = OrangeAccent)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ✅ Confirm Pickup Button for Approved Requests
                Button(
                    onClick = {
                        viewModel.updateRequestStatus(itemId, "Completed")
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Pickup", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            val reservation = reservations.find { it.id == itemId }
            if (reservation != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("User: ${reservation.userId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Item: ${reservation.quantity} x ${reservation.foodName}", color = Color.Gray)
                        Text("Pickup: ${reservation.pickupDate} at ${reservation.pickupTime}", color = Color.Gray)
                        Text("Status: Approved", color = OrangeAccent)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ✅ Confirm Pickup Button for Approved Reservations
                Button(
                    onClick = {
                        viewModel.updateReservationStatus(itemId, "Completed")
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Pickup", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
