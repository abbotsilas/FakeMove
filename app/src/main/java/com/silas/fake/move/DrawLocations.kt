package com.silas.fake.move

import AutoCenteredAndConstrainedPath
import LatLng
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavController
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawLocations(navController: NavController, viewModel: LocationViewModel) {
    var context = LocalContext.current
    var computed by remember { mutableStateOf(false) }
    var centerLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var initialScale by remember { mutableFloatStateOf(0f) }
    val latLngList by viewModel.itemList
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidthPx = configuration.screenWidthDp * density
    val screenHeightPx = configuration.screenHeightDp * density

    println("screenWidth=$screenWidthPx, screenHeight=$screenHeightPx")

    LaunchedEffect(Unit) {
        computed = true
        if (latLngList.isNotEmpty()) {
            centerLatLng = calculateCenter(latLngList)
            initialScale = calculateAdaptiveScale(latLngList, screenWidthPx, screenHeightPx)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DrawLocations") },
                colors = TopAppBarDefaults.topAppBarColors(),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            if (computed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AutoCenteredAndConstrainedPath(
                        centerLatLng,
                        initialScale,
                        latLngList
                    )
                }
            }
        }
    )
}

fun calculateCenter(latLngList: List<LatLng>): LatLng {
    val avgLat = latLngList.map { it.latitude }.average()
    val avgLon = latLngList.map { it.longitude }.average()
    return LatLng(avgLat, avgLon)
}

fun calculateAdaptiveScale(
    latLngList: List<LatLng>,
    screenWidth: Float,
    screenHeight: Float
): Float {
    val latitudes = latLngList.map { it.latitude }
    val longitudes = latLngList.map { it.longitude }

    var latRange = latitudes.maxOrNull()!! - latitudes.minOrNull()!!
    var lonRange = longitudes.maxOrNull()!! - longitudes.minOrNull()!!

    if (latRange < 0.00001) {
        latRange = 0.00001
    }
    if (lonRange < 0.00001) {
        lonRange = 0.00001
    }

    val maxLatDistance = latRange * 10000
    val maxLonDistance = lonRange * 10000

    val scaleX = screenWidth / maxLonDistance.toFloat()
    val scaleY = screenHeight / maxLatDistance.toFloat()

    return min(scaleX, scaleY) * 0.9f
}
