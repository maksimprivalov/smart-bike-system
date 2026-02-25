package com.example.smartbikeapplication.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbikeapplication.data.model.Gps
import com.example.smartbikeapplication.data.model.SensorResponse
import com.example.smartbikeapplication.data.model.Speed
import com.example.smartbikeapplication.data.model.SystemState
import com.example.smartbikeapplication.data.model.Trip
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

    // Emits true when connected but no data received for >3 seconds
    private val _signalLost = MutableStateFlow(false)
    val signalLost: StateFlow<Boolean> = _signalLost

    private var lastDataTime = 0L

    // ─── Demo mode ───────────────────────────────────────────────────────────
    fun startDemo() {
        if (_status.value == BtStatus.DEMO) return
        _status.value = BtStatus.DEMO

        viewModelScope.launch {
            var tick = 0
            val baseLat = 45.2671
            val baseLon = 19.8335

            while (isActive) {
                val t = tick.toDouble()

                val currentSpeed = 22.4 + sin(t * 0.4) * 4.2
                val maxSpeed = 34.8

                val lightLevel = (680 + sin(t * 0.15) * 180).toInt()
                val headlightOn = lightLevel < 400

                val turnSignal = when {
                    tick in 10..12 -> "left"
                    tick in 25..27 -> "right"
                    else -> "none"
                }

                val lat = baseLat + tick * 0.000045
                val lon = baseLon + tick * 0.000032
                val satellites = 7 + (tick % 3)

                _state.value = SensorResponse(
                    timestamp = System.currentTimeMillis() / 1000,
                    gps = Gps(
                        lat = lat,
                        lon = lon,
                        satellites = satellites,
                        fix = true
                    ),
                    speed = Speed(
                        current = currentSpeed,
                        average = 18.2,
                        max = maxSpeed
                    ),
                    trip = Trip(
                        distance_km = tick * 0.0062,
                        duration_sec = tick.toLong()
                    ),
                    system = SystemState(
                        headlight = headlightOn,
                        headlight_mode = "auto",
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
    private var lastContext: Context? = null
    private var lastMac: String? = null

    fun retry() {
        val ctx = lastContext ?: return
        val mac = lastMac ?: return
        repository?.close()
        repository = null
        _status.value = BtStatus.IDLE
        startBluetooth(ctx, mac)
    }

    fun startBluetooth(context: Context, mac: String) {
        if (_status.value == BtStatus.CONNECTING || _status.value == BtStatus.CONNECTED) return

        lastContext = context
        lastMac = mac
        repository = BluetoothRepository(context)
        _status.value = BtStatus.CONNECTING

        viewModelScope.launch {
            val connected = repository?.connect(mac) ?: false

            if (!connected) {
                _status.value = BtStatus.FAILED
                return@launch
            }

            _status.value = BtStatus.CONNECTED
            lastDataTime = System.currentTimeMillis()
            _signalLost.value = false

            // Watchdog: checks every second if data stopped arriving
            val watchdog = launch {
                while (isActive) {
                    delay(1000)
                    if (_status.value == BtStatus.CONNECTED) {
                        _signalLost.value = System.currentTimeMillis() - lastDataTime > 3_000
                    }
                }
            }

            while (isActive) {
                try {
                    val data = repository?.readSensors()
                    if (data != null) {
                        _state.value = data
                        lastDataTime = System.currentTimeMillis()
                        _signalLost.value = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _signalLost.value = false
                    _status.value = BtStatus.FAILED
                    break
                }
                delay(1000)
            }

            watchdog.cancel()
        }
    }

    // ─── Commands to Raspberry Pi ─────────────────────────────────────────────
    fun sendTurnLeft()  = sendCmd("""{"action":"turn_left"}""")
    fun sendTurnRight() = sendCmd("""{"action":"turn_right"}""")
    fun sendTurnOff()   = sendCmd("""{"action":"turn_off"}""")
    fun sendHorn()      = sendCmd("""{"action":"horn"}""")
    fun sendHeadlightMode(mode: String) = sendCmd("""{"action":"headlight","mode":"$mode"}""")

    private fun sendCmd(json: String) {
        viewModelScope.launch {
            repository?.sendCommand(json)
        }
    }
}
