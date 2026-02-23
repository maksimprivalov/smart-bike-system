package com.example.smartbikeapplication

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartbikeapplication.ui.main.SensorViewModel
import com.example.smartbikeapplication.ui.map.MapScreen
import com.example.smartbikeapplication.ui.navigation.Routes
import com.example.smartbikeapplication.ui.sensors.SensorsScreen
import com.example.smartbikeapplication.ui.theme.SmartBikeApplicationTheme

class MainActivity : ComponentActivity() {

    private val bluetoothPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val granted = perms.values.all { it }
            if (granted) {
                val vm = SensorViewModel()
                vm.startBluetooth(
                    applicationContext,
                    "2C:CF:67:20:C9:E0"
                )

            } else {
                Log.e("BT", "Permissions denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestBtPermissions()

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

    private fun requestBtPermissions() {
        val perms = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        bluetoothPermissionLauncher.launch(perms)
    }
}
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    SmartBikeApplicationTheme {
//        Greeting("Android")
//    }
//}