package com.sharebite.app.ui.charity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharebite.app.ui.theme.*

@Composable
fun AboutScreen(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(OrangeAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Eco, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("ShareBite", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Connecting surplus food with communities in need.", fontSize = 13.sp, color = TextGrey)

        Spacer(modifier = Modifier.height(24.dp))

        AboutSection(
            title = "Our Mission",
            body = "ShareBite helps reduce food waste by connecting merchants with surplus food to local charities and communities. Every donation matched means less food wasted and more families fed."
        )

        AboutSection(
            title = "Aligned with UN SDG",
            body = "This app supports Sustainable Development Goal 2: Zero Hunger, and SDG 12: Responsible Consumption and Production, by redirecting surplus food to those who need it most."
        )

        AboutSection(
            title = "How It Works",
            body = "Merchants list their surplus food. Charities browse available donations and apply for what they need. Once confirmed, charities collect the food — simple, fast, and impactful."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardCream)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Version", fontSize = 12.sp, color = TextGrey)
                Text("1.0.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Built for AMIT3353 Mobile Application Development", fontSize = 11.sp, color = TextGrey)
            }
        }
    }
}

@Composable
private fun AboutSection(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 18.dp)) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
        Spacer(modifier = Modifier.height(6.dp))
        Text(body, fontSize = 13.sp, color = TextGrey, lineHeight = 19.sp)
    }
}