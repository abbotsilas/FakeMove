package com.silas.fake.move

import android.content.Context

private const val CONFIG = "config"

object ConfigInfo {
    var postInterval: Long = 200
    var speed: Float = 1.0f
    var shakeMeters: Float = 0.4f
    private var hasLoad = false

    fun loadIfNeed(context: Context) {
        if (hasLoad) {
            return
        }
        val prefs = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        postInterval = prefs.getLong("postInterval", postInterval)
        speed = prefs.getFloat("speed", speed)
        shakeMeters = prefs.getFloat("shakeMeters", shakeMeters)
        hasLoad = true
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong("postInterval", postInterval)
        editor.putFloat("speed", speed)
        editor.putFloat("shakeMeters", shakeMeters)
        editor.apply()

    }
}