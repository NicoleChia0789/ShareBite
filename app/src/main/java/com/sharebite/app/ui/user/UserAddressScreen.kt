package com.sharebite.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.data.model.UserAddress
import com.sharebite.app.data.repository.AddressRepository
import com.sharebite.app.ui.theme.*
import kotlinx.coroutines.launch
import android.widget.Toast

@Composable
fun UserAddressesScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onAddAddress: () -> Unit,
    onEditAddress: (String) -> Unit,
    onDeleteAddress: (String) -> Unit,
    onSetDefault: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val addressRepo = remember { AddressRepository() }

    var addresses by remember { mutableStateOf<List<UserAddress>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 加载地址
    fun loadAddresses() {
        scope.launch {
            isLoading = true
            try {
                addresses = addressRepo.getMyAddresses()
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadAddresses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {



        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage ?: "Error loading addresses", color = RedCancelled)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadAddresses() },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${addresses.size} address${if (addresses.size != 1) "es" else ""} saved",
                                fontSize = 14.sp,
                                color = TextGrey
                            )
                            Button(
                                onClick = onAddAddress,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = OrangeAccent
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Address",
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Add New",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (addresses.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = TextGrey
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No addresses saved yet",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "Tap 'Add New' to add your first address",
                                        fontSize = 14.sp,
                                        color = TextGrey
                                    )
                                }
                            }
                        }
                    } else {
                        items(addresses) { address ->
                            AddressCard(
                                address = address,
                                onEdit = {
                                    onEditAddress(address.id.toString())
                                },
                                onDelete = {
                                    scope.launch {
                                        try {
                                            val deleted = addressRepo.deleteAddress(address.id)
                                            if (deleted) {
                                                loadAddresses()
                                                Toast.makeText(context, "Address deleted", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Failed to delete address", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onSetDefault = {
                                    scope.launch {
                                        try {
                                            val updated = addressRepo.setDefaultAddress(address.id)
                                            if (updated) {
                                                loadAddresses()
                                                Toast.makeText(context, "Default address updated", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Failed to set default", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressCard(
    address: UserAddress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (address.isDefault) Color(0xFFFFF8EE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (address.isDefault)
            androidx.compose.foundation.BorderStroke(2.dp, OrangeAccent)
        else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = address.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (address.isDefault) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = OrangeAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Default",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp),
                        tint = TextGrey
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = RedCancelled
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = address.street,
                fontSize = 14.sp,
                color = TextDark
            )
            Text(
                text = "${address.city}, ${address.state} ${address.zip}",
                fontSize = 14.sp,
                color = TextDark
            )
            Text(
                text = address.country,
                fontSize = 14.sp,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TextGrey
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = address.phone,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
            }

            if (!address.isDefault) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Set as Default",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = OrangeAccent,
                    modifier = Modifier.clickable { onSetDefault() }
                )
            }
        }
    }
}