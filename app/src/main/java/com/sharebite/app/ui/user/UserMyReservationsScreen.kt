package com.sharebite.app.ui.user

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.components.WithdrawConfirmDialog
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

// ✅ 状态优先级函数（定义在顶层，供整个文件使用）
private fun getStatusPriority(status: String): Int {
    return when (status) {
        "Pending" -> 0
        "Confirmed" -> 1
        "Completed" -> 2
        "Cancelled" -> 3
        else -> 999
    }
}

@Composable
fun UserMyReservationsScreen(
    paddingValues: PaddingValues,
    onViewClick: (Long) -> Unit,
    onEditReservation: (Long, Long) -> Unit,
    onBrowseClick: () -> Unit = {}
) {
    val userId = CurrentUser.getUserId()
    val repository = remember { ReservationRepository() }
    val configuration = LocalConfiguration.current
    val locale = configuration.locales.get(0)
    val coroutineScope = rememberCoroutineScope()

    var allReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var filteredReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }

    var cancelTargetId by remember { mutableStateOf<Long?>(null) }

    val statuses = listOf("All", "Pending", "Confirmed", "Completed", "Cancelled")

    suspend fun loadReservations() {
        try {
            allReservations = repository.getMyReservations()
            println("🔍 MyReservations - 找到 ${allReservations.size} 条预订")
        } catch (e: Exception) {
            errorMessage = e.message
            println("🔴 MyReservations 错误: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadReservations()
    }

    LaunchedEffect(selectedStatus, allReservations) {
        // ✅ 先按状态过滤
        val filtered = if (selectedStatus == null || selectedStatus == "All") {
            allReservations
        } else {
            allReservations.filter { it.status == selectedStatus }
        }

        // ✅ 按状态优先级排序：Pending -> Confirmed -> Completed -> Cancelled
        filteredReservations = filtered.sortedBy { getStatusPriority(it.status) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "My Reservations",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Track and manage your community food pickups.",
                fontSize = 13.sp,
                color = TextGrey
            )
        }

        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statuses) { status ->
                val isSelected = if (status == "All") {
                    selectedStatus == null || selectedStatus == "All"
                } else {
                    selectedStatus == status
                }
                FilterChip(
                    onClick = {
                        selectedStatus = if (status == "All") null else status
                    },
                    label = { Text(text = status, fontSize = 13.sp) },
                    selected = isSelected,
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color(0xFFE8E2D8),
                        disabledLabelColor = TextGrey
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }

            errorMessage != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(text = "Error: $errorMessage", color = RedCancelled)
            }

            filteredReservations.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(text = "📋", fontSize = 48.sp)
                        Text(
                            text = "No reservations yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Start browsing food to make your first reservation!",
                            fontSize = 14.sp,
                            color = TextGrey,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onBrowseClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Browse Food",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredReservations,
                    key = { it.id }
                ) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onViewClick = { onViewClick(reservation.id) },
                        onEditClick = { onEditReservation(reservation.listingId, reservation.id) },
                        onCancelClick = { cancelTargetId = reservation.id },
                        locale = locale
                    )
                }
            }
        }
    }

    cancelTargetId?.let { targetId ->
        WithdrawConfirmDialog(
            onKeep = { cancelTargetId = null },
            onWithdraw = {
                coroutineScope.launch {
                    try {
                        repository.cancelMyReservation(targetId)
                        allReservations = allReservations.map { reservation ->
                            if (reservation.id == targetId) {
                                reservation.copy(status = "Cancelled")
                            } else {
                                reservation
                            }
                        }
                        filteredReservations = filteredReservations.map { reservation ->
                            if (reservation.id == targetId) {
                                reservation.copy(status = "Cancelled")
                            } else {
                                reservation
                            }
                        }
                        // ✅ 取消后重新排序
                        filteredReservations = filteredReservations.sortedBy { getStatusPriority(it.status) }
                        cancelTargetId = null
                    } catch (e: Exception) {
                        errorMessage = e.message
                        cancelTargetId = null
                    }
                }
            }
        )
    }
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    locale: Locale
) {
    val isPending = reservation.status == "Pending"
    val isConfirmed = reservation.status == "Confirmed"
    val isCompleted = reservation.status == "Completed"
    val isCancelled = reservation.status == "Cancelled"

    val statusColor = when (reservation.status) {
        "Confirmed" -> GreenConfirmed
        "Pending" -> OrangeAccent
        "Cancelled" -> RedCancelled
        "Completed" -> GreyCompleted
        else -> TextGrey
    }
    val statusBg = when (reservation.status) {
        "Confirmed" -> Color(0xFFE3F5E6)
        "Pending" -> NavSelectedBg
        "Cancelled" -> Color(0xFFFFEDED)
        "Completed" -> Color(0xFFEEEEEE)
        else -> Color(0xFFEEEEEE)
    }

    val statusHeadline = when {
        isPending -> "PENDING"
        isConfirmed -> "CONFIRMED"
        isCompleted -> "COMPLETED"
        isCancelled -> "CANCELLED"
        else -> ""
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
                    color = statusBg
                ) {
                    Text(
                        text = reservation.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${reservation.foodName} × ${reservation.quantity}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${reservation.pickupDate} · ${reservation.pickupTime}",
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
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GreenConfirmed)
                            }
                            IconButton(onClick = onCancelClick, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = RedCancelled)
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
                        Text("Confirmed", fontSize = 12.sp, color = TextGrey)
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
                        Text("Completed", fontSize = 12.sp, color = TextGrey)
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
            }
        }
    }
}