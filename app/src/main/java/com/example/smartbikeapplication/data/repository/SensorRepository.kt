package com.example.smartbikeapplication.data.repository

import com.example.smartbikeapplication.data.api.ApiClient
import com.example.smartbikeapplication.data.model.SensorResponse

class SensorRepository {

    suspend fun loadSensors(): SensorResponse {
        return ApiClient.api.getSensors()
    }
}