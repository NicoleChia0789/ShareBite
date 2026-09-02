package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MerchantHistoryScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onViewImpact: (String) -> Unit = {},
    onViewReceipt: (Long) -> Unit = {}
) {
    val foodRepo = remember { FoodListingRepository() }

    var donations by remember { mutableStateOf<List<FoodListing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    // Date range filter
    var dateRange by remember { mutableStateOf("All Time") }
    val dateRangeOptions = listOf("All Time", "This Week", "This Month", "Last 3 Months")

    LaunchedEffect(Unit) {
        try {
            donations = foodRepo.getAllMyListings()
                .filter { it.status == "available" || it.status == "claimed" || it.status == "completed" }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    // Apply filters
    val filteredDonations = remember(donations, searchQuery, dateRange) {
        var filtered = donations

        // Search filter
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.foodName.contains(searchQuery, ignoreCase = true) ||
                        it.merchantName.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }

        // Date range filter
        filtered = filterByDateRange(filtered, dateRange) { it.createdAt }

        filtered
    }

    // Calculate total impact
    val totalWeight = filteredDonations.sumOf {
        it.totalQuantity.toDouble()
    }
    val totalMeals = filteredDonations.sumOf {
        it.totalQuantity * 2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        // ── Header ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark
                )
            }
            Column {
                Text(
                    text = "Donation History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Review all your past contributions to the community.",
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }
        }

        // ── Search & Filter Row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search charity or date...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = TextGrey
                    )
                },
                shape = RoundedCornerShape(50),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                colors = AppTextFieldDefaults.colors()
            )

            // Date Range Filter Button
            OutlinedButton(
                onClick = { showFilters = !showFilters },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangeAccent
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangeAccent)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Date Range",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Date Range Chips (Expandable) ──
        if (showFilters) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dateRangeOptions.forEach { option ->
                        FilterChip(
                            onClick = { dateRange = option },
                            label = { Text(option, fontSize = 11.sp) },
                            selected = dateRange == option,
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = Color.White,
                                disabledContainerColor = Color(0xFFE8E2D8),
                                disabledLabelColor = TextGrey
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // ── Impact Stats ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImpactStat(
                    icon = Icons.Default.MonitorWeight,
                    label = "Total Weight",
                    value = "${totalWeight} kg",
                    color = OrangeAccent
                )
                ImpactStat(
                    icon = Icons.Default.Restaurant,
                    label = "Est. Meals",
                    value = "${totalMeals} meals",
                    color = GreenConfirmed
                )
                ImpactStat(
                    icon = Icons.Default.People,
                    label = "Donations",
                    value = "${filteredDonations.size}",
                    color = OrangeAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }
            errorMessage != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Error: $errorMessage", color = RedCancelled)
            }
            filteredDonations.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextGrey
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No donations found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Text(
                        text = "Your past contributions will appear here.",
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDonations) { item ->
                    DonationHistoryCard(
                        listing = item,
                        onViewReceipt = { onViewReceipt(item.id) },
                        onViewImpact = { onViewImpact(item.id.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextGrey
        )
    }
}

@Composable
private fun DonationHistoryCard(
    listing: FoodListing,
    onViewReceipt: () -> Unit,
    onViewImpact: () -> Unit
) {
    val displayDate = remember(listing.createdAt) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(listing.createdAt.take(10))
            if (date != null) outputFormat.format(date) else listing.createdAt.take(10)
        } catch (e: Exception) {
            listing.createdAt.take(10)
        }
    }

    val displayTime = remember(listing.createdAt) {
        try {
            listing.createdAt.substringAfter("T").take(5)
        } catch (e: Exception) {
            "10:30 AM"
        }
    }

    val statusColor = when (listing.status) {
        "available" -> OrangeAccent
        "claimed" -> GreenConfirmed
        "completed" -> GreenConfirmed
        else -> TextGrey
    }

    val statusText = when (listing.status) {
        "available" -> "Available"
        "claimed" -> "Claimed"
        "completed" -> "Picked Up"
        else -> listing.status
    }

    val weightSaved = listing.totalQuantity.toDouble()
    val estimatedMeals = (weightSaved * 2).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewImpact() },
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
            // ── Header: Charity Name + Status ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = listing.merchantName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Date & Time ──
            Text(
                text = "$displayDate • $displayTime",
                fontSize = 13.sp,
                color = TextGrey
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Food Name ──
            Text(
                text = "Food: ${listing.foodName}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Metrics: Weight & Meals ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricItem(
                    label = "Weight Saved",
                    value = if (listing.status == "cancelled") "- kg" else "${weightSaved} kg",
                    isHighlight = true
                )
                MetricItem(
                    label = "Est. Meals",
                    value = if (listing.status == "cancelled") "- Meals" else "${estimatedMeals} Meals",
                    isHighlight = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Action Buttons: Receipt | View Impact ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewReceipt,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangeAccent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangeAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Receipt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = onViewImpact,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GreenConfirmed
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenConfirmed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "View Impact",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isHighlight) Icons.Default.Star else Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (isHighlight) OrangeAccent else TextGrey
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextGrey
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
    }
}

// ── Date Range Filter Helper ──
private fun filterByDateRange(
    items: List<FoodListing>,
    range: String,
    dateExtractor: (FoodListing) -> String
): List<FoodListing> {
    val calendar = Calendar.getInstance()
    val today = calendar.time

    val rangeStart = when (range) {
        "This Week" -> {
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.time
        }
        "This Month" -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.time
        }
        "Last 3 Months" -> {
            calendar.add(Calendar.MONTH, -3)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.time
        }
        else -> null // "All Time"
    }

    return if (rangeStart != null) {
        items.filter { item ->
            val dateStr = dateExtractor(item)
            val itemDate = parseDate(dateStr) ?: return@filter false
            itemDate >= rangeStart && itemDate <= today
        }
    } else {
        items
    }
}

private fun parseDate(dateStr: String): Date? {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.parse(dateStr.take(10))
    } catch (e: Exception) {
        null
    }
}