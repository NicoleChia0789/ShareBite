package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.BgCream
import com.sharebite.app.ui.theme.OrangeAccent

@Composable
fun MerchantImpactReportScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit
) {
    // State to control the Review Dialog
    var showReviewDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BgCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Impact Analytics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1B16)
        )
        Text(
            "Your contributions are building the community.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Main Impact Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("LIFETIME IMPACT", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                // Circular Donut representation (Visual only)
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(Color(0xFFFFF1E8), RoundedCornerShape(75.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(Color.White, RoundedCornerShape(55.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("12,482", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = OrangeAccent)
                            Text("LBS DIVERTED", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("35,660", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("MEALS PROVIDED", fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("2,480", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("CO2 SAVED (KG)", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Export / Review Button
                Button(
                    onClick = { showReviewDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Detailed Report", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Community Hero Progress
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Community Hero", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Silver Tier", color = OrangeAccent, fontWeight = FontWeight.Medium)
                    }
                    Text("🏅", fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.5f },
                    modifier = Modifier.fillMaxWidth(),
                    color = OrangeAccent,
                    trackColor = Color(0xFFFFF1E8)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("You're only 50 lbs away from becoming a Gold member. Keep up the amazing work!", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Badges
        Text("Earned Impact Badges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // weight(1f) is supplied HERE, since this Row is the actual RowScope —
            // BadgeCard itself just takes whatever modifier it's given.
            BadgeCard(
                title = "Eco-Warrior",
                subtitle = "Diverted 10,000 lbs of food",
                color = Color(0xFF2E7D32),
                icon = "🌱",
                modifier = Modifier.weight(1f)
            )
            BadgeCard(
                title = "Sustainer",
                subtitle = "Donated 6 consecutive months",
                color = OrangeAccent,
                icon = "💚",
                modifier = Modifier.weight(1f)
            )
            BadgeCard(
                title = "Local Hero",
                subtitle = "Locked",
                color = Color.Gray,
                icon = "⭐",
                isLocked = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Monthly Trends (Simple Bars)
        Text("Monthly Trends", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFFFFF1E8), RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(0.4f to "May", 0.6f to "Jun", 0.5f to "Jul", 0.8f to "Aug", 1.0f to "Sep", 0.7f to "Oct").forEach { (height, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height((height * 80f).dp)
                            .background(if (label == "Sep") OrangeAccent else Color(0xFFFADBC8), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(label, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Top Charity Partners
        Text("Top Charity Partners", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        PartnerCard(name = "City Harvest Network", amount = "3,240 lbs received")
        PartnerCard(name = "Downtown Mission", amount = "2,850 lbs received")
        PartnerCard(name = "Second Chance Pantry", amount = "1,920 lbs received")

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { /* Navigate to all partners later */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View All Partners", color = OrangeAccent, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard", color = Color.Gray)
        }
    }

    // The "Review Report" Dialog
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Impact Report Review", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Here is your detailed impact breakdown:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Total Food Diverted: 12,482 lbs")
                    Text("• Meals Provided: 35,660")
                    Text("• CO2 Saved: 2,480 KG")
                    Text("• Current Tier: Silver")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showReviewDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = OrangeAccent)
                ) {
                    Text("Close")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun BadgeCard(
    title: String,
    subtitle: String,
    color: Color,
    icon: String,
    isLocked: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isLocked) "🔒" else icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(subtitle, fontSize = 10.sp, color = Color.Gray, maxLines = 2)
        }
    }
}

@Composable
fun PartnerCard(name: String, amount: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏢", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(amount, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}