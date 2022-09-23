package com.preonboarding.locationhistory.shared

import android.content.Context
import android.content.SharedPreferences

object SharedPreferences {
    private const val PREFS_NAME = "prefs_name"
    private const val TIME_KEY = "time_key"
    private const val FIRST_CHECK_KEY = "first_key"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var setTime: String?
        get() = prefs.getString(TIME_KEY, "600000")
        set(value) = prefs.edit().putString(TIME_KEY, value).apply()

    var isFirst: Boolean
        get() = prefs.getBoolean(FIRST_CHECK_KEY, true)
        set(value) = prefs.edit().putBoolean(FIRST_CHECK_KEY, value).apply()
}