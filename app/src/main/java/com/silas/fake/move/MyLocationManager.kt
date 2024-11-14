package com.silas.fake.move

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileWriter

@Serializable
data class LocationData(val latitude: Double, val longitude: Double, val time: Long) {
    override fun toString(): String {
        return "lat: $latitude, lon: $longitude, time: $time"
    }
}

interface LocationCallback {
    fun onLocationChanged(location: LocationData)
}

object MyLocationManager {
    private const val TAG = "MyLocationManager"
    private var locationListener: LocationListener? = null

    fun listFiles(context: Context): List<File> {
        val file = File(context.filesDir.absolutePath + "/locations")
        val files = file.listFiles()
        return files!!.filter { it.extension == "loc" }
    }

    fun loadFromFile(context: Context, file: File): List<LocationData> {
        try {
            val lines = file.readLines()
            val list = lines.map { line ->
                val parts = line.split(",")
                LocationData(parts[0].toDouble(), parts[1].toDouble(), parts[2].toLong())
            }
            Log.i(TAG, "loadFromFile: load success")
            return list
        } catch (e: Exception) {
            Log.e(TAG, "loadFromFile: load failed", e)
        }
        return emptyList()
    }

    fun saveToFile(context: Context, list: List<LocationData>, name: String) {
        val fileName = "$name.loc"
        val filePath = context.filesDir.absolutePath + "/locations/" + fileName
        try {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.createNewFile()
            FileWriter(file).use { writer ->
                for (item in list) {
                    writer.write("${item.latitude},${item.longitude},${item.time}")
                    writer.write("\n")
                }
            }
            Log.i(TAG, "saveToFile: save success")
        } catch (e: Exception) {
            Log.e(TAG, "saveToFile: save failed", e)
        }
    }

    fun stopRecord(context: Context) {
        println("stopRecord")
        val listener = locationListener
        if (listener != null) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.removeUpdates(listener)
            locationListener = null
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecord(context: Context, callback: LocationCallback): Boolean {
        println("startRecord")
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = LocationListener { location ->
            val locationData = LocationData(location.latitude, location.longitude, location.time)
            callback.onLocationChanged(locationData)
        }
        val providers = locationManager.getProviders(true);
        Log.i(TAG, "startRecord: $providers")
        if (hasPermission(context)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                listener
            )
            locationListener = listener
            return true
        } else {
            return false
        }
    }


    fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}