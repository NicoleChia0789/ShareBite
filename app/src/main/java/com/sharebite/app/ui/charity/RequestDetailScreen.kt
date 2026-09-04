package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.ui.theme.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.sharebite.app.ui.components.WithdrawConfirmDialog
import androidx.compose.material.icons.filled.Cancel

@Composable
fun RequestDetailScreen(
    requestId: Long,
    paddingValues: PaddingValues,
    onEditClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    val repository = remember { CharityRepository() }
    val coroutineScope = rememberCoroutineScope()

    var request by remember { mutableStateOf<CharityRequest?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 新增：控制 Withdraw 弹窗
    var showWithdrawDialog by remember { mutableStateOf(false) }

    LaunchedEffect(requestId) {
        try {
            request = repository.getMyRequestById(requestId)
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
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
            request == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Request not found.", color = TextGrey)
                }
            }
            else -> {
                val data = request!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // ---------- 状态横幅 ----------
                    StatusBanner(status = data.status, foodName = data.foodName, quantity = data.quantity)

                    Spacer(modifier = Modifier.height(16.dp))

                    // ---------- 核心详情卡片 ----------
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Request Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Spacer(modifier = Modifier.height(12.dp))

                            DetailRow(label = "Quantity", value = "${data.quantity} portions")
                            DetailRow(label = "Collection", value = "${data.pickupDate} · ${data.pickupTime}")
                            DetailRow(label = "Organization", value = data.organisationName)
                            DetailRow(label = "People Served", value = "${data.peopleServed}")
                            DetailRow(label = "Submitted", value = formatDate(data.createdAt))
                            if (!data.note.isNullOrBlank()) {
                                DetailRow(label = "Note", value = data.note)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ---------- 进度时间线 ----------
                    TimelineCard(status = data.status, createdAt = data.createdAt)

                    Spacer(modifier = Modifier.height(20.dp))

                    // ---------- 操作按钮 ----------
                    // 只有 Pending 状态才显示编辑和撤回按钮
                    if (data.status == "Pending") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onEditClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Edit Request", color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { showWithdrawDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = RedCancelled
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Withdraw")
                            }
                        }
                    } else {
                        // 非 Pending 状态只显示一个 "Back" 占位或不显示按钮
                        // 但为了 UI 友好，可以放一个提示文字
                        Text(
                            text = "This request is ${data.status.lowercase()}. No further actions available.",
                            fontSize = 13.sp,
                            color = TextGrey,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // ---------- WITHDRAW 确认弹窗 ----------
    if (showWithdrawDialog) {
        WithdrawConfirmDialog(
            onKeep = { showWithdrawDialog = false },
            onWithdraw = { reason ->
                coroutineScope.launch {
                    try {
                        repository.withdrawMyRequest(requestId, reason)
                        showWithdrawDialog = false
                        // 重新载入，让画面显示最新的 Cancelled 状态
                        request = repository.getMyRequestById(requestId)
                    } catch (e: Exception) {
                        errorMessage = e.message
                        showWithdrawDialog = false
                    }
                }
            }
        )
    }
}

// ---------- 状态横幅 ----------
@Composable
private fun StatusBanner(status: String, foodName: String, quantity: Int) {
    val (bgColor, textColor) = when (status) {
        "Pending" -> Color(0xFFFFF8E1) to Color(0xFFE65100)
        "Confirmed" -> Color(0xFFE8F5E9) to GreenConfirmed
        "Completed" -> Color(0xFFF5F5F5) to GreyCompleted
        "Cancelled" -> Color(0xFFFFEBEE) to RedCancelled
        else -> Color(0xFFF5F5F5) to TextGrey
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = foodName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "$quantity portions",
                    fontSize = 14.sp,
                    color = TextGrey
                )
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = textColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = status.uppercase(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

// ---------- 详情行 ----------
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextGrey)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

// ---------- 时间线卡片 ----------
@Composable
private fun TimelineCard(status: String, createdAt: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Request Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(12.dp))

            val timelineItems = generateTimeline(status, createdAt)
            timelineItems.forEachIndexed { index, item ->
                TimelineStep(
                    title = item.title,
                    time = item.time,
                    description = item.description,
                    isCompleted = item.isCompleted,
                    isLast = index == timelineItems.size - 1,
                    isNegative = item.isNegative
                )
            }
        }
    }
}

// ---------- 时间线单项 ----------
@Composable
private fun TimelineStep(
    title: String,
    time: String,
    description: String,
    isCompleted: Boolean,
    isLast: Boolean,
    isNegative: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            if (isCompleted) {
                Icon(
                    if (isNegative) Icons.Default.Cancel else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isNegative) RedCancelled else GreenConfirmed,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = TextGrey,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(
                            when {
                                isNegative -> RedCancelled
                                isCompleted -> GreenConfirmed
                                else -> Color(0xFFE0E0E0)
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 8.dp)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) TextDark else TextGrey
            )
            Text(time, fontSize = 12.sp, color = TextGrey)
            Text(description, fontSize = 12.sp, color = TextGrey, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// ---------- 生成时间线数据 ----------
private fun generateTimeline(status: String, createdAt: String): List<TimelineItem> {
    val createdShort = createdAt.take(16).replace("T", " · ")

    // Cancelled 是特殊情况，不走正常的四步流程
    if (status == "Cancelled") {
        return listOf(
            TimelineItem(
                title = "Submitted",
                time = createdShort,
                description = "Request has been successfully logged into our system.",
                isCompleted = true
            ),
            TimelineItem(
                title = "Withdrawn",
                time = createdShort,
                description = "This request was withdrawn and will not proceed further.",
                isCompleted = true,
                isNegative = true
            )
        )
    }

    val isSubmitted = true
    val isViewed = status == "Confirmed" || status == "Completed"
    val isApproved = status == "Confirmed" || status == "Completed"
    val isCollected = status == "Completed"

    return listOf(
        TimelineItem(
            title = "Submitted",
            time = createdShort,
            description = "Request has been successfully logged into our system.",
            isCompleted = isSubmitted
        ),
        TimelineItem(
            title = "Merchant viewed",
            time = if (isViewed) createdShort else "Awaiting...",
            description = if (isViewed) "Merchant has reviewed your request." else "Waiting for merchant to review.",
            isCompleted = isViewed
        ),
        TimelineItem(
            title = "Approved",
            time = if (isApproved) createdShort else "Pending...",
            description = if (isApproved) "Request approved by the food donor." else "Pending approval by the food donor.",
            isCompleted = isApproved
        ),
        TimelineItem(
            title = "Collected",
            time = if (isCollected) createdShort else "Not yet",
            description = if (isCollected) "Pickup completed." else "Awaiting collection.",
            isCompleted = isCollected
        )
    )
}

// ---------- 时间线数据类 ----------
private data class TimelineItem(
    val title: String,
    val time: String,
    val description: String,
    val isCompleted: Boolean,
    val isNegative: Boolean = false
)
// ---------- 简单日期格式化 ----------
private fun formatDate(dateStr: String): String {
    return dateStr.take(16).replace("T", " · ")
}
