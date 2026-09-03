package com.sharebite.app.ui.merchant

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class PickupTab { TODAY, THIS_WEEK, UPCOMING }
private enum class PickupSource { REQUEST, RESERVATION }
private enum class PickupPage { SCHEDULE, HISTORY }
private enum class HistoryFilter { ALL, CHARITIES, USERS }

private data class PickupScheduleUiItem(
    val id: Long,
    val source: PickupSource,
    val recipientName: String,
    val foodName: String,
    val quantityText: String,
    val pickupDateText: String,
    val pickupTimeText: String,
    val scheduledAt: Date
) {
    val routeId: String
        get() = if (source == PickupSource.REQUEST) "req_$id" else "res_$id"
}

@Composable
fun MerchantPickupScheduleScreen(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    initialShowHistory: Boolean = false,
    onPickupClick: (String) -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val usersById by viewModel.reservationUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedPageIndex by remember(initialShowHistory) {
        mutableIntStateOf(if (initialShowHistory) 1 else 0)
    }
    var historyFilterIndex by remember { mutableIntStateOf(0) }
    var showOverdue by remember { mutableStateOf(false) }
    val selectedPage = PickupPage.entries[selectedPageIndex]

    BackHandler(enabled = showOverdue) { showOverdue = false }

    LaunchedEffect(Unit) { viewModel.loadMerchantData() }

    val confirmedRequests = remember(requests) {
        requests.filter { it.status.equals("Confirmed", ignoreCase = true) }
    }
    val confirmedReservations = remember(reservations) {
        reservations.filter { it.status.equals("Confirmed", ignoreCase = true) }
    }

    val scheduleItems = remember(confirmedRequests, confirmedReservations, usersById) {
        buildList {
            confirmedRequests.mapNotNullTo(this) { it.toScheduleItem() }
            confirmedReservations.mapNotNullTo(this) { reservation ->
                reservation.toScheduleItem(
                    usersById[reservation.userId]?.name
                        ?.takeIf { it.isNotBlank() }
                        ?: "Individual user"
                )
            }
        }.sortedBy { it.scheduledAt }
    }

    val completedItems = remember(requests, reservations, usersById) {
        buildList {
            requests.filter { it.status.equals("Completed", ignoreCase = true) }
                .mapTo(this) { it.toHistoryItem() }
            reservations.filter { it.status.equals("Completed", ignoreCase = true) }
                .mapTo(this) { reservation ->
                    reservation.toHistoryItem(
                        usersById[reservation.userId]?.name?.takeIf { it.isNotBlank() }
                            ?: "Individual user"
                    )
                }
        }.sortedByDescending { it.scheduledAt }
    }
    val historyFilter = HistoryFilter.entries[historyFilterIndex]
    val visibleHistoryItems = completedItems.filter {
        historyFilter == HistoryFilter.ALL ||
                (historyFilter == HistoryFilter.CHARITIES && it.source == PickupSource.REQUEST) ||
                (historyFilter == HistoryFilter.USERS && it.source == PickupSource.RESERVATION)
    }
    val groupedHistoryItems = visibleHistoryItems.groupBy { formatDbDate(it.scheduledAt) }

    val invalidCount = remember(confirmedRequests, confirmedReservations) {
        confirmedRequests.count { parseSchedule(it.pickupDate, it.pickupTime) == null } +
                confirmedReservations.count { parseSchedule(it.pickupDate, it.pickupTime) == null }
    }

    val now = Date()
    val todayStart = startOfDay(now)
    val todayEnd = endOfDay(now)
    val weekEnd = endOfWeek(now)
    val selectedTab = PickupTab.entries[selectedTabIndex]
    val visibleItems = scheduleItems.filter { item ->
        when (selectedTab) {
            PickupTab.TODAY -> !item.scheduledAt.before(todayStart) &&
                    !item.scheduledAt.after(todayEnd)
            PickupTab.THIS_WEEK -> item.scheduledAt.after(todayEnd) && !item.scheduledAt.after(weekEnd)
            PickupTab.UPCOMING -> item.scheduledAt.after(weekEnd)
        }
    }
    val groupedItems = visibleItems.groupBy { formatDbDate(it.scheduledAt) }
    val overdueItems = scheduleItems.filter { it.scheduledAt.before(todayStart) }
        .sortedByDescending { it.scheduledAt }
    val groupedOverdueItems = overdueItems.groupBy { formatDbDate(it.scheduledAt) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
    ) {
        TabRow(selectedTabIndex = selectedPageIndex, containerColor = Color.White) {
            listOf("Schedule", "History").forEachIndexed { index, title ->
                Tab(
                    selected = selectedPageIndex == index,
                    onClick = {
                        selectedPageIndex = index
                        showOverdue = false
                    },
                    text = { Text(title, fontWeight = FontWeight.Medium) }
                )
            }
        }

        if (selectedPage == PickupPage.SCHEDULE && !showOverdue) {
            TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.White) {
                listOf("Today", "This Week", "Upcoming").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Medium) }
                    )
                }
            }
        }

        if (selectedPage == PickupPage.SCHEDULE && overdueItems.isNotEmpty()) {
            OverdueAlert(
                count = overdueItems.size,
                expanded = showOverdue,
                onToggle = { showOverdue = !showOverdue }
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedPage == PickupPage.HISTORY) {
                item(key = "history_filters") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Charities", "Users").forEachIndexed { index, label ->
                            FilterChip(
                                selected = historyFilterIndex == index,
                                onClick = { historyFilterIndex = index },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeAccent,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
                if (visibleHistoryItems.isEmpty()) {
                    item(key = "empty_history") { EmptyHistory() }
                } else {
                    groupedHistoryItems.forEach { (date, itemsForDate) ->
                        item(key = "history_header_$date") { DateHeader(date, itemsForDate.size) }
                        items(itemsForDate, key = { "history_${it.routeId}" }) { item ->
                            PickupScheduleCard(
                                item = item,
                                isOverdue = false,
                                statusLabel = "Completed",
                                onClick = { onPickupClick(item.routeId) }
                            )
                        }
                    }
                }
            } else if (showOverdue && overdueItems.isNotEmpty()) {
                item(key = "overdue_title") {
                    Column {
                        Text(
                            "OVERDUE PICKUPS",
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Confirmed pickups from previous days that are not completed.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                groupedOverdueItems.forEach { (date, itemsForDate) ->
                    item(key = "overdue_header_$date") {
                        DateHeader(date, itemsForDate.size)
                    }
                    items(itemsForDate, key = { "overdue_${it.routeId}" }) { item ->
                        PickupScheduleCard(
                            item = item,
                            isOverdue = true,
                            onClick = { onPickupClick(item.routeId) }
                        )
                    }
                }

            } else {
                item {
                    ScheduleSummaryCard(
                        tab = selectedTab,
                        count = visibleItems.size
                    )
                }

                if (invalidCount > 0) {
                    item {
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "$invalidCount confirmed pickup${if (invalidCount == 1) " has" else "s have"} an invalid date or time and cannot be scheduled.",
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFF8A4B08),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (visibleItems.isEmpty()) {
                    item { EmptySchedule(tab = selectedTab) }
                } else {
                    groupedItems.forEach { (date, itemsForDate) ->
                        item(key = "header_$date") {
                            DateHeader(date, itemsForDate.size)
                        }
                        items(itemsForDate, key = { it.routeId }) { item ->
                            PickupScheduleCard(
                                item = item,
                                isOverdue = item.scheduledAt.before(now),
                                onClick = { onPickupClick(item.routeId) }
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun OverdueAlert(count: Int, expanded: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onToggle),
        color = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFC62828))
            Spacer(Modifier.width(10.dp))
            Text(
                "$count overdue pickup${if (count == 1) "" else "s"}",
                modifier = Modifier.weight(1f),
                color = Color(0xFFC62828),
                fontWeight = FontWeight.Bold
            )
            Text(if (expanded) "Back to Schedule" else "Show", color = Color(0xFFC62828), fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Back to schedule" else "Show overdue pickups",
                tint = Color(0xFFC62828)
            )
        }
    }
}

@Composable
private fun ScheduleSummaryCard(tab: PickupTab, count: Int) {
    val title = when (tab) {
        PickupTab.TODAY -> "Today, ${formatDisplayDate(Date())}"
        PickupTab.THIS_WEEK -> "Remaining this week"
        PickupTab.UPCOMING -> "After this week"
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = OrangeAccent.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CalendarMonth, null, tint = OrangeAccent)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text("$count pickup${if (count == 1) "" else "s"}", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun DateHeader(date: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(formatHeaderDate(date), fontWeight = FontWeight.Bold, color = Color(0xFF4E342E))
        Spacer(Modifier.weight(1f))
        Text("$count pickup${if (count == 1) "" else "s"}", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun PickupScheduleCard(
    item: PickupScheduleUiItem,
    isOverdue: Boolean,
    statusLabel: String = if (isOverdue) "Overdue" else "Confirmed",
    onClick: () -> Unit
) {
    val isCharity = item.source == PickupSource.REQUEST
    val badgeColor = when {
        isOverdue -> Color(0xFFC62828)
        statusLabel == "Completed" -> Color(0xFF2E7D32)
        else -> Color(0xFF1976D2)
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(
                    if (isCharity) Color(0xFFE8F5E9) else Color(0xFFE3F2FD),
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isCharity) Icons.Default.Business else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isCharity) Color(0xFF2E7D32) else Color(0xFF1976D2)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.recipientName, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${item.quantityText} × ${item.foodName}", color = Color.Gray, fontSize = 13.sp)
                Text(formatDisplayTime(item.pickupTimeText), color = Color.Gray, fontSize = 13.sp)
            }
            Surface(color = badgeColor.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, null, Modifier.size(44.dp), tint = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text("No completed pickups", fontWeight = FontWeight.Bold, color = Color.Gray)
            Text("Completed charity and user pickups appear here.", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmptySchedule(tab: PickupTab) {
    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CalendarMonth, null, Modifier.size(44.dp), tint = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(
                when (tab) {
                    PickupTab.TODAY -> "No pickups today"
                    PickupTab.THIS_WEEK -> "No more pickups this week"
                    PickupTab.UPCOMING -> "No upcoming pickups"
                },
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text("Approved requests and reservations appear here.", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

private fun CharityRequest.toScheduleItem(): PickupScheduleUiItem? {
    val scheduled = parseSchedule(pickupDate, pickupTime) ?: return null
    return PickupScheduleUiItem(
        id = id,
        source = PickupSource.REQUEST,
        recipientName = organisationName.ifBlank { "Charity organisation" },
        foodName = foodName,
        quantityText = quantity.toString(),
        pickupDateText = pickupDate,
        pickupTimeText = pickupTime,
        scheduledAt = scheduled
    )
}

private fun Reservation.toScheduleItem(userName: String): PickupScheduleUiItem? {
    val scheduled = parseSchedule(pickupDate, pickupTime) ?: return null
    return PickupScheduleUiItem(
        id = id,
        source = PickupSource.RESERVATION,
        recipientName = userName,
        foodName = foodName,
        quantityText = "$quantityNum $unit",
        pickupDateText = pickupDate,
        pickupTimeText = pickupTime,
        scheduledAt = scheduled
    )
}

private fun CharityRequest.toHistoryItem(): PickupScheduleUiItem = PickupScheduleUiItem(
    id = id,
    source = PickupSource.REQUEST,
    recipientName = organisationName.ifBlank { "Charity organisation" },
    foodName = foodName,
    quantityText = quantity.toString(),
    pickupDateText = pickupDate,
    pickupTimeText = pickupTime,
    scheduledAt = parseSchedule(pickupDate, pickupTime) ?: parseTimestamp(createdAt) ?: Date(0)
)

private fun Reservation.toHistoryItem(userName: String): PickupScheduleUiItem = PickupScheduleUiItem(
    id = id,
    source = PickupSource.RESERVATION,
    recipientName = userName,
    foodName = foodName,
    quantityText = "$quantityNum $unit",
    pickupDateText = pickupDate,
    pickupTimeText = pickupTime,
    scheduledAt = parseSchedule(pickupDate, pickupTime) ?: parseTimestamp(createdAt) ?: Date(0)
)

private fun parseTimestamp(value: String?): Date? {
    if (value.isNullOrBlank()) return null
    val normalized = value.substringBefore('+').removeSuffix("Z")
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )
    for (pattern in patterns) {
        try {
            return SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(normalized)
        } catch (_: Exception) {
            // Try the next accepted timestamp format.
        }
    }
    return null
}

private fun parseSchedule(date: String, time: String): Date? {
    if (date.isBlank() || time.isBlank()) return null
    val normalizedTime = time.trim().substringBefore('+').removeSuffix("Z")
    val patterns = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd h:mm a")
    for (pattern in patterns) {
        try {
            return SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
                .parse("${date.trim()} $normalizedTime")
        } catch (_: Exception) {
            // Try the next accepted database/display format.
        }
    }
    return null
}

private fun startOfDay(date: Date): Date = Calendar.getInstance().apply {
    time = date
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.time

private fun endOfDay(date: Date): Date = Calendar.getInstance().apply {
    time = date
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.time

private fun endOfWeek(date: Date): Date = Calendar.getInstance().apply {
    time = endOfDay(date)
    val daysUntilSunday = (Calendar.SUNDAY - get(Calendar.DAY_OF_WEEK) + 7) % 7
    add(Calendar.DAY_OF_YEAR, daysUntilSunday)
}.time

private fun formatDbDate(date: Date): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

private fun formatDisplayDate(date: Date): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date)

private fun formatHeaderDate(date: String): String = try {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(date)
    SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(parsed!!)
} catch (_: Exception) {
    date
}

private fun formatDisplayTime(time: String): String {
    val normalized = time.trim().substringBefore('+').removeSuffix("Z")
    val patterns = listOf("HH:mm:ss", "HH:mm", "h:mm a")
    for (pattern in patterns) {
        try {
            val parsed = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(normalized)
            return SimpleDateFormat("h:mm a", Locale.getDefault()).format(parsed!!)
        } catch (_: Exception) {
            // Try the next accepted format.
        }
    }
    return "Invalid time"
}