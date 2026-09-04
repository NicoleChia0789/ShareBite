package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch

private enum class RecipientTab { ALL, CHARITIES, USERS }
private enum class OrderStatusTab { ALL, PENDING, COMPLETED, CANCELLED }

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
    var statusIndex by remember { mutableIntStateOf(0) }
    var pickupRange by remember { mutableStateOf(MerchantPickupRange.TODAY) }
    val pagerState = rememberPagerState(pageCount = { OrderStatusTab.entries.size })
    val scope = rememberCoroutineScope()
    val recipient = RecipientTab.entries[recipientIndex]
    val statusTab = OrderStatusTab.entries[statusIndex]

    LaunchedEffect(pagerState.currentPage) {
        statusIndex = pagerState.currentPage
    }

    val visibleRequests = remember(requests, recipient, statusTab, pickupRange) {
        if (recipient == RecipientTab.USERS) emptyList() else requests
            .filter { statusTab == OrderStatusTab.ALL || it.status.belongsTo(statusTab) }
            .filter { matchesMerchantPickupRange(it.pickupDate, it.pickupTime, pickupRange) }
            .sortedByDescending { it.createdAt }
    }
    val visibleReservations = remember(reservations, recipient, statusTab, pickupRange) {
        if (recipient == RecipientTab.CHARITIES) emptyList() else reservations
            .filter { statusTab == OrderStatusTab.ALL || it.status.belongsTo(statusTab) }
            .filter { matchesMerchantPickupRange(it.pickupDate, it.pickupTime, pickupRange) }
            .sortedByDescending { it.createdAt.orEmpty() }
    }
    val count = visibleRequests.size + visibleReservations.size

    Column(Modifier.fillMaxSize().padding(paddingValues).background(BgCream)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                RecipientTab.ALL to "All",
                RecipientTab.CHARITIES to "Charities",
                RecipientTab.USERS to "Users"
            ).forEachIndexed { index, (tab, label) ->
                FilterChip(
                    selected = recipientIndex == index,
                    onClick = { recipientIndex = index },
                    label = { Text(label, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = when (tab) {
                                RecipientTab.ALL -> Icons.Default.Group
                                RecipientTab.CHARITIES -> Icons.Default.Business
                                RecipientTab.USERS -> Icons.Default.Person
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MerchantPickupRange.entries.forEach { range ->
                FilterChip(
                    selected = pickupRange == range,
                    onClick = { pickupRange = range },
                    label = { Text(if (range == MerchantPickupRange.TODAY) "Today" else if (range == MerchantPickupRange.THIS_WEEK) "This Week" else "Upcoming") },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        LazyRow(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(OrderStatusTab.entries) { tab ->
                FilterChip(
                    selected = statusTab == tab,
                    onClick = {
                        statusIndex = tab.ordinal
                        scope.launch { pagerState.animateScrollToPage(tab.ordinal) }
                    },
                    label = { Text(tab.displayLabel) },
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
            else -> HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageStatus = OrderStatusTab.entries[page]
                val pageRequests = if (recipient == RecipientTab.USERS) emptyList() else requests
                    .filter { pageStatus == OrderStatusTab.ALL || it.status.belongsTo(pageStatus) }
                    .filter { matchesMerchantPickupRange(it.pickupDate, it.pickupTime, pickupRange) }
                    .sortedByDescending { it.createdAt }
                val pageReservations = if (recipient == RecipientTab.CHARITIES) emptyList() else reservations
                    .filter { pageStatus == OrderStatusTab.ALL || it.status.belongsTo(pageStatus) }
                    .filter { matchesMerchantPickupRange(it.pickupDate, it.pickupTime, pickupRange) }
                    .sortedByDescending { it.createdAt.orEmpty() }

                if (pageRequests.isEmpty() && pageReservations.isEmpty()) {
                    EmptyOrders(recipient, pageStatus)
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pageRequests, key = { "request_${it.id}" }) { request ->
                            MerchantOrderCard(
                                recipientName = request.organisationName.ifBlank { "Charity organisation" },
                                foodAndQuantity = "${request.quantity} × ${request.foodName}",
                                sourceLabel = if (request.listingId.isNullOrBlank()) "Charity need" else "Donation application",
                                status = request.status,
                                pickupDate = request.pickupDate,
                                pickupTime = request.pickupTime,
                                onClick = {
                                    onCharityClick(request.id)
                                }
                            )
                        }
                        items(pageReservations, key = { "reservation_${it.id}" }) { reservation ->
                            MerchantOrderCard(
                                recipientName = usersById[reservation.userId]?.name?.takeIf { it.isNotBlank() } ?: "Individual user",
                                foodAndQuantity = "${reservation.quantityNum} × ${reservation.foodName}",
                                sourceLabel = "User reservation",
                                status = reservation.status,
                                pickupDate = reservation.pickupDate,
                                pickupTime = reservation.pickupTime,
                                onClick = {
                                    onReservationClick(reservation.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun String.belongsTo(tab: OrderStatusTab): Boolean = when (tab) {
    OrderStatusTab.ALL -> true
    OrderStatusTab.PENDING -> equals("Pending", true) || equals("Confirmed", true)
    OrderStatusTab.COMPLETED -> equals("Completed", true)
    OrderStatusTab.CANCELLED -> equals("Cancelled", true) || equals("Declined", true)
}

private val OrderStatusTab.displayLabel: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun EmptyOrders(recipient: RecipientTab, status: OrderStatusTab) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (status == OrderStatusTab.COMPLETED || status == OrderStatusTab.CANCELLED) Icons.Default.History else Icons.Default.Inbox,
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
    onClick: () -> Unit
) {
    val displayStatus = if (status.equals("Confirmed", true)) "Pending" else status
    val statusColor = when {
        displayStatus.equals("Pending", true) -> Color(0xFFE65100)
        displayStatus.equals("Completed", true) -> GreyCompleted
        displayStatus.equals("Cancelled", true) || displayStatus.equals("Declined", true) -> RedCancelled
        else -> TextGrey
    }
    val statusBackground = when {
        displayStatus.equals("Pending", true) -> Color(0xFFFFF3E0)
        displayStatus.equals("Completed", true) -> Color(0xFFF5F5F5)
        displayStatus.equals("Cancelled", true) || displayStatus.equals("Declined", true) -> Color(0xFFFFEBEE)
        else -> Color(0xFFF5F5F5)
    }
    val pickupHeadline = when {
        displayStatus.equals("Completed", true) -> "COLLECTED ON $pickupDate"
        displayStatus.equals("Cancelled", true) || displayStatus.equals("Declined", true) -> "CANCELLED"
        else -> "PICK UP ON $pickupDate"
    }
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(pickupHeadline, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
                Surface(shape = RoundedCornerShape(20.dp), color = statusBackground) {
                    Text(
                        displayStatus.uppercase(),
                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(foodAndQuantity, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(4.dp))
            Text(recipientName, fontSize = 14.sp, color = TextGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$pickupDate · $pickupTime", fontSize = 13.sp, color = TextGrey)
            Text(sourceLabel, fontSize = 12.sp, color = TextGrey)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
            ) {
                Text("View", fontWeight = FontWeight.Bold)
            }
        }
    }
}
