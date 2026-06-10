package com.sproutly.app.nearby.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sproutly.app.core.design.*
import com.sproutly.app.nearby.NearbyViewModel
import com.sproutly.app.nearby.model.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(viewModel: NearbyViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Nearby") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Placeholder map card — replace with GoogleMap composable when API key + permissions are ready.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgElevated),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Map, contentDescription = null, tint = LeafMint)
                    Spacer(Modifier.height(8.dp))
                    Text("Map preview", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("Enable location to see pins around you.", color = TextMuted)
                }
            }

            SectionLabel("Plant-friendly within 1.5 km")
            state.places.forEach { PlaceRow(it) }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PlaceRow(place: Place) {
    SproutlyCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(place.tagline, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            }
            Text("${place.distanceKm} km", color = LeafMint, style = MaterialTheme.typography.labelLarge)
        }
    }
}
