package com.silas.fake.move

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.silas.fake.move.ui.theme.FakeMoveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController) {
    val context = LocalContext.current
    var speed by remember { mutableFloatStateOf(ConfigInfo.speed) }
    var shakeMeters by remember { mutableFloatStateOf(ConfigInfo.shakeMeters) }
    var postInterval by remember { mutableLongStateOf(ConfigInfo.postInterval) }
    var changed by remember { mutableStateOf(false) }
    var loopCount by remember { mutableStateOf(ConfigInfo.loopCycleCount.toString()) }
    var loopDistance by remember { mutableIntStateOf(ConfigInfo.loopDistance) }

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
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 40.dp),

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
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "In order to enable to loop cycle, " +
                            "the distance between start point to end" +
                            " point must be less than 10 meters.",
                    color = Color.Red,
                    style = TextStyle(
                        fontSize = 12.sp
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Loop cycle")
                    OutlinedTextField(
                        modifier = Modifier.padding(10.dp),
                        value = loopCount,
                        onValueChange = { newText ->
                            val newCount = newText.toIntOrNull()
                            if (newText.isBlank() || (newCount != null && newCount in 1..100000)) {
                                loopCount = newText
                                changed = true
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("From 1 to 100000") },
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Loop distance:$loopDistance meters")
                Text(
                    color = Color.Red,
                    style = TextStyle(
                        fontSize = 12.sp
                    ),
                    text = "The max distance when loop enabled, otherwise the loop will be restrict to once"
                )
                Slider(
                    value = loopDistance.toFloat(),
                    onValueChange = {
                        changed = true
                        loopDistance = it.toInt()
                    },
                    valueRange = 10f..1000f,
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
                            ConfigInfo.loopCycleCount = loopCount.toIntOrNull() ?: 1
                            ConfigInfo.loopDistance = loopDistance
                            ConfigInfo.save(context)
                        }) {
                        Text("Save")
                    }
                    Button(
                        onClick = {
                            changed = true
                            speed = 1.0f
                            shakeMeters = 0.1f
                            postInterval = 200
                            loopCount = "1"
                            loopDistance = 10
                        }) {
                        Text("Reset")
                    }
                }

            }
        }
    )
}

@Preview
@Composable
fun SettingScreenPreview() {
    FakeMoveTheme {
        SettingScreen(rememberNavController())
    }

}
