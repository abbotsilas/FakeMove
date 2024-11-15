package com.silas.fake.move

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ConfirmDialogViewModel : ViewModel() {
    var showConfirmationDialog by mutableStateOf(false)
    var value by mutableStateOf<Any?>(null)
    var title by mutableStateOf("Are you sure")
    var message by mutableStateOf("Kaka")
    var okCallback by mutableStateOf<() -> Unit>({})
    var cancelCallback by mutableStateOf<() -> Unit>({})
}

@Composable
fun ConfirmDialog(viewModel: ConfirmDialogViewModel) {
    if (viewModel.showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showConfirmationDialog = false },
            title = { Text(viewModel.title) },
            text = { Text(viewModel.message) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.okCallback()
                    viewModel.showConfirmationDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.cancelCallback.invoke()
                    viewModel.showConfirmationDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

}