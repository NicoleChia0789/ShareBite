////package com.sharebite.app.ui.merchant
////
////import androidx.compose.foundation.background
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
////import com.sharebite.app.ui.theme.BgCream
////import com.sharebite.app.ui.theme.OrangeAccent
////
////@Composable
////fun MerchantReservationsPage(
////    paddingValues: PaddingValues,
////    viewModel: MerchantViewModel,
////    onViewAllRequests: () -> Unit,
////    onViewAllReservations: () -> Unit,
////    onPickupDetailsClick: (String) -> Unit
////) {
////    val requests by viewModel.requests.collectAsState()
////    val reservations by viewModel.reservations.collectAsState()
////
////    // ONLY show items that are Confirmed/Approved (ready for pickup)
////    val confirmedRequests = requests.filter { it.status == "Confirmed" }
////    val confirmedReservations = reservations.filter { it.status == "Confirmed" }
////
////    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(BgCream)) {
////        Text("Pickup Requests", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
////
////        LazyColumn(
////            modifier = Modifier.fillMaxSize(),
////            contentPadding = PaddingValues(16.dp),
////            verticalArrangement = Arrangement.spacedBy(12.dp)
////        ) {
////            items(confirmedRequests) { request ->
////                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
////                    Column(modifier = Modifier.padding(16.dp)) {
////                        Text(request.organisationName, fontWeight = FontWeight.Bold)
////                        Text("${request.quantity} x ${request.foodName}", color = Color.Gray)
////                        Spacer(modifier = Modifier.height(12.dp))
////                        Button(onClick = { onPickupDetailsClick("req_${request.id}") }, colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent), modifier = Modifier.fillMaxWidth()) {
////                            Text("View Pickup Details", color = Color.White)
////                        }
////                    }
////                }
////            }
////
////            items(confirmedReservations) { reservation ->
////                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
////                    Column(modifier = Modifier.padding(16.dp)) {
////                        Text("User: ${reservation.userId.take(8)}", fontWeight = FontWeight.Bold)
////                        Text("${reservation.quantity} x ${reservation.foodName}", color = Color.Gray)
////                        Text("Pickup: ${reservation.pickupDate} at ${reservation.pickupTime}", color = Color.Gray)
////                        Spacer(modifier = Modifier.height(12.dp))
////                        Button(onClick = { onPickupDetailsClick("res_${reservation.id}") }, colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent), modifier = Modifier.fillMaxWidth()) {
////                            Text("View Pickup Details", color = Color.White)
////                        }
////                    }
////                }
////            }
////        }
////    }
////}
//
//package com.sharebite.app.ui.merchant
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.sharebite.app.ui.theme.BgCream
//import com.sharebite.app.ui.theme.OrangeAccent
//
//@Composable
//fun MerchantReservationsPage(
//    paddingValues: PaddingValues,
//    viewModel: MerchantViewModel,
//    onViewAllRequests: () -> Unit,
//    onViewAllReservations: () -> Unit,
//    onPickupDetailsClick: (String) -> Unit // kept for compatibility
//) {
//    val requests by viewModel.requests.collectAsState()
//    val generalRequests by viewModel.generalRequests.collectAsState()
//    val reservations by viewModel.reservations.collectAsState()
//
//    val pendingPickups = reservations.count { it.status == "Pending" } +
//            generalRequests.count { it.status == "Pending" }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(paddingValues)
//            .background(BgCream)
//    ) {
//        Text(
//            "Requests & Reservations",
//            style = MaterialTheme.typography.headlineSmall,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(16.dp)
//        )
//
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // 1️⃣ Pickup Schedule Card
//            item {
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(2.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                "Pickup Schedule",
//                                fontWeight = FontWeight.Bold,
//                                fontSize = 16.sp
//                            )
//                            Text(
//                                "$pendingPickups Pending pickups for today",
//                                fontSize = 13.sp,
//                                color = Color.Gray
//                            )
//                        }
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedButton(
//                            onClick = { /* Navigate to a full schedule – can use onViewAllRequests or a new route */ },
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(50),
//                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
//                        ) {
//                            Text("View Schedule")
//                        }
//                    }
//                }
//            }
//
//            // 2️⃣ Charity Requests Section
//            item {
//                SectionCard(
//                    title = "Charity Requests",
//                    subtitle = "Manage NGO & Food Bank claims. Review and approve bulk donation requests.",
//                    onViewAll = onViewAllRequests
//                )
//            }
//
//            // 3️⃣ User Reservations Section
//            item {
//                SectionCard(
//                    title = "User Reservations",
//                    subtitle = "Manage community member claims. Track individual food reservations and pickups.",
//                    onViewAll = onViewAllReservations
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun SectionCard(
//    title: String,
//    subtitle: String,
//    onViewAll: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onViewAll() },
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
//            Column(modifier = Modifier.weight(1f)) {
//                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//            }
//            Icon(
//                Icons.Default.ArrowForward,
//                contentDescription = null,
//                tint = OrangeAccent,
//                modifier = Modifier.size(20.dp)
//            )
//        }
//    }
//}

package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

@Composable
fun MerchantReservationsPage(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onViewAllRequests: () -> Unit,
    onViewAllReservations: () -> Unit,
    onPickupDetailsClick: (String) -> Unit // kept for compatibility
) {
    val requests by viewModel.requests.collectAsState()
    val generalRequests by viewModel.generalRequests.collectAsState()
    val reservations by viewModel.reservations.collectAsState()

    val pendingPickups = reservations.count { it.status == "Pending" } +
            generalRequests.count { it.status == "Pending" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
    ) {
        Text(
            "Requests & Reservations",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp) // more spacing
        ) {
            // 1️⃣ Pickup Schedule Card (enhanced)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0) // soft orange
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Pickup Schedule",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF4E342E)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$pendingPickups Pending pickups for today",
                            fontSize = 14.sp,
                            color = Color(0xFF6D4C41)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { /* navigate */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OrangeAccent
                            )
                        ) {
                            Text("View Schedule")
                        }
                    }
                }
            }

            // 2️⃣ Charity Requests Section (enhanced)
            item {
                SectionCardEnhanced(
                    icon = Icons.Default.Business,
                    title = "Charity Requests",
                    subtitle = "Manage NGO & Food Bank claims. Review and approve bulk donation requests.",
                    accentColor = Color(0xFF1976D2), // blue accent
                    onViewAll = onViewAllRequests
                )
            }

            // 3️⃣ User Reservations Section (enhanced)
            item {
                SectionCardEnhanced(
                    icon = Icons.Default.Person,
                    title = "User Reservations",
                    subtitle = "Manage community member claims. Track individual food reservations and pickups.",
                    accentColor = Color(0xFF2E7D32), // green accent
                    onViewAll = onViewAllReservations
                )
            }
        }
    }
}

@Composable
fun SectionCardEnhanced(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewAll() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent strip
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp) // more padding
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E1B16)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                // "View all" as a text button with arrow icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "View all →",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    // We already have the arrow icon in the row, but we can add an extra icon if needed
                }
            }
        }
    }
}