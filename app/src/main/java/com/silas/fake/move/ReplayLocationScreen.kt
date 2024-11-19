package com.silas.fake.move

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplayLocationScreen(navController: NavController, viewModel: LocationViewModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidthPx = configuration.screenWidthDp * density
    val screenHeightPx = configuration.screenHeightDp * density
    val baseList = viewModel.locationList.toList()
    val playModel = remember { LocationViewModel().apply { traceLast = viewModel.traceLast } }
    var computed by remember { mutableStateOf(false) }
    val player = remember {
        LocationPlayer(
            context = context,
            baseList = baseList,
            playModel = playModel,
            mustMock = false
        )
    }

    fun doBack() {
        context.stopService(Intent(context, MockLocationService::class.java))
        navController.popBackStack()
    }

    val confirmModel = remember {
        ConfirmDialogViewModel().apply {
            title = "Stop playing?"
            message = "Are you sure you want to stop playing and exit?"
            okCallback = {
                doBack()
            }
        }
    }
    LaunchedEffect(Unit) {
        val success = player.start(context as Activity)
        if (!success) {
            navController.popBackStack()

        }
        computed = success
    }
    if (!computed) {
        return
    }
    BackHandler(enabled = true) {
        confirmModel.showConfirmationDialog = true
    }
    ConfirmDialog(confirmModel)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Replay locations") },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                DrawLocationView(playModel, screenWidthPx, screenHeightPx, baseList)

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 20.dp),
                ) {
                    Button(
                        onClick = {
                            context.startService(Intent(context, MockLocationService::class.java))
                            (context as? MainActivity)?.moveTaskToBack(true)
                        }

                    ) { Text("Run at background") }
                    Button(
                        onClick = {
                            doBack()
                        }

                    ) { Text("Stop") }
                }

            }
        }
    )
    DisposableEffect(Unit) {
        onDispose {
            player.stop()
            println("on Disposed")
        }
    }
}