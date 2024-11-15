package com.silas.fake.move

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController) {
    val context = LocalContext.current
    var speed by remember { mutableFloatStateOf(ConfigInfo.speed) }
    var shakeMeters by remember { mutableFloatStateOf(ConfigInfo.shakeMeters) }
    var postInterval by remember { mutableLongStateOf(ConfigInfo.postInterval) }
    var changed by remember { mutableStateOf(false) }
    val confirmModel = remember {
        ConfirmDialogViewModel().apply {
            title = "Some changed"
            message = "You made some changes, do you want to leave without save it ?"
            okCallback = {
                navController.popBackStack()
            }
        }
    }
    BackHandler {
        if (changed) {
            confirmModel.showConfirmationDialog = true
        } else {
            navController.popBackStack()
        }
    }
    ConfirmDialog(confirmModel)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(start = 20.dp, end = 20.dp)

            ) {
                Text("Adjust following settings should be carefully...")
                Spacer(Modifier.height(16.dp))
                Text("Speed:${speed.round(2)}")
                Text(
                    color = Color.Red,
                    style = TextStyle(
                        fontSize = 12.sp
                    ),
                    text = "The speed value is the multiples of the real speed as recording. " +
                            "So 1.0 is the same speed of recording speed."
                )
                Slider(
                    value = speed * 10,
                    onValueChange = {
                        changed = true
                        speed = it / 10f
                    },
                    valueRange = 1f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Shake meters:${shakeMeters.round(2)}")
                Text(
                    color = Color.Red,
                    style = TextStyle(
                        fontSize = 12.sp
                    ),
                    text = "The shake meters value is when replay, the distance deviate the original route."
                )
                Slider(
                    value = shakeMeters * 10,
                    onValueChange = {
                        changed = true
                        shakeMeters = it / 10f
                    },
                    valueRange = 0f..10f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Update location interval:${postInterval} ms")
                Text(
                    color = Color.Red,
                    style = TextStyle(
                        fontSize = 12.sp
                    ),
                    text = "The interval less the update rate will be faster"
                )
                Slider(
                    value = postInterval / 100f,
                    onValueChange = {
                        changed = true
                        postInterval = (it * 100).toLong()
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        enabled = changed,
                        onClick = {
                            changed = false
                            ConfigInfo.speed = speed
                            ConfigInfo.shakeMeters = shakeMeters
                            ConfigInfo.postInterval = postInterval
                            ConfigInfo.save(context)
                        }) {
                        Text("Save")
                    }
                    Button(
                        enabled = changed,
                        onClick = {
                            changed = false
                            speed = ConfigInfo.speed
                            shakeMeters = ConfigInfo.shakeMeters
                            postInterval = ConfigInfo.postInterval
                        }) {
                        Text("Reset")
                    }
                }

            }
        }
    )
}


