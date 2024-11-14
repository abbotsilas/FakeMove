package com.silas.fake.move

import LatLng
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class LocationViewModel : ViewModel() {
    var itemList = mutableStateListOf<LatLng>()
        private set

    fun clear() {
        itemList.clear()
    }

    fun addItem(item: LatLng) {
        itemList.add(item)
    }

    fun setItemList(list: List<LatLng>) {
        itemList.clear();
        itemList.addAll(list);
    }
}