package com.sproutly.app.core.network

import com.sproutly.app.core.config.AppConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp

object SupabaseClientProvider {
    @Volatile
    private var _client: SupabaseClient? = null

    val client: SupabaseClient
        get() = _client ?: error("SupabaseClientProvider.init() not called")

    fun init() {
        if (_client != null) return
        _client = createSupabaseClient(
            supabaseUrl = AppConfig.supabaseUrl,
            supabaseKey = AppConfig.supabaseAnonKey,
        ) {
            httpEngine = OkHttp.create()
            install(Auth) {
                autoLoadFromStorage = false
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }
}
