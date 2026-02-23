package com.example.smartbikeapplication.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class BluetoothClient {

    private val uuid: UUID =
        UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")

    private var socket: BluetoothSocket? = null

    suspend fun connect(macAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)

            socket = device.createRfcommSocketToServiceRecord(uuid)
            adapter.cancelDiscovery()

            socket?.connect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun readLine(): String? = withContext(Dispatchers.IO) {
        try {
            val reader = BufferedReader(InputStreamReader(socket?.inputStream))
            reader.readLine()
        } catch (e: Exception) {
            null
        }
    }

    fun close() {
        socket?.close()
    }
}