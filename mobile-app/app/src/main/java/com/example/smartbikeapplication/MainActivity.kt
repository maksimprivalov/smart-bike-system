package com.example.smartbikeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartbikeapplication.ui.map.MapScreen
import com.example.smartbikeapplication.ui.navigation.Routes
import com.example.smartbikeapplication.ui.sensors.SensorsScreen
import com.example.smartbikeapplication.ui.theme.SmartBikeApplicationTheme

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
                            onOpenMap = { lat, lon ->
                                navController.navigate("map/$lat/$lon")
                            }
                        )
                    }

                    composable(
                        route = "map/{lat}/{lon}",
                        arguments = listOf(
                            navArgument("lat") { type = NavType.StringType },
                            navArgument("lon") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val lat = backStackEntry.arguments
                            ?.getString("lat")?.toDoubleOrNull() ?: 45.2671
                        val lon = backStackEntry.arguments
                            ?.getString("lon")?.toDoubleOrNull() ?: 19.8335

                        MapScreen(
                            lat = lat,
                            lon = lon,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
