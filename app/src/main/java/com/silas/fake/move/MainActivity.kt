@file:OptIn(ExperimentalMaterial3Api::class)

package com.silas.fake.move

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.silas.fake.move.ui.theme.FakeMoveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = LocationPermissionViewModel()
        viewModel.updatePermission(MyLocationManager.hasPermission(this))
        setContent {
            FakeMoveTheme {
                MyAppNavigation(viewModel)
            }
        }
    }


}

@Composable
fun MyAppNavigation(viewModel: LocationPermissionViewModel) {
    val navController = rememberNavController()
    val sharedLocationViewModel = LocationViewModel()
    NavHost(
        navController = navController,
        startDestination = "main",
    ) {

        composable("main") {
            Greeting(
                name = "Android",
                viewModel = viewModel,
                navController = navController
            )
        }
        composable("record") { RecordingScreen(navController, sharedLocationViewModel) }
        composable("play") { LocationList(navController, sharedLocationViewModel) }
        composable("drawLocations") { DrawLocations(navController, sharedLocationViewModel) }
    }
}


@Composable
fun RequestLocationPermissionScreen(viewModel: LocationPermissionViewModel) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        viewModel.updatePermission(isGranted)
    }

    Column(
        modifier = Modifier
            .defaultMinSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Button(
            onClick = {
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            enabled = !viewModel.hasLocationPermission
        ) {
            Text(text = if (viewModel.hasLocationPermission) "Permission Approved" else "Request Permission")
        }
    }
}

@Composable
fun Greeting(name: String, viewModel: LocationPermissionViewModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = "Hello $name!",
        )
        RequestLocationPermissionScreen(viewModel)
        Button(
            enabled = viewModel.hasLocationPermission,
            onClick = {
                navController.navigate("record")
            },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(text = "Record")
        }
        Button(
            enabled = viewModel.hasLocationPermission,
            onClick = {
                navController.navigate("play")
            },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(text = "Play")
        }
        Button(
            enabled = viewModel.hasLocationPermission,
            onClick = {},
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(text = "Settings")
        }
    }

}

@Composable
fun InputDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() }, // Called when the user tries to dismiss the dialog
        confirmButton = {
            TextButton(onClick = {
                onConfirm(inputText) // Pass the input data back to the caller
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        title = {
            Text(text = "Enter File Name")
        },
        text = {
            // Input text field in the dialog
            Column {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("The File Name") }
                )
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FakeMoveTheme {
        Greeting(
            "Preview",
            viewModel = LocationPermissionViewModel(),
            navController = rememberNavController()
        )
    }
}