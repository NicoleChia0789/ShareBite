package com.sharebite.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sharebite.app.ui.theme.RedCancelled
import com.sharebite.app.ui.theme.TextDark
import com.sharebite.app.ui.theme.TextGrey
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun WithdrawConfirmDialog(
    onKeep: () -> Unit,
    onWithdraw: () -> Unit
) {
    Dialog(onDismissRequest = onKeep) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = RedCancelled,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Withdraw this request?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "The merchant will be notified. This cannot be undone.",
                    fontSize = 13.sp,
                    color = TextGrey,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onKeep,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Keep")
                    }
                    OutlinedButton(
                        onClick = onWithdraw,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancelled),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Withdraw")
                    }
                }
            }
        }
    }
}