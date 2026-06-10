package com.sproutly.app.products.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sproutly.app.core.design.*
import com.sproutly.app.products.ProductViewModel
import com.sproutly.app.products.model.Deal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onOpenScanner: () -> Unit,
    viewModel: ProductViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.categories.forEach { c ->
                    Chip(
                        label = c.label,
                        selected = state.selectedCategory == c,
                        onClick = { viewModel.selectCategory(c) }
                    )
                }
            }

            SproutlyCard(accent = true) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = LeafMint)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Scan a label", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("Check vegan status, allergens, nutrition.", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                    GhostButton("Scan", onClick = onOpenScanner)
                }
            }

            SectionLabel("Deals this week")
            state.deals.forEach { DealRow(it) }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DealRow(deal: Deal) {
    SproutlyCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(deal.product.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(deal.highlight, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("€${"%.2f".format(deal.product.priceEur)}", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                deal.product.discountPercent?.let { Text("-$it%", color = LeafMint, style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}
