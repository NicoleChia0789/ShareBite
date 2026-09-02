package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.ui.theme.OrangeAccent

/**
 * Shared card used for a single FoodListing.
 * Used by both MerchantDashboardScreen (capped preview) and
 * MerchantAllListingsScreen (full list).
 */
@Composable
fun ListingCard(listing: FoodListing) {
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
                Text("${listing.remainingQuantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
            }
            OutlinedButton(onClick = {}, modifier = Modifier.height(36.dp)) { Text("View") }
        }
    }
}

/**
 * Shared card used for a single CharityRequest.
 */
@Composable
fun CharityRequestCard(request: CharityRequest) {
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
        }
    }
}

/**
 * Shared card used for a single Reservation.
 */
@Composable
fun ReservationCard(reservation: Reservation) {
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
                    Text("User: ${reservation.userId}", fontWeight = FontWeight.Bold)
                    Text("Reserved: ${reservation.quantityNum} x ${reservation.foodName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("${reservation.pickupDate} at ${reservation.pickupTime}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(reservation.status, color = if (reservation.status == "Pending") OrangeAccent else Color.Gray)
            }
        }
    }
}

