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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.data.repository.ReservationRepository
import com.sharebite.app.data.session.CurrentUser
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserMakeReservationScreen(
    listingId: Long,
    reservationId: Long? = null,
    paddingValues: PaddingValues,
    onSuccess: (Long) -> Unit
) {
    val context = LocalContext.current
    val foodRepo = remember { FoodListingRepository() }
    val userId = CurrentUser.getUserId()
    val reservationRepo = remember { ReservationRepository() }
    val scope = rememberCoroutineScope()

    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var existingReservation by remember { mutableStateOf<Reservation?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var note by rememberSaveable { mutableStateOf("") }

    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTime by rememberSaveable { mutableStateOf<String?>(null) }

    // ✅ 确认弹窗状态
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    // ✅ 是否为编辑模式
    val isEditMode = reservationId != null

    val dateList = remember {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displayFormat = SimpleDateFormat("EEE d", Locale.US)
        val dates = mutableListOf<Triple<String, String, String>>()
        for (i in 0..6) {
            val date = calendar.time
            dates.add(
                Triple(
                    format.format(date),
                    displayFormat.format(date),
                    SimpleDateFormat("EEE", Locale.US).format(date)
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        dates
    }

    val allTimeSlots = remember(listing?.pickupTime) {
        buildMerchantPickupSlots(listing?.pickupTime)
    }
    val deadlineDate = remember(listing?.deadline) { parseReservationDeadline(listing?.deadline.orEmpty()) }
    val availableDates = remember(dateList, deadlineDate) {
        dateList.filter { (dateStr, _, _) ->
            deadlineDate == null || parseReservationDate(dateStr)?.let { !it.after(deadlineDate) } == true
        }
    }
    val availableTimeSlots = remember(selectedDate, deadlineDate) {
        allTimeSlots.filter { time ->
            val selectedDateTime = parseReservationDateTime(selectedDate, time)
            selectedDateTime != null && (deadlineDate == null || !selectedDateTime.after(deadlineDate))
        }
    }

    LaunchedEffect(listingId, reservationId) {
        isLoading = true
        try {
            listing = foodRepo.getListingById(listingId)
            if (reservationId != null) {
                existingReservation = reservationRepo.getMyReservationById(reservationId)
                existingReservation?.let { res ->
                    if (selectedDate == null) {
                        selectedDate = res.pickupDate
                    }
                    if (selectedTime == null) {
                        selectedTime = res.pickupTime
                    }
                    if (note.isBlank()) {
                        note = res.note ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(availableDates, availableTimeSlots) {
        if (selectedDate == null || availableDates.none { it.first == selectedDate }) {
            selectedDate = availableDates.firstOrNull()?.first
        }
        if (selectedTime == null || selectedTime !in availableTimeSlots) {
            selectedTime = availableTimeSlots.firstOrNull()
        }
    }

    // ✅ 实际提交函数
    fun submitReservation() {
        if (isSubmitting || listing == null || selectedDate == null || selectedTime == null) return
        val selectedDateTime = parseReservationDateTime(selectedDate, selectedTime)
        if (selectedDateTime == null || selectedTime !in availableTimeSlots) {
            errorMessage = "Please choose a pickup time before the merchant deadline."
            showConfirmDialog = false
            return
        }
        if (listing!!.status != "available" || listing!!.remainingQuantity <= 0) {
            errorMessage = "This food is fully reserved."
            showConfirmDialog = false
            return
        }
        showConfirmDialog = false
        scope.launch {
            isSubmitting = true
            errorMessage = null

            try {
                if (isEditMode && existingReservation != null) {
                    reservationRepo.updateMyPickupTime(
                        reservationId = reservationId!!,
                        newDate = selectedDate!!,
                        newTime = selectedTime!!
                    )
                    isSubmitting = false
                    onSuccess(reservationId!!)
                } else {
                    val newReservationId = reservationRepo.createMyReservation(
                        listingId = listingId,
                        merchantId = listing!!.merchantId ?: "",
                        foodName = listing!!.foodName,
                        quantityNum = 1,
                        unit = listing!!.unit ?: "unit",
                        pickupDate = selectedDate!!,
                        pickupTime = selectedTime!!,
                        note = note.ifBlank { null }
                    )
                    isSubmitting = false
                    if (newReservationId > 0) {
                        onSuccess(newReservationId)
                    } else {
                        errorMessage = "Failed to create reservation. Please try again."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                e.printStackTrace()
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
                      colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DED5))
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
                                items(availableDates) { (dateStr, displayStr, dayOfWeek) ->
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
                                if (availableTimeSlots.isEmpty()) {
                                    Text(
                                        text = "No pickup times are available before the merchant deadline.",
                                        fontSize = 12.sp,
                                        color = RedCancelled
                                    )
                                }
                                allTimeSlots.chunked(2).forEach { rowSlots ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            rowSlots.forEach { time ->
                                                val isSelected = time == selectedTime
                                                val isAvailable = time in availableTimeSlots
                                                Card(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable(enabled = isAvailable) { selectedTime = time },
                                                    shape = RoundedCornerShape(50),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = when {
                                                            isSelected -> OrangeAccent
                                                            isAvailable -> Color.White
                                                            else -> Color(0xFFE9E0DB)
                                                        }
                                                    ),
                                                    border = if (!isSelected && isAvailable)
                                                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDC0B8))
                                                    else null,
                                                    elevation = CardDefaults.cardElevation(
                                                        defaultElevation = if (isSelected) 4.dp else 0.dp
                                                    )
                                                ) {
                                                    Text(
                                                        time,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = when {
                                                            isSelected -> Color.White
                                                            isAvailable -> TextDark
                                                            else -> TextGrey.copy(alpha = 0.55f)
                                                        },
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

                        // ── 数量选择（固定显示 1 Unit） ──
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                      colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    text = "Max 1 per reservation",
                                    fontSize = 11.sp,
                                    color = TextGrey
                                )
                            }
                        }

                        // ── 备注输入框（编辑模式下只读） ──
                        // ── 备注输入框（始终可编辑） ──
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
                            colors = AppTextFieldDefaults.colors()
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
                        onClick = {
                            if (!isSubmitting && selectedDate != null && selectedTime != null) {
                                showConfirmDialog = true
                            }
                        },
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
                        enabled = !isSubmitting && selectedDate != null && selectedTime != null && selectedTime in availableTimeSlots
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = if (isEditMode) "Update" else "Confirm",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ✅ 确认弹窗
            if (showConfirmDialog) {
                // ✅ 获取正确的 quantity：编辑模式用 existingReservation 的，新建模式用 "1 Unit"
                val displayQuantity = if (isEditMode && existingReservation != null) {
                    "${existingReservation!!.quantityNum} ${existingReservation!!.unit}"
                } else {
                    "1 Unit"
                }

                ConfirmReservationDialog(
                    foodName = item.foodName,
                    merchantName = item.merchantName,
                    address = item.address,
                    date = selectedDate ?: "N/A",
                    time = selectedTime ?: "N/A",
                    note = note,
                    quantity = displayQuantity,  // ✅ 使用正确的 quantity
                    isEditMode = isEditMode,
                    onConfirm = { submitReservation() },
                    onDismiss = { showConfirmDialog = false }
                )
            }
        }
    }
}

// ── ✅ 确认预订弹窗 ──
@Composable
private fun ConfirmReservationDialog(
    foodName: String,
    merchantName: String,
    address: String,
    date: String,
    time: String,
    note: String,
    quantity: String,
    isEditMode: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // ── 标题 ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OrangeAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isEditMode) "Update Reservation" else "Confirm Reservation",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Please review your reservation details",
                            fontSize = 13.sp,
                            color = TextGrey
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── 详情卡片 ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextGrey
                            )
                            Column {
                                Text("Food", fontSize = 11.sp, color = TextGrey)
                                Text(
                                    foodName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                        }

                        Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextGrey
                            )
                            Column {
                                Text("Merchant", fontSize = 11.sp, color = TextGrey)
                                Text(
                                    merchantName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                        }

                        Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextGrey
                            )
                            Column {
                                Text("Pickup Address", fontSize = 11.sp, color = TextGrey)
                                Text(
                                    address.ifBlank { "Not available" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                        }

                        Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = TextGrey
                                )
                                Column {
                                    Text("Date", fontSize = 11.sp, color = TextGrey)
                                    Text(
                                        date,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = TextGrey
                                )
                                Column {
                                    Text("Time", fontSize = 11.sp, color = TextGrey)
                                    Text(
                                        time,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                }
                            }
                        }

                        Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextGrey
                            )
                            Column {
                                Text("Quantity", fontSize = 11.sp, color = TextGrey)
                                Text(
                                    quantity,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                        }

                        if (note.isNotBlank()) {
                            Divider(color = Color(0xFFDDC0B8).copy(alpha = 0.3f))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Message,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = TextGrey
                                )
                                Column {
                                    Text("Note", fontSize = 11.sp, color = TextGrey)
                                    Text(
                                        note,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (isEditMode) "Confirm Update" else "Confirm",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

private fun parseReservationDeadline(deadline: String): Date? {
    if (deadline.isBlank()) return null

    val normalizedDeadline = deadline.trim()
        .substringBefore('+')
        .removeSuffix("Z")
        .replace(Regex("T\\s*"), " ")

    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )

    formats.forEach { format ->
        try {
            format.isLenient = false
            return format.parse(normalizedDeadline)
        } catch (_: Exception) {
        }
    }

    return null
}

private fun parseReservationDate(date: String): Date? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }.parse(date)
    } catch (_: Exception) {
        null
    }
}

private fun parseReservationDateTime(date: String?, time: String?): Date? {
    if (date.isNullOrBlank() || time.isNullOrBlank()) return null

    val normalizedTime = time.trim().substringBefore('+').removeSuffix("Z")
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
        SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
    )

    formats.forEach { format ->
        try {
            format.isLenient = false
            return format.parse("$date $normalizedTime")
        } catch (_: Exception) {
        }
    }
    return null
}

private fun buildMerchantPickupSlots(pickupTime: String?): List<String> {
    val slots = pickupTime
        ?.split(',', ';', '|', '\n')
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?.map { formatReservationTimeSlot(it) }
        ?.distinct()
        .orEmpty()

    return slots.ifEmpty { listOf("10:00 AM", "12:30 PM", "02:00 PM", "04:30 PM") }
}

private fun formatReservationTimeSlot(time: String): String {
    val normalizedTime = time.trim().substringBefore('+').removeSuffix("Z")
    val formats = listOf(
        SimpleDateFormat("HH:mm:ss", Locale.US),
        SimpleDateFormat("HH:mm", Locale.US),
        SimpleDateFormat("hh:mm a", Locale.US)
    )

    formats.forEach { format ->
        try {
            format.isLenient = false
            val parsed = format.parse(normalizedTime)
            if (parsed != null) {
                return SimpleDateFormat("hh:mm a", Locale.US).format(parsed)
            }
        } catch (_: Exception) {
        }
    }
    return normalizedTime
}
