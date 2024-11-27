package com.silas.fake.move

import LatLng
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileWriter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
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
                    viewModel.addLocationItem(item)

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

    fun distance(lat1a: Double, lon1a: Double, lat2a: Double, lon2a: Double): Double {
        val MAXITERS = 20
        val lat1 = lat1a * 0.017453292519943295
        val lat2 = lat2a * 0.017453292519943295
        val lon1 = lon1a * 0.017453292519943295
        val lon2 = lon2a * 0.017453292519943295
        val a = 6378137.0
        val b = 6356752.3142
        val f = (a - b) / a
        val aSqMinusBSqOverBSq = (a * a - b * b) / (b * b)
        val L = lon2 - lon1
        var A = 0.0
        val U1 = atan((1.0 - f) * tan(lat1))
        val U2 = atan((1.0 - f) * tan(lat2))
        val cosU1 = cos(U1)
        val cosU2 = cos(U2)
        val sinU1 = sin(U1)
        val sinU2 = sin(U2)
        val cosU1cosU2 = cosU1 * cosU2
        val sinU1sinU2 = sinU1 * sinU2
        var sigma = 0.0
        var deltaSigma = 0.0
        var cosSqAlpha = 0.0
        var cos2SM = 0.0
        var cosSigma = 0.0
        var sinSigma = 0.0
        var cosLambda = 0.0
        var sinLambda = 0.0
        var lambda = L

        for (iter in 0 until MAXITERS) {
            val lambdaOrig = lambda
            cosLambda = cos(lambda)
            sinLambda = sin(lambda)
            val t1 = cosU2 * sinLambda
            val t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
            val sinSqSigma = t1 * t1 + t2 * t2
            sinSigma = sqrt(sinSqSigma)
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda
            sigma = atan2(sinSigma, cosSigma)
            val sinAlpha = if (sinSigma == 0.0) 0.0 else cosU1cosU2 * sinLambda / sinSigma
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha
            cos2SM = if (cosSqAlpha == 0.0) 0.0 else cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha
            val uSquared = cosSqAlpha * aSqMinusBSqOverBSq
            A =
                1.0 + uSquared / 16384.0 * (4096.0 + uSquared * (-768.0 + uSquared * (320.0 - 175.0 * uSquared)))
            val B =
                uSquared / 1024.0 * (256.0 + uSquared * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)))
            val C = f / 16.0 * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha))
            val cos2SMSq = cos2SM * cos2SM
            deltaSigma =
                B * sinSigma * (cos2SM + B / 4.0 * (cosSigma * (-1.0 + 2.0 * cos2SMSq) - B / 6.0 * cos2SM * (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0 + 4.0 * cos2SMSq)))
            lambda =
                L + (1.0 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SM + C * cosSigma * (-1.0 + 2.0 * cos2SM * cos2SM)))
            val delta = (lambda - lambdaOrig) / lambda
            if (abs(delta) < 1.0E-12) {
                break
            }
        }

        val distance = (b * A * (sigma - deltaSigma))
        return distance
    }

    fun totalDistance(latLngList: List<LatLng>): Double {
        var totalDistance = 0.0
        for (i in 0 until latLngList.size - 1) {
            var dis = distance(
                latLngList[i].latitude, latLngList[i].longitude,
                latLngList[i + 1].latitude, latLngList[i + 1].longitude
            )
            if (dis.isNaN()) {
                dis = 0.0
            }
            totalDistance += dis
        }
        return totalDistance
    }

}

object MyLocationManager {
    private const val TAG = "MyLocationManager"
    private var locationListener: LocationListener? = null
    fun sdCardFileDir(): File {
        val rootDir = Environment.getExternalStorageDirectory()
        val locFilesDir = File(rootDir, "locfiles")
        return locFilesDir
    }

    fun listSDCardFiles(context: Context): List<File> {
        val file = sdCardFileDir()
        val files = file.listFiles() ?: return emptyList()
        return files.filter { it.name.contains(".loc") }
    }

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


fun Long.toTime(): String {
    val sb = StringBuilder()
    val hours = this / 3600000
    val minutes = (this - hours * 3600000) / 60000
    val seconds = (this - hours * 3600000 - minutes * 60000) / 1000
    if (hours < 10) sb.append("0")
    sb.append(hours).append(":")
    if (minutes < 10) sb.append("0")
    sb.append(minutes).append(":")
    if (seconds < 10) sb.append("0")
    sb.append(seconds)
    return sb.toString()
}