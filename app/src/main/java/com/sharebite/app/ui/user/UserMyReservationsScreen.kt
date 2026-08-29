package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserMyReservationsScreen(
    paddingValues: PaddingValues,
    onViewClick: (Long) -> Unit
) {
    val userId = "258c6924-0bdd-481f-a6d8-cdafb4e7abe3"
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

    fun formatDate(dateStr: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(dateStr)
            if (date != null) {
                SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date)
            } else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("My Reservations", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("Track and manage your community food pickups.", fontSize = 13.sp, color = TextGrey)
        }

        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statuses) { status ->
                val isSelected = if (status == "All") {
                    selectedStatus == null || selectedStatus == "All"
                } else {
                    selectedStatus == status
                }
                FilterChip(
                    onClick = {
                        selectedStatus = if (status == "All") null else status
                    },
                    label = { Text(status, fontSize = 13.sp) },
                    selected = isSelected,
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color(0xFFE8E2D8),
                        disabledLabelColor = TextGrey
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }

            errorMessage != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Error: $errorMessage", color = RedCancelled)
            }

            filteredReservations.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextGrey
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No reservations", fontSize = 16.sp, color = TextGrey)
                    Text("Start browsing food!", fontSize = 14.sp, color = TextGrey)
                }
            }

            else -> LazyColumn(
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

    val isToday = try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val pickupDate = format.parse(reservation.pickupDate)
        val today = Date()
        pickupDate != null && format.format(pickupDate) == format.format(today)
    } catch (e: Exception) {
        false
    }

    val statusHeadline = when {
        reservation.status == "Pending" && isToday -> "PICK UP TODAY"
        reservation.status == "Pending" -> "REQUESTED"
        reservation.status == "Confirmed" && isToday -> "PICK UP TODAY"
        reservation.status == "Confirmed" -> "PICK UP"
        reservation.status == "Completed" -> "COLLECTED"
        reservation.status == "Cancelled" -> "CANCELLED"
        else -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    statusHeadline,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reservation.status == "Cancelled") TextGrey else OrangeAccent
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusBg
                ) {
                    Text(
                        reservation.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${reservation.foodName} × ${reservation.quantity}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "${reservation.pickupDate} · ${reservation.pickupTime}",
                fontSize = 13.sp,
                color = TextGrey
            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (reservation.status == "Pending") {
                    Arrangement.SpaceBetween
                } else {
                    Arrangement.Start
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = OrangeAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OrangeAccent)
                }

                if (reservation.status == "Pending") {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Transparent,
                            modifier = Modifier.clickable { /* onEditClick */ }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = GreenConfirmed
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Edit", fontSize = 12.sp, color = GreenConfirmed)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Transparent,
                            modifier = Modifier.clickable { /* onWithdrawClick */ }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = RedCancelled
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Withdraw", fontSize = 12.sp, color = RedCancelled)
                            }
                        }
                    }
                }
            }
        }
    }
}