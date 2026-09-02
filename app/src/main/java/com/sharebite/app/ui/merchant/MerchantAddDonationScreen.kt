package com.sharebite.app.ui.merchant

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sharebite.app.ui.theme.AppTextFieldDefaults
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

private val STEP_LABELS = listOf("Details", "Pickup", "Review")

@OptIn(ExperimentalMaterial3Api::class)
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
    var quantity by rememberSaveable { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by rememberSaveable { mutableStateOf<String?>(null) }

    var pickupDate by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    var prefilled by rememberSaveable { mutableStateOf(false) }

    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    // 编辑模式：加载已有数据
    LaunchedEffect(existingListing) {
        if (!prefilled && existingListing != null) {
            foodName = existingListing.foodName
            category = existingListing.category
            quantity = existingListing.totalQuantity.toString()
            pickupDate = existingListing.pickupDate
            address = existingListing.address
            notes = existingListing.notes ?: ""
            prefilled = true
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> photoUri = uri
        println("🔍 [UI] Photo selected: $photoUri")  // ← 添加这行
    }

    val step0Valid = foodName.isNotBlank() && category.isNotBlank() && quantity.isNotBlank()
    val step1Valid = pickupDate.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
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
                    quantity = quantity,
                    onQuantityChange = { quantity = it },
                    photoUri = photoUri,
                    existingPhotoUrl = existingPhotoUrl,
                    onPickImage = { imagePicker.launch("image/*") }
                )
                1 -> StepPickup(
                    pickupDate = pickupDate,
                    onDateChange = { pickupDate = it },
                    address = address,
                    onAddressChange = { address = it },
                    notes = notes,
                    onNotesChange = { notes = it }
                )
                2 -> StepReview(
                    foodName = foodName,
                    category = category,
                    quantity = quantity,
                    pickupDate = pickupDate,
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
                                    quantity = quantity,
                                    photoUri = photoUri,
                                    existingPhotoUrl = existingPhotoUrl,
                                    pickupDate = pickupDate,
                                    notes = notes.ifBlank { null },
                                    address = address.ifBlank { null },
                                    onComplete = onSaveSuccess
                                )
                            } else {
                                viewModel.createListing(
                                    foodName = foodName,
                                    category = category,
                                    quantity = quantity,
                                    photoUri = photoUri,
                                    pickupDate = pickupDate,
                                    notes = notes.ifBlank { null },
                                    address = address.ifBlank { null },
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
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
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
                            if (isActive) OrangeAccent else Color(0xFFE0E0E0),
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
                    color = if (isActive) OrangeAccent else Color.Gray
                )
            }
            if (index != steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .height(2.dp)
                        .background(if (index < currentStep) OrangeAccent else Color(0xFFE0E0E0))
                )
            }
        }
    }
}

// ─── Step Details ─────────────────────────────
@Composable
private fun StepDetails(
    foodName: String,
    onFoodNameChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
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
        OutlinedTextField(
            value = category,
            onValueChange = onCategoryChange,
            label = { Text("Category") },
            placeholder = { Text("e.g. Bakery, Produce") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = { Text("Quantity") },
            placeholder = { Text("e.g. 24 units") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
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
                        tint = OrangeAccent
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
    onDateChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column {
        Text("Pickup Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = pickupDate,
            onValueChange = onDateChange,
            label = { Text("Pickup Date") },
            placeholder = { Text("2026-08-28") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Pickup Address") },
            placeholder = { Text("123 Main Street, KL") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
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
    quantity: String,
    pickupDate: String,
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
                ReviewRow("Quantity", quantity)
                ReviewRow("Pickup Date", pickupDate)
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