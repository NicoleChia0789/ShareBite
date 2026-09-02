//////////package com.sharebite.app.ui.merchant
//////////
//////////import androidx.compose.foundation.background
//////////import androidx.compose.foundation.layout.*
//////////import androidx.compose.foundation.lazy.LazyColumn
//////////import androidx.compose.foundation.lazy.items
//////////import androidx.compose.foundation.shape.RoundedCornerShape
//////////import androidx.compose.material.icons.Icons
//////////import androidx.compose.material.icons.filled.Search
//////////import androidx.compose.material3.*
//////////import androidx.compose.runtime.*
//////////import androidx.compose.ui.Alignment
//////////import androidx.compose.ui.Modifier
//////////import androidx.compose.ui.graphics.Color
//////////import androidx.compose.ui.text.font.FontWeight
//////////import androidx.compose.ui.unit.dp
//////////import com.sharebite.app.ui.theme.BgCream
//////////import com.sharebite.app.ui.theme.OrangeAccent
//////////
//////////@Composable
//////////fun MerchantAllListingsScreen(
//////////    paddingValues: PaddingValues,
//////////    viewModel: MerchantViewModel,
//////////    onListingClick: (Long) -> Unit
//////////) {
//////////    val listings by viewModel.listings.collectAsState()
//////////
//////////    // Search Query State
//////////    var searchQuery by remember { mutableStateOf("") }
//////////
//////////    // Filter by Search
//////////    val filteredListings = listings.filter {
//////////        it.foodName.contains(searchQuery, ignoreCase = true) ||
//////////                it.category.contains(searchQuery, ignoreCase = true)
//////////    }
//////////
//////////    Column(
//////////        modifier = Modifier
//////////            .fillMaxSize()
//////////            .padding(paddingValues)
//////////            .background(BgCream)
//////////    ) {
//////////        // Search Bar
//////////        OutlinedTextField(
//////////            value = searchQuery,
//////////            onValueChange = { searchQuery = it },
//////////            modifier = Modifier
//////////                .fillMaxWidth()
//////////                .padding(horizontal = 16.dp, vertical = 12.dp),
//////////            placeholder = { Text("Search by food name or category") },
//////////            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//////////            singleLine = true,
//////////            shape = RoundedCornerShape(12.dp),
//////////            colors = OutlinedTextFieldDefaults.colors(
//////////                focusedBorderColor = OrangeAccent,
//////////                unfocusedBorderColor = Color.LightGray
//////////            )
//////////        )
//////////
//////////        if (filteredListings.isEmpty()) {
//////////            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//////////                Text("No listings found.", color = Color.Gray)
//////////            }
//////////        } else {
//////////            LazyColumn(
//////////                modifier = Modifier.fillMaxSize(),
//////////                contentPadding = PaddingValues(16.dp),
//////////                verticalArrangement = Arrangement.spacedBy(12.dp)
//////////            ) {
//////////                items(filteredListings) { listing ->
//////////                    Card(
//////////                        colors = CardDefaults.cardColors(containerColor = Color.White),
//////////                        shape = RoundedCornerShape(12.dp),
//////////                        modifier = Modifier.fillMaxWidth()
//////////                    ) {
//////////                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
//////////                            Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp)))
//////////                            Spacer(modifier = Modifier.width(16.dp))
//////////                            Column(modifier = Modifier.weight(1f)) {
//////////                                Text(listing.foodName, fontWeight = FontWeight.SemiBold)
//////////                                Text("${listing.quantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//////////                                Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
//////////                            }
//////////                            OutlinedButton(
//////////                                onClick = { onListingClick(listing.id) },
//////////                                modifier = Modifier.height(36.dp)
//////////                            ) {
//////////                                Text("View")
//////////                            }
//////////                        }
//////////                    }
//////////                }
//////////            }
//////////        }
//////////    }
//////////}
////////
////////package com.sharebite.app.ui.merchant
////////
////////import androidx.compose.foundation.background
////////import androidx.compose.foundation.layout.*
////////import androidx.compose.foundation.lazy.LazyColumn
////////import androidx.compose.foundation.lazy.items
////////import androidx.compose.foundation.shape.RoundedCornerShape
////////import androidx.compose.material.icons.Icons
////////import androidx.compose.material.icons.filled.Delete
////////import androidx.compose.material.icons.filled.Edit
////////import androidx.compose.material.icons.filled.Search
////////import androidx.compose.material3.*
////////import androidx.compose.runtime.*
////////import androidx.compose.ui.Alignment
////////import androidx.compose.ui.Modifier
////////import androidx.compose.ui.graphics.Color
////////import androidx.compose.ui.text.font.FontWeight
////////import androidx.compose.ui.unit.dp
////////import com.sharebite.app.ui.theme.BgCream
////////import com.sharebite.app.ui.theme.OrangeAccent
////////
////////@Composable
////////fun MerchantAllListingsScreen(
////////    paddingValues: PaddingValues,
////////    viewModel: MerchantViewModel,
////////    onListingClick: (Long) -> Unit,
////////    onEditClick: (Long) -> Unit
////////) {
////////    val listings by viewModel.listings.collectAsState()
////////
////////    var searchQuery by remember { mutableStateOf("") }
////////    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }   // holds the listing awaiting delete confirmation
////////    var pendingDeleteName by remember { mutableStateOf("") }
////////
////////    val filteredListings = listings.filter {
////////        it.foodName.contains(searchQuery, ignoreCase = true) ||
////////                it.category.contains(searchQuery, ignoreCase = true)
////////    }
////////
////////    Column(
////////        modifier = Modifier
////////            .fillMaxSize()
////////            .padding(paddingValues)
////////            .background(BgCream)
////////    ) {
////////        OutlinedTextField(
////////            value = searchQuery,
////////            onValueChange = { searchQuery = it },
////////            modifier = Modifier
////////                .fillMaxWidth()
////////                .padding(horizontal = 16.dp, vertical = 12.dp),
////////            placeholder = { Text("Search by food name or category") },
////////            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
////////            singleLine = true,
////////            shape = RoundedCornerShape(12.dp),
////////            colors = OutlinedTextFieldDefaults.colors(
////////                focusedBorderColor = OrangeAccent,
////////                unfocusedBorderColor = Color.LightGray
////////            )
////////        )
////////
////////        if (filteredListings.isEmpty()) {
////////            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
////////                Text("No listings found.", color = Color.Gray)
////////            }
////////        } else {
////////            LazyColumn(
////////                modifier = Modifier.fillMaxSize(),
////////                contentPadding = PaddingValues(16.dp),
////////                verticalArrangement = Arrangement.spacedBy(12.dp)
////////            ) {
////////                items(filteredListings) { listing ->
////////                    Card(
////////                        colors = CardDefaults.cardColors(containerColor = Color.White),
////////                        shape = RoundedCornerShape(12.dp),
////////                        modifier = Modifier.fillMaxWidth()
////////                    ) {
////////                        Column(modifier = Modifier.padding(16.dp)) {
////////                            Row(verticalAlignment = Alignment.CenterVertically) {
////////                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp)))
////////                                Spacer(modifier = Modifier.width(16.dp))
////////                                Column(modifier = Modifier.weight(1f)) {
////////                                    Text(listing.foodName, fontWeight = FontWeight.SemiBold)
////////                                    Text("${listing.quantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
////////                                    Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
////////                                }
////////                            }
////////
////////                            Spacer(modifier = Modifier.height(12.dp))
////////
////////                            Row(
////////                                modifier = Modifier.fillMaxWidth(),
////////                                horizontalArrangement = Arrangement.spacedBy(8.dp)
////////                            ) {
////////                                OutlinedButton(
////////                                    onClick = { onListingClick(listing.id) },
////////                                    modifier = Modifier.weight(1f),
////////                                    contentPadding = PaddingValues(0.dp)
////////                                ) {
////////                                    Text("View")
////////                                }
////////                                OutlinedButton(
////////                                    onClick = { onEditClick(listing.id) },
////////                                    modifier = Modifier.weight(1f),
////////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent),
////////                                    contentPadding = PaddingValues(0.dp)
////////                                ) {
////////                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
////////                                    Spacer(modifier = Modifier.width(4.dp))
////////                                    Text("Edit")
////////                                }
////////                                OutlinedButton(
////////                                    onClick = {
////////                                        pendingDeleteId = listing.id
////////                                        pendingDeleteName = listing.foodName
////////                                    },
////////                                    modifier = Modifier.weight(1f),
////////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
////////                                    contentPadding = PaddingValues(0.dp)
////////                                ) {
////////                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
////////                                    Spacer(modifier = Modifier.width(4.dp))
////////                                    Text("Delete")
////////                                }
////////                            }
////////                        }
////////                    }
////////                }
////////            }
////////        }
////////    }
////////
////////    // Delete confirmation
////////    val deleteId = pendingDeleteId
////////    if (deleteId != null) {
////////        AlertDialog(
////////            onDismissRequest = { pendingDeleteId = null },
////////            title = { Text("Delete donation?") },
////////            text = { Text("\"$pendingDeleteName\" will be permanently removed. This can't be undone.") },
////////            confirmButton = {
////////                TextButton(
////////                    onClick = {
////////                        viewModel.deleteListing(deleteId)
////////                        pendingDeleteId = null
////////                    }
////////                ) {
////////                    Text("Delete", color = Color.Red)
////////                }
////////            },
////////            dismissButton = {
////////                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
////////            }
////////        )
////////    }
////////}
//////
//////package com.sharebite.app.ui.merchant
//////
//////import androidx.compose.foundation.background
//////import androidx.compose.foundation.layout.*
//////import androidx.compose.foundation.lazy.LazyColumn
//////import androidx.compose.foundation.lazy.items
//////import androidx.compose.foundation.shape.RoundedCornerShape
//////import androidx.compose.material.icons.Icons
//////import androidx.compose.material.icons.filled.Delete
//////import androidx.compose.material.icons.filled.Edit
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
//////    onListingClick: (Long) -> Unit,
//////    onEditClick: (Long) -> Unit // ✅ Added
//////) {
//////    val listings by viewModel.listings.collectAsState()
//////    var searchQuery by remember { mutableStateOf("") }
//////    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
//////    var pendingDeleteName by remember { mutableStateOf("") }
//////
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
//////        OutlinedTextField(
//////            value = searchQuery,
//////            onValueChange = { searchQuery = it },
//////            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
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
//////                        Column(modifier = Modifier.padding(16.dp)) {
//////                            Row(verticalAlignment = Alignment.CenterVertically) {
//////                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp)))
//////                                Spacer(modifier = Modifier.width(16.dp))
//////                                Column(modifier = Modifier.weight(1f)) {
//////                                    Text(listing.foodName, fontWeight = FontWeight.SemiBold)
//////                                    Text("${listing.quantity} left", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//////                                    Text(listing.category, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
//////                                }
//////                            }
//////
//////                            Spacer(modifier = Modifier.height(12.dp))
//////
//////                            Row(
//////                                modifier = Modifier.fillMaxWidth(),
//////                                horizontalArrangement = Arrangement.spacedBy(8.dp)
//////                            ) {
//////                                OutlinedButton(
//////                                    onClick = { onListingClick(listing.id) },
//////                                    modifier = Modifier.weight(1f),
//////                                    contentPadding = PaddingValues(0.dp)
//////                                ) {
//////                                    Text("View")
//////                                }
//////                                OutlinedButton(
//////                                    onClick = { onEditClick(listing.id) }, // ✅ EDIT
//////                                    modifier = Modifier.weight(1f),
//////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent),
//////                                    contentPadding = PaddingValues(0.dp)
//////                                ) {
//////                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
//////                                    Spacer(modifier = Modifier.width(4.dp))
//////                                    Text("Edit")
//////                                }
//////                                OutlinedButton(
//////                                    onClick = {
//////                                        pendingDeleteId = listing.id
//////                                        pendingDeleteName = listing.foodName
//////                                    },
//////                                    modifier = Modifier.weight(1f),
//////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
//////                                    contentPadding = PaddingValues(0.dp)
//////                                ) {
//////                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
//////                                    Spacer(modifier = Modifier.width(4.dp))
//////                                    Text("Delete")
//////                                }
//////                            }
//////                        }
//////                    }
//////                }
//////            }
//////        }
//////    }
//////
//////    // Delete confirmation
//////    val deleteId = pendingDeleteId
//////    if (deleteId != null) {
//////        AlertDialog(
//////            onDismissRequest = { pendingDeleteId = null },
//////            title = { Text("Delete donation?") },
//////            text = { Text("\"$pendingDeleteName\" will be permanently removed.") },
//////            confirmButton = {
//////                TextButton(
//////                    onClick = {
//////                        viewModel.deleteListing(deleteId)
//////                        pendingDeleteId = null
//////                    }
//////                ) {
//////                    Text("Delete", color = Color.Red)
//////                }
//////            },
//////            dismissButton = {
//////                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
//////            }
//////        )
//////    }
//////}
////
//////package com.sharebite.app.ui.merchant
//////
//////import androidx.compose.foundation.background
//////import androidx.compose.foundation.layout.*
//////import androidx.compose.foundation.lazy.LazyColumn
//////import androidx.compose.foundation.lazy.items
//////import androidx.compose.foundation.shape.RoundedCornerShape
//////import androidx.compose.material.icons.Icons
//////import androidx.compose.material.icons.filled.Delete
//////import androidx.compose.material.icons.filled.Edit
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
//////    onListingClick: (Long) -> Unit,
//////    onEditClick: (Long) -> Unit  // ✅ Added onEditClick parameter
//////) {
//////    val listings by viewModel.listings.collectAsState()
//////    var searchQuery by remember { mutableStateOf("") }
//////    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
//////    var pendingDeleteName by remember { mutableStateOf("") }
//////
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
//////                        Column(modifier = Modifier.padding(16.dp)) {
//////                            Row(verticalAlignment = Alignment.CenterVertically) {
//////                                Box(
//////                                    modifier = Modifier
//////                                        .size(50.dp)
//////                                        .background(Color(0xFFEEE5DB), RoundedCornerShape(8.dp))
//////                                )
//////                                Spacer(modifier = Modifier.width(16.dp))
//////                                Column(modifier = Modifier.weight(1f)) {
//////                                    Text(listing.foodName, fontWeight = FontWeight.SemiBold)
//////                                    Text(
//////                                        "${listing.quantity} left",
//////                                        style = MaterialTheme.typography.bodySmall,
//////                                        color = Color.Gray
//////                                    )
//////                                    Text(
//////                                        listing.category,
//////                                        style = MaterialTheme.typography.labelSmall,
//////                                        color = OrangeAccent
//////                                    )
//////                                }
//////                            }
//////
//////                            Spacer(modifier = Modifier.height(12.dp))
//////
//////                            Row(
//////                                modifier = Modifier.fillMaxWidth(),
//////                                horizontalArrangement = Arrangement.spacedBy(8.dp)
//////                            ) {
//////                                OutlinedButton(
//////                                    onClick = { onListingClick(listing.id) },
//////                                    modifier = Modifier.weight(1f),
//////                                    contentPadding = PaddingValues(0.dp)
//////                                ) {
//////                                    Text("View")
//////                                }
//////                                OutlinedButton(
//////                                    onClick = { onEditClick(listing.id) },
//////                                    modifier = Modifier.weight(1f),
//////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent),
//////                                    contentPadding = PaddingValues(0.dp)
//////                                ) {
//////                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
//////                                    Spacer(modifier = Modifier.width(4.dp))
//////                                    Text("Edit")
//////                                }
//////                                OutlinedButton(
//////                                    onClick = {
//////                                        pendingDeleteId = listing.id
//////                                        pendingDeleteName = listing.foodName
//////                                    },
//////                                    modifier = Modifier.weight(1f),
//////                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
//////                                    contentPadding = PaddingValues(0.dp)
//////                                ) {
//////                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
//////                                    Spacer(modifier = Modifier.width(4.dp))
//////                                    Text("Delete")
//////                                }
//////                            }
//////                        }
//////                    }
//////                }
//////            }
//////        }
//////    }
//////
//////    // Delete confirmation dialog
//////    val deleteId = pendingDeleteId
//////    if (deleteId != null) {
//////        AlertDialog(
//////            onDismissRequest = { pendingDeleteId = null },
//////            title = { Text("Delete donation?") },
//////            text = { Text("\"$pendingDeleteName\" will be permanently removed.") },
//////            confirmButton = {
//////                TextButton(
//////                    onClick = {
//////                        viewModel.deleteListing(deleteId)
//////                        pendingDeleteId = null
//////                    }
//////                ) {
//////                    Text("Delete", color = Color.Red)
//////                }
//////            },
//////            dismissButton = {
//////                TextButton(onClick = { pendingDeleteId = null }) {
//////                    Text("Cancel")
//////                }
//////            }
//////        )
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
////import androidx.compose.material.icons.Icons
////import androidx.compose.material.icons.filled.Delete
////import androidx.compose.material.icons.filled.Edit
////import androidx.compose.material.icons.filled.Fastfood
////import androidx.compose.material.icons.filled.Search
////import androidx.compose.material3.*
////import androidx.compose.runtime.*
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.draw.clip
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.layout.ContentScale
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.text.style.TextOverflow
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import coil.compose.AsyncImage
////import com.sharebite.app.ui.theme.BgCream
////import com.sharebite.app.ui.theme.OrangeAccent
////import java.text.SimpleDateFormat
////import java.util.*
////import com.sharebite.app.data.model.FoodListing
////
////@Composable
////fun MerchantAllListingsScreen(
////    paddingValues: PaddingValues,
////    viewModel: MerchantViewModel,
////    onListingClick: (Long) -> Unit,
////    onEditClick: (Long) -> Unit
////) {
////    val listings by viewModel.listings.collectAsState()
////    var searchQuery by remember { mutableStateOf("") }
////    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
////    var pendingDeleteName by remember { mutableStateOf("") }
////
////    // Filter by search query
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
////        // ── Header ──
////        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
////            Text(
////                "My Listings",
////                style = MaterialTheme.typography.headlineSmall,
////                fontWeight = FontWeight.Bold,
////                color = Color(0xFF1E1B16)
////            )
////            Text(
////                "Manage your community food contributions.",
////                style = MaterialTheme.typography.bodyMedium,
////                color = Color.Gray
////            )
////        }
////
////        // ── Search Bar ──
////        OutlinedTextField(
////            value = searchQuery,
////            onValueChange = { searchQuery = it },
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(horizontal = 16.dp, vertical = 4.dp),
////            placeholder = { Text("Search food name or category...") },
////            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
////            singleLine = true,
////            shape = RoundedCornerShape(50),
////            colors = OutlinedTextFieldDefaults.colors(
////                focusedBorderColor = OrangeAccent,
////                unfocusedBorderColor = Color.LightGray,
////                focusedContainerColor = Color.White,
////                unfocusedContainerColor = Color.White
////            )
////        )
////
////        if (filteredListings.isEmpty()) {
////            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
////                Text("No listings found.", color = Color.Gray)
////            }
////        } else {
////            // ── Section Header with count ──
////            Text(
////                "YOUR ACTIVE DONATIONS (${filteredListings.size})",
////                style = MaterialTheme.typography.titleMedium,
////                fontWeight = FontWeight.Bold,
////                color = OrangeAccent,
////                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
////            )
////
////            LazyColumn(
////                modifier = Modifier.fillMaxSize(),
////                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
////                verticalArrangement = Arrangement.spacedBy(12.dp)
////            ) {
////                items(filteredListings) { listing ->
////                    ListingCard(
////                        listing = listing,
////                        onListingClick = { onListingClick(listing.id) },
////                        onEditClick = { onEditClick(listing.id) },
////                        onToggleStatus = {
////                            // TODO: Add this function to ViewModel:
////                             viewModel.updateListingStatus(listing.id,
////                                 if (listing.status == "available") "inactive" else "available"
////                             )
////                            // For now, just a placeholder.
////                        },
////                        onDeleteClick = {
////                            pendingDeleteId = listing.id
////                            pendingDeleteName = listing.foodName
////                        }
////                    )
////                }
////            }
////        }
////    }
////
////    // ── Delete Confirmation Dialog ──
////    val deleteId = pendingDeleteId
////    if (deleteId != null) {
////        AlertDialog(
////            onDismissRequest = { pendingDeleteId = null },
////            title = { Text("Remove listing?") },
////            text = { Text("\"$pendingDeleteName\" will be permanently removed.") },
////            confirmButton = {
////                TextButton(
////                    onClick = {
////                        viewModel.deleteListing(deleteId)
////                        pendingDeleteId = null
////                    }
////                ) {
////                    Text("Remove", color = Color.Red)
////                }
////            },
////            dismissButton = {
////                TextButton(onClick = { pendingDeleteId = null }) {
////                    Text("Cancel")
////                }
////            }
////        )
////    }
////}
////
////// ─── Listing Card ──────────────────────────────────────────────────
////
////@Composable
////fun ListingCard(
////    listing: FoodListing,
////    onListingClick: () -> Unit,
////    onEditClick: () -> Unit,
////    onToggleStatus: () -> Unit,
////    onDeleteClick: () -> Unit
////) {
////    val isActive = listing.status == "available"
////    val timeString = formatTime(listing.createdAt)
////
////    Card(
////        modifier = Modifier
////            .fillMaxWidth()
////            .clickable { onListingClick() },
////        shape = RoundedCornerShape(12.dp),
////        colors = CardDefaults.cardColors(containerColor = Color.White),
////        elevation = CardDefaults.cardElevation(2.dp)
////    ) {
////        Column(modifier = Modifier.padding(16.dp)) {
////            // ── Top row: image, name, quantity ──
////            Row(
////                verticalAlignment = Alignment.CenterVertically,
////                horizontalArrangement = Arrangement.spacedBy(12.dp)
////            ) {
////                // Photo placeholder or image
////                Box(
////                    modifier = Modifier
////                        .size(60.dp)
////                        .clip(RoundedCornerShape(8.dp))
////                        .background(Color(0xFFF5F0E8)),
////                    contentAlignment = Alignment.Center
////                ) {
////                    if (listing.photoUrl != null) {
////                        AsyncImage(
////                            model = listing.photoUrl,
////                            contentDescription = null,
////                            modifier = Modifier.fillMaxSize(),
////                            contentScale = ContentScale.Crop
////                        )
////                    } else {
////                        Icon(
////                            Icons.Default.Fastfood,
////                            contentDescription = null,
////                            tint = OrangeAccent,
////                            modifier = Modifier.size(30.dp)
////                        )
////                    }
////                }
////
////                Column(modifier = Modifier.weight(1f)) {
////                    Text(
////                        listing.foodName,
////                        fontWeight = FontWeight.SemiBold,
////                        fontSize = 16.sp
////                    )
////                    Text(
////                        "${listing.quantity} units left",
////                        style = MaterialTheme.typography.bodySmall,
////                        color = Color.Gray
////                    )
////                }
////
////                // Timestamp (time only)
////                Text(
////                    timeString,
////                    style = MaterialTheme.typography.labelSmall,
////                    color = Color.Gray,
////                    fontSize = 12.sp
////                )
////            }
////
////            // ── Description (truncated) ──
////            if (!listing.description.isNullOrBlank()) {
////                Spacer(modifier = Modifier.height(4.dp))
////                Text(
////                    listing.description,
////                    style = MaterialTheme.typography.bodySmall,
////                    color = Color.Gray,
////                    maxLines = 2,
////                    overflow = TextOverflow.Ellipsis
////                )
////            }
////
////            Spacer(modifier = Modifier.height(12.dp))
////
////            // ── Action buttons ──
////            Row(
////                modifier = Modifier.fillMaxWidth(),
////                horizontalArrangement = Arrangement.spacedBy(8.dp)
////            ) {
////                // Edit button
////                OutlinedButton(
////                    onClick = onEditClick,
////                    modifier = Modifier.weight(1f),
////                    shape = RoundedCornerShape(50),
////                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
////                ) {
////                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
////                    Spacer(modifier = Modifier.width(4.dp))
////                    Text("Edit")
////                }
////
////                // Toggle button: Deactivate / Relist
////                OutlinedButton(
////                    onClick = onToggleStatus,
////                    modifier = Modifier.weight(1f),
////                    shape = RoundedCornerShape(50),
////                    colors = ButtonDefaults.outlinedButtonColors(
////                        contentColor = if (isActive) Color.Red else Color(0xFF2E7D32)
////                    )
////                ) {
////                    Text(if (isActive) "Deactivate" else "Relist")
////                }
////
////                // Remove / Delete as icon button
////                IconButton(
////                    onClick = onDeleteClick,
////                    modifier = Modifier.size(40.dp)
////                ) {
////                    Icon(
////                        Icons.Default.Delete,
////                        contentDescription = "Remove",
////                        tint = Color.Red
////                    )
////                }
////            }
////        }
////    }
////}
////
////// ─── Helper: format timestamp to "HH:mm:ss" ──────────────────────
////
////fun formatTime(timestamp: String): String {
////    return try {
////        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
////        val date = format.parse(timestamp)
////        if (date == null) return ""
////        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
////    } catch (e: Exception) {
////        // Fallback: try to parse with simpler format
////        try {
////            val simpleFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
////            val date = simpleFormat.parse(timestamp)
////            if (date == null) return ""
////            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
////        } catch (e2: Exception) {
////            ""
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
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Fastfood
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
//import com.sharebite.app.data.model.FoodListing
//import com.sharebite.app.ui.theme.BgCream
//import com.sharebite.app.ui.theme.OrangeAccent
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun MerchantAllListingsScreen(
//    paddingValues: PaddingValues,
//    viewModel: MerchantViewModel,
//    onListingClick: (Long) -> Unit,
//    onEditClick: (Long) -> Unit,
//    onAddDonationClick: () -> Unit   // ← NEW parameter
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
//    LaunchedEffect(Unit) {
//        viewModel.loadMerchantData()
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(paddingValues)
//            .background(BgCream)
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // ── Header ──
//            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
//                Text(
//                    "My Listings",
//                    style = MaterialTheme.typography.headlineSmall,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF1E1B16)
//                )
//                Text(
//                    "Manage your community food contributions.",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Color.Gray
//                )
//            }
//
//            // ── Search Bar ──
//            OutlinedTextField(
//                value = searchQuery,
//                onValueChange = { searchQuery = it },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 4.dp),
//                placeholder = { Text("Search food name or category...") },
//                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                singleLine = true,
//                shape = RoundedCornerShape(50),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = OrangeAccent,
//                    unfocusedBorderColor = Color.LightGray,
//                    focusedContainerColor = Color.White,
//                    unfocusedContainerColor = Color.White
//                )
//            )
//
//            if (filteredListings.isEmpty()) {
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text("No listings found.", color = Color.Gray)
//                }
//            } else {
//                // ── Section Header with count ──
//                Text(
//                    "YOUR ACTIVE DONATIONS (${filteredListings.size})",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = OrangeAccent,
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//                )
//
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    items(filteredListings) { listing ->
//                        ListingCard(
//                            listing = listing,
//                            onListingClick = { onListingClick(listing.id) },
//                            onEditClick = { onEditClick(listing.id) },
//                            onToggleStatus = {
//                                viewModel.updateListingStatus(
//                                    listing.id,
//                                    if (listing.status == "available") "inactive" else "available"
//                                )
//                            },
//                            onDeleteClick = {
//                                pendingDeleteId = listing.id
//                                pendingDeleteName = listing.foodName
//                            }
//                        )
//                    }
//                }
//            }
//        }
//
//        // ✅ Floating Action Button – Add Donation
//        FloatingActionButton(
//            onClick = onAddDonationClick,
//            containerColor = OrangeAccent,
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(24.dp)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = "Add Donation", tint = Color.White)
//        }
//    }
//
//    // ── Delete Confirmation Dialog ──
//    val deleteId = pendingDeleteId
//    if (deleteId != null) {
//        AlertDialog(
//            onDismissRequest = { pendingDeleteId = null },
//            title = { Text("Remove listing?") },
//            text = { Text("\"$pendingDeleteName\" will be permanently removed.") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        viewModel.deleteListing(deleteId)
//                        pendingDeleteId = null
//                    }
//                ) {
//                    Text("Remove", color = Color.Red)
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { pendingDeleteId = null }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }
//}
//
//// ─── Listing Card ──────────────────────────────────────────────────
//
//@Composable
//fun ListingCard(
//    listing: FoodListing,
//    onListingClick: () -> Unit,
//    onEditClick: () -> Unit,
//    onToggleStatus: () -> Unit,
//    onDeleteClick: () -> Unit
//) {
//    val isActive = listing.status == "available"
//    val timeString = formatTime(listing.createdAt)
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onListingClick() },
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(2.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(60.dp)
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color(0xFFF5F0E8)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (listing.photoUrl != null) {
//                        AsyncImage(
//                            model = listing.photoUrl,
//                            contentDescription = null,
//                            modifier = Modifier.fillMaxSize(),
//                            contentScale = ContentScale.Crop
//                        )
//                    } else {
//                        Icon(
//                            Icons.Default.Fastfood,
//                            contentDescription = null,
//                            tint = OrangeAccent,
//                            modifier = Modifier.size(30.dp)
//                        )
//                    }
//                }
//
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        listing.foodName,
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = 16.sp
//                    )
//                    Text(
////                        "${listing.remainingQuantity} units left"
//                        "${listing.remainingQuantity} ${listing.portionInfo ?: "units"} left",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color.Gray
//                    )
//                }
//
//                Text(
//                    timeString,
//                    style = MaterialTheme.typography.labelSmall,
//                    color = Color.Gray,
//                    fontSize = 12.sp
//                )
//            }
//
//            if (!listing.description.isNullOrBlank()) {
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    listing.description,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Gray,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                OutlinedButton(
//                    onClick = onEditClick,
//                    modifier = Modifier.weight(1f),
//                    shape = RoundedCornerShape(50),
//                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
//                ) {
//                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text("Edit")
//                }
//
//                OutlinedButton(
//                    onClick = onToggleStatus,
//                    modifier = Modifier.weight(1f),
//                    shape = RoundedCornerShape(50),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = if (isActive) Color.Red else Color(0xFF2E7D32)
//                    )
//                ) {
//                    Text(if (isActive) "Deactivate" else "Relist")
//                }
//
//                IconButton(
//                    onClick = onDeleteClick,
//                    modifier = Modifier.size(40.dp)
//                ) {
//                    Icon(
//                        Icons.Default.Delete,
//                        contentDescription = "Remove",
//                        tint = Color.Red
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ─── Helper: format timestamp to "HH:mm:ss" ──────────────────────
//
//fun formatTime(timestamp: String): String {
//    return try {
//        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
//        val date = format.parse(timestamp)
//        if (date == null) return ""
//        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
//    } catch (e: Exception) {
//        try {
//            val simpleFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
//            val date = simpleFormat.parse(timestamp)
//            if (date == null) return ""
//            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
//        } catch (e2: Exception) {
//            ""
//        }
//    }
//}

package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MerchantAllListingsScreen(
    paddingValues: PaddingValues,
    viewModel: MerchantViewModel,
    onListingClick: (Long) -> Unit,
    onAddDonationClick: () -> Unit
) {
    val listings by viewModel.listings.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredListings = listings.filter {
        it.foodName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        viewModel.loadMerchantData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header ──
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    "My Listings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B16)
                )
                Text(
                    "Manage your community food contributions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // ── Search Bar ──
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Search food name or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (filteredListings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No listings found.", color = Color.Gray)
                        Text(
                            "Tap the + button to add your first donation.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // ── Section Header with count ──
                Text(
                    "YOUR ACTIVE DONATIONS (${filteredListings.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredListings) { listing ->
                        ListingCardSimplified(
                            listing = listing,
                            onClick = { onListingClick(listing.id) }
                        )
                    }
                }
            }
        }

        // ✅ Floating Action Button – Add Donation
        FloatingActionButton(
            onClick = onAddDonationClick,
            containerColor = OrangeAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Donation", tint = Color.White)
        }
    }
}

// ─── Simplified Listing Card ──────────────────────────────────────

@Composable
fun ListingCardSimplified(
    listing: FoodListing,
    onClick: () -> Unit
) {
    val isActive = listing.status == "available"
    val timeString = formatTime(listing.createdAt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Photo ──
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F0E8)),
                    contentAlignment = Alignment.Center
                ) {
                    if (listing.photoUrl != null) {
                        AsyncImage(
                            model = listing.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Fastfood,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // ── Content ──
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        listing.foodName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${listing.remainingQuantity} units left",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            listing.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeAccent
                        )
                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                if (isActive) "Active" else "Inactive",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFF2E7D32) else Color.Red
                            )
                        }
                    }
                }

                // ── Time ──
                Text(
                    timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // ── Description (truncated) ──
            if (!listing.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    listing.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── View Details button ──
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
            ) {
                Text("View Details →", fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─── Helper: format timestamp ──────────────────────────────────────

fun formatTime(timestamp: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val date = format.parse(timestamp)
        if (date == null) return ""
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        try {
            val simpleFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = simpleFormat.parse(timestamp)
            if (date == null) return ""
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
        } catch (e2: Exception) {
            ""
        }
    }
}