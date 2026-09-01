package com.sharebite.app.ui.user

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private data class FilterState(
    val categories: Set<String> = emptySet(),
    val distance: Float = 5.0f,
    val isVegetarian: Boolean = false,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val isNutFree: Boolean = false,
    val sortBy: String = "distance"
)

private val allCategories = listOf("Bakery", "Produce", "Dairy", "Meals", "Vegetables", "Fruits", "Canned Goods", "Prepared Meals", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBrowseScreen(
    paddingValues: PaddingValues,
    onFoodClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FoodListingRepository() }
    val configuration = LocalConfiguration.current  // ✅ 修复：使用 LocalConfiguration

    var allListings by remember { mutableStateOf<List<FoodListing>>(emptyList()) }
    var filteredListings by remember { mutableStateOf<List<FoodListing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isListView by remember { mutableStateOf(true) }

    var showFilterSheet by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(FilterState()) }

    val defaultLocation = LatLng(3.1390, 101.6869)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 11f)
    }

    // ✅ 删除未使用的 hasLocationPermission

    LaunchedEffect(Unit) {
        try {
            allListings = repository.getAvailableListings()
            filteredListings = allListings
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(searchQuery, selectedCategory, filterState, allListings) {
        filteredListings = allListings.filter { listing ->
            val matchesSearch = listing.foodName.contains(searchQuery, ignoreCase = true) ||
                    listing.merchantName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == null || listing.category == selectedCategory

            val matchesFilter = if (filterState.categories.isNotEmpty()) {
                listing.category in filterState.categories
            } else true

            matchesSearch && matchesCategory && matchesFilter
        }

        when (filterState.sortBy) {
            "distance" -> filteredListings = filteredListings.sortedBy { it.distanceKm }
            "expiry" -> filteredListings = filteredListings.sortedBy { it.deadline }
            else -> {}
        }
    }

    val categories = remember(allListings) {
        allListings.map { it.category }.distinct()
    }

    // ✅ 修复：使用 LocalConfiguration 获取当前 Locale
    val currentLocale = configuration.locales.get(0)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCream)
                .padding(paddingValues)
        ) {
            // ── Search Bar + Filter ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search for food near you...", color = TextGrey) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextGrey
                        )
                    },
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = Color(0xFFDDC0B8),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { showFilterSheet = true },
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = TextDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── List / Map Toggle ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(50),
                color = Color(0xFFE8E2D8)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { isListView = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListView) OrangeAccent else Color.Transparent,
                            contentColor = if (isListView) Color.White else TextGrey
                        ),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (isListView) 2.dp else 0.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "List View",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("List View", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = { isListView = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isListView) OrangeAccent else Color.Transparent,
                            contentColor = if (!isListView) Color.White else TextGrey
                        ),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (!isListView) 2.dp else 0.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map View",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Map View", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Category Chips ──
            if (isListView && categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { selectedCategory = null },
                            label = { Text("All", fontSize = 13.sp) },
                            selected = selectedCategory == null,
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = Color.White,
                                disabledContainerColor = Color(0xFFE8E2D8),
                                disabledLabelColor = TextGrey
                            )
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            label = { Text(category, fontSize = 13.sp) },
                            selected = selectedCategory == category,
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = Color.White,
                                disabledContainerColor = Color(0xFFE8E2D8),
                                disabledLabelColor = TextGrey
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Content ──
            when {
                isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = OrangeAccent)
                }

                errorMessage != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(text = "Error: $errorMessage", color = RedCancelled)
                }

                !isListView -> {
                    val locations = filteredListings.mapNotNull { listing ->
                        if (listing.latitude != null && listing.longitude != null &&
                            listing.latitude != 0.0 && listing.longitude != 0.0
                        ) listing else null
                    }

                    LaunchedEffect(filteredListings) {
                        if (locations.isNotEmpty()) {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(locations.first().latitude!!, locations.first().longitude!!),
                                    13f
                                ),
                                500
                            )
                        }
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = true
                        )
                    ) {
                        locations.forEach { listing ->
                            val location = LatLng(listing.latitude!!, listing.longitude!!)
                            Marker(
                                state = MarkerState(position = location),
                                title = listing.foodName,
                                snippet = "${listing.merchantName} · ${listing.distanceKm}km",
                                onClick = {
                                    onFoodClick(listing.id)
                                    true
                                }
                            )
                        }
                    }
                }

                filteredListings.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextGrey
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No food available", fontSize = 16.sp, color = TextGrey)
                        Text(text = "Check back later!", fontSize = 14.sp, color = TextGrey)
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredListings) { listing ->
                        BrowseFoodCard(
                            listing = listing,
                            onClick = { onFoodClick(listing.id) },
                            locale = currentLocale
                        )
                    }
                }
            }
        }

        // ── FAB 定位按钮 ──
        if (!isListView) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f),
                            1000
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(56.dp),
                containerColor = Color.White,
                contentColor = OrangeAccent,
                shape = RoundedCornerShape(50),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = OrangeAccent
                )
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filterState = filterState,
            onFilterChange = { filterState = it },
            onDismiss = { showFilterSheet = false }
        )
    }
}

// ── ✅ 滚动版 Filter Bottom Sheet ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = allCategories
    var localFilterState by remember { mutableStateOf(filterState) }
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFDDC0B8), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                TextButton(
                    onClick = { localFilterState = FilterState() }
                ) {
                    Text(
                        text = "Reset",
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── 可滚动内容 ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                // Food Categories
                Text(
                    text = "Food Categories",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = localFilterState.categories.contains(category)
                        FilterChip(
                            onClick = {
                                localFilterState = localFilterState.copy(
                                    categories = if (isSelected) {
                                        localFilterState.categories - category
                                    } else {
                                        localFilterState.categories + category
                                    }
                                )
                            },
                            label = { Text(category, fontSize = 13.sp) },
                            selected = isSelected,
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = Color.White,
                                disabledContainerColor = Color(0xFFE8E2D8),
                                disabledLabelColor = TextGrey
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Distance
                Text(
                    text = "Distance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0.5 km", fontSize = 12.sp, color = TextGrey)
                    Text(
                        text = "${localFilterState.distance} km",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent
                    )
                    Text(text = "10 km", fontSize = 12.sp, color = TextGrey)
                }
                Slider(
                    value = localFilterState.distance,
                    onValueChange = { localFilterState = localFilterState.copy(distance = it) },
                    valueRange = 0.5f..10f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = OrangeAccent,
                        activeTrackColor = OrangeAccent,
                        inactiveTrackColor = Color(0xFFDDC0B8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dietary Needs
                Text(
                    text = "Dietary Needs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val dietaryOptions = listOf(
                    "Vegetarian" to { localFilterState = localFilterState.copy(isVegetarian = !localFilterState.isVegetarian) },
                    "Vegan" to { localFilterState = localFilterState.copy(isVegan = !localFilterState.isVegan) },
                    "Gluten-Free" to { localFilterState = localFilterState.copy(isGlutenFree = !localFilterState.isGlutenFree) },
                    "Nut-Free" to { localFilterState = localFilterState.copy(isNutFree = !localFilterState.isNutFree) }
                )

                dietaryOptions.forEach { (label, onClick) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClick() }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, fontSize = 14.sp, color = TextDark)
                        Checkbox(
                            checked = when (label) {
                                "Vegetarian" -> localFilterState.isVegetarian
                                "Vegan" -> localFilterState.isVegan
                                "Gluten-Free" -> localFilterState.isGlutenFree
                                "Nut-Free" -> localFilterState.isNutFree
                                else -> false
                            },
                            onCheckedChange = { onClick() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = OrangeAccent,
                                uncheckedColor = Color(0xFFDDC0B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sort By
                Text(
                    text = "Sort By",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SortOption(
                    label = "Distance (Closest)",
                    description = "Show items nearest to your location first",
                    isSelected = localFilterState.sortBy == "distance",
                    onClick = { localFilterState = localFilterState.copy(sortBy = "distance") }
                )
                SortOption(
                    label = "Expiry (Soonest)",
                    description = "Prioritize items that need to be rescued quickly",
                    isSelected = localFilterState.sortBy == "expiry",
                    onClick = { localFilterState = localFilterState.copy(sortBy = "expiry") }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── 底部固定按钮 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        localFilterState = FilterState()
                        onFilterChange(localFilterState)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangeAccent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangeAccent)
                ) {
                    Text(text = "Clear All", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onFilterChange(localFilterState)
                        onDismiss()
                    },
                    modifier = Modifier.weight(2f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(text = "Apply Filters", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SortOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = OrangeAccent,
                unselectedColor = Color(0xFFDDC0B8)
            )
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) OrangeAccent else TextDark
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextGrey,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun BrowseFoodCard(
    listing: FoodListing,
    onClick: () -> Unit,
    locale: Locale
) {
    // 计算剩余天数（用于显示 "X days left"）
    val daysRemaining = remember(listing.deadline, locale) {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", locale)
            val deadline = format.parse(listing.deadline)
            val now = Date()
            if (deadline != null && deadline.after(now)) {
                val diff = deadline.time - now.time
                (diff / (1000 * 60 * 60 * 24)).toInt()
            } else 0
        } catch (_: Exception) {
            0
        }
    }

    // 计算剩余小时（用于显示 "Expires in Xh"）
    val hoursRemaining = remember(listing.deadline, locale) {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", locale)
            val deadline = format.parse(listing.deadline)
            val now = Date()
            if (deadline != null && deadline.after(now)) {
                val diff = deadline.time - now.time
                (diff / (1000 * 60 * 60)).toInt()
            } else 0
        } catch (_: Exception) {
            0
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBE9E7))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ── 图片区域（带叠加标签） ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            ) {
                // 图片
                if (!listing.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = listing.photoUrl,
                        contentDescription = listing.foodName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFEFE6D9)),
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

                // ── 左上角状态标签 ──
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // "AVAILABLE" 标签
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = GreenConfirmed,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "AVAILABLE",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // 分类标签（如 "BAKERY"）
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFF8A65).copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = listing.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // ── 右上角收藏按钮 ──
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.7f),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── 内容区域 ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题行：食物名称 + 距离 + 到期时间（右上角）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listing.foodName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TextGrey
                            )
                            Text(
                                text = listing.merchantName,
                                fontSize = 13.sp,
                                color = TextGrey
                            )
                        }
                    }

                    // 右侧：距离 + 到期时间（右上角）
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${listing.distanceKm} km away",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = OrangeAccent
                        )

                        // ── ✅ 智能倒计时：超过 1 天显示天数，否则显示小时 ──
                        when {
                            daysRemaining >= 1 -> {
                                Text(
                                    text = "$daysRemaining day${if (daysRemaining > 1) "s" else ""} left",
                                    fontSize = 11.sp,
                                    color = if (daysRemaining <= 2) OrangeAccent else TextGrey
                                )
                            }

                            hoursRemaining > 0 -> {
                                Text(
                                    text = "Expires in ${hoursRemaining}h",
                                    fontSize = 11.sp,
                                    color = if (hoursRemaining <= 3) RedCancelled else TextGrey
                                )
                            }

                            else -> {
                                Text(
                                    text = "Expired",
                                    fontSize = 11.sp,
                                    color = RedCancelled
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Request 按钮 ──
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Request",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}