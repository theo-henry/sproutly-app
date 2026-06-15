package com.sproutly.app.nearby.ui

import android.graphics.RectF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sproutly.app.core.config.AppConfig
import com.sproutly.app.core.design.BgDeep
import com.sproutly.app.core.design.BgSurface
import com.sproutly.app.core.design.Divider as DividerColor
import com.sproutly.app.core.design.LeafDeep
import com.sproutly.app.core.design.LeafGreen
import com.sproutly.app.core.design.LeafMint
import com.sproutly.app.core.design.TextPrimary
import com.sproutly.app.core.network.OsmStyle
import com.sproutly.app.nearby.NearbyUiState
import com.sproutly.app.nearby.model.GeoPoint
import com.sproutly.app.nearby.model.Place
import com.sproutly.app.nearby.model.PlaceKind
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/**
 * MapLibre-backed OpenStreetMap view. If the native map path fails to initialize
 * or load, the Compose canvas fallback below still shows the origin and pins.
 */
@Composable
fun NearbyMap(
    state: NearbyUiState,
    selectedPlaceId: String?,
    onPlaceSelected: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    var useFallbackMap by remember { mutableStateOf(false) }

    if (useFallbackMap) {
        FallbackNearbyMap(
            state = state,
            selectedPlaceId = selectedPlaceId,
            onPlaceSelected = onPlaceSelected,
            modifier = modifier,
        )
        return
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(BgSurface),
    ) {
        MapLibreNearbyMap(
            state = state,
            selectedPlaceId = selectedPlaceId,
            onPlaceSelected = onPlaceSelected,
            onMapUnavailable = { useFallbackMap = true },
            modifier = Modifier.fillMaxSize(),
        )

        Text(
            text = AppConfig.MAP_ATTRIBUTION,
            color = TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .background(BgDeep.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun MapLibreNearbyMap(
    state: NearbyUiState,
    selectedPlaceId: String?,
    onPlaceSelected: (Place) -> Unit,
    onMapUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestPlaces by rememberUpdatedState(state.places)
    val latestOnPlaceSelected by rememberUpdatedState(onPlaceSelected)
    val latestOnMapUnavailable by rememberUpdatedState(onMapUnavailable)
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }

    val mapView = remember {
        runCatching {
            MapView(context).also { view ->
                view.onCreate(null)
                view.addOnDidFailLoadingMapListener { reason ->
                    Log.w(MAP_TAG, "MapLibre failed loading map: $reason")
                    latestOnMapUnavailable()
                }
            }
        }.onFailure { error ->
            Log.w(MAP_TAG, "MapLibre failed to initialize", error)
        }.getOrNull()
    }

    if (mapView == null) {
        LaunchedEffect(Unit) { latestOnMapUnavailable() }
        FallbackNearbyMap(
            state = state,
            selectedPlaceId = selectedPlaceId,
            onPlaceSelected = onPlaceSelected,
            modifier = modifier,
        )
        return
    }

    DisposableEffect(lifecycleOwner, mapView) {
        var started = false
        var resumed = false

        fun startMap() {
            if (!started) {
                mapView.onStart()
                started = true
            }
        }

        fun resumeMap() {
            if (!resumed) {
                mapView.onResume()
                resumed = true
            }
        }

        fun pauseMap() {
            if (resumed) {
                mapView.onPause()
                resumed = false
            }
        }

        fun stopMap() {
            if (started) {
                mapView.onStop()
                started = false
            }
        }

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) startMap()
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) resumeMap()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> startMap()
                Lifecycle.Event.ON_RESUME -> resumeMap()
                Lifecycle.Event.ON_PAUSE -> pauseMap()
                Lifecycle.Event.ON_STOP -> stopMap()
                Lifecycle.Event.ON_DESTROY -> {
                    pauseMap()
                    stopMap()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            pauseMap()
            stopMap()
            mapView.onDestroy()
            map = null
            style = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                getMapAsync { readyMap ->
                    map = readyMap
                    readyMap.setMinZoomPreference(9.0)
                    readyMap.setMaxZoomPreference(18.0)
                    readyMap.addOnMapClickListener { latLng ->
                        val screenPoint = readyMap.projection.toScreenLocation(latLng)
                        val tapBounds = RectF(
                            screenPoint.x - TAP_TARGET_PX,
                            screenPoint.y - TAP_TARGET_PX,
                            screenPoint.x + TAP_TARGET_PX,
                            screenPoint.y + TAP_TARGET_PX,
                        )
                        val placeId = readyMap.queryRenderedFeatures(
                            tapBounds,
                            SELECTED_PLACE_LAYER_ID,
                            PLACE_LAYER_ID,
                        ).firstNotNullOfOrNull { feature ->
                            feature.getStringProperty(PROPERTY_ID)
                        }
                        val place = latestPlaces.firstOrNull { it.id == placeId }
                        if (place != null) {
                            latestOnPlaceSelected(place)
                            true
                        } else {
                            false
                        }
                    }
                    readyMap.setStyle(Style.Builder().fromJson(OsmStyle.JSON)) { loadedStyle ->
                        ensureNearbyLayers(loadedStyle)
                        style = loadedStyle
                    }
                }
            }
        },
    )

    LaunchedEffect(map, style, state.places, state.origin, selectedPlaceId) {
        style?.let { updateNearbySources(it, state, selectedPlaceId) }
    }

    LaunchedEffect(map, style, state.origin, state.effectiveRadiusKm) {
        map?.let { readyMap ->
            if (style != null) {
                readyMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(state.origin.lat, state.origin.lng),
                        zoomForRadius(state.effectiveRadiusKm),
                    )
                )
            }
        }
    }
}

/**
 * Compose-rendered backup map. It is intentionally simple and deterministic so
 * Nearby still works on emulators/devices where the native GL tile path fails.
 */
@Composable
private fun FallbackNearbyMap(
    state: NearbyUiState,
    selectedPlaceId: String?,
    onPlaceSelected: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val halfExtentKm = max(state.effectiveRadiusKm * 1.1, 3.5)
    val tapRadiusPx = with(density) { 26.dp.toPx() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(BgSurface),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.places, state.origin, halfExtentKm) {
                    detectTapGestures { tap ->
                        val proj = MapProjection(
                            width = size.width.toFloat(),
                            height = size.height.toFloat(),
                            origin = state.origin,
                            halfExtentKm = halfExtentKm,
                        )
                        val hit = state.places
                            .mapNotNull { place ->
                                val lat = place.lat ?: return@mapNotNull null
                                val lng = place.lng ?: return@mapNotNull null
                                val pos = proj.toOffset(lat, lng)
                                Triple(place, pos, hypot(pos.x - tap.x, pos.y - tap.y))
                            }
                            .filter { it.third < tapRadiusPx }
                            .minByOrNull { it.third }
                        if (hit != null) onPlaceSelected(hit.first)
                    }
                },
        ) {
            val proj = MapProjection(
                width = size.width,
                height = size.height,
                origin = state.origin,
                halfExtentKm = halfExtentKm,
            )
            drawBase()
            drawGridStreets(proj)
            drawParks(proj)
            drawRiver(proj)
            drawOriginRing()
            drawPins(proj, state.places, selectedPlaceId)
        }

        // Bottom-right attribution — kept short so the bottom-left overlay slot
        // remains free for the SelectedPlaceOverlay.
        Text(
            text = "Stylized map of Madrid",
            color = TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .background(BgDeep.copy(alpha = 0.66f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

// ── MapLibre sources/layers ─────────────────────────────────────────────────

private fun ensureNearbyLayers(style: Style) {
    if (style.getSource(ORIGIN_SOURCE_ID) == null) {
        style.addSource(GeoJsonSource(ORIGIN_SOURCE_ID, emptyFeatureCollection()))
    }
    if (style.getSource(PLACES_SOURCE_ID) == null) {
        style.addSource(GeoJsonSource(PLACES_SOURCE_ID, emptyFeatureCollection()))
    }
    if (style.getSource(SELECTED_PLACE_SOURCE_ID) == null) {
        style.addSource(GeoJsonSource(SELECTED_PLACE_SOURCE_ID, emptyFeatureCollection()))
    }

    if (style.getLayer(ORIGIN_HALO_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(ORIGIN_HALO_LAYER_ID, ORIGIN_SOURCE_ID).withProperties(
                circleRadius(18f),
                circleColor("#B7F7CE"),
                circleOpacity(0.24f),
                circleStrokeColor("#F8FFF9"),
                circleStrokeOpacity(0.28f),
                circleStrokeWidth(1.5f),
            )
        )
    }
    if (style.getLayer(ORIGIN_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(ORIGIN_LAYER_ID, ORIGIN_SOURCE_ID).withProperties(
                circleRadius(7f),
                circleColor("#7CE7B2"),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(2.5f),
            )
        )
    }
    if (style.getLayer(PLACE_HALO_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(PLACE_HALO_LAYER_ID, PLACES_SOURCE_ID).withProperties(
                circleRadius(13f),
                circleColor("#0E1E15"),
                circleOpacity(0.58f),
            )
        )
    }
    if (style.getLayer(PLACE_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(PLACE_LAYER_ID, PLACES_SOURCE_ID).withProperties(
                circleRadius(8f),
                circleColor(placeColorExpression()),
                circleStrokeColor("#F8FFF9"),
                circleStrokeOpacity(0.92f),
                circleStrokeWidth(2f),
            )
        )
    }
    if (style.getLayer(SELECTED_PLACE_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(SELECTED_PLACE_LAYER_ID, SELECTED_PLACE_SOURCE_ID).withProperties(
                circleRadius(13f),
                circleColor("#B7F7CE"),
                circleStrokeColor("#07140D"),
                circleStrokeWidth(3.5f),
            )
        )
    }
}

private fun updateNearbySources(
    style: Style,
    state: NearbyUiState,
    selectedPlaceId: String?,
) {
    style.getSourceAs<GeoJsonSource>(ORIGIN_SOURCE_ID)
        ?.setGeoJson(originFeatureCollection(state.origin))
    style.getSourceAs<GeoJsonSource>(PLACES_SOURCE_ID)
        ?.setGeoJson(placeFeatureCollection(state.places))
    style.getSourceAs<GeoJsonSource>(SELECTED_PLACE_SOURCE_ID)
        ?.setGeoJson(selectedFeatureCollection(state.places, selectedPlaceId))
}

private fun placeFeatureCollection(places: List<Place>): FeatureCollection =
    FeatureCollection.fromFeatures(
        places.mapNotNull { place ->
            val lat = place.lat ?: return@mapNotNull null
            val lng = place.lng ?: return@mapNotNull null
            Feature.fromGeometry(Point.fromLngLat(lng, lat)).apply {
                addStringProperty(PROPERTY_ID, place.id)
                addStringProperty(PROPERTY_KIND, place.kind.name)
            }
        }
    )

private fun selectedFeatureCollection(
    places: List<Place>,
    selectedPlaceId: String?,
): FeatureCollection {
    val selected = places.firstOrNull { it.id == selectedPlaceId }
    val lat = selected?.lat
    val lng = selected?.lng
    return if (lat != null && lng != null) {
        FeatureCollection.fromFeature(
            Feature.fromGeometry(Point.fromLngLat(lng, lat)).apply {
                addStringProperty(PROPERTY_ID, selected.id)
                addStringProperty(PROPERTY_KIND, selected.kind.name)
            }
        )
    } else {
        emptyFeatureCollection()
    }
}

private fun originFeatureCollection(origin: GeoPoint): FeatureCollection =
    FeatureCollection.fromFeature(Feature.fromGeometry(Point.fromLngLat(origin.lng, origin.lat)))

private fun emptyFeatureCollection(): FeatureCollection =
    FeatureCollection.fromFeatures(emptyList<Feature>())

private fun placeColorExpression(): Expression =
    Expression.match(
        Expression.get(PROPERTY_KIND),
        Expression.literal("#7CE7B2"),
        Expression.stop(PlaceKind.FULLY_PLANT_BASED.name, "#6EDA8A"),
        Expression.stop(PlaceKind.PLANT_FRIENDLY.name, "#2EBD7E"),
        Expression.stop(PlaceKind.SUPERMARKET.name, "#8ED7FF"),
        Expression.stop(PlaceKind.RESTAURANT.name, "#7CE7B2"),
    )

private fun zoomForRadius(radiusKm: Double): Double =
    if (radiusKm >= 10.0) 11.35 else 12.35

private const val MAP_TAG = "NearbyMap"
private const val ORIGIN_SOURCE_ID = "sproutly-origin-source"
private const val ORIGIN_HALO_LAYER_ID = "sproutly-origin-halo"
private const val ORIGIN_LAYER_ID = "sproutly-origin"
private const val PLACES_SOURCE_ID = "sproutly-places-source"
private const val PLACE_HALO_LAYER_ID = "sproutly-place-halo"
private const val PLACE_LAYER_ID = "sproutly-place"
private const val SELECTED_PLACE_SOURCE_ID = "sproutly-selected-place-source"
private const val SELECTED_PLACE_LAYER_ID = "sproutly-selected-place"
private const val PROPERTY_ID = "id"
private const val PROPERTY_KIND = "kind"
private const val TAP_TARGET_PX = 28f

// ── Projection ───────────────────────────────────────────────────────────────

/**
 * Equirectangular projection centred on [origin]. A half-extent of `H` km maps
 * to half the canvas width/height — so the whole canvas covers a `2H × 2H`
 * square in geographic space (square in km, not in degrees).
 */
private class MapProjection(
    val width: Float,
    val height: Float,
    val origin: GeoPoint,
    val halfExtentKm: Double,
) {
    private val kmPerDegLat = 110.574
    private val kmPerDegLng = 111.320 * cos(origin.lat * PI / 180.0)
    private val pxPerKmX = (width / 2f) / halfExtentKm.toFloat()
    private val pxPerKmY = (height / 2f) / halfExtentKm.toFloat()

    fun toOffset(lat: Double, lng: Double): Offset {
        val dxKm = ((lng - origin.lng) * kmPerDegLng).toFloat()
        val dyKm = ((lat - origin.lat) * kmPerDegLat).toFloat()
        return Offset(
            x = width / 2f + dxKm * pxPerKmX,
            y = height / 2f - dyKm * pxPerKmY,
        )
    }

    fun kmToPxX(km: Double): Float = (km.toFloat() * pxPerKmX)
    fun kmToPxY(km: Double): Float = (km.toFloat() * pxPerKmY)
}

// ── Drawing ──────────────────────────────────────────────────────────────────

private fun DrawScope.drawBase() {
    drawRect(BgSurface, size = size)
}

private fun DrawScope.drawGridStreets(proj: MapProjection) {
    val stepPx = proj.kmToPxX(0.4).coerceAtLeast(8f) // a line every ~400m
    val gridColor = DividerColor.copy(alpha = 0.55f)

    val cx = size.width / 2f
    val cy = size.height / 2f

    // Vertical lines, centred on the origin so the grid moves with the camera
    var offsetX = 0f
    while (cx - offsetX > 0f || cx + offsetX < size.width) {
        if (offsetX == 0f) {
            drawLine(DividerColor.copy(alpha = 0.85f), Offset(cx, 0f), Offset(cx, size.height), 1.6f)
        } else {
            drawLine(gridColor, Offset(cx - offsetX, 0f), Offset(cx - offsetX, size.height), 0.8f)
            drawLine(gridColor, Offset(cx + offsetX, 0f), Offset(cx + offsetX, size.height), 0.8f)
        }
        offsetX += stepPx
    }

    // Horizontal lines
    var offsetY = 0f
    while (cy - offsetY > 0f || cy + offsetY < size.height) {
        if (offsetY == 0f) {
            drawLine(DividerColor.copy(alpha = 0.75f), Offset(0f, cy), Offset(size.width, cy), 1.3f)
        } else {
            drawLine(gridColor, Offset(0f, cy - offsetY), Offset(size.width, cy - offsetY), 0.8f)
            drawLine(gridColor, Offset(0f, cy + offsetY), Offset(size.width, cy + offsetY), 0.8f)
        }
        offsetY += stepPx
    }
}

/**
 * Hand-rough polygons for Madrid's two iconic green spaces. Coordinates are
 * approximate centres + a handful of vertices to give recognisable shapes.
 */
private fun DrawScope.drawParks(proj: MapProjection) {
    // Parque del Retiro — east of centre, elongated north-south
    val retiroCentre = proj.toOffset(40.4151, -3.6844)
    val retiroW = proj.kmToPxX(0.55)
    val retiroH = proj.kmToPxY(0.95)
    drawPath(
        path = blobPath(retiroCentre, retiroW, retiroH, jitter = 0.12f, seed = 41),
        color = LeafDeep,
    )
    drawPath(
        path = blobPath(retiroCentre, retiroW * 0.85f, retiroH * 0.85f, jitter = 0.08f, seed = 27),
        color = LeafGreen.copy(alpha = 0.22f),
    )

    // Casa de Campo — large irregular park west of centre
    val casaCentre = proj.toOffset(40.4170, -3.7470)
    val casaW = proj.kmToPxX(1.2)
    val casaH = proj.kmToPxY(1.8)
    drawPath(
        path = blobPath(casaCentre, casaW, casaH, jitter = 0.18f, seed = 11),
        color = LeafDeep,
    )
    drawPath(
        path = blobPath(casaCentre, casaW * 0.78f, casaH * 0.78f, jitter = 0.12f, seed = 19),
        color = LeafGreen.copy(alpha = 0.18f),
    )

    // Madrid Río strip (along the Manzanares) — thin green ribbon
    val rioTop = proj.toOffset(40.4350, -3.7230)
    val rioBot = proj.toOffset(40.3900, -3.7210)
    val ribbon = Path().apply {
        moveTo(rioTop.x - proj.kmToPxX(0.08), rioTop.y)
        lineTo(rioTop.x + proj.kmToPxX(0.08), rioTop.y)
        lineTo(rioBot.x + proj.kmToPxX(0.10), rioBot.y)
        lineTo(rioBot.x - proj.kmToPxX(0.10), rioBot.y)
        close()
    }
    drawPath(ribbon, color = LeafDeep.copy(alpha = 0.75f))
}

private fun DrawScope.drawRiver(proj: MapProjection) {
    // Manzanares river — slight curve through the west
    val a = proj.toOffset(40.4400, -3.7250)
    val b = proj.toOffset(40.4200, -3.7280)
    val c = proj.toOffset(40.4000, -3.7210)
    val d = proj.toOffset(40.3800, -3.7170)
    val river = Path().apply {
        moveTo(a.x, a.y)
        cubicTo(a.x - 8f, (a.y + b.y) / 2f, b.x + 8f, (a.y + b.y) / 2f, b.x, b.y)
        cubicTo(b.x - 6f, (b.y + c.y) / 2f, c.x + 6f, (b.y + c.y) / 2f, c.x, c.y)
        cubicTo(c.x + 8f, (c.y + d.y) / 2f, d.x - 8f, (c.y + d.y) / 2f, d.x, d.y)
    }
    drawPath(
        path = river,
        color = Color(0xFF1F4D5E),
        style = Stroke(width = proj.kmToPxX(0.08).coerceAtLeast(3f), cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawOriginRing() {
    val centre = Offset(size.width / 2f, size.height / 2f)
    // Outer translucent halo
    drawCircle(LeafMint.copy(alpha = 0.18f), radius = 22f, center = centre)
    drawCircle(LeafMint.copy(alpha = 0.32f), radius = 13f, center = centre)
    drawCircle(Color.White, radius = 9f, center = centre)
    drawCircle(LeafMint, radius = 6f, center = centre)
}

private fun DrawScope.drawPins(
    proj: MapProjection,
    places: List<Place>,
    selectedPlaceId: String?,
) {
    val mapBounds = Rect(Offset.Zero, Size(size.width, size.height))
    places.forEach { place ->
        val lat = place.lat ?: return@forEach
        val lng = place.lng ?: return@forEach
        val pos = proj.toOffset(lat, lng)
        if (!mapBounds.contains(pos)) return@forEach
        val selected = place.id == selectedPlaceId
        drawPin(pos, colorForKind(place.kind, selected), selected)
    }
}

private fun DrawScope.drawPin(tip: Offset, color: Color, selected: Boolean) {
    val headRadius = if (selected) 13f else 10f
    val headCentre = Offset(tip.x, tip.y - headRadius * 1.6f)

    // Shadow under tip
    drawOval(
        color = Color.Black.copy(alpha = 0.32f),
        topLeft = Offset(tip.x - headRadius * 0.6f, tip.y - 1.5f),
        size = Size(headRadius * 1.2f, 4f),
    )

    // Body: triangle from tip up to a line beneath the head
    val body = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(headCentre.x - headRadius * 0.78f, headCentre.y + headRadius * 0.55f)
        lineTo(headCentre.x + headRadius * 0.78f, headCentre.y + headRadius * 0.55f)
        close()
    }
    drawPath(body, color)
    drawCircle(color, radius = headRadius, center = headCentre)

    // White outline + dot
    drawCircle(
        Color.White,
        radius = headRadius,
        center = headCentre,
        style = Stroke(width = if (selected) 2.6f else 1.8f),
    )
    drawCircle(Color.White, radius = headRadius * 0.36f, center = headCentre)
}

private fun colorForKind(kind: PlaceKind, selected: Boolean): Color {
    if (selected) return Color(0xFFB7F7CE)
    return when (kind) {
        PlaceKind.FULLY_PLANT_BASED -> Color(0xFF6EDA8A)
        PlaceKind.PLANT_FRIENDLY -> Color(0xFF2EBD7E)
        PlaceKind.SUPERMARKET -> Color(0xFF8ED7FF)
        PlaceKind.RESTAURANT -> Color(0xFF7CE7B2)
    }
}

/**
 * Builds a smooth closed blob path with [vertexCount] points spaced around an
 * ellipse, each perturbed by [jitter] (0..1) using a deterministic LCG seeded
 * by [seed]. We connect midpoints with quadratic curves through each vertex
 * so the parks look hand-shaped rather than perfect ellipses.
 */
private fun blobPath(
    centre: Offset,
    radiusX: Float,
    radiusY: Float,
    jitter: Float,
    seed: Int,
    vertexCount: Int = 14,
): Path {
    var state = seed.toLong() and 0xFFFFFFFFL
    fun next(): Float {
        state = (state * 1103515245L + 12345L) and 0x7FFFFFFFL
        return state.toFloat() / 0x7FFFFFFF.toFloat()
    }
    val pts = (0 until vertexCount).map { i ->
        val angle = (i.toFloat() / vertexCount) * (2f * PI.toFloat())
        val wobble = 1f + (next() - 0.5f) * 2f * jitter
        Offset(
            x = centre.x + cos(angle.toDouble()).toFloat() * radiusX * wobble,
            y = centre.y + sin(angle.toDouble()).toFloat() * radiusY * wobble,
        )
    }
    val mids = pts.indices.map { i ->
        val a = pts[i]
        val b = pts[(i + 1) % pts.size]
        Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)
    }
    return Path().apply {
        moveTo(mids.last().x, mids.last().y)
        for (i in pts.indices) {
            val ctrl = pts[i]
            val end = mids[i]
            quadraticBezierTo(ctrl.x, ctrl.y, end.x, end.y)
        }
        close()
    }
}
