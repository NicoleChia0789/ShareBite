package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalConfiguration  // ✅ 新增

@Composable
fun UserMyReservationsScreen(
    paddingValues: PaddingValues,
    onViewClick: (Long) -> Unit,
    onBrowseClick: () -> Unit = {}
) {
    val userId = CurrentUser.getUserId()
    val repository = remember { ReservationRepository() }
    val configuration = LocalConfiguration.current  // ✅ 新增
    val locale = configuration.locales.get(0)       // ✅ 新增


    var allReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var filteredReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }

    val statuses = listOf("All", "Pending", "Confirmed", "Completed", "Cancelled")

    LaunchedEffect(Unit) {
        try {
            allReservations = repository.getMyReservations()
            filteredReservations = allReservations
            println("🔍 MyReservations - 找到 ${allReservations.size} 条预订")
        } catch (e: Exception) {
            errorMessage = e.message
            println("🔴 MyReservations 错误: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(selectedStatus, allReservations) {
        filteredReservations = if (selectedStatus == null || selectedStatus == "All") {
            allReservations
        } else {
            allReservations.filter { it.status == selectedStatus }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        // ── 标题 ──
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

        // ── ✅ 横向可滚动的 pill 样式 Filter Tabs ──
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

        // ── Content ──
        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }

            errorMessage != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(text = "Error: $errorMessage", color = RedCancelled)
            }

            filteredReservations.isEmpty() -> {
                // ── ✅ 空状态：emoji + 提示文字 + Browse Food 按钮 ──
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = "📋",
                            fontSize = 48.sp
                        )
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
                items(filteredReservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onViewClick = { onViewClick(reservation.id) },
                        locale = locale
                    )
                }
            }
        }
    }
}

// ── ✅ 卡片：左边信息，右边只保留 View 按钮 ──
@Composable
private fun ReservationCard(
    reservation: Reservation,
    onViewClick: () -> Unit,
    locale: Locale
) {
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

    // 判断是否今天取餐
    val isToday = try {
        val format = SimpleDateFormat("yyyy-MM-dd", locale)
        val pickupDate = format.parse(reservation.pickupDate)
        val today = Date()
        pickupDate != null && format.format(pickupDate) == format.format(today)
    } catch (e: Exception) {
        false
    }

    // 状态描述文字
    val statusHeadline = when {
        reservation.status == "Pending" && isToday -> "PICK UP TODAY"
        reservation.status == "Pending" -> "REQUESTED"
        reservation.status == "Confirmed" && isToday -> "PICK UP TODAY"
        reservation.status == "Confirmed" -> "PICK UP"
        reservation.status == "Completed" -> "COLLECTED"
        reservation.status == "Cancelled" -> "CANCELLED"
        else -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── 左侧信息 ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 状态行
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusHeadline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (reservation.status == "Cancelled") TextGrey else OrangeAccent
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusBg
                    ) {
                        Text(
                            text = reservation.status,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                // 食物名称 + 数量
                Text(
                    text = "${reservation.foodName} × ${reservation.quantity}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                // 取餐日期时间
                Text(
                    text = "${reservation.pickupDate} · ${reservation.pickupTime}",
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }

            // ── ✅ 右侧只保留 View 按钮（pill 形状，大小合理） ──
            Button(
                onClick = onViewClick,
                modifier = Modifier
                    .height(36.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "View",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}