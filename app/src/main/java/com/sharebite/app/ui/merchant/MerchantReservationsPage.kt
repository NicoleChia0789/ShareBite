package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.Reservation
import kotlinx.coroutines.launch

private enum class RecipientTab { ALL, CHARITIES, USERS }
private enum class OrderStatusTab { ALL, PENDING, COMPLETED, CANCELLED }

@Composable
fun MerchantReservationsPage(
    paddingValues: PaddingValues,
    initialStatus: String? = null,
    initialTimeFilter: String? = null,
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
    var statusIndex by rememberSaveable(initialStatus) {
        mutableIntStateOf(when (initialStatus?.lowercase()) {
            "pending" -> OrderStatusTab.PENDING.ordinal
            "completed" -> OrderStatusTab.COMPLETED.ordinal
            "cancelled" -> OrderStatusTab.CANCELLED.ordinal
            else -> OrderStatusTab.ALL.ordinal
        })
    }
    var pickupRange by rememberSaveable(initialTimeFilter) {
        mutableStateOf(when (initialTimeFilter?.lowercase()) {
            "all" -> MerchantPickupRange.ALL
            "thisweek" -> MerchantPickupRange.THIS_WEEK
            "upcoming" -> MerchantPickupRange.UPCOMING
            else -> MerchantPickupRange.ALL
        })
    }
    val pagerState = rememberPagerState(initialPage = statusIndex, pageCount = { OrderStatusTab.entries.size })
    val scope = rememberCoroutineScope()
    var updatedRequest by remember { mutableStateOf<CharityRequest?>(null) }
    var resolvedUpdatedRequestIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var resolvedUpdatedRequestStatuses by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
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
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Row(Modifier.fillMaxSize().padding(paddingValues).background(BgCream)) {
        if (isLandscape) {
            Column(
                modifier = Modifier
                    .width(285.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Manage charity requests and user pickups.", fontSize = 13.sp, color = TextGrey)
                Text("Recipient", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextGrey)
                listOf(
                    RecipientTab.ALL to "All",
                    RecipientTab.CHARITIES to "Charities",
                    RecipientTab.USERS to "Users"
                ).forEachIndexed { index, (tab, label) ->
                    FilterChip(
                        selected = recipientIndex == index,
                        onClick = { recipientIndex = index },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                when (tab) {
                                    RecipientTab.ALL -> Icons.Default.Group
                                    RecipientTab.CHARITIES -> Icons.Default.Business
                                    RecipientTab.USERS -> Icons.Default.Person
                                }, contentDescription = null, modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeAccent, selectedLabelColor = Color.White)
                    )
                }
                Text("Pickup time", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextGrey)
                MerchantPickupRange.entries.forEach { range ->
                    FilterChip(
                        selected = pickupRange == range,
                        onClick = { pickupRange = range },
                        label = { Text(when (range) {
                            MerchantPickupRange.ALL -> "All"
                            MerchantPickupRange.TODAY -> "Today"
                            MerchantPickupRange.THIS_WEEK -> "This Week"
                            MerchantPickupRange.UPCOMING -> "Upcoming"
                        }) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeAccent, selectedLabelColor = Color.White)
                    )
                }
                Text("Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextGrey)
                OrderStatusTab.entries.forEach { tab ->
                    FilterChip(
                        selected = statusTab == tab,
                        onClick = {
                            statusIndex = tab.ordinal
                            scope.launch { pagerState.animateScrollToPage(tab.ordinal) }
                        },
                        label = { Text(tab.displayLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeAccent, selectedLabelColor = Color.White)
                    )
                }
            }
        }
        Column(modifier = if (isLandscape) Modifier.weight(1f).fillMaxHeight() else Modifier.fillMaxWidth()) {
        if (!isLandscape) {
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
                    label = { Text(when (range) {
                        MerchantPickupRange.ALL -> "All"
                        MerchantPickupRange.TODAY -> "Today"
                        MerchantPickupRange.THIS_WEEK -> "This Week"
                        MerchantPickupRange.UPCOMING -> "Upcoming"
                    }) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        LazyRow(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
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
            else -> if (isLandscape) {
                val landscapeListState = rememberLazyListState()
                LaunchedEffect(recipient, statusTab, pickupRange) {
                    landscapeListState.scrollToItem(0)
                }
                val landscapeRequests = if (recipient == RecipientTab.USERS) emptyList() else requests
                    .map { request ->
                        resolvedUpdatedRequestStatuses[request.id]?.let { request.copy(status = it) } ?: request
                    }
                    .filter { statusTab == OrderStatusTab.ALL || it.status.belongsTo(statusTab) }
                    .filter { matchesMerchantPickupRange(it.pickupDate, it.pickupTime, pickupRange) }
                    .sortedWith(compareBy<CharityRequest> { merchantStatusPriority(it.status) }.thenByDescending { it.createdAt })
                val landscapeReservations = if (recipient == RecipientTab.CHARITIES) emptyList() else reservations
                    .filter { statusTab == OrderStatusTab.ALL || it.status.belongsTo(statusTab) }
                    .filter { matchesMerchantPickupRange(it.pickupDate, it.pickupTime, pickupRange) }
                    .sortedWith(compareBy<Reservation> { merchantStatusPriority(it.status) }.thenByDescending { it.createdAt.orEmpty() })

                LazyColumn(
                    state = landscapeListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(landscapeRequests, key = { "landscape_request_${it.id}" }) { request ->
                        MerchantOrderCard(
                            recipientName = request.organisationName.ifBlank { "Charity organisation" },
                            foodAndQuantity = "${request.quantity} × ${request.foodName}",
                            sourceLabel = if (request.listingId.isNullOrBlank()) "Charity need" else "Donation application",
                            status = request.status,
                            hasChanges = request.detailsChanged && request.id !in resolvedUpdatedRequestIds,
                            pickupDate = request.pickupDate,
                            pickupTime = request.pickupTime,
                            onClick = {
                                if (request.detailsChanged && request.id !in resolvedUpdatedRequestIds) {
                                    updatedRequest = request
                                } else {
                                    onCharityClick(request.id)
                                }
                            }
                        )
                    }
                    items(landscapeReservations, key = { "landscape_reservation_${it.id}" }) { reservation ->
                        MerchantOrderCard(
                            recipientName = usersById[reservation.userId]?.name?.takeIf { it.isNotBlank() } ?: "Individual user",
                            foodAndQuantity = "${reservation.quantityNum} × ${reservation.foodName}",
                            sourceLabel = "User reservation",
                            status = reservation.status,
                            hasChanges = false,
                            pickupDate = reservation.pickupDate,
                            pickupTime = reservation.pickupTime,
                            onClick = { onReservationClick(reservation.id) }
                        )
                    }
                }
            } else HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageStatus = OrderStatusTab.entries[page]
                val listState = rememberLazyListState()
                LaunchedEffect(recipient, statusIndex, pickupRange) {
                    listState.scrollToItem(0)
                }
                val pageRequests = if (recipient == RecipientTab.USERS) emptyList() else requests
                    .map { request ->
                        resolvedUpdatedRequestStatuses[request.id]?.let { request.copy(status = it) } ?: request
                    }
                    .filter { pageStatus == OrderStatusTab.ALL || it.status.belongsTo(pageStatus) }
                    .filter { matchesMerchantPickupRange(it.pickupDate, it.pickupTime, pickupRange) }
                    .sortedWith(compareBy<CharityRequest> { merchantStatusPriority(it.status) }.thenByDescending { it.createdAt })
                val pageReservations = if (recipient == RecipientTab.CHARITIES) emptyList() else reservations
                    .filter { pageStatus == OrderStatusTab.ALL || it.status.belongsTo(pageStatus) }
                    .filter { matchesMerchantPickupRange(it.pickupDate, it.pickupTime, pickupRange) }
                    .sortedWith(compareBy<Reservation> { merchantStatusPriority(it.status) }.thenByDescending { it.createdAt.orEmpty() })

                if (pageRequests.isEmpty() && pageReservations.isEmpty()) {
                    EmptyOrders(recipient, pageStatus)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val updatedRequests = pageRequests
                            .filter { it.detailsChanged && it.id !in resolvedUpdatedRequestIds }
                            .sortedBy { merchantStatusPriority(it.status) }
                        items(updatedRequests, key = { "updated_request_${it.id}" }) { request ->
                            MerchantOrderCard(
                                recipientName = request.organisationName.ifBlank { "Charity organisation" },
                                foodAndQuantity = "${request.quantity} × ${request.foodName}",
                                sourceLabel = if (request.listingId.isNullOrBlank()) "Charity need" else "Donation application",
                                status = request.status,
                                hasChanges = true,
                                pickupDate = request.pickupDate,
                                pickupTime = request.pickupTime,
                                onClick = { updatedRequest = request }
                            )
                        }
                        // In All, updated items are shown first, then confirmed is grouped
                        // into the Pending bucket instead of becoming a separate section.
                        val orderedStatuses = if (pageStatus == OrderStatusTab.ALL) {
                            listOf("Pending", "Completed", "Cancelled", "Declined")
                        } else {
                            listOf("Pending", "Confirmed", "Completed", "Cancelled", "Declined")
                        }
                        orderedStatuses.forEach { orderedStatus ->
                        items(pageRequests.filter {
                            ( !it.detailsChanged || it.id in resolvedUpdatedRequestIds) &&
                                    it.status.matchesMerchantOrderBucket(orderedStatus, pageStatus == OrderStatusTab.ALL)
                        }, key = { "request_${it.id}" }) { request ->
                            MerchantOrderCard(
                                recipientName = request.organisationName.ifBlank { "Charity organisation" },
                                foodAndQuantity = "${request.quantity} × ${request.foodName}",
                                sourceLabel = if (request.listingId.isNullOrBlank()) "Charity need" else "Donation application",
                                status = request.status,
                                hasChanges = request.detailsChanged && request.id !in resolvedUpdatedRequestIds,
                                pickupDate = request.pickupDate,
                                pickupTime = request.pickupTime,
                                onClick = {
                                    if (request.detailsChanged && request.id !in resolvedUpdatedRequestIds) {
                                        updatedRequest = request
                                    } else {
                                        onCharityClick(request.id)
                                    }
                                }
                            )
                        }
                        items(pageReservations.filter {
                            it.status.matchesMerchantOrderBucket(orderedStatus, pageStatus == OrderStatusTab.ALL)
                        }, key = { "reservation_${it.id}" }) { reservation ->
                            MerchantOrderCard(
                                recipientName = usersById[reservation.userId]?.name?.takeIf { it.isNotBlank() } ?: "Individual user",
                                foodAndQuantity = "${reservation.quantityNum} × ${reservation.foodName}",
                                sourceLabel = "User reservation",
                                status = reservation.status,
                                hasChanges = false,
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
        }
    }

    updatedRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { updatedRequest = null },
            title = { Text("Updated request", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("This charity changed the request. Review the new details before approving.", color = TextGrey)
                    DetailComparisonRow("Quantity", "${request.previousQuantity ?: request.quantity}", "${request.quantity}")
                    DetailComparisonRow("Pickup date", request.previousPickupDate ?: request.pickupDate, request.pickupDate)
                    DetailComparisonRow("Pickup time", request.previousPickupTime ?: request.pickupTime, request.pickupTime)
                    DetailComparisonRow("Note", request.previousNote ?: "None", request.note ?: "None")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveRequest(request.id)
                        resolvedUpdatedRequestIds = resolvedUpdatedRequestIds + request.id
                        resolvedUpdatedRequestStatuses = resolvedUpdatedRequestStatuses + (
                            request.id to if (request.listingId.isNullOrBlank()) "Pending" else "Confirmed"
                        )
                        updatedRequest = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) { Text("Approve", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.updateRequestStatus(request.id, "Cancelled", "Updated request declined by merchant")
                        resolvedUpdatedRequestIds = resolvedUpdatedRequestIds + request.id
                        resolvedUpdatedRequestStatuses = resolvedUpdatedRequestStatuses + (request.id to "Cancelled")
                        updatedRequest = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancelled)
                ) { Text("Reject") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun String.belongsTo(tab: OrderStatusTab): Boolean = when (tab) {
    OrderStatusTab.ALL -> true
    OrderStatusTab.PENDING -> equals("Pending", true) || equals("Confirmed", true)
    OrderStatusTab.COMPLETED -> equals("Completed", true)
    OrderStatusTab.CANCELLED -> equals("Cancelled", true) || equals("Declined", true)
}

private fun String.matchesMerchantOrderBucket(bucket: String, allPage: Boolean): Boolean =
    if (allPage && bucket.equals("Pending", true)) {
        equals("Pending", true) || equals("Confirmed", true)
    } else {
        equals(bucket, true)
    }

private fun merchantStatusPriority(status: String): Int = when {
    status.equals("Pending", true) -> 0
    status.equals("Confirmed", true) -> 1
    status.equals("Completed", true) -> 2
    status.equals("Cancelled", true) || status.equals("Declined", true) -> 3
    else -> 99
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
    hasChanges: Boolean,
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
            if (hasChanges) {
                Text("Updated, pending approval", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
            }
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

@Composable
private fun DetailComparisonRow(label: String, oldValue: String, newValue: String) {
    Column {
        Text(label, fontSize = 12.sp, color = TextGrey)
        Text("Before: $oldValue", fontSize = 13.sp, color = TextGrey)
        Text("Updated: $newValue", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}
