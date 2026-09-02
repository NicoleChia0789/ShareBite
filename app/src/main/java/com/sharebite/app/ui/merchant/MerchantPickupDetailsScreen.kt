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
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // ✅ 食物名作为标题
                        Text(request.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("From: ${request.organisationName}", color = Color.Gray)
                        Text("Quantity: ${request.quantity}", color = Color.Gray)
                        Text("Pickup Date: ${request.pickupDate}", color = Color.Gray)
                        Text("Pickup Time: ${request.pickupTime}", color = Color.Gray)
                        Text("Status: Approved", color = OrangeAccent)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

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
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // ✅ 食物名作为标题
                        Text(reservation.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Reserved by: ${reservation.userId}", color = Color.Gray)
                        Text("Quantity: ${reservation.quantityNum} × ${reservation.unit}", color = Color.Gray)
                        Text("Pickup Date: ${reservation.pickupDate}", color = Color.Gray)
                        Text("Pickup Time: ${reservation.pickupTime}", color = Color.Gray)
                        Text("Status: Approved", color = OrangeAccent)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

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