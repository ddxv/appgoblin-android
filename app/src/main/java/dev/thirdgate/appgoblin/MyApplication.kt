package dev.thirdgate.appgoblin

import android.app.Application

import dev.openattribution.sdk.OpenAttribution

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize the OpenAttribution SDK, replace with your domain
        OpenAttribution.initialize(this, "https://oa.thirdgate.dev")
    }
}