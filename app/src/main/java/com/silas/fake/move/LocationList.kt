package com.silas.fake.move

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationList(navController: NavController, viewModel: LocationViewModel) {
    val context = LocalContext.current
    val list = remember { mutableStateListOf<LocItem>() }
    val localFileList = remember { mutableStateListOf<LocItem>() }

    LaunchedEffect(Unit) {
        localFileList.addAll(MyLocationManager.listFiles(context).map { LocItem(it) })
        if (viewModel.currentIndex == 0) {
            list.addAll(localFileList)
        } else {
            list.addAll(MyLocationManager.listSDCardFiles(context).map { LocItem(it) })
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location List") },
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

                if (list.isEmpty()) {
                    Text(
                        fontSize = 24.sp,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(20.dp),
                        text = "No record found, you should record the routes first"
                    )
                } else {
                    ShowList(list, viewModel, navController)
                }
                TabRow(
                    selectedTabIndex = viewModel.currentIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Tab(
                        selected = viewModel.currentIndex == 0,
                        onClick = {
                            viewModel.currentIndex = 0
                            list.clear()
                            list.addAll(localFileList)
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text("Internal")
                    }
                    Tab(
                        selected = viewModel.currentIndex == 1,
                        onClick = {
                            viewModel.currentIndex = 1
                            list.clear()
                            list.addAll(MyLocationManager.listSDCardFiles(context).map { LocItem(it) }.toList())
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text("SDCard")
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShowList(list: SnapshotStateList<LocItem>, viewModel: LocationViewModel, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var expandedIndex by remember { mutableIntStateOf(-1) }
    val confirmModel = remember {
        ConfirmDialogViewModel().apply {
            title = "Confirm Delete"
            message = "Are you sure to delete this record?"
        }
    }
    ConfirmDialog(confirmModel)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 44.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),

        ) {
        itemsIndexed(list) { index, item ->

            val interactionSource = remember { MutableInteractionSource() }
            var isPressed by remember { mutableStateOf(false) }

            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> isPressed = true
                        is PressInteraction.Release, is PressInteraction.Cancel -> isPressed =
                            false
                    }
                }
            }
            LaunchedEffect(item) {
                if (item.loading) {
                    item.load()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(if (isPressed) Color.LightGray else Color.White)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(),
                        onClick = {
                            expandedIndex = index
                        },
                    )
                    .padding(8.dp),

                ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.file.name)
                    Spacer(modifier = Modifier.weight(1f))
                    if (item.loading) {
                        Text("loading...")
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .align(Alignment.CenterVertically),
                            horizontalAlignment = Alignment.End,
                        ) {
                            Text(item.duration, style = TextStyle(fontSize = 12.sp, color = Color.Gray))
                            Text("${item.distance} m", style = TextStyle(fontSize = 12.sp, color = Color.Gray))
                        }
                    }

                    if (expandedIndex == index) {
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = { expandedIndex = -1 }
                        ) {
                            DropdownMenuItem(
                                onClick = {

                                    coroutineScope.launch {
                                        expandedIndex = -1
                                        val locationList = MyLocationManager.loadFromFile(item.file)
                                        viewModel.setLocationList(locationList)
                                        viewModel.traceLast = true
                                        navController.navigate("replay")
                                    }

                                },
                                text = { Text("Play") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    coroutineScope.launch {
                                        expandedIndex = -1
                                        viewModel.traceLast = false
                                        viewModel.setLocationList(
                                            MyLocationManager.loadFromFile(item.file)
                                        )
                                        navController.navigate("drawLocations")
                                    }
                                },
                                text = { Text("View") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    coroutineScope.launch {
                                        expandedIndex = -1
                                        val fileUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            item.file
                                        )
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "application/octet-stream"
                                            putExtra(Intent.EXTRA_STREAM, fileUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }

                                        context.startActivity(
                                            Intent.createChooser(
                                                shareIntent,
                                                "Share locFile"
                                            )
                                        )
                                        viewModel.traceLast = false
                                        viewModel.setLocationList(
                                            MyLocationManager.loadFromFile(item.file)
                                        )
                                        navController.navigate("drawLocations")
                                    }
                                },
                                text = { Text("Share") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    coroutineScope.launch {
                                        expandedIndex = -1
                                        val file = list[index].file
                                        confirmModel.message = "Are you sure to delete '${file.name}'?"
                                        confirmModel.okCallback = {
                                            coroutineScope.launch {
                                                file.delete()
                                                list.removeAt(index)
                                            }
                                        }
                                        confirmModel.showConfirmationDialog = true
                                    }
                                },
                                text = { Text("Delete") }
                            )
                        }
                    }
                }
            }
        }
    }
}

class LocItem(val file: File) {
    lateinit var duration: String
    lateinit var distance: String
    var loading by mutableStateOf(true)

    suspend fun load() {
        try {
            val dd = withContext(Dispatchers.IO) {
                val locationList = MyLocationManager.loadFromFile(file)
                val time = locationList.last().time - locationList.first().time
                duration = time.toTime()
                distance = LocationUtils.totalDistance(locationList).round(2)
                true
            }
            println("load result=$dd")
        } finally {
            loading = false
        }

    }

}