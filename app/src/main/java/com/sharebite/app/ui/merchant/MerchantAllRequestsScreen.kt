//package com.sharebite.app.ui.merchant
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.sharebite.app.ui.theme.BgCream
//import com.sharebite.app.ui.theme.OrangeAccent
//
//@Composable
//fun MerchantAllRequestsScreen(
//    paddingValues: PaddingValues,
//    viewModel: MerchantViewModel,
//    onRequestClick: (Long) -> Unit
//) {
//    val requests by viewModel.requests.collectAsState()
//    var selectedFilter by remember { mutableStateOf("Pending") }
//
//    val filteredRequests = when (selectedFilter) {
//        "Approved" -> requests.filter { it.status == "Confirmed" }
//        "Declined" -> requests.filter { it.status == "Cancelled" }
//        else -> requests.filter { it.status == "Pending" }
//    }
//
//    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(BgCream)) {
//        Text("Charity Requests", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
//
//        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            FilterChip(selected = selectedFilter == "Pending", onClick = { selectedFilter = "Pending" }, label = { Text("Pending") })
//            FilterChip(selected = selectedFilter == "Approved", onClick = { selectedFilter = "Approved" }, label = { Text("Approved") })
//            FilterChip(selected = selectedFilter == "Declined", onClick = { selectedFilter = "Declined" }, label = { Text("Declined") })
//        }
//
//        if (filteredRequests.isEmpty()) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No charity requests in this category.", color = Color.Gray) }
//        } else {
//            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                items(filteredRequests) { request ->
//                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFFFE8E8), RoundedCornerShape(50)))
//                                Spacer(modifier = Modifier.width(12.dp))
//                                Column {
//                                    Text(request.organisationName, fontWeight = FontWeight.Bold)
//                                    Text("${request.quantity} x ${request.foodName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//                                }
//                                Spacer(modifier = Modifier.weight(1f))
//                                Text(request.status, color = if (request.status == "Pending") OrangeAccent else Color.Gray)
//                            }
//                            Spacer(modifier = Modifier.height(12.dp))
//                            Button(onClick = { onRequestClick(request.id) }, colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent), modifier = Modifier.fillMaxWidth()) {
//                                Text("View Details", color = Color.White)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

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
import androidx.compose.ui.unit.sp   // ✅ missing import added
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

@Composable
fun MerchantAllRequestsScreen(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onRequestClick: (Long) -> Unit
) {
    val requests by viewModel.requests.collectAsState()          // Donation requests (merchant_id = me)
    val generalRequests by viewModel.generalRequests.collectAsState() // General requests (merchant_id null)

    // Tab selection
    var requestType by remember { mutableStateOf("General") } // "General" or "Donation"

    // Filter selection
    var selectedFilter by remember { mutableStateOf("Pending") }

    // Choose data source
    val allRequests = when (requestType) {
        "General" -> generalRequests
        else -> requests
    }

    val filtered = when (selectedFilter) {
        "Approved" -> allRequests.filter { it.status == "Confirmed" }
        "Declined" -> allRequests.filter { it.status == "Cancelled" }
        else -> allRequests.filter { it.status == "Pending" }
    }

    // Counts for badges
    val pendingCount = allRequests.count { it.status == "Pending" }
    val approvedCount = allRequests.count { it.status == "Confirmed" }
    val declinedCount = allRequests.count { it.status == "Cancelled" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
    ) {
        Text(
            "Charity Requests",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // 🔹 Tab Row: General Request / Donation Request
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
                    selectedContainerColor = OrangeAccent,
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
                    selectedContainerColor = OrangeAccent,
                    selectedLabelColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0)
                )
            )
        }

        // 🔹 Status filter chips with counts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Pending" to pendingCount,
                "Approved" to approvedCount,
                "Declined" to declinedCount
            ).forEach { (label, count) ->
                FilterChip(
                    selected = selectedFilter == label,
                    onClick = { selectedFilter = label },
                    label = { Text("$label ($count)", fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
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
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    onViewDetail: () -> Unit
) {
    val statusColor = when (request.status) {
        "Pending" -> OrangeAccent
        "Confirmed" -> Color(0xFF2E7D32)
        else -> Color.Red
    }
    val bgColor = when (request.status) {
        "Pending" -> OrangeAccent.copy(alpha = 0.15f)
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
                        fontSize = 18.sp   // ✅ sp now works
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // ✅ Fixed Column – now properly closed with content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        request.organisationName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "${request.quantity} × ${request.foodName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,  // ✅ correct ImageVector
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Text(
                            "${request.pickupDate} at ${request.pickupTime}",
                            fontSize = 13.sp,   // ✅ fixed: removed fontWeight = FontSize =
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
            when (request.status) {
                "Pending" -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDecline,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Decline")
                        }
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
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
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}