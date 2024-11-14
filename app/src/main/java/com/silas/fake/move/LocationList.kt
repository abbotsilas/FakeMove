package com.silas.fake.move

import LatLng
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
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LocationList(navController: NavController, viewModel: LocationViewModel) {
    val context = LocalContext.current
    val list = remember { mutableStateListOf<File>() }

//    val list = List(20) { "Item #$it" }
    val coroutineScope = rememberCoroutineScope()

    var expandedIndex by remember { mutableIntStateOf(-1) }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var actionType by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { list.addAll(MyLocationManager.listFiles(context)) }

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
                                                }
                                            },
                                            text = { Text("Play") }
                                        )
                                        DropdownMenuItem(
                                            onClick = {
                                                coroutineScope.launch {
                                                    expandedIndex = -1
                                                    viewModel.setItemList(
                                                        MyLocationManager.loadFromFile(
                                                            context,
                                                            item
                                                        ).map {
                                                            LatLng(it.latitude, it.longitude)
                                                        }
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
                                                    selectedIndex = index
                                                    actionType = "Delete"
                                                    showConfirmationDialog = true
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
    )

    if (showConfirmationDialog) {
        val file = list[selectedIndex]
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm $actionType") },
            text = { Text("Are you sure you want to $actionType '${file.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        file.delete()
                        list.remove(file)
                    }
                    showConfirmationDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmationDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}