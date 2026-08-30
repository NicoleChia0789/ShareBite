package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*

@Composable
fun UserDashboardScreen(
    paddingValues: PaddingValues,
    onBrowseClick: () -> Unit,
    onFoodClick: (Long) -> Unit,
    onViewAllReservations: () -> Unit
) {
    val userId = CurrentUser.getUserId()
    val foodRepo = remember { FoodListingRepository() }
    val reservationRepo = remember { ReservationRepository() }

    var nearbyListings by remember { mutableStateOf<List<FoodListing>>(emptyList()) }
    var recentReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            nearbyListings = foodRepo.getAvailableListings().take(5)
            recentReservations = reservationRepo.getMyReservations().take(3)
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            CircularProgressIndicator(color = OrangeAccent)
        }
        return
    }

    if (errorMessage != null) {
        Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            Text("Error: $errorMessage", color = RedCancelled)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeAccent)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Need a meal?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Browse available food donations near you.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onBrowseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Browse Food →", color = OrangeAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardStatCard(
                    "Available Now",
                    nearbyListings.size.toString(),
                    OrangeAccent,
                    Modifier.weight(1f)
                )
                DashboardStatCard(
                    "My Reservations",
                    recentReservations.size.toString(),
                    GreenConfirmed,
                    Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Available Near You",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    "View All",
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onBrowseClick() }
                )
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(nearbyListings) { listing ->
                    FoodListingCard(
                        listing = listing,
                        onClick = { onFoodClick(listing.id) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Reservations",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    "View All",
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onViewAllReservations() }
                )
            }
        }

        if (recentReservations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No reservations yet. Start browsing!", color = TextGrey, fontSize = 13.sp)
                }
            }
        }

        items(recentReservations) { reservation ->
            RecentReservationCard(
                reservation = reservation,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun FoodListingCard(listing: FoodListing, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
    ) {
        Column {
            if (!listing.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = listing.photoUrl,
                    contentDescription = listing.foodName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        .background(Color(0xFFEFE6D9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = "Food",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFD8C4A8)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Surface(shape = RoundedCornerShape(50), color = NavSelectedBg) {
                    Text(
                        listing.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(listing.foodName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(listing.merchantName, fontSize = 11.sp, color = TextGrey, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ends: ${listing.deadline}", fontSize = 10.sp, color = RedCancelled)
            }
        }
    }
}

@Composable
private fun RecentReservationCard(reservation: Reservation, modifier: Modifier = Modifier) {
    val statusColor = when (reservation.status) {
        "Confirmed" -> GreenConfirmed
        "Pending" -> OrangeAccent
        "Cancelled" -> RedCancelled
        "Completed" -> GreyCompleted
        else -> TextGrey
    }
    val statusBg = when (reservation.status) {
        "Confirmed" -> Color(0xFFE3F5E6)
        "Pending" -> NavSelectedBg
        "Cancelled" -> Color(0xFFFFEDED)
        "Completed" -> Color(0xFFEEEEEE)
        else -> Color(0xFFEEEEEE)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEFE6D9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = "Food",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFD8C4A8)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(reservation.foodName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Pickup: ${reservation.pickupDate}, ${reservation.pickupTime}", fontSize = 12.sp, color = TextGrey)
            }

            Surface(shape = RoundedCornerShape(50), color = statusBg) {
                Text(
                    reservation.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DashboardStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label.uppercase(), fontSize = 11.sp, color = TextGrey)
        }
    }
}