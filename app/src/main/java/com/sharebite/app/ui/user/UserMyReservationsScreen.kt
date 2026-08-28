package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.ui.theme.*

@Composable
fun UserMyReservationsScreen(
    paddingValues: PaddingValues,
    onViewClick: (Long) -> Unit
) {
    val userId = "test-user-id" // 临时，后续替换为真实 userId
    val repository = remember { ReservationRepository() }

    var allReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var filteredReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }

    val statuses = listOf("All", "Pending", "Confirmed", "Completed", "Cancelled")

    LaunchedEffect(Unit) {
        try {
            allReservations = repository.getUserReservations(userId)
            filteredReservations = allReservations
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(selectedStatus, allReservations) {
        filteredReservations = if (selectedStatus == null || selectedStatus == "All") {
            allReservations
        } else {
            allReservations.filter { it.status == selectedStatus }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        // 状态筛选
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statuses) { status ->
                val isSelected = if (status == "All") selectedStatus == null || selectedStatus == "All" else selectedStatus == status
                FilterChip(
                    onClick = {
                        selectedStatus = if (status == "All") null else status
                    },
                    label = { Text(status) },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: $errorMessage", color = RedCancelled)
                }
            }
            filteredReservations.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No reservations", fontSize = 16.sp, color = TextGrey)
                        Text("Start browsing food!", fontSize = 14.sp, color = TextGrey)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReservations) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            onClick = { onViewClick(reservation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    onClick: () -> Unit
) {
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFE6D9)),
                contentAlignment = Alignment.Center
            ) {
                Text("🍱", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reservation.foodName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Pickup: ${reservation.pickupDate} at ${reservation.pickupTime}",
                    fontSize = 12.sp,
                    color = TextGrey
                )
                if (reservation.note.isNotBlank()) {
                    Text(
                        "Note: ${reservation.note}",
                        fontSize = 11.sp,
                        color = TextGrey
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusBg
            ) {
                Text(
                    reservation.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}