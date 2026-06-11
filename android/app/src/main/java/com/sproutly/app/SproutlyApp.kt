package com.sproutly.app

import android.app.Application
import com.sproutly.app.auth.data.DemoAccountStore
import com.sproutly.app.core.network.SupabaseClientProvider
import org.maplibre.android.MapLibre

class SproutlyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        DemoAccountStore.init(this)
        SupabaseClientProvider.init()
    }
}
