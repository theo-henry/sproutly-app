package com.sproutly.app.nearby.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.sproutly.app.core.config.AppConfig
import com.sproutly.app.core.design.BgDeep
import com.sproutly.app.core.design.TextPrimary
import com.sproutly.app.core.network.OsmStyle
import com.sproutly.app.nearby.NearbyUiState
import com.sproutly.app.nearby.model.Place
import com.sproutly.app.nearby.model.PlaceKind
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun NearbyMap(
    state: NearbyUiState,
    selectedPlaceId: String?,
    onPlaceSelected: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    var map by remember { mutableStateOf<MapLibreMap?>(null) }

    Box(modifier = modifier.clip(RoundedCornerShape(24.dp))) {
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { mapLibre ->
                        mapLibre.setStyle(Style.Builder().fromJson(OsmStyle.JSON))
                        mapLibre.uiSettings.isCompassEnabled = false
                        mapLibre.uiSettings.isLogoEnabled = false
                        mapLibre.uiSettings.isAttributionEnabled = false
                        mapLibre.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(AppConfig.MADRID_LAT, AppConfig.MADRID_LNG),
                                12.5,
                            )
                        )
                        map = mapLibre
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        Text(
            text = AppConfig.MAP_ATTRIBUTION,
            color = TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(BgDeep.copy(alpha = 0.66f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }

    // Markers + listener — refreshed when the place set or selection changes.
    LaunchedEffect(map, state.places, selectedPlaceId) {
        map?.let {
            it.setOnMarkerClickListener { marker ->
                val place = state.places.firstOrNull { p -> p.id == marker.snippet }
                if (place != null) { onPlaceSelected(place); true } else false
            }
            updateMarkers(context, it, state, selectedPlaceId)
        }
    }

    // Camera follows origin (initial load + locate-me).
    LaunchedEffect(map, state.origin) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(state.origin.lat, state.origin.lng),
                13.0,
            )
        )
    }

    // Camera flies to selected place at a moderate zoom.
    LaunchedEffect(map, selectedPlaceId, state.places) {
        val place = state.places.firstOrNull { it.id == selectedPlaceId } ?: return@LaunchedEffect
        val lat = place.lat ?: return@LaunchedEffect
        val lng = place.lng ?: return@LaunchedEffect
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15.5))
    }
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context).apply { onCreate(null) } }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }
    return mapView
}

private fun updateMarkers(
    context: Context,
    map: MapLibreMap,
    state: NearbyUiState,
    selectedPlaceId: String?,
) {
    val iconFactory = IconFactory.getInstance(context)
    map.clear()

    map.addMarker(
        MarkerOptions()
            .position(LatLng(state.origin.lat, state.origin.lng))
            .title("You are here")
            .icon(iconFactory.fromBitmap(userDotBitmap()))
    )

    state.places.forEach { place ->
        val lat = place.lat ?: return@forEach
        val lng = place.lng ?: return@forEach
        val selected = place.id == selectedPlaceId
        val color = if (selected) AMBER_SELECTED else colorFor(place.kind)

        map.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .title(place.name)
                .snippet(place.id)
                .icon(iconFactory.fromBitmap(pinBitmap(color, selected = selected)))
        )
    }
}

private val AMBER_SELECTED = Color.rgb(255, 188, 66)

private fun colorFor(kind: PlaceKind): Int = when (kind) {
    PlaceKind.FULLY_PLANT_BASED -> Color.rgb(110, 218, 138)
    PlaceKind.PLANT_FRIENDLY -> Color.rgb(46, 189, 126)
    PlaceKind.SUPERMARKET -> Color.rgb(246, 198, 105)
    PlaceKind.RESTAURANT -> Color.rgb(124, 231, 178)
}

/**
 * Teardrop map pin. MarkerOptions anchors the bitmap by its geometric center, so
 * the bitmap is drawn 2× the pin's height with the pin in the top half — that
 * puts the tip exactly at the center, which then sits on the geographic point.
 */
private fun pinBitmap(color: Int, selected: Boolean): Bitmap {
    val width = if (selected) 52 else 44
    val pinHeight = (width * 1.35f).toInt()
    val totalHeight = pinHeight * 2

    val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val cx = width / 2f
    val headRadius = width * 0.4f
    val headCy = headRadius + 3f
    val tipY = pinHeight.toFloat() - 1f

    // Soft drop shadow under the tip.
    paint.color = Color.argb(110, 0, 0, 0)
    canvas.drawOval(
        cx - headRadius * 0.55f, tipY - 2f,
        cx + headRadius * 0.55f, tipY + 5f,
        paint,
    )

    // Pin body: head circle + downward triangle to the tip.
    paint.color = color
    val body = Path().apply {
        moveTo(cx, tipY)
        lineTo(cx - headRadius * 0.78f, headCy + headRadius * 0.55f)
        lineTo(cx + headRadius * 0.78f, headCy + headRadius * 0.55f)
        close()
    }
    canvas.drawPath(body, paint)
    canvas.drawCircle(cx, headCy, headRadius, paint)

    // White outline so the pin reads against any tile color.
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = if (selected) 4f else 2.5f
    paint.color = Color.WHITE
    canvas.drawCircle(cx, headCy, headRadius, paint)
    paint.style = Paint.Style.FILL

    // White inner dot.
    paint.color = Color.WHITE
    canvas.drawCircle(cx, headCy, headRadius * 0.36f, paint)

    return bitmap
}

/** Soft "you are here" dot with a translucent halo. */
private fun userDotBitmap(): Bitmap {
    val size = 44
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val r = size / 2f

    paint.color = Color.argb(70, 124, 231, 178)
    canvas.drawCircle(r, r, r * 0.95f, paint)

    paint.color = Color.WHITE
    canvas.drawCircle(r, r, r * 0.48f, paint)

    paint.color = Color.rgb(124, 231, 178)
    canvas.drawCircle(r, r, r * 0.32f, paint)
    return bitmap
}
