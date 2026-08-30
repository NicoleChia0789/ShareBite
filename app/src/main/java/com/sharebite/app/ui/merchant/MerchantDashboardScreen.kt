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
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent
import androidx.compose.foundation.layout.PaddingValues

// How many items each dashboard section shows before the rest are hidden
// behind "View All".
private const val SECTION_PREVIEW_LIMIT = 2

@Composable
fun MerchantDashboardScreen(
    paddingValues: PaddingValues,
    onAddDonationClick: () -> Unit,
    onViewAllListingsClick: () -> Unit,
    onViewAllRequestsClick: () -> Unit,
    onViewAllReservationsClick: () -> Unit,
    viewModel: MerchantViewModel   // no default: must be passed in from AppNavHost (shared instance)
) {
    val listings by viewModel.listings.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()

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

            // 2. Listings (capped preview)
            item { Text("Your Active Donations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(listings.take(SECTION_PREVIEW_LIMIT)) { listing ->
                ListingCard(listing)
            }
            if (listings.isEmpty()) {
                item { EmptySectionHint("No active donations yet.") }
            } else {
                item { ViewAllLink(onClick = onViewAllListingsClick) }
            }

            // 3. Charity Requests (capped preview)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Charity Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(requests.take(SECTION_PREVIEW_LIMIT)) { request ->
                CharityRequestCard(request)
            }
            if (requests.isEmpty()) {
                item { EmptySectionHint("No charity requests yet.") }
            } else {
                item { ViewAllLink(onClick = onViewAllRequestsClick) }
            }

            // 4. User Reservations (capped preview)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("User Reservations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(reservations.take(SECTION_PREVIEW_LIMIT)) { reservation ->
                ReservationCard(reservation)
            }
            if (reservations.isEmpty()) {
                item { EmptySectionHint("No reservations yet.") }
            } else {
                item { ViewAllLink(onClick = onViewAllReservationsClick) }
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

/**
 * "View All" link shown under a section's item cards.
 * Shown whenever the section has at least 1 item — even just one — so
 * tapping it always takes the user to that section's full list screen.
 */
@Composable
private fun ViewAllLink(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "View All",
            style = MaterialTheme.typography.labelLarge,
            color = OrangeAccent,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { onClick() }
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptySectionHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray
    )
}