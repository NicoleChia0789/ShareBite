package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.UserSummary
import com.sharebite.app.data.model.Reservation as ReservationModel
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.GreenConfirmed
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.theme.RedCancelled
import com.sharebite.app.ui.theme.TextDark
import com.sharebite.app.ui.theme.TextGrey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MerchantDashboardScreen(
    paddingValues: PaddingValues,
    onAddDonationClick: () -> Unit,
    onViewAllListings: () -> Unit,
    onViewAllRequests: () -> Unit,
    onViewAllReservations: () -> Unit,
    onViewImpact: () -> Unit,
    onViewPickupHistory: () -> Unit,
    onViewPickupSchedule: () -> Unit,
    onRequestClick: (Long) -> Unit,
    onReservationClick: (Long) -> Unit,
    viewModel: MerchantViewModel
) {
    // ── 数据加载 ──────────────────────────────────────────────
    // 登录后立即加载数据，并每 30 秒刷新一次（可选）
    LaunchedEffect(Unit) {
        viewModel.loadMerchantData()
    }

    // 也可以添加一个手动刷新的状态
    val isLoading by viewModel.isLoading.collectAsState()
    val listings by viewModel.listings.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val usersById by viewModel.reservationUsers.collectAsState()
    val username = CurrentUser.getUserName()

    // ── KPI 数据 ──────────────────────────────────────────────
    val pendingCount = requests.count { it.status.equals("Pending", ignoreCase = true) } +
            reservations.count { it.status.equals("Pending", ignoreCase = true) }

    val upcomingPickupCount = requests.count { it.status.equals("Confirmed", ignoreCase = true) } +
            reservations.count { it.status.equals("Confirmed", ignoreCase = true) }

    val completedCount = requests.count { it.status.equals("Completed", ignoreCase = true) } +
            reservations.count { it.status.equals("Completed", ignoreCase = true) }

    // ── Pending Items ──────────────────────────────────────────
    val pendingItems = remember(requests, reservations) {
        val items = mutableListOf<PendingItem>()
        requests.filter { it.status.equals("Pending", ignoreCase = true) }.forEach { req ->
            items.add(PendingItem.Charity(req))
        }
        reservations.filter { it.status.equals("Pending", ignoreCase = true) }.forEach { res ->
            items.add(PendingItem.Reservation(res))
        }
        items.sortedBy { it.createdAt.orEmpty() }.take(2)
    }

    // ── Recent Activity ────────────────────────────────────────
    val recentItems = remember(reservations, requests, usersById) {
        val items = mutableListOf<ActivityItem>()
        reservations.filter { it.status.equals("Completed", ignoreCase = true) }.forEach { res ->
            items.add(
                ActivityItem.Reservation(
                    user = usersById[res.userId]?.name?.takeIf { it.isNotBlank() }
                        ?: "Individual user",
                    food = res.foodName,
                    quantity = res.quantityNum,
                    pickupTime = res.pickupTime,
                    status = res.status,
                    timestamp = res.createdAt ?: ""
                )
            )
        }
        requests.filter { it.status.equals("Completed", ignoreCase = true) }.forEach { req ->
            items.add(
                ActivityItem.Request(
                    org = req.organisationName,
                    food = req.foodName,
                    quantity = req.quantity,
                    status = req.status,
                    timestamp = req.createdAt
                )
            )
        }
        items.sortedByDescending { it.timestamp }.take(2)
    }

    // ── Recent Listings ────────────────────────────────────────
    val recentListings = remember(listings) {
        listings.filter { it.status == "available" }.take(3)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1️⃣ Welcome Header
                item {
                    Column {
                        Text(
                            "Welcome back, $username! 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            "Here is what is happening today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGrey
                        )
                    }
                }

                // 2️⃣ CTA Card（整体不可点击，只有按钮可点击）
                item {
                    CreateDonationCard(onCreateClick = onAddDonationClick)
                }

                // 3️⃣ KPI Cards（图标上，数字下，居中）
                item {
                    KPIGrid(
                        pending = pendingCount,
                        inProgress = upcomingPickupCount,
                        toShip = completedCount,
                        onPendingClick = onViewAllRequests,
                        onUpcomingClick = onViewPickupSchedule,
                        onCompletedClick = onViewPickupHistory
                    )
                }

                // 4️⃣ Awaiting Action（卡片无点击，只有按钮）
                item {
                    AwaitingActionSection(
                        pendingItems = pendingItems,
                        totalPendingCount = pendingCount,
                        usersById = usersById,
                        onViewAll = onViewAllRequests,
                        onItemClick = { item ->
                            when (item) {
                                is PendingItem.Charity -> onRequestClick(item.request.id)
                                is PendingItem.Reservation -> onReservationClick(item.reservation.id)
                            }
                        }
                    )
                }

                // 5️⃣ My Listings（保留原有设计）
                if (recentListings.isNotEmpty()) {
                    item {
                        MyListingsSection(
                            items = recentListings,
                            onViewAll = onViewAllListings,
                            onItemClick = { /* 可跳转到详情，目前没有直接方法，留空或跳转全部 */ }
                        )
                    }
                }

                // 6️⃣ Recent completed pickups
                item {
                    RecentActivitySection(
                        activities = recentItems,
                        onSeeAllClick = onViewPickupHistory
                    )
                }

                // 7️⃣ Impact Report（保留原有设计）
                item {
                    ImpactEntryCard(onViewReport = onViewImpact)
                }
            }
        }
    }
}

// ─── KPI Grid ──────────────────────────────────────────────────────
// 3 cards in a row: Pending Review, Upcoming Pickups, Completed
// 图标在上，数字和标签在下，居中

@Composable
fun KPIGrid(
    pending: Int,
    inProgress: Int,
    toShip: Int,
    onPendingClick: () -> Unit,
    onUpcomingClick: () -> Unit,
    onCompletedClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KPICard(
            icon = Icons.Default.Inbox,
            label = "Pending Review",
            value = pending.toString(),
            iconColor = OrangeAccent,
            onClick = onPendingClick,
            modifier = Modifier.weight(1f)
        )
        KPICard(
            icon = Icons.Default.LocalShipping,
            label = "Upcoming Pickups",
            value = inProgress.toString(),
            iconColor = Color(0xFF1976D2),
            onClick = onUpcomingClick,
            modifier = Modifier.weight(1f)
        )
        KPICard(
            icon = Icons.Default.Inventory2,
            label = "Completed",
            value = toShip.toString(),
            iconColor = Color(0xFFF4A261),
            onClick = onCompletedClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun KPICard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight().then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 图标在上
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
            }
            // 数字居中
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            // 标签在下方
            Box(
                modifier = Modifier.fillMaxWidth().height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextGrey,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─── Create Donation Card ──────────────────────────────────────────
// 卡片本身不可点击，只有按钮

@Composable
fun CreateDonationCard(onCreateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Share Food Now",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF4E342E)
                )
                Text(
                    "List surplus food for charities and individuals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6D4C41)
                )
            }
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(50)
            ) {
                Text("Get Started", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Awaiting Action Section ──────────────────────────────────────
// 卡片无整体点击，只有 "View Details" 按钮

@Composable
fun AwaitingActionSection(
    pendingItems: List<PendingItem>,
    totalPendingCount: Int,
    usersById: Map<String, UserSummary>,
    onViewAll: () -> Unit,
    onItemClick: (PendingItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Awaiting Action ($totalPendingCount)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            TextButton(onClick = onViewAll) {
                Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold)
            }
        }

        if (pendingItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GreenConfirmed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("All clear! No pending requests.", color = TextGrey, fontSize = 14.sp)
                }
            }
        } else {
            pendingItems.forEach { item ->
                when (item) {
                    is PendingItem.Charity -> {
                        val request = item.request
                        AwaitingActionCard(
                            title = request.organisationName,
                            food = request.foodName,
                            quantity = "${request.quantity} ×",
                            isCharity = true,
                            onClick = { onItemClick(item) }
                        )
                    }
                    is PendingItem.Reservation -> {
                        val reservation = item.reservation
                        AwaitingActionCard(
                            title = usersById[reservation.userId]?.name?.takeIf { it.isNotBlank() }
                                ?: "Individual user",
                            food = reservation.foodName,
                            quantity = "${reservation.quantityNum} ×",
                            isCharity = false,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AwaitingActionCard(
    title: String,
    food: String,
    quantity: String,
    isCharity: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCharity) OrangeAccent.copy(alpha = 0.15f)
                                else Color(0xFFE3F2FD)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = if (isCharity) OrangeAccent else Color(0xFF1976D2),
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark,
                            maxLines = 1
                        )
                        Text(
                            text = "$quantity $food",
                            fontSize = 13.sp,
                            color = TextGrey
                        )
                    }
                }

                if (isCharity) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = OrangeAccent.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = OrangeAccent
                            )
                            Text(
                                "Charity",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangeAccent
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            "User",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            // 只有按钮可点击
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Details", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─── Recent Activity Section ──────────────────────────────────────
// 卡片无点击，只有 "See all" 按钮

@Composable
fun RecentActivitySection(
    activities: List<ActivityItem>,
    onSeeAllClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Completed Pickups",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            TextButton(onClick = onSeeAllClick) {
                Text("See all", color = OrangeAccent, fontWeight = FontWeight.Bold)
            }
        }

        if (activities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Text(
                    "No recent activity.",
                    modifier = Modifier.padding(16.dp),
                    color = TextGrey,
                    fontSize = 14.sp
                )
            }
        } else {
            activities.take(1).forEach { activity ->
                when (activity) {
                    is ActivityItem.Reservation -> {
                        ActivityCard(
                            icon = Icons.Default.Person,
                            title = "Pickup completed with ${activity.user}",
                            subtitle = "${activity.quantity} × ${activity.food}",
                            meta = "User reservation completed.",
                            timestamp = activity.timestamp,
                            status = activity.status
                        )
                    }
                    is ActivityItem.Request -> {
                        ActivityCard(
                            icon = Icons.Default.Business,
                            title = "Pickup completed with ${activity.org}",
                            subtitle = "${activity.quantity} × ${activity.food}",
                            meta = "Charity pickup completed.",
                            timestamp = activity.timestamp,
                            status = activity.status
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    meta: String,
    timestamp: String?,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFE8F5E9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = GreenConfirmed
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextGrey, fontSize = 13.sp)
                Text(meta, style = MaterialTheme.typography.bodySmall, color = TextGrey, fontSize = 12.sp)
            }
            val timeAgo = formatTimeAgo(timestamp)
            Text(
                timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey,
                fontSize = 12.sp
            )
        }
    }
}

// ─── Discover Needs Section ──────────────────────────────────────
// 卡片无整体点击，只有 "Offer Help" 按钮

@Composable
fun DiscoverNeedsSection(
    items: List<CharityRequest>,
    onItemClick: (CharityRequest) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Discover Needs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            TextButton(onClick = { /* navigate to full list */ }) {
                Text("See all", color = OrangeAccent, fontWeight = FontWeight.Bold)
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { request ->
                DiscoverNeedCard(
                    request = request,
                    onClick = { onItemClick(request) }
                )
            }
        }
    }
}

@Composable
fun DiscoverNeedCard(
    request: CharityRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    request.organisationName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    fontSize = 18.sp
                )
            }

            Text(
                request.organisationName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextDark,
                maxLines = 1
            )

            Text(
                "${request.quantity} × ${request.foodName}",
                fontSize = 13.sp,
                color = TextGrey,
                maxLines = 2
            )

            Spacer(modifier = Modifier.weight(1f))

            // 只有按钮可点击
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
            ) {
                Text("Offer Help", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─── My Listings Section ──────────────────────────────────────────
// 卡片无整体点击，只有按钮（如果有的话）

@Composable
fun MyListingsSection(
    items: List<FoodListing>,
    onViewAll: () -> Unit,
    onItemClick: (FoodListing) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Listings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            TextButton(onClick = onViewAll) {
                Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold)
            }
        }

        // 改为 LazyRow，支持横向滑动
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { listing ->
                MyListingItem(
                    listing = listing,
                    onClick = { onItemClick(listing) },
                    modifier = Modifier.width(120.dp) // 设置固定宽度，使每个 item 大小一致
                )
            }
        }
    }
}

@Composable
fun MyListingItem(
    listing: FoodListing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 显示真实照片
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F0E8)),
                contentAlignment = Alignment.Center
            ) {
                if (listing.photoUrl != null) {
                    AsyncImage(
                        model = listing.photoUrl,
                        contentDescription = listing.foodName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Fastfood,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                listing.foodName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                maxLines = 1
            )
            Text(
                "${listing.remainingQuantity} left",
                fontSize = 10.sp,
                color = TextGrey
            )
            // 如果需要点击，可以加一个按钮，但这里暂时没有，因为用户没有要求在此处加按钮
            // 如果有需要，可以加一个 "View" 按钮，但用户只要求移除卡片整体点击，所以现在整体无点击
        }
    }
}

// ─── Impact Entry Card ────────────────────────────────────────────
// 卡片无整体点击，只有按钮（点击内部文字或图标都会触发，但只能通过按钮）

@Composable
fun ImpactEntryCard(onViewReport: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GreenConfirmed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Insights,
                    contentDescription = null,
                    tint = GreenConfirmed,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Impact Report",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark
                )
                Text(
                    "See your lifetime donations & badges",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey,
                    fontSize = 13.sp
                )
            }
            // 这里使用 TextButton 而不是直接 Text，确保只有按钮可点击
            TextButton(
                onClick = onViewReport,
                colors = ButtonDefaults.textButtonColors(contentColor = OrangeAccent)
            ) {
                Text("View →", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ─── Data Models ──────────────────────────────────────────────────

sealed class ActivityItem(open val timestamp: String?) {
    data class Reservation(
        val user: String,
        val food: String,
        val quantity: Int,
        val pickupTime: String,
        val status: String,
        override val timestamp: String?
    ) : ActivityItem(timestamp)

    data class Request(
        val org: String,
        val food: String,
        val quantity: Int,
        val status: String,
        override val timestamp: String?
    ) : ActivityItem(timestamp)
}

sealed class PendingItem(open val createdAt: String?) {
    data class Charity(val request: CharityRequest) : PendingItem(request.createdAt)
    data class Reservation(val reservation: ReservationModel) : PendingItem(reservation.createdAt)
}

// ─── Helper ──────────────────────────────────────────────────────

fun formatTimeAgo(timestamp: String?): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val date = format.parse(timestamp) ?: return "now"

        val diff = Date().time - date.time
        val minutes = diff / 1000 / 60
        when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 1440 -> "${minutes / 60}h ago"
            else -> "${minutes / 1440}d ago"
        }
    } catch (e: Exception) {
        "now"
    }
}