package com.example.smartbikeapplication.ui.navigation

sealed class Routes(val route: String) {
    object Sensors : Routes("sensors")
    object Map : Routes("map")
}