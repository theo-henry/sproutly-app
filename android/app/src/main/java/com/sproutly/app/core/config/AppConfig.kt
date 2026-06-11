package com.sproutly.app.core.config

import com.sproutly.app.BuildConfig

object AppConfig {
    val supabaseUrl: String = BuildConfig.SUPABASE_URL
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
    val demoEmail: String = BuildConfig.DEMO_EMAIL
    val demoPassword: String = BuildConfig.DEMO_PASSWORD
    val mapStyleUrl: String = BuildConfig.MAP_STYLE_URL
    val overpassEndpoint: String = BuildConfig.OVERPASS_ENDPOINT

    const val AVATAR_BUCKET = "avatars"
    const val MAP_ATTRIBUTION = "© OpenStreetMap contributors"
    const val MAP_USER_AGENT = "SproutlyAndroid/0.1 contact: sproutly@example.com"

    const val MADRID_LAT = 40.4168
    const val MADRID_LNG = -3.7038
}
