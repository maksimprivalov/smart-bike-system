package com.example.smartbikeapplication.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbikeapplication.data.model.Gps
import com.example.smartbikeapplication.data.model.SensorResponse
import com.example.smartbikeapplication.data.model.Speed
import com.example.smartbikeapplication.data.model.SystemState
import com.example.smartbikeapplication.data.repository.BluetoothRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class BtStatus { IDLE, CONNECTING, CONNECTED, FAILED, DEMO }

class SensorViewModel : ViewModel() {

    private var repository: BluetoothRepository? = null

    private val _state = MutableStateFlow<SensorResponse?>(null)
    val state: StateFlow<SensorResponse?> = _state

    private val _status = MutableStateFlow(BtStatus.IDLE)
    val status: StateFlow<BtStatus> = _status

    // ─── Demo mode ───────────────────────────────────────────────────────────
    // Simulates all project sensors: GPS (NEO-6M), Hall sensor (speed),
    // Photoresistor + ADS1115 (light level), headlight, turn signals
    fun startDemo() {
        if (_status.value == BtStatus.DEMO) return
        _status.value = BtStatus.DEMO

        viewModelScope.launch {
            var tick = 0
            // Base route: Novi Sad (matches Pi mock coordinates)
            val baseLat = 45.2671
            val baseLon = 19.8335

            while (isActive) {
                val t = tick.toDouble()

                // Hall sensor: speed oscillates realistically ~22 km/h
                val currentSpeed = 22.4 + sin(t * 0.4) * 4.2
                val maxSpeed = 34.8

                // LDR (photoresistor via ADS1115): light level varies
                // Higher = brighter (day) → headlight OFF
                // Lower = darker → headlight ON
                val lightLevel = (680 + sin(t * 0.15) * 180).toInt()
                val headlightOn = lightLevel < 400

                // Turn signal: simulate occasional left turn around tick 10-13
                val turnSignal = when {
                    tick in 10..12 -> "left"
                    tick in 25..27 -> "right"
                    else -> "none"
                }

                // GPS: slowly moves along a route (simulates 1 sec intervals)
                val lat = baseLat + tick * 0.000045
                val lon = baseLon + tick * 0.000032

                // Satellite count: small variation 7–9
                val satellites = 7 + (tick % 3)

                _state.value = SensorResponse(
                    timestamp = System.currentTimeMillis() / 1000,
                    gps = Gps(
                        lat = lat,
                        lon = lon,
                        satellites = satellites
                    ),
                    speed = Speed(
                        current = currentSpeed,
                        average = 18.2,
                        max = maxSpeed
                    ),
                    system = SystemState(
                        headlight = headlightOn,
                        turn_signal = turnSignal,
                        light_level = lightLevel
                    )
                )

                tick++
                delay(1000)
            }
        }
    }

    // ─── Real Bluetooth mode ─────────────────────────────────────────────────
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
