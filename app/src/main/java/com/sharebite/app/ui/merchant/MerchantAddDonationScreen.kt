package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

private val STEP_LABELS = listOf("Details", "Pickup", "Review")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantAddDonationScreen(
    paddingValues: PaddingValues,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit,
    viewModel: MerchantViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(0) }

    var foodName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pickupDate by remember { mutableStateOf("") }
    var pickupTime by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    val step0Valid = foodName.isNotBlank() && category.isNotBlank() && quantity.isNotBlank()
    val step1Valid = pickupDate.isNotBlank() && pickupTime.isNotBlank()

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
                "Add New Donation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B16)
            )
            Spacer(modifier = Modifier.height(20.dp))

            StepIndicator(currentStep = currentStep, steps = STEP_LABELS)
            Spacer(modifier = Modifier.height(24.dp))

            when (currentStep) {
                0 -> StepDetails(
                    foodName = foodName, onFoodNameChange = { foodName = it },
                    category = category, onCategoryChange = { category = it },
                    quantity = quantity, onQuantityChange = { quantity = it }
                )
                1 -> StepPickup(
                    pickupDate = pickupDate, onDateChange = { pickupDate = it },
                    pickupTime = pickupTime, onTimeChange = { pickupTime = it },
                    notes = notes, onNotesChange = { notes = it }
                )
                2 -> StepReview(
                    foodName = foodName, category = category, quantity = quantity,
                    pickupDate = pickupDate, pickupTime = pickupTime, notes = notes
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
                        2 -> viewModel.createListing(
                            foodName = foodName,
                            category = category,
                            quantity = quantity,
                            pickupDate = pickupDate,
                            pickupTime = pickupTime,
                            notes = notes.ifBlank { null },
                            onComplete = onSaveSuccess
                        )
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
                    Text(
                        if (currentStep == 2) "Publish Listing" else "Next",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

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
                        .background(if (isActive) OrangeAccent else Color(0xFFE0E0E0), CircleShape),
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

@Composable
private fun StepDetails(
    foodName: String, onFoodNameChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    quantity: String, onQuantityChange: (String) -> Unit
) {
    Column {
        Text("Food Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = foodName, onValueChange = onFoodNameChange,
            label = { Text("Food Name") },
            placeholder = { Text("e.g. Assorted Morning Pastries") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = category, onValueChange = onCategoryChange,
            label = { Text("Category") },
            placeholder = { Text("e.g. Bakery, Produce") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = quantity, onValueChange = onQuantityChange,
            label = { Text("Quantity") },
            placeholder = { Text("e.g. 24 units") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }
}

@Composable
private fun StepPickup(
    pickupDate: String, onDateChange: (String) -> Unit,
    pickupTime: String, onTimeChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit
) {
    Column {
        Text("Pickup Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = pickupDate, onValueChange = onDateChange,
                label = { Text("Pickup Date") }, placeholder = { Text("2026-08-28") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true
            )
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = pickupTime, onValueChange = onTimeChange,
                label = { Text("Pickup Time") }, placeholder = { Text("6:00 PM") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = notes, onValueChange = onNotesChange,
            label = { Text("Pickup Instructions (Optional)") },
            placeholder = { Text("e.g. Back door entrance") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 3
        )
    }
}

@Composable
private fun StepReview(
    foodName: String, category: String, quantity: String,
    pickupDate: String, pickupTime: String, notes: String
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
                ReviewRow("Food Name", foodName)
                ReviewRow("Category", category)
                ReviewRow("Quantity", quantity)
                ReviewRow("Pickup", "$pickupDate at $pickupTime")
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