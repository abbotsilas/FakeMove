package com.silas.fake.move

import AutoCenteredAndConstrainedPath
import LatLng
import SimpleLatLng
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawLocationView(viewModel: LocationViewModel, widthPx: Float, heightPx: Float, baseList: List<LocationData>? = null) {
    var computed by remember { mutableStateOf(false) }
    var centerLatLng by remember { mutableStateOf<LatLng>(SimpleLatLng(0.0, 0.0)) }
    var initialScale by remember { mutableFloatStateOf(0f) }
    val latLngList = viewModel.locationList

    println("DrawLocationView.widthPx=$widthPx, heightPx=$heightPx")

    LaunchedEffect(Unit) {
        computed = true
        if (!baseList.isNullOrEmpty()) {
            centerLatLng = calculateCenter(baseList)
            initialScale = calculateAdaptiveScale(baseList, widthPx, heightPx)
            println("XXXX.initialScale.baseList=$initialScale")
        } else if (latLngList.isNotEmpty()) {
            centerLatLng = calculateCenter(latLngList)
            initialScale = calculateAdaptiveScale(latLngList, widthPx, heightPx)
            println("XXXX.initialScale.latLngList=$initialScale")
        } else {
            println("DrawLocationView.LaunchedEffect.lost")
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (computed) {
            AutoCenteredAndConstrainedPath(
                centerLatLng,
                initialScale,
                viewModel,
                baseList
            )
        } else {
            Text("Computing...")
        }

    }
}

private fun calculateCenter(latLngList: List<LatLng>): LatLng {
    val avgLat = latLngList.map { it.latitude }.average()
    val avgLon = latLngList.map { it.longitude }.average()
    return SimpleLatLng(avgLat, avgLon)
}

private fun calculateAdaptiveScale(
    latLngList: List<LatLng>,
    screenWidth: Float,
    screenHeight: Float
): Float {
    val latitudes = latLngList.map { it.latitude }
    val longitudes = latLngList.map { it.longitude }

    var latRange = latitudes.maxOrNull()!! - latitudes.minOrNull()!!
    var lonRange = longitudes.maxOrNull()!! - longitudes.minOrNull()!!

    if (latRange < 0.00005) {
        latRange = 0.00005
    }
    if (lonRange < 0.00005) {
        lonRange = 0.00005
    }

    val maxLatDistance = latRange * DRAW_SCALE_FACTOR
    val maxLonDistance = lonRange * DRAW_SCALE_FACTOR

    val scaleX = screenWidth / maxLonDistance.toFloat()
    val scaleY = screenHeight / maxLatDistance.toFloat()

    val scale = min(scaleX, scaleY) * 0.9f
    return scale
}
