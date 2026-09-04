package com.sharebite.app.ui.merchant

import android.net.Uri
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.sharebite.app.ui.theme.AppTextFieldDefaults
import com.sharebite.app.ui.theme.*
import com.sharebite.app.ui.charity.filterQuantityInput
import com.sharebite.app.ui.charity.quantityValue
import com.sharebite.app.ui.charity.isQuantityValid
import com.sharebite.app.ui.charity.foodCategoryOptions
import com.sharebite.app.util.LocationUtils
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val STEP_LABELS = listOf("Details", "Pickup", "Review")
private val dietaryOptions = listOf("Regular", "Vegetarian", "Vegan", "Gluten-Free", "Nut-Free")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MerchantAddDonationScreen(
    paddingValues: PaddingValues,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit,
    viewModel: MerchantViewModel,
    listingId: Long? = null  // ✅ 可为空
) {
    val listings by viewModel.listings.collectAsState()
    val existingListing = remember(listingId, listings) {
        listingId?.let { id -> listings.find { it.id == id } }
    }
    val isEditMode = listingId != null

    var currentStep by remember { mutableIntStateOf(0) }

    var foodName by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var dietaryType by rememberSaveable { mutableStateOf("Regular") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by rememberSaveable { mutableStateOf<String?>(null) }

    var pickupDate by rememberSaveable { mutableStateOf("") }
    var pickupTime by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }

    var prefilled by rememberSaveable { mutableStateOf(false) }
    var dateTimeError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(pickupDate, pickupTime) {
        dateTimeError = when {
            pickupDate.isBlank() || pickupTime.isBlank() -> null
            isDateTimeInPast(pickupDate, pickupTime) -> "Collection date/time cannot be in the past"
            else -> null
        }
    }

    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    // 编辑模式：加载已有数据
    LaunchedEffect(existingListing) {
        if (!prefilled && existingListing != null) {
            foodName = existingListing.foodName
            category = existingListing.category
            dietaryType = existingListing.dietaryType ?: "Regular"
            quantity = filterQuantityInput(existingListing.totalQuantity.toString())
            pickupDate = existingListing.pickupDate
            pickupTime = existingListing.pickupTime.orEmpty()
            address = existingListing.address
            latitude = existingListing.latitude
            longitude = existingListing.longitude
            notes = existingListing.notes ?: ""
            existingPhotoUrl = existingListing.photoUrl
            prefilled = true
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> photoUri = uri
        println("🔍 [UI] Photo selected: $photoUri")  // ← 添加这行
    }

    val step0Valid = foodName.isNotBlank() && category.isNotBlank() && isQuantityValid(quantity)
    val step1Valid = pickupDate.isNotBlank() && pickupTime.isNotBlank()
            && !isDateTimeInPast(pickupDate, pickupTime)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationUtils = remember { LocationUtils(context as Activity) }
    var locationMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    fun useCurrentLocation() {
        scope.launch {
            locationMessage = "Detecting location..."
            val settingsOk = locationUtils.checkLocationSettings()
            val location = if (settingsOk) locationUtils.getCurrentLocation() else null
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                if (address.isBlank()) {
                    address = "${location.latitude}, ${location.longitude}"
                }
                locationMessage = "Location added for distance detection."
            } else {
                locationMessage = "Could not detect location. Please enable GPS and try again."
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasLocationPermission = granted
        if (granted) useCurrentLocation() else locationMessage = "Location permission denied."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                if (isEditMode) "Edit Donation" else "Add New Donation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B16)
            )
            Spacer(modifier = Modifier.height(20.dp))
            StepIndicator(currentStep = currentStep, steps = STEP_LABELS)
            Spacer(modifier = Modifier.height(24.dp))

            when (currentStep) {
                0 -> StepDetails(
                    foodName = foodName,
                    onFoodNameChange = { foodName = it },
                    category = category,
                    onCategoryChange = { category = it },
                    dietaryType = dietaryType,
                    onDietaryTypeChange = { dietaryType = it },
                    quantity = quantity,
                    onQuantityChange = { quantity = filterQuantityInput(it) },
                    photoUri = photoUri,
                    existingPhotoUrl = existingPhotoUrl,
                    onPickImage = { imagePicker.launch("image/*") }
                )
                1 -> StepPickup(
                    pickupDate = pickupDate,
                    pickupTime = pickupTime,
                    dateTimeError = dateTimeError,
                    onPickDate = {
                        val calendar = Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selected = Calendar.getInstance().apply { set(year, month, day) }
                                pickupDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selected.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        }.show()
                    },
                    onPickTime = {
                        val calendar = Calendar.getInstance()
                        android.app.TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                pickupTime = String.format(Locale.US, "%02d:%02d:00", hour, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    address = address,
                    onAddressChange = { address = it },
                    onUseCurrentLocation = {
                        if (hasLocationPermission) useCurrentLocation() else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    locationMessage = locationMessage,
                    notes = notes,
                    onNotesChange = { notes = it }
                )
                2 -> StepReview(
                    foodName = foodName,
                    category = category,
                    dietaryType = dietaryType,
                    quantity = quantity,
                    pickupDate = pickupDate,
                    pickupTime = pickupTime,
                    notes = notes,
                    photoUri = photoUri,
                    existingPhotoUrl = existingPhotoUrl,
                    address = address
                )
            }

            if (saveError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(saveError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { if (currentStep == 0) onCancel() else currentStep-- },
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (currentStep == 0) "Cancel" else "Back")
            }

            Button(
                onClick = {
                    when (currentStep) {
                        0, 1 -> currentStep++
                        2 -> {
                            if (isEditMode && listingId != null) {
                                viewModel.updateListing(
                                    listingId = listingId,
                                    foodName = foodName,
                                    category = category,
                                    dietaryType = dietaryType,
                                    quantity = quantity,
                                    photoUri = photoUri,
                                    existingPhotoUrl = existingPhotoUrl,
                                    pickupDate = pickupDate,
                                    pickupTime = pickupTime,
                                    notes = notes.ifBlank { null },
                                    address = address.ifBlank { null },
                                    latitude = latitude,
                                    longitude = longitude,
                                    onComplete = onSaveSuccess
                                )
                            } else {
                                viewModel.createListing(
                                    foodName = foodName,
                                    category = category,
                                    dietaryType = dietaryType,
                                    quantity = quantity,
                                    photoUri = photoUri,
                                    pickupDate = pickupDate,
                                    pickupTime = pickupTime,
                                    notes = notes.ifBlank { null },
                                    address = address.ifBlank { null },
                                    latitude = latitude,
                                    longitude = longitude,
                                    onComplete = onSaveSuccess
                                )
                            }
                        }
                    }
                },
                enabled = !isSaving && when (currentStep) {
                    0 -> step0Valid
                    1 -> step1Valid
                    else -> true
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    val label = when {
                        currentStep == 2 && isEditMode -> "Save Changes"
                        currentStep == 2 -> "Publish Listing"
                        else -> "Next"
                    }
                    Text(label, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── StepIndicator ────────────────────────────
@Composable
private fun StepIndicator(currentStep: Int, steps: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isActive = index <= currentStep
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            if (index != steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .height(2.dp)
                        .background(if (index < currentStep) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0))
                )
            }
        }
    }
}

// ─── Step Details ─────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepDetails(
    foodName: String,
    onFoodNameChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    dietaryType: String,
    onDietaryTypeChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    photoUri: Uri?,
    existingPhotoUrl: String?,
    onPickImage: () -> Unit
) {
    Column {
        Text("Food Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = foodName,
            onValueChange = onFoodNameChange,
            label = { Text("Food Name") },
            placeholder = { Text("e.g. Assorted Morning Pastries") },
            modifier = Modifier.fillMaxWidth(),
            colors = AppTextFieldDefaults.colors(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            foodCategoryOptions.forEach { option ->
                FilterChip(selected = category == option, onClick = { onCategoryChange(option) }, label = { Text(option) })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Dietary needs", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            dietaryOptions.forEach { option ->
                FilterChip(selected = dietaryType == option, onClick = { onDietaryTypeChange(option) }, label = { Text(option) })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = { Text("Quantity") },
            placeholder = { Text("e.g. 24 units") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = quantity.isNotBlank() && !isQuantityValid(quantity),
            supportingText = {
                when {
                    quantity.isBlank() -> Unit
                    !isQuantityValid(quantity) -> Text("Quantity must be at least 1 and no more than 100,000.", color = RedCancelled)
                    else -> Text("${quantityValue(quantity)} units")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { onPickImage() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0E8)),
            shape = RoundedCornerShape(12.dp)
        ) {
            val displayModel: Any? = photoUri ?: existingPhotoUrl
            if (displayModel != null) {
                AsyncImage(
                    model = displayModel,
                    contentDescription = "Donation Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Upload Photo",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap to add photo", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }
    }
}

// ─── Step Pickup ──────────────────────────────
@Composable
private fun StepPickup(
    pickupDate: String,
    pickupTime: String,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    locationMessage: String?,
    notes: String,
    onNotesChange: (String) -> Unit,
    dateTimeError: String? = null
) {
    Column {
        Text("Pickup Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onPickDate,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (pickupDate.isBlank()) "Select pickup date" else pickupDate)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onPickTime,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (pickupTime.isBlank()) "Select pickup time" else formatPickupTime(pickupTime))
        }
        if (dateTimeError != null) {
            Text(
                text = dateTimeError,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Pickup Address") },
            placeholder = { Text("123 Main Street, KL") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextButton(onClick = onUseCurrentLocation, modifier = Modifier.fillMaxWidth()) {
            Text("Use current GPS location")
        }
        if (locationMessage != null) {
            Text(locationMessage, fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Pickup Instructions (Optional)") },
            placeholder = { Text("e.g. Back door") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )
    }
}

// ─── Step Review ──────────────────────────────
@Composable
private fun StepReview(
    foodName: String,
    category: String,
    dietaryType: String,
    quantity: String,
    pickupDate: String,
    pickupTime: String,
    notes: String,
    photoUri: Uri?,
    existingPhotoUrl: String?,
    address: String
) {
    Column {
        Text("Review & Publish", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val displayModel: Any? = photoUri ?: existingPhotoUrl
                if (displayModel != null) {
                    AsyncImage(
                        model = displayModel,
                        contentDescription = "Donation Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                ReviewRow("Food Name", foodName)
                ReviewRow("Category", category)
                ReviewRow("Dietary Needs", dietaryType)
                ReviewRow("Quantity", quantity)
                ReviewRow("Pickup Date", pickupDate)
                ReviewRow("Pickup Time", formatPickupTime(pickupTime))
                ReviewRow("Address", address)
                if (notes.isNotBlank()) ReviewRow("Instructions", notes)
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatPickupTime(value: String): String = try {
    val parsed = SimpleDateFormat("HH:mm:ss", Locale.US).apply { isLenient = false }.parse(value)
    SimpleDateFormat("h:mm a", Locale.US).format(parsed!!)
} catch (_: Exception) {
    value
}
