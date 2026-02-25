package com.example.smartbikeapplication.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.io.OutputStreamWriter
import java.util.UUID
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class BluetoothClient {

    private val uuid: UUID =
        UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")

    private var socket: BluetoothSocket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null

    suspend fun connect(context: Context, macAddress: String): Boolean =
        withContext(Dispatchers.IO) {
            // BLUETOOTH_CONNECT is a runtime permission only on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("BT", "BLUETOOTH_CONNECT permission not granted")
                return@withContext false
            }

            val adapter = BluetoothAdapter.getDefaultAdapter()
            val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)

            // cancelDiscovery needs BLUETOOTH_SCAN on API31+, guard it
            try { adapter.cancelDiscovery() } catch (_: Exception) {}

            // Attempt 1: reflection on channel 1 — most reliable for RPi
            // Pi logs show "RFCOMM channel: 1", so this should connect directly
            try {
                val method = device.javaClass.getMethod("createRfcommSocket", Int::class.java)
                val sock = method.invoke(device, 1) as BluetoothSocket
                if (trySocket(sock)) {
                    Log.d("BT", "Connected via reflection ch1")
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w("BT", "Reflection ch1 failed: ${e.message}")
            }

            // Attempt 2: insecure UUID (SDP lookup, no encryption required)
            try {
                val sock = device.createInsecureRfcommSocketToServiceRecord(uuid)
                if (trySocket(sock)) {
                    Log.d("BT", "Connected via insecure UUID")
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w("BT", "Insecure UUID failed: ${e.message}")
            }

            // Attempt 3: secure UUID
            try {
                val sock = device.createRfcommSocketToServiceRecord(uuid)
                if (trySocket(sock)) {
                    Log.d("BT", "Connected via secure UUID")
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w("BT", "Secure UUID failed: ${e.message}")
            }

            Log.e("BT", "All connection attempts failed for $macAddress")
            false
        }

    private fun trySocket(sock: BluetoothSocket): Boolean {
        return try {
            sock.connect()
            socket = sock
            reader = BufferedReader(InputStreamReader(sock.inputStream))
            writer = PrintWriter(OutputStreamWriter(sock.outputStream), true)
            true
        } catch (e: Exception) {
            Log.w("BT", "trySocket failed: ${e.message}")
            try { sock.close() } catch (_: Exception) {}
            false
        }
    }

    suspend fun readLine(): String? = withContext(Dispatchers.IO) {
        try {
            reader?.readLine()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendCommand(json: String) = withContext(Dispatchers.IO) {
        try {
            writer?.println(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
        } catch (_: Exception) {
        }
    }
}
