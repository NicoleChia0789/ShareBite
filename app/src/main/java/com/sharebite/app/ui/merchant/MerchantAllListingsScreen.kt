//////package com.sharebite.app.ui.merchant
//////
//////import androidx.compose.foundation.background
//////import androidx.compose.foundation.layout.*
//////import androidx.compose.foundation.lazy.LazyColumn
//////import androidx.compose.foundation.lazy.items
//////import androidx.compose.foundation.shape.RoundedCornerShape
//////import androidx.compose.material.icons.Icons
//////import androidx.compose.material.icons.filled.Search
//////import androidx.compose.material3.*
//////import androidx.compose.runtime.*
//////import androidx.compose.ui.Alignment
//////import androidx.compose.ui.Modifier
//////import androidx.compose.ui.graphics.Color
//////import androidx.compose.ui.text.font.FontWeight
//////import androidx.compose.ui.unit.dp
//////import com.sharebite.app.ui.theme.BgCream
//////import com.sharebite.app.ui.theme.OrangeAccent
//////
//////@Composable
//////fun MerchantAllListingsScreen(
//////    paddingValues: PaddingValues,
//////    viewModel: MerchantViewModel,
//////    onListingClick: (Long) -> Unit
//////) {
//////    val listings by viewModel.listings.collectAsState()
//////
//////    // Search Query State
//////    var searchQuery by remember { mutableStateOf("") }
//////
//////    // Filter by Search
//////    val filteredListings = listings.filter {
//////        it.foodName.contains(searchQuery, ignoreCase = true) ||
//////                it.category.contains(searchQuery, ignoreCase = true)
//////    }
//////
//////    Column(
//////        modifier = Modifier
//////            .fillMaxSize()
//////            .padding(paddingValues)
//////            .background(BgCream)
//////    ) {
//////        // Search Bar
//////        OutlinedTextField(
//////            value = searchQuery,
//////            onValueChange = { searchQuery = it },
//////            modifier = Modifier
//////                .fillMaxWidth()
//////                .padding(horizontal = 16.dp, vertical = 12.dp),
//////            placeholder = { Text("Search by food name or category") },
//////            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//////            singleLine = true,
//////            shape = RoundedCornerShape(12.dp),
//////            colors = OutlinedTextFieldDefaults.colors(
//////                focusedBorderColor = OrangeAccent,
//////                unfocusedBorderColor = Color.LightGray
//////            )
//////        )
//////
//////        if (filteredListings.isEmpty()) {
//////            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//////                Text("No listings found.", color = Color.Gray)
//////            }
//////        } else {
//////            LazyColumn(
//////                modifier = Modifier.fillMaxSize(),
//////                contentPadding = PaddingValues(16.dp),
//////                verticalArrangement = Arrangement.spacedBy(12.dp)
//////            ) {
//////                items(filteredListings) { listing ->
//////                    Card(
//////                        colors = CardDefaults.cardColors(containerColor = Color.White),
//////                        shape = RoundedCornerShape(12.dp),
//////                        modifier = Modifier.fillMaxWidth()
//////                    ) {
//////                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
//////                            Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp)))
//////                            Spacer(modifier = Modifier.width(16.dp))
//////                            Column(modifier = Modifier.weight(1f)) {
//////                                Text(listing.foodName, fontWeight = FontWeight.SemiBold)
//////                                Text("${listing.quantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//////                                Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
//////                            }
//////                            OutlinedButton(
//////                                onClick = { onListingClick(listing.id) },
//////                                modifier = Modifier.height(36.dp)
//////                            ) {
//////                                Text("View")
//////                            }
//////                        }
//////                    }
//////                }
//////            }
//////        }
//////    }
//////}
////
////package com.sharebite.app.ui.merchant
////
////import androidx.compose.foundation.background
////import androidx.compose.foundation.layout.*
////import androidx.compose.foundation.lazy.LazyColumn
////import androidx.compose.foundation.lazy.items
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.material.icons.Icons
////import androidx.compose.material.icons.filled.Delete
////import androidx.compose.material.icons.filled.Edit
////import androidx.compose.material.icons.filled.Search
////import androidx.compose.material3.*
////import androidx.compose.runtime.*
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.unit.dp
////import com.sharebite.app.ui.theme.BgCream
////import com.sharebite.app.ui.theme.OrangeAccent
////
////@Composable
////fun MerchantAllListingsScreen(
////    paddingValues: PaddingValues,
////    viewModel: MerchantViewModel,
////    onListingClick: (Long) -> Unit,
////    onEditClick: (Long) -> Unit
////) {
////    val listings by viewModel.listings.collectAsState()
////
////    var searchQuery by remember { mutableStateOf("") }
////    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }   // holds the listing awaiting delete confirmation
////    var pendingDeleteName by remember { mutableStateOf("") }
////
////    val filteredListings = listings.filter {
////        it.foodName.contains(searchQuery, ignoreCase = true) ||
////                it.category.contains(searchQuery, ignoreCase = true)
////    }
////
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(paddingValues)
////            .background(BgCream)
////    ) {
////        OutlinedTextField(
////            value = searchQuery,
////            onValueChange = { searchQuery = it },
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(horizontal = 16.dp, vertical = 12.dp),
////            placeholder = { Text("Search by food name or category") },
////            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
////            singleLine = true,
////            shape = RoundedCornerShape(12.dp),
////            colors = OutlinedTextFieldDefaults.colors(
////                focusedBorderColor = OrangeAccent,
////                unfocusedBorderColor = Color.LightGray
////            )
////        )
////
////        if (filteredListings.isEmpty()) {
////            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
////                Text("No listings found.", color = Color.Gray)
////            }
////        } else {
////            LazyColumn(
////                modifier = Modifier.fillMaxSize(),
////                contentPadding = PaddingValues(16.dp),
////                verticalArrangement = Arrangement.spacedBy(12.dp)
////            ) {
////                items(filteredListings) { listing ->
////                    Card(
////                        colors = CardDefaults.cardColors(containerColor = Color.White),
////                        shape = RoundedCornerShape(12.dp),
////                        modifier = Modifier.fillMaxWidth()
////                    ) {
////                        Column(modifier = Modifier.padding(16.dp)) {
////                            Row(verticalAlignment = Alignment.CenterVertically) {
////                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp)))
////                                Spacer(modifier = Modifier.width(16.dp))
////                                Column(modifier = Modifier.weight(1f)) {
////                                    Text(listing.foodName, fontWeight = FontWeight.SemiBold)
////                                    Text("${listing.quantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
////                                    Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
////                                }
////                            }
////
////                            Spacer(modifier = Modifier.height(12.dp))
////
////                            Row(
////                                modifier = Modifier.fillMaxWidth(),
////                                horizontalArrangement = Arrangement.spacedBy(8.dp)
////                            ) {
////                                OutlinedButton(
////                                    onClick = { onListingClick(listing.id) },
////                                    modifier = Modifier.weight(1f),
////                                    contentPadding = PaddingValues(0.dp)
////                                ) {
////                                    Text("View")
////                                }
////                                OutlinedButton(
////                                    onClick = { onEditClick(listing.id) },
////                                    modifier = Modifier.weight(1f),
////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent),
////                                    contentPadding = PaddingValues(0.dp)
////                                ) {
////                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
////                                    Spacer(modifier = Modifier.width(4.dp))
////                                    Text("Edit")
////                                }
////                                OutlinedButton(
////                                    onClick = {
////                                        pendingDeleteId = listing.id
////                                        pendingDeleteName = listing.foodName
////                                    },
////                                    modifier = Modifier.weight(1f),
////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
////                                    contentPadding = PaddingValues(0.dp)
////                                ) {
////                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
////                                    Spacer(modifier = Modifier.width(4.dp))
////                                    Text("Delete")
////                                }
////                            }
////                        }
////                    }
////                }
////            }
////        }
////    }
////
////    // Delete confirmation
////    val deleteId = pendingDeleteId
////    if (deleteId != null) {
////        AlertDialog(
////            onDismissRequest = { pendingDeleteId = null },
////            title = { Text("Delete donation?") },
////            text = { Text("\"$pendingDeleteName\" will be permanently removed. This can't be undone.") },
////            confirmButton = {
////                TextButton(
////                    onClick = {
////                        viewModel.deleteListing(deleteId)
////                        pendingDeleteId = null
////                    }
////                ) {
////                    Text("Delete", color = Color.Red)
////                }
////            },
////            dismissButton = {
////                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
////            }
////        )
////    }
////}
//
//package com.sharebite.app.ui.merchant
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.sharebite.app.ui.theme.BgCream
//import com.sharebite.app.ui.theme.OrangeAccent
//
//@Composable
//fun MerchantAllListingsScreen(
//    paddingValues: PaddingValues,
//    viewModel: MerchantViewModel,
//    onListingClick: (Long) -> Unit,
//    onEditClick: (Long) -> Unit // ✅ Added
//) {
//    val listings by viewModel.listings.collectAsState()
//    var searchQuery by remember { mutableStateOf("") }
//    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
//    var pendingDeleteName by remember { mutableStateOf("") }
//
//    val filteredListings = listings.filter {
//        it.foodName.contains(searchQuery, ignoreCase = true) ||
//                it.category.contains(searchQuery, ignoreCase = true)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(paddingValues)
//            .background(BgCream)
//    ) {
//        OutlinedTextField(
//            value = searchQuery,
//            onValueChange = { searchQuery = it },
//            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
//            placeholder = { Text("Search by food name or category") },
//            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//            singleLine = true,
//            shape = RoundedCornerShape(12.dp),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = OrangeAccent,
//                unfocusedBorderColor = Color.LightGray
//            )
//        )
//
//        if (filteredListings.isEmpty()) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text("No listings found.", color = Color.Gray)
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(filteredListings) { listing ->
//                    Card(
//                        colors = CardDefaults.cardColors(containerColor = Color.White),
//                        shape = RoundedCornerShape(12.dp),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp)))
//                                Spacer(modifier = Modifier.width(16.dp))
//                                Column(modifier = Modifier.weight(1f)) {
//                                    Text(listing.foodName, fontWeight = FontWeight.SemiBold)
//                                    Text("${listing.quantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//                                    Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                OutlinedButton(
//                                    onClick = { onListingClick(listing.id) },
//                                    modifier = Modifier.weight(1f),
//                                    contentPadding = PaddingValues(0.dp)
//                                ) {
//                                    Text("View")
//                                }
//                                OutlinedButton(
//                                    onClick = { onEditClick(listing.id) }, // ✅ EDIT
//                                    modifier = Modifier.weight(1f),
//                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent),
//                                    contentPadding = PaddingValues(0.dp)
//                                ) {
//                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
//                                    Spacer(modifier = Modifier.width(4.dp))
//                                    Text("Edit")
//                                }
//                                OutlinedButton(
//                                    onClick = {
//                                        pendingDeleteId = listing.id
//                                        pendingDeleteName = listing.foodName
//                                    },
//                                    modifier = Modifier.weight(1f),
//                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
//                                    contentPadding = PaddingValues(0.dp)
//                                ) {
//                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
//                                    Spacer(modifier = Modifier.width(4.dp))
//                                    Text("Delete")
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    // Delete confirmation
//    val deleteId = pendingDeleteId
//    if (deleteId != null) {
//        AlertDialog(
//            onDismissRequest = { pendingDeleteId = null },
//            title = { Text("Delete donation?") },
//            text = { Text("\"$pendingDeleteName\" will be permanently removed.") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        viewModel.deleteListing(deleteId)
//                        pendingDeleteId = null
//                    }
//                ) {
//                    Text("Delete", color = Color.Red)
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
//            }
//        )
//    }
//}

package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

@Composable
fun MerchantAllListingsScreen(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onListingClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit  // ✅ Added onEditClick parameter
) {
    val listings by viewModel.listings.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteName by remember { mutableStateOf("") }

    val filteredListings = listings.filter {
        it.foodName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Search by food name or category") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color.LightGray
            )
        )

        if (filteredListings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No listings found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredListings) { listing ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(listing.foodName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${listing.quantity} left",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        listing.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OrangeAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onListingClick(listing.id) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("View")
                                }
                                OutlinedButton(
                                    onClick = { onEditClick(listing.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit")
                                }
                                OutlinedButton(
                                    onClick = {
                                        pendingDeleteId = listing.id
                                        pendingDeleteName = listing.foodName
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    val deleteId = pendingDeleteId
    if (deleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete donation?") },
            text = { Text("\"$pendingDeleteName\" will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteListing(deleteId)
                        pendingDeleteId = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}