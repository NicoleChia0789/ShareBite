//package com.sharebite.app.ui.merchant
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Storefront
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.sharebite.app.data.model.CharityRequest
//import com.sharebite.app.data.model.Reservation
//import com.sharebite.app.ui.theme.BgCream
//import com.sharebite.app.ui.theme.OrangeAccent
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun MerchantPickupScheduleScreen(
//    paddingValues: PaddingValues,
//    viewModel: MerchantViewModel,
//    onBack: () -> Unit
//) {
//    val reservations by viewModel.reservations.collectAsState()
//    val requests by viewModel.requests.collectAsState()
//
//    // Combine and filter for pickup items (Confirmed or Pending)
//    val pickupItems = remember(reservations, requests) {
//        val items = mutableListOf<PickupScheduleItem>()
//
//        // Add reservations that are not Completed or Cancelled
//        reservations.filter { it.status != "Completed" && it.status != "Cancelled" }
//            .forEach { res ->
//                items.add(
//                    PickupScheduleItem.Reservation(
//                        reservation = res,
//                        pickupDate = res.pickupDate,
//                        pickupTime = res.pickupTime
//                    )
//                )
//            }
//
//        // Add charity requests that are not Completed or Cancelled
//        requests.filter { it.status != "Completed" && it.status != "Cancelled" }
//            .forEach { req ->
//                items.add(
//                    PickupScheduleItem.Request(
//                        request = req,
//                        pickupDate = req.pickupDate,
//                        pickupTime = req.pickupTime
//                    )
//                )
//            }
//
//        // Sort by date, then by time
//        items.sortedWith(
//            compareBy<PickupScheduleItem> { it.pickupDate }
//                .thenBy { it.pickupTime }
//        )
//    }
//
//    // Group by date
//    val groupedItems = pickupItems.groupBy { it.pickupDate }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(paddingValues)
//            .background(BgCream)
//    ) {
//        // Header
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconButton(onClick = onBack) {
//                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//            }
//            Text(
//                "Pickup Schedule",
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF1E1B16)
//            )
//        }
//
//        if (pickupItems.isEmpty()) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text("📅", fontSize = 48.sp)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        "No upcoming pickups",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Gray
//                    )
//                    Text(
//                        "Confirmed pickups will appear here.",
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//                }
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(16.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                groupedItems.forEach { (date, itemsForDate) ->
//                    // Date header
//                    item {
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(8.dp),
//                            colors = CardDefaults.cardColors(
//                                containerColor = OrangeAccent.copy(alpha = 0.15f)
//                            )
//                        ) {
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(12.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(
//                                    Icons.Default.DateRange,
//                                    contentDescription = null,
//                                    tint = OrangeAccent,
//                                    modifier = Modifier.size(20.dp)
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = formatDateDisplay(date),
//                                    fontWeight = FontWeight.Bold,
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF4E342E)
//                                )
//                                Spacer(modifier = Modifier.weight(1f))
//                                Text(
//                                    "${itemsForDate.size} pickup${if (itemsForDate.size > 1) "s" else ""}",
//                                    fontSize = 13.sp,
//                                    color = Color.Gray
//                                )
//                            }
//                        }
//                    }
//
//                    // Items for this date
//                    items(itemsForDate) { item ->
//                        when (item) {
//                            is PickupScheduleItem.Reservation -> {
//                                PickupCard(
//                                    icon = Icons.Default.Person,
//                                    iconColor = Color(0xFF1976D2),
//                                    title = "User: ${item.reservation.userId.take(8)}",
//                                    food = item.reservation.foodName,
//                                    quantity = item.reservation.quantity,
//                                    pickupTime = item.reservation.pickupTime,
//                                    status = item.reservation.status
//                                )
//                            }
//                            is PickupScheduleItem.Request -> {
//                                PickupCard(
//                                    icon = Icons.Default.Storefront,
//                                    iconColor = Color(0xFF2E7D32),
//                                    title = item.request.organisationName,
//                                    food = item.request.foodName,
//                                    quantity = item.request.quantity.toString(),
//                                    pickupTime = item.request.pickupTime,
//                                    status = item.request.status
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// ─── Data model ──────────────────────────────────────────────────
//
//sealed class PickupScheduleItem(
//    open val pickupDate: String,
//    open val pickupTime: String
//) {
//    data class Reservation(
//        val reservation: Reservation,
//        override val pickupDate: String,
//        override val pickupTime: String
//    ) : PickupScheduleItem(pickupDate, pickupTime)
//
//    data class Request(
//        val request: CharityRequest,
//        override val pickupDate: String,
//        override val pickupTime: String
//    ) : PickupScheduleItem(pickupDate, pickupTime)
//}
//
//// ─── Pickup Card ──────────────────────────────────────────────────
//
//@Composable
//fun PickupCard(
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    iconColor: Color,
//    title: String,
//    food: String,
//    quantity: String,
//    pickupTime: String,
//    status: String
//) {
//    val statusColor = when (status) {
//        "Pending" -> OrangeAccent
//        "Confirmed" -> Color(0xFF2E7D32)
//        else -> Color.Gray
//    }
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(2.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Icon
//            Box(
//                modifier = Modifier
//                    .size(44.dp)
//                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(50)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    icon,
//                    contentDescription = null,
//                    tint = iconColor,
//                    modifier = Modifier.size(22.dp)
//                )
//            }
//            Spacer(modifier = Modifier.width(12.dp))
//
//            // Content
//            Column(modifier = Modifier.weight(1f)) {
//                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
//                Text("$quantity × $food", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//                Text("Pickup at $pickupTime", fontSize = 13.sp, color = Color.Gray)
//            }
//
//            // Status badge
//            Surface(
//                shape = RoundedCornerShape(50),
//                color = statusColor.copy(alpha = 0.15f)
//            ) {
//                Text(
//                    status,
//                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
//                    fontSize = 11.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = statusColor
//                )
//            }
//        }
//    }
//}
//
//// ─── Helper: format date ──────────────────────────────────────────
//
//fun formatDateDisplay(dateStr: String): String {
//    return try {
//        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
//        val date = inputFormat.parse(dateStr)
//        if (date != null) outputFormat.format(date) else dateStr
//    } catch (e: Exception) {
//        dateStr
//    }
//}