//////////package com.sharebite.app.ui.merchant
//////////
//////////import androidx.compose.foundation.background
//////////import androidx.compose.foundation.clickable
//////////import androidx.compose.foundation.layout.*
//////////import androidx.compose.foundation.lazy.LazyColumn
//////////import androidx.compose.foundation.lazy.items
//////////import androidx.compose.foundation.shape.RoundedCornerShape
//////////import androidx.compose.material.icons.Icons
//////////import androidx.compose.material.icons.filled.Add
//////////import androidx.compose.material3.*
//////////import androidx.compose.runtime.*
//////////import androidx.compose.ui.Alignment
//////////import androidx.compose.ui.Modifier
//////////import androidx.compose.ui.graphics.Color
//////////import androidx.compose.ui.text.font.FontWeight
//////////import androidx.compose.ui.unit.dp
//////////import androidx.compose.ui.unit.sp
//////////import com.sharebite.app.data.model.CharityRequest
//////////import com.sharebite.app.data.session.CurrentUser
//////////import com.sharebite.app.ui.theme.BgCream
//////////import com.sharebite.app.ui.theme.OrangeAccent
//////////import kotlinx.coroutines.delay
//////////
//////////@Composable
//////////fun MerchantDashboardScreen(
//////////    paddingValues: PaddingValues,
//////////    onAddDonationClick: () -> Unit,
//////////    onViewAllListings: () -> Unit,
//////////    onViewAllRequests: () -> Unit,
//////////    onViewAllReservations: () -> Unit,
//////////    onViewImpact: () -> Unit,
//////////    onRequestClick: (Long) -> Unit,
//////////    onReservationClick: (Long) -> Unit,
//////////    viewModel: MerchantViewModel
//////////) {
//////////    val listings by viewModel.listings.collectAsState()
//////////    val generalRequests by viewModel.generalRequests.collectAsState()
//////////    val reservations by viewModel.reservations.collectAsState()
//////////    val isLoading by viewModel.isLoading.collectAsState()
//////////    val username = CurrentUser.getUserName()
//////////
//////////    // 🔧 测试数据（如果真实数据为空，使用这个）
//////////    val testRequests = remember {
//////////        listOf(
//////////            CharityRequest(
//////////                id = 999,
//////////                organisationName = "🏪 社区食物银行",
//////////                foodName = "🍞 手工面包",
//////////                quantity = 15,
//////////                status = "Pending",
//////////                pickupDate = "2026-09-05",
//////////                pickupTime = "10:00 AM"
//////////            ),
//////////            CharityRequest(
//////////                id = 998,
//////////                organisationName = "🥫 城市救助站",
//////////                foodName = "🥫 罐头食品",
//////////                quantity = 30,
//////////                status = "Pending",
//////////                pickupDate = "2026-09-06",
//////////                pickupTime = "02:00 PM"
//////////            )
//////////        )
//////////    }
//////////
//////////    // 使用真实数据，如果为空则用测试数据
//////////    val displayRequests = if (generalRequests.isNotEmpty()) generalRequests else testRequests
//////////
//////////    LaunchedEffect(Unit) {
//////////        viewModel.loadMerchantData()
//////////        viewModel.loadGeneralRequests()
//////////        // Force a manual refresh after 2 seconds
//////////        delay(2000)
//////////        viewModel.loadMerchantData()
//////////    }
//////////
//////////    LaunchedEffect(generalRequests) {
//////////        println("📦 generalRequests 数量: ${generalRequests.size}")
//////////    }
//////////
//////////    Box(
//////////        modifier = Modifier
//////////            .fillMaxSize()
//////////            .padding(paddingValues)
//////////            .background(BgCream)
//////////    ) {
//////////        if (isLoading) {
//////////            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//////////                CircularProgressIndicator(color = OrangeAccent)
//////////            }
//////////        } else {
//////////            LazyColumn(
//////////                modifier = Modifier.fillMaxSize(),
//////////                contentPadding = PaddingValues(16.dp),
//////////                verticalArrangement = Arrangement.spacedBy(14.dp)
//////////            ) {
//////////                // 1. Welcome Header
////////////                item {
////////////                    Column {
////////////                        Text(
////////////                            "Welcome, $username! 👋",
////////////                            style = MaterialTheme.typography.headlineSmall,
////////////                            fontWeight = FontWeight.Bold,
////////////                            color = Color(0xFF1E1B16)
////////////                        )
////////////                        Text(
////////////                            "Manage your donations and requests.",
////////////                            style = MaterialTheme.typography.bodyMedium,
////////////                            color = Color.Gray
////////////                        )
////////////                    }
////////////                }
//////////                // 1. Welcome Header
//////////                item {
//////////                    Column {
//////////                        Text(
//////////                            "Welcome, $username! 👋",
//////////                            style = MaterialTheme.typography.headlineSmall,
//////////                            fontWeight = FontWeight.Bold,
//////////                            color = Color(0xFF1E1B16)
//////////                        )
//////////                        Text(
//////////                            "Manage your donations and requests.",
//////////                            style = MaterialTheme.typography.bodyMedium,
//////////                            color = Color.Gray
//////////                        )
//////////
//////////                        // ✅ DEBUG: Show the actual logged-in ID and reservation count
//////////                        Text(
//////////                            text = "🔍 Debug: Logged in as: ${CurrentUser.getUserId()}",
//////////                            color = Color.Red,
//////////                            fontSize = 12.sp,
//////////                            modifier = Modifier.padding(top = 8.dp)
//////////                        )
//////////                        Text(
//////////                            text = "📦 Reservations in ViewModel: ${reservations.size}",
//////////                            color = Color.Red,
//////////                            fontSize = 12.sp
//////////                        )
//////////                    }
//////////                }
//////////
//////////                // 2. Stats Card
//////////                item {
//////////                    Card(
//////////                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E8)),
//////////                        shape = RoundedCornerShape(16.dp),
//////////                        modifier = Modifier.fillMaxWidth()
//////////                    ) {
//////////                        Column(modifier = Modifier.padding(20.dp)) {
//////////                            Text("TOTAL IMPACT", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//////////                            Text("1,240", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
//////////                            Text("LBS DIVERTED", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
//////////                        }
//////////                    }
//////////                }
//////////
//////////                // 3. View Impact Report
//////////                item {
//////////                    Card(
//////////                        colors = CardDefaults.cardColors(containerColor = Color.White),
//////////                        shape = RoundedCornerShape(16.dp),
//////////                        modifier = Modifier
//////////                            .fillMaxWidth()
//////////                            .clickable { onViewImpact() }
//////////                    ) {
//////////                        Row(
//////////                            modifier = Modifier
//////////                                .fillMaxWidth()
//////////                                .padding(horizontal = 20.dp, vertical = 16.dp),
//////////                            verticalAlignment = Alignment.CenterVertically,
//////////                            horizontalArrangement = Arrangement.SpaceBetween
//////////                        ) {
//////////                            Row(verticalAlignment = Alignment.CenterVertically) {
//////////                                Box(
//////////                                    modifier = Modifier
//////////                                        .size(40.dp)
//////////                                        .background(Color(0xFFFFF1E8), RoundedCornerShape(50)),
//////////                                    contentAlignment = Alignment.Center
//////////                                ) {
//////////                                    Text("📊", fontSize = 18.sp)
//////////                                }
//////////                                Spacer(modifier = Modifier.width(12.dp))
//////////                                Column {
//////////                                    Text("Impact Report", fontWeight = FontWeight.Bold)
//////////                                    Text("See your lifetime donations & badges", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//////////                                }
//////////                            }
//////////                            Text("View →", color = OrangeAccent, fontWeight = FontWeight.Bold)
//////////                        }
//////////                    }
//////////                }
//////////
//////////                item {
//////////                    Button(
//////////                        onClick = { viewModel.loadMerchantData() },
//////////                        modifier = Modifier.fillMaxWidth(),
//////////                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
//////////                    ) {
//////////                        Text("🔄 Force Refresh Reservations", color = Color.White)
//////////                    }
//////////                }
//////////
//////////                // 4. Active Donations
//////////                if (listings.isNotEmpty()) {
//////////                    item {
//////////                        Row(
//////////                            modifier = Modifier.fillMaxWidth(),
//////////                            horizontalArrangement = Arrangement.SpaceBetween,
//////////                            verticalAlignment = Alignment.CenterVertically
//////////                        ) {
//////////                            Text("Your Active Donations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//////////                            TextButton(onClick = onViewAllListings) { Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold) }
//////////                        }
//////////                    }
//////////                    items(listings.take(2)) { listing ->
//////////                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
//////////                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
//////////                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp)))
//////////                                Spacer(modifier = Modifier.width(16.dp))
//////////                                Column(modifier = Modifier.weight(1f)) {
//////////                                    Text(listing.foodName, fontWeight = FontWeight.SemiBold)
//////////                                    Text("${listing.quantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//////////                                    Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
//////////                                }
//////////                            }
//////////                        }
//////////                    }
//////////                }
//////////
//////////                // ✅ 5. General Charity Requests - 使用 displayRequests
//////////                item {
//////////                    Row(
//////////                        modifier = Modifier.fillMaxWidth(),
//////////                        horizontalArrangement = Arrangement.SpaceBetween,
//////////                        verticalAlignment = Alignment.CenterVertically
//////////                    ) {
//////////                        Text("🍽️ General Food Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//////////                        TextButton(onClick = onViewAllRequests) {
//////////                            Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold)
//////////                        }
//////////                    }
//////////                }
//////////
//////////                // 显示请求卡片（使用 displayRequests）
//////////                items(displayRequests.take(3)) { request ->
//////////                    Card(
//////////                        colors = CardDefaults.cardColors(containerColor = Color.White),
//////////                        shape = RoundedCornerShape(12.dp),
//////////                        modifier = Modifier.fillMaxWidth()
//////////                    ) {
//////////                        Column(modifier = Modifier.padding(16.dp)) {
//////////                            Row(verticalAlignment = Alignment.CenterVertically) {
//////////                                Box(
//////////                                    modifier = Modifier
//////////                                        .size(40.dp)
//////////                                        .background(
//////////                                            if (request.status == "Pending") Color(0xFFFFE8E8)
//////////                                            else Color(0xFFE8F5E9),
//////////                                            RoundedCornerShape(50)
//////////                                        )
//////////                                )
//////////                                Spacer(modifier = Modifier.width(12.dp))
//////////                                Column {
//////////                                    Text(
//////////                                        if (request.organisationName.isNotBlank()) request.organisationName else "未知机构",
//////////                                        fontWeight = FontWeight.Bold
//////////                                    )
//////////                                    Text(
//////////                                        "${request.quantity} x ${request.foodName}",
//////////                                        style = MaterialTheme.typography.bodySmall,
//////////                                        color = Color.Gray
//////////                                    )
//////////                                }
//////////                                Spacer(modifier = Modifier.weight(1f))
//////////                                Text(
//////////                                    request.status,
//////////                                    color = if (request.status == "Pending") OrangeAccent else Color.Gray,
//////////                                    fontSize = 12.sp,
//////////                                    fontWeight = FontWeight.Bold
//////////                                )
//////////                            }
//////////                            Spacer(modifier = Modifier.height(12.dp))
//////////                            OutlinedButton(
//////////                                onClick = { onRequestClick(request.id) },
//////////                                modifier = Modifier.fillMaxWidth(),
//////////                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
//////////                            ) {
//////////                                Text("View Details")
//////////                            }
//////////                        }
//////////                    }
//////////                }
//////////
//////////                // 6. User Reservations
//////////                if (reservations.isNotEmpty()) {
//////////                    item {
//////////                        Row(
//////////                            modifier = Modifier.fillMaxWidth(),
//////////                            horizontalArrangement = Arrangement.SpaceBetween,
//////////                            verticalAlignment = Alignment.CenterVertically
//////////                        ) {
//////////                            Text("User Reservations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//////////                            TextButton(onClick = onViewAllReservations) { Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold) }
//////////                        }
//////////                    }
//////////                    items(reservations.take(2)) { reservation ->
//////////                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
//////////                            Column(modifier = Modifier.padding(16.dp)) {
//////////                                Row(verticalAlignment = Alignment.CenterVertically) {
//////////                                    Box(modifier = Modifier.size(40.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(50)))
//////////                                    Spacer(modifier = Modifier.width(12.dp))
//////////                                    Column {
//////////                                        Text("User: ${reservation.userId.take(8)}", fontWeight = FontWeight.Bold)
//////////                                        Text("Reserved: ${reservation.quantity} x ${reservation.foodName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//////////                                        Text("${reservation.pickupDate} at ${reservation.pickupTime}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//////////                                    }
//////////                                    Spacer(modifier = Modifier.weight(1f))
//////////                                    Text(
//////////                                        reservation.status,
//////////                                        color = if (reservation.status == "Pending") OrangeAccent else Color.Gray,
//////////                                        fontSize = 12.sp,
//////////                                        fontWeight = FontWeight.Bold
//////////                                    )
//////////                                }
//////////                                Spacer(modifier = Modifier.height(12.dp))
//////////                                OutlinedButton(
//////////                                    onClick = { onReservationClick(reservation.id) },
//////////                                    modifier = Modifier.fillMaxWidth(),
//////////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
//////////                                ) {
//////////                                    Text("View Details")
//////////                                }
//////////                            }
//////////                        }
//////////                    }
//////////                }
//////////
//////////                // Empty state
//////////                if (listings.isEmpty() && reservations.isEmpty()) {
//////////                    item {
//////////                        Box(
//////////                            modifier = Modifier
//////////                                .fillMaxWidth()
//////////                                .padding(32.dp),
//////////                            contentAlignment = Alignment.Center
//////////                        ) {
//////////                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//////////                                Text("No donations or requests yet.", color = Color.Gray)
//////////                                Spacer(modifier = Modifier.height(8.dp))
//////////                                Text("Tap the + button to add your first donation.", color = Color.Gray)
//////////                            }
//////////                        }
//////////                    }
//////////                }
//////////            }
//////////        }
//////////
//////////        // FAB 按钮
//////////        FloatingActionButton(
//////////            onClick = onAddDonationClick,
//////////            containerColor = OrangeAccent,
//////////            modifier = Modifier
//////////                .align(Alignment.BottomEnd)
//////////                .padding(24.dp)
//////////        ) {
//////////            Icon(Icons.Default.Add, contentDescription = "Add Donation", tint = Color.White)
//////////        }
//////////    }
//////////}
////////
////////package com.sharebite.app.ui.merchant
////////
////////import androidx.compose.foundation.background
////////import androidx.compose.foundation.clickable
////////import androidx.compose.foundation.layout.*
////////import androidx.compose.foundation.lazy.LazyColumn
////////import androidx.compose.foundation.lazy.items
////////import androidx.compose.foundation.shape.RoundedCornerShape
////////import androidx.compose.material.icons.Icons
////////import androidx.compose.material.icons.filled.Add
////////import androidx.compose.material3.*
////////import androidx.compose.runtime.*
////////import androidx.compose.ui.Alignment
////////import androidx.compose.ui.Modifier
////////import androidx.compose.ui.graphics.Color
////////import androidx.compose.ui.text.font.FontWeight
////////import androidx.compose.ui.unit.dp
////////import androidx.compose.ui.unit.sp
////////import com.sharebite.app.data.model.CharityRequest
////////import com.sharebite.app.data.session.CurrentUser
////////import com.sharebite.app.ui.theme.BgCream
////////import com.sharebite.app.ui.theme.OrangeAccent
////////
////////@Composable
////////fun MerchantDashboardScreen(
////////    paddingValues: PaddingValues,
////////    onAddDonationClick: () -> Unit,
////////    onViewAllListings: () -> Unit,
////////    onViewAllRequests: () -> Unit,
////////    onViewAllReservations: () -> Unit,
////////    onViewImpact: () -> Unit,
////////    onRequestClick: (Long) -> Unit,
////////    onReservationClick: (Long) -> Unit,
////////    viewModel: MerchantViewModel
////////) {
////////    val listings by viewModel.listings.collectAsState()
////////    val generalRequests by viewModel.generalRequests.collectAsState()
////////    val reservations by viewModel.reservations.collectAsState()
////////    val isLoading by viewModel.isLoading.collectAsState()
////////    val username = CurrentUser.getUserName()
////////
////////    // 🔧 测试数据（如果真实数据为空，使用这个）
////////    val testRequests = remember {
////////        listOf(
////////            CharityRequest(
////////                id = 999,
////////                organisationName = "🏪 社区食物银行",
////////                foodName = "🍞 手工面包",
////////                quantity = 15,
////////                status = "Pending",
////////                pickupDate = "2026-09-05",
////////                pickupTime = "10:00 AM"
////////            ),
////////            CharityRequest(
////////                id = 998,
////////                organisationName = "🥫 城市救助站",
////////                foodName = "🥫 罐头食品",
////////                quantity = 30,
////////                status = "Pending",
////////                pickupDate = "2026-09-06",
////////                pickupTime = "02:00 PM"
////////            )
////////        )
////////    }
////////
////////    // 使用真实数据，如果为空则用测试数据
////////    val displayRequests = if (generalRequests.isNotEmpty()) generalRequests else testRequests
////////
////////    LaunchedEffect(Unit) {
////////        viewModel.loadMerchantData()
////////        viewModel.loadGeneralRequests()
////////    }
////////
////////    LaunchedEffect(generalRequests) {
////////        println("📦 generalRequests 数量: ${generalRequests.size}")
////////    }
////////
////////    Box(
////////        modifier = Modifier
////////            .fillMaxSize()
////////            .padding(paddingValues)
////////            .background(BgCream)
////////    ) {
////////        if (isLoading) {
////////            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
////////                CircularProgressIndicator(color = OrangeAccent)
////////            }
////////        } else {
////////            LazyColumn(
////////                modifier = Modifier.fillMaxSize(),
////////                contentPadding = PaddingValues(16.dp),
////////                verticalArrangement = Arrangement.spacedBy(14.dp)
////////            ) {
////////                // 1. Welcome Header
////////                item {
////////                    Column {
////////                        Text(
////////                            "Welcome, $username! 👋",
////////                            style = MaterialTheme.typography.headlineSmall,
////////                            fontWeight = FontWeight.Bold,
////////                            color = Color(0xFF1E1B16)
////////                        )
////////                        Text(
////////                            "Manage your donations and requests.",
////////                            style = MaterialTheme.typography.bodyMedium,
////////                            color = Color.Gray
////////                        )
////////                    }
////////                }
////////
////////                // 2. Stats Card
////////                item {
////////                    Card(
////////                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E8)),
////////                        shape = RoundedCornerShape(16.dp),
////////                        modifier = Modifier.fillMaxWidth()
////////                    ) {
////////                        Column(modifier = Modifier.padding(20.dp)) {
////////                            Text("TOTAL IMPACT", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
////////                            Text("1,240", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
////////                            Text("LBS DIVERTED", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
////////                        }
////////                    }
////////                }
////////
////////                // 3. View Impact Report
////////                item {
////////                    Card(
////////                        colors = CardDefaults.cardColors(containerColor = Color.White),
////////                        shape = RoundedCornerShape(16.dp),
////////                        modifier = Modifier
////////                            .fillMaxWidth()
////////                            .clickable { onViewImpact() }
////////                    ) {
////////                        Row(
////////                            modifier = Modifier
////////                                .fillMaxWidth()
////////                                .padding(horizontal = 20.dp, vertical = 16.dp),
////////                            verticalAlignment = Alignment.CenterVertically,
////////                            horizontalArrangement = Arrangement.SpaceBetween
////////                        ) {
////////                            Row(verticalAlignment = Alignment.CenterVertically) {
////////                                Box(
////////                                    modifier = Modifier
////////                                        .size(40.dp)
////////                                        .background(Color(0xFFFFF1E8), RoundedCornerShape(50)),
////////                                    contentAlignment = Alignment.Center
////////                                ) {
////////                                    Text("📊", fontSize = 18.sp)
////////                                }
////////                                Spacer(modifier = Modifier.width(12.dp))
////////                                Column {
////////                                    Text("Impact Report", fontWeight = FontWeight.Bold)
////////                                    Text("See your lifetime donations & badges", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
////////                                }
////////                            }
////////                            Text("View →", color = OrangeAccent, fontWeight = FontWeight.Bold)
////////                        }
////////                    }
////////                }
////////
////////                // 4. Active Donations
////////                if (listings.isNotEmpty()) {
////////                    item {
////////                        Row(
////////                            modifier = Modifier.fillMaxWidth(),
////////                            horizontalArrangement = Arrangement.SpaceBetween,
////////                            verticalAlignment = Alignment.CenterVertically
////////                        ) {
////////                            Text("Your Active Donations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
////////                            TextButton(onClick = onViewAllListings) { Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold) }
////////                        }
////////                    }
////////                    items(listings.take(2)) { listing ->
////////                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
////////                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
////////                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp)))
////////                                Spacer(modifier = Modifier.width(16.dp))
////////                                Column(modifier = Modifier.weight(1f)) {
////////                                    Text(listing.foodName, fontWeight = FontWeight.SemiBold)
////////                                    Text("${listing.quantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
////////                                    Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
////////                                }
////////                            }
////////                        }
////////                    }
////////                }
////////
////////                // ✅ 5. General Charity Requests - 使用 displayRequests
////////                item {
////////                    Row(
////////                        modifier = Modifier.fillMaxWidth(),
////////                        horizontalArrangement = Arrangement.SpaceBetween,
////////                        verticalAlignment = Alignment.CenterVertically
////////                    ) {
////////                        Text("🍽️ General Food Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
////////                        TextButton(onClick = onViewAllRequests) {
////////                            Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold)
////////                        }
////////                    }
////////                }
////////
////////                // 显示请求卡片（使用 displayRequests）
////////                items(displayRequests.take(3)) { request ->
////////                    Card(
////////                        colors = CardDefaults.cardColors(containerColor = Color.White),
////////                        shape = RoundedCornerShape(12.dp),
////////                        modifier = Modifier.fillMaxWidth()
////////                    ) {
////////                        Column(modifier = Modifier.padding(16.dp)) {
////////                            Row(verticalAlignment = Alignment.CenterVertically) {
////////                                Box(
////////                                    modifier = Modifier
////////                                        .size(40.dp)
////////                                        .background(
////////                                            if (request.status == "Pending") Color(0xFFFFE8E8)
////////                                            else Color(0xFFE8F5E9),
////////                                            RoundedCornerShape(50)
////////                                        )
////////                                )
////////                                Spacer(modifier = Modifier.width(12.dp))
////////                                Column {
////////                                    Text(
////////                                        if (request.organisationName.isNotBlank()) request.organisationName else "未知机构",
////////                                        fontWeight = FontWeight.Bold
////////                                    )
////////                                    Text(
////////                                        "${request.quantity} x ${request.foodName}",
////////                                        style = MaterialTheme.typography.bodySmall,
////////                                        color = Color.Gray
////////                                    )
////////                                }
////////                                Spacer(modifier = Modifier.weight(1f))
////////                                Text(
////////                                    request.status,
////////                                    color = if (request.status == "Pending") OrangeAccent else Color.Gray,
////////                                    fontSize = 12.sp,
////////                                    fontWeight = FontWeight.Bold
////////                                )
////////                            }
////////                            Spacer(modifier = Modifier.height(12.dp))
////////                            OutlinedButton(
////////                                onClick = { onRequestClick(request.id) },
////////                                modifier = Modifier.fillMaxWidth(),
////////                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
////////                            ) {
////////                                Text("View Details")
////////                            }
////////                        }
////////                    }
////////                }
////////
////////                // 6. User Reservations
////////                if (reservations.isNotEmpty()) {
////////                    item {
////////                        Row(
////////                            modifier = Modifier.fillMaxWidth(),
////////                            horizontalArrangement = Arrangement.SpaceBetween,
////////                            verticalAlignment = Alignment.CenterVertically
////////                        ) {
////////                            Text("User Reservations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
////////                            TextButton(onClick = onViewAllReservations) { Text("View All", color = OrangeAccent, fontWeight = FontWeight.Bold) }
////////                        }
////////                    }
////////                    items(reservations.take(2)) { reservation ->
////////                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
////////                            Column(modifier = Modifier.padding(16.dp)) {
////////                                Row(verticalAlignment = Alignment.CenterVertically) {
////////                                    Box(modifier = Modifier.size(40.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(50)))
////////                                    Spacer(modifier = Modifier.width(12.dp))
////////                                    Column {
////////                                        Text("User: ${reservation.userId.take(8)}", fontWeight = FontWeight.Bold)
////////                                        Text("Reserved: ${reservation.quantity} x ${reservation.foodName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
////////                                        Text("${reservation.pickupDate} at ${reservation.pickupTime}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
////////                                    }
////////                                    Spacer(modifier = Modifier.weight(1f))
////////                                    Text(
////////                                        reservation.status,
////////                                        color = if (reservation.status == "Pending") OrangeAccent else Color.Gray,
////////                                        fontSize = 12.sp,
////////                                        fontWeight = FontWeight.Bold
////////                                    )
////////                                }
////////                                Spacer(modifier = Modifier.height(12.dp))
////////                                OutlinedButton(
////////                                    onClick = { onReservationClick(reservation.id) },
////////                                    modifier = Modifier.fillMaxWidth(),
////////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
////////                                ) {
////////                                    Text("View Details")
////////                                }
////////                            }
////////                        }
////////                    }
////////                }
////////
////////                // Empty state
////////                if (listings.isEmpty() && reservations.isEmpty()) {
////////                    item {
////////                        Box(
////////                            modifier = Modifier
////////                                .fillMaxWidth()
////////                                .padding(32.dp),
////////                            contentAlignment = Alignment.Center
////////                        ) {
////////                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
////////                                Text("No donations or requests yet.", color = Color.Gray)
////////                                Spacer(modifier = Modifier.height(8.dp))
////////                                Text("Tap the + button to add your first donation.", color = Color.Gray)
////////                            }
////////                        }
////////                    }
////////                }
////////            }
////////        }
////////
////////        // FAB 按钮
////////        FloatingActionButton(
////////            onClick = onAddDonationClick,
////////            containerColor = OrangeAccent,
////////            modifier = Modifier
////////                .align(Alignment.BottomEnd)
////////                .padding(24.dp)
////////        ) {
////////            Icon(Icons.Default.Add, contentDescription = "Add Donation", tint = Color.White)
////////        }
////////    }
////////}
//////
//////package com.sharebite.app.ui.merchant
//////
//////import androidx.compose.foundation.background
//////import androidx.compose.foundation.clickable
//////import androidx.compose.foundation.layout.*
//////import androidx.compose.foundation.lazy.LazyColumn
//////import androidx.compose.foundation.lazy.items
//////import androidx.compose.foundation.shape.RoundedCornerShape
//////import androidx.compose.material3.*
//////import androidx.compose.runtime.*
//////import androidx.compose.ui.Alignment
//////import androidx.compose.ui.Modifier
//////import androidx.compose.ui.graphics.Color
//////import androidx.compose.ui.text.font.FontWeight
//////import androidx.compose.ui.unit.dp
//////import androidx.compose.ui.unit.sp   // ✅ required for .sp
//////import com.sharebite.app.data.model.CharityRequest
//////import com.sharebite.app.data.model.Reservation
//////import com.sharebite.app.data.session.CurrentUser
//////import com.sharebite.app.ui.theme.BgCream
//////import com.sharebite.app.ui.theme.OrangeAccent
//////import java.text.SimpleDateFormat
//////import java.util.*   // ✅ for Date and Locale
//////
//////@Composable
//////fun MerchantDashboardScreen(
//////    paddingValues: PaddingValues,
//////    onAddDonationClick: () -> Unit,        // kept for compatibility but not used
//////    onViewAllListings: () -> Unit,
//////    onViewAllRequests: () -> Unit,
//////    onViewAllReservations: () -> Unit,
//////    onViewImpact: () -> Unit,
//////    onRequestClick: (Long) -> Unit,
//////    onReservationClick: (Long) -> Unit,
//////    viewModel: MerchantViewModel
//////) {
//////    val listings by viewModel.listings.collectAsState()
//////    val requests by viewModel.requests.collectAsState()              // Donation requests (merchant_id = me)
//////    val generalRequests by viewModel.generalRequests.collectAsState() // General requests (merchant_id null)
//////    val reservations by viewModel.reservations.collectAsState()
//////    val isLoading by viewModel.isLoading.collectAsState()
//////    val username = CurrentUser.getUserName()
//////
//////    // ── Compute counts ──────────────────────────────────────────────
//////    val pendingCharityRequests = generalRequests.count { it.status == "Pending" }
//////    val pendingUserReservations = reservations.count { it.status == "Pending" }
//////    val totalPendingClaims = pendingCharityRequests + pendingUserReservations
//////
//////    val activeRequests = generalRequests.filter { it.status != "Completed" && it.status != "Cancelled" }
//////    val activeReservations = reservations.filter { it.status != "Completed" && it.status != "Cancelled" }
//////    val totalPickups = activeRequests.size + activeReservations.size
//////    val pendingPickupsRequests = activeRequests.count { it.status == "Pending" }
//////    val pendingPickupsReservations = activeReservations.count { it.status == "Pending" }
//////
//////    // ── Recent Activity ──────────────────────────────────────────────
//////    val recentItems = remember(reservations, generalRequests, requests) {
//////        val items = mutableListOf<ActivityItem>()
//////
//////        reservations.take(2).forEach { res ->
//////            items.add(
//////                ActivityItem.Reservation(
//////                    user = res.userId.take(8),
//////                    food = res.foodName,
//////                    quantity = res.quantity,
//////                    pickupTime = res.pickupTime,
//////                    status = res.status,
//////                    timestamp = res.createdAt
//////                )
//////            )
//////        }
//////
//////        generalRequests.filter { it.status == "Confirmed" }.take(2).forEach { req ->
//////            items.add(
//////                ActivityItem.Request(
//////                    org = req.organisationName,
//////                    food = req.foodName,
//////                    quantity = req.quantity,
//////                    status = req.status,
//////                    timestamp = req.createdAt
//////                )
//////            )
//////        }
//////
//////        items.sortedByDescending { it.timestamp }.take(2)
//////    }
//////
//////    Box(
//////        modifier = Modifier
//////            .fillMaxSize()
//////            .padding(paddingValues)
//////            .background(BgCream)
//////    ) {
//////        if (isLoading) {
//////            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//////                CircularProgressIndicator(color = OrangeAccent)
//////            }
//////        } else {
//////            LazyColumn(
//////                modifier = Modifier.fillMaxSize(),
//////                contentPadding = PaddingValues(16.dp),
//////                verticalArrangement = Arrangement.spacedBy(16.dp)
//////            ) {
//////                // 1️⃣ Welcome Header
//////                item {
//////                    Column {
//////                        Text(
//////                            "Welcome back, $username!",
//////                            style = MaterialTheme.typography.headlineSmall,
//////                            fontWeight = FontWeight.Bold,
//////                            color = Color(0xFF1E1B16)
//////                        )
//////                        Text(
//////                            "Here is what is happening today.",
//////                            style = MaterialTheme.typography.bodyMedium,
//////                            color = Color.Gray
//////                        )
//////                    }
//////                }
//////
//////                // 2️⃣ Your Impact
//////                item {
//////                    ImpactCard(
//////                        impact = "1,240 LBS",
//////                        subtitle = "Food diverted from landfill this month.",
//////                        onViewReport = onViewImpact
//////                    )
//////                }
//////
//////                // 3️⃣ Pending Claims
//////                item {
//////                    PendingClaimsCard(
//////                        total = totalPendingClaims,
//////                        charityCount = pendingCharityRequests,
//////                        reservationCount = pendingUserReservations,
//////                        onViewAll = onViewAllRequests
//////                    )
//////                }
//////
//////                // 4️⃣ Pickups Today
//////                item {
//////                    PickupsTodayCard(
//////                        total = totalPickups,
//////                        charityCount = activeRequests.size,
//////                        charityPending = pendingPickupsRequests,
//////                        reservationCount = activeReservations.size,
//////                        reservationPending = pendingPickupsReservations,
//////                        onManage = onViewAllReservations
//////                    )
//////                }
//////
//////                // 5️⃣ Recent Activity
//////                item {
//////                    Row(
//////                        modifier = Modifier.fillMaxWidth(),
//////                        horizontalArrangement = Arrangement.SpaceBetween,
//////                        verticalAlignment = Alignment.CenterVertically
//////                    ) {
//////                        Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//////                        TextButton(onClick = { /* navigate to full activity log */ }) {
//////                            Text("See all", color = OrangeAccent, fontWeight = FontWeight.Bold)
//////                        }
//////                    }
//////                }
//////
//////                items(recentItems.take(2)) { activity ->
//////                    when (activity) {
//////                        is ActivityItem.Reservation -> {
//////                            ActivityCard(
//////                                icon = "👤",
//////                                title = "${activity.user} reserved",
//////                                subtitle = "${activity.quantity} × ${activity.food}",
//////                                meta = "Pickup expected by ${activity.pickupTime}",
//////                                timestamp = activity.timestamp,
//////                                status = activity.status
//////                            )
//////                        }
//////                        is ActivityItem.Request -> {
//////                            ActivityCard(
//////                                icon = "🏢",
//////                                title = "${activity.org} approved",
//////                                subtitle = "${activity.quantity} × ${activity.food}",
//////                                meta = "Donation receipt generated.",
//////                                timestamp = activity.timestamp,
//////                                status = activity.status
//////                            )
//////                        }
//////                    }
//////                }
//////
//////                if (recentItems.isEmpty()) {
//////                    item {
//////                        Box(
//////                            modifier = Modifier
//////                                .fillMaxWidth()
//////                                .padding(32.dp),
//////                            contentAlignment = Alignment.Center
//////                        ) {
//////                            Text("No recent activity.", color = Color.Gray)
//////                        }
//////                    }
//////                }
//////            }
//////        }
//////    }
//////}
//////
//////// ─── Helper Cards ──────────────────────────────────────────────────
//////
//////@Composable
//////fun ImpactCard(
//////    impact: String,
//////    subtitle: String,
//////    onViewReport: () -> Unit
//////) {
//////    Card(
//////        modifier = Modifier
//////            .fillMaxWidth()
//////            .clickable { onViewReport() },
//////        shape = RoundedCornerShape(16.dp),
//////        colors = CardDefaults.cardColors(containerColor = Color.White),
//////        elevation = CardDefaults.cardElevation(2.dp)
//////    ) {
//////        Column(modifier = Modifier.padding(20.dp)) {
//////            Text("Your Impact", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
//////            Text(
//////                impact,
//////                style = MaterialTheme.typography.headlineMedium,
//////                fontWeight = FontWeight.ExtraBold,
//////                color = OrangeAccent
//////            )
//////            Text(
//////                subtitle,
//////                style = MaterialTheme.typography.bodySmall,
//////                color = Color.Gray
//////            )
//////            Spacer(modifier = Modifier.height(8.dp))
//////            Text(
//////                "View Report →",
//////                color = OrangeAccent,
//////                fontWeight = FontWeight.Bold,
//////                style = MaterialTheme.typography.labelLarge
//////            )
//////        }
//////    }
//////}
//////
//////@Composable
//////fun PendingClaimsCard(
//////    total: Int,
//////    charityCount: Int,
//////    reservationCount: Int,
//////    onViewAll: () -> Unit
//////) {
//////    Card(
//////        modifier = Modifier
//////            .fillMaxWidth()
//////            .clickable { onViewAll() },
//////        shape = RoundedCornerShape(16.dp),
//////        colors = CardDefaults.cardColors(containerColor = Color.White),
//////        elevation = CardDefaults.cardElevation(2.dp)
//////    ) {
//////        Column(modifier = Modifier.padding(16.dp)) {
//////            Text("Pending Claims", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
//////            Text(
//////                "$total",
//////                style = MaterialTheme.typography.headlineMedium,
//////                fontWeight = FontWeight.Bold,
//////                color = Color(0xFF1E1B16)
//////            )
//////            Row(
//////                modifier = Modifier.fillMaxWidth(),
//////                horizontalArrangement = Arrangement.spacedBy(8.dp)
//////            ) {
//////                Text("$charityCount Charity Requests", color = Color.Gray)
//////                Text("•", color = Color.Gray)
//////                Text("$reservationCount User Reservations", color = Color.Gray)
//////            }
//////            Spacer(modifier = Modifier.height(8.dp))
//////            OutlinedButton(
//////                onClick = onViewAll,
//////                modifier = Modifier.fillMaxWidth(),
//////                shape = RoundedCornerShape(50),
//////                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
//////            ) {
//////                Text("View All Requests →")
//////            }
//////        }
//////    }
//////}
//////
//////@Composable
//////fun PickupsTodayCard(
//////    total: Int,
//////    charityCount: Int,
//////    charityPending: Int,
//////    reservationCount: Int,
//////    reservationPending: Int,
//////    onManage: () -> Unit
//////) {
//////    Card(
//////        modifier = Modifier
//////            .fillMaxWidth()
//////            .clickable { onManage() },
//////        shape = RoundedCornerShape(16.dp),
//////        colors = CardDefaults.cardColors(containerColor = Color.White),
//////        elevation = CardDefaults.cardElevation(2.dp)
//////    ) {
//////        Column(modifier = Modifier.padding(16.dp)) {
//////            Text("Pickups Today", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
//////            Text(
//////                "$total",
//////                style = MaterialTheme.typography.headlineMedium,
//////                fontWeight = FontWeight.Bold,
//////                color = Color(0xFF1E1B16)
//////            )
//////            Row(
//////                modifier = Modifier.fillMaxWidth(),
//////                horizontalArrangement = Arrangement.spacedBy(8.dp)
//////            ) {
//////                Text("$charityCount Charity Requests ($charityPending pending)", color = Color.Gray)
//////                Text("•", color = Color.Gray)
//////                Text("$reservationCount User Reservations ($reservationPending pending)", color = Color.Gray)
//////            }
//////            Spacer(modifier = Modifier.height(8.dp))
//////            OutlinedButton(
//////                onClick = onManage,
//////                modifier = Modifier.fillMaxWidth(),
//////                shape = RoundedCornerShape(50),
//////                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
//////            ) {
//////                Text("Manage Pickups →")
//////            }
//////        }
//////    }
//////}
//////
//////// ─── Activity Models & Card ───────────────────────────────────────
//////
////////sealed class ActivityItem {
////////    data class Reservation(
////////        val user: String,
////////        val food: String,
////////        val quantity: String,
////////        val pickupTime: String,
////////        val status: String,
////////        val timestamp: String
////////    ) : ActivityItem()
////////    data class Request(
////////        val org: String,
////////        val food: String,
////////        val quantity: Int,
////////        val status: String,
////////        val timestamp: String
////////    ) : ActivityItem()
////////}
//////
//////sealed class ActivityItem {
//////    abstract val timestamp: String?
//////
//////    data class Reservation(
//////        val user: String,
//////        val food: String,
//////        val quantity: String,
//////        val pickupTime: String,
//////        val status: String,
//////        override val timestamp: String?
//////    ) : ActivityItem()
//////
//////    data class Request(
//////        val org: String,
//////        val food: String,
//////        val quantity: Int,
//////        val status: String,
//////        override val timestamp: String?
//////    ) : ActivityItem()
//////}
//////
//////@Composable
//////fun ActivityCard(
//////    icon: String,
//////    title: String,
//////    subtitle: String,
//////    meta: String,
//////    timestamp: String?,
//////    status: String
//////) {
//////    Card(
//////        modifier = Modifier.fillMaxWidth(),
//////        shape = RoundedCornerShape(12.dp),
//////        colors = CardDefaults.cardColors(containerColor = Color.White),
//////        elevation = CardDefaults.cardElevation(1.dp)
//////    ) {
//////        Row(
//////            modifier = Modifier
//////                .fillMaxWidth()
//////                .padding(16.dp),
//////            verticalAlignment = Alignment.CenterVertically
//////        ) {
//////            Text(icon, fontSize = 24.sp)
//////            Spacer(modifier = Modifier.width(12.dp))
//////            Column(modifier = Modifier.weight(1f)) {
//////                Text(title, fontWeight = FontWeight.Bold)
//////                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
//////                Text(meta, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//////            }
//////            val timeAgo = formatTimeAgo(timestamp ?: "")
//////            Text(
//////                timeAgo,
//////                style = MaterialTheme.typography.labelSmall,
//////                color = Color.Gray
//////            )
//////        }
//////    }
//////}
//////
//////// ─── Helper: Format time ago ─────────────────────────────────────
//////
//////fun formatTimeAgo(timestamp: String): String {
//////
//////    if (timestamp.isNullOrBlank()) {
//////        return "now"
//////    }
//////    return try {
//////        // Try to parse ISO‑8601 with milliseconds (e.g., "2026-08-30T04:58:01.766291+00")
//////        // If that fails, fallback to simple date format.
//////        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
//////        val date = format.parse(timestamp)
//////        if (date == null) return "now"
//////        val diff = Date().time - date.time
//////        val minutes = diff / 1000 / 60
//////        when {
//////            minutes < 1 -> "now"
//////            minutes < 60 -> "${minutes}m ago"
//////            minutes < 1440 -> "${minutes / 60}h ago"
//////            else -> "${minutes / 1440}d ago"
//////        }
//////    } catch (e: Exception) {
//////        // If parsing fails, return a generic "now"
//////        "now"
//////    }
//////}
////
////package com.sharebite.app.ui.merchant
////
////import androidx.compose.foundation.background
////import androidx.compose.foundation.clickable
////import androidx.compose.foundation.layout.*
////import androidx.compose.foundation.lazy.LazyColumn
////import androidx.compose.foundation.lazy.items
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.material3.*
////import androidx.compose.runtime.*
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import com.sharebite.app.data.model.CharityRequest
////import com.sharebite.app.data.model.Reservation
////import com.sharebite.app.data.session.CurrentUser
////import com.sharebite.app.ui.theme.BgCream
////import com.sharebite.app.ui.theme.OrangeAccent
////import java.text.SimpleDateFormat
////import java.util.*
////
////@Composable
////fun MerchantDashboardScreen(
////    paddingValues: PaddingValues,
////    onAddDonationClick: () -> Unit,        // kept for compatibility but not used
////    onViewAllListings: () -> Unit,
////    onViewAllRequests: () -> Unit,
////    onViewAllReservations: () -> Unit,
////    onViewImpact: () -> Unit,
////    onRequestClick: (Long) -> Unit,
////    onReservationClick: (Long) -> Unit,
////    viewModel: MerchantViewModel
////) {
////    val listings by viewModel.listings.collectAsState()
////    val requests by viewModel.requests.collectAsState()
////    val generalRequests by viewModel.generalRequests.collectAsState()
////    val reservations by viewModel.reservations.collectAsState()
////    val isLoading by viewModel.isLoading.collectAsState()
////    val username = CurrentUser.getUserName()
////
////    // ── Compute counts ──────────────────────────────────────────────
////    val pendingCharityRequests = generalRequests.count { it.status == "Pending" }
////    val pendingUserReservations = reservations.count { it.status == "Pending" }
////    val totalPendingClaims = pendingCharityRequests + pendingUserReservations
////
////    val activeRequests = generalRequests.filter { it.status != "Completed" && it.status != "Cancelled" }
////    val activeReservations = reservations.filter { it.status != "Completed" && it.status != "Cancelled" }
////    val totalPickups = activeRequests.size + activeReservations.size
////    val pendingPickupsRequests = activeRequests.count { it.status == "Pending" }
////    val pendingPickupsReservations = activeReservations.count { it.status == "Pending" }
////
////    // ── Recent Activity ──────────────────────────────────────────────
////    val recentItems = remember(reservations, generalRequests, requests) {
////        val items = mutableListOf<ActivityItem>()
////
////        reservations.take(2).forEach { res ->
////            items.add(
////                ActivityItem.Reservation(
////                    user = res.userId.take(8),
////                    food = res.foodName,
////                    quantity = res.quantity,
////                    pickupTime = res.pickupTime,
////                    status = res.status,
////                    timestamp = res.createdAt
////                )
////            )
////        }
////
////        generalRequests.filter { it.status == "Confirmed" }.take(2).forEach { req ->
////            items.add(
////                ActivityItem.Request(
////                    org = req.organisationName,
////                    food = req.foodName,
////                    quantity = req.quantity,
////                    status = req.status,
////                    timestamp = req.createdAt
////                )
////            )
////        }
////
////        items.sortedByDescending { it.timestamp }.take(2)
////    }
////
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(paddingValues)
////            .background(BgCream)
////    ) {
////        if (isLoading) {
////            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
////                CircularProgressIndicator(color = OrangeAccent)
////            }
////        } else {
////            LazyColumn(
////                modifier = Modifier.fillMaxSize(),
////                contentPadding = PaddingValues(16.dp),
////                verticalArrangement = Arrangement.spacedBy(16.dp)
////            ) {
////                // 1️⃣ Welcome Header
////                item {
////                    Column {
////                        Text(
////                            "Welcome back, $username!",
////                            style = MaterialTheme.typography.headlineSmall,
////                            fontWeight = FontWeight.Bold,
////                            color = Color(0xFF1E1B16)
////                        )
////                        Text(
////                            "Here is what is happening today.",
////                            style = MaterialTheme.typography.bodyMedium,
////                            color = Color.Gray
////                        )
////                    }
////                }
////
////                // 2️⃣ Your Impact
////                item {
////                    ImpactCard(
////                        impact = "1,240 LBS",
////                        subtitle = "Food diverted from landfill this month.",
////                        onViewReport = onViewImpact
////                    )
////                }
////
////                // 3️⃣ Pending Claims - Cleaner layout
////                item {
////                    PendingClaimsCard(
////                        total = totalPendingClaims,
////                        charityCount = pendingCharityRequests,
////                        reservationCount = pendingUserReservations,
////                        onViewAll = onViewAllRequests
////                    )
////                }
////
////                // 4️⃣ Pickups Today - Cleaner layout
////                item {
////                    PickupsTodayCard(
////                        total = totalPickups,
////                        charityCount = activeRequests.size,
////                        charityPending = pendingPickupsRequests,
////                        reservationCount = activeReservations.size,
////                        reservationPending = pendingPickupsReservations,
////                        onManage = onViewAllReservations
////                    )
////                }
////
////                // 5️⃣ Recent Activity
////                item {
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.SpaceBetween,
////                        verticalAlignment = Alignment.CenterVertically
////                    ) {
////                        Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
////                        TextButton(onClick = { /* navigate to full activity log */ }) {
////                            Text("See all", color = OrangeAccent, fontWeight = FontWeight.Bold)
////                        }
////                    }
////                }
////
////                items(recentItems.take(2)) { activity ->
////                    when (activity) {
////                        is ActivityItem.Reservation -> {
////                            ActivityCard(
////                                icon = "👤",
////                                title = "${activity.user} reserved",
////                                subtitle = "${activity.quantity} × ${activity.food}",
////                                meta = "Pickup expected by ${activity.pickupTime}",
////                                timestamp = activity.timestamp,
////                                status = activity.status
////                            )
////                        }
////                        is ActivityItem.Request -> {
////                            ActivityCard(
////                                icon = "🏢",
////                                title = "${activity.org} approved",
////                                subtitle = "${activity.quantity} × ${activity.food}",
////                                meta = "Donation receipt generated.",
////                                timestamp = activity.timestamp,
////                                status = activity.status
////                            )
////                        }
////                    }
////                }
////
////                if (recentItems.isEmpty()) {
////                    item {
////                        Box(
////                            modifier = Modifier
////                                .fillMaxWidth()
////                                .padding(32.dp),
////                            contentAlignment = Alignment.Center
////                        ) {
////                            Text("No recent activity.", color = Color.Gray)
////                        }
////                    }
////                }
////            }
////        }
////    }
////}
////
////// ─── Helper Cards ──────────────────────────────────────────────────
////
////@Composable
////fun ImpactCard(
////    impact: String,
////    subtitle: String,
////    onViewReport: () -> Unit
////) {
////    Card(
////        modifier = Modifier
////            .fillMaxWidth()
////            .clickable { onViewReport() },
////        shape = RoundedCornerShape(16.dp),
////        colors = CardDefaults.cardColors(containerColor = Color.White),
////        elevation = CardDefaults.cardElevation(2.dp)
////    ) {
////        Column(modifier = Modifier.padding(20.dp)) {
////            Text("Your Impact", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
////            Text(
////                impact,
////                style = MaterialTheme.typography.headlineMedium,
////                fontWeight = FontWeight.ExtraBold,
////                color = OrangeAccent
////            )
////            Text(
////                subtitle,
////                style = MaterialTheme.typography.bodySmall,
////                color = Color.Gray
////            )
////            Spacer(modifier = Modifier.height(8.dp))
////            Text(
////                "View Report →",
////                color = OrangeAccent,
////                fontWeight = FontWeight.Bold,
////                style = MaterialTheme.typography.labelLarge
////            )
////        }
////    }
////}
////
////// ✅ UPDATED: Cleaner layout for Pending Claims
////@Composable
////fun PendingClaimsCard(
////    total: Int,
////    charityCount: Int,
////    reservationCount: Int,
////    onViewAll: () -> Unit
////) {
////    Card(
////        modifier = Modifier
////            .fillMaxWidth()
////            .clickable { onViewAll() },
////        shape = RoundedCornerShape(16.dp),
////        colors = CardDefaults.cardColors(containerColor = Color.White),
////        elevation = CardDefaults.cardElevation(2.dp)
////    ) {
////        Column(modifier = Modifier.padding(16.dp)) {
////            // Label
////            Text(
////                "Pending Claims",
////                style = MaterialTheme.typography.labelMedium,
////                color = Color.Gray,
////                fontSize = 12.sp
////            )
////
////            // Big number
////            Text(
////                "$total",
////                style = MaterialTheme.typography.headlineMedium,
////                fontWeight = FontWeight.Bold,
////                color = Color(0xFF1E1B16)
////            )
////
////            // Stats - now on separate lines for better readability
////            Column(
////                modifier = Modifier.fillMaxWidth(),
////                verticalArrangement = Arrangement.spacedBy(2.dp)
////            ) {
////                Text(
////                    "$charityCount Charity Requests",
////                    color = Color.Gray,
////                    fontSize = 14.sp
////                )
////                Text(
////                    "$reservationCount User Reservations",
////                    color = Color.Gray,
////                    fontSize = 14.sp
////                )
////            }
////
////            Spacer(modifier = Modifier.height(8.dp))
////
////            // Button
////            OutlinedButton(
////                onClick = onViewAll,
////                modifier = Modifier.fillMaxWidth(),
////                shape = RoundedCornerShape(50),
////                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
////            ) {
////                Text("View All Requests →")
////            }
////        }
////    }
////}
////
////// ✅ UPDATED: Cleaner layout for Pickups Today
////@Composable
////fun PickupsTodayCard(
////    total: Int,
////    charityCount: Int,
////    charityPending: Int,
////    reservationCount: Int,
////    reservationPending: Int,
////    onManage: () -> Unit
////) {
////    Card(
////        modifier = Modifier
////            .fillMaxWidth()
////            .clickable { onManage() },
////        shape = RoundedCornerShape(16.dp),
////        colors = CardDefaults.cardColors(containerColor = Color.White),
////        elevation = CardDefaults.cardElevation(2.dp)
////    ) {
////        Column(modifier = Modifier.padding(16.dp)) {
////            // Label
////            Text(
////                "Pickups Today",
////                style = MaterialTheme.typography.labelMedium,
////                color = Color.Gray,
////                fontSize = 12.sp
////            )
////
////            // Big number
////            Text(
////                "$total",
////                style = MaterialTheme.typography.headlineMedium,
////                fontWeight = FontWeight.Bold,
////                color = Color(0xFF1E1B16)
////            )
////
////            // Stats - now on separate lines for better readability
////            Column(
////                modifier = Modifier.fillMaxWidth(),
////                verticalArrangement = Arrangement.spacedBy(2.dp)
////            ) {
////                Text(
////                    "$charityCount Charity Requests ($charityPending pending)",
////                    color = Color.Gray,
////                    fontSize = 14.sp
////                )
////                Text(
////                    "$reservationCount User Reservations ($reservationPending pending)",
////                    color = Color.Gray,
////                    fontSize = 14.sp
////                )
////            }
////
////            Spacer(modifier = Modifier.height(8.dp))
////
////            // Button
////            OutlinedButton(
////                onClick = onManage,
////                modifier = Modifier.fillMaxWidth(),
////                shape = RoundedCornerShape(50),
////                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
////            ) {
////                Text("Manage Pickups →")
////            }
////        }
////    }
////}
////
////// ─── Activity Models & Card ───────────────────────────────────────
////
////sealed class ActivityItem {
////    data class Reservation(
////        val user: String,
////        val food: String,
////        val quantity: String,
////        val pickupTime: String,
////        val status: String,
////        val timestamp: String
////    ) : ActivityItem()
////    data class Request(
////        val org: String,
////        val food: String,
////        val quantity: Int,
////        val status: String,
////        val timestamp: String
////    ) : ActivityItem()
////}
////
////@Composable
////fun ActivityCard(
////    icon: String,
////    title: String,
////    subtitle: String,
////    meta: String,
////    timestamp: String,
////    status: String
////) {
////    Card(
////        modifier = Modifier.fillMaxWidth(),
////        shape = RoundedCornerShape(12.dp),
////        colors = CardDefaults.cardColors(containerColor = Color.White),
////        elevation = CardDefaults.cardElevation(1.dp)
////    ) {
////        Row(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(16.dp),
////            verticalAlignment = Alignment.CenterVertically
////        ) {
////            Text(icon, fontSize = 24.sp)
////            Spacer(modifier = Modifier.width(12.dp))
////            Column(modifier = Modifier.weight(1f)) {
////                Text(title, fontWeight = FontWeight.Bold)
////                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
////                Text(meta, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
////            }
////            val timeAgo = formatTimeAgo(timestamp)
////            Text(
////                timeAgo,
////                style = MaterialTheme.typography.labelSmall,
////                color = Color.Gray
////            )
////        }
////    }
////}
////
////// ─── Helper: Format time ago ─────────────────────────────────────
////
////fun formatTimeAgo(timestamp: String): String {
////    return try {
////        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
////        val date = format.parse(timestamp)
////        if (date == null) return "now"
////        val diff = Date().time - date.time
////        val minutes = diff / 1000 / 60
////        when {
////            minutes < 1 -> "now"
////            minutes < 60 -> "${minutes}m ago"
////            minutes < 1440 -> "${minutes / 60}h ago"
////            else -> "${minutes / 1440}d ago"
////        }
////    } catch (e: Exception) {
////        "now"
////    }
////}
//
//package com.sharebite.app.ui.merchant
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
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
//import androidx.compose.ui.unit.sp
//import com.sharebite.app.data.session.CurrentUser
//import com.sharebite.app.ui.theme.BgCream
//import com.sharebite.app.ui.theme.OrangeAccent
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//
//@Composable
//fun MerchantDashboardScreen(
//    paddingValues: PaddingValues,
//    onAddDonationClick: () -> Unit,
//    onViewAllListings: () -> Unit,
//    onViewAllRequests: () -> Unit,
//    onViewAllReservations: () -> Unit,
//    onViewImpact: () -> Unit,
//    onRequestClick: (Long) -> Unit,
//    onReservationClick: (Long) -> Unit,
//    viewModel: MerchantViewModel
//) {
//
//    // ============================================================
//    // Load merchant data when dashboard opens
//    // ============================================================
//
//    LaunchedEffect(Unit) {
//        viewModel.loadMerchantData()
//    }
//
//
//    // ============================================================
//    // Observe data
//    // ============================================================
//
//    val listings by viewModel.listings.collectAsState()
//
//    // Charity requests already assigned to this merchant
//    val requests by viewModel.requests.collectAsState()
//
//    // Unclaimed charity requests
//    val generalRequests by viewModel.generalRequests.collectAsState()
//
//    // User reservations belonging to this merchant
//    val reservations by viewModel.reservations.collectAsState()
//
//    val isLoading by viewModel.isLoading.collectAsState()
//
//    val username =
//        CurrentUser.getUserName()
//
//
//    // ============================================================
//    // Pending Claims
//    // ============================================================
//
//    // Charity requests for this merchant's posted donations
//    val pendingAssignedCharityRequests =
//        requests.count {
//            it.status == "Pending"
//        }
//
//
//    // General requests from charities that are not assigned
//    // to any merchant yet
//    val pendingGeneralCharityRequests =
//        generalRequests.count {
//            it.status == "Pending"
//        }
//
//
//    // Combine both types of charity requests
//    val pendingCharityRequests =
//        pendingAssignedCharityRequests +
//                pendingGeneralCharityRequests
//
//
//    // User reservations
//    val pendingUserReservations =
//        reservations.count {
//            it.status == "Pending"
//        }
//
//
//    // Total things requiring attention
//    val totalPendingClaims =
//        pendingCharityRequests +
//                pendingUserReservations
//
//
//    // ============================================================
//    // Pickups
//    // ============================================================
//
//    // IMPORTANT:
//    // General/unclaimed requests are NOT included here.
//    //
//    // Only requests already assigned to this merchant
//    // and user reservations are considered pickups.
//    // ============================================================
//
//    val activeRequests =
//        requests.filter {
//            it.status != "Completed" &&
//                    it.status != "Cancelled"
//        }
//
//
//    val activeReservations =
//        reservations.filter {
//            it.status != "Completed" &&
//                    it.status != "Cancelled"
//        }
//
//
//    val totalPickups =
//        activeRequests.size +
//                activeReservations.size
//
//
//    val pendingPickupsRequests =
//        activeRequests.count {
//            it.status == "Pending"
//        }
//
//
//    val pendingPickupsReservations =
//        activeReservations.count {
//            it.status == "Pending"
//        }
//
//
//    // ============================================================
//    // Recent Activity
//    // ============================================================
//
//    val recentItems =
//        remember(
//            reservations,
//            generalRequests,
//            requests
//        ) {
//
//            val items =
//                mutableListOf<ActivityItem>()
//
//
//            // ----------------------------------------------------
//            // Recent user reservations
//            // ----------------------------------------------------
//
//            reservations
//                .take(2)
//                .forEach { res ->
//
//                    items.add(
//                        ActivityItem.Reservation(
//
//                            user =
//                                res.userId.take(8),
//
//                            food =
//                                res.foodName,
//
//                            quantity =
//                                res.quantity,
//
//                            pickupTime =
//                                res.pickupTime,
//
//                            status =
//                                res.status,
//
//                            timestamp =
//                                res.createdAt
//                        )
//                    )
//                }
//
//
//            // ----------------------------------------------------
//            // Recent general charity requests
//            // ----------------------------------------------------
//
//            generalRequests
//                .filter {
//                    it.status == "Confirmed"
//                }
//                .take(2)
//                .forEach { req ->
//
//                    items.add(
//                        ActivityItem.Request(
//
//                            org =
//                                req.organisationName,
//
//                            food =
//                                req.foodName,
//
//                            quantity =
//                                req.quantity,
//
//                            status =
//                                req.status,
//
//                            timestamp =
//                                req.createdAt
//                        )
//                    )
//                }
//
//
//            // Newest first
//            items
//                .sortedByDescending {
//                    it.timestamp
//                }
//                .take(2)
//        }
//
//
//    // ============================================================
//    // Screen
//    // ============================================================
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(paddingValues)
//            .background(BgCream)
//    ) {
//
//        if (isLoading) {
//
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//
//                CircularProgressIndicator(
//                    color = OrangeAccent
//                )
//            }
//
//        } else {
//
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//
//                contentPadding =
//                    PaddingValues(16.dp),
//
//                verticalArrangement =
//                    Arrangement.spacedBy(16.dp)
//            ) {
//
//
//                // =================================================
//                // 1. Welcome Header
//                // =================================================
//
//                item {
//
//                    Column {
//
//                        Text(
//                            text =
//                                "Welcome back, $username!",
//
//                            style =
//                                MaterialTheme.typography.headlineSmall,
//
//                            fontWeight =
//                                FontWeight.Bold,
//
//                            color =
//                                Color(0xFF1E1B16)
//                        )
//
//                        Text(
//                            text =
//                                "Here is what is happening today.",
//
//                            style =
//                                MaterialTheme.typography.bodyMedium,
//
//                            color =
//                                Color.Gray
//                        )
//                    }
//                }
//
//
//                // =================================================
//                // 2. Your Impact
//                // =================================================
//
//                item {
//
//                    ImpactCard(
//
//                        impact =
//                            "1,240 LBS",
//
//                        subtitle =
//                            "Food diverted from landfill this month.",
//
//                        onViewReport =
//                            onViewImpact
//                    )
//                }
//
//
//                // =================================================
//                // 3. Pending Claims
//                // =================================================
//
//                item {
//
//                    PendingClaimsCard(
//
//                        total =
//                            totalPendingClaims,
//
//                        charityCount =
//                            pendingCharityRequests,
//
//                        reservationCount =
//                            pendingUserReservations,
//
//                        onViewAll =
//                            onViewAllRequests
//                    )
//                }
//
//
//                // =================================================
//                // 4. Pickups
//                // =================================================
//
//                item {
//
//                    PickupsTodayCard(
//
//                        total =
//                            totalPickups,
//
//                        charityCount =
//                            activeRequests.size,
//
//                        charityPending =
//                            pendingPickupsRequests,
//
//                        reservationCount =
//                            activeReservations.size,
//
//                        reservationPending =
//                            pendingPickupsReservations,
//
//                        onManage =
//                            onViewAllReservations
//                    )
//                }
//
//
//                // =================================================
//                // 5. Recent Activity Header
//                // =================================================
//
//                item {
//
//                    Row(
//
//                        modifier =
//                            Modifier.fillMaxWidth(),
//
//                        horizontalArrangement =
//                            Arrangement.SpaceBetween,
//
//                        verticalAlignment =
//                            Alignment.CenterVertically
//                    ) {
//
//                        Text(
//                            text =
//                                "Recent Activity",
//
//                            style =
//                                MaterialTheme.typography.titleMedium,
//
//                            fontWeight =
//                                FontWeight.Bold
//                        )
//
//
//                        TextButton(
//                            onClick = {
//                                // TODO:
//                                // Navigate to full activity log
//                            }
//                        ) {
//
//                            Text(
//                                text =
//                                    "See all",
//
//                                color =
//                                    OrangeAccent,
//
//                                fontWeight =
//                                    FontWeight.Bold
//                            )
//                        }
//                    }
//                }
//
//
//                // =================================================
//                // Recent activities
//                // =================================================
//
//                items(
//                    recentItems
//                ) { activity ->
//
//                    when (activity) {
//
//                        is ActivityItem.Reservation -> {
//
//                            ActivityCard(
//
//                                icon =
//                                    "👤",
//
//                                title =
//                                    "${activity.user} reserved",
//
//                                subtitle =
//                                    "${activity.quantity} × ${activity.food}",
//
//                                meta =
//                                    "Pickup expected by ${activity.pickupTime}",
//
//                                timestamp =
//                                    activity.timestamp,
//
//                                status =
//                                    activity.status
//                            )
//                        }
//
//
//                        is ActivityItem.Request -> {
//
//                            ActivityCard(
//
//                                icon =
//                                    "🏢",
//
//                                title =
//                                    "${activity.org} approved",
//
//                                subtitle =
//                                    "${activity.quantity} × ${activity.food}",
//
//                                meta =
//                                    "Donation receipt generated.",
//
//                                timestamp =
//                                    activity.timestamp,
//
//                                status =
//                                    activity.status
//                            )
//                        }
//                    }
//                }
//
//
//                // =================================================
//                // No activity
//                // =================================================
//
//                if (recentItems.isEmpty()) {
//
//                    item {
//
//                        Box(
//
//                            modifier =
//                                Modifier
//                                    .fillMaxWidth()
//                                    .padding(32.dp),
//
//                            contentAlignment =
//                                Alignment.Center
//                        ) {
//
//                            Text(
//                                text =
//                                    "No recent activity.",
//
//                                color =
//                                    Color.Gray
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//// =================================================================
//// Impact Card
//// =================================================================
//
//@Composable
//fun ImpactCard(
//    impact: String,
//    subtitle: String,
//    onViewReport: () -> Unit
//) {
//
//    Card(
//
//        modifier =
//            Modifier
//                .fillMaxWidth()
//                .clickable {
//                    onViewReport()
//                },
//
//        shape =
//            RoundedCornerShape(16.dp),
//
//        colors =
//            CardDefaults.cardColors(
//                containerColor =
//                    Color.White
//            ),
//
//        elevation =
//            CardDefaults.cardElevation(2.dp)
//    ) {
//
//        Column(
//            modifier =
//                Modifier.padding(20.dp)
//        ) {
//
//            Text(
//                text =
//                    "Your Impact",
//
//                style =
//                    MaterialTheme.typography.labelMedium,
//
//                color =
//                    Color.Gray
//            )
//
//
//            Text(
//                text =
//                    impact,
//
//                style =
//                    MaterialTheme.typography.headlineMedium,
//
//                fontWeight =
//                    FontWeight.ExtraBold,
//
//                color =
//                    OrangeAccent
//            )
//
//
//            Text(
//                text =
//                    subtitle,
//
//                style =
//                    MaterialTheme.typography.bodySmall,
//
//                color =
//                    Color.Gray
//            )
//
//
//            Spacer(
//                modifier =
//                    Modifier.height(8.dp)
//            )
//
//
//            Text(
//                text =
//                    "View Report →",
//
//                color =
//                    OrangeAccent,
//
//                fontWeight =
//                    FontWeight.Bold,
//
//                style =
//                    MaterialTheme.typography.labelLarge
//            )
//        }
//    }
//}
//
//
//// =================================================================
//// Pending Claims Card
//// =================================================================
//
//@Composable
//fun PendingClaimsCard(
//    total: Int,
//    charityCount: Int,
//    reservationCount: Int,
//    onViewAll: () -> Unit
//) {
//
//    Card(
//
//        modifier =
//            Modifier
//                .fillMaxWidth()
//                .clickable {
//                    onViewAll()
//                },
//
//        shape =
//            RoundedCornerShape(16.dp),
//
//        colors =
//            CardDefaults.cardColors(
//                containerColor =
//                    Color.White
//            ),
//
//        elevation =
//            CardDefaults.cardElevation(2.dp)
//    ) {
//
//        Column(
//            modifier =
//                Modifier.padding(16.dp)
//        ) {
//
//            Text(
//                text =
//                    "Pending Claims",
//
//                style =
//                    MaterialTheme.typography.labelMedium,
//
//                color =
//                    Color.Gray
//            )
//
//
//            Text(
//                text =
//                    "$total",
//
//                style =
//                    MaterialTheme.typography.headlineMedium,
//
//                fontWeight =
//                    FontWeight.Bold,
//
//                color =
//                    Color(0xFF1E1B16)
//            )
//
//
//            Row(
//
//                modifier =
//                    Modifier.fillMaxWidth(),
//
//                horizontalArrangement =
//                    Arrangement.spacedBy(8.dp)
//            ) {
//
//                Text(
//                    text =
//                        "$charityCount Charity Requests",
//
//                    color =
//                        Color.Gray
//                )
//
//
//                Text(
//                    text =
//                        "•",
//
//                    color =
//                        Color.Gray
//                )
//
//
//                Text(
//                    text =
//                        "$reservationCount User Reservations",
//
//                    color =
//                        Color.Gray
//                )
//            }
//
//
//            Spacer(
//                modifier =
//                    Modifier.height(8.dp)
//            )
//
//
//            OutlinedButton(
//
//                onClick =
//                    onViewAll,
//
//                modifier =
//                    Modifier.fillMaxWidth(),
//
//                shape =
//                    RoundedCornerShape(50),
//
//                colors =
//                    ButtonDefaults.outlinedButtonColors(
//                        contentColor =
//                            OrangeAccent
//                    )
//            ) {
//
//                Text(
//                    text =
//                        "View All Requests →"
//                )
//            }
//        }
//    }
//}
//
//
//// =================================================================
//// Pickups Today Card
//// =================================================================
//
//@Composable
//fun PickupsTodayCard(
//    total: Int,
//    charityCount: Int,
//    charityPending: Int,
//    reservationCount: Int,
//    reservationPending: Int,
//    onManage: () -> Unit
//) {
//
//    Card(
//
//        modifier =
//            Modifier
//                .fillMaxWidth()
//                .clickable {
//                    onManage()
//                },
//
//        shape =
//            RoundedCornerShape(16.dp),
//
//        colors =
//            CardDefaults.cardColors(
//                containerColor =
//                    Color.White
//            ),
//
//        elevation =
//            CardDefaults.cardElevation(2.dp)
//    ) {
//
//        Column(
//            modifier =
//                Modifier.padding(16.dp)
//        ) {
//
//            Text(
//                text =
//                    "Pickups Today",
//
//                style =
//                    MaterialTheme.typography.labelMedium,
//
//                color =
//                    Color.Gray
//            )
//
//
//            Text(
//                text =
//                    "$total",
//
//                style =
//                    MaterialTheme.typography.headlineMedium,
//
//                fontWeight =
//                    FontWeight.Bold,
//
//                color =
//                    Color(0xFF1E1B16)
//            )
//
//
//            Row(
//
//                modifier =
//                    Modifier.fillMaxWidth(),
//
//                horizontalArrangement =
//                    Arrangement.spacedBy(8.dp)
//            ) {
//
//                Text(
//                    text =
//                        "$charityCount Charity Requests ($charityPending pending)",
//
//                    color =
//                        Color.Gray
//                )
//
//
//                Text(
//                    text =
//                        "•",
//
//                    color =
//                        Color.Gray
//                )
//
//
//                Text(
//                    text =
//                        "$reservationCount User Reservations ($reservationPending pending)",
//
//                    color =
//                        Color.Gray
//                )
//            }
//
//
//            Spacer(
//                modifier =
//                    Modifier.height(8.dp)
//            )
//
//
//            OutlinedButton(
//
//                onClick =
//                    onManage,
//
//                modifier =
//                    Modifier.fillMaxWidth(),
//
//                shape =
//                    RoundedCornerShape(50),
//
//                colors =
//                    ButtonDefaults.outlinedButtonColors(
//                        contentColor =
//                            OrangeAccent
//                    )
//            ) {
//
//                Text(
//                    text =
//                        "Manage Pickups →"
//                )
//            }
//        }
//    }
//}
//
//
//// =================================================================
//// Activity Models
//// =================================================================
//
//sealed class ActivityItem {
//
//    abstract val timestamp: String?
//
//
//    data class Reservation(
//
//        val user: String,
//
//        val food: String,
//
//        val quantity: String,
//
//        val pickupTime: String,
//
//        val status: String,
//
//        override val timestamp: String?
//
//    ) : ActivityItem()
//
//
//    data class Request(
//
//        val org: String,
//
//        val food: String,
//
//        val quantity: Int,
//
//        val status: String,
//
//        override val timestamp: String?
//
//    ) : ActivityItem()
//}
//
//
//// =================================================================
//// Activity Card
//// =================================================================
//
//@Composable
//fun ActivityCard(
//
//    icon: String,
//
//    title: String,
//
//    subtitle: String,
//
//    meta: String,
//
//    timestamp: String?,
//
//    status: String
//
//) {
//
//    Card(
//
//        modifier =
//            Modifier.fillMaxWidth(),
//
//        shape =
//            RoundedCornerShape(12.dp),
//
//        colors =
//            CardDefaults.cardColors(
//                containerColor =
//                    Color.White
//            ),
//
//        elevation =
//            CardDefaults.cardElevation(1.dp)
//    ) {
//
//        Row(
//
//            modifier =
//                Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//
//            verticalAlignment =
//                Alignment.CenterVertically
//        ) {
//
//            Text(
//                text =
//                    icon,
//
//                fontSize =
//                    24.sp
//            )
//
//
//            Spacer(
//                modifier =
//                    Modifier.width(12.dp)
//            )
//
//
//            Column(
//                modifier =
//                    Modifier.weight(1f)
//            ) {
//
//                Text(
//                    text =
//                        title,
//
//                    fontWeight =
//                        FontWeight.Bold
//                )
//
//
//                Text(
//                    text =
//                        subtitle,
//
//                    style =
//                        MaterialTheme.typography.bodyMedium,
//
//                    color =
//                        Color.Gray
//                )
//
//
//                Text(
//                    text =
//                        meta,
//
//                    style =
//                        MaterialTheme.typography.bodySmall,
//
//                    color =
//                        Color.Gray
//                )
//            }
//
//
//            val timeAgo =
//                formatTimeAgo(timestamp)
//
//
//            Text(
//
//                text =
//                    timeAgo,
//
//                style =
//                    MaterialTheme.typography.labelSmall,
//
//                color =
//                    Color.Gray
//            )
//        }
//    }
//}
//
//
//// =================================================================
//// Format timestamp
//// =================================================================
//
//fun formatTimeAgo(
//    timestamp: String?
//): String {
//
//    if (timestamp.isNullOrBlank()) {
//        return "now"
//    }
//
//
//    return try {
//
//        val format =
//            SimpleDateFormat(
//                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
//                Locale.getDefault()
//            )
//
//
//        val date =
//            format.parse(timestamp)
//
//
//        if (date == null) {
//            return "now"
//        }
//
//
//        val diff =
//            Date().time - date.time
//
//
//        val minutes =
//            diff / 1000 / 60
//
//
//        when {
//
//            minutes < 1 ->
//                "now"
//
//            minutes < 60 ->
//                "${minutes}m ago"
//
//            minutes < 1440 ->
//                "${minutes / 60}h ago"
//
//            else ->
//                "${minutes / 1440}d ago"
//        }
//
//    } catch (e: Exception) {
//
//        "now"
//    }
//}

package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent
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
    onRequestClick: (Long) -> Unit,
    onReservationClick: (Long) -> Unit,
    viewModel: MerchantViewModel
) {
    val listings by viewModel.listings.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val generalRequests by viewModel.generalRequests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val username = CurrentUser.getUserName()

    // ── Pending Claims counts ──────────────────────────────────────
    val pendingCharityRequests = generalRequests.count { it.status == "Pending" } +
            requests.count { it.status == "Pending" }
    val pendingUserReservations = reservations.count { it.status == "Pending" }
    val totalPendingClaims = pendingCharityRequests + pendingUserReservations

    // ── Recent Activity ──────────────────────────────────────────────
    val recentItems = remember(reservations, generalRequests, requests) {
        val items = mutableListOf<ActivityItem>()
        reservations.take(2).forEach { res ->
            items.add(
                ActivityItem.Reservation(
                    user = res.userId.take(8),
                    food = res.foodName,
                    quantity = res.quantity,
                    pickupTime = res.pickupTime,
                    status = res.status,
                    timestamp = res.createdAt ?: ""
                )
            )
        }
        generalRequests.filter { it.status == "Confirmed" }.take(2).forEach { req ->
            items.add(
                ActivityItem.Request(
                    org = req.organisationName,
                    food = req.foodName,
                    quantity = req.quantity,
                    status = req.status,
                    timestamp = req.createdAt ?: ""
                )
            )
        }
        items.sortedByDescending { it.timestamp }.take(2)
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
                            "Welcome back, $username!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B16)
                        )
                        Text(
                            "Here is what is happening today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                // 2️⃣ Your Impact
                item {
                    ImpactCard(
                        impact = "1,240 LBS",
                        subtitle = "Food diverted from landfill this month.",
                        onViewReport = onViewImpact
                    )
                }

                // 3️⃣ Create Donation (NEW)
                item {
                    CreateDonationCard(
                        onCreateClick = onAddDonationClick
                    )
                }

                // 4️⃣ Pending Claims
                item {
                    PendingClaimsCard(
                        total = totalPendingClaims,
                        charityCount = pendingCharityRequests,
                        reservationCount = pendingUserReservations,
                        onViewAll = onViewAllRequests
                    )
                }

                // 5️⃣ Recent Activity
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { /* navigate to full activity log */ }) {
                            Text("See all", color = OrangeAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(recentItems.take(2)) { activity ->
                    when (activity) {
                        is ActivityItem.Reservation -> {
                            ActivityCard(
                                icon = "👤",
                                title = "${activity.user} reserved",
                                subtitle = "${activity.quantity} × ${activity.food}",
                                meta = "Pickup expected by ${activity.pickupTime}",
                                timestamp = activity.timestamp,
                                status = activity.status
                            )
                        }
                        is ActivityItem.Request -> {
                            ActivityCard(
                                icon = "🏢",
                                title = "${activity.org} approved",
                                subtitle = "${activity.quantity} × ${activity.food}",
                                meta = "Donation receipt generated.",
                                timestamp = activity.timestamp,
                                status = activity.status
                            )
                        }
                    }
                }

                if (recentItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No recent activity.", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ─── Create Donation Card ──────────────────────────────────────────

@Composable
fun CreateDonationCard(
    onCreateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCreateClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0) // Light warm orange
        ),
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
                    "🍽️ Share Food Now",
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

// ─── Impact Card ────────────────────────────────────────────────────

@Composable
fun ImpactCard(
    impact: String,
    subtitle: String,
    onViewReport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewReport() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Your Impact", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(
                impact,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = OrangeAccent
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "View Report →",
                color = OrangeAccent,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ─── Pending Claims Card ──────────────────────────────────────────

@Composable
fun PendingClaimsCard(
    total: Int,
    charityCount: Int,
    reservationCount: Int,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewAll() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Pending Claims",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                "$total",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B16)
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("$charityCount Charity Requests", color = Color.Gray, fontSize = 14.sp)
                Text("$reservationCount User Reservations", color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onViewAll,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
            ) {
                Text("View All Requests →")
            }
        }
    }
}

// ─── Activity Models & Card ──────────────────────────────────────

//sealed class ActivityItem {
//    data class Reservation(
//        val user: String,
//        val food: String,
//        val quantity: String,
//        val pickupTime: String,
//        val status: String,
//        val timestamp: String
//    ) : ActivityItem()
//    data class Request(
//        val org: String,
//        val food: String,
//        val quantity: Int,
//        val status: String,
//        val timestamp: String?
//    ) : ActivityItem()
//}

sealed class ActivityItem(
    open val timestamp: String?
) {
    data class Reservation(
        val user: String,
        val food: String,
        val quantity: String,
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

@Composable
fun ActivityCard(
    icon: String,
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
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(meta, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            val timeAgo = formatTimeAgo(timestamp)
            Text(
                timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

// ─── Helper: Format time ago ──────────────────────────────────────

fun formatTimeAgo(timestamp: String?): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
//        val date = format.parse(timestamp)
//        if (date == null) return "now"
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