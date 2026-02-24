package com.example.smartbikeapplication.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

@Composable
fun MainScreen(
    viewModel: SensorViewModel
) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.startBluetooth(
            context.applicationContext,
            "2C:CF:67:20:C9:E0"
        )
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Raspberry Pi data")

        if (state == null) {
            Text(text = "Downloading...")
        } else {
            Text(text = "Speed: ${state!!.speed.current} km/h")
            Text(text = "Light level: ${state!!.system.light_level}")
            Text(text = "Lat: ${state!!.gps.lat}")
            Text(text = "Lon: ${state!!.gps.lon}")
        }
    }
}
