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
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.sharebite.app.data.repository.FoodListingRepository
private val statusTabs = listOf("All", "Pending", "Confirmed", "Cancelled")

@Composable
fun MyRequestsScreen(
    paddingValues: PaddingValues,
    onViewClick: (Long) -> Unit,        // 点击 View → 跳转详情
    onEditClick: (Long) -> Unit,        // 点击 Edit → 跳转编辑（仅 Pending）
    onWithdrawClick: (Long) -> Unit,     // 点击 Withdraw → 弹出确认（仅 Pending）
    onRequestAgain: (CharityRequest) -> Unit
) {
    val repository = remember { CharityRepository() }
    val listingRepository = remember { FoodListingRepository() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var allRequests by remember { mutableStateOf<List<CharityRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf("All") }

    // 新增：控制 Withdraw 弹窗的显示，记录目前要撤回哪一笔（null 代表不显示）
    var withdrawTargetId by remember { mutableStateOf<Long?>(null) }

    suspend fun loadRequests() {
        try {
            allRequests = repository.getRequests()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        // 标题
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text("My Requests", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Track and manage your community food requests.", fontSize = 13.sp, color = TextGrey)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 状态筛选标签
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

        // 列表内容
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
                                    Toast.makeText(context, "No location info available for this request.", Toast.LENGTH_SHORT).show()
                                } else {
                                    coroutineScope.launch {
                                        try {
                                            val listing = listingRepository.getListingById(listingId)
                                            val lat = listing.latitude
                                            val lng = listing.longitude
                                            val uri = if (lat != null && lng != null) {
                                                Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(listing.merchantName)})")
                                            } else {
                                                Uri.parse("geo:0,0?q=${Uri.encode(listing.address)}")
                                            }
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open directions: ${e.message}", Toast.LENGTH_SHORT).show()
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

    // ---------- WITHDRAW 确认弹窗 ----------
    withdrawTargetId?.let { targetId ->
        WithdrawConfirmDialog(
            onKeep = { withdrawTargetId = null },
            onWithdraw = {
                coroutineScope.launch {
                    try {
                        repository.withdrawRequest(targetId)
                        withdrawTargetId = null
                        loadRequests() // 撤回成功后重新载入列表，立刻反映最新状态
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
    onGetDirections: () -> Unit,        // 新增：Confirmed 专用
    onRequestAgain: () -> Unit          // 新增：Completed 专用
) {
    // ----- 根据状态决定显示内容 -----
    val isPending = request.status == "Pending"
    val isConfirmed = request.status == "Confirmed"
    val isCompleted = request.status == "Completed"
    val isCancelled = request.status == "Cancelled"

    // 顶部状态描述文字（完全照搬图片里的）
    val statusHeadline = when (request.status) {
        "Pending" -> "PICK UP TODAY"
        "Confirmed" -> "REQUESTED 2H AGO"
        "Completed" -> "DELIVERED YESTERDAY"
        "Cancelled" -> "LAST WEEK"
        else -> ""
    }

    // 状态标签颜色和背景
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
            // ----- 顶部行：状态描述 + 状态标签 -----
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
                // 状态标签（小圆角矩形）
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

            // ----- 食物名称 + 数量 -----
            Text(
                text = "${request.foodName} × ${request.quantity}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ----- 商家名称（地址暂时用 OrganisationName）-----
            Text(
                text = request.organisationName,
                fontSize = 14.sp,
                color = TextGrey
            )

            // ----- 新增：日期和时间 -----
            Text(
                text = "${request.pickupDate} · ${request.pickupTime}",
                fontSize = 13.sp,
                color = TextGrey
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ----- 分割线 -----
            Divider(
                color = Color(0xFFE8E2D8),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ----- 底部操作按钮（根据状态不同）-----
            when {
                isPending -> {
                    // Pending：View（左）+ Edit + Withdraw（右）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左边：View（图标 + 文字）
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            IconButton(
                                onClick = onViewClick,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = "View",
                                    tint = OrangeAccent
                                )
                            }
                            Text(
                                text = "View",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = OrangeAccent
                            )
                        }

                        // 右边：Edit + Withdraw
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = GreenConfirmed
                                )
                            }
                            IconButton(
                                onClick = onWithdrawClick,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Withdraw",
                                    tint = RedCancelled
                                )
                            }
                        }
                    }
                }
                isConfirmed -> {
                    // Confirmed：显示 "Get Directions" 按钮
                    Button(
                        onClick = onGetDirections,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Get Directions", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                isCompleted -> {
                    // Completed：显示 "Request Again" 按钮
                    OutlinedButton(
                        onClick = onRequestAgain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GreenConfirmed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Request Again", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                isCancelled -> {
                    // Cancelled：显示灰色提示
                    Text(
                        text = "This request was cancelled.",
                        fontSize = 13.sp,
                        color = TextGrey,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    // 其他状态：不显示
                }
            }
        }
    }
}