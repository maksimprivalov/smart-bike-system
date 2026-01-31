package com.example.smartbikeapplication.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbikeapplication.data.model.SensorResponse
import com.example.smartbikeapplication.data.repository.SensorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SensorViewModel : ViewModel() {

    private val repository = SensorRepository()

    private val _state = MutableStateFlow<SensorResponse?>(null)
    val state: StateFlow<SensorResponse?> = _state

    fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    _state.value = repository.loadSensors()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(2000) // 2 seconds
            }
        }
    }
}
