package com.sproutly.app

import android.app.Application
import com.sproutly.app.core.network.SupabaseClientProvider

class SproutlyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.init()
    }
}
