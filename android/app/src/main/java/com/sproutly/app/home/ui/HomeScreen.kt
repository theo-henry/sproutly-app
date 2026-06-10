package com.sproutly.app.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sproutly.app.core.design.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenAccount: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenNearby: () -> Unit,
) {
    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Sproutly", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onOpenAccount) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "Account", tint = LeafMint)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDeep,
                    titleContentColor = TextPrimary,
                )
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlantHero(modifier = Modifier.fillMaxWidth())

            SproutlyCard {
                SectionLabel("Today")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tofu scramble, lentil bowl, cashew greens.",
                    style = MaterialTheme.typography.titleMedium, color = TextPrimary
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Stat("28g", "Protein")
                    Stat("32 min", "Total prep")
                    Stat("€9.40", "Today")
                }
            }

            SproutlyCard {
                SectionLabel("Reminders")
                Spacer(Modifier.height(8.dp))
                Text("Hydration and B12", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("Stay topped up — drink water and take your supplement.", color = TextMuted)
            }

            SproutlyCard {
                SectionLabel("Near you")
                Spacer(Modifier.height(8.dp))
                Text("Three plant-forward spots", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(10.dp))
                GhostButton("Open map", onClick = onOpenNearby)
            }

            SectionLabel("Quick actions")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction("Log meal", Icons.Outlined.RestaurantMenu, Modifier.weight(1f)) {}
                QuickAction("Scan label", Icons.Outlined.QrCodeScanner, Modifier.weight(1f), onOpenScanner)
                QuickAction("Find spot", Icons.Outlined.Storefront, Modifier.weight(1f), onOpenNearby)
            }

            SectionLabel("For you")
            ForYouCard("High-protein chickpea bowl", "30 min · 32g protein")
            ForYouCard("Creamy cashew pasta", "25 min · 18g protein")

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleLarge, color = LeafMint)
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    SproutlyCard(modifier = modifier, accent = true) {
        Icon(icon, contentDescription = null, tint = LeafMint)
        Spacer(Modifier.height(8.dp))
        Text(label, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        TextButton(onClick = onClick) { Text("Open", color = LeafMint) }
    }
}

@Composable
private fun ForYouCard(title: String, subtitle: String) {
    SproutlyCard {
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
