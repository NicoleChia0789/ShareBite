package com.sharebite.app.ui.merchant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.Reservation as ReservationModel
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.GreenConfirmed
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.theme.RedCancelled
import com.sharebite.app.ui.theme.TextDark
import com.sharebite.app.ui.theme.TextGrey

@Composable
fun MerchantReservationsPage(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onCharityClick: (Long) -> Unit,
    onReservationClick: (Long) -> Unit,
    onViewAllRequests: () -> Unit = {},
    onViewAllReservations: () -> Unit = {},
    onPickupDetailsClick: (String) -> Unit = {}
) {
    val requests by viewModel.requests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val allItems: List<OrderItem> = remember(requests, reservations) {
        val items = mutableListOf<OrderItem>()
        requests.forEach { req ->
            items.add(OrderItem.Charity(req))
        }
        reservations.forEach { res ->
            items.add(OrderItem.Reservation(res))
        }
        items.sortedByDescending { it.createdAt }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "History")

    val groupedItems = remember(allItems, selectedTab) {
        val activeStatuses = listOf("Pending", "Confirmed")
        val historyStatuses = listOf("Completed", "Cancelled")

        val filtered = if (selectedTab == 0) {
            allItems.filter { it.status in activeStatuses }
        } else {
            allItems.filter { it.status in historyStatuses }
        }

        val statusOrder = if (selectedTab == 0) {
            listOf("Pending", "Confirmed")
        } else {
            listOf("Completed", "Cancelled")
        }

        filtered.groupBy { it.status }
            .toList()
            .sortedBy { (status, _) -> statusOrder.indexOf(status) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
    ) {
        Text(
            "Orders",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(label, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledLabelColor = TextGrey
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        } else if (groupedItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (selectedTab == 0) Icons.Default.Inbox else Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextGrey
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (selectedTab == 0) "No active orders." else "No order history.",
                        color = TextGrey,
                        fontSize = 16.sp
                    )
                    Text(
                        if (selectedTab == 0) "Orders from charities and users will appear here." else "Completed and cancelled orders will appear here.",
                        color = TextGrey,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedItems.forEach { (status, items) ->
                    item {
                        val statusColor = when (status) {
                            "Pending" -> OrangeAccent
                            "Confirmed" -> Color(0xFF1976D2)
                            "Completed" -> GreenConfirmed
                            "Cancelled" -> RedCancelled
                            else -> TextGrey
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(20.dp)
                                    .background(statusColor, RoundedCornerShape(2.dp))
                            )
                            Text(
                                status.uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                "(${items.size})",
                                fontSize = 12.sp,
                                color = TextGrey
                            )
                        }
                    }

                    items(items) { item ->
                        when (item) {
                            is OrderItem.Charity -> {
                                val request = item.request
                                // ✅ 食物名作为标题
                                OrderCard(
                                    title = request.foodName,
                                    food = "from ${request.organisationName}",
                                    quantity = "${request.quantity} ×",
                                    status = request.status,
                                    pickupDate = request.pickupDate,
                                    pickupTime = request.pickupTime,
                                    isCharity = true,
                                    onClick = { onCharityClick(request.id) }
                                )
                            }
                            is OrderItem.Reservation -> {
                                val reservation = item.reservation
                                OrderCard(
                                    title = reservation.foodName,
                                    food = "by User: ${reservation.userId.take(8)}",
                                    quantity = "${reservation.quantityNum} ×",
                                    status = reservation.status,
                                    pickupDate = reservation.pickupDate,
                                    pickupTime = reservation.pickupTime,
                                    isCharity = false,
                                    onClick = { onReservationClick(reservation.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class OrderItem(open val createdAt: String?, open val status: String) {
    data class Charity(val request: CharityRequest) :
        OrderItem(request.createdAt, request.status)

    data class Reservation(val reservation: ReservationModel) :
        OrderItem(reservation.createdAt, reservation.status)
}

@Composable
fun OrderCard(
    title: String,
    food: String,
    quantity: String,
    status: String,
    pickupDate: String,
    pickupTime: String,
    isCharity: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (status) {
        "Pending" -> OrangeAccent
        "Confirmed" -> Color(0xFF1976D2)
        "Completed" -> GreenConfirmed
        "Cancelled" -> RedCancelled
        else -> TextGrey
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCharity) OrangeAccent.copy(alpha = 0.15f)
                        else Color(0xFFE3F2FD)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isCharity) Icons.Default.Business else Icons.Default.Person,
                    contentDescription = if (isCharity) "Charity" else "User",
                    tint = if (isCharity) OrangeAccent else Color(0xFF1976D2),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextDark,
                    maxLines = 1
                )
                Text(
                    "$quantity $food",
                    fontSize = 13.sp,
                    color = TextGrey
                )
                Text(
                    "Pickup: $pickupDate at $pickupTime",
                    fontSize = 12.sp,
                    color = TextGrey
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}