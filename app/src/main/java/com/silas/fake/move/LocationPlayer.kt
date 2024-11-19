package com.silas.fake.move

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.cos

class LocationPlayer(
    private val context: Context,
    private val baseList: List<LocationData>,
    private val playModel: LocationViewModel,
    private val mustMock: Boolean = false
) {

    private var currentRunning = false
    private var thread: Thread? = null
    private var currentLocationManager: LocationManager? = null
    private val provider = "gps"
    private var startTime = System.currentTimeMillis()
    private val firstTime = baseList[0].time
    private val totalDistance = LocationUtils.totalDistance(baseList)

    @SuppressLint("WrongConstant")
    fun start(context: Activity): Boolean {
        val prepared = prepare()
        println("XXX.prepared=$prepared")
        if (!prepared) {
            return false
        }
        currentRunning = true
        val ttt = Thread {
            val postInterval = ConfigInfo.postInterval
            try {
                startTime = System.currentTimeMillis()
                while (currentRunning) {
                    val nextLocation = fetchNextLocation()
                    if (nextLocation == null) {
                        currentRunning = false
                        break
                    }
                    playModel.addLocationItem(nextLocation)
                    postLocation(nextLocation)
                    Thread.sleep(postInterval)
                    println("XXX.sendLocation:$nextLocation")
                }
                println("XXX====================================")
            } catch (_: Exception) {
                // ignore
            } finally {
                context.runOnUiThread {
                    Toast.makeText(context, "Play finished!!!", Toast.LENGTH_LONG).show()
                }
            }

        }
        ttt.start()
        thread = ttt
        return true
    }

    private fun lerp(start: Double, stop: Double, fraction: Float): Double {
        return (1 - fraction) * start + fraction * stop
    }

    private fun shakeLocation(location: LocationData): LocationData {
        val configMeter = ConfigInfo.shakeMeters
        if (configMeter == 0.0f) {
            return location
        }
        val shakeMeters = configMeter - Math.random() * configMeter * 2
        val addLat = shakeMeters * 0.0000899
        val addLng = shakeMeters * getLongitudeSpanAt(location.latitude, shakeMeters)
        return LocationData(
            latitude = location.latitude + addLat,
            longitude = location.longitude + addLng,
            time = location.time,
            speed = location.speed,
            bearing = location.bearing,
            accuracy = location.accuracy,
            altitude = location.altitude
        )
    }

    private fun getLongitudeSpanAt(latitude: Double, meters: Double): Double {
        val latitudeInRadians = latitude * PI / 180

        val metersPerLongitudeDegreeAtEquator = 111320.0
        val longitudeInMeters =
            meters / (cos(latitudeInRadians) * metersPerLongitudeDegreeAtEquator)

        return longitudeInMeters
    }

    private fun fetchNextLocation(): LocationData? {
        val pastTime = ((System.currentTimeMillis() - startTime) * ConfigInfo.speed).toLong()
        val matchedTime = firstTime + pastTime
        var from: LocationData? = null
        var to: LocationData? = null
        var distance = 0.0
        var previous: LocationData? = null
        for (item in baseList) {
            if (previous != null) {
                distance += LocationUtils.distance(previous.latitude, previous.longitude, item.latitude, item.longitude)
            }
            if (item.time <= matchedTime) {
                from = item
            }
            if (item.time > matchedTime) {
                to = item
                break
            }
            previous = item
        }
        if (from != null && to != null) {
            playModel.matchedProgress = distance / totalDistance
            val fraction = (matchedTime - from.time).toFloat() / (to.time - from.time).toFloat()
            return LocationData(
                latitude = lerp(from.latitude, to.latitude, fraction),
                longitude = lerp(from.longitude, to.longitude, fraction),
                time = System.currentTimeMillis(),
                speed = lerp(from.speed, to.speed, fraction),
                bearing = lerp(from.bearing, to.bearing, fraction),
                accuracy = lerp(from.accuracy, to.accuracy, fraction),
                altitude = lerp(from.altitude, to.altitude, fraction)
            ).let { shakeLocation(it) }
        }
        playModel.matchedProgress = 1.0
        return null
    }

    private fun postLocation(item: LocationData) {
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
    }

    @SuppressLint("WrongConstant")
    private fun prepare(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            Toast.makeText(context, "Unable to obtain location service!!!", Toast.LENGTH_SHORT).show()
            return false
        }
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
            if (mustMock) {
                return false
            }
        }
        return true
    }

    fun stop() {
        currentLocationManager?.removeTestProvider(provider)
        currentLocationManager = null;
        currentRunning = false
        thread?.interrupt()
        thread = null
    }

}