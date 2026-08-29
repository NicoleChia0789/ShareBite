package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.repository.CharityRepository
import com.sharebite.app.data.repository.FoodListingRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ApplyForFoodScreen(
    listingId: Long,
    paddingValues: PaddingValues,
    onSubmitSuccess: () -> Unit
) {
    val listingRepository = remember { FoodListingRepository() }
    val charityRepository = remember { CharityRepository() }
    val coroutineScope = rememberCoroutineScope()

    var listing by remember { mutableStateOf<FoodListing?>(null) }
    var isLoadingListing by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // 只有这三项是使用者可以填的（其他都是商家定的固定资料）
    // ⚠️ organisationName 目前手动填，是因为还没做登入功能；
    // 之后做完 Auth，这里应该改成自动带入登入者的机构名称，并改为只读显示
    var organisationName by remember { mutableStateOf("") }
    var peopleServed by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(listingId) {
        try {
            listing = listingRepository.getListingById(listingId)
        } catch (e: Exception) {
            loadError = e.message
        } finally {
            isLoadingListing = false
        }
    }

    fun validateForm(): List<String> {
        val errors = mutableListOf<String>()
        if (organisationName.isBlank()) errors.add("Organisation name")
        return errors
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {
        when {
            isLoadingListing -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            loadError != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $loadError", color = RedCancelled)
                }
            }
            listing == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Listing not found.", color = TextGrey)
                }
            }
            else -> {
                val data = listing!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 40.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // ---------- APPLYING FROM BANNER ----------
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardCream)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Applying from", fontSize = 12.sp, color = TextGrey)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${data.foodName} · ${data.merchantName}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ---------- FIXED DETAILS (商家定的，不可改) ----------
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Set by donor · cannot be changed",
                                fontSize = 11.sp,
                                color = TextGrey
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FixedDetailRow(label = "Quantity", value = data.quantity)
                            FixedDetailRow(label = "Collection date", value = data.pickupDate)
                            FixedDetailRow(label = "Collection time", value = data.deadline)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Your Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "This will be auto-filled from your account once login is added.",
                        fontSize = 11.sp,
                        color = TextGrey
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- Organisation name（暂时手动填，等 Auth 完成后改为自动带入） -----
                    OutlinedTextField(
                        value = organisationName,
                        onValueChange = { organisationName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Organisation name") },
                        placeholder = { Text("e.g. Hope Charity Home") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = TextGrey,
                            focusedLabelColor = OrangeAccent,
                            unfocusedLabelColor = TextGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- People served -----
                    OutlinedTextField(
                        value = peopleServed,
                        onValueChange = { peopleServed = filterQuantityInput(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("People served") },
                        placeholder = { Text("e.g. 30") },
                        supportingText = {
                            if (peopleServed.isNotBlank()) {
                                Text("= ${formatWithThousandSeparator(peopleServed)} people")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = TextGrey,
                            focusedLabelColor = OrangeAccent,
                            unfocusedLabelColor = TextGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- Additional note -----
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        label = { Text("Additional note (Optional)") },
                        placeholder = { Text("Dietary restrictions, or how you'll use this donation...") },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = TextGrey,
                            focusedLabelColor = OrangeAccent,
                            unfocusedLabelColor = TextGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ----- Submit button -----
                    Button(
                        onClick = {
                            val errors = validateForm()
                            if (errors.isNotEmpty()) {
                                errorMessage = "Please fill in all required fields:\n• ${errors.joinToString("\n• ")}"
                                showErrorDialog = true
                                return@Button
                            }
                            isSubmitting = true
                            coroutineScope.launch {
                                try {
                                    charityRepository.createRequestFromListing(
                                        merchantId = data.merchantId,
                                        listingId = data.id.toString(),
                                        foodName = data.foodName,
                                        category = data.category,
                                        quantity = data.quantity.filter { c -> c.isDigit() }.toIntOrNull() ?: 0,
                                        pickupDate = data.pickupDate,
                                        pickupTime = data.deadline,
                                        organisationName = organisationName,
                                        peopleServed = peopleServed.toIntOrNull() ?: 0,
                                        note = note.ifBlank { null }
                                    )
                                    isSubmitting = false
                                    onSubmitSuccess()
                                } catch (e: Exception) {
                                    isSubmitting = false
                                    errorMessage = e.message ?: "Failed to submit request"
                                    showErrorDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Submit request", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // ----- 错误提示弹窗 -----
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Incomplete Form", color = RedCancelled, fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage, color = TextDark) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) { Text("OK", color = Color.White) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun FixedDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextGrey)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}