package com.silas.fake.move

import LatLng
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class LocationViewModel : ViewModel() {
    private val _itemList = mutableStateOf<List<LatLng>>(emptyList())
    val itemList: State<List<LatLng>> get() = _itemList

    fun setItemList(list: List<LatLng>) {
        _itemList.value = list
    }
}