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

@Composable
fun MainScreen(
    viewModel: SensorViewModel
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startAutoRefresh()
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Raspberry Pi data")

        if (state == null) {
            Text(text = "Downloading...")
        } else {
            Text(text = "Temperature: ${state!!.temperature} °C")
            Text(text = "Humidity: ${state!!.humidity} %")
            Text(text = "Pressure: ${state!!.pressure} hPa")
        }
    }
}
