package com.example.smartbikeapplication.ui.sensors

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SensorsScreen(
    onOpenMap: () -> Unit
) {
    Column {
        Text("Sensor values")
        Button(onClick = onOpenMap) {
            Text("Open map")
        }
    }
}
