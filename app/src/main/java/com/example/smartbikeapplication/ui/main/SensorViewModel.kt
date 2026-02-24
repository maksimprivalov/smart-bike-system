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

enum class BtStatus { IDLE, CONNECTING, CONNECTED, FAILED }

class SensorViewModel : ViewModel() {

    private var repository: BluetoothRepository? = null

    private val _state = MutableStateFlow<SensorResponse?>(null)
    val state: StateFlow<SensorResponse?> = _state

    private val _status = MutableStateFlow(BtStatus.IDLE)
    val status: StateFlow<BtStatus> = _status

    fun startBluetooth(context: Context, mac: String) {
        if (_status.value == BtStatus.CONNECTING || _status.value == BtStatus.CONNECTED) return

        repository = BluetoothRepository(context)
        _status.value = BtStatus.CONNECTING

        viewModelScope.launch {
            val connected = repository?.connect(mac) ?: false

            if (!connected) {
                _status.value = BtStatus.FAILED
                return@launch
            }

            _status.value = BtStatus.CONNECTED

            while (isActive) {
                try {
                    val data = repository?.readSensors()
                    if (data != null) {
                        _state.value = data
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _status.value = BtStatus.FAILED
                    break
                }
                delay(1000)
            }
        }
    }
}
