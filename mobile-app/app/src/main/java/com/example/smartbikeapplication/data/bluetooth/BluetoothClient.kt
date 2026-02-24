package com.example.smartbikeapplication.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class BluetoothClient {

    private val uuid: UUID =
        UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")

    private var socket: BluetoothSocket? = null
    private var reader: BufferedReader? = null

    suspend fun connect(context: Context, macAddress: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@withContext false
                }

                val adapter = BluetoothAdapter.getDefaultAdapter()
                val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)

                adapter.cancelDiscovery()

                println("🔥 creating RFCOMM socket via UUID")

                socket = device.createRfcommSocketToServiceRecord(uuid)
                socket?.connect()
                reader = BufferedReader(InputStreamReader(socket!!.inputStream))

                println("✅ RFCOMM connected")
                true

            } catch (e: Exception) {
                e.printStackTrace()
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

    fun close() {
        try {
            reader?.close()
            socket?.close()
        } catch (_: Exception) {
        }
    }
}