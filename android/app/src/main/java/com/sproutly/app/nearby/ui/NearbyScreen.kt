package com.sproutly.app.nearby.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sproutly.app.core.design.BgDeep
import com.sproutly.app.core.design.BgSurface
import com.sproutly.app.core.design.Divider
import com.sproutly.app.core.design.LeafMint
import com.sproutly.app.core.design.MintPillButton
import com.sproutly.app.core.design.SproutlyCard
import com.sproutly.app.core.design.TextMuted
import com.sproutly.app.core.design.TextPrimary
import com.sproutly.app.core.permissions.PermissionHelpers
import com.sproutly.app.nearby.NearbyUiState
import com.sproutly.app.nearby.NearbyViewModel
import com.sproutly.app.nearby.model.DietFocus
import com.sproutly.app.nearby.model.LocationSource
import com.sproutly.app.nearby.model.NearbyFilters
import com.sproutly.app.nearby.model.Place
import com.sproutly.app.nearby.model.PlaceKind
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    initialSupermarketMode: Boolean = false,
    productStoreHint: String? = null,
    viewModel: NearbyViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var selectedPlaceId by rememberSaveable { mutableStateOf<String?>(null) }
    var showFallbackNotice by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val selectedPlace = state.places.firstOrNull { it.id == selectedPlaceId }

    LaunchedEffect(state.fallbackNoticeId, state.locationSource) {
        if (state.fallbackNoticeId > 0 && state.locationSource == LocationSource.MADRID_FALLBACK) {
            showFallbackNotice = true
            delay(4_000)
            showFallbackNotice = false
        } else {
            showFallbackNotice = false
        }
    }

    LaunchedEffect(selectedPlaceId) {
        if (selectedPlaceId != null && scrollState.value > 0) {
            scrollState.animateScrollTo(0)
        }
    }

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> viewModel.onLocationPermissionResult(result.values.any { it }) }

    LaunchedEffect(Unit) {
        val granted = PermissionHelpers.hasFineLocation(context) ||
            PermissionHelpers.isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        viewModel.loadInitial(granted)
        if (!granted) locationLauncher.launch(locationPermissions)
    }

    LaunchedEffect(initialSupermarketMode, productStoreHint) {
        if (initialSupermarketMode) viewModel.focusSupermarkets()
    }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Nearby", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !state.loading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDeep,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp)
                .padding(top = 4.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Map with slim overlays — no card above it.
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                NearbyMap(
                    state = state,
                    selectedPlaceId = selectedPlaceId,
                    onPlaceSelected = { selectedPlaceId = it.id },
                    modifier = Modifier.fillMaxSize(),
                )
                if (selectedPlace == null) {
                    MapHeaderChip(
                        state = state,
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    )
                }
                LocateButton(
                    enabled = !state.loading,
                    onClick = {
                        if (state.locationSource == LocationSource.DEVICE) viewModel.refresh()
                        else locationLauncher.launch(locationPermissions)
                    },
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                )
                if (showFallbackNotice) {
                    FallbackLocationNotice(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(top = 58.dp, start = 12.dp, end = 12.dp),
                    )
                }
                if (state.loading) {
                    MapLoadingOverlay(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                    )
                }
                if (selectedPlace != null) {
                    SelectedPlaceOverlay(
                        place = selectedPlace,
                        onDismiss = { selectedPlaceId = null },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    )
                }
            }

            if (initialSupermarketMode) {
                ProductStorePrompt(productStoreHint)
            }

            FiltersSection(
                filters = state.filters,
                onChange = {
                    selectedPlaceId = null
                    viewModel.setFilters(it)
                },
            )

            ResultsHeader(state)

            if (state.error != null) {
                ErrorCard(state.error ?: "Could not load nearby places.", onRetry = viewModel::refresh)
            } else if (!state.loading && state.places.isEmpty()) {
                EmptyCard()
            } else {
                state.places.forEach { place ->
                    PlaceRow(
                        place = place,
                        selected = place.id == selectedPlaceId,
                        onClick = { selectedPlaceId = place.id },
                    )
                }
            }
        }
    }
}

// ── Map overlays ────────────────────────────────────────────────────────────

@Composable
private fun ProductStorePrompt(storeName: String?) {
    SproutlyCard(accent = true) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.Storefront, contentDescription = null, tint = LeafMint)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    storeName?.let { "Good place to check: $it" } ?: "Supermarkets for your cart",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Product availability is an estimate for the MVP, not live stock.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun MapHeaderChip(state: NearbyUiState, modifier: Modifier = Modifier) {
    val location = if (state.locationSource == LocationSource.DEVICE) "Your area" else "Madrid"
    val diet = when (state.filters.dietFocus) {
        DietFocus.VEGAN -> "Vegan"
        DietFocus.VEGETARIAN -> "Vegetarian"
        DietFocus.FLEXIBLE -> "Plant-friendly"
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = BgDeep.copy(alpha = 0.72f),
        modifier = modifier.border(1.dp, Divider, RoundedCornerShape(50)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(
                imageVector = if (state.locationSource == LocationSource.DEVICE) {
                    Icons.Outlined.MyLocation
                } else Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = LeafMint,
                modifier = Modifier.size(14.dp),
            )
            Text(location, color = TextPrimary, style = MaterialTheme.typography.labelMedium)
            Text("·", color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Text(diet, color = LeafMint, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FallbackLocationNotice(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BgDeep.copy(alpha = 0.92f),
        modifier = modifier.border(1.dp, Divider, RoundedCornerShape(18.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Icon(
                Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = LeafMint,
                modifier = Modifier.size(16.dp),
            )
            Text(
                "Couldn't get your exact location. Showing central Madrid.",
                color = TextPrimary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MapLoadingOverlay(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BgDeep.copy(alpha = 0.9f),
        modifier = modifier.border(1.dp, Divider, RoundedCornerShape(18.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            CircularProgressIndicator(
                color = LeafMint,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp),
            )
            Column {
                Text(
                    "Gathering nearby data",
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Finding places to show on the map.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/**
 * Compact card that anchors to the bottom of the map when a place is selected.
 * Shows the name + distance from the user, with a close button to clear.
 *
 * Future: the leading slot can host an image (Google Places Photos requires a
 * paid API key; Wikidata `image=*` tags are present on only a handful of POIs,
 * so this is wired but unused for now).
 */
@Composable
private fun SelectedPlaceOverlay(
    place: Place,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BgDeep.copy(alpha = 0.94f),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Divider, RoundedCornerShape(18.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.NearMe,
                        contentDescription = null,
                        tint = LeafMint,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${place.distanceKm} km away",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (place.kind == PlaceKind.FULLY_PLANT_BASED) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "· plant-only",
                            color = LeafMint,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextMuted)
            }
        }
    }
}

@Composable
private fun LocateButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = BgDeep.copy(alpha = 0.78f),
        modifier = modifier
            .size(36.dp)
            .border(1.dp, Divider, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Outlined.MyLocation,
                contentDescription = "Locate me",
                tint = if (enabled) LeafMint else TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Filters ─────────────────────────────────────────────────────────────────

@Composable
private fun FiltersSection(filters: NearbyFilters, onChange: (NearbyFilters) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "FILTERS",
            color = TextMuted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterPill(
                "All",
                selected = !filters.fullyPlantBased && !filters.plantFriendly &&
                    !filters.supermarkets && !filters.restaurants && !filters.openNow,
            ) {
                onChange(
                    filters.copy(
                        fullyPlantBased = false,
                        plantFriendly = false,
                        supermarkets = false,
                        restaurants = false,
                        openNow = false,
                    )
                )
            }
            FilterPill("Restaurants", selected = filters.restaurants) {
                onChange(filters.copy(restaurants = !filters.restaurants))
            }
            FilterPill("Supermarkets", selected = filters.supermarkets) {
                onChange(filters.copy(supermarkets = !filters.supermarkets))
            }
            FilterPill("Plant-only", selected = filters.fullyPlantBased) {
                onChange(filters.copy(fullyPlantBased = !filters.fullyPlantBased))
            }
            FilterPill("Veg-friendly", selected = filters.plantFriendly) {
                onChange(filters.copy(plantFriendly = !filters.plantFriendly))
            }
            FilterPill("Open now", selected = filters.openNow) {
                onChange(filters.copy(openNow = !filters.openNow))
            }
            FilterPill("≤ 5 km", selected = filters.maxDistanceKm == 5.0) {
                onChange(filters.copy(maxDistanceKm = 5.0))
            }
            FilterPill("≤ 10 km", selected = filters.maxDistanceKm == 10.0) {
                onChange(filters.copy(maxDistanceKm = 10.0))
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) LeafMint else BgSurface
    val fg = if (selected) BgDeep else TextPrimary
    val borderColor = if (selected) LeafMint else Divider
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        contentColor = fg,
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

// ── Results & states ────────────────────────────────────────────────────────

@Composable
private fun ResultsHeader(state: NearbyUiState) {
    val visible = state.places.size
    val radius = state.filters.maxDistanceKm.toInt()
    val text = when {
        state.loading -> "Searching within $radius km…"
        state.error != null -> "We hit a snag"
        visible == 0 -> "No matches within $radius km"
        visible == 1 -> "1 spot within $radius km"
        else -> "$visible spots within $radius km"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (state.error != null) Color(0xFFE07A6A) else LeafMint),
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    SproutlyCard {
        Text("Couldn't reach OpenStreetMap", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(message, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        MintPillButton("Try again", onClick = onRetry, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun EmptyCard() {
    SproutlyCard {
        Text("Nothing matches here yet", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Try widening to 10 km or removing a filter. Coverage depends on community-tagged OpenStreetMap data.",
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PlaceRow(place: Place, selected: Boolean, onClick: () -> Unit) {
    SproutlyCard(accent = selected, modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(place.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
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
