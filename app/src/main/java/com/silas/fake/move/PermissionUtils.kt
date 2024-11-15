package com.silas.fake.move

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlin.math.pow
import kotlin.math.roundToInt

const val DRAW_SCALE_FACTOR = 111320

fun Double.round(i: Int): String {
    if (isNaN()) {
        return "NaN"
    }
    val factor = 10.0.pow(i.toDouble())
    return ((this * factor).roundToInt() / factor).toString()
}

fun Float.round(i: Int): String {
    if (isNaN()) {
        return "NaN"
    }
    val factor = 10.0.pow(i.toDouble())
    return ((this * factor).roundToInt() / factor).toString()
}

object PermissionUtils {
    fun hasLocationPermission(context: Context): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        println("hasLocationPermission=$hasPermission")
        return hasPermission
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }
}