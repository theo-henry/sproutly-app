package com.sproutly.app.core.network

/**
 * Inline MapLibre style JSON that renders real OpenStreetMap raster tiles —
 * no API key needed. The previous default (`demotiles.maplibre.org/style.json`)
 * is a MapLibre demo style that only shows country outlines at city zoom, which
 * made the Nearby map look broken.
 *
 * Attribution: rendered separately on top of the map by NearbyMap.kt.
 * Usage policy: keep traffic low — fine for an MVP/course project.
 */
object OsmStyle {
    const val JSON: String = """
{
  "version": 8,
  "name": "Sproutly OSM",
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": [
        "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
        "https://b.tile.openstreetmap.org/{z}/{x}/{y}.png",
        "https://c.tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "minzoom": 0,
      "maxzoom": 19,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    { "id": "background", "type": "background", "paint": { "background-color": "#0E1E15" } },
    { "id": "osm", "type": "raster", "source": "osm", "paint": { "raster-saturation": -0.35, "raster-contrast": 0.05, "raster-brightness-min": 0.05 } }
  ]
}
"""
}
