package com.example.smartbikeapplication.data.model

data class SensorResponse(
    val timestamp: Long,
    val gps: Gps,
    val speed: Speed,
    val system: SystemState
)

data class Gps(
    val lat: Double,
    val lon: Double,
    val satellites: Int
)

data class Speed(
    val current: Double,
    val average: Double,
    val max: Double
)

data class SystemState(
    val headlight: Boolean,
    val turn_signal: String,
    val light_level: Int
)