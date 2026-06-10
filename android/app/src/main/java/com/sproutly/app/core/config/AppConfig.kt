package com.sproutly.app.core.config

import com.sproutly.app.BuildConfig

object AppConfig {
    val supabaseUrl: String = BuildConfig.SUPABASE_URL
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
    val demoEmail: String = BuildConfig.DEMO_EMAIL
    val demoPassword: String = BuildConfig.DEMO_PASSWORD

    const val AVATAR_BUCKET = "avatars"
}
