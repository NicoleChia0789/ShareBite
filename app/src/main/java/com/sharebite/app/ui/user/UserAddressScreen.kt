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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*

data class Address(
    val id: String,
    val label: String,
    val street: String,
    val city: String,
    val state: String,
    val zip: String,
    val country: String,
    val phone: String,
    val isDefault: Boolean = false
)

@Composable
fun UserAddressesScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onAddAddress: () -> Unit,
    onEditAddress: (String) -> Unit,
    onDeleteAddress: (String) -> Unit,
    onSetDefault: (String) -> Unit
) {
    val addresses = remember {
        emptyList<Address>()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
    ) {


        Spacer(modifier = Modifier.height(8.dp))

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

                    // ✅ 使用 Button 替代 TextButton
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
                        onEdit = { onEditAddress(address.id) },
                        onDelete = { onDeleteAddress(address.id) },
                        onSetDefault = { onSetDefault(address.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressCard(
    address: Address,
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
                text = address.street.ifBlank { "Not set yet" },
                fontSize = 14.sp,
                color = if (address.street.isBlank()) TextGrey else TextDark
            )
            Text(
                text = if (address.city.isNotBlank()) "${address.city}, ${address.state} ${address.zip}" else "Not set yet",
                fontSize = 14.sp,
                color = if (address.city.isBlank()) TextGrey else TextDark
            )
            Text(
                text = address.country.ifBlank { "Not set yet" },
                fontSize = 14.sp,
                color = if (address.country.isBlank()) TextGrey else TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (address.phone.isBlank()) TextGrey else TextGrey
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = address.phone.ifBlank { "Not set yet" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (address.phone.isBlank()) TextGrey else TextDark
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