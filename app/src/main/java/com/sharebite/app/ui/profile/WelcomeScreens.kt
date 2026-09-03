package com.sharebite.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.sharebite.app.R
import com.sharebite.app.ui.theme.ShareBiteBorder
import com.sharebite.app.ui.theme.ShareBiteCream
import com.sharebite.app.ui.theme.ShareBiteMuted
import com.sharebite.app.ui.theme.ShareBiteOrange
import com.sharebite.app.ui.theme.ShareBiteOrangeSoft
import com.sharebite.app.ui.theme.ShareBiteText

@Composable
fun ChooseRoleScreen(onRoleSelected: (String) -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShareBiteCream)
            .padding(horizontal = 24.dp, vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader(
            title = "Choose your role",
            subtitle = "Select the option that best describes you."
        )
        Spacer(Modifier.height(34.dp))
        RoleOption("Merchant", "Share surplus food from your business.", Icons.Default.Storefront) {
            onRoleSelected("Merchant")
        }
        Spacer(Modifier.height(14.dp))
        RoleOption("Individual user", "Find and reserve food near you.", Icons.Default.Person) {
            onRoleSelected("User")
        }
        Spacer(Modifier.height(14.dp))
        RoleOption("Charity / NGO", "Support people in need with donated food.", Icons.Default.VolunteerActivism) {
            onRoleSelected("Charity")
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Back to login",
            color = ShareBiteOrange,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(12.dp)
        )
    }
}

@Composable
fun AuthHeader(title: String, subtitle: String) {
    Image(
        painter = painterResource(R.drawable.sharebite_logo),
        contentDescription = "ShareBite logo",
        modifier = Modifier.size(82.dp),
        contentScale = ContentScale.Fit
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        color = ShareBiteText,
        textAlign = TextAlign.Center,
        modifier = Modifier.height(38.dp)
    )
    Text(
        text = subtitle,
        color = ShareBiteMuted,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.height(48.dp)
    )
}

@Composable
private fun RoleOption(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ShareBiteBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(ShareBiteOrangeSoft, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ShareBiteOrange)
            }
            Column {
                Text(title, color = ShareBiteText, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(description, color = ShareBiteMuted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
