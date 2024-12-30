package com.silas.fake.move

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(navController: NavController, viewModel: LocationViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    var text by remember { mutableStateOf("Waiting...") }
    var recording by remember { mutableStateOf(false) }
    val list = remember { mutableListOf<LocationData>() }
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val boxSize: MutableState<IntSize> = remember { mutableStateOf(IntSize(0, 0)) }

    LaunchedEffect(Unit) {
        viewModel.clearLocationList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record locations") },
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(0.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        modifier = Modifier.padding(
                            top = 0.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        text = """
                        If you want to record your location, you should make sure that
                        you have not select the "Mock location app" in developer options,
                        otherwise, the location recorded at this time will not represent
                        your actual location.
                    """.trimIndent().lines().joinToString(separator = " ")
                    )

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = {
                                selectedTabIndex = 0
                            }
                        ) {
                            Text(text = "Log")
                        }
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = {
                                selectedTabIndex = 1
                            }
                        ) {
                            Text(text = "Map")
                        }
                    }
                    when (selectedTabIndex) {
                        0 -> {
                            Text(
                                text = text,
                                softWrap = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .align(Alignment.Start)
                                    .verticalScroll(scrollState)
                                    .horizontalScroll(horizontalScroll)
                            )
                        }

                        1 -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .onSizeChanged {
                                        boxSize.value = it
                                    }
                                    .align(Alignment.Start)
                            ) {
                                val size = boxSize.value;
                                if (size.height == 0 || size.width == 0) {
                                    Text("Waiting...")
                                } else {
                                    DrawLocationView(
                                        viewModel,
                                        size.width.toFloat(),
                                        size.height.toFloat()
                                    )

                                }
                            }

                        }
                    }

                    Row(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Button(
                            enabled = !recording,
                            onClick = {
                                val success =
                                    MyLocationManager.startRecord(context, object : LocationCallback {
                                        override fun onLocationChanged(location: LocationData) {
                                            text += "\n$location"
                                            if (text.length > 10000) {
                                                text = text.substring(text.length - 10000)
                                            }
                                            list.add(location)
                                            viewModel.addLocationItem(location)
                                        }
                                    })
                                text = "start success: $success"
                                list.clear()
                                viewModel.clearLocationList()
                                recording = success
                            }) {
                            Text(text = "Start")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            enabled = recording,
                            onClick = {
                                recording = false
                                MyLocationManager.stopRecord(context)
                                text += "\nstopped"
                            }) {
                            Text(text = "Stop")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            enabled = list.isNotEmpty(),
                            onClick = {
                                showDialog = true
                            }
                        ) {
                            Text(text = "Save")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    if (showDialog) {
                        InputDialog(
                            onConfirm = { result ->
                                showDialog = false
                                coroutineScope.launch {
                                    MyLocationManager.saveToFile(context, list, result)
                                    if (snackbarHostState.currentSnackbarData == null) {
                                        snackbarHostState.showSnackbar("Saved to $result")
                                    }
                                }
                            },
                            onDismiss = {
                                showDialog = false
                            }
                        )
                    }

                }
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 0.dp),
                )
            }


        }
    )


    LaunchedEffect(text) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    DisposableEffect(Unit) {
        println("on Created")

        onDispose {
            if (recording) {
                MyLocationManager.stopRecord(context)
            }
            println("on Disposed")
        }
    }
}