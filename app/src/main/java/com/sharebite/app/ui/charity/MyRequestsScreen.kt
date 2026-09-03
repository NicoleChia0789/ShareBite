package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.ui.theme.*
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Refresh
import com.sharebite.app.ui.components.WithdrawConfirmDialog
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.sharebite.app.data.repository.FoodListingRepository
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.Add

private val statusTabs = listOf("All", "Pending", "Confirmed", "Completed", "Cancelled")

@Composable
fun MyRequestsScreen(
    paddingValues: PaddingValues,
    initialStatus: String? = null,
    onViewClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onWithdrawClick: (Long) -> Unit,
    onRequestAgain: (CharityRequest) -> Unit,
    onCreateRequest: () -> Unit
) {
    val repository = remember { CharityRepository() }
    val listingRepository = remember { FoodListingRepository() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var allRequests by remember { mutableStateOf<List<CharityRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedStatus by rememberSaveable(initialStatus) { mutableStateOf(initialStatus ?: "All") }
    var withdrawTargetId by rememberSaveable { mutableStateOf<Long?>(null) }

    suspend fun loadRequests() {
        try {
            allRequests = repository.getMyRequests()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadRequests()
    }

    val filteredRequests = if (selectedStatus == "All") {
        allRequests
    } else {
        allRequests.filter { it.status == selectedStatus }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCream)
                .padding(paddingValues)
        ) {
            Text(
                "Track and manage your community food requests.",
                fontSize = 13.sp,
                color = TextGrey,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusTabs) { status ->
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
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                filteredRequests.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No requests found.", color = TextGrey)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredRequests) { request ->
                            RequestCard(
                                request = request,
                                onViewClick = { onViewClick(request.id) },
                                onEditClick = { onEditClick(request.id) },
                                onWithdrawClick = { withdrawTargetId = request.id },
                                onGetDirections = {
                                    val listingId = request.listingId?.toLongOrNull()
                                    if (listingId == null) {
                                        Toast.makeText(
                                            context,
                                            "No location info available for this request.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        coroutineScope.launch {
                                            try {
                                                val listing =
                                                    listingRepository.getListingById(listingId)
                                                val lat = listing.latitude
                                                val lng = listing.longitude
                                                val uri = if (lat != null && lng != null) {
                                                    Uri.parse(
                                                        "geo:$lat,$lng?q=$lat,$lng(${
                                                            Uri.encode(
                                                                listing.merchantName
                                                            )
                                                        })"
                                                    )
                                                } else {
                                                    Uri.parse("geo:0,0?q=${Uri.encode(listing.address)}")
                                                }
                                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "Could not open directions: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                onRequestAgain = {
                                    onRequestAgain(request)
                                }
                            )
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onCreateRequest,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .padding(bottom = paddingValues.calculateBottomPadding()),
            containerColor = OrangeAccent,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Food Request")
        }
    }

    // ---------- WITHDRAW 确认弹窗 ----------
    withdrawTargetId?.let { targetId ->
        WithdrawConfirmDialog(
            onKeep = { withdrawTargetId = null },
            onWithdraw = {
                coroutineScope.launch {
                    try {
                        // ✅ FIX: Use withdrawMyRequest() instead of withdrawRequest()
                        repository.withdrawMyRequest(targetId)
                        withdrawTargetId = null
                        loadRequests()
                    } catch (e: Exception) {
                        errorMessage = e.message
                        withdrawTargetId = null
                    }
                }
            }
        )
    }
}

@Composable
private fun RequestCard(
    request: CharityRequest,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onGetDirections: () -> Unit,
    onRequestAgain: () -> Unit
) {
    val isPending = request.status == "Pending"
    val isConfirmed = request.status == "Confirmed"
    val isCompleted = request.status == "Completed"
    val isCancelled = request.status == "Cancelled"

    val statusHeadline = when (request.status) {
        "Pending", "Confirmed" -> {
            if (request.pickupDate == getTodayDateString()) {
                "PICK UP TODAY"
            } else {
                "PICK UP ON ${formatShortDate(request.pickupDate).uppercase()}"
            }
        }
        "Completed" -> "COLLECTED ON ${formatShortDate(request.pickupDate).uppercase()}"
        "Cancelled" -> "CANCELLED"
        else -> ""
    }

    val (statusColor, statusBgColor) = when (request.status) {
        "Pending" -> Color(0xFFE65100) to Color(0xFFFFF3E0)
        "Confirmed" -> GreenConfirmed to Color(0xFFE8F5E9)
        "Completed" -> GreyCompleted to Color(0xFFF5F5F5)
        "Cancelled" -> RedCancelled to Color(0xFFFFEBEE)
        else -> TextGrey to Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusHeadline,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBgColor
                ) {
                    Text(
                        text = request.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${request.foodName} × ${formatWithThousandSeparator(request.quantity)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = request.organisationName,
                fontSize = 14.sp,
                color = TextGrey
            )

            Text(
                text = "${request.pickupDate} · ${request.pickupTime}",
                fontSize = 13.sp,
                color = TextGrey
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(
                color = Color(0xFFE8E2D8),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            when {
                isPending -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onViewClick, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Visibility, contentDescription = "View", tint = OrangeAccent)
                            }
                            Text("View", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OrangeAccent)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GreenConfirmed)
                            }
                            IconButton(onClick = onWithdrawClick, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Withdraw", tint = RedCancelled)
                            }
                        }
                    }
                }
                isConfirmed -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onViewClick, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Visibility, contentDescription = "View", tint = OrangeAccent)
                            }
                            Text("View", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OrangeAccent)
                        }
                        Button(
                            onClick = onGetDirections,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Directions", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                isCompleted -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onViewClick, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Visibility, contentDescription = "View", tint = OrangeAccent)
                            }
                            Text("View", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OrangeAccent)
                        }
                        OutlinedButton(
                            onClick = onRequestAgain,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenConfirmed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request Again", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                isCancelled -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onViewClick, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Visibility, contentDescription = "View", tint = OrangeAccent)
                            }
                            Text("View", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OrangeAccent)
                        }
                        Text("Cancelled", fontSize = 12.sp, color = TextGrey)
                    }
                }
                else -> {}
            }
        }
    }
}

private fun formatShortDate(dateStr: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val outputFormat = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.US)
        val date = inputFormat.parse(dateStr)
        if (date != null) outputFormat.format(date) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}
