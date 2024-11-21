package com.silas.fake.move

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LocationList(navController: NavController, viewModel: LocationViewModel) {
    val context = LocalContext.current
    val list = remember { mutableStateListOf<File>() }

    val coroutineScope = rememberCoroutineScope()

    var expandedIndex by remember { mutableIntStateOf(-1) }

    val confirmModel = remember {
        ConfirmDialogViewModel().apply {
            title = "Confirm Delete"
            message = "Are you sure to delete this record?"
        }
    }

    LaunchedEffect(Unit) { list.addAll(MyLocationManager.listFiles(context)) }

    ConfirmDialog(confirmModel)

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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
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

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
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
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = item.name)
                                    if (expandedIndex == index) {
                                        DropdownMenu(
                                            expanded = true,
                                            onDismissRequest = { expandedIndex = -1 }
                                        ) {
                                            DropdownMenuItem(
                                                onClick = {

                                                    coroutineScope.launch {
                                                        expandedIndex = -1
                                                        val locationList = MyLocationManager.loadFromFile(item)
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
                                                            MyLocationManager.loadFromFile(item)
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
                                                            item
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
                                                            MyLocationManager.loadFromFile(item)
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
                                                        val file = list[index]
                                                        confirmModel.message = "Are you sure to delete '${file.name}'?"
                                                        confirmModel.okCallback = {
                                                            coroutineScope.launch {
                                                                file.delete()
                                                                list.remove(file)
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
            }
        }
    )
}
