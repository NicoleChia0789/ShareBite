package com.sharebite.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*

@Composable
fun MerchantHelpScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {


        Text(
            text = "Get help with donating food and managing your listings.",
            fontSize = 14.sp,
            color = TextGrey
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Frequently Asked Questions ──
        Text(
            text = "Frequently Asked Questions",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        HelpItem(
            icon = Icons.Default.AddBox,
            question = "How do I add a donation?",
            answer = "Go to your Dashboard and tap 'Add Donation'. Fill in the food details, quantity, pickup date, and address."
        )

        HelpItem(
            icon = Icons.Default.Edit,
            question = "How do I edit or delete a listing?",
            answer = "Go to My Listings, select the item, and tap 'Edit' to modify details or 'Delete' to remove it."
        )

        HelpItem(
            icon = Icons.Default.Assignment,
            question = "How do I manage food requests?",
            answer = "Go to Requests to view all incoming requests. You can approve or decline them from there."
        )

        HelpItem(
            icon = Icons.Default.CheckCircle,
            question = "How do I mark a request as completed?",
            answer = "After the charity has picked up the donation, go to the request detail and tap 'Mark as Completed'."
        )

        HelpItem(
            icon = Icons.Default.Insights,
            question = "How do I track my impact?",
            answer = "Go to the Impact tab to see your total donations, food saved, and people helped."
        )

        HelpItem(
            icon = Icons.Default.LocationOn,
            question = "Do I need to provide a pickup address?",
            answer = "Yes, charities need an address to pick up the donation. You can set your business address in your profile."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Still Need Help? ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Still need help?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Contact us at support@sharebite.com",
                    fontSize = 13.sp,
                    color = OrangeAccent,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We typically respond within 24 hours.",
                    fontSize = 12.sp,
                    color = TextGrey
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "ShareBite v2.4.1",
            fontSize = 12.sp,
            color = TextGrey,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun HelpItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    question: String,
    answer: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(OrangeAccent.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = OrangeAccent
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = answer,
                    fontSize = 13.sp,
                    color = TextGrey,
                    lineHeight = 18.sp
                )
            }
        }
    }
}