package com.example.smartbikeapplication.data.api

import com.example.smartbikeapplication.data.model.SensorResponse
import retrofit2.http.GET

interface SensorApi {

    @GET("api/sensors")
    suspend fun getSensors(): SensorResponse
}