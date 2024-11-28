package com.silas.fake.move

import LatLng
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LocationViewModel : ViewModel() {
    var currentIndex by mutableIntStateOf(0)
    var traceLast by mutableStateOf(false)
    var loopCount by mutableIntStateOf(1)

    var matchedProgress by mutableDoubleStateOf(Double.NaN)

    var locationList = mutableStateListOf<LocationData>()
        private set


    fun clearLocationList() {
        locationList.clear()
    }

    fun addLocationItem(item: LocationData) {
        locationList.add(item)
    }

    fun setLocationList(list: List<LocationData>) {
        locationList.clear();
        locationList.addAll(list);
    }
}