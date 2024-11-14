package com.silas.fake.move

import LatLng
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileWriter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class LocationData(
    override val latitude: Double,
    override val longitude: Double,
    val time: Long,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val altitude: Double,
) : LatLng {
    override fun toString(): String {
        return "lat: $latitude, lon: $longitude, time: $time"
    }
}

interface LocationCallback {
    fun onLocationChanged(location: LocationData)
}

object MockLocations {
    var list = mutableListOf<LocationData>()
}

object LocationUtils {
    private var currentRunning = false
    private var thread: Thread? = null;
    private var currentLocationManager: LocationManager? = null
    private const val provider = "gps"
    fun stopMockLocation(context: Context) {
    }

    fun startMockLocation(context: Context, list: List<LocationData>) {
    }

    fun stopPlayLocationIfNeed() {
        currentLocationManager?.removeTestProvider(provider)
        currentLocationManager = null;
        currentRunning = false
        thread?.interrupt()
        thread = null
    }

    fun playLocation2(
        context: Context,
        viewModel: LocationViewModel,
        list: List<LocationData>
    ) {
        val theList = MockLocations.list
        theList.clear()
        theList.addAll(list)
        context.startService(Intent(context, MockLocationService::class.java))
    }


    @SuppressLint("WrongConstant")
    fun playLocation(
        context: Context,
        viewModel: LocationViewModel,
        list: List<LocationData>
    ) {
        stopPlayLocationIfNeed()
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null) {
            try {
                locationManager.addTestProvider(
                    provider,
                    false,
                    true,
                    false,
                    false,
                    true,
                    true,
                    true,
                    3,
                    1
                )
                locationManager.setTestProviderEnabled(provider, true)
                currentLocationManager = locationManager
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    "You did not select 'Mock location app' !",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        currentRunning = true
        val ttt = Thread {
            try {
                for (item in list) {
                    viewModel.addItem(item)

                    currentLocationManager?.apply {
                        val mockLocation = Location(provider).apply {
                            latitude = item.latitude
                            longitude = item.longitude
                            accuracy = item.accuracy
                            speed = item.speed
                            bearing = item.bearing
                            altitude = item.altitude
                            time = System.currentTimeMillis()
                            elapsedRealtimeNanos = System.nanoTime()
                        }
                        setTestProviderLocation(provider, mockLocation)
                    }
                    Thread.sleep(1000L)
                    println("XXX.sendItem....")
                    if (!currentRunning) {
                        println("XXX.thread.interrupt")
                        break
                    }
                }
            } catch (_: Exception) {

            }

        }
        ttt.start()
        thread = ttt
    }

    fun getLongitudeSpanAt(latitude: Double, meters: Double): Double {
        val latitudeInRadians = latitude * PI / 180

        val metersPerLongitudeDegreeAtEquator = 111320.0
        val longitudeInMeters =
            meters / (cos(latitudeInRadians) * metersPerLongitudeDegreeAtEquator)

        return longitudeInMeters
    }

    fun vincenty(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val a = 6378137.0
        val f = 1 / 298.257223563
        val b = (1 - f) * a

        val φ1 = Math.toRadians(lat1)
        val λ1 = Math.toRadians(lon1)
        val φ2 = Math.toRadians(lat2)
        val λ2 = Math.toRadians(lon2)

        val L = λ2 - λ1
        val U1 = atan((1 - f) * tan(φ1))
        val U2 = atan((1 - f) * tan(φ2))

        val sinU1 = sin(U1)
        val cosU1 = cos(U1)
        val sinU2 = sin(U2)
        val cosU2 = cos(U2)

        var sinσ: Double
        var cosσ: Double
        var σ: Double
        var sinα: Double
        var cos2α: Double
        var cos2σm: Double
        var C: Double

        var λ = L
        var iterCount = 0
        do {
            val sinλ = sin(λ)
            val cosλ = cos(λ)
            sinσ = sqrt((cosU2 * sinλ).pow(2) + (cosU1 * sinU2 - sinU1 * cosU2 * cosλ).pow(2))
            cosσ = sinU1 * sinU2 + cosU1 * cosU2 * cosλ
            σ = atan2(sinσ, cosσ)

            sinα = cosU1 * cosU2 * sinλ / sinσ
            cos2α = 1 - sinα.pow(2)
            cos2σm = cosσ - 2 * sinU1 * sinU2 / cos2α

            C = f / 16 * cos2α * (4 + f * (4 - 3 * cos2α))

            λ =
                L + (1 - C) * f * sinα * (σ + C * sinσ * (cos2σm + C * cosσ * (-1 + 2 * cos2σm.pow(2))))
        } while (abs(λ) > 1e-12 && iterCount++ < 100)

        val u2 = cos2α * (a.pow(2) - b.pow(2)) / (b.pow(2))
        val A = 1 + u2 / 16384 * (4096 + u2 * (-768 + u2 * (320 - 175 * u2)))
        val B = u2 / 1024 * (256 + u2 * (-128 + u2 * (74 - 47 * u2)))

        val deltaσ =
            B * sinσ * (cos2σm + B / 4 * (cosσ * (-1 + 2 * cos2σm.pow(2))) - B / 6 * cos2σm * (-3 + 4 * sinσ.pow(
                2
            )) * (-3 + 4 * cos2σm.pow(2)))

        return b * A * (σ - deltaσ)
    }

    fun totalDistance(latLngList: List<LatLng>): Double {
        var totalDistance = 0.0
        for (i in 0 until latLngList.size - 1) {
            totalDistance += vincenty(
                latLngList[i].latitude, latLngList[i].longitude,
                latLngList[i + 1].latitude, latLngList[i + 1].longitude
            )
        }
        return totalDistance
    }

}

object MyLocationManager {
    private const val TAG = "MyLocationManager"
    private var locationListener: LocationListener? = null

    fun listFiles(context: Context): List<File> {
        val file = File(context.filesDir.absolutePath + "/locations")
        val files = file.listFiles() ?: return emptyList()
        return files.filter { it.extension == "loc" }
    }

    fun loadFromFile(file: File): List<LocationData> {
        try {
            val lines = file.readLines()
            val list = lines.map { line ->
                val parts = line.split(",")
                LocationData(
                    latitude = parts[0].toDouble(),
                    longitude = parts[1].toDouble(),
                    time = parts[2].toLong(),
                    speed = parts[3].toFloat(),
                    bearing = parts[4].toFloat(),
                    accuracy = parts[5].toFloat(),
                    altitude = parts[6].toDouble(),
                )
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
                    writer.write("${item.latitude},${item.longitude},${item.time},${item.speed},${item.bearing},${item.accuracy},${item.altitude}")
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
            val locationData = LocationData(
                location.latitude,
                location.longitude,
                location.time,
                location.speed,
                location.bearing,
                location.accuracy,
                location.altitude
            )
            callback.onLocationChanged(locationData)
        }
        val providers = locationManager.getProviders(true);
        Log.i(TAG, "startRecord: $providers")
        if (PermissionUtils.hasLocationPermission(context)) {
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

}