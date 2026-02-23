package com.example.smartbikeapplication.ui.sensors

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartbikeapplication.ui.main.SensorViewModel

@Composable
fun SensorsScreen(
    onOpenMap: () -> Unit
) {
    val viewModel: SensorViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.startBluetooth(
            context.applicationContext,
            "2C:CF:67:20:C9:E0"
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Sensor values")

        if (state == null) {
            Text("Waiting for Raspberry...")
        } else {
            Text("Speed: ${state!!.speed.current}")
            Text("Light: ${state!!.system.light_level}")
            Text("Lat: ${state!!.gps.lat}")
        }

        Button(onClick = onOpenMap) {
            Text("Open map")
        }
    }
}