package com.sharebite.app.ui.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserMakeReservationScreen(
    listingId: Long,
    paddingValues: PaddingValues,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current  // ✅ 添加
    val foodRepo = remember { FoodListingRepository() }
    val reservationRepo = remember { ReservationRepository() }
    val scope = rememberCoroutineScope()

    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }

    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    val dateList = remember {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEE d", Locale.getDefault())
        val dates = mutableListOf<Triple<String, String, String>>()
        for (i in 0..6) {
            val date = calendar.time
            dates.add(
                Triple(
                    format.format(date),
                    displayFormat.format(date),
                    SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        dates
    }

    val timeSlots = listOf("10:00 AM", "12:30 PM", "02:00 PM", "04:30 PM")

    LaunchedEffect(listingId) {
        try {
            listing = foodRepo.getListingById(listingId)
            selectedDate = dateList.firstOrNull()?.first
            selectedTime = timeSlots.firstOrNull()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    fun submitReservation() {
        if (isSubmitting || listing == null || selectedDate == null || selectedTime == null) return
        scope.launch {
            isSubmitting = true
            try {
                reservationRepo.createReservation(
                    userId = "258c6924-0bdd-481f-a6d8-cdafb4e7abe3",
                    listingId = listingId,
                    merchantId = listing!!.merchantId ?: "",
                    foodName = listing!!.foodName,
                    quantity = listing!!.quantity,
                    pickupDate = selectedDate!!,
                    pickupTime = selectedTime!!,
                    note = note
                )
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
                isSubmitting = false
            }
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            CircularProgressIndicator(color = OrangeAccent)
        }

        listing == null -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
            Text("Food not found", color = TextGrey)
        }

        else -> {
            val item = listing!!

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    // ── 顶部食物图片 ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (!item.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = item.photoUrl,
                                contentDescription = item.foodName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFEFE6D9))
                                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fastfood,
                                    contentDescription = "Food",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFFD8C4A8)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = item.foodName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = item.merchantName,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── 取餐地址卡片 ──
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(OrangeAccent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = OrangeAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.address.ifBlank { "Address not available" },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                    // ✅ 修复：添加 context 引用
                                    Text(
                                        text = "View on Map",
                                        fontSize = 12.sp,
                                        color = OrangeAccent,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(item.address)}")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        }
                                    )
                                }
                            }
                        }

                        // ── 日期选择 ──
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select Date",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(dateList) { (dateStr, displayStr, dayOfWeek) ->
                                    val isSelected = dateStr == selectedDate
                                    Card(
                                        modifier = Modifier
                                            .width(68.dp)
                                            .clickable { selectedDate = dateStr },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) OrangeAccent else Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = if (isSelected) 4.dp else 1.dp
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                dayOfWeek,
                                                fontSize = 11.sp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextGrey,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                displayStr.split(" ").last(),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else TextDark
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── 时间槽 ──
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Available Times",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                timeSlots.chunked(2).forEach { rowSlots ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rowSlots.forEach { time ->
                                            val isSelected = time == selectedTime
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedTime = time },
                                                shape = RoundedCornerShape(50),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) OrangeAccent else Color.White
                                                ),
                                                border = if (!isSelected)
                                                    androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDC0B8))
                                                else null,
                                                elevation = CardDefaults.cardElevation(
                                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                                )
                                            ) {
                                                Text(
                                                    time,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else TextDark,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                        if (rowSlots.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // ── 数量选择 ──
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Quantity",
                                        fontSize = 12.sp,
                                        color = TextGrey
                                    )
                                    Text(
                                        text = "1 Unit",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }
                                Text(
                                    text = "Max limit per reservation",
                                    fontSize = 11.sp,
                                    color = TextGrey
                                )
                            }
                        }

                        // ── 备注输入框 ──
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            label = { Text("Message for Merchant (Optional)", color = TextGrey) },
                            placeholder = { Text("Let the donor know when you'll be arriving...", color = TextGrey) },
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangeAccent,
                                unfocusedBorderColor = Color(0xFFDDC0B8)
                            )
                        )

                        if (errorMessage != null) {
                            Text(
                                text = "Error: $errorMessage",
                                color = RedCancelled,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ── 底部固定 Confirm 按钮 ──
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = BgCream,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { submitReservation() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = !isSubmitting && selectedDate != null && selectedTime != null
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Confirm",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}