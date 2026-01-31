package com.example.smartbikeapplication.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.example.smartbikeapplication.ui.theme.SmartBikeApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartBikeApplicationTheme {
                val viewModel = remember { SensorViewModel() }
                MainScreen(viewModel)
            }
        }
    }
}
