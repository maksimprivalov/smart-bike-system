package com.example.smartbikeapplication.data.model

data class SensorResponse(
    val timestamp: Long,
    val gps: Gps,
    val speed: Speed,
    val trip: Trip = Trip(),
    val system: SystemState
)

data class Gps(
    val lat: Double,
    val lon: Double,
    val satellites: Int,
    val fix: Boolean = false
)

data class Speed(
    val current: Double,
    val average: Double,
    val max: Double
)

data class Trip(
    val distance_km: Double = 0.0,
    val duration_sec: Long = 0
)

data class SystemState(
    val headlight: Boolean,
    val headlight_mode: String = "auto",
    val turn_signal: String,
    val light_level: Int
)
