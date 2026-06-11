package com.sproutly.app.nearby.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
                        // Sensible starting camera so the user sees Madrid immediately,
                        // before the first place query resolves.
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

    LaunchedEffect(map, state.origin, state.places, selectedPlaceId) {
        map?.let {
            it.setOnMarkerClickListener { marker ->
                val place = state.places.firstOrNull { p -> p.id == marker.snippet }
                if (place != null) { onPlaceSelected(place); true } else false
            }
            updateMarkers(context, it, state, selectedPlaceId)
        }
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
    val origin = LatLng(state.origin.lat, state.origin.lng)
    val iconFactory = IconFactory.getInstance(context)
    map.clear()

    map.addMarker(
        MarkerOptions()
            .position(origin)
            .title("You are here")
            .icon(iconFactory.fromBitmap(dotBitmap(Color.rgb(124, 231, 178), 38, stroke = true)))
    )

    state.places.forEach { place ->
        val lat = place.lat ?: return@forEach
        val lng = place.lng ?: return@forEach
        val selected = place.id == selectedPlaceId
        val color = when (place.kind) {
            PlaceKind.FULLY_PLANT_BASED -> Color.rgb(168, 235, 142)
            PlaceKind.PLANT_FRIENDLY -> Color.rgb(109, 214, 154)
            PlaceKind.SUPERMARKET -> Color.rgb(246, 198, 105)
            PlaceKind.RESTAURANT -> Color.rgb(149, 216, 191)
        }

        map.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .title(place.name)
                .snippet(place.id)
                .icon(iconFactory.fromBitmap(dotBitmap(color, if (selected) 42 else 30, stroke = selected)))
        )
    }

    map.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13.0))
}

private fun dotBitmap(color: Int, size: Int, stroke: Boolean): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val radius = size / 2f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    if (stroke) {
        paint.color = Color.WHITE
        canvas.drawCircle(radius, radius, radius * 0.48f, paint)
    }

    paint.color = color
    canvas.drawCircle(radius, radius, if (stroke) radius * 0.34f else radius * 0.42f, paint)

    if (!stroke) {
        paint.color = Color.argb(72, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawCircle(radius, radius, radius * 0.48f, paint)
    }
    return bitmap
}
