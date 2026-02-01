package com.example.smartbikeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartbikeapplication.ui.main.MainScreen
import com.example.smartbikeapplication.ui.main.SensorViewModel
import com.example.smartbikeapplication.ui.theme.SmartBikeApplicationTheme
import com.example.smartbikeapplication.ui.map.MapScreen
import com.example.smartbikeapplication.ui.navigation.Routes
import com.example.smartbikeapplication.ui.sensors.SensorsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartBikeApplicationTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.Sensors.route
                ) {
                    composable(Routes.Sensors.route) {
                        SensorsScreen(
                            onOpenMap = {
                                navController.navigate(Routes.Map.route)
                            }
                        )
                    }
                    composable(Routes.Map.route) {
                        MapScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmartBikeApplicationTheme {
        Greeting("Android")
    }
}