package com.example.smartbikeapplication.data.repository

import android.content.Context
import com.example.smartbikeapplication.data.bluetooth.BluetoothClient
import com.example.smartbikeapplication.data.model.SensorResponse
import com.google.gson.Gson

class BluetoothRepository(
    private val context: Context
) {

    private val client = BluetoothClient()
    private val gson = Gson()

    suspend fun connect(mac: String): Boolean {
        return client.connect(context, mac)
    }

    suspend fun readSensors(): SensorResponse? {
        val line = client.readLine() ?: return null
        return try {
            gson.fromJson(line, SensorResponse::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun sendCommand(json: String) = client.sendCommand(json)

    fun close() = client.close()
}
