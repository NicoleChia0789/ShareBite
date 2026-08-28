package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.ui.theme.*

@Composable
fun UserDashboardScreen(
    paddingValues: PaddingValues,
    onBrowseClick: () -> Unit,
    onFoodClick: (Long) -> Unit,
    onViewAllReservations: () -> Unit
) {
    // 临时 userId，等 Member D 做好 Auth 再换
    val userId = "test-user-id"

    val foodRepo = remember { FoodListingRepository() }
    val reservationRepo = remember { ReservationRepository() }

    // 页面状态
    var nearbyListings by remember { mutableStateOf<List<FoodListing>>(emptyList()) }
    var recentReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 页面加载时从 Supabase 拿数据
    LaunchedEffect(Unit) {
        try {
            nearbyListings = foodRepo.getAvailableListings().take(5)
            recentReservations = reservationRepo.getUserReservations(userId).take(3)
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    // 显示加载中
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
        return
    }

    // 显示错误
    if (errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Error: $errorMessage", color = RedCancelled)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Hero Banner ──
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeAccent)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Need a meal?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Browse available food donations near you.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onBrowseClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Browse Food →",
                            color = OrangeAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ── Stats Row ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 总可用 listings 数量
                DashboardStatCard(
                    label = "Available Now",
                    value = nearbyListings.size.toString(),
                    color = OrangeAccent,
                    modifier = Modifier.weight(1f)
                )
                // 用户的 reservation 数量
                DashboardStatCard(
                    label = "My Reservations",
                    value = recentReservations.size.toString(),
                    color = GreenConfirmed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Nearby Food Listings ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Available Near You",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    "View All",
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onBrowseClick() }
                )
            }
        }

        // 横向滚动的食物卡片
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(nearbyListings) { listing ->
                    FoodListingCard(
                        listing = listing,
                        onClick = { onFoodClick(listing.id) }
                    )
                }
            }
        }

        // ── Recent Reservations ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Reservations",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    "View All",
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onViewAllReservations() }
                )
            }
        }

        // 如果没有 reservation 显示提示
        if (recentReservations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No reservations yet. Start browsing!",
                        color = TextGrey,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Recent reservation cards
        items(recentReservations) { reservation ->
            RecentReservationCard(
                reservation = reservation,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 底部空间
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Food Listing Card (横向滚动用) ──
@Composable
private fun FoodListingCard(
    listing: FoodListing,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 食物图片
            if (!listing.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = listing.photoUrl,
                    contentDescription = listing.foodName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 没有图片时显示占位色块
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(Color(0xFFEFE6D9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍱", fontSize = 32.sp)
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                // 分类 badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NavSelectedBg
                ) {
                    Text(
                        listing.category,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    listing.foodName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    listing.merchantName,
                    fontSize = 11.sp,
                    color = TextGrey,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 到期时间
                Text(
                    "Ends: ${listing.deadline}",
                    fontSize = 10.sp,
                    color = RedCancelled
                )
            }
        }
    }
}

// ── Recent Reservation Card ──
@Composable
private fun RecentReservationCard(
    reservation: Reservation,
    modifier: Modifier = Modifier
) {
    // 根据 status 决定颜色
    val statusColor = when (reservation.status) {
        "Confirmed" -> GreenConfirmed
        "Pending"   -> OrangeAccent
        "Cancelled" -> RedCancelled
        "Completed" -> GreyCompleted
        else        -> TextGrey
    }
    val statusBg = when (reservation.status) {
        "Confirmed" -> Color(0xFFE3F5E6)
        "Pending"   -> NavSelectedBg
        "Cancelled" -> Color(0xFFFFEDED)
        "Completed" -> Color(0xFFEEEEEE)
        else        -> Color(0xFFEEEEEE)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 占位图
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFE6D9)),
                contentAlignment = Alignment.Center
            ) {
                Text("🍱", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reservation.foodName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Pickup: ${reservation.pickupDate}, ${reservation.pickupTime}",
                    fontSize = 12.sp,
                    color = TextGrey
                )
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusBg
            ) {
                Text(
                    reservation.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Stats Card ──
@Composable
private fun DashboardStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label.uppercase(), fontSize = 11.sp, color = TextGrey)
        }
    }
}