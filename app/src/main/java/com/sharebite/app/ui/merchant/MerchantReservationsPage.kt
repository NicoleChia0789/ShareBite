package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*

private enum class RecipientTab { ALL, CHARITIES, USERS }
private enum class OrderStatusTab { PENDING, CONFIRMED, HISTORY }

@Composable
fun MerchantReservationsPage(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onCharityClick: (Long) -> Unit,
    onReservationClick: (Long) -> Unit,
    onViewAllRequests: () -> Unit = {},
    onViewAllReservations: () -> Unit = {},
    onPickupScheduleClick: () -> Unit = {},
    onPickupClick: (String) -> Unit = {}
) {
    val requests by viewModel.requests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val usersById by viewModel.reservationUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var recipientIndex by remember { mutableIntStateOf(0) }
    var charityStatusIndex by remember { mutableIntStateOf(0) }
    var userStatusIndex by remember { mutableIntStateOf(0) }
    val recipient = RecipientTab.entries[recipientIndex]
    val statusIndex = if (recipient == RecipientTab.CHARITIES) charityStatusIndex else userStatusIndex
    val statusTab = OrderStatusTab.entries[statusIndex]

    val visibleRequests = remember(requests, recipient, statusTab) {
        if (recipient == RecipientTab.USERS) emptyList() else requests
            .filter { it.status.belongsTo(statusTab) }.sortedByDescending { it.createdAt }
    }
    val visibleReservations = remember(reservations, recipient, statusTab) {
        if (recipient == RecipientTab.CHARITIES) emptyList() else reservations
            .filter { it.status.belongsTo(statusTab) }.sortedByDescending { it.createdAt.orEmpty() }
    }
    val count = visibleRequests.size + visibleReservations.size

    Column(Modifier.fillMaxSize().padding(paddingValues).background(BgCream)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Requests & Reservations", fontWeight = FontWeight.Bold, color = TextDark)
            OutlinedButton(onClick = onPickupScheduleClick, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Pickup schedule")
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Charities", "Users").forEachIndexed { index, label ->
                FilterChip(
                    selected = recipientIndex == index,
                    onClick = { recipientIndex = index },
                    label = { Text(label, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Pending", "Confirmed", "History").forEachIndexed { index, label ->
                FilterChip(
                    selected = statusIndex == index,
                    onClick = {
                        if (recipient == RecipientTab.CHARITIES) charityStatusIndex = index
                        else userStatusIndex = index
                    },
                    label = { Text(label) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Text(
            when (recipient) {
                RecipientTab.ALL -> "All requests and reservations ($count)"
                RecipientTab.CHARITIES -> "Charity requests ($count)"
                RecipientTab.USERS -> "User reservations ($count)"
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }
            count == 0 -> EmptyOrders(recipient, statusTab)
            recipient == RecipientTab.CHARITIES -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleRequests, key = { it.id }) { request ->
                    MerchantOrderCard(
                        recipientName = request.organisationName.ifBlank { "Charity organisation" },
                        foodAndQuantity = "${request.quantity} × ${request.foodName}",
                        sourceLabel = if (request.listingId.isNullOrBlank()) "Charity need" else "Donation application",
                        status = request.status,
                        pickupDate = request.pickupDate,
                        pickupTime = request.pickupTime,
                        isCharity = true,
                        actionLabel = statusTab.actionLabel,
                        onClick = {
                            if (statusTab == OrderStatusTab.CONFIRMED) onPickupClick("req_${request.id}")
                            else onCharityClick(request.id)
                        }
                    )
                }
            }
            recipient == RecipientTab.USERS -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleReservations, key = { it.id }) { reservation ->
                    MerchantOrderCard(
                        recipientName = usersById[reservation.userId]?.name?.takeIf { it.isNotBlank() }
                            ?: "Individual user",
                        foodAndQuantity = "${reservation.quantityNum} × ${reservation.foodName}",
                        sourceLabel = "User reservation",
                        status = reservation.status,
                        pickupDate = reservation.pickupDate,
                        pickupTime = reservation.pickupTime,
                        isCharity = false,
                        actionLabel = statusTab.actionLabel,
                        onClick = {
                            if (statusTab == OrderStatusTab.CONFIRMED) onPickupClick("res_${reservation.id}")
                            else onReservationClick(reservation.id)
                        }
                    )
                }
            }
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleRequests, key = { "request_${it.id}" }) { request ->
                    MerchantOrderCard(
                        recipientName = request.organisationName.ifBlank { "Charity organisation" },
                        foodAndQuantity = "${request.quantity} x ${request.foodName}",
                        sourceLabel = if (request.listingId.isNullOrBlank()) "Charity need" else "Donation application",
                        status = request.status,
                        pickupDate = request.pickupDate,
                        pickupTime = request.pickupTime,
                        isCharity = true,
                        actionLabel = statusTab.actionLabel,
                        onClick = { if (statusTab == OrderStatusTab.CONFIRMED) onPickupClick("req_${request.id}") else onCharityClick(request.id) }
                    )
                }
                items(visibleReservations, key = { "reservation_${it.id}" }) { reservation ->
                    MerchantOrderCard(
                        recipientName = usersById[reservation.userId]?.name?.takeIf { it.isNotBlank() } ?: "Individual user",
                        foodAndQuantity = "${reservation.quantityNum} x ${reservation.foodName}",
                        sourceLabel = "User reservation",
                        status = reservation.status,
                        pickupDate = reservation.pickupDate,
                        pickupTime = reservation.pickupTime,
                        isCharity = false,
                        actionLabel = statusTab.actionLabel,
                        onClick = { if (statusTab == OrderStatusTab.CONFIRMED) onPickupClick("res_${reservation.id}") else onReservationClick(reservation.id) }
                    )
                }
            }
        }
    }
}

private fun String.belongsTo(tab: OrderStatusTab): Boolean = when (tab) {
    OrderStatusTab.PENDING -> equals("Pending", true)
    OrderStatusTab.CONFIRMED -> equals("Confirmed", true)
    OrderStatusTab.HISTORY -> equals("Completed", true) || equals("Cancelled", true) || equals("Declined", true)
}

private val OrderStatusTab.actionLabel: String
    get() = when (this) {
        OrderStatusTab.PENDING -> "View details"
        OrderStatusTab.CONFIRMED -> "View pickup"
        OrderStatusTab.HISTORY -> "View details"
    }

@Composable
private fun EmptyOrders(recipient: RecipientTab, status: OrderStatusTab) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (status == OrderStatusTab.HISTORY) Icons.Default.History else Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = TextGrey
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No ${status.name.lowercase()} ${when (recipient) { RecipientTab.CHARITIES -> "charity requests"; RecipientTab.USERS -> "user reservations"; RecipientTab.ALL -> "requests or reservations" }}.",
                color = TextGrey,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun MerchantOrderCard(
    recipientName: String,
    foodAndQuantity: String,
    sourceLabel: String,
    status: String,
    pickupDate: String,
    pickupTime: String,
    isCharity: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    val statusColor = when {
        status.equals("Pending", true) -> OrangeAccent
        status.equals("Confirmed", true) -> Color(0xFF1976D2)
        status.equals("Completed", true) -> GreenConfirmed
        status.equals("Cancelled", true) || status.equals("Declined", true) -> RedCancelled
        else -> TextGrey
    }
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(
                        if (isCharity) OrangeAccent.copy(alpha = 0.15f) else Color(0xFFE3F2FD)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isCharity) Icons.Default.Business else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isCharity) OrangeAccent else Color(0xFF1976D2)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(recipientName, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(foodAndQuantity, fontSize = 13.sp, color = TextGrey)
                }
                Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.12f)) {
                    Text(
                        status,
                        Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isCharity) OrangeAccent.copy(alpha = 0.10f) else Color(0xFFE3F2FD)
            ) {
                Text(
                    sourceLabel,
                    Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = if (isCharity) OrangeAccent else Color(0xFF1976D2),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("Pickup: $pickupDate at $pickupTime", fontSize = 12.sp, color = TextGrey)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(12.dp)
            ) { Text(actionLabel, color = Color.White) }
        }
    }
}
