package com.silas.fake.move

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LocationPermissionViewModel : ViewModel() {
    var hasLocationPermission by mutableStateOf(false)
        private set

    fun updatePermission(hasPermission: Boolean) {
        hasLocationPermission = hasPermission
    }
}