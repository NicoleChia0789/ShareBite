package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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

@Composable
fun MerchantAllRequestsScreen(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onRequestClick: (Long) -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val generalRequests by viewModel.generalRequests.collectAsState()

    var requestType by remember { mutableStateOf("General") }
    var selectedFilter by remember { mutableStateOf("Confirmed") }
    var pickupRange by remember { mutableStateOf(MerchantPickupRange.TODAY) }

    val allRequests = when (requestType) {
        // A Bazaar request starts in generalRequests, then moves into requests
        // after this merchant offers help. Keep both stages visible here.
        "General" -> (generalRequests + requests.filter { it.listingId.isNullOrBlank() })
            .distinctBy { it.id }
        else -> requests.filter { !it.listingId.isNullOrBlank() }
    }

    val statusFiltered = when (selectedFilter) {
        "Confirmed" -> allRequests.filter { it.status == "Confirmed" }
        "Declined" -> allRequests.filter { it.status == "Cancelled" }
        else -> allRequests.filter { it.status == "Pending" }
    }
    val filtered = statusFiltered.filter {
        matchesMerchantPickupRange(it.pickupDate, it.pickupTime, pickupRange)
    }

    val pendingCount = allRequests.count { it.status == "Pending" }
    val approvedCount = allRequests.count { it.status == "Confirmed" }
    val declinedCount = allRequests.count { it.status == "Cancelled" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MerchantPickupRange.entries.forEach { range ->
                FilterChip(
                    selected = pickupRange == range,
                    onClick = { pickupRange = range },
                    label = { Text(if (range == MerchantPickupRange.TODAY) "Today" else if (range == MerchantPickupRange.THIS_WEEK) "This Week" else "Upcoming") }
                )
            }
        }
//        Text(
//            "Charity Requests",
//            style = MaterialTheme.typography.headlineSmall,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(16.dp)
//        )

        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = requestType == "General",
                onClick = { requestType = "General" },
                label = { Text("General Request") },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0)
                )
            )
            FilterChip(
                selected = requestType == "Donation",
                onClick = { requestType = "Donation" },
                label = { Text("Donation Request") },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0)
                )
            )
        }

        // Status filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Pending" to pendingCount,
                "Confirmed" to approvedCount,
                "Declined" to declinedCount
            ).forEach { (label, count) ->
                FilterChip(
                    selected = selectedFilter == label,
                    onClick = { selectedFilter = label },
                    label = { Text("$label ($count)", fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No requests in this category.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { request ->
                    RequestCardEnhanced(
                        request = request,
                        showApprovalActions = request.listingId.isNullOrBlank(),
                        onApprove = {
                            viewModel.updateRequestStatus(request.id, "Confirmed")
                        },
                        onDecline = {
                            viewModel.updateRequestStatus(request.id, "Cancelled")
                        },
                        onViewDetail = { onRequestClick(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestCardEnhanced(
    request: CharityRequest,
    showApprovalActions: Boolean = true,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    onViewDetail: () -> Unit
) {
    val statusColor = when (request.status) {
        "Pending" -> MaterialTheme.colorScheme.primary
        "Confirmed" -> Color(0xFF2E7D32)
        else -> Color.Red
    }
    val bgColor = when (request.status) {
        "Pending" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        "Confirmed" -> Color(0xFFE8F5E9)
        else -> Color(0xFFFFEBEE)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        request.organisationName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                // ✅ 食物名作为标题
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        request.foodName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "${request.quantity} × from ${request.organisationName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    // ✅ 时间显示优化
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Text(
                            "${request.pickupDate}  ${request.pickupTime}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
                // Status badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = bgColor
                ) {
                    Text(
                        request.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            // Actions
            when {
                request.status == "Pending" && showApprovalActions -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // ✅ Decline 按钮：红色底白字，宽度 0.6f
                        OutlinedButton(
                            onClick = onViewDetail,
                            modifier = Modifier.weight(0.6f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("View Details", color = Color.White)
                        }
                        // ✅ Approve 按钮：橙色，宽度 1f
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Approve", color = Color.White)
                        }
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = onViewDetail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}
