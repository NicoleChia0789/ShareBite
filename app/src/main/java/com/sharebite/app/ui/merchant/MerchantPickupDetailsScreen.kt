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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MerchantPickupDetailsScreen(
    paddingValues: PaddingValues,
    pickupItemId: String,
    viewModel: MerchantViewModel,
    onBack: () -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val usersById by viewModel.reservationUsers.collectAsState()

    val idParts = remember(pickupItemId) { pickupItemId.split("_", limit = 2) }
    val itemType = idParts.getOrNull(0)
    val itemId = idParts.getOrNull(1)?.toLongOrNull()

    fun isOverdue(date: String, time: String): Boolean {
        val patterns = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd h:mm a")
        return patterns.any { pattern ->
            try {
                val scheduled = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
                    .parse("${date.trim()} ${time.trim()}")
                scheduled != null && scheduled.before(Date())
            } catch (_: Exception) {
                false
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.loadMerchantData() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
            .padding(16.dp)
    ) {
        Text("Pickup Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (itemId == null || itemType !in setOf("req", "res")) {
            Text("Invalid pickup reference", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
        } else if (itemType == "req") {
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
                        Text("Status: ${request.status}", color = OrangeAccent)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (request.status.equals("Confirmed", ignoreCase = true) &&
                    !isOverdue(request.pickupDate, request.pickupTime)
                ) {
                    Button(
                        onClick = {
                            viewModel.updateRequestStatus(itemId, "Completed")
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Picked Up", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else if (request.status.equals("Confirmed", ignoreCase = true)) {
                    Text("Overdue pickup. This record remains Confirmed until it is handled.", color = Color(0xFFC62828))
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
                        val userName = usersById[reservation.userId]?.name
                            ?.takeIf { it.isNotBlank() }
                            ?: "Individual user"
                        Text("Reserved by: $userName", color = Color.Gray)
                        Text("Quantity: ${reservation.quantityNum} × ${reservation.unit}", color = Color.Gray)
                        Text("Pickup Date: ${reservation.pickupDate}", color = Color.Gray)
                        Text("Pickup Time: ${reservation.pickupTime}", color = Color.Gray)
                        Text("Status: ${reservation.status}", color = OrangeAccent)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (reservation.status.equals("Confirmed", ignoreCase = true) &&
                    !isOverdue(reservation.pickupDate, reservation.pickupTime)
                ) {
                    Button(
                        onClick = {
                            viewModel.updateReservationStatus(itemId, "Completed")
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Picked Up", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else if (reservation.status.equals("Confirmed", ignoreCase = true)) {
                    Text("Overdue pickup. This record remains Confirmed until it is handled.", color = Color(0xFFC62828))
                }
            }
        }
    }
}
