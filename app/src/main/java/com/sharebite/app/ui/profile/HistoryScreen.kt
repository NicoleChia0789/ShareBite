package com.sharebite.app.ui.profile

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onViewDetails: (Long) -> Unit = {}
) {
    val userRole = CurrentUser.getUserRole()
    val reservationRepo = remember { ReservationRepository() }
    val charityRepo = remember { CharityRepository() }

    var historyItems by remember { mutableStateOf<List<Any>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedStatus by rememberSaveable { mutableStateOf("All") }
    var sortOrder by rememberSaveable { mutableStateOf("Newest") }

    val filteredHistoryItems = remember(historyItems, selectedStatus, sortOrder, userRole) {
        historyItems
            .filter { item ->
                val status = when (item) {
                    is Reservation -> item.status
                    is CharityRequest -> item.status
                    else -> ""
                }
                selectedStatus == "All" || status == selectedStatus
            }
            .sortedWith(compareBy<Any> { item ->
                when (item) {
                    is Reservation -> item.pickupDate
                    is CharityRequest -> item.pickupDate
                    else -> ""
                }
            }.let { comparator ->
                if (sortOrder == "Newest") comparator.reversed() else comparator
            })
    }

    LaunchedEffect(Unit) {
        try {
            when (userRole) {
                "user" -> {
                    val reservations = reservationRepo.getMyReservations()
                    historyItems = reservations.filter {
                        it.status == "Completed" || it.status == "Cancelled"
                    }
                }
                "charity" -> {
                    val requests = charityRepo.getMyRequests()
                    historyItems = requests.filter {
                        it.status == "Completed" || it.status == "Cancelled"
                    }
                }
                else -> {
                    historyItems = emptyList()
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {

        if (!isLoading && errorMessage == null) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("All", "Completed", "Cancelled")) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(listOf("Newest", "Oldest")) { order ->
                    FilterChip(
                        selected = sortOrder == order,
                        onClick = { sortOrder = order },
                        label = { Text(order) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (order == "Newest") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }


        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $errorMessage", color = RedCancelled)
                }
            }
            filteredHistoryItems.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextGrey
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (historyItems.isEmpty()) "No history found" else "No matching history",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Text(
                            text = if (historyItems.isEmpty())
                                "Your completed and cancelled items will appear here."
                            else
                                "Try selecting a different status filter.",
                            fontSize = 14.sp,
                            color = TextGrey
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (userRole) {
                        "user" -> {
                            items(filteredHistoryItems.filterIsInstance<Reservation>()) { item ->
                                HistoryCard(
                                    title = item.foodName,
                                    merchantName = "Merchant: ${item.merchantId.take(8)}",
                                    date = item.pickupDate,
                                    time = item.pickupTime,
                                    status = item.status,
                                    items = listOf(
                                        "${item.quantityNum} ${item.unit ?: "units"} of ${item.foodName}",
                                        "Pickup completed successfully"
                                    ),
                                    onViewDetails = { onViewDetails(item.id) }
                                )
                            }
                        }
                        "charity" -> {
                            items(filteredHistoryItems.filterIsInstance<CharityRequest>()) { item ->
                                HistoryCard(
                                    title = item.foodName,
                                    merchantName = item.organisationName,
                                    date = item.pickupDate,
                                    time = item.pickupTime,
                                    status = item.status,
                                    items = listOf(
                                        "${item.quantity} units of ${item.foodName}",
                                        "Served ${item.peopleServed} people"
                                    ),
                                    onViewDetails = { onViewDetails(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── History Card ──
@Composable
private fun HistoryCard(
    title: String,
    merchantName: String,
    date: String,
    time: String,
    status: String,
    items: List<String>,
    onViewDetails: () -> Unit
) {
    val displayDate = remember(date) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
            val parsedDate = inputFormat.parse(date)
            if (parsedDate != null) outputFormat.format(parsedDate) else date
        } catch (e: Exception) {
            date
        }
    }

    val displayTime = time.ifBlank { "10:30 AM" }

    val statusColor = when (status) {
        "Completed" -> GreenConfirmed
        "Cancelled" -> RedCancelled
        else -> TextGrey
    }
    val statusBg = when (status) {
        "Completed" -> Color(0xFFE3F5E6)
        "Cancelled" -> Color(0xFFFFEDED)
        else -> Color(0xFFEEEEEE)
    }
    val statusIcon = when (status) {
        "Completed" -> Icons.Default.CheckCircle
        "Cancelled" -> Icons.Default.Cancel
        else -> Icons.Default.History
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ── Header Row: Title + Status ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = statusColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = merchantName,
                fontSize = 13.sp,
                color = TextGrey
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Completed: $displayDate, $displayTime",
                fontSize = 13.sp,
                color = TextGrey
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Items Received:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { itemText ->
                    HistoryItemRow(
                        text = itemText,
                        isChecked = status == "Completed"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onViewDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangeAccent
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangeAccent)
            ) {
                Text(
                    text = "View Details",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HistoryItemRow(
    text: String,
    isChecked: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isChecked) GreenConfirmed else Color(0xFFDDC0B8)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextDark
        )
    }
}
