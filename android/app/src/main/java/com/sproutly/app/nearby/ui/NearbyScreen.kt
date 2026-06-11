package com.sproutly.app.nearby.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sproutly.app.core.design.BgDeep
import com.sproutly.app.core.design.Chip
import com.sproutly.app.core.design.Divider
import com.sproutly.app.core.design.LeafMint
import com.sproutly.app.core.design.MintPillButton
import com.sproutly.app.core.design.SectionLabel
import com.sproutly.app.core.design.SproutlyCard
import com.sproutly.app.core.design.TextMuted
import com.sproutly.app.core.design.TextPrimary
import com.sproutly.app.core.permissions.PermissionHelpers
import com.sproutly.app.nearby.NearbyViewModel
import com.sproutly.app.nearby.model.LocationSource
import com.sproutly.app.nearby.model.NearbyFilters
import com.sproutly.app.nearby.model.Place
import com.sproutly.app.nearby.model.PlaceKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(viewModel: NearbyViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var selectedPlaceId by rememberSaveable { mutableStateOf<String?>(null) }
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        viewModel.onLocationPermissionResult(result.values.any { it })
    }

    LaunchedEffect(Unit) {
        val hasLocationPermission = PermissionHelpers.hasFineLocation(context) ||
            PermissionHelpers.isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        viewModel.loadInitial(hasLocationPermission)
        if (!hasLocationPermission) {
            locationLauncher.launch(locationPermissions)
        }
    }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Nearby") },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !state.loading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh nearby places")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDeep,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LocationStatus(
                source = state.locationSource,
                loading = state.loading,
                onRequestLocation = { locationLauncher.launch(locationPermissions) },
            )

            NearbyMap(
                state = state,
                selectedPlaceId = selectedPlaceId,
                onPlaceSelected = { selectedPlaceId = it.id },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
            )

            FilterRow(
                filters = state.filters,
                onChange = {
                    selectedPlaceId = null
                    viewModel.setFilters(it)
                },
            )

            if (state.error != null) {
                ErrorCard(
                    message = state.error ?: "Could not load nearby places.",
                    onRetry = viewModel::refresh,
                )
            }

            if (state.loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = LeafMint,
                    trackColor = Divider,
                )
            }

            SectionLabel("Vegetarian restaurants and supermarkets")

            if (!state.loading && state.places.isEmpty() && state.error == null) {
                EmptyCard()
            }

            state.places.forEach { place ->
                PlaceRow(
                    place = place,
                    selected = place.id == selectedPlaceId,
                    onClick = { selectedPlaceId = place.id },
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LocationStatus(
    source: LocationSource,
    loading: Boolean,
    onRequestLocation: () -> Unit,
) {
    SproutlyCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = if (source == LocationSource.DEVICE) {
                    Icons.Outlined.MyLocation
                } else {
                    Icons.Outlined.LocationOn
                },
                contentDescription = null,
                tint = LeafMint,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (source == LocationSource.DEVICE) {
                        "Using your device location"
                    } else {
                        "Showing central Madrid"
                    },
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = if (source == LocationSource.DEVICE) {
                        "Results update around your current position."
                    } else {
                        "Allow location for nearby results around you."
                    },
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (source == LocationSource.MADRID_FALLBACK) {
                TextButton(onClick = onRequestLocation, enabled = !loading) {
                    Text("Use GPS")
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    filters: NearbyFilters,
    onChange: (NearbyFilters) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chip("All", selected = filters == NearbyFilters(maxDistanceKm = filters.maxDistanceKm)) {
            onChange(NearbyFilters(maxDistanceKm = filters.maxDistanceKm))
        }
        Chip("Vegetarian", selected = filters.plantFriendly) {
            onChange(filters.copy(plantFriendly = !filters.plantFriendly))
        }
        Chip("Fully plant-based", selected = filters.fullyPlantBased) {
            onChange(filters.copy(fullyPlantBased = !filters.fullyPlantBased))
        }
        Chip("Restaurants", selected = filters.restaurants) {
            onChange(filters.copy(restaurants = !filters.restaurants))
        }
        Chip("Supermarkets", selected = filters.supermarkets) {
            onChange(filters.copy(supermarkets = !filters.supermarkets))
        }
        Chip("5 km", selected = filters.maxDistanceKm == 5.0) {
            onChange(filters.copy(maxDistanceKm = 5.0))
        }
        Chip("10 km", selected = filters.maxDistanceKm == 10.0) {
            onChange(filters.copy(maxDistanceKm = 10.0))
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    SproutlyCard {
        Text(
            text = "OpenStreetMap search failed",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(6.dp))
        Text(message, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        MintPillButton("Try again", onClick = onRetry, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun EmptyCard() {
    SproutlyCard {
        Text(
            text = "No matching places found nearby",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Try the 10 km radius or fewer filters. Madrid coverage depends on OpenStreetMap tags.",
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PlaceRow(place: Place, selected: Boolean, onClick: () -> Unit) {
    SproutlyCard(
        accent = selected,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        place.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    KindBadge(place.kind)
                }
                Spacer(Modifier.height(4.dp))
                Text(place.tagline, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                if (place.address != null) {
                    Spacer(Modifier.height(3.dp))
                    Text(place.address, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                "${place.distanceKm} km",
                color = LeafMint,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun KindBadge(kind: PlaceKind) {
    val label = when (kind) {
        PlaceKind.FULLY_PLANT_BASED -> "plant-only"
        PlaceKind.PLANT_FRIENDLY -> "veg options"
        PlaceKind.SUPERMARKET -> "market"
        PlaceKind.RESTAURANT -> "food"
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = LeafMint.copy(alpha = 0.14f),
        contentColor = LeafMint,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
