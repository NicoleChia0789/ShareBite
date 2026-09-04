package com.sharebite.app.ui.charity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.ui.theme.OrangeAccent
import com.sharebite.app.ui.theme.TextDark
import com.sharebite.app.ui.theme.TextGrey
import com.sharebite.app.ui.theme.RedCancelled
import com.sharebite.app.ui.theme.AppTextFieldDefaults
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.saveable.rememberSaveable

private val categoryTabs = listOf("All Items") + foodCategoryOptions

@Composable
fun BrowseDonationsScreen(paddingValues: PaddingValues, onFoodClick: (Long) -> Unit) {
    val repository = remember { FoodListingRepository() }

    var allListings by remember { mutableStateOf<List<FoodListing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var searchFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    val searchQuery = searchFieldValue.text
    var selectedCategory by rememberSaveable { mutableStateOf("All Items") }
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    // ✅ 添加 isDataLoaded 标志
    var isDataLoaded by rememberSaveable { mutableStateOf(false) }

    // ✅ 修改 LaunchedEffect
    LaunchedEffect(Unit) {
        // 如果数据未加载，或者数据为空但已加载（旋转后数据丢失），则重新加载
        if (!isDataLoaded || allListings.isEmpty()) {
            try {
                allListings = repository.getAvailableListings()
                isDataLoaded = true
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // 依搜索字 + 分类，过滤出要显示的列表
    val filteredListings = allListings.filter { listing ->
        val matchesCategory = selectedCategory == "All Items" || listing.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                listing.foodName.contains(searchQuery, ignoreCase = true) ||
                listing.merchantName.contains(searchQuery, ignoreCase = true) ||
                listing.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (isLandscape) {
            Column(
                modifier = Modifier.width(270.dp).fillMaxHeight().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Find fresh surplus food from merchants near you.", fontSize = 13.sp, color = TextGrey)
                OutlinedTextField(
                    value = searchFieldValue,
                    onValueChange = { searchFieldValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search merchant, food...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGrey) },
                    singleLine = true,
                    shape = RoundedCornerShape(21.dp),
                    colors = AppTextFieldDefaults.colors(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextGrey)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categoryTabs) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeAccent, selectedLabelColor = Color.White)
                        )
                    }
                }
            }
        }

        Column(
            modifier = if (isLandscape) Modifier.weight(1f).fillMaxHeight() else Modifier.fillMaxWidth()
        ) {
        if (!isLandscape) {
        Text(
            "Find fresh surplus food from merchants near you.",
            fontSize = 13.sp,
            color = TextGrey,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        // ---------- SEARCH BAR ----------
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(42.dp),
            shape = RoundedCornerShape(21.dp),
            color = Color.White,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGrey, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchFieldValue,
                    onValueChange = { searchFieldValue = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextDark),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text("Search merchant, food...", fontSize = 13.sp, color = TextGrey)
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---------- CATEGORY FILTER CHIPS ----------
        if (!isLandscape) LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryTabs) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent,
                        selectedLabelColor = Color.White
        )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---------- LISTING RESULTS ----------
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
            filteredListings.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No donations found.", color = TextGrey)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredListings) { listing ->
                        FoodListingCard(listing = listing, onCardClick = { onFoodClick(listing.id) })
                    }
                }
            }
        }
        }
    }
}
@Composable
private fun FoodListingCard(listing: FoodListing, onCardClick: () -> Unit) {
    Card(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 图片占位区（之后可以换成 AsyncImage 加载 photo_url）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFEFE6D9))
            ) {
                if (!listing.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = listing.photoUrl,
                        contentDescription = listing.foodName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(20.dp),
                    color = categoryColor(listing.category)
                ) {
                    Text(
                        listing.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(listing.foodName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextGrey,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${listing.distanceKm}km", fontSize = 12.sp, color = TextGrey)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    listing.description ?: "",
                    fontSize = 12.sp,
                    color = TextGrey,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("MERCHANT", fontSize = 10.sp, color = TextGrey)
                        Text(listing.merchantName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Button(
                        onClick = onCardClick,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Request", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

private fun categoryColor(category: String): Color = when (category.lowercase()) {
    "bakery" -> Color(0xFF2E7D32)
    "vegetables" -> Color(0xFFF57C00)
    "fruits" -> Color(0xFFE64A19)
    "dairy" -> Color(0xFF6D4C41)
    "canned goods" -> Color(0xFF8D6E63)
    "prepared meals" -> Color(0xFF1565C0)
    "other" -> Color(0xFF757575)
    else -> Color(0xFF757575)
}
