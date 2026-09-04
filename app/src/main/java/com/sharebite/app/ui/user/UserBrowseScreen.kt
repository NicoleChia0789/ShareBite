package com.sharebite.app.ui.user

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.ui.theme.*
import com.sharebite.app.util.LocationUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ✅ FilterState：分类、膳食类型、排序
@Stable
private data class FilterState(
    val categories: Set<String> = emptySet(),
    val dietaryTypes: Set<String> = emptySet(),
    val sortBy: String = "distance"
)

// ✅ FilterState 的 Saver（用于 rememberSaveable）
private val FilterStateSaver = Saver<FilterState, Map<String, Any>>(
    save = {
        mapOf(
            "categories" to it.categories.toList(),
            "dietaryTypes" to it.dietaryTypes.toList(),
            "sortBy" to it.sortBy
        )
    },
    restore = {
        FilterState(
            categories = (it["categories"] as? List<String>)?.toSet() ?: emptySet(),
            dietaryTypes = (it["dietaryTypes"] as? List<String>)?.toSet() ?: emptySet(),
            sortBy = it["sortBy"] as? String ?: "distance"
        )
    }
)

private val allDietaryTypes = listOf("Vegetarian", "Vegan", "Gluten-Free", "Nut-Free", "Regular")
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
    val configuration = LocalConfiguration.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // ── 位置相关 ──
    val locationUtils = remember { LocationUtils(context as android.app.Activity) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLocationLoading by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isGettingLocation by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(locationUtils.hasLocationPermission())
    }
    var awaitingGpsResolution by rememberSaveable { mutableStateOf(false) }

    fun fetchLocation() {
        if (isGettingLocation) return
        isGettingLocation = true
        scope.launch {
            try {
                isLocationLoading = true
                locationError = null

                val settingsOk = locationUtils.checkLocationSettings()
                if (!settingsOk) {
                    // LocationUtils launches Android's native GPS settings prompt.
                    // Retry after the user returns from that prompt.
                    awaitingGpsResolution = true
                    locationError = null
                    isLocationLoading = false
                    isGettingLocation = false
                    return@launch
                }

                userLocation = locationUtils.getCurrentLocation()
                if (userLocation == null) {
                    userLocation = locationUtils.getLastKnownLocation()
                }
                if (userLocation == null) {
                    locationError = "Cannot get your location. Please try again."
                } else {
                    locationError = null
                }
            } catch (e: Exception) {
                locationError = "Location error: ${e.message}"
            } finally {
                isLocationLoading = false
                isGettingLocation = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted  // ✅ 更新权限状态
        if (isGranted) {
            fetchLocation()
        } else {
            locationError = null
        }
    }

    fun requestLocation() {
        if (locationUtils.hasLocationPermission()) {
            fetchLocation()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && awaitingGpsResolution) {
                awaitingGpsResolution = false
                scope.launch {
                    delay(500)
                    fetchLocation()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── 列表状态（✅ 全部使用 rememberSaveable） ──
    var allListings by remember { mutableStateOf<List<FoodListing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var isListView by rememberSaveable { mutableStateOf(true) }
    var showFilterScreen by remember { mutableStateOf(false) }
    var filterState by rememberSaveable(stateSaver = FilterStateSaver) {
        mutableStateOf(FilterState())
    }
    var isDataLoaded by rememberSaveable { mutableStateOf(false) }

    val defaultLocation = LatLng(3.1390, 101.6869)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 11f)
    }

    val categories by remember {
        derivedStateOf {
            allListings.map { it.category }.distinct()
        }
    }

    // ✅ 屏幕旋转时关闭键盘
    LaunchedEffect(configuration.orientation) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    // ── 1️⃣ 数据加载（只执行一次） ──
    LaunchedEffect(Unit) {
        if (!isDataLoaded || allListings.isEmpty()) {
            isDataLoaded = true
            try {
                allListings = repository.getAvailableListings()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // ── 2️⃣ 权限和位置（只执行一次） ──
    LaunchedEffect(Unit) {
        if (locationUtils.hasLocationPermission()) {
            fetchLocation()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // ── 3️⃣ 过滤逻辑（✅ 使用 remember 缓存，避免重复计算） ──
    val filteredListings = remember(
        searchQuery, selectedCategory, filterState, allListings, userLocation
    ) {
        val baseListings = allListings
        val currentLocation = userLocation

        // 1. 过滤过期食物
        val validListings = baseListings.filter { listing ->
            !isListingExpired(listing.deadline)
        }

        // 2. 搜索 + 分类
        val filteredBySearchAndCategory = validListings.filter { listing ->
            val matchesSearch = listing.foodName.contains(searchQuery, ignoreCase = true) ||
                    listing.merchantName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || listing.category == selectedCategory
            val matchesFilter = filterState.categories.isEmpty() || listing.category in filterState.categories
            matchesSearch && matchesCategory && matchesFilter
        }

        // 3. 膳食类型
        val filteredByDietary = if (filterState.dietaryTypes.isNotEmpty()) {
            filteredBySearchAndCategory.filter { listing ->
                val dietaryType = listing.dietaryType ?: "Regular"
                filterState.dietaryTypes.contains(dietaryType)
            }
        } else {
            filteredBySearchAndCategory
        }

        // 4. 动态计算距离
        val listingsWithDistance = filteredByDietary.map { listing ->
            val distance = if (currentLocation != null && listing.latitude != null && listing.longitude != null) {
                locationUtils.calculateDistance(
                    currentLocation.latitude.toDouble(),
                    currentLocation.longitude.toDouble(),
                    listing.latitude.toDouble(),
                    listing.longitude.toDouble()
                )
            } else {
                listing.distanceKm
            }
            listing.copy(distanceKm = distance)
        }

        // 5. 排序
        when (filterState.sortBy) {
            "distance" -> listingsWithDistance.sortedBy { it.distanceKm }
            "expiry" -> listingsWithDistance.sortedBy { it.deadline }
            else -> listingsWithDistance
        }
    }

    val currentLocale = configuration.locales.get(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCream)
                .padding(paddingValues)
        ) {
            if (configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                Column(
                    modifier = Modifier.width(285.dp).fillMaxHeight().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Find available food near you.", fontSize = 13.sp, color = TextGrey)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search for food near you...", color = TextGrey, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGrey) },
                        shape = RoundedCornerShape(21.dp),
                        singleLine = true,
                        colors = AppTextFieldDefaults.colors()
                    )
                    Button(
                        onClick = { showFilterScreen = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(21.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Filters")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = isListView, onClick = { isListView = true }, label = { Text("List") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = !isListView, onClick = { isListView = false }, label = { Text("Map") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedButton(
                        onClick = { requestLocation() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(21.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Use GPS to calculate distance")
                    }
                    Text(
                        text = if (userLocation == null) "Current location: Not detected" else "Current location: Detected",
                        fontSize = 12.sp,
                        color = if (userLocation == null) TextGrey else GreenConfirmed
                    )
                    if (categories.isNotEmpty()) {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextGrey)
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = { Text("All") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeAccent, selectedLabelColor = Color.White)
                                )
                            }
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = if (selectedCategory == category) null else category },
                                    label = { Text(category) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeAccent, selectedLabelColor = Color.White)
                                )
                            }
                        }
                    }
                }
            }
        Column(
            modifier = if (configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) Modifier.weight(1f).fillMaxHeight() else Modifier.fillMaxWidth()
        ) {
        if (configuration.orientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            // ── 位置状态 ──
            if (isLocationLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = OrangeAccent)
                    Text("Getting your location...", fontSize = 12.sp, color = TextGrey)
                }
            }
            Text(
                text = if (userLocation == null) "Current location: Not detected" else "Current location: Detected",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = if (userLocation == null) TextGrey else GreenConfirmed
            )
            TextButton(
                onClick = { requestLocation() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Use GPS to calculate distance")
            }

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
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            // 可以在这里处理焦点变化
                        },
                    placeholder = { Text("Search for food near you...", color = TextGrey) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextGrey
                        )
                    },
                    shape = RoundedCornerShape(50),
                    colors = AppTextFieldDefaults.colors() ,
                    singleLine = true
                )

                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { showFilterScreen = true },
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

                        LaunchedEffect(filteredListings, userLocation) {
                            try {
                            val mapPoints = buildList {
                                userLocation?.let { add(LatLng(it.latitude, it.longitude)) }
                                locations.forEach { add(LatLng(it.latitude!!, it.longitude!!)) }
                            }
                            when {
                                mapPoints.size > 1 -> {
                                    val bounds = LatLngBounds.builder().apply {
                                        mapPoints.forEach { include(it) }
                                    }.build()
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngBounds(bounds, 96),
                                        500
                                    )
                                }
                                userLocation != null -> {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(userLocation!!.latitude, userLocation!!.longitude),
                                            14f
                                        ),
                                        500
                                    )
                                }
                                locations.isNotEmpty() -> {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(locations.first().latitude!!, locations.first().longitude!!),
                                            13f
                                        ),
                                        500
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // 如果动画失败，使用默认位置
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(defaultLocation, 11f),
                                500
                            )
                        }
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = false
                        ),
                        properties = MapProperties(
                            isMyLocationEnabled = hasLocationPermission  // ✅ 根据权限状态动态设置
                        )
                    ) {
                        locations.forEach { listing ->
                            val location = LatLng(listing.latitude!!, listing.longitude!!)
                            Marker(
                                state = MarkerState(position = location),
                                title = listing.foodName,
                                snippet = "${listing.merchantName} · ${formatDistance(listing.distanceKm)}",
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
                    items(
                        items = filteredListings,
                        key = { it.id }
                    ) { listing ->
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
                    requestLocation()
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(56.dp),
                containerColor = Color.White,
                contentColor = OrangeAccent,
                shape = RoundedCornerShape(50),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = if (userLocation != null) Icons.Default.MyLocation else Icons.Default.GpsNotFixed,
                    contentDescription = "My Location",
                    tint = OrangeAccent
                )
            }
        }
        }
    }

    // ── Filter Bottom Sheet ──
    if (showFilterScreen) {
        FilterBottomSheet(
            filterState = filterState,
            onFilterChange = { filterState = it },
            onDismiss = { showFilterScreen = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var localFilterState by remember { mutableStateOf(filterState) }

    LaunchedEffect(filterState) {
        if (localFilterState != filterState) {
            localFilterState = filterState
        }
    }

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
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── 头部标题 + 关闭按钮 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Row {
                    TextButton(
                        onClick = {
                            localFilterState = FilterState()
                            onFilterChange(localFilterState)
                            onDismiss()
                        }
                    ) {
                        Text("Reset", color = OrangeAccent, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Food Categories ──
            Text(
                text = "Food Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allCategories.forEach { category ->
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
                            disabledContainerColor = Color(0xFFF0EDE6),
                            disabledLabelColor = TextDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Dietary Types ──
            Text(
                text = "Dietary Preferences",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allDietaryTypes.forEach { dietary ->
                    val isSelected = localFilterState.dietaryTypes.contains(dietary)
                    FilterChip(
                        onClick = {
                            localFilterState = localFilterState.copy(
                                dietaryTypes = if (isSelected) {
                                    localFilterState.dietaryTypes - dietary
                                } else {
                                    localFilterState.dietaryTypes + dietary
                                }
                            )
                        },
                        label = { Text(dietary, fontSize = 13.sp) },
                        selected = isSelected,
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeAccent,
                            selectedLabelColor = Color.White,
                            disabledContainerColor = Color(0xFFF0EDE6),
                            disabledLabelColor = TextDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Sort By ──
            Text(
                text = "Sort By",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
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

            Button(
                onClick = {
                    onFilterChange(localFilterState)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Apply Filters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                color = TextDark.copy(alpha = 0.7f),
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
    val parsedDate = remember(listing.deadline) { tryParseDeadline(listing.deadline) }

    val daysRemaining = remember(listing.deadline, parsedDate) {
        if (parsedDate != null) {
            val now = Date()
            if (parsedDate.after(now)) {
                val diff = parsedDate.time - now.time
                (diff / (1000 * 60 * 60 * 24)).toInt()
            } else 0
        } else {
            999
        }
    }

    val hoursRemaining = remember(listing.deadline, parsedDate) {
        if (parsedDate != null) {
            val now = Date()
            if (parsedDate.after(now)) {
                val diff = parsedDate.time - now.time
                ((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)).toInt()
            } else 0
        } else {
            999
        }
    }

    val isExpired = daysRemaining <= 0 && hoursRemaining <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isExpired) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired) Color(0xFFE8E2D8) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpired) Color(0xFFD0C8BC) else Color(0xFFE8DED5)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            ) {
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
                            .background(if (isExpired) Color(0xFFD8D0C4) else Color(0xFFEFE6D9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = "Food",
                            modifier = Modifier.size(48.dp),
                            tint = if (isExpired) Color(0xFFB0A898) else Color(0xFFD8C4A8)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isExpired) TextGrey else GreenConfirmed,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = if (isExpired) "EXPIRED" else "AVAILABLE",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isExpired) TextGrey.copy(alpha = 0.6f) else Color(0xFFFF8A65).copy(alpha = 0.9f),
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
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
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
                            color = if (isExpired) TextGrey else TextDark
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

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatDistance(listing.distanceKm),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isExpired) TextGrey else OrangeAccent
                        )

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

                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isExpired) TextGrey else OrangeAccent
                    ),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    enabled = !isExpired
                ) {
                    Text(
                        text = if (isExpired) "Expired" else "Request",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ── 统一的日期解析函数 ──
private fun tryParseDeadline(deadline: String): Date? {
    if (deadline.isBlank()) return null

    if (deadline.contains("today", ignoreCase = true)) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.time
    }

    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    )

    formats.forEach { format ->
        try {
            return format.parse(deadline)
        } catch (_: Exception) { }
    }
    return null
}

// ── 判断食物是否过期 ──
private fun isListingExpired(deadline: String): Boolean {
    val parsed = tryParseDeadline(deadline) ?: return false
    return parsed.before(Date())
}

// ── 辅助函数：格式化距离 ──
private fun formatDistance(distance: Double): String {
    return String.format(Locale.US, "%.1f km", distance)
}
