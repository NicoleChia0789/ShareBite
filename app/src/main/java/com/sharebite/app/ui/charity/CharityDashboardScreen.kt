package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.repository.CharityRepository

// Color
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.theme.GreenConfirmed
import com.sharebite.app.ui.theme.GreyCompleted
import com.sharebite.app.ui.theme.RedCancelled
import com.sharebite.app.ui.theme.TextGrey
import com.sharebite.app.ui.theme.TextDark
import com.sharebite.app.ui.theme.CardCream

// 假的数据模型，之后会换成从 Supabase 拿真实数据
data class ActivityItem(
    val merchantName: String,
    val pickupTime: String,
    val status: String // "Confirmed", "Pending", etc
)

@Composable
fun CharityDashboardScreen(
    paddingValues: PaddingValues,
    onCreateRequest: () -> Unit  // 新增
) {
    val repository = remember { CharityRepository() }

    var allRequests by remember { mutableStateOf<List<CharityRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            allRequests = repository.getAllRequests()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val pending = allRequests.count { it.status == "Pending" }
    val confirmed = allRequests.count { it.status == "Confirmed" }
    val completed = allRequests.count { it.status == "Completed" }
    val cancelled = allRequests.count { it.status == "Cancelled" }
    val goalPercent = ((confirmed.toFloat() / 10f) * 100).coerceAtMost(100f).toInt()

    val recentActivities = allRequests
        .filter { it.status == "Confirmed" }
        .take(3)
        .map { ActivityItem(it.organisationName.ifBlank { it.foodName }, "${it.pickupDate}, ${it.pickupTime}", it.status) }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeAccent)
            }
            return
        }

        if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $errorMessage", color = RedCancelled)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            // ---------- HERO SECTION ----------
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Together, we can end hunger.",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Your efforts have helped provide over 1,200 meals this month. Every request matters.",
                    fontSize = 14.sp,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCreateRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp)  // 可选的，保持一致风格
                ) {
                    Text("+ Create Food Request", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- STATS GRID (2x2) ----------
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard("Pending", pending.toString(), OrangeAccent, Modifier.weight(1f))
                    StatCard("Confirmed", confirmed.toString(), GreenConfirmed, Modifier.weight(1f))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard("Completed", completed.toString(), GreyCompleted, Modifier.weight(1f))
                    StatCard("Cancelled", cancelled.toString(), RedCancelled, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- RECENT ACTIVITY ----------
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Recent Activity", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Confirmed donations ready for pickup", fontSize = 12.sp, color = TextGrey)
                    }
                    Text(
                        "View All",
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                recentActivities.forEach { activity ->
                    ActivityCard(activity)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---------- COMMUNITY PULSE ----------
            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Community Pulse",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Area demand is 15% higher than usual today.",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily Goal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                        Text("$goalPercent%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { goalPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GreenConfirmed,
                        trackColor = Color(0xFFE8E2D8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }


@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
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

@Composable
private fun ActivityCard(activity: ActivityItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // 之后可以换成真实图片，先用一个色块占位
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFE6D9))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(activity.merchantName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Pickup: ${activity.pickupTime}", fontSize = 12.sp, color = TextGrey)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFE3F5E6)
            ) {
                Text(
                    activity.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = GreenConfirmed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CharityDashboardScreenPreview() {
    CharityDashboardScreen(
        paddingValues = PaddingValues(),
        onCreateRequest = {}  // ← 改成正确的参数名
    )
}