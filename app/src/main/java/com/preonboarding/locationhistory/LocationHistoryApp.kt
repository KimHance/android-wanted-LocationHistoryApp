package com.preonboarding.locationhistory

import android.app.Application
import com.preonboarding.locationhistory.shared.SharedPreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LocationHistoryApp : Application() {
    companion object {
        lateinit var prefs: SharedPreferences
    }

    override fun onCreate() {
        prefs = SharedPreferences
        prefs.init(this)
        super.onCreate()
    }

}