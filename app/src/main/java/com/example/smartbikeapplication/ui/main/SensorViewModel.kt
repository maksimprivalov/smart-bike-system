package com.example.smartbikeapplication.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbikeapplication.data.model.SensorResponse
import com.example.smartbikeapplication.data.repository.BluetoothRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SensorViewModel : ViewModel() {

    private var repository: BluetoothRepository? = null

    private val _state = MutableStateFlow<SensorResponse?>(null)
    val state: StateFlow<SensorResponse?> = _state

    fun startBluetooth(context: Context, mac: String) {
        repository = BluetoothRepository(context)

        viewModelScope.launch {
            val connected = repository?.connect(mac) ?: false

            if (!connected) {
                println("BT CONNECT FAILED")
                return@launch
            }

            println("BT CONNECTED")

            while (isActive) {
                try {
                    val data = repository?.readSensors()
                    if (data != null) {
                        _state.value = data
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000)
            }
        }
    }
}
