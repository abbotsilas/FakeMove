package com.silas.fake.move

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PermissionViewModel : ViewModel() {
    var hasLocationPermission by mutableStateOf(false)
        private set
    var hasNotificationPermission by mutableStateOf(false)
        private set

    fun updateLocationPermission(hasPermission: Boolean) {
        hasLocationPermission = hasPermission
    }

    fun updateNotificationPermission(hasPermission: Boolean) {
        hasNotificationPermission = hasPermission
    }
}